package ru.tpo.mirtesen.driver

import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import java.time.Duration

object DriverFactory {

    private val headless: Boolean =
        System.getProperty("headless", "false").toBoolean()

    fun create(browser: String): WebDriver = when (browser.lowercase()) {
        "chrome"  -> createChrome()
        "firefox" -> createFirefox()
        else      -> throw IllegalArgumentException("Unsupported browser: $browser")
    }

    private fun createChrome(): WebDriver {
        val opts = ChromeOptions()
        if (headless) opts.addArguments("--headless=new")
        opts.addArguments(
            "--window-size=1920,1080",
            "--lang=ru-RU,ru;q=0.9"
        )
        opts.setPageLoadStrategy(PageLoadStrategy.EAGER)
        val driver = ChromeDriver(opts)
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30))
        return driver
    }

    private fun createFirefox(): WebDriver {
        val opts = FirefoxOptions()
        if (headless) opts.addArguments("-headless")
        opts.addArguments("-width=1920", "-height=1080")
        opts.addPreference("intl.accept_languages", "ru-RU, ru, en-US, en")
        opts.setPageLoadStrategy(PageLoadStrategy.EAGER)
        val driver = FirefoxDriver(opts)
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30))
        return driver
    }
}
