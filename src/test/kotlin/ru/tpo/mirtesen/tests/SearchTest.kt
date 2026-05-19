package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * UC-2: Поиск публикаций.
 *
 * Проверяем что поиск работает: находит статьи по слову, отражает запрос в URL,
 * позволяет открыть результат. Отдельно проверяем граничные случаи —
 * пустой запрос и запрос без результатов не должны ронять сервер.
 *
 */
class SearchTest : BaseTest() {

    // TC-05: ищем «путешествия»  должны появиться результаты, страница не пустая.
    @ParameterizedTest(name = "UC-2 TC-05 Поиск возвращает публикации [{0}]")
    @MethodSource("browsers")
    fun searchReturnsResults(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("путешествия")
        results.waitForResults()
        assertTrue(results.hasResults(),
            "Поиск по 'путешествия' должен вернуть список публикаций")
    }

    // TC-06: ищем «природа» и смотрим на URL  запрос должен быть виден в адресной строке.
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

    // TC-07: ищем «кулинария», кликаем на первый результат  должна открыться статья с заголовком.
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

    // TC-08: отправляем пустой запрос  сервер не должен упасть с ошибкой 500.
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

    // TC-09: ищем заведомо несуществующую строку —сервер не должен падать, должен показать пустой список или «ничего не найдено».
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

    // TC-10: ищем «путешествия» и проверяем что у найденных карточек есть хоть какой-то заголовок.
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
