package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class PostEditorPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val OPEN_EDITOR = By.xpath(
            "//button[contains(concat(' ', normalize-space(@class), ' '), ' left-menu__create-post-btn ')]"
        )

        private val BODY_INPUT = By.xpath(
            "//*[contains(concat(' ', normalize-space(@class), ' '), ' plain-post-editor__body ')]" +
                    "//*[contains(concat(' ', normalize-space(@class), ' '), ' mt-lexical-input ')" +
                    " and @contenteditable='true'" +
                    " and @role='textbox']"
        )

        private val PUBLISH_BUTTON = By.xpath(
            "//*[contains(concat(' ', normalize-space(@class), ' '), ' plain-post-editor__footer ')]" +
                    "//button[contains(concat(' ', normalize-space(@class), ' '), ' save-btn ')" +
                    " and contains(concat(' ', normalize-space(@class), ' '), ' btn-primary ')]"
        )

        private val AUTH_FORM = By.xpath(
            "//*[contains(concat(' ', normalize-space(@class), ' '), ' auth-form__form ')]"
        )
    }

    fun openFromMainPage(): PostEditorPage {
        openUrl(MainPage.URL)

        if (!isPresent(OPEN_EDITOR)) {
            return this
        }

        jsClick(OPEN_EDITOR)

        waitUntil { d ->
            d.findElements(BODY_INPUT).isNotEmpty() ||
                    d.findElements(AUTH_FORM).isNotEmpty()
        }

        return this
    }

    fun fill(body: String): PostEditorPage {
        val bodyElement = waitVisible(BODY_INPUT)
        bodyElement.scrollIntoView()
        bodyElement.click()
        bodyElement.sendKeys(body)
        return this
    }

    fun fill(title: String, body: String): PostEditorPage {
        val bodyElement = waitVisible(BODY_INPUT)
        bodyElement.scrollIntoView()
        bodyElement.click()
        bodyElement.sendKeys("$title\n$body")
        return this
    }

    fun publish(): PostEditorPage {
        waitUntil { d ->
            val button = d.findElements(PUBLISH_BUTTON).firstOrNull()
            button != null && button.isDisplayed && button.isEnabled
        }

        jsClick(PUBLISH_BUTTON)
        return this
    }

    fun hasEditor(): Boolean =
        findAll(BODY_INPUT).any { it.isDisplayed }

    fun hasPublishAction(): Boolean =
        findAll(PUBLISH_BUTTON).any { it.isDisplayed && it.isEnabled }

    fun requiresAuth(): Boolean =
        isPresent(AUTH_FORM)

    fun bodyText(): String =
        findAll(BODY_INPUT)
            .firstOrNull { it.isDisplayed }
            ?.visibleText()
            .orEmpty()

    private fun WebElement.visibleText(): String =
        text.trim().ifBlank {
            getAttribute("textContent")?.trim().orEmpty()
        }

    private fun WebElement.scrollIntoView() {
        (driver as JavascriptExecutor).executeScript(
            "arguments[0].scrollIntoView({block:'center'});",
            this
        )
    }
}