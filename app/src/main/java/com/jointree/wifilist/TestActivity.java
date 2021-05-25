package com.jointree.wifilist;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jointree.wifilist.Database.Databases;
import com.jointree.wifilist.Entity.Address;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    ArrayList<String> list;
    private TestAdapter testAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        overridePendingTransition(R.anim.vertical_enter,R.anim.none);

        list = new ArrayList<>();

        Databases db = Databases.getDatabases(this);

        db.addressDao().getAllAddress().observe(this, new Observer<List<Address>>() {
            @Override
            public void onChanged(List<Address> addresses) {
                list.add(addresses.toString());

            }

        });

        testAdapter = new TestAdapter(db.addressDao().getAddress());

        RecyclerView recyclerView_test = findViewById(R.id.recyclerview_test);

        recyclerView_test.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_test.setAdapter(testAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isFinishing()){
            overridePendingTransition(R.anim.none,R.anim.vertical_exit);
        }
    }

}
