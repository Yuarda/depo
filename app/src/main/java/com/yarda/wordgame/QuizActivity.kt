package com.yarda.wordgame

import android.os.Bundle
import android.view.View // Ekranda nesneleri gizlemek için gerekli kütüphane
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {

    private var userId: Int = -1
    private var wordsList: List<WordResponse> = listOf()
    private var currentIndex = 0

    private lateinit var tvProgress: TextView
    private lateinit var tvEnglishWord: TextView
    private lateinit var btnOptions: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        userId = intent.getIntExtra("USER_ID", -1)

        tvProgress = findViewById(R.id.tvProgress)
        tvEnglishWord = findViewById(R.id.tvEnglishWord)

        btnOptions = listOf(
            findViewById(R.id.btnOption1),
            findViewById(R.id.btnOption2),
            findViewById(R.id.btnOption3),
            findViewById(R.id.btnOption4)
        )

        if (userId != -1) {
            loadDailyQuiz()
        } else {
            Toast.makeText(this, "Kullanıcı ID bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnOptions.forEach { button ->
            button.setOnClickListener {
                val selectedAnswer = button.text.toString()
                checkAndSubmitAnswer(selectedAnswer)
            }
        }
    }

    private fun loadDailyQuiz() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getDailyQuiz(userId)
                wordsList = response.kelimeler

                // Liste boş dönerse test zaten bugün yapılmış demektir.
                if (wordsList.isEmpty()) {
                    tvEnglishWord.text = "Bugünkü testini zaten tamamladın!\nYarın tekrar gel."
                    tvProgress.text = "Harika İlerliyorsun!"

                    // BUTONLARI EKRANDAN TAMAMEN GİZLE
                    btnOptions.forEach {
                        it.visibility = View.GONE
                    }
                } else {
                    showCurrentWord()
                }
            } catch (e: Exception) {
                Toast.makeText(this@QuizActivity, "Quiz yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCurrentWord() {
        val currentWord = wordsList[currentIndex]
        tvEnglishWord.text = currentWord.ing
        tvProgress.text = "Soru ${currentIndex + 1} / ${wordsList.size}"

        val siklar = currentWord.siklar ?: emptyList()

        for (i in btnOptions.indices) {
            if (i < siklar.size) {
                btnOptions[i].visibility = View.VISIBLE // Butonu görünür yap
                btnOptions[i].text = siklar[i]
                btnOptions[i].isEnabled = true
            } else {
                // Şık sayısı 4'ten az gelirse fazlalık butonları ekrandan gizle
                btnOptions[i].visibility = View.GONE
            }
        }
    }

    private fun checkAndSubmitAnswer(selectedAnswer: String) {
        if (currentIndex >= wordsList.size) return

        // Cevap verilince çift tıklanmasın diye butonları kilitle
        btnOptions.forEach { it.isEnabled = false }

        val currentWord = wordsList[currentIndex]
        val isCorrect = if (selectedAnswer.equals(currentWord.tr, ignoreCase = true)) 1 else 0

        val answerData = AnswerData(
            user_id = userId,
            word_id = currentWord.id,
            is_correct = isCorrect
        )

        lifecycleScope.launch {
            try {
                RetrofitClient.apiService.submitAnswer(answerData)
                if (isCorrect == 1) {
                    Toast.makeText(this@QuizActivity, "Doğru!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@QuizActivity, "Yanlış! Doğrusu: ${currentWord.tr}", Toast.LENGTH_LONG).show()
                }

                currentIndex++
                if (currentIndex < wordsList.size) {
                    showCurrentWord()
                } else {
                    finishDailyQuizSession()
                }
            } catch (e: Exception) {
                Toast.makeText(this@QuizActivity, "Cevap gönderilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                btnOptions.forEach { it.isEnabled = true }
            }
        }
    }

    private fun finishDailyQuizSession() {
        tvEnglishWord.text = "Tebrikler, bugünkü testi bitirdin!"
        tvProgress.text = "Sınav Bitti"

        btnOptions.forEach {
            it.visibility = View.GONE
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.completeDailyQuiz(userId)
                Toast.makeText(this@QuizActivity, response.mesaj, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@QuizActivity, "Sınav tamamlanamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}