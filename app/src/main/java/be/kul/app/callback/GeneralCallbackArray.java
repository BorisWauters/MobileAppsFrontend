package be.kul.app.callback;

import org.json.JSONArray;

public interface GeneralCallbackArray {

    void onSuccess(JSONArray result);

    void onFail();
}
