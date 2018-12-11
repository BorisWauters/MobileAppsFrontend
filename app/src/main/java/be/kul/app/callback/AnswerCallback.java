package be.kul.app.callback;

import be.kul.app.room.model.AnswerEntity;

public interface AnswerCallback {

    void onSuccess(AnswerEntity answerEntity);
}
