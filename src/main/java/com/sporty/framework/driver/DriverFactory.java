package com.sporty.framework.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public static WebDriver initDriverMobile() {
    	
        if (tlDriver.get() == null) {
            // Setup chromedriver binary
            WebDriverManager.chromedriver().setup();


            // Mobile emulation configuration (iPhone-like)
            Map<String, Object> deviceMetrics = new HashMap<>();
            deviceMetrics.put("width", 390);
            deviceMetrics.put("height", 844);
            deviceMetrics.put("pixelRatio", 3.0);

            Map<String, Object> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceMetrics", deviceMetrics);
            mobileEmulation.put("userAgent",
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) " +
                            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1");

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);

            // Common options
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--remote-allow-origins=*");  
            options.addArguments("--disable-gpu");
            // fix for recent Chrome + Selenium
            options.addArguments("--window-size=390,844");     // match mobile viewport
            // do NOT add headless when you want to record GIF locally
            
            
            // create driver
            tlDriver.set(new ChromeDriver(options));

            // timeouts
            tlDriver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(8));
            tlDriver.get().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            tlDriver.get().manage().timeouts().scriptTimeout(Duration.ofSeconds(60));


            // small JS to ensure viewport meta is applied (best-effort)
            try {
                ((JavascriptExecutor) tlDriver.get()).executeScript(
                        "document.querySelector('meta[name=viewport]')?.setAttribute('content','width=device-width, initial-scale=1');");
            } catch (Exception ignored) {}
        }
        return tlDriver.get();
    }

    public static WebDriver getDriver() {
        return tlDriver.get();
    }

    public static void quitDriver() {
        if (tlDriver.get() != null) {
            try { tlDriver.get().quit(); } catch (Exception ignored) {}
            tlDriver.remove();
        }
    }
}
