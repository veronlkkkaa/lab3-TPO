package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.AuthPage

/**
 * UC-3: Авторизация на Миртесен.
 *
 * Проверяем базовые сценарий входа, например:  открыть форму, выбрать вход по почте,
 * ввести логин и пароль, убедиться что сайт пустил.
 */
class AuthTest : BaseTest() {

    // TC-11: открываем страницу входа и смотрим есть ли форма вообще ?
    @ParameterizedTest(name = "UC-3 TC-11 Форма входа доступна [{0}]")
    @MethodSource("browsers")
    fun loginFormIsAvailable(browser: String) {
        setup(browser)
        val authPage = AuthPage(driver).open()
        assertTrue(authPage.hasLoginForm(), "Форма авторизации должна быть доступна")
    }

    // TC-12: вводим настоящие логин и пароль. сайт должен пустить без ошибок.
    // Нужны переменные окружения MIRTESEN_LOGIN и MIRTESEN_PASSWORD, иначе тест пропускается.
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

    // TC-13: вводим правильный логин, но  неверный пароль. сайт должен показать ошибку и не пустить.
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

    // TC-14: входим, затем выходим. проверяем что сессия действительно закончилась.
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
