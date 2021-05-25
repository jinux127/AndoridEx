package com.jointree.wifilist.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.jointree.wifilist.Dao.AddressDao;
import com.jointree.wifilist.Entity.Address;

@Database(entities = {Address.class}, version = 1, exportSchema = false) // 엔티티의 데이터 변경이 있을경우 버전값을 올려주어야함
public abstract class Databases extends RoomDatabase {
    public abstract AddressDao addressDao();

    private static Databases INSTANCE;

    public static Databases getDatabases(final Context context) {
        if (INSTANCE == null){
            synchronized (Databases.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), Databases.class, "Address")
                            .fallbackToDestructiveMigration()// 따로 Migration정의를 해주지 않아도됨, 이전에 생성한 DB 삭제후 새로운 DB만듬, 안할경우 .addMigration(추가되는 필드 or 삭제되는 필드)를 통해 정의 해주어야함
                            .allowMainThreadQueries()//메인쓰레드에서 데이터베이스 접근 허용
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public static void destroyInstance(){
        INSTANCE = null;
    }
}
