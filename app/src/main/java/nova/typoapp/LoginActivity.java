package nova.typoapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;



    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    TextInputLayout textLoginEmail, textLoginPassword;


    private CallbackManager callbackManager;

    private List<String> permissionNeeds = Arrays.asList("email");
    Button buttonFacebook;

    String email, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_login);

        buttonFacebook = (Button)findViewById(R.id.buttonFacebookLogin);
        setLoginButton();

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult result) {

                GraphRequest request;
                request = GraphRequest.newMeRequest(result.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject userObject, GraphResponse response) {


                        if (response.getError() != null) {

                        } else {
                            Log.i("TAG", "user: " + userObject.toString());
                            Log.i("TAG", "AccessToken: " + result.getAccessToken().getToken());

                            try {
                                email = userObject.getString("email");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(LoginActivity.this, "페이스북 계정 "+email+" 으로 로그인하였습니다.", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);

                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);

                            finish();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                Log.d("Tag", "로그인 하려다 맘");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Tag", "에러 : " + error.getLocalizedMessage());
            }
        });





        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.loginEmail);
        populateAutoComplete();
        mLoginFormView = findViewById(R.id.login_form);
        mPasswordView = (EditText) findViewById(R.id.loginPassword);
        Button mEmailSignInButton = (Button) findViewById(R.id.buttonLogin);
        mProgressView = findViewById(R.id.login_progress);

        textLoginEmail = (TextInputLayout)findViewById(R.id.textLoginEmail);
        textLoginPassword = (TextInputLayout)findViewById(R.id.textLoginPassword);



        mPasswordView.setOnFocusChangeListener(focusChangeListener);
        mEmailView.setOnFocusChangeListener(focusChangeListener);



        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });





        ///////////////////////////////// 나의 고유 코드



        findViewById(R.id.buttonLogin).setOnClickListener(mClickListener);
        findViewById(R.id.buttonJoin).setOnClickListener(mClickListener);


    }

    private void setLoginButton() {
        if (AccessToken.getCurrentAccessToken() != null) {
            buttonFacebook.setText("로그아웃");
            buttonFacebook.setOnClickListener(new View.OnClickListener() {
                @Override


                public void onClick(View view) {

                    Toast.makeText(LoginActivity.this, "로그아웃 하셨습니다.", Toast.LENGTH_SHORT).show();

                    LoginManager.getInstance().logOut();
                    setLoginButton();
                }
            });
        } else {
            buttonFacebook.setText("페이스북으로 로그인");
            buttonFacebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, permissionNeeds);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {

//   buttonLogin은 위에서 이미 리스너가 지정되있다.
                case R.id.buttonLogin:
//                    Toast.makeText(LoginActivity.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();

                    attemptLogin();


                    break;
                case R.id.buttonJoin:
                    intent = new Intent(getApplicationContext(), JoinActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };




    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }



    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if(!hasFocus){
                switch (v.getId()) {



                    case R.id.loginEmail:

                        textLoginEmail.setErrorEnabled(true);
                        email = mEmailView.getText().toString();

                        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {

                            if (TextUtils.isEmpty(email)) {
                                textLoginEmail.setError(getString(R.string.error_field_required));
                            } else {
                                textLoginEmail.setError(getString(R.string.error_invalid_email));
                            }


                        }
                        //no Error
                        else {
                            textLoginEmail.setError(null);
                            textLoginEmail.setErrorEnabled(false);
                        }

                        break;


                    case R.id.loginPassword:
                        textLoginPassword.setErrorEnabled(true);
                        password = mPasswordView.getText().toString();

                        // 패스워드 빈 것만 확인. 형식 확인 필요없다
                        if (TextUtils.isEmpty(password)) {
                            textLoginPassword.setError(getString(R.string.error_field_required));

                        }
                        //에러가 없다
                        else {
                            textLoginPassword.setError(null);
                            textLoginPassword.setErrorEnabled(false);
                        }


                        break;
                }
            }
            else{


                switch (v.getId()) {

                    // 주의 : 생년 월일은 다이얼로그에서 처리하였다. 입력 완료 -> 에러 널로!
                    case R.id.loginEmail:


                        textLoginEmail.setError(null);
                        textLoginEmail.setErrorEnabled(false);

                        break;


                    case R.id.loginPassword:



                        textLoginPassword.setError(null);
                        textLoginPassword.setErrorEnabled(false);




                }






            }


        }
    };
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
        textLoginPassword.setError(null);
        textLoginPassword.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;



        // Check for a valid email address.

        // 패스워드 형식 확인하기.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            if(TextUtils.isEmpty(password)){
                textLoginPassword.setError(getString(R.string.error_field_required));
            }
            else{
                textLoginPassword.setError(getString(R.string.error_invalid_password));
            }

            focusView = textLoginPassword;
            cancel = true;
        }
        //이메일 형식 확인하기

        if (TextUtils.isEmpty(email)) {

            textLoginEmail.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {

            textLoginEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
//            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {

        return  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        //굳이 확인할 필요 없음, 틀린 것만 보면 된다.
        return true;
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

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {



            //DB에서 회원 정보를 확인하고 로그인하라
            CheckDB cdb = new CheckDB();
            cdb.execute();

            return true;


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }






    public class CheckDB extends AsyncTask<Void, Integer, Boolean>
    {
        String json_result = "";
        String passwordEnc = "";
        @Override
        protected Boolean doInBackground(Void... unused) {

/* 인풋 파라메터값 생성 */

            passwordEnc = md5(password);

            String param = "u_email=" + email + "&u_pw=" + passwordEnc;
            Log.e("passEnc", passwordEnc);
            boolean success = false;

            try {
/* 서버연결 */
                URL url = new URL(
                        "http://115.68.231.13/project/android/login2.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

/* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

/* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;


                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                json_result = buff.toString().trim();

                //<editor-fold desc="json 파싱 관련파트.">
                // json_result는 결과값으로 가져온 json String이다. json오브젝트에 이 스트링을 담는다.


                JSONObject jsonRes = null;
                try {
                    jsonRes = new JSONObject(json_result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // jsonRes오브젝트에서 로그인 메세지와, 로그인 정보가 담긴 쿠키를 추출한다.
                JSONObject json_cookie = null;
                String login_msg = null;
                try {
                    login_msg = jsonRes.getString("login_msg");
                    json_cookie = jsonRes.getJSONObject("login_cookie");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e("parsed DATA", login_msg+json_cookie.toString());




                // RECV 데이터에 php에서 뱉은 echo가 들어간다!
                Log.e("RECV DATA", json_result);

                //json을 성공적으로 서버에서 수신했다. 쿠키를 저장시키자
                if(json_result.contains("success")){

                    success = true;
                    // 추출한 쿠키를 쉐어드에 저장시킨다.
                    SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref_login.edit();
                    try {
                        String cookie_email = json_cookie.getString("email");
                        String cookie_name = json_cookie.getString("name");
                        String cookie_birthday = json_cookie.getString("birthday");

                        editor.putString(getString(R.string.cookie_email), cookie_email);
                        editor.putString(getString(R.string.cookie_name),cookie_name);
                        editor.putString(getString(R.string.cookie_birthday),cookie_birthday);

                        editor.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //</editor-fold>

                Log.e("success", String.valueOf(success));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if(success){
//                Snackbar.make(findViewById(R.id.email_sign_in_button), "환영합니다. 계정"+email+"으로 가입하셨습니다.", Snackbar.LENGTH_LONG).show();


                LauncherActivity.LoginToken = true;
                Toast.makeText(LoginActivity.this, "환영합니다. 계정"+email+"으로 로그인하셨습니다.", Toast.LENGTH_SHORT).show();




                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

                finish();
            }
            //로그인이 실패하였다.
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else{

                Toast toast = Toast.makeText(LoginActivity.this, "등록되지 않은 계정이거나, 아이디 혹은 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT);
                int offsetX = 0;
                int offsetY = 0;
                toast.setGravity(Gravity.CENTER, offsetX, offsetY);
                toast.show();

            }

        }
    }
}

