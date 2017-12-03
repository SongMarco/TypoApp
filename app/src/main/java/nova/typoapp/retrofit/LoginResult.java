package nova.typoapp.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017-12-03.
 */

//{"login_msg":"login_success","login_cookie":{"email":"hh@hh.com","name":"김고","birthday":"1992.5.27"}}

public class LoginResult {

    @SerializedName("login_msg")
    String login_msg;

    public LoginInfo getInfo() {
        return info;
    }

    public void setInfo(LoginInfo info) {
        this.info = info;
    }

    // 중요 @@@@@ 밑에 SerializedName을 붙이지 않으면 JSON을 인식하지 못한다.
    @SerializedName("login_info")
    public LoginInfo info;



    public String getLogin_msg() {
        return login_msg;
    }

    public void setLogin_msg(String login_msg) {
        this.login_msg = login_msg;
    }

}


