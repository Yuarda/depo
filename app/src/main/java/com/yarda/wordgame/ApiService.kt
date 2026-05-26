package com.yarda.wordgame
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PUT
import okhttp3.ResponseBody

interface ApiService {
    @POST("/signup")
    suspend fun signUp(@Body user: UserAuth): AuthResponse

    @POST("/login")
    suspend fun login(@Body user: UserAuth): AuthResponse

    @GET("/daily_quiz/{user_id}")
    suspend fun getDailyQuiz(
        @Path("user_id") userId: Int,
        @Query("word_limit") limit: Int = 10
    ): QuizResponse

    @POST("/submit")
    suspend fun submitAnswer(@Body answer: AnswerData): SubmitResponse

    @GET("/wordle/start/{user_id}")
    suspend fun startWordle(@Path("user_id") userId: Int): WordleStartResponse

    @POST("/wordle/check")
    suspend fun checkWordle(@Body guess: WordleGuess): WordleCheckResponse

    @GET("/report/{user_id}")
    suspend fun getReport(@Path("user_id") userId: Int): ReportResponse

    @PUT("/update_settings")
    suspend fun updateSettings(@Body settings: SettingsUpdate): SettingsResponse

    @GET("/report/chart/{user_id}")
    suspend fun getReportChart(@Path("user_id") userId: Int): ResponseBody

    @PUT("/forgot_password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotResponse

    @POST("/add_word")
    suspend fun addWord(@Body wordData: WordCreateRequest): AddWordResponse

    @POST("/complete_daily_quiz/{user_id}")
    suspend fun completeDailyQuiz(@Path("user_id") userId: Int): CompleteQuizResponse

}



object RetrofitClient {
    private const val BASE_URL = "http://10.50.212.4:8000"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}