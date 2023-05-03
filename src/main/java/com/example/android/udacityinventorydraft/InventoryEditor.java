package com.example.android.udacityinventorydraft;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by MP on 4/10/2017.
 */

public class InventoryEditor extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    public static final int EXISTING_INV_LOADER = 0;

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.udacityinventorydraft";

    private static final String IMAGE_LOCATION = "imageLoc";
    private static final String IMAGE_URI = "imgUri";
    private static final String LOG_TAG = InventoryEditor.class.getSimpleName();
    private static final int REQUEST_EXTERNAL_STORAGE = 3;
    private static int LOAD_IMAGE_RESULTS = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public String imgText;

    public TextView mTextView;
    InventoryDbHelper mDbHelper;
    private Uri mCurrentInvUri;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private boolean isGallaryPic = false;
    private EditText mQuantityEditText;
    private ImageView mImageView;
    private Uri imgUri; //image Uri
    private boolean mInvHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInvHasChanged = true;
            return false;
        }
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        mTextView = (TextView) findViewById(R.id.placeholder);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        Button emailButton = (Button) findViewById(R.id.order);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent orderEmail = new Intent(Intent.ACTION_SENDTO);
                orderEmail.setType("text/html");
                orderEmail.setData(Uri.parse("mailto:"));
                orderEmail.putExtra(Intent.EXTRA_SUBJECT, "The URI of the product I want to order is " + imgUri);
                startActivity(orderEmail);
            }
        });

        Button minus = (Button) findViewById(R.id.minus);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minusOne();
            }
        });

        Button plus = (Button) findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusOne();
            }
        });

        Intent intent = getIntent();
        mCurrentInvUri = intent.getData();
        imgUri = intent.getData();

        if (mCurrentInvUri == null) {
            setTitle("Add Inventory");
        } else {
            setTitle("Edit Inventory");
            getLoaderManager().initLoader(EXISTING_INV_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mImageView = (ImageView) findViewById(R.id.img_button);
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        int result = ContextCompat.checkSelfPermission(InventoryEditor.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            verifyStoragePermissions(InventoryEditor.this);
        } else {
            //send the intent
        }
    }

    private void minusOne() {
        String previousQuantity = mQuantityEditText.getText().toString();
        int previousQuantityInt;
        if (previousQuantity.equals("0")) {
            Toast.makeText(this, "Quantity is 0",
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            previousQuantityInt = Integer.parseInt(previousQuantity);
            mQuantityEditText.setText(String.valueOf(previousQuantityInt - 1));
        }
    }

    private void plusOne() {
        String previousQuantity = mQuantityEditText.getText().toString();
        int previousQuantityInt;

        previousQuantityInt = Integer.parseInt(previousQuantity);
        mQuantityEditText.setText(String.valueOf(previousQuantityInt + 1));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (imgUri != null) {
            outState.putString(IMAGE_URI, imgUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(IMAGE_URI) &&
                !savedInstanceState.getString(IMAGE_URI).equals("")) {

            imgUri = Uri.parse(savedInstanceState.getString(IMAGE_URI));
            mTextView.setText(imgUri.toString());

            ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageView.setImageBitmap(getBitmapFromUri(imgUri));
                }
            });
        }
    }

    public void openImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, LOAD_IMAGE_RESULTS);
        mImageView.setVisibility(View.VISIBLE);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // use xml layout to inflate view
        return LayoutInflater.from(context).inflate(R.layout.activity_inventory, parent, false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                writeData();
                return true;
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void writeData() {
        if (isValid()) {
            mDbHelper = new InventoryDbHelper(this);
            EditText textName = (EditText) findViewById(R.id.edit_name);
            EditText textPrice = (EditText) findViewById(R.id.edit_price);
            EditText textQuantity = (EditText) findViewById(R.id.edit_quantity);


            String invName = textName.getText().toString();
            int invPrice = Integer.parseInt(textPrice.getText().toString());
            int invQuantity = Integer.parseInt(textQuantity.getText().toString());

            String imgUriString = imgUri.toString();

            ContentValues values = new ContentValues();
            values.put(InventoryContract.InvEntry.COLUMN_INV_NAME, invName);
            values.put(InventoryContract.InvEntry.COLUMN_INV_PRICE, invPrice);
            values.put(InventoryContract.InvEntry.COLUMN_INV_QUANTITY, invQuantity);
            values.put(InventoryContract.InvEntry.COLUMN_IMAGE, imgUriString);


            if (mCurrentInvUri == null) {
                getContentResolver().insert(InventoryContract.InvEntry.CONTENT_URI, values);
                Toast.makeText(this, "save successful", Toast.LENGTH_SHORT).show();
            } else {
                getContentResolver().update(mCurrentInvUri, values, null, null);
                Toast.makeText(this, "update successful", Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
            Toast.makeText(this, "to save inventory all fields must be filled and a picture must be added, does not pass isValid method", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isValid() {
        EditText textName = (EditText) findViewById(R.id.edit_name);
        EditText textPrice = (EditText) findViewById(R.id.edit_price);
        EditText textQuantity = (EditText) findViewById(R.id.edit_quantity);
        Log.v("my_tag", "textName output " + textName + "textPrice output " + textPrice + "textQuantity output " + textQuantity + "imageText output " + imgText);

        return (textName.getText().toString().trim().length() != 0 &&
                textPrice.getText().toString().trim().length() != 0 &&
                textQuantity.getText().toString().trim().length() != 0 &&
                imgUri != null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && resultData != null) {

            if (resultData != null) {
                imgUri = resultData.getData();

                Log.i(LOG_TAG, "Uri: " + imgUri.toString());

                mTextView.setText(imgUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(imgUri));


            } else {
                Log.v("my_tag", "selected image is null");
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryContract.InvEntry._ID,
                InventoryContract.InvEntry.COLUMN_INV_NAME,
                InventoryContract.InvEntry.COLUMN_INV_PRICE,
                InventoryContract.InvEntry.COLUMN_INV_QUANTITY,
                InventoryContract.InvEntry.COLUMN_IMAGE};

        return new CursorLoader(this, mCurrentInvUri, projection, null, null, null);
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = null;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            }
            Bitmap img = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            return img;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int idColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry._ID);
            int idNameColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry.COLUMN_INV_NAME);
            int idPriceColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry.COLUMN_INV_PRICE);
            int idQuantityColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry.COLUMN_INV_QUANTITY);
            int idImgColumnIndex = cursor.getColumnIndex(InventoryContract.InvEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(idNameColumnIndex);
            int price = cursor.getInt(idPriceColumnIndex);
            final int quantity = cursor.getInt(idQuantityColumnIndex);
            String imageText = cursor.getString(idImgColumnIndex);
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            imgUri = Uri.parse(imageText);
            mImageView.setImageBitmap(getBitmapFromUri(imgUri));
            // Uri selectedImage = Uri.parse(imageText);
            // mImageView.setImageBitmap(getBitmapFromUri(selectedImage));
            //mImageView.setImageBitmap(getBitmapFromUri(imgUri));
        }
    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton("Yes, delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteOneInventory();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteOneInventory() {

        // Only perform the delete if this is an existing pet.
        if (mCurrentInvUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsSelected = getContentResolver().delete(mCurrentInvUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsSelected == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "no inventory deleted",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "delete successful",
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }


}
