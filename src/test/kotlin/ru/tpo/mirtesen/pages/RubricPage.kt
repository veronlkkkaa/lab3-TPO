package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions

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
        wait.until(
            ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(POST_CARDS),
                ExpectedConditions.presenceOfElementLocated(RUBRIC_MARKER)
            )
        )
        return this
    }

    fun hasPostCards(): Boolean = findAll(POST_CARDS).isNotEmpty()
}
