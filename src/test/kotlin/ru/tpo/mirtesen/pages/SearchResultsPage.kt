package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions

class SearchResultsPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val RESULT_CARDS = By.xpath("//a[contains(@href,'.mirtesen.ru/blog/')]")

        private val RESULT_TITLES = By.xpath(
            "//a[contains(@href,'.mirtesen.ru/blog/')]//*[self::h3 or self::h4]"
        )

        private val FIRST_RESULT_LINK = By.xpath("(//a[contains(@href,'.mirtesen.ru/blog/')])[1]")

        private val NO_RESULTS_MSG = By.xpath(
            "//*[contains(normalize-space(),'ничего не найдено') " +
            "or contains(normalize-space(),'Ничего не найдено') " +
            "or contains(normalize-space(),'Нет результатов')]"
        )

        private val BODY = By.xpath("//body")

        private val SERVER_ERROR = By.xpath(
            "//*[contains(normalize-space(),'Internal Server Error') " +
            "or contains(normalize-space(),'Bad Gateway') " +
            "or contains(normalize-space(),'Service Unavailable') " +
            "or contains(normalize-space(),'Ошибка сервера')]"
        )
    }

    fun waitForSearchPage() {
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY))
        wait.until { getUrl().contains("/search") || getUrl().contains("q=") }
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

    fun hasServerError(): Boolean = isPresent(SERVER_ERROR)

    fun getResultCount(): Int = findAll(RESULT_CARDS).size

    fun getResultTitles(): List<String> = textValues(RESULT_TITLES)

    fun resultsHaveNonEmptyTitles(): Boolean {
        val titles = getResultTitles()
        if (titles.isNotEmpty()) return true
        return textValues(RESULT_CARDS).isNotEmpty()
    }

    fun openFirstResult(): ArticlePage {
        val link = waitClickable(FIRST_RESULT_LINK)
        (driver as JavascriptExecutor).executeScript("arguments[0].click();", link)
        return ArticlePage(driver)
    }

    private fun textValues(locator: By): List<String> {
        repeat(3) {
            try {
                return findAll(locator)
                    .mapNotNull { it.safeText() }
                    .filter { it.isNotBlank() }
            } catch (e: StaleElementReferenceException) {
                // The feed is dynamic; retry by locating elements again.
            }
        }
        return emptyList()
    }

    private fun WebElement.safeText(): String? = try {
        text.trim()
    } catch (e: StaleElementReferenceException) {
        null
    }
}
