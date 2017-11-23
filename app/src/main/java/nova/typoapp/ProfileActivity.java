package nova.typoapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {


    @BindView(R.id.profileEmail)
    TextView profileEmail;

    @BindView(R.id.profileName)
    TextView profileName;
    @BindView(R.id.profileBirthday)
    TextView profileBirthday;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);

        String email = pref_login.getString(getString(R.string.cookie_email) , "");
        String name = pref_login.getString(getString(R.string.cookie_name) , "");
        String birthday = pref_login.getString(getString(R.string.cookie_birthday) , "");

        profileEmail.setText(email);
        profileName.setText(name);
        profileBirthday.setText(birthday);

        //,프로필


    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
