package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.AuthPage
import ru.tpo.mirtesen.pages.PostEditorPage

/**
 * UC-4: Создание публикации.
 *
 * Проверяем сценарий написания поста: без входа нельзя, после входа редактор открывается,
 * текст вводится. Реальная публикация только если явно разрешено через переменную окружения.
 *
 */
class PostCreationTest : BaseTest() {

    // TC-15: без входа нажимаем «Написать»  сайт должен либо показать форму входа, либо не открыть редактор вообще.
    @ParameterizedTest(name = "UC-4 TC-15 Создание публикации требует входа [{0}]")
    @MethodSource("browsers")
    fun anonymousUserMustLoginBeforeCreatingPost(browser: String) {
        setup(browser)
        val editor = PostEditorPage(driver).openFromMainPage()
        assertTrue(editor.requiresAuth() || !editor.hasEditor(),
            "Создание публикации должно быть недоступно без авторизации")
    }

    // TC-16: входим, открываем редактор, вводим заголовок и текст и проверяем что редактор принял данные.
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

    // TC-17: заполняем и реально публикуем запись. работает только если задан MIRTESEN_ALLOW_PUBLISH=true,
    // чтобы случайно не засрать сайт при каждом прогоне тестов.
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
