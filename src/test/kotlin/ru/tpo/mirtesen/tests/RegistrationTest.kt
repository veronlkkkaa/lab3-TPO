package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.AuthPage
import java.util.Scanner

class RegistrationTest : BaseTest() {

    @ParameterizedTest(name = "UC-6 TC-21 Форма регистрации доступна [{0}]")
    @MethodSource("browsers")
    fun registrationFormIsAvailable(browser: String) {
        setup(browser)

        val authPage = AuthPage(driver).openRegistration()

        assertTrue(authPage.hasRegistrationForm(), "Форма регистрации должна быть доступна")
    }

    @ParameterizedTest(name = "UC-6 TC-22 Форма регистрации по почте доступна [{0}]")
    @MethodSource("browsers")
    fun emailRegistrationFormIsAvailable(browser: String) {
        setup(browser)

        val authPage = AuthPage(driver).openEmailRegistration()

        assertTrue(authPage.hasEmailRegistrationForm(), "Форма регистрации по email должна быть доступна")
    }

    @Test
    fun manualUserCanRegisterByEmailWithConfirmationCode() {
        assumeTrue(manualRegistrationEnabled(), "Для ручной регистрации нужен MIRTESEN_MANUAL_REGISTRATION=true")

        val browser = System.getenv("MIRTESEN_REGISTRATION_BROWSER")?.trim().takeUnless { it.isNullOrBlank() }
            ?: "chrome"
        setup(browser)

        val name = envOrPrompt("MIRTESEN_REGISTRATION_NAME", "Имя для регистрации")
            ?: error("Имя обязательно для регистрации по email")
        val email = envOrPrompt("MIRTESEN_REGISTRATION_EMAIL", "Email для регистрации")
            ?: error("Email обязателен для регистрации по email")

        val authPage = AuthPage(driver).openEmailRegistration()
        authPage.fillEmailRegistration(name, email)
        authPage.submitRegistration()

        completeRegistrationWithCode(authPage)
    }

    @Test
    fun manualUserCanRegisterWithConfirmationCode() {
        assumeTrue(manualRegistrationEnabled(), "Для ручной регистрации нужен MIRTESEN_MANUAL_REGISTRATION=true")

        val browser = System.getenv("MIRTESEN_REGISTRATION_BROWSER")?.trim().takeUnless { it.isNullOrBlank() }
            ?: "chrome"
        setup(browser)

        val authPage = AuthPage(driver).openRegistration()
        authPage.fillRegistration(readRegistrationData())
        authPage.submitRegistration()

        completeRegistrationWithCode(authPage)
    }

    private fun completeRegistrationWithCode(authPage: AuthPage) {
        val codeStepVisible = authPage.waitForRegistrationCodeStep()
        assertTrue(
            codeStepVisible || authPage.isAuthenticated() || authPage.hasAuthError(),
            "После отправки регистрации должен появиться шаг подтверждения, авторизованная сессия или ошибка формы"
        )

        if (authPage.isAuthenticated()) return
        assertTrue(codeStepVisible, "Шаг ввода кода подтверждения должен быть доступен")

        val code = System.getenv("MIRTESEN_REGISTRATION_CODE")?.trim().takeUnless { it.isNullOrBlank() }
            ?: prompt(
                "Введите код подтверждения и нажмите Enter. " +
                    "Если вводите код прямо в браузере, подтвердите форму там и нажмите Enter здесь пустой строкой"
            )
        if (code.isBlank()) {
            authPage.waitForManualRegistrationCompletion()
        } else {
            authPage.enterRegistrationCode(code)
            authPage.waitForManualRegistrationCompletion()
        }

        assertTrue(authPage.isAuthenticated(), "После подтверждения кода пользователь должен быть авторизован")
    }

    private fun readRegistrationData(): AuthPage.RegistrationData =
        AuthPage.RegistrationData(
            name = envOrPrompt("MIRTESEN_REGISTRATION_NAME", "Имя для регистрации"),
            lastName = envOrPrompt("MIRTESEN_REGISTRATION_LASTNAME", "Фамилия для регистрации", optional = true),
            email = envOrPrompt("MIRTESEN_REGISTRATION_EMAIL", "Email для регистрации", optional = true),
            phone = envOrPrompt("MIRTESEN_REGISTRATION_PHONE", "Телефон для регистрации", optional = true),
            password = envOrPrompt("MIRTESEN_REGISTRATION_PASSWORD", "Пароль для регистрации", optional = true, secret = true)
        )

    private fun manualRegistrationEnabled(): Boolean =
        System.getenv("MIRTESEN_MANUAL_REGISTRATION").equals("true", ignoreCase = true)

    private fun envOrPrompt(name: String, label: String, optional: Boolean = false, secret: Boolean = false): String? {
        val envValue = System.getenv(name)?.trim().orEmpty()
        if (envValue.isNotBlank()) return envValue
        val suffix = if (optional) " (необязательно, можно оставить пустым)" else ""
        val value = prompt("$label$suffix", secret).trim()
        return value.ifBlank { null }
    }

    private fun prompt(label: String, secret: Boolean = false): String {
        val console = System.console()
        if (console != null) {
            return if (secret) {
                console.readPassword("$label: ")?.concatToString().orEmpty()
            } else {
                console.readLine("$label: ").orEmpty()
            }
        }

        print("$label: ")
        if (!scanner.hasNextLine()) return ""
        return scanner.nextLine()
    }

    companion object {
        private val scanner = Scanner(System.`in`)
    }
}
