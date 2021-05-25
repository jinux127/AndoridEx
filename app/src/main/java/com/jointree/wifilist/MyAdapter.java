package com.jointree.wifilist;

import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<ScanResult> items;

    public MyAdapter(List<ScanResult> items){
        this.items = items;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item , parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private ArrayList<HashMap<String, String>> mArrayList = new ArrayList<>();
        public TextView tvWifiName;

        public MyViewHolder(View itemView){
            super(itemView);
            tvWifiName = itemView.findViewById(R.id.tv_wifiName);
        }
        public void setItem(ScanResult item){
            tvWifiName.setText(item.SSID +"    레벨:"+item.level+"dBm");
        }
    }
}
