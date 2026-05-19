package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage

/**
 * UC-5: Навигация по рубрикам через левое меню.
 *
 * Проверяем что левое меню на главной работает: блок отображается,
 * внутри есть «Моя лента» и рубрики, и по рубрике можно перейти на её страницу.
 *
 */
class RubricNavigationTest : BaseTest() {

    // TC-18: открываем главную и проверяем что левый блок с меню вообще есть.
    @ParameterizedTest(name = "UC-5 TC-18 Левый блок отображается [{0}]")
    @MethodSource("browsers")
    fun leftNavigationBlockIsVisible(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasLeftNavigationBlock(),
            "На главной странице должен отображаться левый навигационный блок")
    }

    // TC-19: смотрим что внутри левого блока  должны быть «Моя лента» и хотя бы одна рубрика.
    @ParameterizedTest(name = "UC-5 TC-19 Левый блок содержит пункты навигации [{0}]")
    @MethodSource("browsers")
    fun leftNavigationContainsFeedAndRubrics(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasMyFeedLink(), "Левый блок должен содержать пункт «Моя лента»")
        assertTrue(page.hasRubricLinks(), "Левый блок должен содержать ссылки на рубрики")
    }

    // TC-20: кликаем на первую рубрику в меню и проверяем что открылась страница с публикациями.
    @ParameterizedTest(name = "UC-5 TC-20 Рубрика открывается из левого меню [{0}]")
    @MethodSource("browsers")
    fun userCanOpenRubricFromLeftNavigation(browser: String) {
        setup(browser)
        val rubricPage = MainPage(driver).open().openFirstRubric()
        assertTrue(rubricPage.hasPostCards(),
            "После перехода в рубрику должны отображаться карточки публикаций")
    }
}
