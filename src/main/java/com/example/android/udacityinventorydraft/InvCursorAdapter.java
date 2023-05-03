package com.example.android.udacityinventorydraft;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.udacityinventorydraft.InventoryContract.InvEntry;

/**
 * Created by MP on 4/12/2017.
 */

public class InvCursorAdapter extends CursorAdapter {

    public InvCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // make new blank list item view
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // use xml layout to inflate view
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    // when cursor points at row the data can be bound to xml layout element
    public void bindView(View view, final Context context, final Cursor cursor) {
        // find views
        TextView Name = (TextView) view.findViewById(R.id.name);
        TextView Price = (TextView) view.findViewById(R.id.price);
        final TextView updateQuantity = (TextView) view.findViewById(R.id.quantity_within_linear);

        ImageView image = (ImageView) view.findViewById(R.id.image_list);

        LinearLayout parentView = (LinearLayout) view.findViewById(R.id.list_linear_parent);

        // find columns
        int idColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry._ID);
        int idNameColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry.COLUMN_INV_NAME);
        int idPriceColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry.COLUMN_INV_PRICE);
        int idQuantityColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry.COLUMN_INV_QUANTITY);
        int idImageColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry.COLUMN_IMAGE);
        // read cursor
        final int rowId = cursor.getInt(idColumnIndex);
        final String invName = cursor.getString(idNameColumnIndex);
        final String invPrice = cursor.getString(idPriceColumnIndex);
        final int invQuantity = cursor.getInt(idQuantityColumnIndex);
        final String invImage = cursor.getString(idImageColumnIndex);
        //image.setImageBitmap(BitmapFactory.decodeFile(invImage));
        image.setImageURI(Uri.parse(invImage));
        // add cursor values to textviews
        Name.setText(invName);
        Price.setText(invPrice);
        updateQuantity.setText(String.valueOf(invQuantity));

        final int position = cursor.getPosition();

        Button sell = (Button) view.findViewById(R.id.sell);
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = invQuantity;
                // int quantity = Integer.parseInt(updateQuantity.getText().toString());
                // fix if/else statemetn
                if (quantity < 1) {
                    Toast.makeText(context, "out of stock, 0 quantity",
                            Toast.LENGTH_SHORT).show();
                } else if (quantity > 0) {
                    quantity = quantity - 1;
                    String quantityCount = Integer.toString(quantity);
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InvEntry.COLUMN_INV_QUANTITY, quantityCount);
                    Uri currentInventoryUri = ContentUris.withAppendedId(InventoryContract.InvEntry.CONTENT_URI, rowId);
                    context.getContentResolver().update(currentInventoryUri, values,
                            null, null);
                    updateQuantity.setText(quantityCount);
                }
                Uri mCurrentInvUri = ContentUris.withAppendedId(InvEntry.CONTENT_URI, position);
                updateQuantity.setText(String.valueOf(quantity));
                ContentValues values = new ContentValues();
                values.put(InvEntry.COLUMN_INV_QUANTITY, quantity);
                context.getContentResolver().update(mCurrentInvUri, values, null, null);
            }
        });
    }
}

