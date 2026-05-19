package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions

class PostEditorPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val OPEN_EDITOR = By.xpath(
            "//button[contains(@class,'left-menu__create-post-btn') and contains(normalize-space(),'Создать пост')]"
        )

        private val TITLE_INPUT = By.xpath(
            "//*[self::input or self::textarea][@name='title' or contains(@placeholder,'Заголов')]"
        )

        private val BODY_INPUT = By.xpath(
            "//*[@contenteditable='true']"
        )

        private val PUBLISH_BUTTON = By.xpath(
            "//*[self::button or self::a][contains(normalize-space(),'Опубликовать') or contains(normalize-space(),'Сохранить')]"
        )

        private val AUTH_FORM = By.xpath("//*[contains(normalize-space(),'Вход по почте')]")
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
        try {
            return waitVisible(BODY_INPUT)
        } catch (e: TimeoutException) {
            val candidates = findAll(BODY_INPUT).filter { it.isDisplayed }
            if (candidates.isNotEmpty()) {
                return candidates.first()
            }
            return null
        }
    }

    private fun WebElement.visibleText(): String =
        text.trim().ifBlank {
            getAttribute("textContent")?.trim().orEmpty()
        }

    private fun WebElement.scrollIntoView() {
        (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView({block:'center'});", this)
    }
}
