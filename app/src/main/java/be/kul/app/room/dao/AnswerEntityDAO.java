package be.kul.app.room.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import be.kul.app.room.model.AnswerEntity;
import be.kul.app.room.model.QuestionEntity;

import java.util.List;
@Dao
public interface AnswerEntityDAO {

    @Insert
    void insert(AnswerEntity answerEntity);

    @Query("DELETE FROM answer")
    void deleteAll();

    @Query("SELECT * from answer ORDER BY answerId ASC")
    LiveData<List<AnswerEntity>> getAllAnswers();

    @Query("SELECT * FROM answer WHERE answerId = :answerId")
    AnswerEntity getAnswerById(int answerId);
}
