package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage

class FeedTest : BaseTest() {

    @ParameterizedTest(name = "UC-1 TC-01 Лента содержит публикации")
    @MethodSource("browsers")
    fun feedHasPostCards(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasPostCards(),
            "Главная страница должна содержать карточки публикаций")
    }

    @ParameterizedTest(name = "UC-1 TC-02 Карточки содержат заголовки")
    @MethodSource("browsers")
    fun feedCardsHaveTitles(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasPostTitles(),
            "Карточки публикаций должны содержать непустые заголовки")
    }

    @ParameterizedTest(name = "UC-1 TC-03 Карточки содержат ссылки")
    @MethodSource("browsers")
    fun feedCardsHavePostLinks(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.postCardsHaveLinks(),
            "Карточки публикаций должны содержать ссылки на публикации")
    }

    @ParameterizedTest(name = "UC-1 TC-04 Первая публикация открывается")
    @MethodSource("browsers")
    fun userCanOpenFirstFeedPost(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assumeTrue(page.hasPostCards(), "Лента пустая — нечего открывать")
        val article = page.openFirstPost()
        assertTrue(article.hasTitle(),
            "После открытия публикации должна отображаться статья с заголовком")
    }
}
