package cz.utb.qrguard.model

enum class WarningType(val label: String) {
    INSECURE_HTTP("Nezabezpečené připojení (http://)"),
    IP_ADDRESS("Odkaz obsahuje IP adresu místo domény."),
    URL_SHORTENER("Použit známy zkracovač odkazů.")
}

data class ScanResult(
    val id: Long = 0,
    val fullUrl : String,
    val domain : String,
    val warnings: List<WarningType>,
    val timestamp: Long = System.currentTimeMillis()
    )