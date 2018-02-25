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
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.LoginInfo;
import nova.typoapp.retrofit.LoginResult;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */

/*
로그인 액티비티

로그인 액티비티에서는 이메일과 비밀번호로 로그인을 할 수 있으며,
가입하지 않은 사용자는 회원가입을 할 수 있다.

 */

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     * 자동완성시 이메일 목록 확인이 필요하므로, 연락처 목록 읽기 권한 필요,
     * 그러나 현재는 자동완성을 사용하고 있지 않으므로, 주석처리 하는 것을 권장
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private List<String> permissionNeeds = Arrays.asList("email");

    /**
     *
     * Keep track of the login task to ensure we can cancel it if requested.
     *클래스 변수들 정의.
     *
     */

    /*
    로그인 처리를 담당하는 스레드.
    실제 사용처는 ctrl+B로 클래스를 참고하라
      */
    private LoginRetrofitTask mAuthTask = null;

    /*
    UI에 대한 정의
    근데 왜 private 로 사용하는 것일까?
    - 이 뷰들은 이 액티비티 안에서만 접근하길 원하기 때문.
    즉, 액티비티 안에서 사용할 변수를 명확히 할 수 있다.

     */
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private TextInputLayout textLoginEmail, textLoginPassword;



    private CallbackManager callbackManager;


    private Button buttonFacebook;

    private String email, password;



    /*
    로그인 액티비티 초기화

    주로 로그인 관련 뷰 - 입력창, 버튼 등 - 을 세팅하게 되는데,
    페이스북 로그인 API 관련 세팅 코드가 긴 편이다.

    페이스북 관련 기능을 세팅하고,
    뷰를 세팅하고
    리스너를 세팅하게 된다.

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_login);

        buttonFacebook = (Button) findViewById(R.id.buttonFacebookLogin);
        setFacebookLoginButton();

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<com.facebook.login.LoginResult>() {
            @Override
            public void onSuccess(final com.facebook.login.LoginResult result) {

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

                            Toast.makeText(LoginActivity.this, "페이스북 계정 " + email + " 으로 로그인하였습니다.", Toast.LENGTH_SHORT).show();
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


        /*
        로그인 관련 뷰들을 inflate 한다.
        이메일 및 비밀번호 입력란과 각종 버튼들이 세팅된다.
         */
        mEmailView = (AutoCompleteTextView) findViewById(R.id.loginEmail);
        populateAutoComplete();
        mLoginFormView = findViewById(R.id.login_form);
        mPasswordView = (EditText) findViewById(R.id.loginPassword);
        Button mEmailSignInButton = (Button) findViewById(R.id.buttonLogin);
        mProgressView = findViewById(R.id.login_progress);
        textLoginEmail = (TextInputLayout) findViewById(R.id.textLoginEmail);
        textLoginPassword = (TextInputLayout) findViewById(R.id.textLoginPassword);


        /*
        이메일과 패스워드 입력 란에는
        포커스 체인지 리스너를 세팅한다.

        입력한 형식이 적절치 않거나, 입력을 하지 않았을 경우 예외처리를 적용한다.
        자세한 사항은 focusChangeListener 참조
         */
        mPasswordView.setOnFocusChangeListener(focusChangeListener);
        mEmailView.setOnFocusChangeListener(focusChangeListener);


        /*
        로그인 버튼 클릭리스너

        로그인 버튼을 클릭시 로그인을 위한 스레드를 실행하게 된다.
        */
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


    /*
    페이스북 로그인을 위한 버튼을 세팅한다.

    로그인을 한 상태라면 로그아웃 텍스트와 해당 동작을,
    로그인이 되지 않은 상태라면 페이스북으로 로그인 텍스트와 동작을 버튼에 세팅
     */
    private void setFacebookLoginButton() {
        if (AccessToken.getCurrentAccessToken() != null) {
            buttonFacebook.setText("로그아웃");
            buttonFacebook.setOnClickListener(new View.OnClickListener() {
                @Override


                public void onClick(View view) {

                    Toast.makeText(LoginActivity.this, "로그아웃 하셨습니다.", Toast.LENGTH_SHORT).show();

                    LoginManager.getInstance().logOut();
                    setFacebookLoginButton();
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


    /*
    onActivityResult에서는 페이스북 로그인 이후 콜백을 받아 처리하게 된다.(분명하지 않음 - 확실히 할 것)

    ex) 페이스북 로그인 버튼 -> 페이스북 계정 로그인 하기 -> 페이스북 로그인 됬는지 안됬는지 파악해서
    onActivityResult에서 콜백메소드를 통해 적절히 세팅.

     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    //로그인 버튼과 회원가입 버튼에 대한 클릭 리스너
    Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {

                /*
                로그인 버튼 클릭 -> 로그인 관련 메소드가 수행된다.

                사용자가 정확히 ID/PW 정보를 입력하였는지 등을 체크하고 로그인 처리한다.

                 */
                case R.id.buttonLogin:
//                    Toast.makeText(LoginActivity.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();

                    attemptLogin();


                    break;

                    /*
                    회원가입 버튼 클릭 -> 회원가입 액티비티를 띄운다.
                     */
                case R.id.buttonJoin:
                    intent = new Intent(getApplicationContext(), JoinActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };


    /*
    자동 완성과 관련된 코드. 아직 제대로 동작하지 않는다.
     */
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


    /*
    OnFocusChangeListener

    포커스가 바뀔 때 어떤 행동을 할지 지정하는 리스너다.

    이메일 : 입력 X 혹은 적절치 않은 형식(정규식 처리)
    비밀번호 : 입력 X
    위와 같은 경우 빨간색의 에러메시지를 출력한다.

    */
    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {


            //사용자 포커스가 있다가 없어졌을 때 동작
            if (!hasFocus) {

                switch (v.getId()) {


                    //이메일 입력 칸에서 포커스가 있다가 없어질 때

                    case R.id.loginEmail:


                        textLoginEmail.setErrorEnabled(true);
                        email = mEmailView.getText().toString();

                        //이메일 입력칸이 비었는지, 이메일 형식이 유효한지 검사
                        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {

                            //비었다면 이메일 입력이 필요하다고 에러 출력
                            if (TextUtils.isEmpty(email)) {
                                textLoginEmail.setError(getString(R.string.error_field_required));
                            }
                            //비지 않았다면 적절치 않은 형식으로 에러 출력
                            else {
                                textLoginEmail.setError(getString(R.string.error_invalid_email));
                            }


                        }
                        //에러 없음. 정상 진행
                        else {
                            textLoginEmail.setError(null);
                            textLoginEmail.setErrorEnabled(false);
                        }

                        break;

                    //비밀번호 입력칸
                    //패스워드 입력 여부만 판단함. 맞고 / 틀리고는 로그인 시도할 때 판단하게 됨
                    case R.id.loginPassword:
                        textLoginPassword.setErrorEnabled(true);
                        password = mPasswordView.getText().toString();

                        // 패스워드가 입력되지 않았음을 확인함
                        if (TextUtils.isEmpty(password)) {
                            textLoginPassword.setError(getString(R.string.error_field_required));

                        }
                        //에러가 없음
                        else {
                            textLoginPassword.setError(null);
                            textLoginPassword.setErrorEnabled(false);
                        }
                        break;
                }
            }

            //사용자 포커스가 없다가 있었을 때.
            //특정 칸 입력 후 포커스를 옮겼을 때 에러가 있는 걸 보고,
            //다시 해당 칸을 재입력할 때 이 케이스가 발생한다.
            else {


                switch (v.getId()) {

                    //이메일을 수정하려 시도할 때, 붉은 에러메시지를 없애줌
                    case R.id.loginEmail:


                        textLoginEmail.setError(null);
                        textLoginEmail.setErrorEnabled(false);

                        break;


                    //패스워드를 수정하려 시도할 때 에러메시지 없애줌
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

    /*
    로그인을 시도하는 메소드.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        //에러 메시지 초기화
        mEmailView.setError(null);
        mPasswordView.setError(null);
        textLoginPassword.setError(null);
        textLoginPassword.setError(null);

        // Store values at the time of the login attempt.
        // 사용자가 입력한 정보를 변수화
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;


        // 패스워드 형식 확인하기.
        // 패스워드 입력칸이 빈 것을 확인한다.
        // 원래는 패스워드 형식을 검사하는 isPasswordValid 조건이 있었지만,
        // 패스워드가 비었는지 여부만 확인하도록 변경되었다.
        if (TextUtils.isEmpty(password) ) {

            textLoginPassword.setError(getString(R.string.error_field_required));

            cancel = true;
        }
        //이메일 형식 확인하기
        //이메일이 비었는지, 적절한 형식인지 검사(정규식)
        if (TextUtils.isEmpty(email)) {

            textLoginEmail.setError(getString(R.string.error_field_required));
            cancel = true;
        } else if (!isEmailValid(email)) {

            textLoginEmail.setError(getString(R.string.error_invalid_email));
            cancel = true;
        }

        //각 예외처리를 통해 cancel bool변수가 true이면, 로그인 처리하지 않고
        //에러메시지를 확인할 수 있다.
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
//            focusView.requestFocus();
        }
        //cancel 변수가 false 이면, 오류 없이 정상적으로 로그인을 시도한 것이다.
        //서버에서 로그인한 이메일과 패스워드를 확인하는 스레드(userLoginTask)를 실행한다.
        else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new LoginRetrofitTask(email, password);
            mAuthTask.execute();
        }
    }

    //이메일 형식에 대한 정규식 검사
    private boolean isEmailValid(String email) {

        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Shows the progress UI and hides the login form.
     * 프로그레스 관련 UI를 세팅한다. 로그인할 때 동그랗게 돌아가는 로딩 모양이 보인다.
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

    /*
    자동 로딩 관련 파트. 접어둔다
     */

    //region 자동 로딩 관련 파트, 사용하지 않으므로 접어둠.(추후 정리할 때 삭제할 것)
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


    /*
    자동완성 관련 파트
    */
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
    //endregion




    /*
     UserLoginTask

     사용자가 형식에 맞는 이메일과 패스워드를 입력할 경우 동작
     서버를 통해 사용자 정보를 검사하고 로그인처리한다.

     사용자 패스워드는 암호화해서 전송한다.
      */
    String json_result = "";
    public class LoginRetrofitTask extends AsyncTask<Void, String, String> {

        private final String mEmail;
        private final String mPassword;

        LoginRetrofitTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        final String TAG = "myTag";


        /*
        doInBackground 에서 레트로핏2와 okHttpClient 를 이용, 서버와 http 통신을 수행한다.

        레트로핏2의 http 통신 과정은 okHttpClient 세팅, 레트로핏 객체 세팅, 레트로핏 call 의 실행으로 이루어진다.
         */
        @Override
        protected String doInBackground(Void... voids) {

            /*


            레트로핏의 okHttpClient 를 세팅한다.
            해당 클라이언트에는 3가지의 인터셉터가 적용되었다.

            먼저 안드로이드에서 세션-쿠키를 전송하고 받아서 저장하기 위한 두 인터셉터가 있다.
            서버에 전송하기 위한 쿠키값은 PHPSESSID 를 사용한다. 이것을 서버에 전송해야
            서버에서 세션에 저장된 데이터를 이용할 수 있다.
            1. AddCookiesInterceptor : http 통신할 때 클라이언트의 sharedPreference 에 저장된 쿠키를 서버로 전송할 때 사용
            2. ReceivedCookiesInterceptor : http 통신 이후 서버에서 받은 쿠키를 sharedPreference 에 저장할 때 사용

            3. httpLoggingInterceptor : 안드로이드 로그캣에서 http 통신 로그를 확인할 때 사용한다.
            로그캣에서 http를 검색하면 http 통신을 확인할 수 있다.(리퀘스트, 리스폰스, 쿠키, 오가는 데이터 등을 확인 가능)

            4. StethoInterceptor : 페이스북에서 제작한 라이브러리다. httpLoggingInterceptor 와 마찬가지로 로그를 확인할 때 사용하는데,
            http 통신 뿐만 아니라 앱의 sharedPreference 에 어떤 값이 저장된지도 확인 가능하다. 자세한 사항은 Stetho 검색
             */

            //okhttp client 세팅
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AddCookiesInterceptor(LoginActivity.this))
                    .addInterceptor(new ReceivedCookiesInterceptor(LoginActivity.this))
                    .addInterceptor(httpLoggingInterceptor)
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();

            //레트로핏 객체 세팅
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();


//            Log.e(TAG, "shared-before call: "+getSharedPreferences(getString(R.string.key_pref_Login) , MODE_PRIVATE ).getAll()  ) ;

            ApiService apiService = retrofit.create(ApiService.class);

            //패스워드를 암호화한다. 이 값을 http 통신에서 전송 후, 서버에서 DB에 저장된 패스워드값과 비교하게 된다.
            String passwordEnc = LoginActivity.md5(password);


            //레트로핏 call 객체를 만든다. 이 call이 execute 되면 http 통신이 이루어진다.
            String token = FirebaseInstanceId.getInstance().getToken();

            Call<LoginResult> call = apiService.loginMember(email, passwordEnc, token);


            // http 통신을 수행한다.

            try {
//                SharedPreferenceBase.setContext(LoginActivity.this);

                //loginResult는 http의 reponse 이며, json을 자바 객체로 만들었다.
                //자세한 사항은 ctrl+B로 클래스를 확인해보라
                LoginResult loginResult = call.execute().body();



                //통신 이후로 로그로 shared 에 필요한 변수들이 담겼는지 확인한다.
                Log.e(TAG, "shared-after call: "+getSharedPreferences(getString(R.string.key_pref_Login) , MODE_PRIVATE ).getAll()  ) ;


                Log.e("info", loginResult.toString() );

                //로그인 성공 여부를 가져온다.
                json_result = loginResult.getLogin_msg();

                LoginInfo login_info = loginResult.getInfo();
                Log.e("info", login_info.getEmail() );


                //쉐어드 프리퍼런스에 로그인 정보를 저장한다. - 이메일, 이름, 생년월일
                SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref_login.edit();
                String cookie_email = login_info.getEmail();
                String cookie_name = login_info.getName();
                String cookie_birthday = login_info.getBirthday();

                String cookie_profile_url = login_info.getProfile_url();

                editor.putString(getString(R.string.cookie_email), cookie_email);
                editor.putString(getString(R.string.cookie_name), cookie_name);
                editor.putString(getString(R.string.cookie_birthday), cookie_birthday);
                editor.putString("cookie_profile_url", cookie_profile_url);






                editor.apply();
                Log.e(TAG, "shared: "+getSharedPreferences(getString(R.string.key_pref_Login), MODE_PRIVATE ).getAll()  ) ;


                //onPost 에 들어갈 결과값

                return json_result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            Log.e("LogResult", result);

            //로그인 결과가 성공이라면 로그인 성공 메시지를 띄우고,
            //메인 액티비티로 이동한다.
            if (result.contains("success")) {

                Toast.makeText(LoginActivity.this, "계정 " + email + " (으)로 로그인하셨습니다.", Toast.LENGTH_SHORT).show();

                // 파이어베이스 토큰을 가져온다. 토큰은 기기마다 정해지며, 앱을 삭제 후 재설치시 새 토큰이 발급된다.


//                MyFireBaseInstanceIDService myFireBaseInstanceIDService = new MyFireBaseInstanceIDService();
//                myFireBaseInstanceIDService.onTokenRefresh();
//                Log.e("abc", "onCreate at Login: "+token );
                

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

                finish();
            }


            // ID/PW가 맞지 않아 로그인에 실패하였다.
            // 에러 메시지를 띄운다.
            else {

                Toast toast = Toast.makeText(LoginActivity.this, "등록되지 않은 계정이거나, 아이디 혹은 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT);
                int offsetX = 0;
                int offsetY = 0;
                toast.setGravity(Gravity.CENTER, offsetX, offsetY);
                toast.show();

            }

            mAuthTask = null;
            showProgress(false);
        }


        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }



    //패스워드를 서버에 전송하기 전에 암호화하는 메소드
    //파라미터로 들어온 문자열을 XXX방식으로 암호화한다. (보안상 주석에서 밝히지 않음)
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


    /*
    httpUrlConnection 으로 서버와 통신하는 스레드다.
    지금은 레트로핏으로 http 통신을 한다.
    httpURLConnection 의 사용법을 기억하기 위해 주석처리하고 남겨둔다.
     */

//    public class CheckDB extends AsyncTask<Void, Integer, Boolean> {
//        String json_result = "";
//        String passwordEnc = "";
//
//        @Override
//        protected Boolean doInBackground(Void... unused) {
//
///* 인풋 파라메터값 생성 */
//
//            passwordEnc = md5(password);
//
//            String param = "u_email=" + email + "&u_pw=" + passwordEnc;
//            Log.e("passEnc", passwordEnc);
//            boolean success = false;
//
//            try {
///* 서버연결 */
//                URL url = new URL(
//                        "http://115.68.231.13/project/android/login.php");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                conn.setRequestMethod("POST");
//                conn.setDoInput(true);
//                conn.connect();
//
///* 안드로이드 -> 서버 파라메터값 전달 */
//                OutputStream outs = conn.getOutputStream();
//                outs.write(param.getBytes("UTF-8"));
//                outs.flush();
//                outs.close();
//
///* 서버 -> 안드로이드 파라메터값 전달 */
//                InputStream is = null;
//                BufferedReader in = null;
//
//
//                is = conn.getInputStream();
//                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
//                String line = null;
//                StringBuffer buff = new StringBuffer();
//                while ((line = in.readLine()) != null) {
//                    buff.append(line + "\n");
//                }
//                json_result = buff.toString().trim();
//
//                //<editor-fold desc="json 파싱 관련파트.">
//                // json_result는 결과값으로 가져온 json String이다. json오브젝트에 이 스트링을 담는다.
//
//
//                JSONObject jsonRes = null;
//                try {
//                    jsonRes = new JSONObject(json_result);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                // jsonRes오브젝트에서 로그인 메세지와, 로그인 정보가 담긴 쿠키를 추출한다.
//                JSONObject json_cookie = null;
//                String login_msg = null;
//                try {
//                    login_msg = jsonRes.getString("login_msg");
//                    json_cookie = jsonRes.getJSONObject("login_cookie");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                Log.e("parsed DATA", login_msg + json_cookie.toString());
//
//
//                // RECV 데이터에 php에서 뱉은 echo가 들어간다!
//                Log.e("RECV DATA", json_result);
//
//                //json을 성공적으로 서버에서 수신했다. 쿠키를 저장시키자
//                if (json_result.contains("success")) {
//
//                    success = true;
//                    // 추출한 쿠키를 쉐어드에 저장시킨다.
//                    SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = pref_login.edit();
//                    try {
//                        String cookie_email = json_cookie.getString("email");
//                        String cookie_name = json_cookie.getString("name");
//                        String cookie_birthday = json_cookie.getString("birthday");
//
//                        editor.putString(getString(R.string.cookie_email), cookie_email);
//                        editor.putString(getString(R.string.cookie_name), cookie_name);
//                        editor.putString(getString(R.string.cookie_birthday), cookie_birthday);
//
//                        editor.apply();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                //</editor-fold>
//
//                Log.e("success", String.valueOf(success));
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            return success;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean success) {
//            super.onPostExecute(success);
//
//            if (success) {
////                Snackbar.make(findViewById(R.id.email_sign_in_button), "환영합니다. 계정"+email+"으로 가입하셨습니다.", Snackbar.LENGTH_LONG).show();
//
//
//                Toast.makeText(LoginActivity.this, "환영합니다. 계정" + email + "으로 로그인하셨습니다.", Toast.LENGTH_SHORT).show();
//
//
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//
//                finish();
//            }
//            //로그인이 실패하였다.
//            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
//            else {
//
//                Toast toast = Toast.makeText(LoginActivity.this, "등록되지 않은 계정이거나, 아이디 혹은 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT);
//                int offsetX = 0;
//                int offsetY = 0;
//                toast.setGravity(Gravity.CENTER, offsetX, offsetY);
//                toast.show();
//
//            }
//
//        }
//    }
}

