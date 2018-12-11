package be.kul.app.room.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import be.kul.app.Question;
import be.kul.app.callback.GeneralCallback;
import be.kul.app.callback.QuestionCallback;
import be.kul.app.callback.QuestionDeleteCallback;
import be.kul.app.room.dao.QuestionEntityDAO;
import be.kul.app.room.dao.UserEntityDAO;
import be.kul.app.room.database.RoomDatabase;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;
import org.json.JSONObject;

import java.util.List;

public class QuestionEntityRepository {

    private QuestionEntityDAO mQuestionEntityDAO;
    private LiveData<List<QuestionEntity>> mAllQuestions;

    public QuestionEntityRepository(Application application){
        RoomDatabase db = RoomDatabase.getDatabase(application);
        mQuestionEntityDAO = db.questionEntityDAO();
        mAllQuestions = mQuestionEntityDAO.getAllQuestions();
    }

    public LiveData<List<QuestionEntity>> getAllQuestions() {
        return mAllQuestions;
    }

    public void insert (QuestionEntity questionEntity) {
        new QuestionEntityRepository.insertAsyncTask(mQuestionEntityDAO).execute(questionEntity);
    }

    public void getQuestionById(int questionId, final QuestionCallback questionCallback){

        new QuestionEntityRepository.getQuestionByIdAsyncTask(mQuestionEntityDAO, new QuestionCallback() {
            @Override
            public void onSuccess(QuestionEntity questionEntity) {
              questionCallback.onSuccess(questionEntity);
            }
        }).execute(questionId);
    }

    public void deleteAllQuestions(final QuestionDeleteCallback questionDeleteCallback){
        new QuestionEntityRepository.deleteAllQuestionsAsyncTask(mQuestionEntityDAO, new QuestionDeleteCallback() {
            @Override
            public void onSuccess() {

            }
        }).execute();
    }

    private static class insertAsyncTask extends AsyncTask<QuestionEntity, Void, Void> {

        private QuestionEntityDAO mAsyncTaskDao;

        insertAsyncTask(QuestionEntityDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final QuestionEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class getQuestionByIdAsyncTask extends AsyncTask<Integer, Void, Void>{
        private QuestionEntityDAO mAsyncTaskDao;
        private  QuestionCallback questionCallback;

        getQuestionByIdAsyncTask(QuestionEntityDAO dao, QuestionCallback questionCallback){
            mAsyncTaskDao = dao;
            this.questionCallback = questionCallback;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            QuestionEntity questionEntity = mAsyncTaskDao.getQuestionById(params[0]);
            questionCallback.onSuccess(questionEntity);
            return null;
        }

    }

    private static class deleteAllQuestionsAsyncTask extends AsyncTask<Void, Void, Void>{
        private QuestionEntityDAO mAsyncTaskDao;
        private  QuestionDeleteCallback questionDeleteCallback;

        deleteAllQuestionsAsyncTask(QuestionEntityDAO dao, QuestionDeleteCallback questionDeleteCallback){
            mAsyncTaskDao = dao;
            this.questionDeleteCallback= questionDeleteCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAsyncTaskDao.deleteAll();
            questionDeleteCallback.onSuccess();
            return null;
        }

    }
}
