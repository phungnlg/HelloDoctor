package com.phungnlg.hellodoctor.adapter;

import com.phungnlg.hellodoctor.CommentItem;
import com.phungnlg.hellodoctor.NewsItem;
import com.phungnlg.hellodoctor.NotificationItem;
import com.phungnlg.hellodoctor.object.DoctorProfile;
import com.phungnlg.hellodoctor.object.UserInfo;

import java.util.LinkedHashMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Phil on 7/11/2017.
 */

public interface IApiService {
    @GET("Notifications/{id}.json")
    Call<LinkedHashMap<String, NotificationItem>> getNotification(@Path("id") String id);

    @GET("User/{id}.json")
    Observable<UserInfo> getUserInfo(@Path("id") String id);

    @GET("News.json")
    Observable<LinkedHashMap<String, NewsItem>> getNewsList();

    @GET("Comments/{id}.json")
    Observable<LinkedHashMap<String, CommentItem>> getComments(@Path("id") String id);

    @GET("Profile/{id}.json")
    Observable<DoctorProfile> getDoctorProfile(@Path("id") String id);
}
