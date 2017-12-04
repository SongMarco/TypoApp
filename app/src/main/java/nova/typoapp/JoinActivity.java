package nova.typoapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nova.typoapp.retrofit.ApiService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * A login screen that offers login via email/password.
 */
public class JoinActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {


    public static final Pattern VALID_NAME = Pattern.compile("^[가-힣a-zA-Z]{1,10}$");




    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private userJoinTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private TextInputLayout textEmail, textPassword, textPasswordConf, textName, textBirthday;
    private EditText mPasswordView, mPasswordConfirmView, mName, mBirthday;
    private View mProgressView;
    private View mLoginFormView;

    String email, password, passwordConfirm, name, birthday;

    int setYear = 1992;
    int setMonth = 5;
    int setDay = 27;

    Button buttonFacebookJoin;
    CallbackManager callbackManager;
    private List<String> permissionNeeds = Arrays.asList("email");

    DatePicker datepicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_join);
        // Set up the login form.


        buttonFacebookJoin = (Button)findViewById(R.id.buttonFacebookJoin);

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

                            Toast.makeText(JoinActivity.this, "페이스북 계정"+email+"으로 간편가입/로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);

                            //액티비티 스택을 초기화시킨다@@

                            Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);


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



        mEmailView = (AutoCompleteTextView) findViewById(R.id.loginEmail);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.editPassword);
        mPasswordConfirmView = (EditText)findViewById(R.id.editPasswordConfirm);
        mBirthday = (EditText)findViewById(R.id.editBirthday);
        mName = (EditText)findViewById(R.id.editName);


//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                    attemptJoin();
//                    return true;
//                }
//                return false;
//            }
//        });



//        mPasswordConfirmView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                    attemptJoin();
//                    return true;
//                }
//                return false;
//            }
//        });

        Button buttonSignUp = (Button) findViewById(R.id.email_sign_in_button);
        buttonSignUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptJoin();


            }
        });


        mBirthday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

//                DatePickerDialog dialog = new DatePickerDialog(JoinActivity.this, listener, 1992, 5, 27);
//                dialog.set
//
//                dialog.show();


                AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_datepick, null);

                //todo 와 이거진짜 돌았어!! 내가 inflate 시킨 view에서 findVB처리해야지 변수가 잡히는구나@@@@
                datepicker = (DatePicker)view.findViewById(R.id.datePicker);


                builder.setView(view);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year, monthOfYear, dayOfMonth;
                        year = datepicker.getYear();
                        monthOfYear = datepicker.getMonth()+1;
                        dayOfMonth = datepicker.getDayOfMonth();

                        setYear = year;
                        setMonth = monthOfYear;
                        setDay = dayOfMonth;

                        birthday = year + "." + monthOfYear + "." + dayOfMonth;
                        mBirthday.setText(birthday);

                        textBirthday.setError(null);
                        textBirthday.setErrorEnabled(false);

                        dialog.dismiss();
                    }});

                builder.setNeutralButton("취소", null);

                datepicker.updateDate(setYear, setMonth-1, setDay);


                final AlertDialog customDialog = builder.create();
                customDialog.show();
            }
        });




        textEmail = (TextInputLayout)findViewById(R.id.textEmail);
        textPassword= (TextInputLayout)findViewById(R.id.textPassword);
        textPasswordConf = (TextInputLayout)findViewById(R.id.textPasswordConf);
        textName = (TextInputLayout)findViewById(R.id.textName);
        textBirthday = (TextInputLayout)findViewById(R.id.textBirthday);


        mEmailView.setOnFocusChangeListener(focusChangeListener);
        mPasswordView.setOnFocusChangeListener(focusChangeListener);
        mPasswordConfirmView.setOnFocusChangeListener(focusChangeListener);
        mName.setOnFocusChangeListener(focusChangeListener);
        mBirthday.setOnFocusChangeListener(focusChangeListener);


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setLoginButton() {
        if (AccessToken.getCurrentAccessToken() != null) {
            buttonFacebookJoin.setText("로그아웃");
            buttonFacebookJoin.setOnClickListener(new View.OnClickListener() {
                @Override


                public void onClick(View view) {

                    Toast.makeText(JoinActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                    LoginManager.getInstance().logOut();
                    setLoginButton();
                }
            });
        } else {
            buttonFacebookJoin.setText("페이스북으로 간편 가입");
            buttonFacebookJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    LoginManager.getInstance().logInWithReadPermissions(JoinActivity.this, permissionNeeds);
                }
            });
        }
    }
