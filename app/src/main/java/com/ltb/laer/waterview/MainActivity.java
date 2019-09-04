package com.ltb.laer.waterview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ltb.laer.waterview.listener.WaterClickListener;
import com.ltb.laer.waterview.model.Water;
import com.ltb.laer.waterview.view.WaterView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WaterView mWaterView;

    private List<Water> mWaters = new ArrayList<>();

    {
        for (int i = 0; i < 10; i++) {
            mWaters.add(new Water((int) (i + Math.random() * 4), "item" + i));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWaterView = findViewById(R.id.wv_water);
        mWaterView.setWaters(mWaters);
        mWaterView.setCallBack(new WaterClickListener() {
            @Override
            public void onWaterClick(Water water) {
                Toast.makeText(MainActivity.this, "当前点击的是：" + water.getName() + "水滴的值是:"
                        + water.getNumber(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onRest(View view) {
        mWaterView.setWaters(mWaters);
    }
}
