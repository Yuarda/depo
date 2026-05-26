package com.yarda.wordgame
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        userId = intent.getIntExtra("USER_ID", -1)

        val btnGoToQuiz = findViewById<Button>(R.id.btnGoToQuiz)
        val btnGoToWordle = findViewById<Button>(R.id.btnGoToWordle)

        if (userId == -1) {
            Toast.makeText(this, "Kullanıcı bilgisi alınamadı!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnGoToQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("USER_ID", userId) // ID'yi Quiz'e taşıyoruz
            startActivity(intent)
        }

        btnGoToWordle.setOnClickListener {
            val intent = Intent(this, WordleActivity::class.java)
            intent.putExtra("USER_ID", userId) // ID'yi Wordle'a taşıyoruz
            startActivity(intent)
        }
        val btnGoToSettings = findViewById<Button>(R.id.btnGoToSettings)

        btnGoToSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        val btnGoToAnalysis = findViewById<Button>(R.id.btnGoToAnalysis)

        btnGoToAnalysis.setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val btnGoToAddWord = findViewById<Button>(R.id.btnGoToAddWord)

        btnGoToAddWord.setOnClickListener {
            val intent = Intent(this, AddWordActivity::class.java)
            startActivity(intent)
        }
    }

}