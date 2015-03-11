package fyp.leungww.exsplit;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountDBAdapter {
    private AccountDBHelper accountDBHelper;

    public AccountDBAdapter(Context context){
        this.accountDBHelper = new AccountDBHelper(context);
    }

    public long insert(long holder, String currency){
        long id;
        Account account = getAccount(holder, currency);
        if(account == null){
            SQLiteDatabase db = accountDBHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(accountDBHelper.CURRENCY, currency);
            contentValues.put(accountDBHelper.HOLDER, holder);
            contentValues.put(accountDBHelper.BALANCE, 0.0);
            id = db.insert(accountDBHelper.TABLE_NAME,null,contentValues);
            db.close();
        }else{
            id = account.get_id();
        }
        return id;
    }

    public Account getAccount(long holder, String currency){
        SQLiteDatabase db = accountDBHelper.getWritableDatabase();
        String[] columns = {accountDBHelper.PRIMARY_KEY, accountDBHelper.BALANCE};
        String selection = accountDBHelper.HOLDER+" =? AND "+accountDBHelper.CURRENCY+" =?";
        String[] selectionArgs = {holder+"", currency};
        Cursor cursor = db.query(accountDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        Account account = null;
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(accountDBHelper.PRIMARY_KEY);
            long _id = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(accountDBHelper.BALANCE);
            double balance = cursor.getDouble(index2);
            account = new Account(_id, holder, currency, balance);
        }
        cursor.close();
        return account;
    }

    public List<Account> getAccounts(long holder){
        SQLiteDatabase db = accountDBHelper.getWritableDatabase();
        String[] columns = {accountDBHelper.PRIMARY_KEY, accountDBHelper.CURRENCY, accountDBHelper.BALANCE};
        String selection = accountDBHelper.HOLDER+" =?";
        String[] selectionArgs = {holder+""};
        Cursor cursor = db.query(accountDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        List<Account> accounts = new ArrayList<>();
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(accountDBHelper.PRIMARY_KEY);
            long _id = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(accountDBHelper.CURRENCY);
            String currency = cursor.getString(index2);
            int index3 = cursor.getColumnIndex(accountDBHelper.BALANCE);
            double balance = cursor.getDouble(index3);
            accounts.add(new Account(_id, holder, currency, balance));
        }
        cursor.close();
        return accounts;
    }

    public void updateBalances(List<Long> holders, String currency, List<Double> amounts){
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
    }

    static class AccountDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="account.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="Account";
        private static final String PRIMARY_KEY = "_id";
        private static final String CURRENCY = "currency";
        private static final String BALANCE = "balance";
        private static final String HOLDER = "holder";

        public AccountDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+CURRENCY+" TEXT, "+BALANCE+" REAL, "+HOLDER+" INTEGER);";
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
