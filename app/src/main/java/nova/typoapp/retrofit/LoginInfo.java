package nova.typoapp.retrofit;


import com.google.gson.annotations.SerializedName;

//{"login_msg":"login_success","login_info":{"email":"hh@hh.com","name":"김고","birthday":"1992.5.27"}}
public class LoginInfo{

    @SerializedName("email")
    String email;

    @SerializedName("name")
    String name;

    @SerializedName("birthday")
    String birthday;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }



}
