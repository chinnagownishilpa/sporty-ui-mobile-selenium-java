package com.sporty.framework.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class TwitchHomePage {
	private final WebDriver driver;
	private final WebDriverWait wait;

	public TwitchHomePage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	}

	public void openHome() {
		driver.get("https://m.twitch.tv");
	}

	
	public void openSearchResultsPage(String query) {
	    String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
	    String url = "https://m.twitch.tv/search?term=" + q;

	    try {
	        // use navigate().to which tends to play nicer with eager strategy
	        driver.navigate().to(url);
	    } catch (Exception e) {
	        // fallback: use JS navigation if driver.get / navigate hangs
	        try {
	            ((JavascriptExecutor) driver).executeScript("window.location.href = arguments[0];", url);
	        } catch (Exception ignored) {}
	    }

	    // Wait for a reliable results element (longer wait)
	    WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
	    By resultsSelector = By.cssSelector(
	        "a[href*='/channel/'], a[href*='/video/'], div[data-a-target='search-result'], div[data-a-target='card']");
	    try {
	        longWait.until(ExpectedConditions.presenceOfElementLocated(resultsSelector));
	        // little pause to allow lazy images/layout
	        Thread.sleep(800);
	    } catch (Exception e) {
	        // don't fail immediately — we'll try selecting with retries later
	        System.out.println("Warning: search results not immediately present: " + e.getMessage());
	    }
	}


	public void closeModalIfPresent() {
		try {
			// common close buttons by aria/title/data-a-target
			By closeSelectors = By.cssSelector(
					"button[aria-label='Close'], button[title='Close'], button[data-a-target='close-button']");
			List<WebElement> list = driver.findElements(closeSelectors);
			for (WebElement b : list) {
				if (b.isDisplayed()) {
					try {
						b.click();
						Thread.sleep(400);
					} catch (Exception ignored) {
					}
				}
			}

			// explicit top banner - buttons with visible text like "Dismiss", "Open", "Open
			// App", "Sign Up"
			try {
				By textBtns = By.xpath(
						"//button[normalize-space()='Dismiss' or normalize-space()='Open' or normalize-space()='Open App' or normalize-space()='Sign Up' or normalize-space()='Got it']");
				List<WebElement> tb = driver.findElements(textBtns);
				for (WebElement b : tb) {
					if (b.isDisplayed()) {
						try {
							b.click();
							Thread.sleep(400);
						} catch (Exception ignored) {
						}
					}
				}
			} catch (Exception ignored) {
			}

			// remove cookie banners or overlays by clicking "aria-label" or by hiding via
			// JS
			try {
				// hide any overlay elements (best-effort)
				((JavascriptExecutor) driver).executeScript(
						"document.querySelectorAll('[role=\"dialog\"],[class*=\"cookie\"],[class*=\"banner\"]').forEach(e=>e.style.display='none');");
				Thread.sleep(300);
			} catch (Exception ignored) {
			}

		} catch (Exception ignored) {
		}
	}

	public void clickSearchIcon() {
		System.out.println("DEBUG: Trying to click search icon...");

		// LOCATOR candidates
		By[] searchIcons = new By[] { By.cssSelector("button[data-a-target='top-nav__search-button']"),
				By.cssSelector("button[aria-label='Search']"), By.cssSelector("button[aria-label*='Search']"),
				By.xpath("//*[contains(@aria-label,'Search') or contains(@title,'Search')]") };

		// Mobile hamburger menu
		By hamburgerMenu = By.cssSelector(
				"button[aria-label='Open navigation menu'], button[data-a-target='mobile-nav__open-button']");

		// Step 1: Try clicking search icon directly
		for (By locator : searchIcons) {
			try {
				WebElement el = driver.findElement(locator);
				el.click();
				System.out.println("DEBUG: Search icon clicked directly");
				return;
			} catch (Exception ignored) {
			}
		}

		// Step 2: Try opening mobile menu
		try {
			WebElement menu = driver.findElement(hamburgerMenu);
			menu.click();
			Thread.sleep(1000);
			System.out.println("DEBUG: Mobile menu opened");
		} catch (Exception ignored) {
			System.out.println("DEBUG: Hamburger menu not found");
		}

		// Step 3: Try clicking search again from menu
		for (By locator : searchIcons) {
			try {
				WebElement el = driver.findElement(locator);
				el.click();
				System.out.println("DEBUG: Search icon clicked after opening menu");
				return;
			} catch (Exception ignored) {
			}
		}

		throw new RuntimeException("Search icon not found on mobile UI");
	}

	// Example simple selector (mobile)
	private final By searchIcon = By.cssSelector("button[data-a-target='top-nav__search-button']");

	// Or robust fallback in the click method:
	By[] candidates = new By[] { By.cssSelector("button[data-a-target='top-nav__search-button']"),
			By.cssSelector("button[aria-label='Search']"), By.cssSelector("button[aria-label*='Search']"),
			By.xpath("//*[contains(@aria-label,'Search') or contains(@title,'Search') or contains(@href,'/search')]") };

	
	
	
	// helper methods
	private String safeGetAttr(WebElement el, String attr) {
		try {
			String v = el.getAttribute(attr);
			return v == null ? null : v.trim();
		} catch (Exception e) {
			return null;
		}
	}

	private String safeGetText(WebElement el) {
		try {
			String t = el.getText();
			return t == null ? null : t.trim();
		} catch (Exception e) {
			return null;
		}
	}

	public void enterSearch(String text) {
		By searchInput = By.cssSelector("input[type='search'], input[placeholder*='Search']");
		wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
		WebElement input = driver.findElement(searchInput);
		input.clear();
		input.sendKeys(text);
		input.sendKeys(Keys.ENTER);
	}

	public void scrollDownTimes(int times) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i < times; i++) {
			js.executeScript("window.scrollBy(0, window.innerHeight * 0.8);");
			try {
				Thread.sleep(1200);
			} catch (InterruptedException ignored) {
			}
		}
	}

	public void selectAnyStreamer() {
	    By[] selectors = new By[] {
	        By.cssSelector("a[href*='/channel/']"),
	        By.cssSelector("a[href*='/video/']"),
	        By.cssSelector("div[data-a-target='search-result'] a"),
	        By.cssSelector("div[data-a-target='card'] a"),
	        By.cssSelector("a[data-a-target='preview-card-title-link']"),
	        By.cssSelector("a[href*='/watch/']"),
	        By.cssSelector("a[href*='/videos/']")
	    };

	    int maxAttempts = 6;
	    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
	        System.out.println("DEBUG: selectAnyStreamer attempt " + attempt);
	        for (By sel : selectors) {
	            try {
	                List<WebElement> list = driver.findElements(sel);
	                if (list != null && !list.isEmpty()) {
	                    for (WebElement e : list) {
	                        try {
	                            if (!e.isDisplayed()) continue;
	                            wait.until(ExpectedConditions.elementToBeClickable(e)).click();
	                            System.out.println("DEBUG: clicked streamer using selector: " + sel);
	                            return;
	                        } catch (Exception clickEx) {
	                            try {
	                                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
	                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", e);
	                                System.out.println("DEBUG: js-clicked streamer");
	                                return;
	                            } catch (Exception ignored) {}
	                        }
	                    }
	                }
	            } catch (Exception ignored) {}
	        }

	        // scroll and wait before next attempt
	        try {
	            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, window.innerHeight * 0.8);");
	            Thread.sleep(1200);
	        } catch (InterruptedException ignored) {}
	    }

	    // Save debug page + screenshot to inspect why nothing matched
	    try {
	        java.nio.file.Path debugDir = java.nio.file.Paths.get("target", "debug");
	        java.nio.file.Files.createDirectories(debugDir);
	        java.nio.file.Files.write(debugDir.resolve("search_results_page.html"),
	                driver.getPageSource().getBytes(java.nio.charset.StandardCharsets.UTF_8));
	        takeScreenshot("target/debug/search_results_screenshot.png");
	        System.out.println("DEBUG: saved page + screenshot to target/debug/");
	    } catch (Exception ex) {
	        System.out.println("DEBUG: failed to save artifacts: " + ex.getMessage());
	    }

	    throw new NoSuchElementException("No streamer selector matched after retries — debug saved to target/debug/");
	}


	public void waitForStreamerPageLoaded() {
		By possible = By
				.cssSelector("div[data-test-selector='player-container'], h1, h2, div[data-a-target='channel-header']");
		wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(possible),
				ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-a-target='player-container']"))));
	}

	public void takeScreenshot(String path) {
		try {
			File target = new File(path);
			target.getParentFile().mkdirs();
			byte[] src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
			try (java.io.FileOutputStream fos = new java.io.FileOutputStream(target)) {
				fos.write(src);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
