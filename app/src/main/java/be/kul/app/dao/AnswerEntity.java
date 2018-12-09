package be.kul.app.dao;

public class AnswerEntity {

    private int answerId;
    private String answerDescription;
    private QuestionEntity questionEntity;
    private UserEntity userEntity;

    public AnswerEntity(){}

    public AnswerEntity(int answerId, String answerDescription, QuestionEntity questionEntity, UserEntity userEntity){
        this.answerId = answerId;
        this.answerDescription = answerDescription;
        this.questionEntity = questionEntity;
        this.userEntity = userEntity;
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

    public QuestionEntity getQuestionEntity() {
        return questionEntity;
    }

    public void setQuestionEntity(QuestionEntity questionEntity) {
        this.questionEntity = questionEntity;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }
}
