package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions

class ArticlePage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val TITLE = By.xpath("//h1")

        private val CONTENT = By.xpath(
            "//div[@class='blog_content']" +
            " | //div[contains(@class,'blog_content')]" +
            " | //div[contains(@class,'post_body')]" +
            " | //div[contains(@class,'post-body')]" +
            " | //div[contains(@class,'post-content')]" +
            " | //div[contains(@class,'post_content')]" +
            " | //div[contains(@class,'article-body')]" +
            " | //div[contains(@class,'article_body')]" +
            " | //div[contains(@class,'entry-content')]" +
            " | //article//p[string-length(normalize-space())>50]" +
            " | //p[string-length(normalize-space())>100]"
        )

        private val AUTHOR = By.xpath("//a[contains(@href,'mirtesen.ru/people/')]")

        private val PUBLISH_DATE = By.xpath(
            "//time" +
            " | //span[contains(@class,'date')]" +
            " | //span[contains(@class,'time')]"
        )

        private val COMMENTS_SECTION = By.xpath(
            "//div[contains(@class,'comment')]" +
            " | //section[contains(@id,'comment')]" +
            " | //div[contains(@id,'comment')]"
        )
    }

    fun hasTitle(): Boolean = try {
        wait.until(ExpectedConditions.presenceOfElementLocated(TITLE))
        driver.findElement(TITLE).text.trim().isNotEmpty()
    } catch (e: Exception) {
        false
    }

    fun getTitleText(): String = waitVisible(TITLE).text.trim()

    fun hasContent(): Boolean = try {
        wait.until(ExpectedConditions.presenceOfElementLocated(CONTENT))
        true
    } catch (e: Exception) {
        false
    }

    fun hasAuthor(): Boolean = isPresent(AUTHOR)

    fun hasDate(): Boolean = isPresent(PUBLISH_DATE)

    fun hasCommentsSection(): Boolean = isPresent(COMMENTS_SECTION)
}
