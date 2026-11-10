package cz.utb.qrguard.model

// Vyhodnocení textu načteného z QR kódu: rozpozná webový odkaz,
// vytáhne doménu a najde rizikové znaky podle pravidel aplikace.
object UrlAnalyzer {

    // Známé zkracovače odkazů – za krátkou adresou může být skryta jakákoli cílová stránka.
    private val urlShorteners = setOf(
        "bit.ly",
        "tinyurl.com",
        "t.co",
        "goo.gl",
        "ow.ly",
        "is.gd",
        "buff.ly",
        "cutt.ly",
        "rb.gy",
        "rebrand.ly",
        "shorturl.at",
        "t.ly"
    )

    private val ipv4Regex = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")

    // Vrátí výsledek kontroly, nebo null, pokud text není webový odkaz
    // (jiné schéma než http/https, chybějící doména nebo obyčejný text).
    fun analyze(rawValue: String): ScanResult? {
        val url = rawValue.trim()
        val scheme = url.substringBefore("://", missingDelimiterValue = "").lowercase()
        if (scheme != "http" && scheme != "https") return null

        val domain = extractDomain(url)
        if (domain.isEmpty()) return null

        val warnings = buildList {
            if (scheme == "http") add(WarningType.INSECURE_HTTP)
            if (isIpAddress(domain)) add(WarningType.IP_ADDRESS)
            if (isUrlShortener(domain)) add(WarningType.URL_SHORTENER)
        }

        return ScanResult(fullUrl = url, domain = domain, warnings = warnings)
    }

    // Doména je část za "://" až k prvnímu /, ? nebo #.
    // Přihlašovací údaje před zavináčem a port za dvojtečkou do domény nepatří –
    // skutečný cíl odkazu je až to, co je za zavináčem (http://banka.cz@podvod.cz).
    private fun extractDomain(url: String): String {
        val authority = url.substringAfter("://")
            .takeWhile { it != '/' && it != '?' && it != '#' }
        val domainWithPort = authority.substringAfterLast('@')
        val domain = if (domainWithPort.startsWith("[")) {
            // IPv6 adresa je v hranatých závorkách, dvojtečky v ní neoddělují port.
            domainWithPort.substringBefore(']') + "]"
        } else {
            domainWithPort.substringBefore(':')
        }
        return domain.lowercase()
    }

    private fun isIpAddress(domain: String): Boolean = when {
        domain.startsWith("[") -> true
        ipv4Regex.matches(domain) -> domain.split('.').all { it.toInt() <= 255 }
        else -> false
    }

    // Předpona www. na typ domény nic nemění, pro porovnání se seznamem ji odřízneme.
    private fun isUrlShortener(domain: String): Boolean =
        domain.removePrefix("www.") in urlShorteners
}
