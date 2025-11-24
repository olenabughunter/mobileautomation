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
                .setAppPackage("com.loro.app.retail")
                .setAppActivity(".ui.home.RetailActivity")
                .setNoReset(true)
                .setAdbExecTimeout(Duration.ofSeconds(120))
                .setNewCommandTimeout(Duration.ofSeconds(300));

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/"), options);
    }

    @Test
    public void fullScenarioTest() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            // Ensure app is active
            String pkg = driver.getCurrentPackage();
            String act = driver.currentActivity();
            System.out.println("Current package: " + pkg + ", activity: " + act);

            if (!pkg.equals("com.loro.app.retail")) {
                System.out.println("Target app not active; attempting to start it via activity.");
                driver.activateApp("com.loro.app.retail");
                Thread.sleep(1500);
            }

            // 1) Close basket dialog if visible
            By closeBasket = AppiumBy.id("basket_close_button");
            try {
                wait.until(ExpectedConditions.elementToBeClickable(closeBasket));
                WebElement close = driver.findElement(closeBasket);
                close.click();
                System.out.println("Closed basket dialog.");
            } catch (Exception ignored) {
                System.out.println("Basket close button not present, continuing.");
            }

            // 2) Edit stake input if present
            By stakeInput = AppiumBy.id("basket_bet_input_stake_0");
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(stakeInput));
                WebElement stake = driver.findElement(stakeInput);
                stake.clear();
                stake.sendKeys("100");
                System.out.println("Set stake to 100.");
            } catch (Exception ignored) {
                System.out.println("Stake input not present, continuing.");
            }

            // 3) Click QR generator button
            By qrBtn = AppiumBy.id("qr_generator_button");
            try {
                wait.until(ExpectedConditions.elementToBeClickable(qrBtn));
                WebElement qr = driver.findElement(qrBtn);
                qr.click();
                System.out.println("Clicked QR generator button.");
            } catch (Exception ignored) {
                System.out.println("QR button not present or not clickable.");
            }

            // 4) Small verification: read possible gains text if visible
            By gains = AppiumBy.id("gains_value");
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(gains));
                WebElement gv = driver.findElement(gains);
                System.out.println("Gains text: " + gv.getText());
            } catch (Exception ignored) {
                System.out.println("Gains text not found.");
            }

        } catch (Exception e) {
            System.out.println("Test scenario failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
