package com.example.hellosearch;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.hellosearch.model.Business;
import com.example.hellosearch.model.Search;
import com.example.hellosearch.model.Yelp;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String LOGTAG = "MainActivity";

    private EditText searchText;
    private ImageView imageView;

    private LocationManager locationManager;
    private Location lastKnownLocation;
    private LocationListener mLocationListener;
    private Double mLongitude, mLatitude;

    final int LOCATION_REFRESH_TIME = 1;
    final int LOCATION_REFRESH_DISTANCE = 1;

    private RecyclerView imagesGrid;
    private ImagesAdapter imagesAdapter;
    private ArrayList<Business> businesses;

    private ProgressBar mProgressBar;

    private static final String STATE_SEARCH = "search";
    private static final String STATE_IMAGES = "images";

    private Search businessSearch;
    private Yelp yelp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();

                Log.d(LOGTAG, "Latitude: " + mLatitude + "Longitude: " + mLongitude);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_SEARCH)) {
                businessSearch = savedInstanceState.getParcelable(STATE_SEARCH);
            }
            if (savedInstanceState.containsKey(STATE_IMAGES)) {
                businesses = savedInstanceState.getParcelableArrayList(STATE_IMAGES);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Hello Search");
        mProgressBar = (ProgressBar) findViewById(R.id.main_imagesloading);

        if (businesses == null) {
            businesses = new ArrayList<>();
        }


        imagesGrid = (RecyclerView) findViewById(R.id.main_imagesgrid);
        imagesGrid.setItemAnimator(null);
        final RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 4);
        imagesGrid.setLayoutManager(layoutManager);

        imagesAdapter = new ImagesAdapter(businesses);
        imagesAdapter.setOnItemClickListener(new ImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                openImageResult(position, view);
            }
        });

        imagesAdapter.setMoreImagesCallback(new ImagesAdapter.MoreImagesCallback() {
            @Override
            public void onMoreImagesPressed() {
                nextPageAsync();
            }
        });
        imagesGrid.setAdapter(imagesAdapter);

        imageView = (ImageView) findViewById(R.id.search_clear_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText("");
                searchText.setHint(getString(R.string.abc_search_hint));
                businesses = new ArrayList<>();
                imagesAdapter = new ImagesAdapter(businesses);
                imagesGrid.setAdapter(imagesAdapter);
            }
        });
        searchText = (EditText) findViewById(R.id.search_edit_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String working = s.toString();
                if (working.length() >= 1) {
                    imageView.setImageResource(R.drawable.ic_clear_white_24dp);
                } else {
                    imageView.setImageResource(R.drawable.abc_ic_search_api_mtrl_alpha);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (searchText.length() > 0) {
                        // hide virtual keyboard
                        InputMethodManager imm = (InputMethodManager) getApplicationContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                        searchAsync(searchText.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });
    }

    //Was trying to implement a method get current location but ran out of time.
    //So I am hard coding the Longitude and Latitude of TrunkClub Chicago.
    private void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mLatitude = lastKnownLocation.getLatitude();
        mLongitude = lastKnownLocation.getLongitude();

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE,
                    mLocationListener);
    }

    private void searchAsync(final String query) {
        new AsyncTask<Void, Void, Search>() {
            @Override
            protected void onPreExecute() {
                //notifyAdapterEmpty();
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Search doInBackground(Void... nothing) {
                if(mLatitude == null || mLongitude == null){
                    mLatitude = 41.892179; //Trunk Club Chicago location.
                    mLongitude = -87.636655;
                }
                    yelp = new Yelp(query, mLatitude, mLongitude);
                    String response = yelp.searchForBusinessesByLocation();
                    return yelp.parseResponse(response);
            }

            @Override
            protected void onPostExecute(Search temp) {
                businessSearch = temp;
                mProgressBar.setVisibility(View.GONE);
                if (businessSearch != null) {

                    if (businessSearch.businesses != null) {
                        businesses.addAll(businessSearch.businesses);
                        imagesAdapter.notifyItemRangeInserted(0, businessSearch.businesses.size());
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "No Businesses Found", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }.execute();
    }

    private void nextPageAsync() {
        if (businessSearch != null) {
            new AsyncTask<Void, Void, Search>() {
                @Override
                protected Search doInBackground(Void... params) {
                    return yelp.nextPage();
                }
                @Override
                protected void onPostExecute(Search temp) {
                    businessSearch = temp;

                    if(businessSearch != null) {

                        if (businessSearch.businesses != null) {
                            businesses.addAll(businessSearch.businesses);
                            imagesAdapter.notifyItemRangeInserted(0, businessSearch.businesses.size());
                        }
                    }
                }
            }.execute();
        }
    }

    //If given more time, I was going to implement a method to clear the adapter properly.
    private void notifyAdapterEmpty() {
        int oldSize = businesses.size();
        int offset = businesses.isEmpty() ? 0 : 1;
        businesses.clear();
        imagesAdapter.notifyItemRangeRemoved(0, oldSize + offset);
    }

    private void openImageResult(final int position, final View viewToZoomFrom) {
        final LayoutInflater factory = getLayoutInflater();

        final View businessView = factory.inflate(R.layout.business_view, null);

        ImageView businessImage = (ImageView) businessView.findViewById(R.id.businessImage);

        Picasso.with(this).load(businesses.get(position).image_url).resize(400,400).into(businessImage);


        TextView businessNumber = (TextView) businessView.findViewById(R.id.businessNumber);
        businessNumber.setText(businesses.get(position).display_phone);

        ImageView businessRating = (ImageView) businessView.findViewById(R.id.businessRating);

        Picasso.with(this).load(businesses.get(position).rating_img_url).into(businessRating);
        new MaterialDialog.Builder(this)
                .title(businesses.get(position).businessName)
                .customView(businessView, true)
                .positiveText("Go Back")
                .show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_SEARCH, businessSearch);
        outState.putParcelableArrayList(STATE_IMAGES, businesses);
    }
}
