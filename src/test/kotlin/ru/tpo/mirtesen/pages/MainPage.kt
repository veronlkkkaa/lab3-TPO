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
            "//*[@id='cookie-accept' or @data-testid='cookie-accept' or @data-test='cookie-accept']" +
            " | //*[contains(@class,'cookie')]" +
            "//*[self::button or self::a][contains(@class,'accept') or @type='submit']"
        )

        val POST_CARDS: By = By.xpath(
            "//article[contains(concat(' ', normalize-space(@class), ' '), ' post-card ')]"
        )

        val POST_TITLES: By = By.xpath(
            "//article[contains(concat(' ', normalize-space(@class), ' '), ' post-card ')]" +
            "//*[self::h1 or self::h2 or self::h3]" +
            "[contains(@class,'post-preview__title') or string-length(normalize-space())>0]"
        )

        private val FIRST_POST_LINK = By.xpath(
            "//article[contains(concat(' ', normalize-space(@class), ' '), ' post-card ')]" +
            "//a[contains(@href,'.mirtesen.ru/blog/') or starts-with(@href,'//') and contains(@href,'/blog/')]"
        )

        private val LEFT_MENU = By.xpath("//*[contains(@class,'left-menu')]")

        private val MY_FEED_LINK = By.xpath(
            "//*[contains(@class,'left-menu')]//a[" +
            "contains(@class,'left-menu__item_title-anchor') or @href='https://mirtesen.ru/' or @href='//mirtesen.ru/']"
        )

        private val RUBRIC_LINKS = By.xpath(
            "//*[contains(@class,'left-menu')]//a[" +
            "(contains(@href,'/topic/') or contains(@href,'/tag/') or contains(@href,'topic/')) " +
            "and string-length(normalize-space())>0]"
        )
    }

    fun open(): MainPage {
        openUrl(URL)
        try { waitUntil { d -> d.findElements(POST_CARDS).isNotEmpty() } } catch (e: Exception) { }
        dismissDialogs()
        return this
    }

    fun search(query: String): SearchResultsPage {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8)
        openUrl("${URL}search/posts/?q=$encoded")
        return SearchResultsPage(driver)
    }

    fun hasPostCards(): Boolean = findAll(POST_CARDS).isNotEmpty()

    fun hasLeftNavigationBlock(): Boolean = isPresent(LEFT_MENU)

    fun hasMyFeedLink(): Boolean = isPresent(MY_FEED_LINK)

    fun hasRubricLinks(): Boolean =
        hrefValues(RUBRIC_LINKS)
            .any { it.isNotBlank() }

    fun postCardsHaveLinks(): Boolean =
        hrefValues(FIRST_POST_LINK)
            .any { it.contains(".mirtesen.ru/blog/") }

    fun hasPostTitles(): Boolean {
        val byTag = textValues(POST_TITLES).any { it.isNotEmpty() }
        if (byTag) return true
        return textValues(POST_CARDS).any { it.isNotEmpty() }
    }

    fun openFirstPost(): ArticlePage {
        val href = hrefValues(FIRST_POST_LINK)
            .firstOrNull { it.contains(".mirtesen.ru/blog/") }
            ?: throw NoSuchElementException("No post link found on main feed")
        openUrl(normalizePostUrl(href))
        return ArticlePage(driver)
    }

    fun openFirstRubric(): RubricPage {
        val href = hrefValues(RUBRIC_LINKS)
            .firstOrNull { it.isNotBlank() }
            ?: throw NoSuchElementException("No rubric link found in left menu")
        openUrl(normalizePostUrl(href))
        return RubricPage(driver).waitForPage()
    }

    private fun dismissDialogs() {
        if (isPresent(COOKIE_ACCEPT)) {
            try { driver.findElement(COOKIE_ACCEPT).click() } catch (e: Exception) { }
        }
    }

    private fun normalizePostUrl(href: String): String = when {
        href.startsWith("//") -> "https:$href"
        href.startsWith("/") -> URL.trimEnd('/') + href
        else -> href
    }

    private fun textValues(locator: By): List<String> =
        findAll(locator).mapNotNull { it.safeText() }

    private fun hrefValues(locator: By): List<String> {
        repeat(3) {
            try {
                return findAll(locator).mapNotNull { it.getAttribute("href") }
            } catch (e: StaleElementReferenceException) {
            }
        }
        return emptyList()
    }

    private fun WebElement.safeText(): String? = try {
        text.trim().ifBlank { getAttribute("textContent")?.trim() }
    } catch (e: StaleElementReferenceException) {
        null
    }
}
