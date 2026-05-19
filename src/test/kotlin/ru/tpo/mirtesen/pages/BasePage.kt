package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

abstract class BasePage(protected val driver: WebDriver) {

    protected val wait: WebDriverWait = WebDriverWait(driver, Duration.ofSeconds(30))
    protected val shortWait: WebDriverWait = WebDriverWait(driver, Duration.ofSeconds(5))

    protected fun waitVisible(locator: By): WebElement =
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator))

    protected fun waitClickable(locator: By): WebElement =
        wait.until(ExpectedConditions.elementToBeClickable(locator))

    protected fun openUrl(url: String) {
        var lastError: WebDriverException? = null
        val openWait = WebDriverWait(driver, Duration.ofSeconds(75))
        openWait.pollingEvery(Duration.ofSeconds(2))

        try {
            openWait.until {
                try {
                    driver.get(url)
                    true
                } catch (e: WebDriverException) {
                    lastError = e
                    if (!isTransientNetworkError(e)) {
                        throw e
                    }
                    try {
                        driver.navigate().to("about:blank")
                    } catch (ignored: WebDriverException) {
                    }
                    false
                }
            }
            return
        } catch (e: TimeoutException) {
            throw lastError ?: e
        }
    }

    protected fun click(locator: By) {
        waitClickable(locator).click()
    }

    protected fun jsClick(locator: By) {
        val element = waitClickable(locator)
        (driver as JavascriptExecutor).executeScript("arguments[0].click();", element)
    }

    protected fun type(locator: By, value: String) {
        val element = waitVisible(locator)
        element.clear()
        element.sendKeys(value)
    }

    protected fun isPresent(locator: By): Boolean = try {
        shortWait.until(ExpectedConditions.presenceOfElementLocated(locator))
        true
    } catch (e: Exception) {
        false
    }

    protected fun findAll(locator: By): List<WebElement> = driver.findElements(locator)

    fun getUrl(): String = driver.currentUrl

    fun getTitle(): String = driver.title

    private fun isTransientNetworkError(error: Throwable?): Boolean {
        if (error == null) return false
        val message = error.message.orEmpty().lowercase()
        return message.contains("err_connection_reset") ||
            message.contains("err_connection_refused") ||
            message.contains("nssfailure") ||
            message.contains("connection reset") ||
            message.contains("connection refused") ||
            message.contains("timeoutexception") ||
            isTransientNetworkError(error.cause)
    }
}
