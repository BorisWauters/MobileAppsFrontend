package be.kul.app;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import be.kul.app.adapters.QuestionAdapter;
import be.kul.app.callback.GeneralCallbackArray;
import be.kul.app.callback.QuestionCallback;
import be.kul.app.callback.QuestionDeleteCallback;
import be.kul.app.callback.UserCallback;
import be.kul.app.room.database.RoomDatabase;
import be.kul.app.room.model.AnswerEntity;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;
import be.kul.app.listeners.QuestionOnClickListener;
import be.kul.app.room.repositories.AnswerEntityRepository;
import be.kul.app.room.repositories.QuestionEntityRepository;
import be.kul.app.room.repositories.UserEntityRepository;
import be.kul.app.room.viewmodels.AnswerEntityViewModel;
import be.kul.app.room.viewmodels.QuestionEntityViewModel;
import be.kul.app.room.viewmodels.UserEntityViewModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class Dashboard extends AppCompatActivity {

    private String username;
    private int userId;
    private UserEntity userEntity;

    private TextView mStatusTextView;

    //recycler view stuff
    private List<QuestionEntity> questionList = new ArrayList<>();
    private RecyclerView recyclerView;
    private QuestionAdapter mAdapter;

    // hamburger menu stuff
    private DrawerLayout mDrawerLayout;


    // floating action button stuff
    private FloatingActionButton fab;
    private RestController restController;

    // Room database stuff
    private UserEntityViewModel mUserEntityViewModel;
    private QuestionEntityViewModel mQuestionEntityViewModel;
    private AnswerEntityViewModel mAnswerEntityViewModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_menu_white_18dp);

        mStatusTextView = findViewById(R.id.status);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        /* extract intent info*/
        Intent intent = getIntent();
        userEntity = (UserEntity)intent.getSerializableExtra("UserEntity");

        mStatusTextView.setText(userEntity.getUsername() + " " + userEntity.getUserId());

        //Room database model initialization
        mUserEntityViewModel = ViewModelProviders.of(this).get(UserEntityViewModel.class);
        mQuestionEntityViewModel = ViewModelProviders.of(this).get(QuestionEntityViewModel.class);
        mAnswerEntityViewModel = ViewModelProviders.of(this).get(AnswerEntityViewModel.class);

        //save the logged in user in the room database
        mUserEntityViewModel.getUserById(userEntity.getUserId(), new UserCallback() {
            @Override
            public void onSuccess(UserEntity userEntity) {
                if(userEntity == null)
                    mUserEntityViewModel.insert(userEntity);
            }
        });



        //facebook logout: LoginManager.getInstance().logOut();

        // initialize floating add button
        fab = findViewById(R.id.fab);

        // set on click listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToNewQuestion();
                /*Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });



        // recycler view source: https://www.androidhive.info/2016/01/android-working-with-recycler-view/
        // set up recycler view to sho all questions
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new QuestionAdapter(questionList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // add click listener to the view
        recyclerView.addOnItemTouchListener(new QuestionOnClickListener(getApplicationContext(), recyclerView, new QuestionOnClickListener.ClickListener(){
            @Override
            public void onClick(View view, int position) {
                QuestionEntity questionEntity = questionList.get(position);
                Toast.makeText(getApplicationContext(), questionEntity.getQuestionTitle() + " is selected!", Toast.LENGTH_SHORT).show();
                sendToQuestionDetail(questionEntity);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        recyclerView.setAdapter(mAdapter);

        prepareQuestionData();

        // add callback to hamburger menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        if(menuItem.getItemId() == R.id.logout){
                            // sign user out
                        }
                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendToQuestionDetail(QuestionEntity questionEntity){
        Intent i = new Intent(this, Question.class);
        i.putExtra("question", questionEntity);
        i.putExtra("user", userEntity);
        startActivity(i);
    }

    public void sendToNewQuestion(){
        Intent i = new Intent(this, NewQuestion.class);
        i.putExtra("UserEntity", userEntity);
        startActivity(i);
    }

    private void prepareQuestionData(){
        restController = new RestController(this);
        restController.requestQuestions(new GeneralCallbackArray() {
            @Override
            public void onSuccess(JSONArray result) {
                System.out.println(result.toString());

                // if no questions, there will be no jsonArray
                if(result != null){
                    for (int i = 0; i < result.length() ; i++) {
                        JSONObject question = null;
                        try {
                            question = result.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            // extract the user object from the question
                            JSONObject userObject = question.getJSONObject("userEntity");
                            UserEntity userEntity = new UserEntity(Integer.parseInt(userObject.getString("userId")), userObject.getString("username"), userObject.getString("password"));
                            QuestionEntity questionEntity = new QuestionEntity(question.getInt("questionId"), question.getString("questionTitle"),
                                    question.getString("questionDescription"), Integer.parseInt(userObject.getString("userId")), userEntity);
                            questionList.add(questionEntity);

                            mQuestionEntityViewModel.deleteAllQuestions(new QuestionDeleteCallback() {
                                @Override
                                public void onSuccess() {
                                    addNewQuestionsToRoom();
                                }
                            });




                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onFail() {

            }
        });
       /* questionList.add(new QuestionEntity(1,"test 1", "description 1", 1));
        questionList.add(new QuestionEntity(2,"test 2", "description 2", 1));
        questionList.add(new QuestionEntity(3,"test 3", "description 3", 1));
        questionList.add(new QuestionEntity(4,"test 4", "description 4", 1));
        questionList.add(new QuestionEntity(5,"test 5", "description 5", 1));
        questionList.add(new QuestionEntity(6,"test 6", "description 6", 1));
        questionList.add(new QuestionEntity(7,"test 7", "description 7", 1));
        questionList.add(new QuestionEntity(8,"test 8", "description 8", 1));*/

    }

    private void addNewQuestionsToRoom(){
        for(QuestionEntity questionEntity : questionList){
            mQuestionEntityViewModel.insert(questionEntity);
        }
    }
}
