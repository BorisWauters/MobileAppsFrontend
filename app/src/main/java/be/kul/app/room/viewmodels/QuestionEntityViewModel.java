package be.kul.app.room.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import be.kul.app.callback.AllQuestionsCallback;
import be.kul.app.callback.QuestionCallback;
import be.kul.app.callback.QuestionDeleteCallback;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;
import be.kul.app.room.repositories.QuestionEntityRepository;
import be.kul.app.room.repositories.UserEntityRepository;

import java.util.ArrayList;
import java.util.List;

public class QuestionEntityViewModel extends AndroidViewModel {

    // Room database stuff
    private QuestionEntityRepository mQuestionEntityRepository;

    private LiveData<List<QuestionEntity>> mAllQuestions;


    public QuestionEntityViewModel (Application application){
        super(application);
        mQuestionEntityRepository = new QuestionEntityRepository(application);
        mAllQuestions = mQuestionEntityRepository.getAllQuestions();
    }

    public LiveData<List<QuestionEntity>> getAllQuestions(){ return mAllQuestions;}

    public void insert(QuestionEntity questionEntity){ mQuestionEntityRepository.insert(questionEntity);}

    public void getQuestionById(int questionId, final QuestionCallback questionCallback){
        mQuestionEntityRepository.getQuestionById(questionId, new QuestionCallback() {
            @Override
            public void onSuccess(QuestionEntity questionEntity) {
                questionCallback.onSuccess(questionEntity);
            }
        });
    }

    public void deleteAllQuestions(final QuestionDeleteCallback questionDeleteCallback){
        mQuestionEntityRepository.deleteAllQuestions(new QuestionDeleteCallback() {
            @Override
            public void onSuccess() {
                questionDeleteCallback.onSuccess();
            }
        });
    }

    public void getAllQuestionsAsList(final AllQuestionsCallback allQuestionsCallback){
        mQuestionEntityRepository.getAllQuestionsAsList(new AllQuestionsCallback() {
            @Override
            public void onSuccess(List<QuestionEntity> questions) {
                allQuestionsCallback.onSuccess(questions);
            }
        });
    }
}
