package com.thavelka.feedme.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.thavelka.feedme.R;
import com.thavelka.feedme.models.Listing;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ListingsActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private Realm realm;
    private RealmResults<Listing> listings;
    private RealmChangeListener<RealmResults<Listing>> changeListener;
    private ListingAdapter adapter;

    @BindView(R.id.listings_navigation) BottomNavigationView bottomNavigationView;
    @BindView(R.id.listings_recyclerView) RecyclerView recyclerView;
    @BindView(R.id.listings_fab) FloatingActionButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        setContentView(R.layout.activity_listings);
        ButterKnife.bind(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        listings = realm.where(Listing.class).findAll();
        changeListener = element -> {
            if (element != null && element.isValid() && element.isLoaded() && adapter != null) {
                adapter.notifyDataSetChanged();
            }
        };
        listings.addChangeListener(changeListener);
        adapter = new ListingAdapter(this, listings, listing ->
                Toast.makeText(this, "Tapped listing", Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        if (changeListener != null) listings.removeChangeListener(changeListener);
        super.onDestroy();
        if (realm != null) realm.close();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                return true;
            case R.id.navigation_dashboard:
                return true;
            case R.id.navigation_notifications:
                return true;
        }
        return false;
    }

    @OnClick(R.id.listings_fab) void onClickFab() {
        Intent i = new Intent(this, EditListingActivity.class);
        i.putExtra(EditListingActivity.ARG_LISTING_TYPE, Listing.TYPE_FOOD);
        startActivity(i);
    }
}
