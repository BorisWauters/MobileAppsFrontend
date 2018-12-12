package be.kul.app.callback;

import be.kul.app.room.model.AnswerEntity;

import java.util.List;

public interface AllAnswersCallback {

    void onSuccess(List<AnswerEntity> answers);
}
