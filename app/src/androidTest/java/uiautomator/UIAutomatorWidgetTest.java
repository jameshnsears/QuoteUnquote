package uiautomator;

import static org.junit.Assert.assertEquals;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import com.github.jameshnsears.quoteunquote.BuildConfig;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UIAutomatorWidgetTest extends UiAutomatorUtility {
    @Test
    public void test_0_createWidget() {
        if (canTestBeRun()) {
            waitForLauncher();
            dragWidgetOntoHomeScreen();
            waitForConfigurationScreen();
            closeConfigurationScreen();
        }
    }

    private boolean canTestBeRun() {
        return (BuildConfig.FLAVOR.equals("uiautomator"));
    }

    @Test
    public void test_configuration_inspect_0_Quotations() throws InterruptedException {
        if (canTestBeRun()) {
            displayConfigurationScreen();

            waitForConfigurationScreen();

            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

            device.waitForWindowUpdate(device.getLauncherPackageName(), TIMEOUT);
            Thread.sleep(1000);

            UiObject2 all = device.findObject(
                    By.desc("All: %d")
            );
            assertEquals("All: 10", all.getText());

            UiObject2 author = device.findObject(
                    By.desc("Source: %d")
            );
            assertEquals("Source: 5", author.getText());

            UiObject2 favourites = device.findObject(
                    By.desc("Favourite: %d")
            );
            assertEquals("Favourite: 0", favourites.getText());
        }
    }

    @Test
    public void test_configuration_inspect_1_Appearance() {
        if (canTestBeRun()) {
            displayConfigurationScreen();

            waitForConfigurationScreen();

            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

            UiObject2 appearance = device.findObject(
                    By.desc("Appearance")
            );
            appearance.click();

            closeConfigurationScreen();
        }
    }

    @Test
    public void test_favourite_0_Mark() {
        if (canTestBeRun()) {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

            UiObject2 favourite = device.findObject(
                    By.desc("Favourite, toggle")
            );
            favourite.click();

            device.waitForWindowUpdate(device.getLauncherPackageName(), TIMEOUT);

            displayConfigurationScreen();

            waitForConfigurationScreen();

            UiObject2 favourites = device.findObject(
                    By.desc("Favourite: %d")
            );
            assertEquals("Favourite: 1", favourites.getText());

            closeConfigurationScreen();
        }
    }

    @Test
    public void test_favourite_1_Export() {
        if (canTestBeRun()) {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

            displayConfigurationScreen();

            waitForConfigurationScreen();

            UiObject2 export = device.findObject(
                    By.desc("Export")
            );
            export.click();

            // unable to get SAVE button in uiautomatorviewer
            device.waitForWindowUpdate(device.getLauncherPackageName(), TIMEOUT);

            device.pressBack();

            device.pressBack();

            UiObject2 quotation = device.findObject(
                    By.desc("Quotation")
            );
            assertEquals("q1.1\n", quotation.getText());
        }
    }
}
