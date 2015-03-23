package fyp.leungww.exsplit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillDBAdapter {
    private BillDBHelper billDBHelper;
    private ItemDBHelper itemDBHelper;
    private PaymentDBHelper paymentDBHelper;
    private ItemSplitDBHelper itemSplitDBHelper;

    public BillDBAdapter(Context context){
        this.billDBHelper = new BillDBHelper(context);
        this.itemDBHelper = new ItemDBHelper(context);
        this.paymentDBHelper = new PaymentDBHelper(context);
        this.itemSplitDBHelper = new ItemSplitDBHelper(context);
    }

    public long insert(String description, String createdDate, String category, long trip, String currency, double total, List<Item> items, List<Long> travellers_id, List<Double> amounts_needed, List<Double> amounts_paid) throws SQLException {
        SQLiteDatabase billDB = billDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BillDBHelper.DESCRIPTION, description);
        contentValues.put(BillDBHelper.CREATED_DATE, createdDate);
        contentValues.put(BillDBHelper.CATEGORY, category);
        contentValues.put(BillDBHelper.TRIP, trip);
        contentValues.put(BillDBHelper.CURRENCY, currency);
        contentValues.put(BillDBHelper.TOTAL, total);
        long id = billDB.insert(BillDBHelper.TABLE_NAME,null,contentValues);
        billDB.close();
        if(id < 0){
            throw new SQLException("Errors occurred in creating the bill in database. Please try again.");
        }else{
            SQLiteDatabase itemDB = itemDBHelper.getWritableDatabase();
            SQLiteDatabase itemSplitDB = itemSplitDBHelper.getWritableDatabase();
            SQLiteDatabase paymentDB = paymentDBHelper.getWritableDatabase();

            itemDB.beginTransaction();
            itemSplitDB.beginTransaction();
            paymentDB.beginTransaction();
            try {
                for (Item item : items) {
                    contentValues = new ContentValues();
                    contentValues.put(ItemDBHelper.BILL, id);
                    contentValues.put(ItemDBHelper.NAME, item.getName());
                    contentValues.put(ItemDBHelper.PRICE, item.getPrice());
                    long item_id = itemDB.insert(ItemDBHelper.TABLE_NAME, null, contentValues);
                    for (Map.Entry<Long, Double> entry : item.getAmounts().entrySet()) {
                        Long sharer = entry.getKey();
                        Double amount = entry.getValue();
                        ContentValues itemSplit = new ContentValues();
                        itemSplit.put(ItemSplitDBHelper.ITEM,item_id);
                        itemSplit.put(ItemSplitDBHelper.SHARER,sharer);
                        itemSplit.put(ItemSplitDBHelper.AMOUNT,amount);
                        long itemSplit_id = itemSplitDB.insert(ItemSplitDBHelper.TABLE_NAME, null, itemSplit);
                    }
                }
                for(int index=0;index<travellers_id.size();index++){
                    contentValues = new ContentValues();
                    contentValues.put(PaymentDBHelper.BILL, id);
                    contentValues.put(PaymentDBHelper.PAYER, travellers_id.get(index));
                    contentValues.put(PaymentDBHelper.AMOUNT_NEEDED, amounts_needed.get(index));
                    contentValues.put(PaymentDBHelper.AMOUNT_PAID, amounts_paid.get(index));
                    long payment_id = paymentDB.insert(PaymentDBHelper.TABLE_NAME, null, contentValues);
                }
                itemDB.setTransactionSuccessful();
                itemSplitDB.setTransactionSuccessful();
                paymentDB.setTransactionSuccessful();
            }finally {
                itemDB.endTransaction();
                itemSplitDB.endTransaction();
                paymentDB.endTransaction();
            }
            itemDB.close();
            itemSplitDB.close();
            paymentDB.close();
        }
        return id;
    }

    public Bill getBill(long bill_id){
        SQLiteDatabase billDB = billDBHelper.getWritableDatabase();
        SQLiteDatabase itemDB = itemDBHelper.getWritableDatabase();
        SQLiteDatabase itemSplitDB = itemSplitDBHelper.getWritableDatabase();

        String[] bill_columns = {BillDBHelper.DESCRIPTION, BillDBHelper.CREATED_DATE, BillDBHelper.CATEGORY, BillDBHelper.TRIP, BillDBHelper.CURRENCY, BillDBHelper.TOTAL};
        String bill_selection = BillDBHelper.PRIMARY_KEY+" =?";
        String[] bill_selectionArgs = {bill_id+""};
        Cursor cursor = billDB.query(BillDBHelper.TABLE_NAME, bill_columns,bill_selection,bill_selectionArgs,null,null,null);
        Bill bill = null;
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(BillDBHelper.DESCRIPTION);
            String description = cursor.getString(index1);
            int index2 = cursor.getColumnIndex(BillDBHelper.CREATED_DATE);
            String createdDate = cursor.getString(index2);
            int index3 = cursor.getColumnIndex(BillDBHelper.CATEGORY);
            String category = cursor.getString(index3);
            int index4 = cursor.getColumnIndex(BillDBHelper.TRIP);
            Long trip = cursor.getLong(index4);
            int index5 = cursor.getColumnIndex(BillDBHelper.CURRENCY);
            String currency = cursor.getString(index5);
            int index6 = cursor.getColumnIndex(BillDBHelper.TOTAL);
            Double total = cursor.getDouble(index6);
            try {
                bill = new Bill(bill_id, trip, description, createdDate, category, currency, total);
            } catch (ParseException e) {

            }
        }
        cursor.close();
        billDB.close();

        String[] item_columns = {ItemDBHelper.PRIMARY_KEY, ItemDBHelper.NAME, ItemDBHelper.PRICE};
        String item_selection = ItemDBHelper.BILL+" =?";
        String[] item_selectionArgs = {bill_id+""};
        cursor = itemDB.query(ItemDBHelper.TABLE_NAME, item_columns,item_selection,item_selectionArgs,null,null,null);
        List<Long> items_id = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<Double> prices = new ArrayList<>();
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(ItemDBHelper.PRIMARY_KEY);
            Long item_id = cursor.getLong(index1);
            int index2 = cursor.getColumnIndex(ItemDBHelper.NAME);
            String name = cursor.getString(index2);
            int index3 = cursor.getColumnIndex(ItemDBHelper.PRICE);
            double price = cursor.getDouble(index3);
            items_id.add(item_id);
            names.add(name);
            prices.add(price);
        }
        cursor.close();

        List<Map<Long, Double>> amountsList = new ArrayList<>();
        for(Long item_id:items_id){
            String[] itemSplit_columns = {ItemSplitDBHelper.SHARER, ItemSplitDBHelper.AMOUNT};
            String itemSplit_selection = ItemSplitDBHelper.ITEM+" =?";
            String[] itemSplit_selectionArgs = {item_id+""};
            cursor = itemSplitDB.query(ItemSplitDBHelper.TABLE_NAME, itemSplit_columns,itemSplit_selection,itemSplit_selectionArgs,null,null,null);
            Map<Long, Double> amounts = new HashMap<>();
            while(cursor.moveToNext()){
                int index1 = cursor.getColumnIndex(ItemSplitDBHelper.SHARER);
                long sharer = cursor.getLong(index1);
                int index2 = cursor.getColumnIndex(ItemSplitDBHelper.AMOUNT);
                double amount = cursor.getDouble(index2);
                amounts.put(sharer, amount);

            }
            amountsList.add(amounts);
            cursor.close();
        }
        bill.addItems(names, prices, amountsList);
        itemDB.close();
        itemSplitDB.close();
        return bill;
    }

    public Double getAmountNeeded(long bill_id, long payer_id){
        SQLiteDatabase db = paymentDBHelper.getWritableDatabase();
        String[] columns = {paymentDBHelper.AMOUNT_NEEDED};
        String selection = paymentDBHelper.BILL+" =? AND "+paymentDBHelper.PAYER+" =?";
        String[] selectionArgs = {bill_id+"", payer_id+""};
        Cursor cursor = db.query(paymentDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        Double amount = null;
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(paymentDBHelper.AMOUNT_NEEDED);
            amount = cursor.getDouble(index1);
        }
        cursor.close();
        db.close();
        return amount;
    }

    public Double getAmountPaid(long bill_id, long payer_id){
        SQLiteDatabase db = paymentDBHelper.getWritableDatabase();
        String[] columns = {paymentDBHelper.AMOUNT_PAID};
        String selection = paymentDBHelper.BILL+" =? AND "+paymentDBHelper.PAYER+" =?";
        String[] selectionArgs = {bill_id+"", payer_id+""};
        Cursor cursor = db.query(paymentDBHelper.TABLE_NAME, columns,selection,selectionArgs,null,null,null);
        Double amount = null;
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(paymentDBHelper.AMOUNT_PAID);
            amount = cursor.getDouble(index1);
        }
        cursor.close();
        db.close();
        return amount;
    }

    /*public String getAll(){
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
        return travellers.toString();
    }*/

    static class BillDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="bill.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="Bill";
        private static final String PRIMARY_KEY = "_id";
        private static final String DESCRIPTION = "description";
        private static final String CREATED_DATE = "createdDate";
        private static final String CATEGORY = "category";
        private static final String TRIP = "trip";
        private static final String CURRENCY = "currency";
        private static final String TOTAL = "total";

        public BillDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +DESCRIPTION+" TEXT, "+CREATED_DATE+" TEXT, "+CATEGORY+" TEXT, "+TRIP+" INTEGER, "+CURRENCY+" TEXT, "+TOTAL+" REAL);";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String dropTable = "DROP TABLE IF EXISTS "+TABLE_NAME+";";
            db.execSQL(dropTable);
            onCreate(db);
        }
    }

    static class ItemDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="item.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="item";
        private static final String PRIMARY_KEY = "_id";
        private static final String BILL = "bill";
        private static final String NAME = "name";
        private static final String PRICE = "price";

        public ItemDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+BILL+" INTEGER, "+NAME+" TEXT, "+PRICE+" REAL);";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String dropTable = "DROP TABLE IF EXISTS "+TABLE_NAME+";";
            db.execSQL(dropTable);
            onCreate(db);
        }
    }

    static class PaymentDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="payment.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="Payment";
        private static final String PRIMARY_KEY = "_id";
        private static final String BILL = "bill";
        private static final String PAYER = "payer";
        private static final String AMOUNT_NEEDED = "amountNeeded";
        private static final String AMOUNT_PAID = "amountPaid";

        public PaymentDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+BILL+" INTEGER, "+PAYER+" INTEGER, "+AMOUNT_NEEDED+" REAL, "+AMOUNT_PAID+" REAL);";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String dropTable = "DROP TABLE IF EXISTS "+TABLE_NAME+";";
            db.execSQL(dropTable);
            onCreate(db);
        }
    }

    static class ItemSplitDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME="itemSplit.db";
        private static final int DB_VERSION=1;
        private static final String TABLE_NAME="ItemSplit";
        private static final String PRIMARY_KEY = "_id";
        private static final String ITEM = "item";
        private static final String SHARER = "sharer";
        private static final String AMOUNT = "amount";

        public ItemSplitDBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE "+TABLE_NAME+" ("+PRIMARY_KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ITEM+" INTEGER, "+SHARER+" INTEGER, "+AMOUNT+" REAL);";
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
