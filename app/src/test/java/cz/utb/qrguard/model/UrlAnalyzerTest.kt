package cz.utb.qrguard.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Testy pravidel pro vyhodnocení odkazu z QR kódu.
// Běží na JVM, takže je lze spustit bez emulátoru: ./gradlew test
class UrlAnalyzerTest {

    // --- Rozpoznání domény ---

    @Test
    fun `bezpecny odkaz nema zadne upozorneni`() {
        val result = UrlAnalyzer.analyze("https://www.utb.cz/studenti/rozvrh")

        assertEquals("www.utb.cz", result?.domain)
        assertEquals(emptyList<WarningType>(), result?.warnings)
    }

    @Test
    fun `cely odkaz se uchova vcetne parametru`() {
        val url = "https://www.utb.cz/hledat?q=rozvrh&rok=2026"

        assertEquals(url, UrlAnalyzer.analyze(url)?.fullUrl)
    }

    @Test
    fun `schema a domena se prevedou na mala pismena`() {
        val result = UrlAnalyzer.analyze("HTTPS://WWW.UTB.CZ/Studenti")

        assertEquals("www.utb.cz", result?.domain)
        assertEquals(emptyList<WarningType>(), result?.warnings)
    }

    @Test
    fun `port neni soucasti domeny`() {
        assertEquals("example.com", UrlAnalyzer.analyze("https://example.com:8443/app")?.domain)
    }

    @Test
    fun `domenou je az to co je za zavinacem`() {
        // Klasický trik: na začátku odkazu je důvěryhodný název, cíl je ale jiný.
        val result = UrlAnalyzer.analyze("https://www.mojebanka.cz@podvod.example/login")

        assertEquals("podvod.example", result?.domain)
    }

    @Test
    fun `zpetne lomitko ukoncuje domenu stejne jako lomitko`() {
        // Prohlížeč bere \ jako /, takže zavináč je až v cestě a cílem je podvod.example.
        val result = UrlAnalyzer.analyze("https://podvod.example\\@www.mojebanka.cz/login")

        assertEquals("podvod.example", result?.domain)
    }

    @Test
    fun `mezery okolo odkazu se ignoruji`() {
        assertEquals("example.com", UrlAnalyzer.analyze("  https://example.com/  ")?.domain)
    }

    // --- Pravidlo: nezabezpečené http:// ---

    @Test
    fun `http odkaz upozorni na nezabezpecene pripojeni`() {
        val result = UrlAnalyzer.analyze("http://example.com/login")

        assertEquals(listOf(WarningType.INSECURE_HTTP), result?.warnings)
    }

    // --- Pravidlo: IP adresa místo domény ---

    @Test
    fun `ipv4 adresa misto domeny je upozorneni`() {
        val result = UrlAnalyzer.analyze("https://192.168.1.10/platba")

        assertEquals(listOf(WarningType.IP_ADDRESS), result?.warnings)
    }

    @Test
    fun `ipv6 adresa misto domeny je upozorneni`() {
        val result = UrlAnalyzer.analyze("https://[2001:db8::1]:8080/platba")

        assertEquals("[2001:db8::1]", result?.domain)
        assertEquals(listOf(WarningType.IP_ADDRESS), result?.warnings)
    }

    @Test
    fun `cislo mimo rozsah ip adresy se bere jako domena`() {
        val result = UrlAnalyzer.analyze("https://999.1.1.1/")

        assertEquals(emptyList<WarningType>(), result?.warnings)
    }

    // --- Pravidlo: zkracovač odkazů ---

    @Test
    fun `znamy zkracovac odkazu je upozorneni`() {
        val result = UrlAnalyzer.analyze("https://bit.ly/3xYzAbc")

        assertEquals(listOf(WarningType.URL_SHORTENER), result?.warnings)
    }

    @Test
    fun `zkracovac se pozna i s predponou www`() {
        val result = UrlAnalyzer.analyze("https://www.tinyurl.com/abcd")

        assertEquals(listOf(WarningType.URL_SHORTENER), result?.warnings)
    }

    @Test
    fun `podobna domena neni zkracovac`() {
        val result = UrlAnalyzer.analyze("https://bit.ly.example.com/3xYzAbc")

        assertEquals(emptyList<WarningType>(), result?.warnings)
    }

    // --- Více pravidel najednou ---

    @Test
    fun `http zkracovac ma obe upozorneni`() {
        val result = UrlAnalyzer.analyze("http://bit.ly/3xYzAbc?utm_source=qr")

        assertTrue(result?.warnings?.containsAll(
            listOf(WarningType.INSECURE_HTTP, WarningType.URL_SHORTENER)
        ) == true)
        assertEquals(2, result?.warnings?.size)
    }

    @Test
    fun `http na ip adrese ma obe upozorneni`() {
        val result = UrlAnalyzer.analyze("http://10.0.0.1/admin")

        assertEquals(
            listOf(WarningType.INSECURE_HTTP, WarningType.IP_ADDRESS),
            result?.warnings
        )
    }

    // --- Text, který není webový odkaz ---

    @Test
    fun `obycejny text neni webovy odkaz`() {
        assertNull(UrlAnalyzer.analyze("Ahoj, tohle je jen text"))
    }

    @Test
    fun `jine schema neni webovy odkaz`() {
        assertNull(UrlAnalyzer.analyze("mailto:student@utb.cz"))
        assertNull(UrlAnalyzer.analyze("WIFI:S:MojeSit;T:WPA;P:tajneheslo;;"))
    }

    @Test
    fun `odkaz bez schematu neni webovy odkaz`() {
        assertNull(UrlAnalyzer.analyze("www.example.com"))
    }

    @Test
    fun `odkaz bez domeny neni webovy odkaz`() {
        assertNull(UrlAnalyzer.analyze("https:///cesta"))
    }

    @Test
    fun `prazdny text neni webovy odkaz`() {
        assertNull(UrlAnalyzer.analyze(""))
    }
}
