package be.kul.app.room.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import be.kul.app.callback.AllUsersCallback;
import be.kul.app.callback.UserCallback;
import be.kul.app.callback.UserDeleteCallback;
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

    public void getAllUsersAsList(final AllUsersCallback allUsersCallback){
        new UserEntityRepository.getAllUsersAsListAsyncTask(mUserEntityDAO, new AllUsersCallback() {
            @Override
            public void onSuccess(List<UserEntity> users) {
                allUsersCallback.onSuccess(users);
            }
        }).execute();
    }

    public void getUserByName(String username, final UserCallback userCallback){
        new UserEntityRepository.getUserByNameAsyncTask(mUserEntityDAO, new UserCallback() {
            @Override
            public void onSuccess(UserEntity userEntity) {
                userCallback.onSuccess(userEntity);
            }
        }).execute(username);
    }

    public void deleteAllUsers(final UserDeleteCallback userDeleteCallback){
        new UserEntityRepository.deleteAllUsersAsyncTask(mUserEntityDAO, new UserDeleteCallback() {
            @Override
            public void onSuccess() {
                userDeleteCallback.onSuccess();
            }
        }).execute();
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

    private static class getUserByNameAsyncTask extends AsyncTask<String, Void, Void>{
        private UserEntityDAO mAsyncTaskDao;
        private UserCallback userCallback;

        getUserByNameAsyncTask(UserEntityDAO dao, UserCallback userCallback) {
            mAsyncTaskDao = dao;
            this.userCallback = userCallback;
        }

        @Override
        protected Void doInBackground(final String... params) {
            UserEntity userEntity = mAsyncTaskDao.getUserByName(params[0]);
            userCallback.onSuccess(userEntity);
            return null;
        }
    }

    private static class getAllUsersAsListAsyncTask extends AsyncTask<String, Void, Void>{
        private UserEntityDAO mAsyncTaskDao;
        private AllUsersCallback allUsersCallback;

        getAllUsersAsListAsyncTask(UserEntityDAO dao, AllUsersCallback allUsersCallback) {
            mAsyncTaskDao = dao;
            this.allUsersCallback = allUsersCallback;
        }

        @Override
        protected Void doInBackground(final String... params) {
            allUsersCallback.onSuccess(mAsyncTaskDao.getAllUsersAsList());
            return null;
        }
    }

    private static class deleteAllUsersAsyncTask extends AsyncTask<Void, Void, Void>{
        private UserEntityDAO mAsyncTaskDao;
        private UserDeleteCallback userDeleteCallback;

        deleteAllUsersAsyncTask(UserEntityDAO dao, UserDeleteCallback userDeleteCallback) {
            mAsyncTaskDao = dao;
            this.userDeleteCallback = userDeleteCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAsyncTaskDao.deleteAll();
            userDeleteCallback.onSuccess();
            return null;
        }
    }
}
