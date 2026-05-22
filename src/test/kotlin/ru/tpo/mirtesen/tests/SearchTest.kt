package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.MainPage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class SearchTest : BaseTest() {

    companion object {
        @JvmStatic
        fun browsersAndBadSearchQueries() = listOf("chrome", "firefox").flatMap { browser ->
            badSearchQueries().map { args -> Arguments.of(browser, args.get()[0] as String) }
        }

        @JvmStatic
        fun browsersAndBadPeopleSearchQueries() = listOf("chrome", "firefox").flatMap { browser ->
            badPeopleSearchQueries().map { args -> Arguments.of(browser, args.get()[0] as String) }
        }

        @JvmStatic
        fun badSearchQueries() = listOf(
            Arguments.of("   "),
            Arguments.of("!@#\$%^&*()[]{}|"),
            Arguments.of("<script>alert(document.title)</script>"),
            Arguments.of("'; DROP TABLE posts; --"),
            Arguments.of("a".repeat(500)),
            Arguments.of("​‌‍"),


            Arguments.of("А".repeat(500)),
            Arguments.of("null"),
            Arguments.of("undefined"),
        )

        @JvmStatic
        fun badPeopleSearchQueries() = listOf(
            Arguments.of(""),
            Arguments.of("   "),
            Arguments.of("!@#\$%^&*()[]{}|"),
            Arguments.of("<script>alert(document.title)</script>"),
            Arguments.of("'; DROP TABLE users; --"),
            Arguments.of("a".repeat(500)),
            Arguments.of("​‌‍"),


            Arguments.of("А".repeat(500)),
            Arguments.of("null"),
        )
    }

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
        assumeTrue(results.hasResults(), "Поиск не вернул результатов — нечего открывать")
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

    @ParameterizedTest(name = "UC-2 TC-11 Нестандартный ввод не вызывает ошибку сервера: [{1}] [{0}]")
    @MethodSource("browsersAndBadSearchQueries")
    fun malformedSearchQueryHandledWithoutServerError(browser: String, query: String) {
        setup(browser)
        val results = MainPage(driver).search(query)
        results.waitForSearchPage()

        assertFalse(results.hasServerError(),
            "Запрос «${query.take(40)}» не должен приводить к ошибке сервера")
        assertTrue(results.getUrl().contains("search"),
            "После нестандартного запроса страница должна остаться в разделе поиска. URL: ${results.getUrl()}")
    }

    @ParameterizedTest(name = "UC-2 TC-13 Вкладка «Публикации» показывает посты, а не профили [{0}]")
    @MethodSource("browsers")
    fun postsTabShowsBlogPostsNotUserProfiles(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("путешествия")
        results.waitForResults()

        if (results.hasResults()) {
            assertFalse(results.hasProfileLinksInSearchArea(),
                "Вкладка «Публикации» не должна содержать ссылки на профили пользователей — только на записи блога")
        }
    }

    @ParameterizedTest(name = "UC-2 TC-14 Вкладка «Люди» не показывает записи блога [{0}]")
    @MethodSource("browsers")
    fun peopleTabDoesNotContainBlogPostLinks(browser: String) {
        setup(browser)
        val results = MainPage(driver).searchPeople("путешествия")
        results.waitForSearchPage()

        assertFalse(results.hasBlogPostLinksInSearchArea(),
            "Вкладка «Люди» не должна содержать ссылки на записи блога (.mirtesen.ru/blog/)")
    }

    @ParameterizedTest(name = "UC-2 TC-15 Страница поиска без результатов не падает [{0}]")
    @MethodSource("browsers")
    fun searchPageWithNoResultsDoesNotCrash(browser: String) {
        setup(browser)
        val results = MainPage(driver).search("aаёйzz_no_match_unique_tpo_2026_xyz")
        results.waitForSearchPage()

        assertFalse(results.hasServerError(),
            "Запрос без результатов не должен приводить к ошибке сервера")
        assertTrue(results.hasNoResultsMessage() || results.getResultCount() == 0,
            "Поиск без совпадений должен показать сообщение об отсутствии результатов или пустой список")
        assertTrue(results.getUrl().contains("search"),
            "Должны остаться на странице поиска, а не редиректиться. URL: ${results.getUrl()}")
    }

    @ParameterizedTest(name = "UC-2 TC-16 Нестандартный ввод в поиске людей не вызывает ошибку сервера: [{1}] [{0}]")
    @MethodSource("browsersAndBadPeopleSearchQueries")
    fun malformedPeopleSearchQueryHandledWithoutServerError(browser: String, query: String) {
        setup(browser)
        val results = MainPage(driver).searchPeople(query)
        results.waitForSearchPage()

        assertFalse(results.hasServerError(),
            "Запрос «${query.take(40)}» в поиске людей не должен приводить к ошибке сервера")
        assertTrue(results.getUrl().contains("search"),
            "После нестандартного запроса в поиске людей страница должна остаться в разделе поиска. URL: ${results.getUrl()}")
    }

    @ParameterizedTest(name = "UC-2 TC-17 Пустой запрос в поиске людей без ошибки сервера [{0}]")
    @MethodSource("browsers")
    fun emptyPeopleSearchQueryHandledWithoutServerError(browser: String) {
        setup(browser)
        val results = MainPage(driver).searchPeople("")
        results.waitForSearchPage()

        assertFalse(results.hasServerError(),
            "Пустой запрос в поиске людей не должен приводить к ошибке сервера")
        assertTrue(results.getUrl().contains("search"),
            "URL пустого поиска людей должен оставаться страницей поиска. URL: ${results.getUrl()}")
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