//    DialogInterface.OnClickListener dateListener = new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//
//
//            year = datepicker.getYear();
//            monthOfYear = datepicker.getMonth();
//            dayOfMonth = datepicker.getDayOfMonth();
//
//            birthday = year + "." + monthOfYear + "." + dayOfMonth;
//            mBirthday.setText(birthday);
//
//            textBirthday.setError(null);
//            textBirthday.setErrorEnabled(false);
//        }
//
//    };


    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if(!hasFocus){
                switch (v.getId()) {

                    // 주의 : 생년 월일은 다이얼로그에서 처리하였다. 입력 완료 -> 에러 널로!
                    case R.id.editName:

                        textName.setErrorEnabled(true);
                        name = mName.getText().toString();

                        if( TextUtils.isEmpty(name) || !isNameValid(name) ){

                            if(TextUtils.isEmpty(name)){
                                textName.setError(getString(R.string.error_field_required));
                            }
                            else{
                                textName.setError(getString(R.string.error_invalid_name));
                            }


                        }
                        //no Error
                        else{
                            textName.setError(null);
                            textName.setErrorEnabled(false);
                        }

                        break;


                    case R.id.loginEmail:
                        textEmail.setErrorEnabled(true);
                        email = mEmailView.getText().toString();

                        // Check for a valid email address.
                        if (TextUtils.isEmpty(email)) {
                            textEmail.setError(getString(R.string.error_field_required));


                        } else if (!isEmailValid(email)) {
                            textEmail.setError(getString(R.string.error_invalid_email));

                        }
                        //에러가 없다
                        else {

                            textEmail.setError(null);
                            textEmail.setErrorEnabled(false);
                        }


                        break;
                    case R.id.editPassword:

                        textPassword.setErrorEnabled(true);
                        password = mPasswordView.getText().toString();

                        // 패스워드 형식 확인하기.
                        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
                            if(TextUtils.isEmpty(password)){
                                textPassword.setError(getString(R.string.error_field_required));
                            }
                            else if(!isPasswordValid(password)){
                                textPassword.setError(getString(R.string.error_invalid_password));
                            }

                        }
                        //no error
                        else{

                            textPassword.setError(null);
                            textPassword.setErrorEnabled(false);
                        }


                        mPasswordView.setNextFocusDownId(R.id.editPasswordConfirm);

                        break;
                    case R.id.editPasswordConfirm:

                        // 암호 확인은 입력이 마지막꺼라서 안되니까 컨펌할때 한번더 입력한다.
                        textPasswordConf.setErrorEnabled(true);
                        passwordConfirm = mPasswordConfirmView.getText().toString();

                        //패스워드 확인 형식 확인하기.
                        if (TextUtils.isEmpty(passwordConfirm) || !isPasswordValid(passwordConfirm)) {
                            if(TextUtils.isEmpty(passwordConfirm)){
                                textPasswordConf.setError(getString(R.string.error_field_required));
                            }
                            else{
                                textPasswordConf.setError(getString(R.string.error_invalid_password));
                            }

                        }
                        //no error
                        else{

                            textPasswordConf.setError(null);
                            textPasswordConf.setErrorEnabled(false);
                        }

                        break;


                }
            }
            else{


                switch (v.getId()) {

                    // 주의 : 생년 월일은 다이얼로그에서 처리하였다. 입력 완료 -> 에러 널로!
                    case R.id.editName:


                        textName.setError(null);
                        textName.setErrorEnabled(false);

                        break;


                    case R.id.loginEmail:



                        textEmail.setError(null);
                        textEmail.setErrorEnabled(false);



                        break;
                    case R.id.editPassword:



                        textPassword.setError(null);
                        textPassword.setErrorEnabled(false);


                        break;
                    case R.id.editPasswordConfirm:




                        textPasswordConf.setError(null);
                        textPasswordConf.setErrorEnabled(false);


                        break;


                }






            }


        }
    };

    TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            //입력 후 에러 체크하기



            return false;
        }
    };

    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            monthOfYear++;

            birthday = year + "." + monthOfYear + "." + dayOfMonth;
            mBirthday.setText(birthday);

            textBirthday.setError(null);
            textBirthday.setErrorEnabled(false);

//            Toast.makeText(getApplicationContext(), year + "년" + monthOfYear + "월" + dayOfMonth +"일", Toast.LENGTH_SHORT).show();
        }
    };


    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptJoin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
