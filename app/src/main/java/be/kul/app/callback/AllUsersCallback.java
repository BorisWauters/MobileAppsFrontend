package be.kul.app.callback;

import be.kul.app.room.model.UserEntity;

import java.util.List;

public interface AllUsersCallback {

    void onSuccess(List<UserEntity> users);
}
