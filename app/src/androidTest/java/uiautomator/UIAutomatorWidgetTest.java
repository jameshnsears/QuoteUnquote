package uiautomator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

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
        // don't run in GitHub Actions
        if (System.getenv("CI") == null) {
            // only run at this API level, as this emulator's UI is what we've coded against
            return Build.VERSION.SDK_INT == Build.VERSION_CODES.R;
        }
        return false;
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

            UiObject2 previousSwitch = device.findObject(
                    By.desc("Add displayed Source / Search results to Previous")
            );
            assertTrue(previousSwitch.isChecked());

            UiObject2 author = device.findObject(
                    By.desc("Source: %d")
            );
            assertEquals("Source: 5", author.getText());

            UiObject2 favourites = device.findObject(
                    By.desc("Favourites: %d")
            );
            assertEquals("Favourites: 0", favourites.getText());

            UiObject2 search = device.findObject(
                    By.desc("Search: %d")
            );
            assertEquals("Search: 0", search.getText());
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
                    By.desc("Favourites: %d")
            );
            assertEquals("Favourites: 1", favourites.getText());

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

    @Test
    public void test_next_sequential() {
        // WIP

        /*
        content-desc: ...
        Quotation: press q1.1
        Appearance: press Appearance
        Toolbar: press Toolbar
        Next, sequential: press Next, Sequential
        press Back button

        Quotation: assert q1.1
        Author: assert a1
        Position: assert 1/8

        Next, sequential: press Next, Sequential
        Quotation: assert q1.2
        Author: assert a1
        Position: assert 2/8

        Next, sequential: press Next, Sequential
        Quotation: assert q1.3
        Author: assert a1
        Position: assert 3/8

        Previous: press Previous
        Quotation: assert q1.2
        Author: assert a1
        Position: assert 2/8

        Quotation: press q1.2
        Author: %d: press Author
        Selected Author: select a2
        press Back button

        Quotation: assert q2.1
        Author: assert a2
        Position: assert 1/2

        Next, sequential: press Next, Sequential
        Quotation: assert q2.2
        Author: assert a2
        Position: assert 2/2
        */
    }
}
