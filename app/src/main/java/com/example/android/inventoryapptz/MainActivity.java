package com.example.android.inventoryapptz;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapptz.data.ProductContract;
import com.example.android.inventoryapptz.data.ProductContract.ProductEntry;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;

    //sale button
    public void onSalePress(View view) {
        ContentResolver resolver = getContentResolver();

        Button saleButton = (Button) view;
        int currentProductId = Integer.parseInt(saleButton.getTag().toString());

        Uri uri = Uri.parse("content://com.example.android.inventoryapptz/inventoryapptz/"
                + String.valueOf(currentProductId));

        Cursor cursor = resolver.query(uri, null, null, null, null);

        int quantity = 0;
        if (cursor != null && cursor.moveToFirst()) {
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            String quantityValue = cursor.getString(quantityColumnIndex);
            quantity = Integer.parseInt(quantityValue);
            cursor.close();
        }

        ContentValues values = new ContentValues();

        // subtract one from the quantity and display in the TextView
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, --quantity);

        // if the user wants to go with quantity below 0
        if (quantity <= 0) {
            // inform the user
            Toast.makeText(this, "all products sold!",
                    Toast.LENGTH_SHORT).show();

            // set the quantity to 0 and display
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 0);

        }

        resolver.update(uri, values, null, null);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup button to open EditorActivity
        Button addProductButton = (Button) findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }


        });

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the cursor.
        // These is no product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        // setup item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {


                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentPetUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);

                intent.setData(currentPetUri);

                startActivity(intent);

            }

        });

        // Kick off the loader
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about


        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductContract.ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update ProductCursorAdapter with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from product database");
    }
}

