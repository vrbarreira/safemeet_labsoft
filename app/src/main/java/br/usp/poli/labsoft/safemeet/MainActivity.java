package br.usp.poli.labsoft.safemeet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnCreateEvent, btnSearchEvent, btnTrkPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreateEvent = (Button) findViewById(R.id.btnCreateEvent);
        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
                startActivity(intent);
            }
        });

        btnSearchEvent = (Button) findViewById(R.id.btnSearchEvent);
        btnSearchEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnTrkPerson = (Button) findViewById(R.id.btnTrkPerson);
        btnTrkPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TrackerActivity.class);
                startActivity(intent);
            }
        });
    }
}
