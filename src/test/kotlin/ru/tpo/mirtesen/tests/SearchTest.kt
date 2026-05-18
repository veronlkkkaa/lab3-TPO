package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage

/**
 * UC-1: Поиск публикаций на Миртесен.
 *
 * Актор: Пользователь.
 * Предусловие: сайт mirtesen.ru доступен.
 * Основной сценарий: пользователь вводит поисковый запрос →
 *   система показывает список подходящих публикаций →
 *   пользователь выбирает интересующую статью.
 *
 * Тест-кейсы:
 *   TC-01 — поиск по слову «путешествия» возвращает список публикаций
 *   TC-02 — URL страницы результатов содержит поисковый запрос
 *   TC-03 — клик на первую публикацию открывает страницу статьи с заголовком
 */
class SearchTest : BaseTest() {

    /**
     * TC-01: Поиск по слову «путешествия» возвращает список публикаций.
     * Предусловие: mirtesen.ru доступен.
     * Шаги: открыть mirtesen.ru → перейти на /search/posts/?q=путешествия.
     * Ожидаемый результат: страница содержит ссылки на статьи (a[href*='.mirtesen.ru/blog/']).
     */
    @ParameterizedTest(name = "TC-01 Поиск возвращает публикации [{0}]")
    @MethodSource("browsers")
    fun searchReturnsResults(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("путешествия")
        results.waitForResults()
        assertTrue(results.hasResults(),
            "Поиск по 'путешествия' должен вернуть список публикаций")
    }

    /**
     * TC-02: URL страницы результатов содержит поисковый запрос.
     * Предусловие: mirtesen.ru доступен.
     * Шаги: перейти на /search/posts/?q=природа → считать getCurrentUrl().
     * Ожидаемый результат: URL содержит «search» и «q=».
     */
    @ParameterizedTest(name = "TC-02 Запрос отражён в URL [{0}]")
    @MethodSource("browsers")
    fun searchQueryAppearsInUrl(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("природа")
        results.waitForResults()
        val url = results.getUrl()
        assertTrue(url.contains("search") && url.contains("q="),
            "URL должен содержать путь search и поисковый запрос. Текущий URL: $url")
    }

    /**
     * TC-03: Клик на первую публикацию открывает страницу статьи с заголовком.
     * Предусловие: результаты поиска «кулинария» содержат публикации.
     * Шаги: поиск «кулинария» → клик на первый результат.
     * Ожидаемый результат: страница статьи содержит непустой заголовок h1.
     */
    @ParameterizedTest(name = "TC-03 Страница статьи содержит заголовок [{0}]")
    @MethodSource("browsers")
    fun firstResultOpensArticle(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("кулинария")
        results.waitForResults()
        assertTrue(results.hasResults(), "Должны быть результаты поиска для перехода")
        val article = results.openFirstResult()
        assertTrue(article.hasTitle(),
            "Страница статьи должна содержать непустой заголовок h1")
    }
}
