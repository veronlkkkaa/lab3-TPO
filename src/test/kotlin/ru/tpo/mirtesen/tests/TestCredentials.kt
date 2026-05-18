package ru.tpo.mirtesen.tests

data class TestCredentials(val login: String, val password: String) {
    companion object {
        fun loginFromEnvironment(): String? {
            val login = System.getenv("MIRTESEN_LOGIN")?.trim().orEmpty()
            return login.ifBlank { null }
        }

        fun fromEnvironment(): TestCredentials? {
            val login = loginFromEnvironment().orEmpty()
            val password = System.getenv("MIRTESEN_PASSWORD").orEmpty()
            if (login.isBlank() || password.isBlank()) return null
            return TestCredentials(login, password)
        }
    }
}
