package be.kul.app.dao;

import java.io.Serializable;

public class QuestionEntity implements Serializable {

    private String questionTitle;
    private int questionId;
    private String questionDescription;
    private int userId;
    private UserEntity userEntity;

    public QuestionEntity(){}

    public QuestionEntity(int questionId, String questionTitle, String questionDescription, int userId, UserEntity userEntity){
        this.questionId = questionId;
        this.questionTitle = questionTitle;
        this.questionDescription = questionDescription;
        this.userId = userId;
        this.userEntity = userEntity;
    }

    // getters and setters

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getQuestionDescription() {
        return questionDescription;
    }

    public void setQuestionDescription(String questionDescription) {
        this.questionDescription = questionDescription;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
