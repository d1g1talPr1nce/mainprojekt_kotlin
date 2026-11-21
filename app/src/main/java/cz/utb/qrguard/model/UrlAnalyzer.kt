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

    // Doména je část za "://" až k prvnímu /, \, ? nebo #.
    // Zpětné lomítko bere prohlížeč u http/https stejně jako lomítko (standard WHATWG URL),
    // proto musí ukončit doménu i tady – jinak by https://podvod.cz\@banka.cz ukázal
    // doménu banka.cz, ale prohlížeč by otevřel podvod.cz.
    // Přihlašovací údaje před zavináčem a port za dvojtečkou do domény nepatří –
    // skutečný cíl odkazu je až to, co je za zavináčem (http://banka.cz@podvod.cz).
    private fun extractDomain(url: String): String {
        val authority = url.substringAfter("://")
            .takeWhile { it != '/' && it != '\\' && it != '?' && it != '#' }
        val domainWithPort = authority.substringAfterLast('@')
        val domain = if (domainWithPort.startsWith("[")) {
            // IPv6 adresa je v hranatých závorkách, dvojtečky v ní neoddělují port.
            domainWithPort.substringBefore(']') + "]"
        } else {
            domainWithPort.substringBefore(':')
        }
        return domain.lowercase()
    }

    private fun isIpAddress(domain: String): Boolean =
        domain.startsWith("[") || parseIpv4(domain) != null

    // Převede hostitele na 32bitovou IPv4 adresu stejně jako prohlížeč (standard WHATWG URL),
    // jinak vrátí null. Prohlížeč přijímá i méně obvyklé zápisy téže adresy:
    //   192.168.0.1  =  3232235521  =  0xC0A80001  =  0300.0250.0.1  =  192.168.1
    // Útočník je používá, aby IP adresa na první pohled nevypadala jako IP adresa.
    private fun parseIpv4(host: String): Long? {
        val parts = host.split('.').toMutableList()
        // Koncová tečka (192.168.0.1.) na adrese nic nemění.
        if (parts.size > 1 && parts.last().isEmpty()) parts.removeAt(parts.lastIndex)
        if (parts.size > 4) return null

        val numbers = parts.map { parseIpv4Number(it) ?: return null }

        // Všechny části kromě poslední jsou jeden bajt (0–255).
        // Poslední část vyplní zbývající bajty: u čtyř částí 1 bajt, u jedné části celé 4 bajty.
        if (numbers.dropLast(1).any { it > 255 }) return null
        val last = numbers.last()
        if (last >= 1L shl (8 * (5 - numbers.size))) return null

        var address = last
        numbers.dropLast(1).forEachIndexed { index, number ->
            address += number shl (8 * (3 - index))
        }
        return address
    }

    // Jedna část IPv4 adresy: 0x… je šestnáctkově, úvodní 0 znamená osmičkově, jinak desítkově.
    private fun parseIpv4Number(part: String): Long? {
        if (part.isEmpty()) return null
        val (digits, radix) = when {
            part.startsWith("0x") -> part.drop(2) to 16
            part.length > 1 && part.startsWith("0") -> part.drop(1) to 8
            else -> part to 10
        }
        if (digits.isEmpty()) return 0
        if (!digits.all { Character.digit(it, radix) >= 0 }) return null
        return digits.toLongOrNull(radix)
    }

    // Předpona www. na typ domény nic nemění, pro porovnání se seznamem ji odřízneme.
    private fun isUrlShortener(domain: String): Boolean =
        domain.removePrefix("www.") in urlShorteners
}
