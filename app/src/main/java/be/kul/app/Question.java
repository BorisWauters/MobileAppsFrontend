package be.kul.app;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import be.kul.app.adapters.AnswerAdapter;
import be.kul.app.adapters.QuestionAdapter;
import be.kul.app.callback.GeneralCallback;
import be.kul.app.callback.GeneralCallbackArray;
import be.kul.app.dao.AnswerEntity;
import be.kul.app.dao.QuestionEntity;
import be.kul.app.dao.UserEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Question extends AppCompatActivity {

    private QuestionEntity questionEntity;
    private UserEntity userEntity;
    private TextView mStatusView, answer;
    private TextView questionTitle, questionDescription;

    private RestController restController;
    private Button addAnswer;

    private List<AnswerEntity> answerList = new ArrayList<>();
    private RecyclerView recyclerView;
    private AnswerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Intent intent = getIntent();
        questionEntity =(QuestionEntity)intent.getSerializableExtra("question");
        userEntity = (UserEntity) intent.getSerializableExtra("user");

        restController = new RestController(this);

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

        mAdapter = new AnswerAdapter(answerList);
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
                                    questionObject.getString("questionTitle"), questionObject.getString("questionDescription"), userEntityQuestion.getUserId(),userEntityQuestion);
                            AnswerEntity answerEntity = new AnswerEntity(answer.getInt("answerId"), answer.getString("answerDescription"), questionEntity, userEntityAnswer);
                            answerList.add(answerEntity);
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
