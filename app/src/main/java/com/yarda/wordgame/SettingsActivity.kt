package com.yarda.wordgame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        userId = intent.getIntExtra("USER_ID", -1)

        val etWordLimit = findViewById<EditText>(R.id.etWordLimit)
        val btnSaveSettings = findViewById<Button>(R.id.btnSaveSettings)

        if (userId == -1) {
            Toast.makeText(this, "Kullanıcı bilgisi alınamadı!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSaveSettings.setOnClickListener {
            val limitText = etWordLimit.text.toString().trim()

            if (limitText.isEmpty()) {
                Toast.makeText(this, "Lütfen bir sayı girin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newLimit = limitText.toInt()

            val updateData = SettingsUpdate(user_id = userId, new_limit = newLimit)

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.updateSettings(updateData)

                    if (response.hata != null) {
                        // Backend "hata" döndürdüyse
                        Toast.makeText(this@SettingsActivity, response.hata, Toast.LENGTH_LONG).show()
                    } else {
                        // Başarılı olduysa
                        Toast.makeText(this@SettingsActivity, "${response.mesaj}. Yeni Limit: ${response.yeni_limit}", Toast.LENGTH_LONG).show()
                        finish() // Ekranı kapat ve ana menüye dön
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Bağlantı hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}