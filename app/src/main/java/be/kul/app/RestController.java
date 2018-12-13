package be.kul.app;

import android.content.Context;
import android.util.Log;
import be.kul.app.callback.GeneralCallback;
import be.kul.app.callback.GeneralCallbackArray;
import be.kul.app.callback.RegisterUserCallback;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RestController {
    private Context mContext;
    private RequestQueue requestQueue;
    private final String serverIp = "http://10.110.146.46:8080";


    public RestController(Context context) {
        this.mContext = context;
        //initialize request queue instance
        requestQueue = Volley.newRequestQueue(mContext);
    }

    public void checkIfUserExistsEmailOnly(String email, final GeneralCallback generalCallback) {


        //https://android--examples.blogspot.com/2017/02/android-volley-json-object-request.html
        //initialize JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                serverIp + "/user/name/" + email,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        generalCallback.onSuccess(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        generalCallback.onFail();
                    }
                }
        );
        //Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);

    }

    public void checkIfUserExistsEmailAndPassword(String email, String password, final GeneralCallback generalCallback) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            // Change this to UTF-16 if needed
            md.update(password.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            password = String.format("%064x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        JSONObject request = new JSONObject();
        try {
            request.put("username", email);
            request.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("INFO:REQUEST", request.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                serverIp + "/user/name",
                request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        generalCallback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        generalCallback.onFail();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    public void registerNewUser(String email, final RegisterUserCallback registerUserCallback) {

        JSONObject request = new JSONObject();
        try {
            request.put("username", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //https://android--examples.blogspot.com/2017/02/android-volley-json-object-request.html
        //initialize JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                serverIp + "/user",
                request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        registerUserCallback.onSuccess(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        System.out.println("Error registering user");
                    }
                }
        );
        //Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);

    }

    public void registerNewUserEmailAndPassword(String email, String password, final GeneralCallback generalCallback) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            // Change this to UTF-16 if needed
            md.update(password.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            password = String.format("%064x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {

        }

        Log.d("INFO: REG", password);
        JSONObject request = new JSONObject();
        try {
            request.put("username", email);
            request.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //https://android--examples.blogspot.com/2017/02/android-volley-json-object-request.html
        //initialize JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                serverIp + "/user",
                request,
                new Response.Listener<JSONObject>() {

                    String password;

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            password = response.getString("password");
                        } catch (JSONException e) {

                        }
                        Log.d("INFO after REG", password);
                        generalCallback.onSuccess(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        System.out.println("Error registering user");
                    }
                }
        );
        //Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void addNewQuestion(String title, String description, UserEntity userEntity, final GeneralCallback generalCallback) {
        JSONObject request = new JSONObject();
        try {
            request.put("questionTitle", title);
            request.put("questionDescription", description);
            JSONObject userEntityJson = new JSONObject();
            userEntityJson.put("userId", userEntity.getUserId());
            userEntityJson.put("username", userEntity.getUsername());
            userEntityJson.put("password", userEntity.getPassword());
            request.put("userEntity", userEntityJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //https://android--examples.blogspot.com/2017/02/android-volley-json-object-request.html
        //initialize JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                serverIp + "/question",
                request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        generalCallback.onSuccess(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        System.out.println("Error adding question user");
                    }
                }
        );
        //Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void requestQuestions(final GeneralCallbackArray generalCallbackArray) {
        //initialize JsonObjectRequest
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET,
                serverIp + "/question",
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        generalCallbackArray.onSuccess(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        System.out.println("Error adding question user");
                    }
                }
        );
        //Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void requestAnswers(int questionId, final GeneralCallbackArray generalCallbackArray) {
        //initialize JsonObjectRequest
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET,
                serverIp + "/answer/question/" + String.valueOf(questionId),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        generalCallbackArray.onSuccess(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        System.out.println("Error adding question user");
                    }
                }
        );
        //Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void submitAnswer(String answer, QuestionEntity questionEntity, UserEntity userEntity, final GeneralCallback generalCallback) {
        JSONObject request = new JSONObject();
        try {
            request.put("answerDescription", answer);

            // create the question JSON and add parameters
            JSONObject questionObject = new JSONObject();
            questionObject.put("questionId", questionEntity.getQuestionId());

            // create user JSON for the question
            /*JSONObject questionUserObject = new JSONObject();
            questionUserObject.put("userId", questionEntity.getUserId());
            questionObject.put("userEntity", questionUserObject);*/

            // create the user JSON and add parameters
            JSONObject userObject = new JSONObject();
            userObject.put("userId", userEntity.getUserId());

            // add both JSON objects to the main object
            request.put("questionEntity", questionObject);
            request.put("userEntity", userObject);
            System.out.println(request.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //https://android--examples.blogspot.com/2017/02/android-volley-json-object-request.html
        //initialize JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                serverIp + "/answer",
                request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        generalCallback.onSuccess(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        System.out.println("Error adding question user");
                    }
                }
        );
        //Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }
}
