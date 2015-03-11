package fyp.leungww.exsplit;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.facebook.model.GraphUser;

import java.util.List;

public class TravellerDBAdapter {
    private TravellerDBHelper travellerDBHelper;

    public TravellerDBAdapter(Context context){
        this.travellerDBHelper = new TravellerDBHelper(context);
    }

    public long insert(String name, String fbUserId){
        long id;
        Traveller traveller = getTraveller(fbUserId);
        if(traveller == null){
            SQLiteDatabase db = travellerDBHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(TravellerDBHelper.NAME, name);
            contentValues.put(TravellerDBHelper.FB_USERID, fbUserId);
            id = db.insert(TravellerDBHelper.TABLE_NAME,null,contentValues);
            db.close();
        }else{
            id = traveller.get_id();
        }
        return id;
    }

   /*public void insertAll(List<GraphUser> travellers){
        SQLiteDatabase db = travellerDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
           for (GraphUser traveller:travellers){
                ContentValues contentValues = new ContentValues();
                contentValues.put(TravellerDBHelper.NAME, traveller.getName());
                contentValues.put(TravellerDBHelper.FB_USERID, traveller.getId());
                long id = db.insert(TravellerDBHelper.TABLE_NAME, null, contentValues);
           }
           db.setTransactionSuccessful();
        } finally {
           db.endTransaction();
        }
   }*/

    public Traveller getTraveller(long _id){
        SQLiteDatabase db = travellerDBHelper.getWritableDatabase();
        String[] columns = {TravellerDBHelper.NAME, TravellerDBHelper.FB_USERID};
        String selection = TravellerDBHelper.PRIMARY_KEY+" =?";
        String[] selectionArgs = {_id+""};
        Cursor cursor = db.query(TravellerDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        Traveller traveller = null;
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(TravellerDBHelper.NAME);
            String name = cursor.getString(index1);
            int index2 = cursor.getColumnIndex(TravellerDBHelper.FB_USERID);
            String fbUserId = cursor.getString(index2);
            traveller = new Traveller(_id, name, fbUserId);
        }
        cursor.close();
        db.close();
        return traveller;
    }

    public Traveller getTraveller(String fbUserId){
        SQLiteDatabase db = travellerDBHelper.getWritableDatabase();
        String[] columns = {TravellerDBHelper.PRIMARY_KEY, TravellerDBHelper.NAME};
        String selection = TravellerDBHelper.FB_USERID+" =?";
        String[] selectionArgs = {fbUserId};
        Cursor cursor = db.query(TravellerDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        Traveller traveller = null;
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(TravellerDBHelper.PRIMARY_KEY);
            long _id = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(TravellerDBHelper.NAME);
            String name = cursor.getString(index2);
            traveller = new Traveller(_id, name, fbUserId);
        }
        cursor.close();
        db.close();
        return traveller;
    }

    public String getAll(){
        SQLiteDatabase db = travellerDBHelper.getWritableDatabase();
        String[] columns = {TravellerDBHelper.PRIMARY_KEY, TravellerDBHelper.NAME, TravellerDBHelper.FB_USERID};
        Cursor cursor = db.query(TravellerDBHelper.TABLE_NAME, columns,null,null,null,null,null);
        StringBuffer travellers = new StringBuffer();
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(TravellerDBHelper.PRIMARY_KEY);
            long _id = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(TravellerDBHelper.NAME);
            String name = cursor.getString(index2);
            int index3 = cursor.getColumnIndex(TravellerDBHelper.FB_USERID);
            String fbUserId = cursor.getString(index3);
            travellers.append(_id+" "+name+" "+fbUserId);
        }
        cursor.close();
        db.close();
        return travellers.toString();
    }

    static class TravellerDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="traveller.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="Traveller";
        private static final String PRIMARY_KEY = "_id";
        private static final String NAME = "name";
        private static final String FB_USERID = "fbUserId";

        public TravellerDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+NAME+" TEXT, "+FB_USERID+" TEXT);";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String dropTable = "DROP TABLE IF EXISTS "+TABLE_NAME+";";
            db.execSQL(dropTable);
            onCreate(db);
        }
    }
}
