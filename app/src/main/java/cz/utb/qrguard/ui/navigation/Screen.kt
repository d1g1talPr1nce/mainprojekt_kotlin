package cz.utb.qrguard.ui.navigation

// Seznam všech obrazovek aplikace a jejich tras pro Navigation Compose.
sealed class Screen(val route: String, val title: String) {
    data object Scanner : Screen("scanner", "Skener")
    data object Result : Screen("result", "Výsledek")
    data object History : Screen("history", "Historie")
}
