package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        const val URL = "https://mirtesen.ru/"

        private val COOKIE_ACCEPT = By.xpath(
            "//*[@id='cookie-accept']" +
                    " | //*[contains(@class,'cookie')]//*[self::button or self::a][contains(@class,'accept') or @type='submit']"
        )

        val POST_CARDS: By = By.xpath(
            "//div[contains(@class,'person-feed')]//article[contains(@class,'post-card')]"
        )

        val POST_TITLES: By = By.xpath(
            "//div[contains(@class,'person-feed')]//article[contains(@class,'post-card')]" +
                    "//h3[contains(@class,'post-preview__title')]"
        )

        private val FIRST_POST_LINK = By.xpath(
            "(//div[contains(@class,'person-feed')]//article[contains(@class,'post-card')]" +
                    "//a[contains(@class,'post-preview__overlay') and contains(@href,'/blog/')])[1]"
        )

        private val LEFT_MENU = By.xpath(
            "//div[contains(@class,'left-menu')]"
        )

        private val MY_FEED_LINK = By.xpath(
            "//div[contains(@class,'left-menu')]" +
                    "//a[contains(@class,'left-menu__item_title-anchor') and normalize-space()='Моя лента']"
        )

        private val RUBRIC_LINKS = By.xpath(
            "//*[@id='topics-menu']//a[contains(@class,'left-menu__item_element') and contains(@href,'/topic/')]"
        )
    }

    fun open(): MainPage {
        openUrl(URL)

        try {
            waitUntil { d -> d.findElements(POST_CARDS).isNotEmpty() }
        } catch (e: Exception) {
        }

        dismissDialogs()
        return this
    }

    fun search(query: String): SearchResultsPage {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8)
        openUrl("${URL}search/posts/?q=$encoded")
        return SearchResultsPage(driver)
    }

    fun searchPeople(query: String): SearchResultsPage {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8)
        openUrl("${URL}search/people/?q=$encoded")
        return SearchResultsPage(driver)
    }

    fun hasPostCards(): Boolean =
        findAll(POST_CARDS).isNotEmpty()

    fun hasLeftNavigationBlock(): Boolean =
        isPresent(LEFT_MENU)

    fun hasMyFeedLink(): Boolean =
        isPresent(MY_FEED_LINK)

    fun hasRubricLinks(): Boolean =
        hrefValues(RUBRIC_LINKS).any { it.isNotBlank() }

    fun postCardsHaveLinks(): Boolean =
        hrefValues(FIRST_POST_LINK).any { it.contains("/blog/") }

    fun hasPostTitles(): Boolean {
        val titles = textValues(POST_TITLES)
        if (titles.any { it.isNotBlank() }) return true

        return textValues(POST_CARDS).any { it.isNotBlank() }
    }

    fun openFirstPost(): ArticlePage {
        val href = hrefValues(FIRST_POST_LINK)
            .firstOrNull { it.contains("/blog/") }
            ?: throw NoSuchElementException("No post link found on main feed")

        openUrl(normalizeUrl(href))
        return ArticlePage(driver)
    }

    fun openFirstRubric(): RubricPage {
        val href = hrefValues(RUBRIC_LINKS)
            .firstOrNull { it.isNotBlank() }
            ?: throw NoSuchElementException("No rubric link found in left menu")

        openUrl(normalizeUrl(href))
        return RubricPage(driver).waitForPage()
    }

    private fun dismissDialogs() {
        if (isPresent(COOKIE_ACCEPT)) {
            try {
                driver.findElement(COOKIE_ACCEPT).click()
            } catch (e: Exception) {
            }
        }
    }

    private fun normalizeUrl(href: String): String = when {
        href.startsWith("//") -> "https:$href"
        href.startsWith("/") -> URL.trimEnd('/') + href
        else -> href
    }

    private fun textValues(locator: By): List<String> {
        repeat(3) {
            val values = findAll(locator)
                .mapNotNull { it.safeText() }
                .filter { it.isNotBlank() }

            if (values.isNotEmpty()) return values
        }

        return emptyList()
    }

    private fun hrefValues(locator: By): List<String> {
        repeat(3) {
            try {
                val values = findAll(locator)
                    .mapNotNull { it.getAttribute("href") }
                    .filter { it.isNotBlank() }

                if (values.isNotEmpty()) return values
            } catch (e: StaleElementReferenceException) {
            }
        }

        return emptyList()
    }

    private fun WebElement.safeText(): String? = try {
        text.trim().ifBlank {
            getAttribute("textContent")?.trim()
        }
    } catch (e: StaleElementReferenceException) {
        null
    }
}