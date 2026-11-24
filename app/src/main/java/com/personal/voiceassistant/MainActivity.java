package com.personal.voiceassistant;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 60, 50, 60);

        TextView title = new TextView(this);
        title.setText("পার্সোনাল ভয়েস অ্যাসিস্ট্যান্ট");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 40);
        layout.addView(title);

        SharedPreferences prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);

        Switch masterSwitch = new Switch(this);
        masterSwitch.setText("মাস্টার সুইচ (সব সিস্টেম অন/অফ)");
        masterSwitch.setTextSize(18);
        masterSwitch.setChecked(prefs.getBoolean("master_switch", false));
        masterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("master_switch", isChecked).apply();
            Toast.makeText(this, isChecked ? "চালু হয়েছে" : "বন্ধ হয়েছে", Toast.LENGTH_SHORT).show();
        });
        layout.addView(masterSwitch);

        Button permBtn = new Button(this);
        permBtn.setText("প্রয়োজনীয় সব পারমিশন দিন");
        permBtn.setOnClickListener(v -> requestAllPermissions());
        layout.addView(permBtn);

        setContentView(layout);
    }

    private void requestAllPermissions() {
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.RECEIVE_SMS);
        perms.add(Manifest.permission.READ_SMS);
        perms.add(Manifest.permission.READ_PHONE_STATE);
        perms.add(Manifest.permission.CALL_PHONE);
        perms.add(Manifest.permission.READ_CONTACTS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        ActivityCompat.requestPermissions(this, perms.toArray(new String), 101);
    }
}
