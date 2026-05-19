package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class PostEditorPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val OPEN_EDITOR = By.xpath(
            "//button[contains(normalize-space(),'Написать') " +
            "or contains(normalize-space(),'Создать публикацию') " +
            "or contains(normalize-space(),'Создать пост')]" +
            " | //a[contains(normalize-space(),'Написать') " +
            "or contains(@href,'/editor') " +
            "or contains(@href,'/post/add') " +
            "or contains(@href,'/post/create')]"
        )

        private val TITLE_INPUT = By.xpath(
            "//input[@name='title' or contains(@placeholder,'Заголов')]" +
            " | //textarea[@name='title' or contains(@placeholder,'Заголов')]"
        )

        private val BODY_INPUT = By.xpath(
            "//*[@contenteditable='true']" +
            " | //textarea[contains(@placeholder,'Текст')]"
        )

        private val PUBLISH_BUTTON = By.xpath(
            "//button[contains(normalize-space(),'Опубликовать') " +
            "or contains(normalize-space(),'Сохранить')]" +
            " | //a[contains(normalize-space(),'Опубликовать') " +
            "or contains(normalize-space(),'Сохранить')]"
        )

        private val AUTH_FORM = By.xpath("//*[contains(normalize-space(),'Вход по почте')]")
    }

    fun openFromMainPage(): PostEditorPage {
        openUrl(MainPage.URL)
        if (!isPresent(OPEN_EDITOR)) {
            return this
        }
        jsClick(OPEN_EDITOR)
        waitUntil { d ->
            d.findElements(TITLE_INPUT).isNotEmpty() ||
            d.findElements(BODY_INPUT).isNotEmpty() ||
            d.findElements(AUTH_FORM).isNotEmpty()
        }
        return this
    }

    fun fill(title: String, body: String): PostEditorPage {
        if (isPresent(TITLE_INPUT)) {
            type(TITLE_INPUT, title)
        }
        val bodyElement = visibleBodyInput()
        if (bodyElement != null) {
            bodyElement.scrollIntoView()
            bodyElement.click()
            bodyElement.sendKeys(body)
        }
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

    private fun visibleBodyInput(): WebElement? {
        val candidates = findAll(BODY_INPUT).filter { it.isDisplayed }
        return candidates.firstOrNull()
    }

    private fun WebElement.visibleText(): String =
        text.trim().ifBlank {
            getAttribute("textContent")?.trim().orEmpty()
        }

    private fun WebElement.scrollIntoView() {
        (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView({block:'center'});", this)
    }
}
