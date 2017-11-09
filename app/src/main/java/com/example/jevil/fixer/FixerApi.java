package com.example.jevil.fixer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FixerApi {

    @GET("/{date}") // интерфейс запросов к API сайта, предоставленный библиотекой Retrofit
    Call<FixerModel> getRates(@Path("date") String date, @Query("base") String currency);
}