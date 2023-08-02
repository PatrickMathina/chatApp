package com.patsofts.chatapp.Fragments;

import com.patsofts.chatapp.Notification.MyResponse;
import com.patsofts.chatapp.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface API_svs {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA9Mqz3h4:APA91bHuoGCutvK_gRvo_EwVhFo5Lop9zJJozIZUyWY4oSidA2JAa3cfthwy8XJNavoB9HUShE7_oKHHCWsjsEcdnaFoyN7PnynEq8nBt_6Q1XZkVDb_V2EnlNtZfsxrpsr71gbiB1-6"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
