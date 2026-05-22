package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage

class RubricNavigationTest : BaseTest() {

    @ParameterizedTest(name = "UC-5 TC-18 Левый блок отображается [{0}]")
    @MethodSource("browsers")
    fun leftNavigationBlockIsVisible(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasLeftNavigationBlock(),
            "На главной странице должен отображаться левый навигационный блок")
    }

    @ParameterizedTest(name = "UC-5 TC-19 Левый блок содержит пункты навигации [{0}]")
    @MethodSource("browsers")
    fun leftNavigationContainsFeedAndRubrics(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasMyFeedLink(), "Левый блок должен содержать пункт «Моя лента»")
        assertTrue(page.hasRubricLinks(), "Левый блок должен содержать ссылки на рубрики")
    }

    @ParameterizedTest(name = "UC-5 TC-20 Рубрика открывается из левого меню [{0}]")
    @MethodSource("browsers")
    fun userCanOpenRubricFromLeftNavigation(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assumeTrue(page.hasRubricLinks(), "Рубрики не найдены в левом меню — нечего открывать")
        val rubricPage = page.openFirstRubric()
        assertTrue(rubricPage.hasPostCards(),
            "После перехода в рубрику должны отображаться карточки публикаций")
    }
}
