package com.zhopa.alina.cannongame;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    public static Drawable spider_target;
    public static Bitmap spider_target_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        AssetManager assetManager = getAssets();
        try (InputStream stream = assetManager.open("spider_1.png")) {
            spider_target = Drawable.createFromStream(stream, "spider_1.png");
            spider_target_bitmap = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            Log.e("Main activity", "error loading spider img : "+e);
        }
*/

    }
}
