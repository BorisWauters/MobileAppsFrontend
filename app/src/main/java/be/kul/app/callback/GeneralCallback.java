package be.kul.app.callback;

import org.json.JSONObject;

public interface GeneralCallback {

    void onSuccess(JSONObject result);

    void onFail();
}
