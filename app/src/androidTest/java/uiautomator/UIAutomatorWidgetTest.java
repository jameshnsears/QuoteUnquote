package uiautomator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import com.github.jameshnsears.quoteunquote.BuildConfig;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UIAutomatorWidgetTest extends UiAutomatorUtility {
    @Before
    public void setUp() {
        assumeTrue(BuildConfig.FLAVOR.equals("uiautomator"));
    }

    @Test
    public void test_0_createWidget() {
        waitForLauncher();
        dragWidgetOntoHomeScreen();
        waitForConfigurationScreen();
        closeConfigurationScreen();
    }

    @Test
    public void test_configuration_inspect_0_Quotations() throws InterruptedException {
        displayConfigurationScreen();

        waitForConfigurationScreen();

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        device.waitForWindowUpdate(device.getLauncherPackageName(), TIMEOUT);
        Thread.sleep(1000);

        UiObject2 all = device.findObject(
                By.desc("All: %d")
        );
        assertThat(all.getText(), equalTo("All: 10"));

        UiObject2 author = device.findObject(
                By.desc("Source: %d")
        );
        assertThat(author.getText(), equalTo("Source: 5"));

        UiObject2 favourites = device.findObject(
                By.desc("Favourite: %d")
        );
        assertThat(favourites.getText(), equalTo("Favourite: 0"));
    }

    @Test
    public void test_configuration_inspect_1_Appearance() {
        displayConfigurationScreen();

        waitForConfigurationScreen();

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        UiObject2 appearance = device.findObject(
                By.desc("Appearance")
        );
        appearance.click();

        closeConfigurationScreen();
    }

    @Test
    public void test_favourite_0_Mark() {
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
        assertThat(favourites.getText(), equalTo("Favourite: 1"));

        closeConfigurationScreen();
    }

    @Test
    public void test_favourite_1_Export() {
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
        assertThat(quotation.getText(), equalTo("q1.1\n"));
    }
}
