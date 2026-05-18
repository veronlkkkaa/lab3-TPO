package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
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

    protected fun isPresent(locator: By): Boolean = try {
        shortWait.until(ExpectedConditions.presenceOfElementLocated(locator))
        true
    } catch (e: Exception) {
        false
    }

    protected fun findAll(locator: By): List<WebElement> = driver.findElements(locator)

    fun getUrl(): String = driver.currentUrl

    fun getTitle(): String = driver.title
}
