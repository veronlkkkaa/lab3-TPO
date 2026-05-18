package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage

/**
 * UC-5: Навигация по рубрикам в левом меню Миртесен.
 *
 * Актор: Пользователь.
 * Предусловие: сайт mirtesen.ru доступен.
 * Основной сценарий: пользователь открывает главную страницу ->
 *   видит левый навигационный блок с пунктом «Моя лента» и рубриками ->
 *   выбирает интересующую рубрику -> система показывает публикации выбранной рубрики.
 *
 * Тест-кейсы:
 *   TC-18 — на главной странице отображается левый навигационный блок
 *   TC-19 — левый блок содержит ссылку «Моя лента» и ссылки на рубрики
 *   TC-20 — пользователь может открыть рубрику из левого меню
 */
class RubricNavigationTest : BaseTest() {

    /**
     * TC-18: На главной странице отображается левый навигационный блок.
     * Предусловие: mirtesen.ru доступен.
     * Шаги: открыть главную страницу.
     * Ожидаемый результат: левый блок навигации присутствует на странице.
     */
    @ParameterizedTest(name = "UC-5 TC-18 Левый блок отображается [{0}]")
    @MethodSource("browsers")
    fun leftNavigationBlockIsVisible(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasLeftNavigationBlock(),
            "На главной странице должен отображаться левый навигационный блок")
    }

    /**
     * TC-19: Левый блок содержит ссылку «Моя лента» и ссылки на рубрики.
     * Предусловие: главная страница загружена.
     * Шаги: открыть главную страницу -> найти пункты левого меню.
     * Ожидаемый результат: в левом блоке есть «Моя лента» и хотя бы одна рубрика.
     */
    @ParameterizedTest(name = "UC-5 TC-19 Левый блок содержит пункты навигации [{0}]")
    @MethodSource("browsers")
    fun leftNavigationContainsFeedAndRubrics(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasMyFeedLink(), "Левый блок должен содержать пункт «Моя лента»")
        assertTrue(page.hasRubricLinks(), "Левый блок должен содержать ссылки на рубрики")
    }

    /**
     * TC-20: Пользователь может открыть рубрику из левого меню.
     * Предусловие: главная страница содержит левое меню с рубриками.
     * Шаги: открыть главную страницу -> перейти по первой ссылке рубрики.
     * Ожидаемый результат: открывается страница рубрики с карточками публикаций.
     */
    @ParameterizedTest(name = "UC-5 TC-20 Рубрика открывается из левого меню [{0}]")
    @MethodSource("browsers")
    fun userCanOpenRubricFromLeftNavigation(browser: String) {
        setup(browser)
        val rubricPage = MainPage(driver).open().openFirstRubric()
        assertTrue(rubricPage.hasPostCards(),
            "После перехода в рубрику должны отображаться карточки публикаций")
    }
}
