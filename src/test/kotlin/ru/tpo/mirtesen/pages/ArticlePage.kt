package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

class ArticlePage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val TITLE = By.xpath("//h1")
    }

    fun hasTitle(): Boolean = try {
        waitUntil { d -> d.findElements(TITLE).isNotEmpty() }
        driver.findElement(TITLE).text.trim().isNotEmpty()
    } catch (e: Exception) {
        false
    }

}
