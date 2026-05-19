package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class SearchTest : BaseTest() {

    @ParameterizedTest(name = "UC-2 TC-05 Поиск возвращает публикации [{0}]")
    @MethodSource("browsers")
    fun searchReturnsResults(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("путешествия")
        results.waitForResults()
        assertTrue(results.hasResults(),
            "Поиск по 'путешествия' должен вернуть список публикаций")
    }

    @ParameterizedTest(name = "UC-2 TC-06 Запрос отражён в URL [{0}]")
    @MethodSource("browsers")
    fun searchQueryAppearsInUrl(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("природа")
        results.waitForResults()
        val url = results.getUrl()
        assertTrue(url.contains("search") && queryValue(url).isNotBlank(),
            "URL должен содержать путь search и непустой параметр q. Текущий URL: $url")
    }

    @ParameterizedTest(name = "UC-2 TC-07 Страница статьи содержит заголовок [{0}]")
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

    @ParameterizedTest(name = "UC-2 TC-08 Пустой запрос без ошибки сервера [{0}]")
    @MethodSource("browsers")
    fun emptySearchQueryHandledWithoutServerError(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("")
        results.waitForSearchPage()

        assertFalse(results.hasServerError(),
            "Пустой поисковый запрос не должен приводить к ошибке сервера")
        assertTrue(results.getUrl().contains("search") && results.getUrl().contains("q="),
            "URL пустого поиска должен оставаться страницей поиска. Текущий URL: ${results.getUrl()}")
    }

    @ParameterizedTest(name = "UC-2 TC-09 Запрос без результатов обработан [{0}]")
    @MethodSource("browsers")
    fun noResultsSearchHandledCorrectly(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("zzzxxyy_no_results_tpo_2026")
        results.waitForSearchPage()

        assertFalse(results.hasServerError(),
            "Запрос без результатов не должен приводить к ошибке сервера")
        assertTrue(results.hasNoResultsMessage() || results.getResultCount() == 0,
            "Для запроса без совпадений должно быть сообщение или пустой список результатов")
    }

    @ParameterizedTest(name = "UC-2 TC-10 Результаты имеют заголовки [{0}]")
    @MethodSource("browsers")
    fun searchResultsHaveNonEmptyTitles(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("путешествия")
        results.waitForResults()

        assertTrue(results.resultsHaveNonEmptyTitles(),
            "Найденные публикации должны иметь непустые заголовки")
    }

    private fun queryValue(url: String): String {
        val queryPart = url.substringAfter("?", "")
        val rawValue = queryPart
            .split("&")
            .firstOrNull { it.startsWith("q=") }
            ?.substringAfter("q=")
            ?: ""

        return URLDecoder.decode(rawValue, StandardCharsets.UTF_8)
    }
}
