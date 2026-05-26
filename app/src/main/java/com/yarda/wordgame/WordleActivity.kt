package com.yarda.wordgame

import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class WordleActivity : AppCompatActivity() {

    private var userId: Int = -1
    private var wordId: Int = -1
    private var wordLength: Int = 0
    private var currentRow = 0
    private val maxAttempts = 6 // Klasik Wordle 6 hak verir

    private lateinit var tvWordleInfo: TextView
    private lateinit var wordleGrid: GridLayout
    private lateinit var etWordleGuess: EditText
    private lateinit var btnSubmitGuess: Button

    private val gridCells = mutableListOf<MutableList<TextView>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wordle)

        userId = intent.getIntExtra("USER_ID", -1)

        tvWordleInfo = findViewById(R.id.tvWordleInfo)
        wordleGrid = findViewById(R.id.wordleGrid)
        etWordleGuess = findViewById(R.id.etWordleGuess)
        btnSubmitGuess = findViewById(R.id.btnSubmitGuess)

        if (userId != -1) {
            startWordleGame()
        } else {
            Toast.makeText(this, "Kullanıcı ID bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSubmitGuess.setOnClickListener {
            submitGuess()
        }
    }

    private fun startWordleGame() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.startWordle(userId)

                // Eğer seviye 7 olmuş kelime yoksa API uyarı mesajı döner
                if (response.kelime_id == 0 || response.kelime_uzunlugu == 0) {
                    tvWordleInfo.text = "Oynamak için önce bazı kelimeleri\nTamamen Öğrenilmiş (Seviye 7) yapmalısınız!"
                    etWordleGuess.isEnabled = false
                    btnSubmitGuess.isEnabled = false
                    return@launch
                }

                wordId = response.kelime_id
                wordLength = response.kelime_uzunlugu
                tvWordleInfo.text = "Kelime Uzunluğu: $wordLength harf\nKalan Hak: $maxAttempts"

                etWordleGuess.filters = arrayOf(InputFilter.LengthFilter(wordLength))

                createGrid()

            } catch (e: Exception) {
                Toast.makeText(this@WordleActivity, "Bağlantı Hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Harf sayısına göre dinamik kare kutular oluşturan fonksiyon
    // Harf sayısına ve ekran genişliğine göre dinamik boyutlandıran fonksiyon
    private fun createGrid() {
        wordleGrid.rowCount = maxAttempts
        wordleGrid.columnCount = wordLength

        // 1. Ekran genişliğini al
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // 2. Ekranın sağından ve solundan bırakılan ana boşluklar (yaklaşık 48dp)
        val horizontalPadding = 48 * displayMetrics.density
        val availableWidth = screenWidth - horizontalPadding

        // 3. Kutular arası boşluk (margin - 4dp)
        val marginInDp = 4
        val marginInPx = (marginInDp * displayMetrics.density).toInt()

        // 4. İdeal kutu boyutunu hesapla: (Kullanılabilir Genişlik - Toplam Boşluklar) / Harf Sayısı
        val totalMargins = (marginInPx * 2) * wordLength
        var cellSize = ((availableWidth - totalMargins) / wordLength).toInt()

        // 5. Kelime çok kısaysa (örn 4 harf), kutular devasa olmasın diye bir üst sınır koyalım (max 60dp)
        val maxCellSize = (60 * displayMetrics.density).toInt()
        if (cellSize > maxCellSize) {
            cellSize = maxCellSize
        }

        // 6. Kelime uzunsa içindeki yazıyı da biraz küçültelim ki sığsın
        val calculatedTextSize = if (wordLength >= 7) 18f else 24f

        for (row in 0 until maxAttempts) {
            val rowCells = mutableListOf<TextView>()
            for (col in 0 until wordLength) {
                val textView = TextView(this).apply {
                    text = ""
                    textSize = calculatedTextSize
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#D3D6DA")) // Varsayılan gri-beyaz

                    // Hesaplanan dinamik boyutları uygula
                    val params = GridLayout.LayoutParams().apply {
                        width = cellSize
                        height = cellSize
                        setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(col)
                    }
                    layoutParams = params
                }
                wordleGrid.addView(textView)
                rowCells.add(textView)
            }
            gridCells.add(rowCells)
        }
    }

    private fun submitGuess() {
        val guessText = etWordleGuess.text.toString().trim().uppercase()

        if (guessText.length != wordLength) {
            Toast.makeText(this, "Lütfen $wordLength harfli bir kelime girin!", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmitGuess.isEnabled = false // İşlem bitene kadar butonu kilitle

        val guessData = WordleGuess(word_id = wordId, guess = guessText)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.checkWordle(guessData)
                val details = response.details

                if (details != null && details.size == wordLength) {

                    // Gelen sonuca göre kutucukları renklendir
                    for (i in 0 until wordLength) {
                        val tv = gridCells[currentRow][i]
                        val detail = details[i]

                        tv.text = detail.harf.uppercase()

                        // Python'dan gelen renkleri Android renklerine çeviriyoruz
                        when (detail.renk) {
                            "green" -> tv.setBackgroundColor(Color.parseColor("#6AAA64")) // Doğru yer
                            "yellow" -> tv.setBackgroundColor(Color.parseColor("#C9B458")) // Yanlış yer
                            "grey" -> tv.setBackgroundColor(Color.parseColor("#787C7E"))  // Yok
                        }
                    }

                    currentRow++
                    etWordleGuess.text.clear()
                    tvWordleInfo.text = "Kelime Uzunluğu: $wordLength harf\nKalan Hak: ${maxAttempts - currentRow}"

                    // Kazanma veya Kaybetme Durumları
                    if (response.isWin == true) {
                        tvWordleInfo.text = "🏆 TEBRİKLER! KELİMEYİ BİLDİNİZ!"
                        tvWordleInfo.setTextColor(Color.parseColor("#6AAA64"))
                        etWordleGuess.isEnabled = false
                        btnSubmitGuess.isEnabled = false
                    } else if (currentRow >= maxAttempts) {
                        tvWordleInfo.text = "❌ OYUN BİTTİ!\nDoğru Cevap: ${response.dogru_cevap}"
                        tvWordleInfo.setTextColor(Color.parseColor("#E53935"))
                        etWordleGuess.isEnabled = false
                        btnSubmitGuess.isEnabled = false
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this@WordleActivity, "Tahmin gönderilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // Oyun bitmediyse butonu tekrar aktif et
                if (currentRow < maxAttempts && etWordleGuess.isEnabled) {
                    btnSubmitGuess.isEnabled = true
                }
            }
        }
    }
}