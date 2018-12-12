package be.kul.app.room.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;

import java.util.ArrayList;
import java.util.List;
@Dao
public interface QuestionEntityDAO {
    @Insert
    void insert(QuestionEntity questionEntity);

    @Query("DELETE FROM question")
    void deleteAll();

    @Query("SELECT * from question ORDER BY questionId ASC")
    LiveData<List<QuestionEntity>> getAllQuestions();

    @Query("SELECT * FROM question  WHERE questionId = :questionId")
    QuestionEntity getQuestionById(int questionId);

    @Query("SELECT * FROM question ORDER BY questionId ASC")
    List<QuestionEntity> getAllQuestionsAsList();
}
