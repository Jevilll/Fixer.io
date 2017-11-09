package com.example.jevil.fixer;

// класс для хранения элементов
class Item {
    private String currency;
    private Double value;
    private String date;
    private int id;

    Item(String currency, Double value) {
        this.currency = currency;
        this.value = value;
    }

    Item(int id, String currency, String date) {
        this.id = id;
        this.currency = currency;
        this.date = date;
    }

    String getCurrency() {
        return currency;
    }

    Double getValue() {
        return value;
    }

    int getId() {
        return id;
    }
}
