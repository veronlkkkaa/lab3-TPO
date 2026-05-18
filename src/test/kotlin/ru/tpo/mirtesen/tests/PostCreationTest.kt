package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.AuthPage
import ru.tpo.mirtesen.pages.PostEditorPage

/**
 * UC-4: Создание публикации на Миртесен.
 *
 * Актор: зарегистрированный пользователь.
 * Предусловие: пользователь авторизован.
 * Основной сценарий: пользователь входит в систему ->
 *   открывает редактор публикации -> заполняет заголовок и текст ->
 *   сохраняет или публикует запись.
 *
 * Тест-кейсы:
 *   TC-15 — неавторизованный пользователь не может открыть создание публикации без входа
 *   TC-16 — авторизованный пользователь может открыть редактор и заполнить текст публикации
 *   TC-17 — публикация тестовой записи выполняется только при явном разрешении
 */
class PostCreationTest : BaseTest() {

    /**
     * TC-15: Неавторизованный пользователь не может создать публикацию без входа.
     * Предусловие: пользователь не авторизован.
     * Шаги: открыть главную -> нажать кнопку создания публикации.
     * Ожидаемый результат: система показывает форму авторизации или не открывает редактор.
     */
    @ParameterizedTest(name = "UC-4 TC-15 Создание публикации требует входа [{0}]")
    @MethodSource("browsers")
    fun anonymousUserMustLoginBeforeCreatingPost(browser: String) {
        setup(browser)
        val editor = PostEditorPage(driver).openFromMainPage()
        assertTrue(editor.requiresAuth() || !editor.hasEditor(),
            "Создание публикации должно быть недоступно без авторизации")
    }

    /**
     * TC-16: Авторизованный пользователь может открыть редактор и заполнить текст публикации.
     * Предусловие: заданы MIRTESEN_LOGIN и MIRTESEN_PASSWORD.
     * Шаги: войти в систему -> открыть редактор публикации -> заполнить заголовок и текст.
     * Ожидаемый результат: редактор доступен, текст введен, доступно действие сохранения/публикации.
     */
    @ParameterizedTest(name = "UC-4 TC-16 Авторизованный пользователь заполняет публикацию [{0}]")
    @MethodSource("browsers")
    fun authorizedUserCanFillPostEditor(browser: String) {
        val credentials = TestCredentials.fromEnvironment()
        assumeTrue(credentials != null, "Нужны MIRTESEN_LOGIN и MIRTESEN_PASSWORD")

        setup(browser)
        AuthPage(driver).open().loginByEmail(credentials!!.login, credentials.password)

        val title = "Тестовая публикация TPO $browser ${System.currentTimeMillis()}"
        val body = "Автоматический тест Selenium WebDriver для лабораторной работы TPO."
        val editor = PostEditorPage(driver).openFromMainPage().fill(title, body)

        assertTrue(editor.hasEditor(), "После авторизации должен открыться редактор публикации")
        assertTrue(editor.bodyText().contains("Автоматический тест") || editor.hasPublishAction(),
            "Редактор должен принять текст публикации или показать действие публикации")
    }

    /**
     * TC-17: Публикация тестовой записи выполняется только при явном разрешении.
     * Предусловие: заданы MIRTESEN_LOGIN, MIRTESEN_PASSWORD и MIRTESEN_ALLOW_PUBLISH=true.
     * Шаги: войти в систему -> открыть редактор -> заполнить публикацию -> нажать «Опубликовать».
     * Ожидаемый результат: команда публикации выполнена без ошибки Selenium.
     */
    @ParameterizedTest(name = "UC-4 TC-17 Публикация тестовой записи [{0}]")
    @MethodSource("browsers")
    fun authorizedUserCanPublishPostWhenExplicitlyAllowed(browser: String) {
        val credentials = TestCredentials.fromEnvironment()
        val publishAllowed = System.getenv("MIRTESEN_ALLOW_PUBLISH").equals("true", ignoreCase = true)
        assumeTrue(credentials != null, "Нужны MIRTESEN_LOGIN и MIRTESEN_PASSWORD")
        assumeTrue(publishAllowed, "Для реальной публикации нужен MIRTESEN_ALLOW_PUBLISH=true")

        setup(browser)
        AuthPage(driver).open().loginByEmail(credentials!!.login, credentials.password)

        val title = "Тестовая публикация TPO $browser ${System.currentTimeMillis()}"
        val body = "Запись создана автоматическим Selenium-тестом для проверки сценария публикации."
        val editor = PostEditorPage(driver).openFromMainPage().fill(title, body)

        assertTrue(editor.hasPublishAction(), "В редакторе должна быть кнопка сохранения или публикации")
        editor.publish()
    }
}
