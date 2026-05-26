package com.yarda.wordgame

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AnalysisActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Kullanıcı bilgisi alınamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchReportData()
        fetchReportChart()
    }

    private fun fetchReportData() {
        val tvTotal = findViewById<TextView>(R.id.tvTotalWords)
        val tvLearned = findViewById<TextView>(R.id.tvLearned)
        val tvInProgress = findViewById<TextView>(R.id.tvInProgress)
        val tvStruggling = findViewById<TextView>(R.id.tvStruggling)
        val tvSuccessRate = findViewById<TextView>(R.id.tvSuccessRate)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getReport(userId)

                tvTotal.text = "Toplam Etkileşim: ${response.toplam_etkilesim_kurulan_kelime}"
                tvLearned.text = "Tamamen Öğrenilen: ${response.tamamen_ogrenilen_kelime_sayisi}"
                tvInProgress.text = "Devam Eden: ${response.devam_eden_kelime_sayisi}"
                tvStruggling.text = "Zorlanılan: ${response.zorlanilan_kelime_sayisi}"
                tvSuccessRate.text = "Genel Başarı: %${response.genel_basari_yuzdesi}"

            } catch (e: Exception) {
                Toast.makeText(this@AnalysisActivity, "Veriler alınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchReportChart() {
        val ivChart = findViewById<ImageView>(R.id.ivReportChart)

        lifecycleScope.launch {
            try {
                val responseBody = RetrofitClient.apiService.getReportChart(userId)

                val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())

                if (bitmap != null) {
                    ivChart.setImageBitmap(bitmap)
                } else {
                    Toast.makeText(this@AnalysisActivity, "Grafik verisi boş veya hatalı.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@AnalysisActivity, "Grafik yüklenemedi (Hiç kelimeniz olmayabilir).", Toast.LENGTH_LONG).show()
            }
        }
    }
}