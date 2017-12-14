package nova.typoapp.retrofit;

/**
 * Created by Administrator on 2017-12-02.
 */


import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


//Api를 제공해주는 메소드를 정의해놓은 인터페이스다

//get과 post방식에 따라 코드가 달라지는 점을 유의하자.

public interface ApiService {
    //먼저 기준점이 되는 url 주소를 지정한다.
    //이후 annotation 안의 문자열은 ~~/ "annotation" 이런 식으로 들어가고,
    //메소드의 파라미터들은 필드 혹은 쿼리 식으로 들어간다. -> 겟과 포스트에 따라 용어가 달라짐을 주의할 것.


    public static final String API_URL = "http://115.68.231.13/project/android/";

//    public static final String API_URL = "http://jsonplaceholder.typicode.com/";


    //예제 코드들
//    @GET("comments")
//    Call<ResponseBody>getComment(@Query("postId") int postId);
//
//    @POST("comments")
//    Call<ResponseBody>getPostComment(@Query("postId") int postId);


    //게시판 리스트를 불러오는 메소드. 파라미터 필요 없음
    @GET("getFeedList.php")
    Call<ResponseBody> getFeedList();


    //댓글 리스트를 불러오는 메소드
    @FormUrlEncoded
    @POST("getCommentList.php")
    Call<ResponseBody> getCommentList(@Field("feedID") int feedID);


    //포스트 방식으로 글쓰기 해주기. formurlEncoded 와 필드 골뱅이를 주목할 것.
    @FormUrlEncoded
    @POST("writeFeed.php")
    Call<ResponseBody> write(@Field("writer") String writer, @Field("email") String email, @Field("title") String title, @Field("content") String content, @Field("imgUrl") String imgUrl);

    //포스트 방식으로 글 수정
    @FormUrlEncoded
    @POST("editFeed.php")
    Call<ResponseBody> editFeed(@Field("feedID") int FeedID,
                                @Field("writer") String writer,
                                @Field("email") String email,
                                @Field("title") String title,
                                @Field("content") String content,
                                @Field("imgUrl") String imgUrl);

    //포스트 방식으로 댓글 작성
    @FormUrlEncoded
    @POST("writeComment.php")
    Call<ResponseBody> writeComment( @Field("feedID") int feedID, @Field("content") String content);


    //회원가입
    @FormUrlEncoded
    @POST("join.php")
    Call<ResponseBody> joinMember(@Field("u_email") String email, @Field("u_pw") String pw, @Field("u_name") String name, @Field("u_birthday") String birthday);

    //로그인
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResult> loginMember(@Field("u_email") String email, @Field("u_pw") String pw);

    //회원 정보 확인(세션 정보 가져오기)
    //아무것도 날리지 않는다. 인터셉터 객체가 콜 날릴 때 세션ID를 추가해준다.
    @POST("looksess.php")
    Call<LoginInfo> lookSession();


    //뉴스피드 이미지 업로드하기
    @Multipart
    @POST("uploadImage.php")
    Call<ImageUploadResult> uploadImage(@Part MultipartBody.Part image);

    //프로필 이미지 업로드하기
    @Multipart
    @POST("uploadImageProfile_new.php")
    Call<ImageUploadResult> uploadImageProfile(@Part MultipartBody.Part image);

    //게시글 삭제하기
    @FormUrlEncoded
    @POST("deleteFeed.php")
    Call<ResponseBody> deleteFeed(@Field("feedID") int feedID);

}