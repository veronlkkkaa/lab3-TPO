package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage

/**
 * UC-1: Лента публикаций на главной странице.
 *
 * Проверяем что главная вообще показывает публикации: карточки есть,
 * у них есть заголовки и ссылки, и по ссылке можно перейти на статью.
 *
 */
class FeedTest : BaseTest() {

    // TC-01: открываем главную и проверяем что лента не пустая.
    @ParameterizedTest(name = "UC-1 TC-01 Лента содержит публикации [{0}]")
    @MethodSource("browsers")
    fun feedHasPostCards(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasPostCards(),
            "Главная страница должна содержать карточки публикаций")
    }

    // TC-02: проверяем что у карточек есть хоть какой-то текст заголовка, не пустые.
    @ParameterizedTest(name = "UC-1 TC-02 Карточки содержат заголовки [{0}]")
    @MethodSource("browsers")
    fun feedCardsHaveTitles(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasPostTitles(),
            "Карточки публикаций должны содержать непустые заголовки")
    }

    // TC-03: проверяем что карточки кликабельны и у них есть ссылки на статьи.
    @ParameterizedTest(name = "UC-1 TC-03 Карточки содержат ссылки [{0}]")
    @MethodSource("browsers")
    fun feedCardsHavePostLinks(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.postCardsHaveLinks(),
            "Карточки публикаций должны содержать ссылки на публикации")
    }

    // TC-04: кликаем на первую карточку и смотрим что статья открылась с заголовком.
    @ParameterizedTest(name = "UC-1 TC-04 Первая публикация открывается [{0}]")
    @MethodSource("browsers")
    fun userCanOpenFirstFeedPost(browser: String) {
        setup(browser)
        val article = MainPage(driver).open().openFirstPost()
        assertTrue(article.hasTitle(),
            "После открытия публикации должна отображаться статья с заголовком")
    }
}
