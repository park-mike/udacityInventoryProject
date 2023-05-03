package com.example.android.udacityinventorydraft;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by MP on 4/10/2017.
 */

public class InventoryContract {

    //unique name for content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.udacityinventorydraft";
    // inventory is acceptable path
    public static final String PATH_INVENTORY = "inventory";
    // create start of all URIs
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // prevent accidental instance
    private InventoryContract() {
    }

    // inner class which defines
    public static final class InvEntry implements BaseColumns {

        // uri for inventory data
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // db table name
        public final static String TABLE_NAME = "inventory";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_INV_NAME = "name";

        public final static String COLUMN_INV_PRICE = "price";

        public final static String COLUMN_INV_QUANTITY = "quantity";

        public static final String COLUMN_IMAGE = "image";
    }
}
