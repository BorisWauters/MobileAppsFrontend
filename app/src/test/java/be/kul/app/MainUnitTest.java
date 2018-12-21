package be.kul.app;

import android.content.Intent;
import android.support.transition.Visibility;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.TestCase.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MainUnitTest {

    /*@Test
    public void clickingLogin_shouldStartLoginActivity() {
        MainActivity activity = Robolectric.setupActivity(MainActivity.class);
        activity.findViewById(R.id.login_buttonEmail).performClick();

        Intent expectedIntent = new Intent(activity, LoginActivity.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }*/

    @Test
    public void loginButton_shouldHaveText() {
        MainActivity activity = Robolectric.setupActivity(MainActivity.class);
        int visibility = activity.findViewById(R.id.login_buttonEmail).getVisibility();

        assertEquals(visibility, View.VISIBLE);
    }
}
