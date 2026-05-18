package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage

/**
 * UC-2: Просмотр ленты публикаций на главной странице Миртесен.
 *
 * Актор: Пользователь.
 * Предусловие: сайт mirtesen.ru доступен.
 * Основной сценарий: пользователь открывает главную страницу →
 *   система отображает ленту публикаций с заголовками и превью.
 *
 * Тест-кейсы:
 *   TC-04 — главная страница содержит карточки публикаций
 *   TC-05 — карточки публикаций содержат непустые заголовки
 */
class FeedTest : BaseTest() {

    /**
     * TC-04: Главная страница содержит карточки публикаций.
     * Предусловие: mirtesen.ru доступен.
     * Шаги: открыть https://mirtesen.ru/ → дождаться загрузки.
     * Ожидаемый результат: присутствуют ссылки вида a[href*='.mirtesen.ru/blog/'].
     */
    @ParameterizedTest(name = "TC-04 Лента содержит публикации [{0}]")
    @MethodSource("browsers")
    fun feedHasPostCards(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasPostCards(),
            "Главная страница должна содержать карточки публикаций")
    }

    /**
     * TC-05: Карточки публикаций на главной содержат непустые заголовки.
     * Предусловие: главная страница загружена.
     * Шаги: открыть главную → считать текст h4 внутри карточек.
     * Ожидаемый результат: хотя бы один заголовок непустой.
     */
    @ParameterizedTest(name = "TC-05 Карточки содержат заголовки [{0}]")
    @MethodSource("browsers")
    fun feedCardsHaveTitles(browser: String) {
        setup(browser)
        val page = MainPage(driver).open()
        assertTrue(page.hasPostTitles(),
            "Карточки публикаций должны содержать непустые заголовки")
    }
}
