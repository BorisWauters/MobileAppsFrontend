package be.kul.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import be.kul.app.adapters.AnswerAdapter;
import be.kul.app.callback.AnswerCallback;
import be.kul.app.callback.AnswerDeleteCallback;
import be.kul.app.callback.GeneralCallback;
import be.kul.app.callback.GeneralCallbackArray;
import be.kul.app.room.model.AnswerEntity;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;
import be.kul.app.room.repositories.UserEntityRepository;
import be.kul.app.room.viewmodels.AnswerEntityViewModel;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Question extends AppCompatActivity {

    private QuestionEntity questionEntity;
    private UserEntity userEntity;
    private TextView mStatusView, answer;
    private TextView questionTitle, questionDescription;

    private RestController restController;
    private Button addAnswer;

    private List<AnswerEntity> answerList = new ArrayList<>();
    private Map<Integer, UserEntity> userMap = new HashMap<>();
    private RecyclerView recyclerView;
    private AnswerAdapter mAdapter;

    private AnswerEntityViewModel mAnswerEntityViewModel;
    private UserEntityViewModel mUserEntityViewModel;

    private GoogleApiClient mGoogleApiClient;

    // hamburger menu stuff
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Intent intent = getIntent();
        questionEntity =(QuestionEntity)intent.getSerializableExtra("question");
        userEntity = (UserEntity) intent.getSerializableExtra("user");

        //Voor menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Voor menu
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_menu_white_18dp);

        // menu
        mDrawerLayout = findViewById(R.id.drawer_layout);

        restController = new RestController(this);

        mAnswerEntityViewModel = ViewModelProviders.of(this).get(AnswerEntityViewModel.class);
        mUserEntityViewModel = ViewModelProviders.of(this).get(UserEntityViewModel.class);

        mStatusView = findViewById(R.id.status);
        mStatusView.setText("Question Id: " + questionEntity.getQuestionId());
        addAnswer = findViewById(R.id.answerAdd);
        answer = findViewById(R.id.answer);

        questionTitle = findViewById(R.id.questionTitle);
        questionDescription = findViewById(R.id.questionDescription);

        questionTitle.setText(questionEntity.getQuestionTitle());
        questionDescription.setText(questionEntity.getQuestionDescription());

        // set up recycler view to show all anwers
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_answers);

        mAdapter = new AnswerAdapter(answerList,userMap);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        recyclerView.setAdapter(mAdapter);

        prepareAnswerData();

        addAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(answer.getText().equals("")){
                    Snackbar.make(v, "Please enter an answer!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    submitAnswer(answer.getText().toString());
                }
            }
        });



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

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
                            Intent intent1 = new Intent(Question.this,MainActivity.class);
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

    private void submitAnswer(String answer){
        //submit the answer to the database
        restController.submitAnswer(answer, questionEntity, userEntity, new GeneralCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                sendToDashboard();
            }

            @Override
            public void onFail() {

            }
        });

    }

    private void sendToDashboard(){
        Intent intent = new Intent(this, Dashboard.class);
        intent.putExtra("UserEntity", userEntity);
        startActivity(intent);
    }

    private void prepareAnswerData(){

        restController.requestAnswers(questionEntity.getQuestionId(), new GeneralCallbackArray() {
            @Override
            public void onSuccess(JSONArray result) {
                System.out.println(result.toString());

                // if no questions, there will be no jsonArray
                if(result != null){
                    for (int i = 0; i < result.length() ; i++) {
                        JSONObject answer = null;
                        try {
                            answer = result.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            // extract the user object from the question
                            JSONObject userEntityAnswerObject = answer.getJSONObject("userEntity");
                            JSONObject questionObject = answer.getJSONObject("questionEntity");
                            JSONObject userEntityQuestionObject = questionObject.getJSONObject("userEntity");
                            UserEntity userEntityAnswer = new UserEntity(userEntityAnswerObject.getInt("userId"), userEntityAnswerObject.getString("username"),
                                    userEntityAnswerObject.getString("password"));
                            UserEntity userEntityQuestion = new UserEntity(userEntityQuestionObject.getInt("userId"), userEntityQuestionObject.getString("username"),
                                    userEntityQuestionObject.getString("password"));
                            QuestionEntity questionEntity = new QuestionEntity(Integer.parseInt(questionObject.getString("questionId")),
                                    questionObject.getString("questionTitle"), questionObject.getString("questionDescription"), userEntityQuestion.getUserId(), userEntityQuestion);
                            AnswerEntity answerEntity = new AnswerEntity(answer.getInt("answerId"), answer.getString("answerDescription"), questionEntity, userEntityAnswer);
                            answerList.add(answerEntity);
                            if(!userMap.containsKey(userEntityAnswer.getUserId())){
                                userMap.put(userEntityAnswer.getUserId(), userEntityAnswer);
                            }
                            mAnswerEntityViewModel.deleteAllAnswers(new AnswerDeleteCallback() {
                                @Override
                                public void onSuccess() {
                                    addNewAnswersToRoom();
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

    private void addNewAnswersToRoom(){
        for(AnswerEntity answerEntity : answerList){
            mAnswerEntityViewModel.insert(answerEntity);
        }
    }
}
