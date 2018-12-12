package be.kul.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/* Google sign in imports*/
import android.widget.Toast;
import be.kul.app.callback.GeneralCallback;
import be.kul.app.callback.RegisterUserCallback;
import be.kul.app.room.model.UserEntity;
import com.facebook.*;
import com.facebook.login.LoginManager;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /* Google sign in stuff*/
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;
    // resource: https://github.com/googlesamples/google-services

    /* Facebook sign in stuff*/
    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private LoginButton loginButton;

    private Button loginAndEmailButton;

    private Context mContext;
    private Activity mActivity;
    private boolean userExists = true;
    private RestController restController;

    private boolean networkConnected = false;
    private boolean isReceiverRegistered = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = getNetworkInfo(context);
            if (info != null && info.isConnected()) {
                // Code to execute if wifi connected
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.login_button).setVisibility(View.VISIBLE);
            } else {
                // Code to execute if wifi disconnected
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.login_button).setVisibility(View.GONE);
            }
        }
    };

    private NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* FireBase */
        mAuth = FirebaseAuth.getInstance();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // check if there is an internet connection available
        networkConnected = isOnline();

        //app context
        mContext = getApplicationContext();
        restController = new RestController(mContext);
        loginAndEmailButton = findViewById(R.id.login_buttonEmail);

        loginAndEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendToLogin(v);

            }
        });


        /* Google stuff*/
        findViewById(R.id.sign_in_button).setOnClickListener(this);


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        /* Facebook stuff*/
        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
        Log.d("API INFO", String.valueOf(loggedOut));
        if (!loggedOut) {
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
            public void onSuccess(final LoginResult loginResult) {
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
                                    checkIfUserExistsFacebook(email, AccessToken.getCurrentAccessToken());
                                    /*String id = object.getString("id");
                                    String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                                    //txtUsername.setText("First Name: " + first_name + "\nLast Name: " + last_name);
                                    mStatusTextView.setText(email);
                                    Picasso.get().load(image_url).into(imageView);*/

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );

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

        // if not internet connection, disable google and facebook login
        if(!networkConnected){
            findViewById(R.id.login_button).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        }


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

    @Override
    protected void onResume() {
        super.onResume();
        if (!isReceiverRegistered) {
            isReceiverRegistered = true;
            registerReceiver(receiver, new IntentFilter("android.net.wifi.STATE_CHANGE")); // IntentFilter to wifi state change is "android.net.wifi.STATE_CHANGE"
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isReceiverRegistered) {
            isReceiverRegistered = false;
            unregisterReceiver(receiver);
        }
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

    private void checkIfUserExistsFacebook(final String email, final AccessToken accessToken) {
        Log.d("INFO", "Checking facebook login");
        restController.checkIfUserExistsEmailOnly(email, new GeneralCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    Log.d("INFO", "Successful Login on Facebook");
                    String firstName = result.getString("username");
                    int id = Integer.parseInt(result.getString("userId"));

                    signInOnFireBase(firstName);
                    sendToDashboard(firstName, id);
                } catch (JSONException e) {

                }
            }

            @Override
            public void onFail() {
                registerUserOnFirebase(email);
                registerNewUserFacebook(email);
            }
        });
    }

    private void sendToDashboard(String username, int id) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user.isEmailVerified()) {
            Intent i = new Intent(this, Dashboard.class);
            UserEntity userEntity = new UserEntity(id, username, null);
            i.putExtra("UserEntity", userEntity);
            startActivity(i);
        } else {
            signOut();
            LoginManager.getInstance().logOut();
            Toast.makeText(MainActivity.this, "Please verify your email!",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void sendToLogin(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void registerNewUserFacebook(String email) {
        restController.registerNewUser(email, new RegisterUserCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    String firstName = result.getString("username");
                    int id = Integer.parseInt(result.getString("userId"));

                    sendToDashboard(firstName, id);
                } catch (JSONException e) {

                }
            }
        });
    }

    private void checkIfUserExistsGoogle(final GoogleSignInAccount account) {
        restController.checkIfUserExistsEmailOnly(account.getEmail(), new GeneralCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    System.out.println("User Checked!");
                    // Get the current student (json object) data
                    String firstName = result.getString("username");
                    int id = Integer.parseInt(result.getString("userId"));

                    // Display the formatted json data in text view
                    //mTextView.append(firstName +" " + lastName +"\nage : " + age);
                    //mTextView.append("\n\n");
                    //updateUI(account);
                    Log.d("INFO", "Signing into firebase");
                    signInOnFireBase(firstName);
                    sendToDashboard(firstName, id);

                } catch (JSONException e) {
                    System.out.println("Error unmarshalling parameters");
                }
            }

            @Override
            public void onFail() {
                registerUserOnFirebase(account.getEmail());
                registerNewUserGoogle(account);
            }
        });

    }

    private void registerNewUserGoogle(final GoogleSignInAccount account) {
        restController.registerNewUser(account.getEmail(), new RegisterUserCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    // Get the current student (json object) data
                    String firstName = result.getString("username");
                    int id = Integer.parseInt(result.getString("userId"));

                    // Display the formatted json data in text view
                    //mTextView.append(firstName +" " + lastName +"\nage : " + age);
                    //mTextView.append("\n\n");
                    //updateUI(account);
                    sendToDashboard(firstName, id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void signInOnFireBase(String email) {

        final boolean res;
        mAuth.signInWithEmailAndPassword(email, email)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });


    }

    private void registerUserOnFirebase(String email) {
        mAuth.createUserWithEmailAndPassword(email, email)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this,
                                                        "Verification email sent to " + user.getEmail(),
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e(TAG, "sendEmailVerification", task.getException());
                                                Toast.makeText(MainActivity.this,
                                                        "Failed to send verification email.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);

        } else {
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
        if (v.getId() == R.id.sign_in_button) {
            signIn();
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


}
