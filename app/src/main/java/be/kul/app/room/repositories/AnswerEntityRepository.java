package be.kul.app.room.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.os.AsyncTask;
import be.kul.app.callback.AnswerCallback;
import be.kul.app.room.dao.AnswerEntityDAO;
import be.kul.app.room.dao.QuestionEntityDAO;
import be.kul.app.room.database.RoomDatabase;
import be.kul.app.room.model.AnswerEntity;
import be.kul.app.room.model.QuestionEntity;

import java.util.List;

public class AnswerEntityRepository {

    private AnswerEntityDAO mAnswerEntityDAO;
    private LiveData<List<AnswerEntity>> mAllAnswers;

    public AnswerEntityRepository(Application application){
        RoomDatabase db = RoomDatabase.getDatabase(application);
        mAnswerEntityDAO = db.answerEntityDAO();
        mAllAnswers = mAnswerEntityDAO.getAllAnswers();
    }

    public LiveData<List<AnswerEntity>> getAllAnswers() {
        return mAllAnswers;
    }

    public void insert (AnswerEntity answerEntity) {
        new AnswerEntityRepository.insertAsyncTask(mAnswerEntityDAO).execute(answerEntity);
    }

    public void getAnswerById(int answerId, final AnswerCallback answerCallback){
        new AnswerEntityRepository.getAnswerByIdAsyncTask(mAnswerEntityDAO, new AnswerCallback() {
            @Override
            public void onSuccess(AnswerEntity answerEntity) {
                answerCallback.onSuccess(answerEntity);
            }
        }).execute(answerId);
    }

    private static class insertAsyncTask extends AsyncTask<AnswerEntity, Void, Void> {

        private AnswerEntityDAO mAsyncTaskDao;

        insertAsyncTask(AnswerEntityDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final AnswerEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class getAnswerByIdAsyncTask extends AsyncTask<Integer, Void, Void> {

        private AnswerEntityDAO mAsyncTaskDao;
        private AnswerCallback answerCallback;

        getAnswerByIdAsyncTask(AnswerEntityDAO dao, final AnswerCallback answerCallback) {
            mAsyncTaskDao = dao;
            this.answerCallback = answerCallback;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            AnswerEntity answerEntity = mAsyncTaskDao.getAnswerById(params[0]);
            answerCallback.onSuccess(answerEntity);
            return null;
        }
    }
}
