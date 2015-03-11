package fyp.leungww.exsplit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripDBAdapter {
    private TripDBHelper tripDBHelper;

    public TripDBAdapter(Context context){
        this.tripDBHelper = new TripDBHelper(context);
    }

    public long insert(String name, String fromDate, String toDate, String countries, long traveller){
        SQLiteDatabase db = tripDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(tripDBHelper.ID, getNextId());
        contentValues.put(tripDBHelper.NAME, name);
        contentValues.put(tripDBHelper.FROMDATE, fromDate);
        contentValues.put(tripDBHelper.TODATE, toDate);
        contentValues.put(tripDBHelper.COUNTRIES, countries);
        contentValues.put(tripDBHelper.TRAVELLER, traveller);
        long _id = db.insert(tripDBHelper.TABLE_NAME,null,contentValues);
        db.close();
        return _id;
    }

    public long insertAll(String name, String fromDate, String toDate, String countries, List<Long> travellers){
        long id = getNextId();
        SQLiteDatabase db = tripDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Long traveller : travellers) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(tripDBHelper.ID, id);
                contentValues.put(tripDBHelper.NAME, name);
                contentValues.put(tripDBHelper.FROMDATE, fromDate);
                contentValues.put(tripDBHelper.TODATE, toDate);
                contentValues.put(tripDBHelper.COUNTRIES, countries);
                contentValues.put(tripDBHelper.TRAVELLER, traveller);
                long _id = db.insert(tripDBHelper.TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
        return id;
    }

    public long getNextId(){
        //Get next available trip id
        SQLiteDatabase db = tripDBHelper.getWritableDatabase();
        String[] columns = {tripDBHelper.ID};
        String selection = tripDBHelper.ID+" = (SELECT MAX("+tripDBHelper.ID+"))";
        //Cursor cursor = db.query(tripDBHelper.TABLE_NAME, columns, "price=(SELECT MAX(price))", null, null, null, null);
        Cursor cursor = db.query(tripDBHelper.TABLE_NAME, columns, selection,null,null,null,null);
        long id = 1;
        while(cursor.moveToNext()) {
            int index = cursor.getColumnIndex(tripDBHelper.ID);
            id = cursor.getLong(index) + 1;
        }
        cursor.close();
        db.close();
        return id;
    }

    public List<Trip> getTrips(long traveller){
        SQLiteDatabase db = tripDBHelper.getWritableDatabase();
        String[] columns = {tripDBHelper.ID, tripDBHelper.NAME, tripDBHelper.FROMDATE, tripDBHelper.TODATE, tripDBHelper.COUNTRIES};
        String selection = tripDBHelper.TRAVELLER+" =?";
        String[] selectionArgs = {traveller+""};
        Cursor cursor = db.query(tripDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        Map<Long, Trip> trips = new HashMap<>();
        while(cursor.moveToNext()){
            int index = cursor.getColumnIndex(tripDBHelper.ID);
            long id = cursor.getLong(index);
            if (!trips.containsKey(id)){
                index = cursor.getColumnIndex(tripDBHelper.NAME);
                String name = cursor.getString(index);
                index = cursor.getColumnIndex(tripDBHelper.FROMDATE);
                String fromDate = cursor.getString(index);
                index = cursor.getColumnIndex(tripDBHelper.TODATE);
                String toDate = cursor.getString(index);
                index = cursor.getColumnIndex(tripDBHelper.COUNTRIES);
                String countries = cursor.getString(index);
                try {
                    Trip trip = new Trip(id, name, fromDate, toDate, countries);
                    trips.put(id, trip);
                } catch (ParseException e) {

                }
            }
        }
        cursor.close();
        if(trips.size() > 0){
            for(Map.Entry<Long, Trip> entry:trips.entrySet()){
                long tripId = entry.getKey();
                Trip trip = entry.getValue();
                String[] columns1 = {tripDBHelper.TRAVELLER};
                String selection1 = tripDBHelper.ID+" =?";
                String[] selectionArgs1 = {tripId+""};
                Cursor cursor1 = db.query(tripDBHelper.TABLE_NAME, columns1,selection1,selectionArgs1,null,null,null);
                List<Long> travellers = new ArrayList<>();
                while(cursor1.moveToNext()){
                    int index1 = cursor1.getColumnIndex(tripDBHelper.TRAVELLER);
                    long traveller_id = cursor1.getLong(index1);
                    travellers.add(traveller_id);
                }
                cursor1.close();
                trip.setTravellers(travellers);
            }
        }
        db.close();
        return new ArrayList<Trip>(trips.values());
    }

    public String getAll(){
        SQLiteDatabase db = tripDBHelper.getWritableDatabase();
        String[] columns = {tripDBHelper.PRIMARY_KEY, tripDBHelper.ID, tripDBHelper.NAME, tripDBHelper.FROMDATE, tripDBHelper.TODATE, tripDBHelper.COUNTRIES, tripDBHelper.TRAVELLER};
        Cursor cursor = db.query(tripDBHelper.TABLE_NAME, columns,null,null,null,null,null);
        StringBuffer trips = new StringBuffer();
        while(cursor.moveToNext()){
            int index = cursor.getColumnIndex(tripDBHelper.PRIMARY_KEY);
            long _id = cursor.getLong(index);
            index = cursor.getColumnIndex(tripDBHelper.ID);
            long id = cursor.getLong(index);
            index = cursor.getColumnIndex(tripDBHelper.NAME);
            String name = cursor.getString(index);
            index = cursor.getColumnIndex(tripDBHelper.FROMDATE);
            String fromDate = cursor.getString(index);
            index = cursor.getColumnIndex(tripDBHelper.TODATE);
            String toDate = cursor.getString(index);
            index = cursor.getColumnIndex(tripDBHelper.COUNTRIES);
            String countries = cursor.getString(index);
            index = cursor.getColumnIndex(tripDBHelper.TRAVELLER);
            long traveller = cursor.getLong(index);
            trips.append(_id+" "+id+" "+name+" "+fromDate+" "+" "+toDate+" "+countries+" "+traveller+"\n");
        }
        cursor.close();
        db.close();
        return trips.toString();
    }

    static class TripDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="trip.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="Trip";
        private static final String PRIMARY_KEY = "_id";
        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String FROMDATE = "fromDate";
        private static final String TODATE = "toDate";
        private static final String COUNTRIES = "countries";
        private static final String TRAVELLER = "traveller";

        public TripDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +ID+" INTEGER, "+NAME+" TEXT, "+FROMDATE+" TEXT, "+TODATE+" TEXT, "+COUNTRIES+" TEXT, "+TRAVELLER+" INTEGER);";
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
