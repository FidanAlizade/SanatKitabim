package com.example.sanatkitabim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.sanatkitabim.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
private ActivityMainBinding binding;
ArrayList<Art> artArrayList;
SQLiteDatabase database;
ArtAdapter artAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        artArrayList = new ArrayList<Art>();



        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter = new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);


        getData();
    }

    public void getData(){
        try {

            database = this.openOrCreateDatabase("ArtBook",MODE_PRIVATE,null);
          Cursor cursor = database.rawQuery("SELECT * FROM art", null);
          int nameIx = cursor.getColumnIndex("artname");
          int idIx = cursor.getColumnIndex("id");

          while (cursor.moveToNext()){
              String name = cursor.getString(nameIx);
              int id = cursor.getInt(idIx);
              //art javayi olustur
              Art art = new Art(name,id);
              artArrayList.add(art);
          }

            artAdapter.notifyDataSetChanged();
            cursor.close();

          //sonra ArtAdapteri yarat arrayListi recyclerView a baglamaq icin
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.art_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_art){
            Intent intent = new Intent(MainActivity.this,ArtActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}