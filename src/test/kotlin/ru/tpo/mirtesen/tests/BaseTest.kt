package ru.tpo.mirtesen.tests

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.provider.Arguments
import org.openqa.selenium.WebDriver
import ru.tpo.mirtesen.driver.DriverFactory
import java.util.stream.Stream

abstract class BaseTest {

    protected lateinit var driver: WebDriver

    companion object {
        @JvmStatic
        fun browsers(): Stream<Arguments> = Stream.of(
            Arguments.of("chrome"),
            Arguments.of("firefox")
        )
    }

    protected fun setup(browser: String) {
        driver = DriverFactory.create(browser)
    }

    @AfterEach
    fun tearDown() {
        if (::driver.isInitialized) {
            driver.quit()
        }
    }
}
