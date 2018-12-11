package be.kul.app.room.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import be.kul.app.callback.QuestionCallback;
import be.kul.app.callback.UserCallback;
import be.kul.app.room.dao.UserEntityDAO;
import be.kul.app.room.database.RoomDatabase;
import be.kul.app.room.model.UserEntity;

import java.util.List;

public class UserEntityRepository {

    private UserEntityDAO mUserEntityDAO;
    private LiveData<List<UserEntity>> mAllUsers;

    public UserEntityRepository(Application application){
        RoomDatabase db = RoomDatabase.getDatabase(application);
        mUserEntityDAO = db.userEntityDAO();
        mAllUsers = mUserEntityDAO.getAllUsers();
    }

    public LiveData<List<UserEntity>> getAllUsers() {
        return mAllUsers;
    }

    public void insert (UserEntity userEntity) {
        new insertAsyncTask(mUserEntityDAO).execute(userEntity);
    }

    public void getUserById(int userId, final UserCallback userCallback){
        new UserEntityRepository.getUserByIdAsyncTask(mUserEntityDAO, new UserCallback() {
            @Override
            public void onSuccess(UserEntity userEntity) {
                userCallback.onSuccess(userEntity);
            }
        });
    }

    private static class insertAsyncTask extends AsyncTask<UserEntity, Void, Void> {

        private UserEntityDAO mAsyncTaskDao;

        insertAsyncTask(UserEntityDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final UserEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class getUserByIdAsyncTask extends AsyncTask<Integer, Void, Void>{
        private UserEntityDAO mAsyncTaskDao;
        private UserCallback userCallback;

        getUserByIdAsyncTask(UserEntityDAO dao, UserCallback userCallback) {
            mAsyncTaskDao = dao;
            this.userCallback = userCallback;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            UserEntity userEntity = mAsyncTaskDao.getUserById(params[0]);
            userCallback.onSuccess(userEntity);
            return null;
        }
    }
}
