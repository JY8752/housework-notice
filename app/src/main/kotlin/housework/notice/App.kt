package housework.notice

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun main() {
    //環境変数から値を取得する
    val houseWorkSheetUrl = System.getenv("HOUSEWORK_SHEET_URL")
    val token = System.getenv("LINE_NOTIFY_TOKEN")

    if(houseWorkSheetUrl.isNullOrEmpty() || token.isNullOrEmpty()) throw RuntimeException("環境変数設定してよ！！")

    //メッセージ作成
    val today = LocalDateTime.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    val message = """
        [$today]
        今日も1日お疲れ様です( ^ω^ )
        家事表の入力お忘れなく~~~~
        $houseWorkSheetUrl
    """.trimIndent()

    LineNotify(token).notifyMessage(message)
}

class LineNotify(token: String) {
    companion object {
        const val CONNECTION_TIMEOUT_MILLISECONDS = 3 * 1000L
        const val READ_TIMEOUT_MILLISECONDS = 3 * 1000L
        const val WRITE_TIMEOUT_MILLISECONDS = 3 * 1000L

        const val LINE_NOTIFY_URL = "https://notify-api.line.me/api/notify"
        private val MEDIA_TYPE = "application/x-www-form-urlencoded".toMediaType()
    }
    //clientの作成
    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
        .readTimeout(READ_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
        .writeTimeout(WRITE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
        .build()

    //header
    private val headers = Headers.Builder()
        .add("Authorization", "Bearer $token")
        .build()

    //APIコール
    fun notifyMessage(message: String) {
        //requestの作成
        val request = Request.Builder()
            .url(LINE_NOTIFY_URL)
            .post("message=$message".toRequestBody(MEDIA_TYPE))
            .headers(headers)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("リクエストに失敗しました")
            println(response.body!!.string())
        }
    }
}