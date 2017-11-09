package com.example.jevil.fixer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

// модель данных для преобразования json
class FixerModel {

   @SerializedName("rates")
   @Expose
   private LinkedTreeMap rates;

    LinkedTreeMap getRates() {return rates;}

    @SerializedName("base")
    @Expose
    private String base;

    @SerializedName("date")
    @Expose
    private String date;

}