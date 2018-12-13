package be.kul.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import be.kul.app.callback.AllQuestionsCallback;
import be.kul.app.callback.GeneralCallbackArray;
import be.kul.app.callback.QuestionDeleteCallback;
import be.kul.app.callback.UserCallback;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;
import be.kul.app.listeners.QuestionOnClickListener;
import be.kul.app.room.viewmodels.AnswerEntityViewModel;
import be.kul.app.room.viewmodels.QuestionEntityViewModel;
import be.kul.app.room.viewmodels.UserEntityViewModel;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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

    private GoogleApiClient mGoogleApiClient;

    private boolean networkConnection;



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

        // check for network connection
        networkConnection = isOnline();

        /* extract intent info*/
        Intent intent = getIntent();
        userEntity = (UserEntity)intent.getSerializableExtra("UserEntity");

        mStatusTextView.setText(userEntity.getUsername() + " " + userEntity.getUserId());

        //Room database model initialization
        mUserEntityViewModel = ViewModelProviders.of(this).get(UserEntityViewModel.class);
        mQuestionEntityViewModel = ViewModelProviders.of(this).get(QuestionEntityViewModel.class);
        mAnswerEntityViewModel = ViewModelProviders.of(this).get(AnswerEntityViewModel.class);

        //save the logged in user in the room database
        mUserEntityViewModel.getUserByName(userEntity.getUsername(), new UserCallback() {
            @Override
            public void onSuccess(UserEntity userEntity1) {
                if(userEntity1 == null)
                    mUserEntityViewModel.insert(userEntity);
            }
        });




        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();


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
        // set up recycler view to show all questions
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

        if(networkConnection)
            prepareQuestionDataWithInternet();
        else{
            // if no internet connection, no questions can be added
            fab.setVisibility(View.GONE);
            Snackbar.make(findViewById(android.R.id.content), "There is no internet connection!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            prepareQuestionDataWithoutInternet(new QuestionDeleteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Stuff that updates the UI
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                }
            });
        }


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
                            LoginManager.getInstance().logOut();
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                    new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(Status status) {
                                            // ...
                                            Toast.makeText(getApplicationContext(),"Logged Out",Toast.LENGTH_SHORT).show();
                                            Intent i=new Intent(getApplicationContext(),MainActivity.class);
                                            startActivity(i);
                                        }
                                    });
                            Intent intent1 = new Intent(Dashboard.this,MainActivity.class);
                            startActivity(intent1);
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

    private void prepareQuestionDataWithInternet(){
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


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    addNewQuestionsToRoom();
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onFail() {

            }
        });

    }

    public void prepareQuestionDataWithoutInternet(final QuestionDeleteCallback questionDeleteCallback){
        mQuestionEntityViewModel.getAllQuestionsAsList(new AllQuestionsCallback() {
            @Override
            public void onSuccess(List<QuestionEntity> questions) {
                questionList.clear();
                questionList.addAll(questions);
                questionDeleteCallback.onSuccess();
            }
        });
    }


    private void addNewQuestionsToRoom(){
        // first delete previous saved questions
        mQuestionEntityViewModel.deleteAllQuestions(new QuestionDeleteCallback() {
            @Override
            public void onSuccess() {
                for(QuestionEntity questionEntity : questionList){
                    mQuestionEntityViewModel.insert(questionEntity);
                }

            }
        });

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    public void onStop(){
        super.onStop();
        // Method to stop the service

        stopService(new Intent(getBaseContext(), ShakeService.class));

        //startService(new Intent(this, ShakeService.class));

    }

}
