package com.jointree.wifilist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jointree.wifilist.Entity.Address;

import java.util.ArrayList;
import java.util.HashMap;

public class TestHolder extends RecyclerView.ViewHolder {
    private ArrayList<HashMap<String ,String>> mapArrayList = new ArrayList<>();
    TextView tv_test;

    public TestHolder(@NonNull View itemView) {
        super(itemView);
        tv_test = itemView.findViewById(R.id.tv_test);
    }
    public void setItem(Address address){
        tv_test.setText("Id: "+address.getId()+" 주소: "+address.address );
    }
}
