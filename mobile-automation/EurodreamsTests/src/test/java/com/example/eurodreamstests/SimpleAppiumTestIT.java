package com.example.eurodreamstests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class SimpleAppiumTestIT {
    private AndroidDriver driver;

    @BeforeClass
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName("Pixel_9_Pro_XL")
                .setUdid("emulator-5554")
                // Provide either an .apk path or the appPackage/appActivity if app is already on device
                // .setApp("/path/to/app.apk")
                .setAppPackage("com.android.settings")
                .setAppActivity(".Settings")
                .setNoReset(true)
                .setAdbExecTimeout(Duration.ofSeconds(120))
                .setNewCommandTimeout(Duration.ofSeconds(300));

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/"), options);
    }

    @Test
    public void sampleTest() {
        // Sanity check: verify session by printing current package and activity
        String pkg = driver.getCurrentPackage();
        String act = driver.currentActivity();
        System.out.println("Current package: " + pkg + ", activity: " + act);

        // Try a small interaction if available: click the settings button in the app (if package matches)
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            By settingsBtn = AppiumBy.id("com.loro.app.retail:id/home_settings_button");
            if (pkg.equals("com.loro.app.retail")) {
                wait.until(ExpectedConditions.elementToBeClickable(settingsBtn));
                WebElement el = driver.findElement(settingsBtn);
                el.click();
                System.out.println("Clicked settings button.");
                // wait a bit and go back
                Thread.sleep(1000);
                driver.navigate().back();
                System.out.println("Navigated back.");
            } else {
                System.out.println("Target app not active, skipping interactions.");
            }
        } catch (Exception e) {
            System.out.println("Interaction failed: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
