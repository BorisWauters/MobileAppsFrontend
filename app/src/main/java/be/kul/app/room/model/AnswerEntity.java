package be.kul.app.room.model;

import android.arch.persistence.room.*;
import android.support.annotation.NonNull;

@Entity(tableName = "answer"/*,foreignKeys = {@ForeignKey(entity = UserEntity.class,
        parentColumns = "answerId",
        childColumns = "user_id"), @ForeignKey(entity = QuestionEntity.class,
        parentColumns = "answerId",
        childColumns = "question_id")}*/)
public class AnswerEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "answerId")
    private int answerId;

    @ColumnInfo(name = "answerDescription")
    private String answerDescription;

    @ColumnInfo(name = "questionId")
    private int questionId;

    @ColumnInfo(name = "userId")
    private int userId;


    public AnswerEntity(){}

    public AnswerEntity(int answerId, String answerDescription, QuestionEntity questionEntity, UserEntity userEntity){
        this.answerId = answerId;
        this.answerDescription = answerDescription;
        this.userId = userEntity.getUserId();
        this.questionId = questionEntity.getQuestionId();

    }

    // getters and setters


    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public String getAnswerDescription() {
        return answerDescription;
    }

    public void setAnswerDescription(String answerDescription) {
        this.answerDescription = answerDescription;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


}
