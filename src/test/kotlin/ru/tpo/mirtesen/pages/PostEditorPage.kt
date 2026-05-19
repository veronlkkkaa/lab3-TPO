package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions

class PostEditorPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val OPEN_EDITOR = By.xpath(
            "//button[contains(normalize-space(),'Написать') " +
            "or contains(normalize-space(),'Создать публикацию') " +
            "or contains(normalize-space(),'Создать пост') " +
            "or contains(normalize-space(),'Добавить публикацию')]" +
            " | //a[contains(normalize-space(),'Написать') " +
            "or contains(normalize-space(),'Создать публикацию') " +
            "or contains(normalize-space(),'Создать пост') " +
            "or contains(@href,'/editor') " +
            "or contains(@href,'/post/add') " +
            "or contains(@href,'/post/create')]"
        )

        private val TITLE_INPUT = By.xpath(
            "//input[contains(@placeholder,'Заголов') " +
            "or contains(@aria-label,'Заголов') " +
            "or @name='title']" +
            " | //textarea[contains(@placeholder,'Заголов') " +
            "or contains(@aria-label,'Заголов') " +
            "or @name='title']"
        )

        private val BODY_INPUT = By.xpath(
            "//*[@contenteditable='true' and (" +
            "contains(@class,'mt-lexical-input') " +
            "or contains(@class,'editor') " +
            "or contains(@aria-label,'текст') " +
            "or contains(@aria-label,'Текст')" +
            ")]" +
            " | //textarea[contains(@placeholder,'Текст') " +
            "or contains(@placeholder,'текст') " +
            "or contains(@aria-label,'Текст') " +
            "or contains(@aria-label,'текст')]"
        )

        private val PUBLISH_BUTTON = By.xpath(
            "//button[contains(normalize-space(),'Опубликовать') " +
            "or contains(normalize-space(),'Сохранить')]" +
            " | //a[contains(normalize-space(),'Опубликовать') " +
            "or contains(normalize-space(),'Сохранить')]"
        )

        private val AUTH_FORM = By.xpath(
            "//*[contains(@class,'auth-form') or contains(normalize-space(),'Вход по почте')]"
        )
    }

    fun openFromMainPage(): PostEditorPage {
        openUrl(MainPage.URL)
        if (!isPresent(OPEN_EDITOR)) {
            return this
        }
        try {
            jsClick(OPEN_EDITOR)
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(TITLE_INPUT),
                    ExpectedConditions.presenceOfElementLocated(BODY_INPUT),
                    ExpectedConditions.presenceOfElementLocated(AUTH_FORM)
                )
            )
        } catch (e: TimeoutException) {
            return this
        }
        return this
    }

    fun fill(title: String, body: String): PostEditorPage {
        if (isPresent(TITLE_INPUT)) {
            type(TITLE_INPUT, title)
        }
        val bodyElement = waitVisible(BODY_INPUT)
        bodyElement.click()
        bodyElement.sendKeys(body)
        return this
    }

    fun publish(): PostEditorPage {
        jsClick(PUBLISH_BUTTON)
        return this
    }

    fun hasEditor(): Boolean = isPresent(TITLE_INPUT) || isPresent(BODY_INPUT)

    fun hasPublishAction(): Boolean = isPresent(PUBLISH_BUTTON)

    fun requiresAuth(): Boolean = isPresent(AUTH_FORM)

    fun bodyText(): String = findAll(BODY_INPUT).firstOrNull()?.visibleText().orEmpty()

    private fun WebElement.visibleText(): String = text.trim()
}
