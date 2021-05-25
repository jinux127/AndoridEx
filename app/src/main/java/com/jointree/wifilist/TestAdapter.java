package com.jointree.wifilist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jointree.wifilist.Entity.Address;

import java.util.ArrayList;
import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestHolder> {

    private List<Address> list;

    public TestAdapter(List<Address> list) {
        this.list = list;

    }

    @NonNull
    @Override
    public TestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //뷰홀더 생성
        Context context = parent.getContext();
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        /*
        layoutInflater.inflate(resource, root, attachToRoot)
        resource : View를 만들고 싶은 레이아웃 파일
        root : 생성될 View의 parent 명시
        attachToRoot: true > root의 자식 view 를 자동으로 추가, 이 때 root 는 null이 될수 없음
        */
        View view = layoutInflater.inflate(R.layout.recyclerview_test,parent,false);
        /*
        return : attachToRoot에 따라서 리턴값이 달라진다. true : root , false : XML 내 최상위 뷰가 리턴
        */
        return new TestHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestHolder holder, int position) { //뷰홀더가 재활용 될 때 실행되는 메서드
        holder.setItem(list.get(position));
    }


    @Override
    public int getItemCount() {//아이템 개수 조회
        return list.size();
    }
}
