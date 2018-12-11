package be.kul.app.room.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import be.kul.app.callback.AnswerCallback;
import be.kul.app.callback.AnswerDeleteCallback;
import be.kul.app.room.model.AnswerEntity;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.repositories.AnswerEntityRepository;
import be.kul.app.room.repositories.QuestionEntityRepository;

import java.util.List;

public class AnswerEntityViewModel extends AndroidViewModel {

    // Room database stuff
    private AnswerEntityRepository mAnswerEntityRepository;

    private LiveData<List<AnswerEntity>> mAllAnswers;


    public AnswerEntityViewModel (Application application){
        super(application);
        mAnswerEntityRepository = new AnswerEntityRepository(application);
        mAllAnswers = mAnswerEntityRepository.getAllAnswers();
    }

    LiveData<List<AnswerEntity>> getAllUsers(){ return mAllAnswers;}

    public void insert(AnswerEntity answerEntity){ mAnswerEntityRepository.insert(answerEntity);}

    public void getAnswerById(int answerId, final AnswerCallback answerCallback){
        mAnswerEntityRepository.getAnswerById(answerId, new AnswerCallback() {
            @Override
            public void onSuccess(AnswerEntity answerEntity) {
                answerCallback.onSuccess(answerEntity);
            }
        });

    }

    public void deleteAllAnswers(final AnswerDeleteCallback answerDeleteCallback){
        mAnswerEntityRepository.deleteAllAnswers(new AnswerDeleteCallback() {
            @Override
            public void onSuccess() {
                answerDeleteCallback.onSuccess();
            }
        });
    }
}
