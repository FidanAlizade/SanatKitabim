package com.example.sanatkitabim;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.sanatkitabim.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    private ActivityArtBinding binding;
    Bitmap selectedImage;
    SQLiteDatabase sqLiteDatabase;
    String artName;
    String painterName;
    String year;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();
        sqLiteDatabase = this.openOrCreateDatabase("ArtBook",MODE_PRIVATE,null);



        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if(info.matches("new")){
//new art ise temizliyirik sehifeni sadece
            binding.artName.setText("");
            binding.painterName.setText("");
            binding.year.setText("");

            binding.saveButton.setVisibility(View.VISIBLE);

           // Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
        //    binding.imageView.setImageBitmap(selectImage);

            binding.imageView.setImageResource(R.drawable.selectimage);

        }else{
            //OLD-DURSA
            binding.saveButton.setVisibility(View.INVISIBLE);
         //olsa bana bir id yollandi o id deki degeri cekelim
            int artId = intent.getIntExtra("artId",0);

            try {
                //cekme islemi

                Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM art WHERE id =?", new String [] {String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("yeartext");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.artName.setText(cursor.getString(artNameIx));
                    binding.painterName.setText(cursor.getString(painterNameIx));
                    binding.year.setText(cursor.getString(yearIx));


                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void selectimage(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //izin isteme mantigi gosterilsin Snacbarda
                Snackbar.make(view,"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //izin istenilsin butonuna tiklandiqda
                        //request permission
                        //permiision launcheri
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                //promoy request permission
                //permiision launcheri
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            //go to gallery
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //activitylauncheri intenti alacaq gallerye aparacaq
            activityResultLauncher.launch(intentToGallery);

        }
    }


    public void save(View view){
//SAVE edende etmezden once bu sekli balacalasdirsin:
        artName = binding.artName.getText().toString();
        painterName = binding.painterName.getText().toString();
        year = binding.year.getText().toString();
        //imageni metodla balacalasdir
        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        //ByteArrayOutputStream sinifinden obje yaradiriq ki, smallImageni byte massivine qoya bilsin
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //bunun ucun ilk once sekli compress edirik, pngye cevirik
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);

        //byte massivi yaradiriq. outputStream metodu ile byte dizisine pngye cevirilmis sekli qoyuruq
        byte[] byteArray = outputStream.toByteArray();
// indi ise bu verileri sqlde saxlayaq


        try {
            sqLiteDatabase = this.openOrCreateDatabase("ArtBook",MODE_PRIVATE,null);
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS art (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, yeartext VARCHAR, image BLOB)");
            String sqlString = "INSERT INTO art (artname, paintername, yeartext, image) VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }
        //save edib bitdikden sonra bunlari etsin
        Intent intent = new Intent(ArtActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width/ (float) height;
             if(bitmapRatio>1){
                 width = maximumSize;
                 height = (int) (width / bitmapRatio);
             }else{
                 height = maximumSize;
                 width = (int) (height * bitmapRatio);
             }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    public void registerLauncher(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                //kontrol et intent alinmis mi
                //if intent resultu varsa
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent intentFromResult = result.getData(); //burdaki getData() gallery e aparan intentin putextrasini verir
                    if(intentFromResult !=null){
                        Uri imageData = intentFromResult.getData();// resmin datasini aliyor
                     //   binding.imageView.setImageURI(imageData); eger sadece kullaniciya gosterei olsaydiq bu usulla da etmek bes ederdi ama sql e yaramir
                        try {
                            if(Build.VERSION.SDK_INT >=28){
                                //decoder sinifina diyoruz ki uri olan image datayi al
                                ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(),imageData);
                                //bitmapa cevir
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                //kullaniciya goster
                                binding.imageView.setImageBitmap(selectedImage);
                            }else{
                                selectedImage = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                //kontrol et izin verilmis mi
                if(result){
                    // verilmisse go to gallery
                    //MediaStore.Images.Media.EXTERNAL_CONTENT_URI bu intentin yolu
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    //activityResultLauncher i kullan
                    activityResultLauncher.launch(intentToGallery);
                }else{
                    // verilmemisse Toast yazdir
                    Toast.makeText(ArtActivity.this,"Permission needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}