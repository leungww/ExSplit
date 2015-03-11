package fyp.leungww.exsplit;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OweDBAdapter {
    private OweDBHelper oweDBHelper;

    public OweDBAdapter(Context context){
        this.oweDBHelper = new OweDBHelper(context);
    }

    public void insert(long bill, List<Long> debtors, List<Long> creditors, List<Double> amountsOwed){
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int index=0;index<debtors.size();index++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(OweDBHelper.BILL, bill);
                contentValues.put(OweDBHelper.DEBTOR, debtors.get(index));
                contentValues.put(OweDBHelper.CREDITOR, creditors.get(index));
                contentValues.put(OweDBHelper.AMOUNT_OWED, amountsOwed.get(index));
                long _id = db.insert(OweDBHelper.TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
    }

    public Map<Long, Double> getDebts(long debtor){
        Map<Long, Double> debts = new HashMap<>();
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        String[] columns = {OweDBHelper.CREDITOR, OweDBHelper.AMOUNT_OWED};
        String selection = OweDBHelper.DEBTOR+" =?";
        String[] selectionArgs = {debtor+""};
        Cursor cursor = db.query(OweDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(OweDBHelper.CREDITOR);
            long creditor = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(OweDBHelper.AMOUNT_OWED);
            double amountOwed = cursor.getDouble(index2);
            debts.put(creditor, amountOwed);
        }
        cursor.close();
        return debts;
    }

    public Map<Long, Double> getCredits(long creditor){
        Map<Long, Double> credits = new HashMap<>();
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        String[] columns = {OweDBHelper.DEBTOR, OweDBHelper.AMOUNT_OWED};
        String selection = OweDBHelper.CREDITOR+" =?";
        String[] selectionArgs = {creditor+""};
        Cursor cursor = db.query(OweDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(OweDBHelper.DEBTOR);
            long debtor = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(OweDBHelper.AMOUNT_OWED);
            double amountCredited = cursor.getDouble(index2);
            credits.put(debtor, amountCredited);
        }
        cursor.close();
        return credits;
    }

    /*public void delete(List<Long> holders, String currency, List<Double> amounts){
        SQLiteDatabase db = accountDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for(int index=0;index<holders.size();index++){
                Account account = getAccount(holders.get(0), currency);
                if(account != null){
                    BigDecimal amountBD = BigDecimal.valueOf(amounts.get(index));
                    double balance = account.getBalance();
                    BigDecimal balanceBD = BigDecimal.valueOf(balance);
                    balanceBD = balanceBD.add(amountBD);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AccountDBHelper.BALANCE, balanceBD.doubleValue());
                    String where = AccountDBHelper.PRIMARY_KEY+" =?";
                    String[] whereArgs = {account.get_id()+""};
                    int count = db.update(AccountDBHelper.TABLE_NAME, contentValues, where, whereArgs);
                }
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
    }*/

    static class OweDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="owe.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="Owe";
        private static final String PRIMARY_KEY = "_id";
        private static final String BILL = "bill";
        private static final String DEBTOR = "debtor";
        private static final String CREDITOR = "creditor";
        private static final String AMOUNT_OWED = "amountOwed";

        public OweDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +BILL+" INTEGER, "+DEBTOR+" INTEGER, "+CREDITOR+" INTEGER, "+AMOUNT_OWED+" REAL);";
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
