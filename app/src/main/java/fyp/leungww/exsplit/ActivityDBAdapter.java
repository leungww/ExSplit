package fyp.leungww.exsplit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ActivityDBAdapter {
    private ActivityDBHelper activityDBHelper;

    public ActivityDBAdapter(Context context){
        this.activityDBHelper = new ActivityDBHelper(context);
    }

    public void insert(long traveller, String createdDate, String category, String description, int isSystemGenerated, long objectId){
        SQLiteDatabase db = activityDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(activityDBHelper.TRAVELLER, traveller);
        contentValues.put(activityDBHelper.CREATED_DATE, createdDate);
        contentValues.put(activityDBHelper.CATEGORY, category);
        contentValues.put(activityDBHelper.DESCRIPTION, description);
        contentValues.put(activityDBHelper.IS_SYSTEM_GENERATED, isSystemGenerated);
        contentValues.put(activityDBHelper.OBJECT_ID, objectId);
        long _id = db.insert(activityDBHelper.TABLE_NAME,null,contentValues);
        db.close();
    }

    public void insertAll(List<Long> travellers, String createdDate, String category, String description, int isSystemGenerated, long objectId){
        SQLiteDatabase db = activityDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Long traveller : travellers) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(activityDBHelper.TRAVELLER, traveller);
                contentValues.put(activityDBHelper.CREATED_DATE, createdDate);
                contentValues.put(activityDBHelper.CATEGORY, category);
                contentValues.put(activityDBHelper.DESCRIPTION, description);
                contentValues.put(activityDBHelper.IS_SYSTEM_GENERATED, isSystemGenerated);
                contentValues.put(activityDBHelper.OBJECT_ID, objectId);
                long _id = db.insert(activityDBHelper.TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
    }

    public List<Activity> getActivities(long traveller){
        SQLiteDatabase db = activityDBHelper.getWritableDatabase();
        String[] columns = {activityDBHelper.PRIMARY_KEY, activityDBHelper.CREATED_DATE, activityDBHelper.CATEGORY,
                activityDBHelper.DESCRIPTION, activityDBHelper.IS_SYSTEM_GENERATED, activityDBHelper.OBJECT_ID};
        String selection = activityDBHelper.TRAVELLER+" =?";
        String[] selectionArgs = {traveller+""};
        Cursor cursor = db.query(activityDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        List<Activity> activities = new ArrayList<>();
        while(cursor.moveToNext()){
            int index = cursor.getColumnIndex(activityDBHelper.PRIMARY_KEY);
            long _id = cursor.getLong(index);
            index = cursor.getColumnIndex(activityDBHelper.CREATED_DATE);
            String createdDate = cursor.getString(index);
            index = cursor.getColumnIndex(activityDBHelper.CATEGORY);
            String category = cursor.getString(index);
            index = cursor.getColumnIndex(activityDBHelper.DESCRIPTION);
            String description = cursor.getString(index);
            index = cursor.getColumnIndex(activityDBHelper.IS_SYSTEM_GENERATED);
            int isSystemGenerated = cursor.getInt(index);
            index = cursor.getColumnIndex(activityDBHelper.OBJECT_ID);
            long objectId = cursor.getLong(index);
            Activity activity = new Activity(_id, traveller, createdDate, category, description, isSystemGenerated, objectId);
            activities.add(activity);
        }
        cursor.close();
        db.close();
        return activities;
    }

    static class ActivityDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="activity.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="Activity";
        private static final String PRIMARY_KEY = "_id";
        private static final String TRAVELLER = "traveller";
        private static final String CREATED_DATE = "createDate";
        private static final String CATEGORY = "category";
        private static final String DESCRIPTION = "description";
        private static final String IS_SYSTEM_GENERATED = "isSystemGenerated";
        private static final String OBJECT_ID = "objectId";

        public ActivityDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +TRAVELLER+" INTEGER, "+CREATED_DATE+" TEXT, "+CATEGORY+" TEXT, "+DESCRIPTION+" TEXT, "+IS_SYSTEM_GENERATED+" INTEGER, "+OBJECT_ID+" INTEGER);";
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
