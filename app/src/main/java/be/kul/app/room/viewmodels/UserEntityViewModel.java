package be.kul.app.room.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import be.kul.app.callback.AllUsersCallback;
import be.kul.app.callback.UserCallback;
import be.kul.app.callback.UserDeleteCallback;
import be.kul.app.room.model.AnswerEntity;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;
import be.kul.app.room.repositories.AnswerEntityRepository;
import be.kul.app.room.repositories.QuestionEntityRepository;
import be.kul.app.room.repositories.UserEntityRepository;

import java.util.List;

public class UserEntityViewModel extends AndroidViewModel {

    // Room database stuff
    private UserEntityRepository mUserEntityRepository;

    private LiveData<List<UserEntity>> mAllUsers;


    public UserEntityViewModel (Application application){
        super(application);
        mUserEntityRepository = new UserEntityRepository(application);
        mAllUsers = mUserEntityRepository.getAllUsers();
    }

    LiveData<List<UserEntity>> getAllUsers(){ return mAllUsers;}

    public void insert(UserEntity userEntity){ mUserEntityRepository.insert(userEntity);}

    public void getUserById(int userId, final UserCallback userCallback){
        mUserEntityRepository.getUserById(userId, new UserCallback() {
            @Override
            public void onSuccess(UserEntity userEntity) {
                userCallback.onSuccess(userEntity);
            }
        });
    }

    public void getUserByName(String username, final UserCallback userCallback){
        mUserEntityRepository.getUserByName(username, new UserCallback() {
            @Override
            public void onSuccess(UserEntity userEntity) {
                userCallback.onSuccess(userEntity);
            }
        });
    }

    public void getAllUsersAsList(final AllUsersCallback allUsersCallback){
        mUserEntityRepository.getAllUsersAsList(new AllUsersCallback() {
            @Override
            public void onSuccess(List<UserEntity> users) {
                allUsersCallback.onSuccess(users);
            }
        });
    }

    public void deleteAllUsers(final UserDeleteCallback userDeleteCallback){
        mUserEntityRepository.deleteAllUsers(new UserDeleteCallback() {
            @Override
            public void onSuccess() {
                userDeleteCallback.onSuccess();
            }
        });
    }
}
