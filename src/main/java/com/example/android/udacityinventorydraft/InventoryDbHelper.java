package com.example.android.udacityinventorydraft;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MP on 4/10/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryContract.InvEntry.TABLE_NAME + "("
                + InventoryContract.InvEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.InvEntry.COLUMN_INV_NAME + " TEXT NOT NULL, "
                + InventoryContract.InvEntry.COLUMN_INV_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.InvEntry.COLUMN_INV_QUANTITY + " INTEGER NOT NULL DEFAULT 0,"
                + InventoryContract.InvEntry.COLUMN_IMAGE + " TEXT NOT NULL); ";
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
