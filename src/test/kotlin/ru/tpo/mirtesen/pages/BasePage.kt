package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.WebDriverException

abstract class BasePage(protected val driver: WebDriver) {

    private val navigationLock = Any()
    private var lastNavigationAt = 0L

    companion object {
        private const val NAVIGATION_PAUSE_MS = 800L
        private const val WAIT_TIMEOUT_MS = 30_000L
    }

    protected fun waitUntil(condition: (WebDriver) -> Boolean) {
        val deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS
        while (!condition(driver)) {
            if (System.currentTimeMillis() >= deadline) {
                throw TimeoutException("Condition was not met within ${WAIT_TIMEOUT_MS}ms")
            }
            awaitNextPoll()
        }
    }

    protected fun waitVisible(locator: By): WebElement {
        waitUntil { d ->
            val el = d.findElements(locator).firstOrNull() ?: return@waitUntil false
            el.isDisplayed
        }
        return driver.findElements(locator).first { it.isDisplayed }
    }

    protected fun waitClickable(locator: By): WebElement {
        waitUntil { d ->
            val el = d.findElements(locator).firstOrNull() ?: return@waitUntil false
            el.isDisplayed && el.isEnabled
        }
        return driver.findElements(locator).first { it.isDisplayed && it.isEnabled }
    }

    protected fun openUrl(url: String) {
        var success = false
        while (!success) {
            try {
                throttleNavigation()
                driver.get(url)
                waitUntil { d ->
                    val readyState = (d as JavascriptExecutor)
                        .executeScript("return document.readyState")
                    readyState == "interactive" || readyState == "complete"
                }
                success = true
            } catch (e: WebDriverException) {
                if (!isTransientNetworkError(e)) throw e
                stopLoading()
            }
        }
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

    protected fun isPresent(locator: By): Boolean =
        driver.findElements(locator).isNotEmpty()

    protected fun findAll(locator: By): List<WebElement> = driver.findElements(locator)

    fun getUrl(): String = driver.currentUrl

    private fun awaitNextPoll() {
        try {
            (driver as JavascriptExecutor).executeAsyncScript(
                "var done = arguments[0]; setTimeout(done, 200);"
            )
        } catch (_: WebDriverException) { }
    }

    private fun throttleNavigation() {
        synchronized(navigationLock) {
            val deadline = lastNavigationAt + NAVIGATION_PAUSE_MS
            while (System.currentTimeMillis() < deadline) {
                awaitNextPoll()
            }
            lastNavigationAt = System.currentTimeMillis()
        }
    }

    private fun stopLoading() {
        try {
            (driver as JavascriptExecutor).executeScript("window.stop();")
        } catch (ignored: WebDriverException) { }
        try {
            throttleNavigation()
            driver.navigate().to("about:blank")
        } catch (ignored: WebDriverException) { }
    }

    private fun isTransientNetworkError(error: Throwable?): Boolean {
        if (error == null) return false
        val message = error.message.orEmpty().lowercase()
        return message.contains("err_connection_reset") ||
            message.contains("err_connection_refused") ||
            message.contains("err_connection_closed") ||
            message.contains("err_empty_response") ||
            message.contains("err_timed_out") ||
            message.contains("err_http2_protocol_error") ||
            message.contains("nssfailure") ||
            message.contains("connection reset") ||
            message.contains("connection refused") ||
            message.contains("connection closed") ||
            message.contains("empty response") ||
            message.contains("navigation timed out") ||
            message.contains("timed out after") ||
            message.contains("read timed out") ||
            message.contains("timeoutexception") ||
            isTransientNetworkError(error.cause)
    }
}
