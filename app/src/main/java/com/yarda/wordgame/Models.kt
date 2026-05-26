package com.yarda.wordgame

import com.google.gson.annotations.SerializedName

data class UserAuth(
    val username: String,
    val password: String
)

data class AuthResponse(
    val mesaj: String?,
    val status: String?,
    val user_id: Int
)

data class WordResponse(
    val id: Int,
    val ing: String,
    val tr: String,
    val siklar: List<String>? = emptyList()
)

data class QuizResponse(
    val kullanici_id: Int,
    val bugunku_soru_sayisi: Int,
    val kelimeler: List<WordResponse>
)

data class AnswerData(
    val user_id: Int,
    val word_id: Int,
    val is_correct: Int
)

data class SubmitResponse(
    val durum: String?,
    val yeni_seviye: Int?,
    @SerializedName("HATA") val hata: String?
)

data class WordleStartResponse(
    @SerializedName("kelime id") val kelime_id: Int,
    @SerializedName("kelime uzunluğu") val kelime_uzunlugu: Int,
    val mesaj: String
)

data class WordleGuess(
    val word_id: Int,
    val guess: String
)

data class WordleDetail(
    val harf: String,
    val renk: String
)

data class WordleCheckResponse(
    val isWin: Boolean?,
    val dogru_cevap: String?,
    val details: List<WordleDetail>?
)
data class SettingsUpdate(
    val user_id: Int,
    val new_limit: Int
)
data class SettingsResponse(
    val mesaj: String?,
    val kullanici: String?,
    val yeni_limit: Int?,
    val hata: String?
)
data class ForgotPasswordRequest(
    val username: String,
    val new_password: String
)

data class ForgotResponse(
    val mesaj: String,
    val kullanici: String
)

data class WordCreateRequest(
    val eng_word: String,
    val tur_word: String,
    val picture_path: String = "",
    val samples: List<String> = emptyList()
)

data class AddWordResponse(
    val mesaj: String,
    val word_id: Int
)

data class CompleteQuizResponse(
    val mesaj: String
)



data class ReportResponse(
    val kullanici_id: Int,
    val toplam_etkilesim_kurulan_kelime: Int,
    val tamamen_ogrenilen_kelime_sayisi: Int,
    val devam_eden_kelime_sayisi: Int,
    val zorlanilan_kelime_sayisi: Int,
    val genel_basari_yuzdesi: Double,
    val seviye_dagilimi: Map<String, Int>
)