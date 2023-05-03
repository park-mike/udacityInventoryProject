package com.example.android.udacityinventorydraft;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by MP on 4/10/2017.
 */

public class InventoryProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int INVENTORY_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #INVENTORY}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #INVENTORY_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;


    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = database.query(InventoryContract.InvEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.InvEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventory(uri, contentvalues);
            default:
                throw new IllegalArgumentException("Insertion not supported for" + uri);
        }
    }

    private Uri insertInventory(Uri uri, ContentValues values) {
        String name = values.getAsString(InventoryContract.InvEntry.COLUMN_INV_NAME);
        if (name == null) {
            Toast.makeText(getContext(), "enter text in fields", Toast.LENGTH_SHORT).show();
            //         throw new IllegalArgumentException("Inventory requires name");
        }
        Integer quantity = values.getAsInteger(InventoryContract.InvEntry.COLUMN_INV_QUANTITY);
        if (quantity == null) {
            Toast.makeText(getContext(), "enter quantity in fields", Toast.LENGTH_SHORT).show();
            //     throw new IllegalArgumentException("Inventory requires quant");
        }
        Integer price = values.getAsInteger(InventoryContract.InvEntry.COLUMN_INV_PRICE);
        if (price == null) {
            //      throw new IllegalArgumentException("Inventory requires price");
            Toast.makeText(getContext(), "enter price in fields", Toast.LENGTH_SHORT).show();
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(InventoryContract.InvEntry.TABLE_NAME, null, values);
        if (id == -1) {
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentvalues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentvalues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryContract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentvalues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("update not supported for" + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryContract.InvEntry.COLUMN_INV_NAME)) {
            String name = values.getAsString(InventoryContract.InvEntry.COLUMN_INV_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Enter Inventory Name");
            }
        }

        if (values.containsKey(InventoryContract.InvEntry.COLUMN_INV_PRICE)) {
            Integer price = values.getAsInteger(InventoryContract.InvEntry.COLUMN_INV_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Inventory requires valid price");
            }
        }

        if (values.containsKey(InventoryContract.InvEntry.COLUMN_INV_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.InvEntry.COLUMN_INV_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("inventory requires valid quantity");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryContract.InvEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InventoryContract.InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("deletion is not supported for" + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);

        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryContract.InvEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InvEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri with match" + uri + " " + match);
        }
    }


}
