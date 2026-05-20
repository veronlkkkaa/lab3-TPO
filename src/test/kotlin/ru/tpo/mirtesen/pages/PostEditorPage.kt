package ru.tpo.mirtesen.pages

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class PostEditorPage(driver: WebDriver) : BasePage(driver) {

    companion object {
        private val OPEN_EDITOR = By.xpath(
            "//*[self::button or self::a][" +
            "contains(@class,'left-menu__create-post-btn') " +
            "or contains(@class,'create-post') " +
            "or contains(@class,'editor') " +
            "or contains(@href,'/editor') " +
            "or contains(@href,'/post/add') " +
            "or contains(@href,'/post/create')]"
        )

        private val TITLE_INPUT = By.xpath(
            "//input[@name='title' or contains(@class,'title') or contains(@data-test,'title') or contains(@data-testid,'title')]" +
            " | //textarea[@name='title' or contains(@class,'title') or contains(@data-test,'title') or contains(@data-testid,'title')]"
        )

        private val BODY_INPUT = By.xpath(
            "//*[@contenteditable='true']" +
            " | //textarea[@name='text' or @name='body' or @name='content' " +
            "or contains(@class,'text') or contains(@class,'body') or contains(@class,'content') " +
            "or contains(@data-test,'text') or contains(@data-test,'body') or contains(@data-test,'content') " +
            "or contains(@data-testid,'text') or contains(@data-testid,'body') or contains(@data-testid,'content')]"
        )

        private val PUBLISH_BUTTON = By.xpath(
            "//*[self::button or self::a][not(@disabled) and (" +
            "contains(@class,'publish') " +
            "or contains(@class,'save') " +
            "or contains(@data-test,'publish') " +
            "or contains(@data-test,'save') " +
            "or contains(@data-testid,'publish') " +
            "or contains(@data-testid,'save') " +
            "or @type='submit')]"
        )

        private val AUTH_FORM = By.xpath(
            "//form[.//input[@name='email'] or .//input[@type='password']]" +
            " | //*[contains(@class,'auth-form')]"
        )
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
