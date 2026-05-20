package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class AuthPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private const val DEFAULT_WAIT_TIMEOUT_MS = 30_000L
        private const val REGISTRATION_EMAIL_TIMEOUT_MS = 120_000L
        private const val REGISTRATION_VALIDATION_TIMEOUT_MS = 10_000L
        private const val MANUAL_REGISTRATION_TIMEOUT_MS = 300_000L

        private const val LOGIN_URL = "${MainPage.URL}?auth=login"
        private const val LOGIN_FALLBACK_URL = "${MainPage.URL}login"

        private val HEADER_LOGIN_BUTTON = By.xpath(
            "//*[contains(@class,'header-login')]//*[contains(@class,'btn-secondary')]"
        )

        private val HEADER_REGISTRATION_BUTTON = By.xpath(
            "//*[contains(@class,'header-login')]//*[contains(@class,'btn-primary')]"
        )

        private val EMAIL_LOGIN_TAB = By.xpath(
            "//*[contains(@class,'auth-form')]" +
            "//*[contains(@class,'auth-form__form__submit-secondary')]" +
            "//*[self::button or self::a]" +
            " | //*[contains(@class,'auth-form')]" +
            "//*[self::button or self::a][" +
            "contains(@class,'email') or contains(@class,'mail') or contains(@href,'email') or contains(@href,'mail')]"
        )

        private val EMAIL_INPUT = By.xpath(
            "//*[contains(@class,'auth-form')]//input[@type='email' or @name='email']"
        )

        private val NAME_INPUT = By.xpath(
            "//*[contains(@class,'auth-form')]//input[@name='name']"
        )

        private val PASSWORD_INPUT = By.xpath(
            "//*[contains(@class,'auth-form')]//input[@type='password' or @name='password']"
        )

        private val LOGIN_SUBMIT = By.xpath(
            "//*[contains(@class,'auth-form__form__submit')]//*[contains(@class,'btn-primary')]"
        )

        private val AUTH_FORM = By.xpath(
            "//*[contains(@class,'auth-form')]"
        )

        private val AUTH_ERROR = By.xpath(
            "//*[contains(@class,'auth-form')]" +
            "//*[contains(@class,'label-error') or contains(@class,'form-error') or @role='alert']"
        )

        private val AUTHENTICATED_MARKER = By.xpath(
            "//*[contains(@class,'user-menu')]" +
            " | //*[contains(@class,'header')]//*[contains(@class,'profile') or contains(@class,'avatar')]"
        )

        private val USER_MENU_TRIGGER = By.xpath(
            "//*[contains(@class,'user-menu')]" +
            " | //*[contains(@class,'header')]//*[contains(@class,'profile') or contains(@class,'avatar')]"
        )

        private val LOGOUT_ACTION = By.xpath(
            "//*[contains(@class,'logout') or contains(@href,'logout')]"
        )

        private val LOGOUT_CONFIRM = By.xpath(
            "//*[contains(@class,'logout-modal')]//*[contains(@class,'btn-primary')]"
        )

        private val AUTH_SUBMIT = By.xpath(
            "//*[contains(@class,'auth-form__form__submit')]//*[contains(@class,'btn-primary')]"
        )

        private val REGISTRATION_EMAIL_SENT = By.xpath(
            "//*[contains(@class,'auth-form__form__body')]//a[starts-with(@href,'mailto:')]"
        )

        private val REGISTRATION_EMAIL_SENT_ADDRESS = By.xpath(
            "//*[contains(@class,'auth-form__form__body')]//a[starts-with(@href,'mailto:')]"
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

    fun hasEmailRegistrationForm(): Boolean = isPresent(AUTH_FORM) && isPresent(EMAIL_INPUT)

    fun fillEmailRegistration(name: String, email: String): AuthPage {
        switchToEmailMode()
        typeIfVisible(NAME_INPUT, name)
        type(EMAIL_INPUT, email)
        return this
    }

    fun fillRegistrationName(name: String): AuthPage {
        typeVisible(NAME_INPUT, name)
        return this
    }

    fun fillRegistrationEmail(email: String): AuthPage {
        type(EMAIL_INPUT, email)
        return this
    }

    fun registrationNameValue(): String = visibleInputValue(NAME_INPUT)

    fun registrationEmailValue(): String = visibleInputValue(EMAIL_INPUT)

    fun submitRegistration(): AuthPage {
        jsClick(AUTH_SUBMIT)
        return this
    }

    fun waitForRegistrationEmailSent(): Boolean {
        waitUntilWithin(REGISTRATION_EMAIL_TIMEOUT_MS) {
            isPresent(REGISTRATION_EMAIL_SENT) || hasAuthError()
        }
        return isPresent(REGISTRATION_EMAIL_SENT)
    }

    fun waitForRegistrationValidationResult(): Boolean {
        waitUntilWithin(REGISTRATION_VALIDATION_TIMEOUT_MS) {
            isPresent(REGISTRATION_EMAIL_SENT) || hasAuthError() || hasInvalidRegistrationInput()
        }
        return isPresent(REGISTRATION_EMAIL_SENT)
    }

    fun hasInvalidRegistrationInput(): Boolean =
        findAll(NAME_INPUT).any { it.isDisplayed && !it.isValidFormInput() } ||
            findAll(EMAIL_INPUT).any { it.isDisplayed && !it.isValidFormInput() }

    fun registrationEmailSentAddress(): String =
        findAll(REGISTRATION_EMAIL_SENT_ADDRESS)
            .firstOrNull { it.isDisplayed }
            ?.getAttribute("href")
            ?.removePrefix("mailto:")
            ?.trim()
            .orEmpty()

    fun openRegistrationAcceptUrl(url: String): AuthPage {
        if (url.isNotBlank()) {
            openUrl(url)
        }
        return this
    }

    fun waitForManualRegistrationCompletion() {
        waitUntilWithin(MANUAL_REGISTRATION_TIMEOUT_MS) {
            hasRegistrationEmailSentScreenGoneAfterConfirmation() || hasAuthError()
        }
    }

    fun hasRegistrationEmailSentScreenGoneAfterConfirmation(): Boolean =
        !isPresent(REGISTRATION_EMAIL_SENT) &&
            !hasAuthError() &&
            (isAuthenticated() || !isPresent(AUTH_FORM) || driver.currentUrl.contains("/action/accept/"))

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
        waitUntilWithin(DEFAULT_WAIT_TIMEOUT_MS) {
            isPresent(AUTHENTICATED_MARKER) || hasAuthTokenCookie() || hasAuthError()
        }
        return MainPage(driver)
    }

    fun hasLoginForm(): Boolean = isPresent(AUTH_FORM) || isPresent(EMAIL_INPUT)

    fun hasAuthError(): Boolean = authErrorText().isNotBlank()

    fun authErrorText(): String =
        findAll(AUTH_ERROR)
            .mapNotNull { element ->
                try {
                    if (element.isDisplayed) element.visibleText() else null
                } catch (e: WebDriverException) {
                    null
                }
            }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString("; ")

    fun isAuthenticated(): Boolean =
        !hasAuthError() && (!isPresent(AUTH_FORM) || isPresent(AUTHENTICATED_MARKER) || hasAuthTokenCookie())

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
            name.contains("jwt") ||
                name.contains("refresh")
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

    private fun typeVisible(locator: By, value: String) {
        val element = waitVisible(locator)
        element.scrollIntoView()
        element.clear()
        element.sendKeys(value)
    }

    private fun visibleInputValue(locator: By): String =
        findAll(locator)
            .firstOrNull { it.isDisplayed }
            ?.getAttribute("value")
            ?.trim()
            .orEmpty()

    private fun WebElement.scrollIntoView() {
        (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView({block:'center'});", this)
    }

    private fun WebElement.visibleText(): String =
        text.trim().ifBlank {
            getAttribute("textContent")?.trim().orEmpty()
        }

    private fun WebElement.isValidFormInput(): Boolean =
        (driver as JavascriptExecutor).executeScript("return arguments[0].validity.valid;", this) as Boolean

    private fun waitUntilWithin(timeoutMs: Long, condition: () -> Boolean): Boolean {
        return try {
            WebDriverWait(driver, Duration.ofMillis(timeoutMs))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(WebDriverException::class.java)
                .until { condition() }
            true
        } catch (e: TimeoutException) {
            condition()
        }
    }
}
