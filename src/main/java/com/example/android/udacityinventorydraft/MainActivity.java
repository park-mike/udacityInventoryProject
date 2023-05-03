package com.example.android.udacityinventorydraft;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.udacityinventorydraft.InventoryContract.InvEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int INVENTORY_LOADER = 0;

    // adapter for list view
    InvCursorAdapter mCursorAdapter;
    Cursor cursor;
    int rowsSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // fab to open editor
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InventoryEditor.class);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
        // find list view to hold data
        ListView invListView = (ListView) findViewById(R.id.list);
        // set empty view which shows user how to add items
        View emptyView = findViewById(R.id.empty_view);
        invListView.setEmptyView(emptyView);

        // create list item for each row of data
        // cursor set to null because no data yet
        mCursorAdapter = new InvCursorAdapter(this, cursor);
        invListView.setAdapter(mCursorAdapter);


        invListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, InventoryEditor.class);
                Uri currentInvUri = ContentUris.withAppendedId(InvEntry.CONTENT_URI, id);
                intent.setData(currentInvUri);
                startActivity(intent);
            }
        });

    }

    private void deleteAllInv() {

        rowsSelected = getContentResolver().delete(InvEntry.CONTENT_URI, null, null);
        Toast.makeText(this, "All Inventory Deleted",
                Toast.LENGTH_SHORT).show();
    }

    // use xml to set menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to delete all entries?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAllInv();
                            }
                        })
                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        })
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // projection defines what columns are needed


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_INV_NAME,
                InvEntry.COLUMN_INV_PRICE,
                InvEntry.COLUMN_INV_QUANTITY,
                InvEntry.COLUMN_IMAGE
        };

        return new CursorLoader(this, InvEntry.CONTENT_URI, projection, null, null, null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