//        mEmailView.setError(null);
//        mPasswordView.setError(null);

        // Store values at the time of the login attempt.


        textPasswordConf.setErrorEnabled(true);
        passwordConfirm = mPasswordConfirmView.getText().toString();




        boolean cancel = false;
        View focusView = null;

        //형식검사 예외처리 들어간다. 밑칸 -> 위칸 순으로 처리하여, 포커스가 위에 것이 최종 포커스가 되도록 하자.






        //패스워드 확인 형식 확인하기.
        if (TextUtils.isEmpty(passwordConfirm) || !isPasswordValid(passwordConfirm)) {
            if(TextUtils.isEmpty(passwordConfirm)){
                textPasswordConf.setError(getString(R.string.error_field_required));
            }
            else{
                textPasswordConf.setError(getString(R.string.error_invalid_password));
            }

            focusView = textPasswordConf;
            cancel = true;
        }

        // 패스워드 형식 확인하기.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            if(TextUtils.isEmpty(password)){
                textPassword.setError(getString(R.string.error_field_required));
            }
            else{
                textPassword.setError(getString(R.string.error_invalid_password));
            }

            focusView = textPassword;
            cancel = true;
        }

        //패스워드 확인과 패스워드 일치 확인하기.
        if (  (password!=null && passwordConfirm!=null) && !password.equals(passwordConfirm) ) {

            textPassword.setError(getString(R.string.error_different_password));
            textPasswordConf.setError(getString(R.string.error_different_password));

            focusView = textPassword;
            cancel = true;
        }


        // 이메일 형식 확인하기
        if (TextUtils.isEmpty(email)) {
            textEmail.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            textEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }


        //이름 형식 확인하기.
        if( TextUtils.isEmpty(name) || !isNameValid(name) ){

            if(TextUtils.isEmpty(name)){
                textName.setError(getString(R.string.error_field_required));
            }
            else{
                textName.setError(getString(R.string.error_invalid_name));
            }

            focusView = textName;
            cancel = true;

        }
        //생년월일 입력여부 확인하기.
        if( TextUtils.isEmpty(birthday) ){

            textBirthday.setError(getString(R.string.error_field_required));

            focusView = textBirthday;
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

            mAuthTask = new userJoinTask(email, password, name, birthday);
            mAuthTask.execute((Void) null);
        }
    }



    private boolean isEmailValid(String email) {


//        return email.contains("@");
        return  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

    }

    private boolean isPasswordValid(String password) {
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^*()&+=])(?=\\S+$).{4,}$";
        final String PASSWORD_PATTERN_noBig = "^(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^*()&+=])(?=\\S+$).{6,16}$";
        pattern = Pattern.compile(PASSWORD_PATTERN_noBig);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    private boolean isNameValid(String name){

        Pattern pattern;
        Matcher matcher;

        final String NAME_PATTERN = "^[가-힣a-zA-Z]{1,10}$";

        pattern = Pattern.compile(NAME_PATTERN);
        matcher = pattern.matcher(name);

        return matcher.matches();

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
                new ArrayAdapter<>(JoinActivity.this,
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
    public class userJoinTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mName;
        private final String mBirthday;

        userJoinTask(String email, String password, String name, String birthday) {
            mEmail = email;
            mPassword = password;
            mName = name;
            mBirthday = birthday;

        }

        @Override
        protected Boolean doInBackground(Void... params) {


            //DB에 회원 정보를 중복체크 후 기입한다.
          JoinRetrofitTask jtask = new JoinRetrofitTask();
          jtask.execute();

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


    //todo 회원가입 레트로핏으로 구현하기ㅇ

    String json_result = "";
    public class JoinRetrofitTask extends AsyncTask<Void, String, String> {


        @Override
        protected String doInBackground(Void... voids) {

            //region//글쓰기

            Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiService.API_URL).build();
            ApiService apiService = retrofit.create(ApiService.class);



            String passwordEnc = LoginActivity.md5(password);

            Call<ResponseBody> comment = apiService.joinMember(email, passwordEnc, name, birthday);

            try {
                json_result = comment.execute().body().string();
                return json_result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override

        protected void onPostExecute(String result){

            super.onPostExecute(result);

            Log.e("LogResult", result);


            if ( result.contains("success") ) {
//                Snackbar.make(findViewById(R.id.email_sign_in_button), "환영합니다. 계정"+email+"으로 가입하셨습니다.", Snackbar.LENGTH_LONG).show();



                Toast.makeText(JoinActivity.this, "이메일 계정 "+email+" (으)로 회원 가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

                finish();
            }

            //아이디 중복으로 가입이 실패하였다.
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else{
                if( result.contains("email")){
                    Snackbar.make(findViewById(R.id.email_sign_in_button), "이미 가입된 이메일입니다.", Snackbar.LENGTH_LONG).show();
                    textEmail.setErrorEnabled(true);
                    textEmail.setError(getString(R.string.error_mail_exists));
                }
            }



        }

    }

    public class registDB extends AsyncTask<Void, Integer, Boolean>
    {
        String json_result = "";
        String passwordEnc = "";
        @Override
        protected Boolean doInBackground(Void... unused) {

/* 인풋 파라메터값 생성 */


            passwordEnc = LoginActivity.md5(password);
            String param = "u_email=" + email + "&u_pw=" + passwordEnc + "&u_name=" + name + "&u_birthday=" + birthday ;

            boolean success = false;

            try {
/* 서버연결 */
                URL url = new URL(
                        "http://115.68.231.13/project/android/join.php");
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

                Log.e("success", String.valueOf(json_result));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(json_result.contains("success")){
                success = true;
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if(success){
//                Snackbar.make(findViewById(R.id.email_sign_in_button), "환영합니다. 계정"+email+"으로 가입하셨습니다.", Snackbar.LENGTH_LONG).show();

                Toast.makeText(JoinActivity.this, "환영합니다. 계정"+email+"으로 가입하셨습니다.", Toast.LENGTH_SHORT).show();

                finish();
            }
            //아이디 중복으로 가입이 실패하였다.
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else{
                if(json_result.contains("email")){
                    Snackbar.make(findViewById(R.id.email_sign_in_button), "이미 가입된 이메일입니다.", Snackbar.LENGTH_LONG).show();
                    textEmail.setErrorEnabled(true);
                    textEmail.setError(getString(R.string.error_mail_exists));

                }

            }

        }
    }

}

