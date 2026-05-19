package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class SearchResultsPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val RESULT_CARDS = By.xpath("//a[contains(@href,'.mirtesen.ru/blog/')]")

        private val RESULT_TITLES = By.xpath(
            "//a[contains(@href,'.mirtesen.ru/blog/')]//*[self::h3 or self::h4]"
        )

        private val FIRST_RESULT_LINK = By.xpath(
            "(//a[contains(@href,'.mirtesen.ru/blog/')])[1]"
        )

        private val NO_RESULTS_MSG = By.xpath(
            "//*[contains(@class,'empty') or contains(@class,'not-found') or contains(@class,'no-results')]" +
            " | //*[contains(@data-test,'empty') or contains(@data-testid,'empty')]" +
            " | //*[contains(@data-test,'no-results') or contains(@data-testid,'no-results')]"
        )

        private val BODY = By.xpath("//body")

        private val SERVER_ERROR = By.xpath(
            "//*[contains(@class,'server-error') or contains(@class,'service-error') or contains(@class,'error-page')]" +
            " | //*[contains(@data-test,'server-error') or contains(@data-testid,'server-error')]"
        )
    }

    fun waitForSearchPage() {
        waitUntil { d -> d.findElements(BODY).isNotEmpty() }
        waitUntil { getUrl().contains("/search") || getUrl().contains("q=") }
    }

    fun waitForResults() {
        waitUntil { d ->
            d.findElements(RESULT_CARDS).isNotEmpty() || d.findElements(NO_RESULTS_MSG).isNotEmpty()
        }
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
