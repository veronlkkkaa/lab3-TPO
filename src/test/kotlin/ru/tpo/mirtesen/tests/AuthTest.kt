package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.AuthPage

@Execution(ExecutionMode.SAME_THREAD)
class AuthTest : BaseTest() {

    @ParameterizedTest(name = "UC-3 TC-11 Форма входа доступна")
    @MethodSource("browsers")
    fun loginFormIsAvailable(browser: String) {
        setup(browser)
        val authPage = AuthPage(driver).open()
        assertTrue(authPage.hasLoginForm(), "Форма авторизации должна быть доступна")
    }

    @ParameterizedTest(name = "UC-3 TC-12 Успешная авторизация")
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

    @ParameterizedTest(name = "UC-3 TC-13 Неверный пароль не авторизует пользователя")
    @MethodSource("browsers")
    fun invalidPasswordDoesNotLoginUser(browser: String) {
        val login = TestCredentials.loginFromEnvironment()
        assumeTrue(login != null, "Нужен MIRTESEN_LOGIN")

        setup(browser)
        val authPage = AuthPage(driver).open()
        authPage.loginByEmail(login!!, "wrong-password-${System.currentTimeMillis()}")

        assertFalse(authPage.isAuthenticated(), "Пользователь не должен быть авторизован с неверным паролем")
        assertTrue(
            authPage.hasAuthError() || authPage.hasLoginForm(),
            "При неверном пароле должна отображаться ошибка авторизации или форма входа должна остаться открытой"
        )
    }

    @ParameterizedTest(name = "UC-3 TC-14 Пользователь может выйти из аккаунта")
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
