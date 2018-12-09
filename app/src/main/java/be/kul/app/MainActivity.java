package be.kul.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/* Google sign in imports*/
import be.kul.app.callback.GeneralCallback;
import be.kul.app.callback.RegisterUserCallback;
import be.kul.app.dao.UserEntity;
import com.facebook.*;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/* Facebook sign in imports*/
import com.facebook.appevents.AppEventsLogger;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.Serializable;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    /* Google sign in stuff*/
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    // resource: https://github.com/googlesamples/google-services

    /* Facebook sign in stuff*/
    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private LoginButton loginButton;

    private Context mContext;
    private Activity mActivity;
    private boolean userExists = true;
    private RestController restController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Views
        mStatusTextView = findViewById(R.id.status);

        //app context
        mContext = getApplicationContext();
        restController = new RestController(mContext);
        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);



        /* Google stuff*/
        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]

        /* Facebook stuff*/
        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
        Log.d("API INFO", String.valueOf(loggedOut));
        if(!loggedOut){
            //Picasso.get().load(Profile.getCurrentProfile().getProfilePictureUri(200, 200)).into(imageView);

            Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());

            //Using Graph API
            getUserProfile(AccessToken.getCurrentAccessToken());
        }

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                boolean loggedIn = AccessToken.getCurrentAccessToken() == null;

                Log.d("Facebook login check", loggedIn + " ??");
                GraphRequest request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.d("TAG", object.toString());
                                try {
                                    /*String first_name = object.getString("first_name");
                                    String last_name = object.getString("last_name");*/
                                    String email = object.getString("email");
                                    checkIfUserExistsFacebook(email);
                                    /*String id = object.getString("id");
                                    String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                                    //txtUsername.setText("First Name: " + first_name + "\nLast Name: " + last_name);
                                    mStatusTextView.setText(email);
                                    Picasso.get().load(image_url).into(imageView);*/

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name,last_name,email,id");
                request.setParameters(parameters);
                request.executeAsync();

                //getUserProfile(AccessToken.getCurrentAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //updateUI(account);
        // [END on_start_sign_in]
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Facebook callback method*/
        callbackManager.onActivityResult(requestCode, resultCode, data);

        /* Google callback method*/
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // [END onActivityResult]

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            /*successful login, check user credentials against rest backend*/
            checkIfUserExistsGoogle(account);


            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void checkIfUserExistsFacebook(final String email){
        Log.d("INFO", "Checking facebook login");
        restController.checkIfUserExistsVolleyRequest(email, new GeneralCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    Log.d("INFO", "Successful Login on Facebook");
                    String firstName = result.getString("username");
                    int id = Integer.parseInt(result.getString("userId"));
                    mStatusTextView.setText(firstName);
                    sendToDashboard(firstName,id);
                }catch(JSONException e){

                }
            }

            @Override
            public void onFail() {
                registerNewUserFacebook(email);
            }
        });
    }

    private void sendToDashboard(String username, int id){
        Intent i = new Intent(this, Dashboard.class);
        UserEntity userEntity = new UserEntity(id, username, null);
        i.putExtra("UserEntity", userEntity);
        startActivity(i);
    }

    private void registerNewUserFacebook(String email){
        restController.registerNewUser(email, new RegisterUserCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    String firstName = result.getString("username");
                    int id = Integer.parseInt(result.getString("userId"));
                    mStatusTextView.setText(firstName);
                    sendToDashboard(firstName, id);
                }catch(JSONException e){

                }
            }
        });
    }

    private void checkIfUserExistsGoogle(final GoogleSignInAccount account){
        restController.checkIfUserExistsVolleyRequest(account.getEmail(), new GeneralCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try{
                    System.out.println("User Checked!");
                    // Get the current student (json object) data
                    String firstName = result.getString("username");
                    int id = Integer.parseInt(result.getString("userId"));
                    mStatusTextView.setText(firstName);
                    // Display the formatted json data in text view
                    //mTextView.append(firstName +" " + lastName +"\nage : " + age);
                    //mTextView.append("\n\n");
                    //updateUI(account);
                    sendToDashboard(firstName, id);

                }catch (JSONException e){
                    System.out.println("Error unmarshalling parameters");
                }
            }

            @Override
            public void onFail() {
                registerNewUserGoogle(account);
            }
        });

    }

    private void registerNewUserGoogle(final GoogleSignInAccount account){
        restController.registerNewUser(account.getEmail(), new RegisterUserCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try{
                    // Get the current student (json object) data
                    String firstName = result.getString("username");
                    int id = Integer.parseInt(result.getString("userId"));
                    mStatusTextView.setText(firstName + " " + id);
                    // Display the formatted json data in text view
                    //mTextView.append(firstName +" " + lastName +"\nage : " + age);
                    //mTextView.append("\n\n");
                    //updateUI(account);
                    sendToDashboard(firstName, id);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("TAG", object.toString());
                        try {
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String email = object.getString("email");
                            String id = object.getString("id");
                            String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                            //txtUsername.setText("First Name: " + first_name + "\nLast Name: " + last_name);
                            mStatusTextView.setText(email);
                            //Picasso.get().load(image_url).into(imageView);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

}
