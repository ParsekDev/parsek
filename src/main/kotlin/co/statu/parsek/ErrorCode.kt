package co.statu.parsek

enum class ErrorCode(val statusCode: Int = 200, val statusMessage: String = "") {
    BAD_REQUEST(400),
    INTERNAL_SERVER_ERROR(500),
}