package be.kul.app.callback;

import be.kul.app.room.model.UserEntity;

public interface UserCallback {

    void onSuccess(UserEntity userEntity);
}
