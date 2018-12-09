package be.kul.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import be.kul.app.callback.GeneralCallback;
import be.kul.app.dao.UserEntity;
import org.json.JSONObject;

public class NewQuestion extends AppCompatActivity {

    private EditText questionTitleText, questionDescriptionText;
    private Button addQuestion;
    private int userId;
    private UserEntity userEntity;
    private RestController restController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_question);

        Intent i = getIntent();
        userEntity= (UserEntity) i.getSerializableExtra("UserEntity");
        restController = new RestController(this);

        questionTitleText = findViewById(R.id.questionTitleText);
        questionDescriptionText = findViewById(R.id.questionDescriptionText);

        addQuestion = findViewById(R.id.questionAddButton);

        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuestion();
            }
        });

    }

    public void addQuestion(){
        restController.addNewQuestion(questionTitleText.getText().toString(), questionDescriptionText.getText().toString(), userEntity, new GeneralCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if(!result.isNull("questionId")){
                    sendToDashboard();
                }
            }

            @Override
            public void onFail() {

            }
        });
    }

    public void sendToDashboard(){
        Intent intent = new Intent(this, Dashboard.class);
        intent.putExtra("UserEntity", userEntity);
        startActivity(intent);
    }
}
