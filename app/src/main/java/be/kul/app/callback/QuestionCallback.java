package be.kul.app.callback;

import be.kul.app.room.model.QuestionEntity;

public interface QuestionCallback {

    void onSuccess(QuestionEntity questionEntity);
}
