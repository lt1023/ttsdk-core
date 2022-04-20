package com.linktower.games;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.linktower.R;

public class Protocol extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol);
        Button btn = (Button) findViewById(R.id.but_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(intent);
                finish();
//                System.exit(0);
            }
        });
    }
}
