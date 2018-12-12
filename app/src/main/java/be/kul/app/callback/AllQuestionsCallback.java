package be.kul.app.callback;

import be.kul.app.room.model.QuestionEntity;

import java.util.ArrayList;
import java.util.List;

public interface AllQuestionsCallback {

    void onSuccess(List<QuestionEntity> questions);
}
