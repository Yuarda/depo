package com.yarda.wordgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yarda.wordgame.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val tvForgotPassword = findViewById< TextView>(R.id.tvForgotPassword)

        tvForgotPassword.setOnClickListener {
            val intent = android.content.Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        btnLogin.setOnClickListener {
            val user = UserAuth(etUsername.text.toString(), etPassword.text.toString())
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.login(user)
                    navigateToHome(response.user_id)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Hata veya yanlış bilgi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnSignup.setOnClickListener {
            val user = UserAuth(etUsername.text.toString(), etPassword.text.toString())
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.signUp(user)
                    Toast.makeText(this@MainActivity, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                    navigateToHome(response.user_id)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Kayıt Başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToHome(userId: Int) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }

}