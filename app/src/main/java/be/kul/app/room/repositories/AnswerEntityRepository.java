package be.kul.app.room.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.os.AsyncTask;
import be.kul.app.callback.AllAnswersCallback;
import be.kul.app.callback.AnswerCallback;
import be.kul.app.callback.AnswerDeleteCallback;
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

    public void deleteAllAnswers(final AnswerDeleteCallback answerDeleteCallback){
        new AnswerEntityRepository.deleteAllAnswersAsyncTask(mAnswerEntityDAO, new AnswerDeleteCallback() {
            @Override
            public void onSuccess() {
                answerDeleteCallback.onSuccess();
            }
        }).execute();
    }

    public void getAllAnswersAsList(final AllAnswersCallback allAnswersCallback){
        new AnswerEntityRepository.getAllAnswersAsListAsyncTask(mAnswerEntityDAO, new AllAnswersCallback() {
            @Override
            public void onSuccess(List<AnswerEntity> answers) {
                allAnswersCallback.onSuccess(answers);
            }
        }).execute();
    }

    private static class insertAsyncTask extends AsyncTask<AnswerEntity, Void, Void> {

        private AnswerEntityDAO mAsyncTaskDao;

        insertAsyncTask(AnswerEntityDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final AnswerEntity... params) {
            AnswerEntity answerEntity1 = mAsyncTaskDao.getAnswerById(params[0].getAnswerId());
            if(answerEntity1 != null){
                mAsyncTaskDao.deleteById(params[0].getAnswerId());
                mAsyncTaskDao.insert(params[0]);
            }else
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

    private static class deleteAllAnswersAsyncTask extends AsyncTask<Void, Void, Void> {

        private AnswerEntityDAO mAsyncTaskDao;
        private AnswerDeleteCallback answerDeleteCallback;

        deleteAllAnswersAsyncTask(AnswerEntityDAO dao, final AnswerDeleteCallback answerDeleteCallback) {
            mAsyncTaskDao = dao;
            this.answerDeleteCallback = answerDeleteCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAsyncTaskDao.deleteAll();
            answerDeleteCallback.onSuccess();
            return null;
        }
    }

    private static class getAllAnswersAsListAsyncTask extends AsyncTask<Void, Void, Void> {

        private AnswerEntityDAO mAsyncTaskDao;
        private AllAnswersCallback allAnswersCallback;

        getAllAnswersAsListAsyncTask(AnswerEntityDAO dao, final AllAnswersCallback allAnswersCallback) {
            mAsyncTaskDao = dao;
            this.allAnswersCallback = allAnswersCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {

            allAnswersCallback.onSuccess( mAsyncTaskDao.getAllAnswersAsList());
            return null;
        }
    }
}
