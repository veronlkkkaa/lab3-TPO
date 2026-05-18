package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions

class SearchResultsPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val RESULT_CARDS = By.xpath("//a[contains(@href,'.mirtesen.ru/blog/')]")

        private val RESULT_TITLES = By.xpath("//a[contains(@href,'.mirtesen.ru/blog/')]//h4")

        private val FIRST_RESULT_LINK = By.xpath("(//a[contains(@href,'.mirtesen.ru/blog/')])[1]")

        private val NO_RESULTS_MSG = By.xpath(
            "//*[contains(normalize-space(),'ничего не найдено') " +
            "or contains(normalize-space(),'не найдено') " +
            "or contains(normalize-space(),'нет результатов')]"
        )
    }

    fun waitForResults() {
        wait.until(
            ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(RESULT_CARDS),
                ExpectedConditions.presenceOfElementLocated(NO_RESULTS_MSG)
            )
        )
    }

    fun hasResults(): Boolean = findAll(RESULT_CARDS).isNotEmpty()

    fun hasNoResultsMessage(): Boolean = isPresent(NO_RESULTS_MSG)

    fun getResultCount(): Int = findAll(RESULT_CARDS).size

    fun getResultTitles(): List<String> = findAll(RESULT_TITLES)
        .map { it.text }
        .filter { it.isNotBlank() }

    fun openFirstResult(): ArticlePage {
        val link = waitClickable(FIRST_RESULT_LINK)
        (driver as JavascriptExecutor).executeScript("arguments[0].click();", link)
        return ArticlePage(driver)
    }
}
