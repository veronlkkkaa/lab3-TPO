package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        const val URL = "https://mirtesen.ru/"

        private val COOKIE_ACCEPT = By.xpath(
            "//button[contains(normalize-space(),'Принять') " +
            "or contains(normalize-space(),'принять') " +
            "or contains(normalize-space(),'Согласен') " +
            "or @id='cookie-accept']"
        )

        val POST_CARDS: By = By.xpath("//a[contains(@href,'.mirtesen.ru/blog/')]")

        val POST_TITLES: By = By.xpath(
            "//a[contains(@href,'.mirtesen.ru/blog/')]//h4" +
            " | //a[contains(@href,'.mirtesen.ru/blog/')]//h3" +
            " | //a[contains(@href,'.mirtesen.ru/blog/')]//b" +
            " | //a[contains(@href,'.mirtesen.ru/blog/')]//strong" +
            " | //article[contains(@class,'post-card')]//*[contains(@class,'title')]" +
            " | //article[contains(@class,'post-card')]//*[contains(@class,'post-preview__title')]" +
            " | //article[contains(@class,'post-card')]//h3" +
            " | //article[contains(@class,'post-card')]//h4"
        )

        private val LEFT_MENU = By.xpath("//*[contains(@class,'left-menu')]")

        private val MY_FEED_LINK = By.xpath(
            "//*[contains(@class,'left-menu')]//a[contains(normalize-space(),'Моя лента')]"
        )

        private val RUBRIC_LINKS = By.xpath(
            "//*[contains(@class,'left-menu')]//a[" +
            "(contains(@href,'/topic/') " +
            "or contains(@href,'/tag/') " +
            "or contains(@href,'/popular/') " +
            "or contains(@href,'/category/')) " +
            "and string-length(normalize-space())>0]"
        )
    }

    fun open(): MainPage {
        openUrl(URL)
        try { wait.until(ExpectedConditions.presenceOfElementLocated(POST_CARDS)) } catch (e: Exception) { }
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
        findAll(RUBRIC_LINKS)
            .mapNotNull { it.getAttribute("href") }
            .any { it.isNotBlank() }

    fun postCardsHaveLinks(): Boolean =
        findAll(POST_CARDS)
            .mapNotNull { it.getAttribute("href") }
            .any { it.contains(".mirtesen.ru/blog/") }

    fun hasPostTitles(): Boolean {
        val byTag = textValues(POST_TITLES).any { it.isNotEmpty() }
        if (byTag) return true
        return textValues(POST_CARDS).any { it.isNotEmpty() }
    }

    fun openFirstPost(): ArticlePage {
        val href = findAll(POST_CARDS)
            .mapNotNull { it.getAttribute("href") }
            .firstOrNull { it.contains(".mirtesen.ru/blog/") }
            ?: throw NoSuchElementException("No post link found on main feed")
        openUrl(normalizePostUrl(href))
        return ArticlePage(driver)
    }

    fun openFirstRubric(): RubricPage {
        val href = findAll(RUBRIC_LINKS)
            .mapNotNull { it.getAttribute("href") }
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

    private fun WebElement.safeText(): String? = try {
        text.trim().ifBlank { getAttribute("textContent")?.trim() }
    } catch (e: StaleElementReferenceException) {
        null
    }
}
