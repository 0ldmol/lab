// 包名必须是"com.example.layoutlab2"，与Manifest中的package一致
package com.example.layoutlab2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class LinearActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear); // 关联线性布局
    }
}