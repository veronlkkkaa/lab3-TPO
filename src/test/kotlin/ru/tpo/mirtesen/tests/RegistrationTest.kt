package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.AuthPage
import java.io.File
import java.util.Scanner

@Execution(ExecutionMode.SAME_THREAD)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class RegistrationTest : BaseTest() {

    companion object {
        private const val DEFAULT_REGISTERED_EMAIL = "pavlich.draffen@dtgun.com"

        private val scanner = Scanner(System.`in`)

        @JvmStatic
        fun invalidEmailRegistrationData() = listOf(
            Arguments.of("", "invalid-name-empty@example.com"),
            Arguments.of("Тест", ""),
            Arguments.of("Тест", "not-an-email")
        )
    }

    @Order(1)
    @ParameterizedTest(name = "UC-6 TC-21 Форма регистрации по почте доступна [{0}]")
    @MethodSource("browsers")
    fun emailRegistrationFormIsAvailable(browser: String) {
        setup(browser)

        val authPage = AuthPage(driver).openEmailRegistration()

        assertTrue(authPage.hasEmailRegistrationForm(), "Форма регистрации по email должна быть доступна")
    }

    @Order(2)
    @ParameterizedTest(name = "UC-6 TC-23 Некорректные данные регистрации не отправляют форму: name={0}, email={1}")
    @MethodSource("invalidEmailRegistrationData")
    fun invalidEmailRegistrationDataDoesNotSubmit(name: String, email: String) {
        setup("chrome")

        val authPage = AuthPage(driver).openEmailRegistration()
        authPage.fillRegistrationName(name)
        authPage.fillRegistrationEmail(email)
        assertEquals(name, authPage.registrationNameValue(), "В поле имени должно быть введено тестовое значение")
        assertEquals(email, authPage.registrationEmailValue(), "В поле email должно быть введено тестовое значение")
        authPage.submitRegistration()

        val emailSent = authPage.waitForRegistrationValidationResult()

        assertFalse(emailSent, "Для некорректных данных не должен появляться экран отправленного письма")
        assertTrue(
            authPage.hasAuthError() || authPage.hasInvalidRegistrationInput() || authPage.hasEmailRegistrationForm(),
            "Для некорректных данных должна появиться ошибка формы, HTML5-invalid состояние или форма должна остаться открытой"
        )
    }

    @Order(3)
    @Test
    fun manualUserCanRegisterByEmailWithConfirmationCode() {
        assumeTrue(manualRegistrationEnabled(), "Для ручной регистрации нужен MIRTESEN_MANUAL_REGISTRATION=true")

        val browser = envValue("MIRTESEN_REGISTRATION_BROWSER") ?: "chrome"
        setup(browser)

        val name = envOrPrompt("MIRTESEN_REGISTRATION_NAME", "Имя для регистрации")
            ?: error("Имя обязательно для регистрации по email")
        val email = envOrPrompt("MIRTESEN_REGISTRATION_EMAIL", "Email для регистрации")
            ?: error("Email обязателен для регистрации по email")

        val authPage = AuthPage(driver).openEmailRegistration()
        authPage.fillEmailRegistration(name, email)
        assertEquals(name, authPage.registrationNameValue(), "В поле имени должно быть введено значение из MIRTESEN_REGISTRATION_NAME")
        assertEquals(email, authPage.registrationEmailValue(), "В поле email должно быть введено значение из MIRTESEN_REGISTRATION_EMAIL")
        authPage.submitRegistration()

        completeRegistrationByEmailLink(authPage, email)
    }

    @Order(4)
    @ParameterizedTest(name = "UC-6 TC-22 Повторная регистрация уже занятого email запрещена [{0}]")
    @MethodSource("browsers")
    fun registeredEmailCannotBeRegisteredAgain(browser: String) {
        setup(browser)

        val registeredEmail = registeredEmailForNegativeTest()

        val authPage = AuthPage(driver).openEmailRegistration()
        authPage.fillEmailRegistration("Тест", registeredEmail)
        assertEquals("Тест", authPage.registrationNameValue(), "В поле имени должно быть введено тестовое имя")
        assertEquals(registeredEmail, authPage.registrationEmailValue(), "В поле email должен быть введён уже зарегистрированный email")
        authPage.submitRegistration()

        val emailSent = authPage.waitForRegistrationValidationResult()

        assertFalse(emailSent, "Для уже зарегистрированного email не должен появляться экран отправленного письма")
        assertTrue(
            authPage.hasAuthError() || authPage.hasEmailRegistrationForm(),
            "Для уже зарегистрированного email должна появиться ошибка или форма регистрации должна остаться открытой"
        )
        if (authPage.hasAuthError()) {
            assertTrue(
                authPage.authErrorText().contains("зарегистрирован", ignoreCase = true),
                "Ошибка должна сообщать, что email уже зарегистрирован. Фактическая ошибка: ${authPage.authErrorText()}"
            )
        }
    }

    private fun completeRegistrationByEmailLink(authPage: AuthPage, email: String) {
        val emailSent = authPage.waitForRegistrationEmailSent()
        assertTrue(
            emailSent || authPage.hasAuthError(),
            "После отправки регистрации должен появиться экран отправленного письма или ошибка формы"
        )

        assertTrue(
            emailSent,
            "Экран отправленного письма должен быть доступен. Ошибка формы: ${authPage.authErrorText().ifBlank { "нет" }}"
        )
        assertEquals(email, authPage.registrationEmailSentAddress(), "Письмо должно быть отправлено на email из MIRTESEN_REGISTRATION_EMAIL")

        val acceptUrl = registrationAcceptUrl()
        authPage.openRegistrationAcceptUrl(acceptUrl)
        authPage.waitForManualRegistrationCompletion()

        assertTrue(
            authPage.hasRegistrationEmailSentScreenGoneAfterConfirmation(),
            "После перехода по ссылке подтверждения экран отправленного письма должен исчезнуть. Ошибка формы: ${authPage.authErrorText().ifBlank { "нет" }}"
        )
    }

    private fun manualRegistrationEnabled(): Boolean =
        System.getenv("MIRTESEN_MANUAL_REGISTRATION").equals("true", ignoreCase = true)

    private fun registeredEmailForNegativeTest(): String =
        envValue("MIRTESEN_REGISTERED_EMAIL")
            ?: if (manualRegistrationEnabled()) {
                envValue("MIRTESEN_REGISTRATION_EMAIL") ?: DEFAULT_REGISTERED_EMAIL
            } else {
                DEFAULT_REGISTERED_EMAIL
            }

    private fun registrationAcceptUrl(): String =
        envValue("MIRTESEN_REGISTRATION_ACCEPT_URL") ?: promptForRegistrationAcceptUrl()

    private fun promptForRegistrationAcceptUrl(): String {
        while (true) {
            val value = promptRequired("Вставьте ссылку подтверждения из письма и нажмите Enter").trim()
            if (value.isBlank()) {
                println("Ссылка пустая, вставьте ссылку из письма.")
                continue
            }
            if (!value.contains("/action/accept/")) {
                println("Это не похоже на ссылку подтверждения MirTesen, попробуйте еще раз.")
                continue
            }
            return value
        }
    }

    private fun envOrPrompt(name: String, label: String): String? {
        envValue(name)?.let { return it }
        val value = prompt(label).trim()
        return value.ifBlank { null }
    }

    private fun envValue(name: String): String? =
        System.getenv(name)?.trim()?.takeUnless { it.isBlank() }

    private fun promptRequired(label: String): String =
        readLineFromTerminal(label)
            ?: error(
                "Не удалось прочитать ввод из консоли. " +
                    "Передайте ссылку через MIRTESEN_REGISTRATION_ACCEPT_URL или запустите тест из интерактивного терминала."
            )

    private fun prompt(label: String): String {
        return readLineFromTerminal(label).orEmpty()
    }

    private fun readLineFromTerminal(label: String): String? {
        val console = System.console()
        if (console != null) {
            return console.readLine("$label: ").orEmpty()
        }

        print("$label: ")
        if (scanner.hasNextLine()) {
            return scanner.nextLine()
        }

        val tty = File("/dev/tty")
        if (tty.exists() && tty.canRead()) {
            return try {
                tty.bufferedReader().use { it.readLine() }
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

}
