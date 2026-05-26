package com.yarda.wordgame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etResetUsername = findViewById<EditText>(R.id.etResetUsername)
        val etResetNewPassword = findViewById<EditText>(R.id.etResetNewPassword)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            val username = etResetUsername.text.toString().trim()
            val newPassword = etResetNewPassword.text.toString().trim()

            if (username.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestData = ForgotPasswordRequest(username, newPassword)

            lifecycleScope.launch {
                try {
                    // Başarılı (200 OK)
                    val response = RetrofitClient.apiService.forgotPassword(requestData)
                    Toast.makeText(this@ForgotPasswordActivity, "${response.mesaj} (${response.kullanici})", Toast.LENGTH_LONG).show()
                    finish()

                } catch (e: HttpException) {
                    if (e.code() == 404) {
                        Toast.makeText(this@ForgotPasswordActivity, "Kullanıcı bulunamadı!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "Sunucu Hatası: ${e.code()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ForgotPasswordActivity, "Bağlantı hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}