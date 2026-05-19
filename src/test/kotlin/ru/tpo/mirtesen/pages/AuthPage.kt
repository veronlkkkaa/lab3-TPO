package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.TimeoutException

class AuthPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private const val LOGIN_URL = "${MainPage.URL}?auth=login"
        private const val LOGIN_FALLBACK_URL = "${MainPage.URL}login"

        private val EMAIL_LOGIN_TAB = By.xpath(
            "//button[contains(normalize-space(),'Вход по почте')]" +
            " | //a[contains(normalize-space(),'Вход по почте')]"
        )

        private val EMAIL_INPUT = By.xpath("//input[@name='email']")

        private val PASSWORD_INPUT = By.xpath("//input[@type='password']")

        private val LOGIN_SUBMIT = By.xpath(
            "//form[.//input[@name='email'] and .//input[@name='password']]" +
            "//button[contains(normalize-space(),'Войти')]"
        )

        private val AUTH_FORM = By.xpath("//*[contains(normalize-space(),'Вход по почте')]")

        private val AUTH_ERROR = By.xpath(
            "//*[contains(normalize-space(),'Неправильный пароль') " +
            "or contains(normalize-space(),'Введите пароль') " +
            "or contains(normalize-space(),'Введите e-mail')]"
        )

        private val AUTHENTICATED_MARKER = By.xpath(
            "//a[contains(@href,'/people/') and not(contains(@href,'/search/'))]" +
            " | //button[contains(@class,'profile')]"
        )

        private val USER_MENU_TRIGGER = By.xpath(
            "//button[contains(@class,'profile')]" +
            " | //a[contains(@href,'/people/') and not(contains(@href,'/search/'))]"
        )

        private val LOGOUT_ACTION = By.xpath(
            "//button[contains(normalize-space(),'Выйти')]" +
            " | //a[contains(normalize-space(),'Выйти')]"
        )

        private val LOGOUT_CONFIRM = By.xpath(
            "//*[contains(@class,'logout-modal')]//button[contains(normalize-space(),'Выйти')]"
        )
    }

    fun open(): AuthPage {
        openUrl(LOGIN_URL)
        if (waitForLoginForm()) return this

        openUrl(LOGIN_FALLBACK_URL)
        if (waitForLoginForm()) return this

        throw TimeoutException("Форма авторизации не отрисовалась на mirtesen.ru")
    }

    private fun waitForLoginForm(): Boolean {
        return try {
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(AUTH_FORM),
                    ExpectedConditions.presenceOfElementLocated(EMAIL_LOGIN_TAB),
                    ExpectedConditions.presenceOfElementLocated(EMAIL_INPUT)
                )
            )
            true
        } catch (e: TimeoutException) {
            false
        }
    }

    fun ensureOpened(): AuthPage {
        if (!hasLoginForm()) {
            open()
        }
        return this
    }

    fun loginByEmail(email: String, password: String): MainPage {
        if (!isPresent(EMAIL_INPUT) && isPresent(EMAIL_LOGIN_TAB)) {
            try {
                jsClick(EMAIL_LOGIN_TAB)
            } catch (e: TimeoutException) {
            }
        }
        type(EMAIL_INPUT, email)
        type(PASSWORD_INPUT, password)
        jsClick(LOGIN_SUBMIT)
        wait.until { isPresent(AUTHENTICATED_MARKER) || hasAuthTokenCookie() || isPresent(AUTH_ERROR) }
        return MainPage(driver)
    }

    fun hasLoginForm(): Boolean = isPresent(AUTH_FORM) || isPresent(EMAIL_INPUT)

    fun hasAuthError(): Boolean = isPresent(AUTH_ERROR)

    fun isAuthenticated(): Boolean =
        !isPresent(AUTH_ERROR) && (isPresent(AUTHENTICATED_MARKER) || hasAuthTokenCookie())

    fun logout(): AuthPage {
        if (isPresent(USER_MENU_TRIGGER)) {
            jsClick(USER_MENU_TRIGGER)
        }
        if (isPresent(LOGOUT_ACTION)) {
            jsClick(LOGOUT_ACTION)
            if (isPresent(LOGOUT_CONFIRM)) {
                jsClick(LOGOUT_CONFIRM)
            }
            waitForLoggedOutState()
        } else {
            driver.manage().deleteAllCookies()
            open()
        }
        return this
    }

    fun isLoggedOut(): Boolean =
        !hasAuthTokenCookie() && (isPresent(AUTH_FORM) || !isPresent(AUTHENTICATED_MARKER))

    private fun waitForLoggedOutState() {
        try {
            wait.until { isLoggedOut() }
        } catch (e: TimeoutException) {
            open()
            wait.until { isLoggedOut() }
        }
    }

    private fun hasAuthTokenCookie(): Boolean =
        driver.manage().cookies.any { cookie ->
            val name = cookie.name.lowercase()
            val value = cookie.value.orEmpty()
            name.contains("jwt") ||
                name.contains("refresh") ||
                value.count { it == '.' } >= 2
        }
}
