package com.example.translator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String API_KEY = "trnsl.1.1.20161226T142450Z.43f9510c630c5335.b75c313efc551c5ce37533bd391d70af53e34ec7";
    private static final String PATH = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=";

    private static int language_count = 0;

    private final static String TAG = "log";
    MyAsynkTask myAsynkTask;

    private final String FILE_NAME = "slova.txt";

    private String select_language = "en-uk";

    EditText text_input;
    EditText text_output;
    Button btn_go;
    Button btn;
    ImageButton btn_change;
    TextView txt_one_lang;
    TextView txt_second_lang;

    private static int empt_req = 0;

    private String str_1 = "";

    private int error_conn = 0;

    private final String NOT_TRANSLATE = "nottr";
    private final String NOT_CONNECT = "notconnect";
    private final String FINDING = "finding";
    private final String FILED_EMPTY = "field_empty";

    private String nameText = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitView();
    }

    private void InitView() {
        text_input = (EditText) findViewById(R.id.input_text);
        text_output = (EditText) findViewById(R.id.output_text);
        btn_go = (Button) findViewById(R.id.button_translate);
        btn = (Button) findViewById(R.id.btn_offline);
        btn_change = (ImageButton) findViewById(R.id.btn_change);
        txt_one_lang = (TextView) findViewById(R.id.text_one);
        txt_second_lang = (TextView) findViewById(R.id.text_two);
    }


    @Override
    protected void onResume() {
        super.onResume();
        btn_go.setOnClickListener(this);
        btn.setOnClickListener(this);
        btn_change.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        String in_text = "";
        in_text = text_input.getText().toString();
        switch (v.getId()) {
            case R.id.button_translate:
                if(in_text.equals("")){
                    showMsg(FILED_EMPTY);
                }else {
                    AsynkTaskNetwok taskNetwok = new AsynkTaskNetwok();
                    taskNetwok.execute(in_text, select_language);
                }
                break;
            case R.id.btn_offline:
                if(in_text.equals("")){
                    showMsg(FILED_EMPTY);
                }else {
                    myAsynkTask = new MyAsynkTask();
                    myAsynkTask.execute(FILE_NAME, in_text);
                }
                break;
            case R.id.btn_change:
                changeLanguage();
                break;
            default:
                break;
        }

    }

    private void changeLanguage() {
        if (language_count == 0) {
            txt_one_lang.setText(getResources().getString(R.string.ukrainian_one));
            txt_second_lang.setText(getResources().getString(R.string.english_one));
            select_language = "uk-en";
            language_count = 1;
        } else if (language_count == 1) {
            txt_one_lang.setText(getResources().getString(R.string.english_one));
            txt_second_lang.setText(getResources().getString(R.string.ukrainian_one));
            select_language = "en-uk";
            language_count = 0;
        }
    }


    private String getTranslateFromJson(String str) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(str);
        StringBuilder builder = new StringBuilder();
        JSONArray array = (JSONArray) object.get("text");
        for (Object s : array) {
            builder.append(s.toString() + "\n");
        }
        return builder.toString();
    }

    class AsynkTaskNetwok extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMsg(FINDING);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String requestUrl = PATH + API_KEY + "&text=" + URLEncoder.encode(params[0], "UTF-8") + "&lang=" + params[1];
                URL url = new URL(requestUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                Log.d(TAG, "work");
                int rc = httpURLConnection.getResponseCode();
                if (rc == 200) {
                    String line = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                        Log.d(TAG, line + "\n");
                    }
                    str_1 = getTranslateFromJson(stringBuilder.toString());
                } else {
                    Log.d(TAG, "response code = " + rc);
                }
            } catch (Exception exc) {
                Log.d(TAG, "error to translate");
                error_conn = 1;
            }
            return str_1;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "receive " + str_1);
            text_output.setText(s);
            showMsg(NOT_CONNECT);
        }

    }

    class MyAsynkTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";

            String[] partstr = null;

            String str_second = "";
            AssetManager assetManager = getAssets();
            StringBuilder stringBuilder = null;
            try {
                InputStream inputStream = assetManager.open(params[0]);
                Log.d(TAG, "params 0 = " + params[0]);
                Log.d(TAG, "params 1 = " + params[1] + "\nthis input user to translate");
                int i = 0;
                empt_req = 0;
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    partstr = line.split("\t");
                    if (language_count == 0) {
                        if (partstr[0].equals(params[1])) {
                            Log.d(TAG, "equals + " + partstr[0]);
                            str_second = str_second + " - " + partstr[2] + "\n";
                            empt_req = 1;
                        }
                    }
                    if (language_count == 1) {
                        if (partstr[2].equals(params[1])) {
                            Log.d(TAG, "equals + " + partstr[2]);
                            str_second = str_second + " - " + partstr[0] + "\n";
                            empt_req = 1;
                        }
                    }
                }
                Log.d(TAG, "all is good");

            } catch (IOException exc) {
                Log.d(TAG, "error to IO");
            }
            return str_second;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "find " + s);
            text_output.setText(s);
            showMsg(NOT_TRANSLATE);
        }

    }

    private void showMsg(String s) {
        if (s.equals(NOT_TRANSLATE)) {
            if (empt_req == 0) {
                Toast tst = Toast.makeText(this, "Переклад слова не знайдено", Toast.LENGTH_SHORT);
                tst.setGravity(Gravity.CENTER, 0, 0);
                tst.show();
            }
        } else if (s.equals(FINDING)) {
            Toast tst = Toast.makeText(this, "Виконується переклад", Toast.LENGTH_SHORT);
            tst.setGravity(Gravity.CENTER, 0, 0);
            tst.show();
        } else if (s.equals(NOT_CONNECT)) {
            if (error_conn == 1) {
                Toast tst = Toast.makeText(this, "Немає доступу до інтернету", Toast.LENGTH_SHORT);
                tst.setGravity(Gravity.CENTER, 0, 0);
                tst.show();
                error_conn = 0;
            }
        }
        else if(s.equals(FILED_EMPTY)){
            Toast tst = Toast.makeText(this, "Введіть слово для перекладу", Toast.LENGTH_SHORT);
            tst.setGravity(Gravity.CENTER, 0, 0);
            tst.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intent = new Intent(this, About_activity.class);
                startActivity(intent);
                break;
            case R.id.action_exit:
                showDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showDialog();
    }
	//має бути викликаний метод onCreateDialog, який викликається коли виконується функція showDialog
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setMessage("Бажаєте вийти ?")
                .setCancelable(true)
                .setPositiveButton("Так", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setNegativeButton("Ні", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
}
