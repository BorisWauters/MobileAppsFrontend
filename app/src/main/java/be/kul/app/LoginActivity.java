package be.kul.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import be.kul.app.callback.AllUsersCallback;
import be.kul.app.callback.GeneralCallback;
import be.kul.app.callback.UserCallback;
import be.kul.app.room.model.UserEntity;
import be.kul.app.room.repositories.UserEntityRepository;
import be.kul.app.room.viewmodels.UserEntityViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private UserLoginTaskWithoutInternet mAuthTaskWithoutInternet = null;

    private FirebaseAuth mAuth;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private RestController restController;

    private boolean networkConnection;

    private UserEntityViewModel mUserEntityViewModel;

    private List<UserEntity> currentUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        networkConnection = isOnline();

        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mUserEntityViewModel = ViewModelProviders.of(this).get(UserEntityViewModel.class);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // get all current users
        mUserEntityViewModel.getAllUsersAsList(new AllUsersCallback() {
            @Override
            public void onSuccess(List<UserEntity> users) {
                updateUsers(users);
            }
        });

        Button mEmailSignInButton =  findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                attemptLogin();

            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();
    }




    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if(networkConnection){

                //TODO: get all users, check if this one is already in the room db, if not get size, use as id and add user
                boolean userExistsInRoom = false;
                for(UserEntity userEntity : currentUsers){
                    if(userEntity.getUsername().equals(email))
                        userExistsInRoom = true;
                }

                mAuthTask = new UserLoginTask(email, password, userExistsInRoom);
                mAuthTask.execute((Void) null);
            }else{
                mAuthTaskWithoutInternet = new UserLoginTaskWithoutInternet(email, password);
                mAuthTaskWithoutInternet.execute();
            }

        }
    }

    public void updateUsers(List<UserEntity> users){
        currentUsers = users;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final boolean mUserExistsInRoom;

        UserLoginTask(String email, String password, boolean userExistsInRoom) {
            mEmail = email;
            mPassword = password;
            mUserExistsInRoom = userExistsInRoom;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            restController = new RestController(LoginActivity.this);
            restController.checkIfUserExistsEmailOnly(mEmail, new GeneralCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    if(!mUserExistsInRoom){
                        String username = "";
                        String password = "";
                        int id = 0;
                        try{
                            username = result.getString("username");
                            password = result.getString("password");
                            id = result.getInt("userId");

                        }catch(JSONException e){

                        }
                        Log.d("INFO", "inserting user into room");
                        UserEntity userEntity = new UserEntity(id, username, password);
                        mUserEntityViewModel.insert(userEntity);
                    }

                    checkOnEmailAndPassWord(result);
                }

                @Override
                public void onFail() {
                    restController.registerNewUserEmailAndPassword(mEmail, mPassword, new GeneralCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            String username = null;
                            String password = "";
                            int id = 0;
                            try{
                                username = result.getString("username");
                                password = result.getString("password");
                                id = result.getInt("userId");
                            }catch(JSONException e){

                            }

                            UserEntity userEntity = new UserEntity(id,username,password);
                            mUserEntityViewModel.insert(userEntity);
                            registerUserOnFireBase(username);
                        }

                        @Override
                        public void onFail() {
                            Toast.makeText(LoginActivity.this, "Something went wrong, try again later",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });





            return true;
        }

        private void checkOnEmailAndPassWord(JSONObject result){
            restController = new RestController(LoginActivity.this);
            restController.checkIfUserExistsEmailAndPassword(mEmail, mPassword, new GeneralCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    String username = null;
                    int userId = 0;
                    try{
                        username = result.getString("username");
                        userId =  result.getInt("userId");
                    }catch(JSONException e){

                    }
                    signInOnFireBase(username);
                    sendToDashboard(username, userId);
                }

                @Override
                public void onFail() {
                    Toast.makeText(LoginActivity.this, "Credentials incorrect",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        private void sendToDashboard(String username, int id) {
            //FirebaseUser user = mAuth.getCurrentUser();
            //if(user.isEmailVerified()){
                Log.d("INFO", "FireBase Email VERIFIED");
                Intent i = new Intent(LoginActivity.this, Dashboard.class);
                UserEntity userEntity = new UserEntity(id, username, null);
                i.putExtra("UserEntity", userEntity);
                startActivity(i);
            /*}else{
                Toast.makeText(LoginActivity.this, "Please verify your email!",
                        Toast.LENGTH_LONG).show();
            }*/

        }

        private void signInOnFireBase(String email) {

            mAuth.signInWithEmailAndPassword(email, email)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("INFO", "signInWithEmail:success");


                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("INFO", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });


        }
        private void registerUserOnFireBase(String email) {
            mAuth.createUserWithEmailAndPassword(email, email)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                System.out.println("TEST");
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("INFO", "createUserWithEmail:success");
                                final FirebaseUser user = mAuth.getCurrentUser();
                                user.sendEmailVerification()
                                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                if (task.isSuccessful()) {
                                                    Log.e("INFO", "sendEmailVerification Successful");
                                                    Toast.makeText(LoginActivity.this,
                                                            "Verification email sent to " + user.getEmail(),
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.e("INFO", "sendEmailVerification", task.getException());
                                                    Toast.makeText(LoginActivity.this,
                                                            "Failed to send verification email.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("INFO", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class UserLoginTaskWithoutInternet extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTaskWithoutInternet(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            // check user against room database //todo add flag which contains if the email is verified or not to the DB
            mUserEntityViewModel.getUserByName(mEmail, new UserCallback() {
                @Override
                public void onSuccess(UserEntity userEntity) {
                    //first hash the password for matching purposes
                    String password = "";
                    try{
                        MessageDigest md = MessageDigest.getInstance("SHA");
                        // Change this to UTF-16 if needed
                        md.update( mPassword.getBytes( StandardCharsets.UTF_8 ) );
                        byte[] digest = md.digest();
                        password= String.format( "%064x", new BigInteger( 1, digest ) );
                    }catch(NoSuchAlgorithmException e){

                    }
                    if(userEntity != null){
                        if(userEntity.getPassword().equals(password))
                            sendToDashboard(userEntity.getUsername(), userEntity.getUserId());
                        else{
                            Toast.makeText(LoginActivity.this, "Credentials incorrect",
                                    Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(LoginActivity.this, "Connect to internet!",
                                Toast.LENGTH_LONG).show();
                    }

                }
            });
            return true;
        }


        private void sendToDashboard(String username, int id) {

            // if statement which checks the email verified field in the db
                Log.d("INFO", "FireBase Email VERIFIED");
                Intent i = new Intent(LoginActivity.this, Dashboard.class);
                UserEntity userEntity = new UserEntity(id, username, null);
                i.putExtra("UserEntity", userEntity);
                startActivity(i);
            /*}else{
                Toast.makeText(LoginActivity.this, "Please verify your email!",
                        Toast.LENGTH_LONG).show();
            }*/

        }




        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

