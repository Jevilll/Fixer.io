package com.example.jevil.fixer;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Calendar date = Calendar.getInstance();
    String dateString;
    Button btnDate;
    Spinner spinner;
    RecyclerView rv;
    String currency = "USD"; // значение спиннера по умолчанию
    String dateFormat = getDateFormat(date);
    ListAdapter mainAdapter;
    ProgressBar pb;
    TextView tvError;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    boolean isLoad = false;

    List<Item> items = new ArrayList<>();
    ArrayList<Item> savesList;
    ArrayList<String> arrayListForView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // инициализируем элементы
        spinner = (Spinner) findViewById(R.id.spinner);
        btnDate = (Button) findViewById(R.id.button);
        pb = (ProgressBar) findViewById(R.id.pb);
        tvError = (TextView) findViewById(R.id.tvError);

        // настраиваем recycleView
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // настраиваем адаптер для spinner
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.currencyList, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // вызываем адаптер
        spinner.setAdapter(adapter);
        // добавляем слушатель
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {
                String[] choose = getResources().getStringArray(R.array.currencyList);
                // сохраняем в глобальную переменную валюту
                currency = choose[selectedItemPosition];
                // при загрузке списка оффлайн, устанавливая значение спиннера, вызывается onItemSelected (не должен, обрабатываем)
                if (!isLoad) makeRequest();
                isLoad = false;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // вызываем диалоговое окно выбора даты
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MainActivity.this, datePickerDialog,
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
        // сохраняем выбранную дату
        setInitialDateTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.sort_alphabet: //сортируем по алфавиту
                sortAlphabet();
                return true;
            case R.id.sort_value: // по значению
                sortValue();
                return true;
            case R.id.action_save: // сохраняем
                save();
                return true;
            case R.id.action_load: // загружаем
                openSaves();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // установка даты и времени на текст кнопки
    private void setInitialDateTime() {
        dateString = DateUtils.formatDateTime(this,
                date.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
        btnDate.setText(dateString); // записываем дату в текст кнопки (при первом запуске выставляется текущая дата)
        dateFormat = getDateFormat(date);
    }

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener datePickerDialog = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, monthOfYear);
            date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
            makeRequest();
        }
    };

    public void makeRequest() {
        // контролируем отображение элементов при ошибке соединения
        tvError.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
        items.clear();

        // основная работа по загрузке данных, используя библиотеку Retrofit 2
        FixerApi jsonApi = Controller.getApi(); // создаем запрос
        Call<FixerModel> call = jsonApi.getRates(dateFormat, currency);
        call.enqueue(new Callback<FixerModel>() {
            @Override
            public void onResponse(Call<FixerModel> call, Response<FixerModel> response) {
                // при успешном сооединении получаем данные
                LinkedTreeMap<String, Double> l = response.body().getRates();
                // конвертируем LinkedTreeMap в List
                for (Map.Entry<String, Double> entry : l.entrySet())
                    items.add(new Item(entry.getKey(), entry.getValue()));
                refreshList(); // выводим полученные данные
            }

            @Override
            public void onFailure(Call<FixerModel> call, Throwable t) {
                // действия при ошибке соединения
                pb.setVisibility(View.GONE);
                rv.setVisibility(View.GONE);
                tvError.setVisibility(View.VISIBLE);
                Snackbar snackbar = Snackbar.make(
                        rv,
                        "Ошибка соединения",
                        Snackbar.LENGTH_LONG
                );
                snackbar.setAction("Повторить", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeRequest();
                    }
                });
                snackbar.show();
            }
        });
    }

    // получем дату в формате для обращения к API ресурса
    String getDateFormat(Calendar calendar) {
        if (calendar.get(Calendar.DAY_OF_MONTH) < 10)
            return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-0" + calendar.get(Calendar.DAY_OF_MONTH);
        else
            return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    // сортировка по алфавиту
    public void sortAlphabet() {
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getCurrency().compareTo(o2.getCurrency());
            }
        });
        refreshList();
    }

    // сортировка по значению
    public void sortValue() {
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        refreshList();
    }

    // обновляем данные в recycleView
    public void refreshList() {
        mainAdapter = new ListAdapter(items);
        rv.setAdapter(mainAdapter);
        rv.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
    }

    // сохраняем в БД
    public void save() {
        getSaves(); // получаем существующие сохраненные записи
        if (arrayListForView.contains(currency + " за " + dateString)) { // проверяем на совпадение с уже существующими
            Snackbar snackbar = Snackbar.make(
                    rv,
                    "Такая запись уже есть",
                    Snackbar.LENGTH_SHORT
            );
            snackbar.show();
        } else if (items.size() != 0) { // если список не пустой, сохраняем
            ContentValues contentValues = new ContentValues(); // добавляем строки в таблицу (ключ - значение)
            contentValues.put(DBHelper.KEY_CURRENCY, currency);
            contentValues.put(DBHelper.KEY_DATE, dateString);

            StringBuilder builder = new StringBuilder(); // преобразуем List в одну строку путем добавления разделителей, для удобного хранения в БД
            for (Item item : items) {
                builder.append(item.getCurrency()).append(":").append(item.getValue()).append(",");
            }

            contentValues.put(DBHelper.KEY_VALUE, builder.toString());

            // создаем экземпляры БД
            dbHelper = new DBHelper(this);
            db = dbHelper.getWritableDatabase();
            db.insert(DBHelper.TABLE_SAVE, null, contentValues); //добавление данных в БД

            // закрываем
            dbHelper.close();
            db.close();

            Snackbar snackbar = Snackbar.make(
                    rv,
                    "Сохранено",
                    Snackbar.LENGTH_SHORT
            );
            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar.make(
                    rv,
                    "Нет данных",
                    Snackbar.LENGTH_SHORT
            );
            snackbar.show();
        }
    }

    // показываем диалоговое окно с выбором сохраненных записей
    public void openSaves() {
        getSaves();
        String[] arrayForView = arrayListForView.toArray(new String[arrayListForView.size()]);
        if (arrayForView.length != 0) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this);
            builder.setTitle("Загрузить");
            builder.setNegativeButton("Закрыть", null);
            builder.setItems(arrayForView, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    load(savesList.get(which).getId());
                }
            });
            builder.show();
        } else { // в случае если сохраненных записей нет
            Snackbar snackbar = Snackbar.make(
                    rv,
                    "Нет сохраненных записей",
                    Snackbar.LENGTH_SHORT
            );
            snackbar.show();
        }
    }

    public void getSaves() {
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        savesList = new ArrayList<>();
        String[] columns = new String[]{DBHelper.KEY_ID, DBHelper.KEY_CURRENCY, DBHelper.KEY_DATE};

        arrayListForView = new ArrayList<>();

        Cursor c = db.query(DBHelper.TABLE_SAVE, columns, null, null, null, null, null);
        if (c.moveToLast()) {
            do
            { // получаем из БД данные, разделенные на два списка, один для заполненя адаптера списка диалога, второй хранит id этих элементов в БД
                arrayListForView.add(c.getString(c.getColumnIndex(DBHelper.KEY_CURRENCY)) + " за " + c.getString(c.getColumnIndex(DBHelper.KEY_DATE)));
                savesList.add(new Item(c.getInt(c.getColumnIndex(DBHelper.KEY_ID)),
                        c.getString(c.getColumnIndex(DBHelper.KEY_CURRENCY)),
                        c.getString(c.getColumnIndex(DBHelper.KEY_DATE))));
                // Переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToPrevious());
        }
        c.close();
        dbHelper.close();
        db.close();
    }

    // загружаем оффлайн
    public void load(int id) {
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        Cursor c = db.query(DBHelper.TABLE_SAVE, null, "_id" + " = " + id, null, null, null, null);
        c.moveToFirst();

        // чтобы не срабатывал onItemSelected у спиннера лишний раз
        if (!c.getString(c.getColumnIndex(DBHelper.KEY_CURRENCY)).equals(currency)) {
            isLoad = true;
        }
        currency = c.getString(c.getColumnIndex(DBHelper.KEY_CURRENCY));
        String[] choose = getResources().getStringArray(R.array.currencyList);
        int elemIndex = Arrays.binarySearch(choose, currency);
        if (elemIndex == -31) { // особенность бинарного поиска в массиве, нулевой элемент возвращается как количество элементов с отрицательным значением (так и не понял почему так происходит)
            spinner.setSelection(0);
        } else {
            spinner.setSelection(elemIndex);
        }
        dateString = c.getString(c.getColumnIndex(DBHelper.KEY_DATE));
        btnDate.setText(dateString);

        items.clear();

        // преобразуем данные из БД в формат для загрузки в адаптер
        String[] splitArray = c.getString(c.getColumnIndex(DBHelper.KEY_VALUE)).split(",");
        for (String s : splitArray) {
            String[] finalSplit = s.split(":");
            items.add(new Item(finalSplit[0], Double.valueOf(finalSplit[1])));
        }

        refreshList();
        Snackbar snackbar = Snackbar.make(
                rv,
                "Загружено",
                Snackbar.LENGTH_SHORT
        );
        snackbar.show();
    }

}
