package be.kul.app.room.model;

import android.arch.persistence.room.*;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "question"/*,foreignKeys = @ForeignKey(entity = UserEntity.class,
        parentColumns = "questionId",
        childColumns = "userId")*/)
public class QuestionEntity implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "questionId")
    private int questionId;

    @ColumnInfo(name = "questionTitle")
    private String questionTitle;

    @ColumnInfo(name = "questionDescription")
    private String questionDescription;

    @ColumnInfo(name = "userId")
    private int userId;


    public QuestionEntity(){}

    public QuestionEntity(int questionId, String questionTitle, String questionDescription, int userId, UserEntity userEntity){
        this.questionId = questionId;
        this.questionTitle = questionTitle;
        this.questionDescription = questionDescription;
        this.userId = userId;
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
