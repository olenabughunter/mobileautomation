package com.example.eurodreamstests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.io.ByteArrayInputStream;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Allure;
import org.testng.annotations.Listeners;

@Listeners({io.qameta.allure.testng.AllureTestNg.class})
public class SimpleAppiumTestIT {
    private AndroidDriver driver;

    @BeforeClass
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName("Pixel_9")
                .setUdid("emulator-5554")
                // Target app package requested by the task
                .setAppPackage("com.loro.app.retail.jint")
                .setNoReset(true)
                .setAdbExecTimeout(Duration.ofSeconds(300))
                .setNewCommandTimeout(Duration.ofSeconds(300));

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/"), options);
    }

    @Test
    @Feature("Settings navigation")
    @Description("Start the app, find the settings button on home page (may be in French) and click it. Retry up to 3 times before reporting stuck.")
    @Severity(SeverityLevel.CRITICAL)
    public void openSettingsFromHome() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        try {
            // Ensure target app is active
            try {
                driver.activateApp("com.loro.app.retail.jint");
            } catch (Exception e) {
                // If activateApp is not available or fails, continue and rely on capabilities
                System.out.println("activateApp failed or not supported: " + e.getMessage());
            }

            // Candidate locators (IDs, accessibility ids, and text in French/English)
            By[] candidates = new By[]{
                    // resource id variants (fully-qualified if available)
                    AppiumBy.id("com.loro.app.retail:id/settings_button"),
                    AppiumBy.id("com.loro.app.retail.jint:id/settings_button"),
                    AppiumBy.accessibilityId("Paramètres"),
                    AppiumBy.accessibilityId("Réglages"),
                    AppiumBy.accessibilityId("Settings"),
                    // XPath/text contains (covers French labels like "Paramètres" or "Réglages")
                    AppiumBy.xpath("//*[contains(@text, 'Param') or contains(@text, 'Régl') or contains(@text, 'Settings')]")
            };

            boolean clicked = false;
            int maxAttempts = 3;

            for (int attempt = 1; attempt <= maxAttempts && !clicked; attempt++) {
                Allure.step("Attempt " + attempt + " to find and click settings button");
                System.out.println("Attempt " + attempt + " to find settings");

                for (By locator : candidates) {
                    try {
                        wait.until(ExpectedConditions.elementToBeClickable(locator));
                        WebElement el = driver.findElement(locator);
                        el.click();
                        clicked = true;
                        Allure.step("Clicked settings using locator: " + locator.toString());
                        attachScreenshot("after_click_attempt_" + attempt);
                        break;
                    } catch (Exception e) {
                        // not found/clickable with this locator, continue to next
                        System.out.println("Locator not clickable/found: " + locator.toString() + " - " + e.getMessage());
                    }
                }

                if (!clicked) {
                    // Wait a bit and capture state
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    attachScreenshot("attempt_failed_state_" + attempt);
                }
            }

            if (!clicked) {
                String msg = "Stuck: could not find/click settings after " + maxAttempts + " attempts";
                Allure.step(msg);
                attachScreenshot("stuck_final_state");
                throw new RuntimeException(msg);
            }

            // Verify settings page opened - look for typical French/English labels on settings page
            By settingsHeader = AppiumBy.xpath("//*[contains(@text, 'Param') or contains(@text, 'Régl') or contains(@text, 'Settings') or contains(@content-desc, 'Param')]");
            try {
                wait.withTimeout(Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfElementLocated(settingsHeader));
                Allure.step("Settings page appeared");
                attachScreenshot("settings_page_opened");
            } catch (Exception e) {
                attachScreenshot("settings_missing_after_click");
                throw new RuntimeException("Settings page did not open or header not found: " + e.getMessage());
            }

        } catch (Exception e) {
            // Attach final screenshot and rethrow so CI picks up failure
            try { attachScreenshot("error_final"); } catch (Exception ignored) {}
            Allure.step("Test failed: " + e.getMessage());
            throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
        }
    }

    private void attachScreenshot(String name) {
        try {
            byte[] bytes = driver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name + ".png", new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            System.out.println("Failed to take/attach screenshot: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
