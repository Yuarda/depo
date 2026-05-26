package com.yarda.wordgame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddWordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word)

        val etEngWord = findViewById<EditText>(R.id.etEngWord)
        val etTurWord = findViewById<EditText>(R.id.etTurWord)
        val etSampleSentence = findViewById<EditText>(R.id.etSampleSentence)
        val etPicturePath = findViewById<EditText>(R.id.etPicturePath)
        val btnAddWord = findViewById<Button>(R.id.btnAddWord)

        btnAddWord.setOnClickListener {
            val engWord = etEngWord.text.toString().trim()
            val turWord = etTurWord.text.toString().trim()
            val sampleSentence = etSampleSentence.text.toString().trim()
            val picturePath = etPicturePath.text.toString().trim()

            if (engWord.isEmpty() || turWord.isEmpty()) {
                Toast.makeText(this, "İngilizce ve Türkçe alanları zorunludur!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val samplesList = if (sampleSentence.isNotEmpty()) listOf(sampleSentence) else emptyList()

            val requestData = WordCreateRequest(
                eng_word = engWord,
                tur_word = turWord,
                picture_path = picturePath,
                samples = samplesList
            )

            btnAddWord.isEnabled = false

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.addWord(requestData)
                    Toast.makeText(this@AddWordActivity, response.mesaj, Toast.LENGTH_LONG).show()

                    // Başarılı kayıttan sonra alanları temizle
                    etEngWord.text.clear()
                    etTurWord.text.clear()
                    etSampleSentence.text.clear()
                    etPicturePath.text.clear()

                } catch (e: Exception) {
                    Toast.makeText(this@AddWordActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnAddWord.isEnabled = true
                }
            }
        }
    }
}