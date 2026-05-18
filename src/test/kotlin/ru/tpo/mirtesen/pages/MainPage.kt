package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
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
            " | //a[contains(@href,'.mirtesen.ru/blog/')]//strong"
        )
    }

    fun open(): MainPage {
        driver.get(URL)
        try { wait.until(ExpectedConditions.presenceOfElementLocated(POST_CARDS)) } catch (e: Exception) { }
        dismissDialogs()
        return this
    }

    fun search(query: String): SearchResultsPage {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8)
        driver.get("${URL}search/posts/?q=$encoded")
        return SearchResultsPage(driver)
    }

    fun hasPostCards(): Boolean = findAll(POST_CARDS).isNotEmpty()

    fun hasPostTitles(): Boolean {
        val byTag = findAll(POST_TITLES).map { it.text.trim() }.any { it.isNotEmpty() }
        if (byTag) return true
        return findAll(POST_CARDS).map { it.text.trim() }.any { it.isNotEmpty() }
    }

    private fun dismissDialogs() {
        if (isPresent(COOKIE_ACCEPT)) {
            try { driver.findElement(COOKIE_ACCEPT).click() } catch (e: Exception) { }
        }
    }
}
