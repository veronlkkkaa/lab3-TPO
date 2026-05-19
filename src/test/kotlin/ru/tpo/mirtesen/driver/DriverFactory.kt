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
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--window-size=1920,1080",
            "--disable-blink-features=AutomationControlled",
            "--lang=ru-RU,ru;q=0.9",
            "--disable-features=IsolateOrigins,site-per-process",
            "--disable-backgrounding-occluded-windows",
            "--disable-renderer-backgrounding",
            "--disable-background-timer-throttling",
            "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        )
        opts.setExperimentalOption("excludeSwitches", listOf("enable-automation"))
        opts.setExperimentalOption("useAutomationExtension", false)
        opts.setPageLoadStrategy(PageLoadStrategy.EAGER)
        val driver = ChromeDriver(opts)
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30))
        driver.executeCdpCommand(
            "Page.addScriptToEvaluateOnNewDocument",
            mapOf("source" to """
                Object.defineProperty(navigator, 'webdriver', {get: () => undefined});
                Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]});
                Object.defineProperty(navigator, 'languages', {get: () => ['ru-RU', 'ru', 'en-US', 'en']});
                window.chrome = { runtime: {}, loadTimes: function(){}, csi: function(){}, app: {} };
            """.trimIndent())
        )
        return driver
    }

    private fun createFirefox(): WebDriver {
        val opts = FirefoxOptions()
        if (headless) opts.addArguments("-headless")
        opts.addArguments("-width=1920", "-height=1080")
        opts.addPreference("network.proxy.type", 5)
        opts.addPreference("dom.webdriver.enabled", false)
        opts.addPreference("useAutomationExtension", false)
        opts.addPreference("intl.accept_languages", "ru-RU, ru, en-US, en")
        opts.setPageLoadStrategy(PageLoadStrategy.EAGER)
        val driver = FirefoxDriver(opts)
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30))
        return driver
    }
}
