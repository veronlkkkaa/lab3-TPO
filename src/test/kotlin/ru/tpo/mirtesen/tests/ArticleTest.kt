package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.tpo.mirtesen.pages.ArticlePage
import ru.tpo.mirtesen.pages.MainPage

/**
 * UC-3: Просмотр страницы публикации на Миртесен.
 *
 * Актор: Пользователь.
 * Предусловие: результаты поиска содержат публикации.
 * Основной сценарий: пользователь переходит на страницу статьи →
 *   видит заголовок, текст статьи и информацию об авторе.
 *
 * Тест-кейсы:
 *   TC-06 — страница статьи содержит заголовок и текст
 *   TC-07 — страница статьи содержит автора или дату публикации
 */
class ArticleTest : BaseTest() {

    private fun openFirstArticleFor(query: String): ArticlePage {
        val results = MainPage(driver).search(query)
        results.waitForResults()
        assertTrue(results.hasResults(),
            "Для перехода к статье необходимы результаты поиска по '$query'")
        return results.openFirstResult()
    }

    /**
     * TC-06: Страница статьи содержит заголовок и основной текст.
     * Предусловие: результаты поиска «история» содержат публикации.
     * Шаги: поиск «история» → клик на первый результат.
     * Ожидаемый результат: h1 непустой, присутствует div.blog_content.
     */
    @ParameterizedTest(name = "TC-06 Статья: заголовок и текст [{0}]")
    @MethodSource("browsers")
    fun articleHasTitleAndContent(browser: String) {
        setup(browser)
        val article = openFirstArticleFor("история")
        assertTrue(article.hasTitle(),   "Страница статьи должна содержать заголовок h1")
        assertTrue(article.hasContent(), "Страница статьи должна содержать текст (div.blog_content)")
    }

    /**
     * TC-07: Страница статьи содержит информацию об авторе или дату публикации.
     * Предусловие: результаты поиска «спорт» содержат публикации.
     * Шаги: поиск «спорт» → клик на первый результат.
     * Ожидаемый результат: присутствует a[href*='mirtesen.ru/people/'] или <time>.
     */
    @ParameterizedTest(name = "TC-07 Статья: автор или дата [{0}]")
    @MethodSource("browsers")
    fun articleHasAuthorOrDate(browser: String) {
        setup(browser)
        val article = openFirstArticleFor("спорт")
        assertTrue(article.hasTitle(), "Страница статьи должна содержать заголовок h1")
        assertTrue(article.hasAuthor() || article.hasDate(),
            "Страница статьи должна содержать автора или дату публикации")
    }
}
