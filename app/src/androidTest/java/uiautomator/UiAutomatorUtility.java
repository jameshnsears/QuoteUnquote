package uiautomator;

import android.graphics.Point;
import android.graphics.Rect;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

public class UiAutomatorUtility {
    protected static final int TIMEOUT = 2500;
    protected UiDevice device;

    protected void waitForLauncher() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressHome();
        device.wait(Until.hasObject(By.pkg(device.getLauncherPackageName()).depth(0)), TIMEOUT);
    }

    protected void dragWidgetOntoHomeScreen() {
        // tuned for UI on API 30 / R

        // long press on center of home screen
        int x = device.getDisplayWidth() / 2;
        int y = device.getDisplayHeight() / 2;
        device.swipe(x, y, x, y, 150);

        // launch widgets list
        device.findObject(By.text("Widgets")).click();
        device.waitForIdle();

        // scroll to our widget in the list
        UiObject2 widget = device.findObject(By.text("Quote Unquote"));
        while (widget == null) {
            // Swipe bottom to top
            device.swipe(x, y, x, 0, 25);
            widget = device.findObject(By.text("Quote Unquote"));
        }

        // place widget on home screen
        Rect visibleBounds = widget.getVisibleBounds();
        Point c = new Point(visibleBounds.left + 150, visibleBounds.bottom + 150);
        Point dest = new Point(c.x + 250, c.y + 250);
        Point[] pointArray = {c, c, dest};
        device.swipe(pointArray, 25);
    }

    protected void displayConfigurationScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.waitForIdle();

        UiObject2 quotation = device.findObject(
                By.desc("Quotation")
        );
        quotation.click();
    }

    protected void waitForConfigurationScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.waitForIdle();

        device.wait(Until.hasObject(By.text("Configuration")), UiAutomatorUtility.TIMEOUT);
    }

    protected void closeConfigurationScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.waitForIdle();

        device.pressBack();
        device.wait(Until.hasObject(By.text("Quote Unquote")), UiAutomatorUtility.TIMEOUT);
    }
}
