package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.TimeoutException
import java.time.Duration

class AuthPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private const val LOGIN_URL = "${MainPage.URL}?auth=login"
        private const val LOGIN_FALLBACK_URL = "${MainPage.URL}login"

        private val HEADER_LOGIN_BUTTON = By.xpath(
            "//*[contains(@class,'header-login')]" +
            "//*[self::button or self::a][contains(@class,'btn-secondary') or contains(@class,'login')]"
        )

        private val HEADER_REGISTRATION_BUTTON = By.xpath(
            "//*[contains(@class,'header-login')]" +
            "//*[self::button or self::a][contains(@class,'btn-primary') or contains(@class,'register')]"
        )

        private val EMAIL_LOGIN_TAB = By.xpath(
            "(//*[contains(@class,'auth-form')]" +
            "//*[contains(@class,'auth-form__form__submit-secondary')]" +
            "//*[self::button or self::a])[1]" +
            " | //*[self::button or self::a][" +
            "contains(@class,'email') or contains(@class,'mail') " +
            "or contains(@data-test,'email') or contains(@data-testid,'email') " +
            "or contains(@href,'email') or contains(@href,'mail')]"
        )

        private val EMAIL_INPUT = By.xpath(
            "//input[@name='email' or @type='email']" +
            " | //*[contains(@class,'auth-form__form__email')]//input"
        )

        private val PHONE_INPUT = By.xpath(
            "//input[@name='phone' or @type='tel']" +
            " | //*[contains(@class,'auth-form__form__phone')]//input"
        )

        private val NAME_INPUT = By.xpath(
            "//input[@name='name' or @name='firstName' or @name='firstname']"
        )

        private val LAST_NAME_INPUT = By.xpath(
            "//input[@name='lastname' or @name='lastName' or @name='surname']"
        )

        private val PASSWORD_INPUT = By.xpath(
            "//input[@type='password' or @name='password']" +
            " | //*[contains(@class,'auth-form') and contains(@class,'password')]//input"
        )

        private val LOGIN_SUBMIT = By.xpath(
            "//*[contains(@class,'auth-form')]" +
            "//button[not(@disabled) and (@type='submit' or contains(@class,'auth-form__form__submit'))]" +
            " | //form[.//input[@name='email' or @type='email'] and .//input[@type='password']]" +
            "//button[not(@disabled) and (@type='submit' or contains(@class,'btn'))]"
        )

        private val AUTH_FORM = By.xpath(
            "//form[.//input[@name='email'] or .//input[@type='password']]" +
            " | //*[contains(@class,'auth-form')]"
        )

        private val AUTH_ERROR = By.xpath(
            "//form[.//input[@name='email'] or .//input[@type='password']]" +
            "//*[contains(@class,'error') or contains(@class,'invalid') or @role='alert']" +
            " | //*[contains(@class,'auth-form')]" +
            "//*[contains(@class,'error') or contains(@class,'invalid') or @role='alert']"
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
            "//*[self::button or self::a][" +
            "contains(@class,'logout') or contains(@href,'logout') " +
            "or contains(@data-test,'logout') or contains(@data-testid,'logout')]"
        )

        private val LOGOUT_CONFIRM = By.xpath(
            "//*[contains(@class,'logout-modal') or contains(@class,'logout')]" +
            "//button[not(@disabled) and (@type='submit' or contains(@class,'btn') or contains(@class,'primary'))]"
        )

        private val AUTH_SUBMIT = By.xpath(
            "//*[contains(@class,'auth-form')]" +
            "//button[not(@disabled) and (@type='submit' or contains(@class,'auth-form__form__submit') or contains(@class,'btn'))]" +
            " | //form[.//input]" +
            "//button[not(@disabled) and (@type='submit' or contains(@class,'btn'))]"
        )

        private val CODE_INPUT = By.xpath(
            "//input[@name='code' or @inputmode='numeric' or contains(@class,'auth-form-input-code-control')]" +
            " | //*[contains(@class,'auth-form__form__input-code')]//input"
        )
    }

    fun open(): AuthPage {
        openUrl(LOGIN_URL)
        openLoginDialogFromHeader()
        if (waitForLoginForm()) return this

        openUrl(LOGIN_FALLBACK_URL)
        openLoginDialogFromHeader()
        if (waitForLoginForm()) return this

        throw TimeoutException("Форма авторизации не отрисовалась на mirtesen.ru")
    }

    private fun waitForLoginForm(): Boolean {
        return try {
            waitUntil { d ->
                d.findElements(AUTH_FORM).isNotEmpty() ||
                d.findElements(EMAIL_LOGIN_TAB).isNotEmpty() ||
                d.findElements(EMAIL_INPUT).isNotEmpty()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun openLoginDialogFromHeader() {
        if (isPresent(AUTH_FORM)) return
        if (isPresent(HEADER_LOGIN_BUTTON)) {
            try {
                jsClick(HEADER_LOGIN_BUTTON)
            } catch (e: TimeoutException) {
            }
        }
    }

    fun ensureOpened(): AuthPage {
        if (!hasLoginForm()) {
            open()
        }
        return this
    }

    fun openRegistration(): AuthPage {
        openUrl(MainPage.URL)
        if (!isPresent(AUTH_FORM) && isPresent(HEADER_REGISTRATION_BUTTON)) {
            jsClick(HEADER_REGISTRATION_BUTTON)
        }
        waitUntil { d -> d.findElements(AUTH_FORM).isNotEmpty() }
        return this
    }

    fun openEmailRegistration(): AuthPage {
        openRegistration()
        switchToEmailMode()
        waitUntil { d -> d.findElements(EMAIL_INPUT).isNotEmpty() }
        return this
    }

    fun hasRegistrationForm(): Boolean = isPresent(AUTH_FORM)

    fun hasEmailRegistrationForm(): Boolean = isPresent(AUTH_FORM) && isPresent(EMAIL_INPUT)

    fun fillRegistration(data: RegistrationData): AuthPage {
        typeIfVisible(NAME_INPUT, data.name)
        typeIfVisible(LAST_NAME_INPUT, data.lastName)
        typeIfVisible(EMAIL_INPUT, data.email)
        typeIfVisible(PHONE_INPUT, data.phone)
        typeIfVisible(PASSWORD_INPUT, data.password)
        return this
    }

    fun fillEmailRegistration(name: String, email: String): AuthPage {
        switchToEmailMode()
        typeIfVisible(NAME_INPUT, name)
        type(EMAIL_INPUT, email)
        return this
    }

    fun submitRegistration(): AuthPage {
        jsClick(AUTH_SUBMIT)
        return this
    }

    fun waitForRegistrationCodeStep(): Boolean = try {
        waitUntil { isPresent(CODE_INPUT) || isAuthenticated() || isPresent(AUTH_ERROR) }
        isPresent(CODE_INPUT)
    } catch (e: Exception) {
        false
    }

    fun enterRegistrationCode(code: String): AuthPage {
        val codeInput = waitVisible(CODE_INPUT)
        codeInput.clear()
        codeInput.sendKeys(code)
        if (isPresent(AUTH_SUBMIT)) {
            jsClick(AUTH_SUBMIT)
        }
        return this
    }

    fun waitForManualRegistrationCompletion() {
        waitUntil { isAuthenticated() || !isPresent(AUTH_FORM) }
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
        waitUntil { isPresent(AUTHENTICATED_MARKER) || hasAuthTokenCookie() || isPresent(AUTH_ERROR) }
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
        waitUntil { isLoggedOut() }
    }

    private fun hasAuthTokenCookie(): Boolean =
        driver.manage().cookies.any { cookie ->
            val name = cookie.name.lowercase()
            val value = cookie.value.orEmpty()
            name.contains("jwt") ||
                name.contains("refresh") ||
                value.count { it == '.' } >= 2
        }

    private fun switchToEmailMode() {
        if (isPresent(EMAIL_INPUT)) return
        if (isPresent(EMAIL_LOGIN_TAB)) {
            try {
                jsClick(EMAIL_LOGIN_TAB)
            } catch (e: TimeoutException) {
            }
        }
    }

    private fun typeIfVisible(locator: By, value: String?) {
        if (value.isNullOrBlank()) return
        val element = findAll(locator).firstOrNull { it.isDisplayed && it.isEnabled } ?: return
        element.scrollIntoView()
        element.clear()
        element.sendKeys(value)
    }

    private fun WebElement.scrollIntoView() {
        (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView({block:'center'});", this)
    }

    data class RegistrationData(
        val name: String?,
        val lastName: String?,
        val email: String?,
        val phone: String?,
        val password: String?
    )
}

private object WebDriverWaitFactory {
    fun seconds(driver: WebDriver, seconds: Long) =
        org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(seconds))
}
