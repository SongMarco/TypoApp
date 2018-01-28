package nova.typoapp.retrofit;

/**
 * Created by Administrator on 2017-12-02.
 */


import java.util.ArrayList;

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


    //게시판 리스트를 불러오는 메소드. 파라미터 필요 없음
    @GET("getMyFeed.php")
    Call<ResponseBody> getMyFeed();


    //  무한 스크롤을 할 때, 영단어 게시물 아이템을 더 가져오는 메소드
    // 마지막 리스트에서 보인 아이템의 피드번호를 서버로 보낸다.
    @FormUrlEncoded
    @POST("getMoreFeed.php")
    Call<ResponseBody> getMoreFeed(@Field("lastFeedNum") int lastFeedNum);







    //댓글 리스트를 불러오는 메소드
    @FormUrlEncoded
    @POST("getCommentList.php")
    Call<ResponseBody> getCommentList(@Field("feedID") int feedID);

    //대댓글 리스트를 불러오는 메소드
    @FormUrlEncoded
    @POST("getSubCommentList.php")
    Call<ResponseBody> getSubCommentList(@Field("commentID") int commentID);

    //게시물을 검색하는 메소드
    @FormUrlEncoded
    @POST("searchFeed.php")
    Call<ResponseBody> searchFeed(@Field("searchWord") String searchWord);


    //게시물에 좋아요를 한 사람의 리스트를 불러오는 메소드
    @FormUrlEncoded
    @POST("getLikeList.php")
    Call<ResponseBody> getLikeList(@Field("feedID") int feedID);



    //단어장 모음 페이지로 갔을 때 단어장 리스트를 가져오는 메소드
    //세션 정보를 기준으로 세트 정보를 가져올 수 있으므로, 파라미터를 세팅하지 않아도 ok

    @POST("wordset/getWordSetList.php")
    Call<ResponseBody> getWordSetList();

    //그룹에서 단어장 리스트를 가져오는 메소드
    //그룹 id를 서버에 보내어, 그룹에 속한 단어장을 가져온다.
    @FormUrlEncoded
    @POST("group/getGroupWordSet.php")
    Call<ResponseBody> getGroupWordSet(@Field("idGroup") int idGroup);



    //단어장에 단어들을 세팅하는 메소드
    //서버에 단어장 id를 보내고, 단어-단어장 db 에서 단어들을 가져온다.
    @FormUrlEncoded
    @POST("wordset/getWordListFromSet.php")
    Call<ResponseBody> getWordListFromSet(@Field("idWordSet") int idWordSet);



    //단어장을 추가하는 메소드
    //단어장 제목
    @FormUrlEncoded
    @POST("wordset/addWordSet.php")
    Call<ResponseBody> addWordSet(@Field("nameWordSet") String nameWordSet);


    //단어장을 그룹에 추가하는 메소드
    //단어장 제목을 서버로 보낸다.
    @FormUrlEncoded
    @POST("group/addGroupWordSet.php")
    Call<ResponseBody> addGroupWordSet(@Field("nameWordSet") String nameWordSet , @Field("idGroup") int idGroup);



    //그룹을 추가하는 메소드
    @FormUrlEncoded
    @POST("group/addGroup.php")
    Call<ResponseBody> addGroup(@Field("nameGroup") String nameGroup, @Field("contentGroup") String contentGroup, @Field("imgUrlGroup") String imgUrlGroup );

    //그룹 리스트를 가져오는 메소드
    //그룹 탭을 눌렀을 때 / 혹은 그룹을 추가하여 새로고침이 필요할 때 사용
    //세션을 통해 그룹의 정보를 가져올 수 있으므로, 파라미터를 세팅하지 않아도 ok
    @POST("group/getGroupList.php")
    Call<ResponseBody> getGroupList();


    //단어 게시물을 단어장에 추가하는 메소드
    @FormUrlEncoded
    @POST("wordset/addWordToSet.php")
    Call<ResponseBody> addFeedToSet(@Field("nameWordSet") String nameWordSet, @Field("nameWord") String nameWord, @Field("idWord") int idWord );



    // 문자 인식으로 단어장을 만들 때, 인식한 문자 리스트로 단어장을 세팅하는 메소드
    @FormUrlEncoded
    @POST("wordset/checkWordExistInServer.php")
    Call<ResponseBody> checkWordExistInServer(@Field("listWord[]") ArrayList<String> listWord );



    // 서버에 Ocr 단어장 리스트를 업로드하는 메소드
    @FormUrlEncoded
    @POST("wordset/uploadOcrSet.php")
    Call<ResponseBody> uploadOcrSet(@Field("listWord[]") ArrayList<String> listWord );



    //////////////////////////////////////////////////////////


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

    //포스트 방식으로 댓 수정
    @FormUrlEncoded
    @POST("editComment.php")
    Call<ResponseBody> editComment(@Field("commentID") int commentID, @Field("content") String content);


    //포스트 방식으로 댓글 작성
    @FormUrlEncoded
    @POST("writeComment.php")
    Call<ResponseBody> writeComment(@Field("feedID") int feedID, @Field("content") String content);

    //포스트 방식으로 대댓글 작성
    @FormUrlEncoded
    @POST("writeSubComment.php")
    Call<ResponseBody> writeSubComment(@Field("commentID") int commentID, @Field("content") String content);

    //회원가입
    @FormUrlEncoded
    @POST("join.php")
    Call<ResponseBody> joinMember(@Field("u_email") String email, @Field("u_pw") String pw, @Field("u_name") String name, @Field("u_birthday") String birthday);

    //로그인
    // 이메일과 암호화된 비밀번호, 기기 토큰을 전송한다.
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResult> loginMember(@Field("u_email") String email, @Field("u_pw") String pw, @Field("Token") String Token);

    //회원 정보 확인(세션 정보 가져오기)
    //메소드상으로는 아무것도 넣지 않는다. 인터셉터 객체가 콜 날릴 때 세션ID를 추가해준다.
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


    //댓글 삭제하기
    @FormUrlEncoded
    @POST("deleteComment.php")
    Call<ResponseBody> deleteComment(@Field("commentID") int commentID);

    //대댓글 삭제하기.
    //보내는 변수는 댓글 삭제와 같다.
    //DB상에서 둘 다 commentID를 id값으로 사용하기 때문이다.
    //대댓글은 depth 와, subcomment_ comment_ id 변수를 가진다. (댓글 DB(table_comment) 참조)
    @FormUrlEncoded
    @POST("deleteSubComment.php")
    Call<ResponseBody> deleteSubComment(@Field("commentID") int commentID);


    //단어장 삭제하기
    @FormUrlEncoded
    @POST("wordset/deleteWordSet.php")
    Call<ResponseBody> deleteWordSet(@Field("setId") int setId);

    //단어장 내부의 단어 삭제하기
    @FormUrlEncoded
    @POST("wordset/deleteWordInSet.php")
    Call<ResponseBody> deleteWordInSet(@Field("idWord") int idWord , @Field("idWordSet") int idWordSet );

    //단어장 단어장 수정하기
    @FormUrlEncoded
    @POST("wordset/editWordSet.php")
    Call<ResponseBody> editWordSet(@Field("setId") int setId,@Field("nameWordSet") String nameWordSet       );


    //게시글에 좋아요 적용하기(이미 좋아요 적용되있으면 좋아요 해제, 좋아하지 않으면 좋아요 적용)
    @FormUrlEncoded
    @POST("likeFeed.php")
    Call<ResponseBody> likeFeed(@Field("feedID") int feedID, @Field("type") String type);


    // 서버에서 댓글 하나에 대한 정보를 가져오는 메소드
    // 알림에서 답글 액티비티로 접근시 사용
    @FormUrlEncoded
    @POST("getCommentInfo.php")
    Call<ResponseBody> getCommentInfo (@Field("commentID") int commentID );


    // 서버에서 게시물 하나에 대한 정보를 가져오는 메소드
    // 알림에서 답글 액티비티로 접근시 사용
    @FormUrlEncoded
    @POST("getFeedInfo.php")
    Call<ResponseBody> getFeedInfo (@Field("feedID") int feedID );



    // 알림페이지에 표시하기 위한 알림 목록을 서버에서 가져오는 메소드
    // 세션 id값만 인터셉터에서 보내주면 오케이. 파라미터 필요 없음

    @POST("notice/getNoticeList.php")
    Call<ResponseBody> getNoticeList();







    ////////////////////// fcm 관련 코드
    //FCM 토큰을 서버에 보내기 위한 메소드
    @FormUrlEncoded
    @POST("fcm/registerToken.php")
    Call<ResponseBody> registerToken(@Field("Token") String token);

    // 게시물(Feed)에 댓글이 달릴 때(Comment) fcm 메세지를 보내는 메소드
    // 세션에서 계정 정보를 꺼내어 메시지에 담는다.
    @FormUrlEncoded
    @POST("fcm/fcmSendMessageWhenCommentFeed.php")
    Call<ResponseBody> fcmSendMessageWhenCommentFeed(@Field("wordName") String wordName, @Field("emailFeedWriter") String emailFeedWriter, @Field("feedID") int feedID);


    // 댓글(Comment)에 답글이 달릴 때(Reply) fcm 메세지를 보내는 메소드
    // 세션에서 계정 정보를 꺼내어 메시지에 담는다.
    @FormUrlEncoded
    @POST("fcm/fcmSendMessageWhenReplyComment.php")
    Call<ResponseBody> fcmSendMessageWhenReplyComment (@Field("emailCommentWriter") String emailCommentWriter, @Field("feedID") int feedID ,@Field("commentID") int commentID );


}