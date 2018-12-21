package be.kul.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import com.google.android.apps.common.testing.accessibility.framework.proto.FrameworkProtos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenuItem;

import static junit.framework.TestCase.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class DashboardUnitTest {

    @Before
    public void setup() {

    }

    /*@Test
    public void clickingQuestion_shouldStartQuestionActivity() {
        Dashboard activity = Robolectric.setupActivity(Dashboard.class);

        // Recycler view test



        Intent expectedIntent = new Intent(activity, Question.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }*/

    @Test
    public void clickingNewQuestion_shouldStartNewQuestionActivity() {
        Dashboard activity = Robolectric.setupActivity(Dashboard.class);

        // iets met login

        activity.findViewById(R.id.fab).performClick();

        Intent expectedIntent = new Intent(activity, NewQuestion.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }




}
