package fyp.leungww.exsplit;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class OweDBAdapter {
    private OweDBHelper oweDBHelper;

    public OweDBAdapter(Context context){
        this.oweDBHelper = new OweDBHelper(context);
    }

    public void insert(long bill, String currency, List<Long> debtors, List<Long> creditors, List<Double> amountsOwed){
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int index=0;index<debtors.size();index++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(OweDBHelper.BILL, bill);
                contentValues.put(OweDBHelper.CURRENCY, currency);
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

    public Map<Long, Owe> getDebts(long debtor){
        Map<Long, Owe> debts = new HashMap<>();
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        String[] columns = {OweDBHelper.BILL, OweDBHelper.CURRENCY, OweDBHelper.CREDITOR, OweDBHelper.AMOUNT_OWED};
        String selection = OweDBHelper.DEBTOR+" =?";
        String[] selectionArgs = {debtor+""};
        Cursor cursor = db.query(OweDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(OweDBHelper.BILL);
            long bill = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(OweDBHelper.CURRENCY);
            String currency = cursor.getString(index2);
            int index3 = cursor.getColumnIndex(OweDBHelper.CREDITOR);
            long creditor = cursor.getLong(index3);
            int index4 = cursor.getColumnIndex(OweDBHelper.AMOUNT_OWED);
            double amountOwed = cursor.getDouble(index4);
            if(debts.containsKey(creditor)){
                Owe debt = debts.get(creditor);
                debt.addBill(bill, amountOwed, currency);
            }else{
                Owe debt = new Owe(creditor);
                debt.addBill(bill, amountOwed, currency);
                debts.put(creditor,debt);
            }
        }
        cursor.close();
        db.close();
        return debts;
    }

    public Map<Long, Owe> getCredits(long creditor){
        Map<Long, Owe> credits = new HashMap<>();
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        String[] columns = {OweDBHelper.BILL, OweDBHelper.CURRENCY, OweDBHelper.DEBTOR, OweDBHelper.AMOUNT_OWED};
        String selection = OweDBHelper.CREDITOR+" =?";
        String[] selectionArgs = {creditor+""};
        Cursor cursor = db.query(OweDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(OweDBHelper.BILL);
            long bill = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(OweDBHelper.CURRENCY);
            String currency = cursor.getString(index2);
            int index3 = cursor.getColumnIndex(OweDBHelper.DEBTOR);
            long debtor = cursor.getLong(index3);
            int index4 = cursor.getColumnIndex(OweDBHelper.AMOUNT_OWED);
            double amountCredited = cursor.getDouble(index4);
            if(credits.containsKey(debtor)){
                Owe credit = credits.get(debtor);
                credit.addBill(bill, amountCredited, currency);
            }else{
                Owe credit = new Owe(debtor);
                credit.addBill(bill, amountCredited, currency);
                credits.put(debtor,credit);
            }
        }
        cursor.close();
        db.close();
        return credits;
    }

    public Queue<Debt> getDebts(String currency, long debtor, long creditor){
        Queue<Debt> queue = new ArrayDeque<>();
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        String[] columns = {OweDBHelper.PRIMARY_KEY, OweDBHelper.AMOUNT_OWED};
        String selection = OweDBHelper.CURRENCY+" =? AND "+OweDBHelper.DEBTOR+" =? AND "+OweDBHelper.CREDITOR+" =?";
        String[] selectionArgs = {currency, debtor+"", creditor+""};
        Cursor cursor = db.query(OweDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(OweDBHelper.PRIMARY_KEY);
            long _id = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(OweDBHelper.AMOUNT_OWED);
            double amountOwed = cursor.getDouble(index2);
            Debt debt = new Debt(_id,amountOwed);
            queue.add(debt);
        }
        cursor.close();
        db.close();
        return queue;
    }

    public int updateAmountOwed(Debt debt){
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OweDBHelper.AMOUNT_OWED, debt.getAmountOwed());
        String where = OweDBHelper.PRIMARY_KEY+" =?";
        String[] whereArgs = {debt.get_id()+""};
        int count = db.update(OweDBHelper.TABLE_NAME, contentValues, where, whereArgs);
        db.close();
        return count;
    }

    public void deleteOwes(List<Debt> debts){
        SQLiteDatabase db = oweDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for(Debt debt:debts){
                String where = OweDBHelper.PRIMARY_KEY+" =?";
                String[] whereArgs = {debt.get_id()+""};
                int count = db.delete(OweDBHelper.TABLE_NAME, where, whereArgs);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
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
        private static final String CURRENCY = "currency";
        private static final String DEBTOR = "debtor";
        private static final String CREDITOR = "creditor";
        private static final String AMOUNT_OWED = "amountOwed";

        public OweDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +BILL+" INTEGER, "+CURRENCY+" TEXT, "+DEBTOR+" INTEGER, "+CREDITOR+" INTEGER, "+AMOUNT_OWED+" REAL);";
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
