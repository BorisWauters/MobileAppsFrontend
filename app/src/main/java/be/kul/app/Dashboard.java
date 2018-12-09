package be.kul.app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import be.kul.app.adapters.QuestionAdapter;
import be.kul.app.callback.GeneralCallback;
import be.kul.app.callback.GeneralCallbackArray;
import be.kul.app.dao.QuestionEntity;
import be.kul.app.dao.UserEntity;
import be.kul.app.listeners.QuestionOnClickListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
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

    // floating action button stuff
    private FloatingActionButton fab;
    private RestController restController;

    private Toolbar mActionBarToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mStatusTextView = findViewById(R.id.status);

        /* extract intent info*/
        Intent intent = getIntent();
        userEntity = (UserEntity)intent.getSerializableExtra("UserEntity");

        mStatusTextView.setText(userEntity.getUsername() + " " + userEntity.getUserId());

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
                            questionList.add(new QuestionEntity(question.getInt("questionId"), question.getString("questionTitle"),
                                    question.getString("questionDescription"), Integer.parseInt(userObject.getString("userId")), userEntity));
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
}
