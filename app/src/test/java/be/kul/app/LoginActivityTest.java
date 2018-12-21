package be.kul.app;

import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest {

    @Test
    public void loginButton_shouldHaveText() {
        LoginActivity activity = Robolectric.setupActivity(LoginActivity.class);
        int visibility = activity.findViewById(R.id.email_sign_in_button).getVisibility();

        assertEquals(visibility, View.VISIBLE);
    }
}
