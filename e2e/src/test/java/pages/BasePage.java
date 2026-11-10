package pages;

import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage extends PageObject {

    protected static final String BASE_URL = System.getProperty("webdriver.base.url", "http://localhost:8081");
    protected static final Duration TIMEOUT = Duration.ofSeconds(12);

    protected WebElement waitVisible(By locator) {
        return new WebDriverWait(getDriver(), TIMEOUT)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return new WebDriverWait(getDriver(), TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitClickable(locator).click();
    }

    protected void type(By locator, String value) {
        WebElement element = waitVisible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected void openPath(String path) {
        getDriver().get(BASE_URL + path);
    }

    protected void waitUntilText(By locator, String expected) {
        new WebDriverWait(getDriver(), TIMEOUT)
                .until(ExpectedConditions.textToBe(locator, expected));
    }

    protected void waitUntilContainsText(By locator, String expected) {
        new WebDriverWait(getDriver(), TIMEOUT)
                .until(ExpectedConditions.textToBePresentInElementLocated(locator, expected));
    }

    protected void waitUntilNoToasts() {
        new WebDriverWait(getDriver(), TIMEOUT)
                .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".toast")));
    }

    protected String xpathText(String value) {
        return "'" + value.replace("'", "&apos;") + "'";
    }
}
