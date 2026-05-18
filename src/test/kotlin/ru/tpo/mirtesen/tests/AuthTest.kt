package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.AuthPage

/**
 * UC-3: Авторизация пользователя на Миртесен.
 *
 * Актор: зарегистрированный пользователь.
 * Предусловие: пользователь имеет тестовую учетную запись.
 * Основной сценарий: пользователь открывает форму входа ->
 *   выбирает вход по электронной почте -> вводит логин и пароль ->
 *   система авторизует пользователя.
 *
 * Тест-кейсы:
 *   TC-11 — форма входа доступна неавторизованному пользователю
 *   TC-12 — вход с валидными данными переводит пользователя в авторизованное состояние
 *   TC-13 — вход с неверным паролем отображает ошибку авторизации
 *   TC-14 — авторизованный пользователь может выйти из аккаунта
 */
class AuthTest : BaseTest() {

    /**
     * TC-11: Форма входа доступна неавторизованному пользователю.
     * Предусловие: mirtesen.ru доступен.
     * Шаги: открыть https://mirtesen.ru/?auth=login.
     * Ожидаемый результат: отображается форма авторизации.
     */
    @ParameterizedTest(name = "UC-3 TC-11 Форма входа доступна [{0}]")
    @MethodSource("browsers")
    fun loginFormIsAvailable(browser: String) {
        setup(browser)
        val authPage = AuthPage(driver).open()
        assertTrue(authPage.hasLoginForm(), "Форма авторизации должна быть доступна")
    }

    /**
     * TC-12: Вход с валидными данными переводит пользователя в авторизованное состояние.
     * Предусловие: заданы MIRTESEN_LOGIN и MIRTESEN_PASSWORD.
     * Шаги: открыть форму входа -> выбрать вход по почте -> ввести email и пароль -> нажать «Войти».
     * Ожидаемый результат: ошибка авторизации не отображается, появляются признаки сессии пользователя.
     */
    @ParameterizedTest(name = "UC-3 TC-12 Успешная авторизация [{0}]")
    @MethodSource("browsers")
    fun validCredentialsLoginUser(browser: String) {
        val credentials = TestCredentials.fromEnvironment()
        assumeTrue(credentials != null, "Нужны MIRTESEN_LOGIN и MIRTESEN_PASSWORD")

        setup(browser)
        val authPage = AuthPage(driver).open()
        authPage.loginByEmail(credentials!!.login, credentials.password)

        assertFalse(authPage.hasAuthError(), "После входа не должно быть ошибки авторизации")
        assertTrue(authPage.isAuthenticated(),
            "После входа должны отображаться признаки авторизованной сессии")
    }

    /**
     * TC-13: Вход с неверным паролем отображает ошибку авторизации.
     * Предусловие: задан MIRTESEN_LOGIN.
     * Шаги: открыть форму входа -> выбрать вход по почте -> ввести email и неверный пароль -> нажать «Войти».
     * Ожидаемый результат: пользователь не авторизован, отображается ошибка авторизации.
     */
    @ParameterizedTest(name = "UC-3 TC-13 Неверный пароль не авторизует пользователя [{0}]")
    @MethodSource("browsers")
    fun invalidPasswordDoesNotLoginUser(browser: String) {
        val login = TestCredentials.loginFromEnvironment()
        assumeTrue(login != null, "Нужен MIRTESEN_LOGIN")

        setup(browser)
        val authPage = AuthPage(driver).open()
        authPage.loginByEmail(login!!, "wrong-password-${System.currentTimeMillis()}")

        assertTrue(authPage.hasAuthError(), "При неверном пароле должна отображаться ошибка авторизации")
        assertFalse(authPage.isAuthenticated(), "Пользователь не должен быть авторизован с неверным паролем")
    }

    /**
     * TC-14: Авторизованный пользователь может выйти из аккаунта.
     * Предусловие: заданы MIRTESEN_LOGIN и MIRTESEN_PASSWORD.
     * Шаги: выполнить успешный вход -> открыть меню пользователя -> нажать «Выйти».
     * Ожидаемый результат: признаки авторизованной сессии исчезают.
     */
    @ParameterizedTest(name = "UC-3 TC-14 Пользователь может выйти из аккаунта [{0}]")
    @MethodSource("browsers")
    fun authorizedUserCanLogout(browser: String) {
        val credentials = TestCredentials.fromEnvironment()
        assumeTrue(credentials != null, "Нужны MIRTESEN_LOGIN и MIRTESEN_PASSWORD")

        setup(browser)
        val authPage = AuthPage(driver).open()
        authPage.loginByEmail(credentials!!.login, credentials.password)
        assertTrue(authPage.isAuthenticated(), "Перед выходом пользователь должен быть авторизован")

        authPage.logout()
        assertTrue(authPage.isLoggedOut(), "После выхода пользователь не должен оставаться авторизованным")
    }
}
