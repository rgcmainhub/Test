package com.music.rgc.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        dataManager = new DataManager(this);

        Button btnExport = findViewById(R.id.btnExportBackup);
        if (btnExport != null) {
            btnExport.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    String json = dataManager.exportBackupJson();
                    Toast.makeText(SettingsActivity.this, "Backup generated successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}