package com.example.translator;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class About_activity extends Activity implements View.OnClickListener {

    Button btn_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        btn_close = (Button) findViewById(R.id.btn_exit);

    }

    @Override
    protected void onResume() {
        super.onResume();
        btn_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.finish();
    }
}
