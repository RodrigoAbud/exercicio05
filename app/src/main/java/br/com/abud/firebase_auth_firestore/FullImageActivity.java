package br.com.abud.firebase_auth_firestore;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class FullImageActivity extends AppCompatActivity {

    private ImageView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        fullImageView = findViewById(R.id.fullImageView);

        byte[] byteArray = getIntent().getByteArrayExtra("figura");
        Bitmap figura = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        fullImageView.setImageBitmap(figura);
    }
}
