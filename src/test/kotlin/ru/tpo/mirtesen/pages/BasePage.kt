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

    companion object {
        private const val WAIT_TIMEOUT_MS = 30_000L
        private const val WAIT_POLLING_MS = 200L
    }

    protected fun waitUntil(condition: (WebDriver) -> Boolean) {
        wait().until { d -> condition(d) }
    }

    protected fun waitVisible(locator: By): WebElement {
        return wait().until(ExpectedConditions.visibilityOfElementLocated(locator))
    }

    protected fun waitClickable(locator: By): WebElement {
        return wait().until(ExpectedConditions.elementToBeClickable(locator))
    }

    protected fun openUrl(url: String) {
        try {
            driver.get(url)
        } catch (e: TimeoutException) {
            Thread.sleep(500)
            driver.get(url)
        }
        waitUntil { d ->
            val readyState = (d as JavascriptExecutor)
                .executeScript("return document.readyState")
            readyState == "interactive" || readyState == "complete"
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

    private fun wait() =
        WebDriverWait(driver, Duration.ofMillis(WAIT_TIMEOUT_MS))
            .pollingEvery(Duration.ofMillis(WAIT_POLLING_MS))
            .ignoring(WebDriverException::class.java)
}
