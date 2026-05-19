package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

class RubricPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val POST_CARDS = By.xpath("//a[contains(@href,'.mirtesen.ru/blog/')]")

        private val RUBRIC_MARKER = By.xpath(
            "//*[contains(@class,'topic') " +
            "or contains(@class,'rubric') " +
            "or contains(@class,'post-card')]"
        )
    }

    fun waitForPage(): RubricPage {
        waitUntil { d ->
            d.findElements(POST_CARDS).isNotEmpty() || d.findElements(RUBRIC_MARKER).isNotEmpty()
        }
        return this
    }

    fun hasPostCards(): Boolean = findAll(POST_CARDS).isNotEmpty()
}
