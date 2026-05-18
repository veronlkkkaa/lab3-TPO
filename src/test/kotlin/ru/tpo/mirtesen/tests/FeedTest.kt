package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage

/**
 * UC-1: Просмотр ленты публикаций на главной странице Миртесен.
 *
 * Актор: Пользователь.
 * Предусловие: сайт mirtesen.ru доступен.
 * Основной сценарий: пользователь открывает главную страницу →
 *   система отображает ленту публикаций с заголовками и превью →
 *   пользователь выбирает публикацию из ленты.
 *
 * Тест-кейсы:
 *   TC-01 — главная страница содержит карточки публикаций
 *   TC-02 — карточки публикаций содержат непустые заголовки
 *   TC-03 — карточки публикаций содержат ссылки на публикации
 *   TC-04 — пользователь может открыть первую публикацию из ленты
 */
class FeedTest : BaseTest() {

    /**
     * TC-01: Главная страница содержит карточки публикаций.
     * Предусловие: mirtesen.ru доступен.
     * Шаги: открыть https://mirtesen.ru/ → дождаться загрузки.
     * Ожидаемый результат: присутствуют ссылки вида a[href*='.mirtesen.ru/blog/'].
     */
    @ParameterizedTest(name = "UC-1 TC-01 Лента содержит публикации [{0}]")
    @MethodSource("browsers")
    fun feedHasPostCards(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasPostCards(),
            "Главная страница должна содержать карточки публикаций")
    }

    /**
     * TC-02: Карточки публикаций на главной содержат непустые заголовки.
     * Предусловие: главная страница загружена.
     * Шаги: открыть главную → считать текст h4 внутри карточек.
     * Ожидаемый результат: хотя бы один заголовок непустой.
     */
    @ParameterizedTest(name = "UC-1 TC-02 Карточки содержат заголовки [{0}]")
    @MethodSource("browsers")
    fun feedCardsHaveTitles(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasPostTitles(),
            "Карточки публикаций должны содержать непустые заголовки")
    }

    /**
     * TC-03: Карточки публикаций содержат ссылки на публикации.
     * Предусловие: главная страница загружена.
     * Шаги: открыть главную → найти ссылки карточек публикаций.
     * Ожидаемый результат: хотя бы одна карточка содержит href вида .mirtesen.ru/blog/.
     */
    @ParameterizedTest(name = "UC-1 TC-03 Карточки содержат ссылки [{0}]")
    @MethodSource("browsers")
    fun feedCardsHavePostLinks(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.postCardsHaveLinks(),
            "Карточки публикаций должны содержать ссылки на публикации")
    }

    /**
     * TC-04: Пользователь может открыть первую публикацию из ленты.
     * Предусловие: главная страница содержит публикации.
     * Шаги: открыть главную → перейти по первой ссылке публикации.
     * Ожидаемый результат: открывается страница статьи с непустым заголовком.
     */
    @ParameterizedTest(name = "UC-1 TC-04 Первая публикация открывается [{0}]")
    @MethodSource("browsers")
    fun userCanOpenFirstFeedPost(browser: String) {
        setup(browser)
        val article = MainPage(driver).open().openFirstPost()
        assertTrue(article.hasTitle(),
            "После открытия публикации должна отображаться статья с заголовком")
    }
}
