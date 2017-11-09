package com.example.jevil.fixer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Controller {
    // базовый URL для запросов
    private static final String BASE_URL = "https://api.fixer.io";

    public static FixerApi getApi() {

        // создаем объект gson для парсинга json объектов, полученных с сайта
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // инициализируем библиотеку Retrofit 2
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))//Конвертер, необходимый для преобразования JSON'а в объекты
                .build();

        FixerApi jsonApi = retrofit.create(FixerApi.class);//Создаем объект, при помощи которого будем выполнять запросы
        return jsonApi;
    }
}