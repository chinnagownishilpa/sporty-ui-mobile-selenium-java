package com.sporty.tests;

import com.sporty.framework.driver.DriverFactory;
import com.sporty.framework.pages.TwitchHomePage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TwitchMobileTest {
	WebDriver driver;
	TwitchHomePage home;

	@BeforeMethod
	public void setup() {
		driver = DriverFactory.initDriverMobile();
		home = new TwitchHomePage(driver);
	}

	@Test
	public void searchAndOpenStreamer_mobile() {
		System.out.println("STEP: openHome");
		home.openHome();

		System.out.println("STEP: closeModalIfPresent (1)");
		home.closeModalIfPresent();

		System.out.println("STEP: openSearchResultsPage");
		home.openSearchResultsPage("StarCraft II");

		System.out.println("STEP: scrollDownTimes");
		home.scrollDownTimes(2);

		System.out.println("STEP: selectAnyStreamer");
		home.selectAnyStreamer();

		System.out.println("STEP: waitForStreamerPageLoaded");
		home.waitForStreamerPageLoaded();

		System.out.println("STEP: closeModalIfPresent (2)");
		home.closeModalIfPresent();

		String path = "target/screenshots/streamer_page.png";
		System.out.println("STEP: takeScreenshot -> " + path);
		home.takeScreenshot(path);

		System.out.println("TEST FINISHED: screenshot saved -> " + path);
	}

	@AfterMethod
	public void tearDown() {
		DriverFactory.quitDriver();
	}
}
