/**
 * Copyright 2017. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.nearby.message;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MessageListener mMessageListener;

    private GoogleApiClient mGoogleApiClient;

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;

    private GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener;

    private MessageDatAdaptor datAdaptor;

    private RecyclerView messageRecyclerView;

    private boolean mSubscribed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                subscribe();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*Intent intent = new Intent()
                .setData(new Uri.Builder()
                        .scheme("yourscheme")
                        .authority("host")
                        .appendPath("path")
                        .build())
                .setPackage("com.yourapp.ui");
        Log.i("TEST >>>>", "Use this intent url: " + intent.toUri(Intent.URI_INTENT_SCHEME));*/

        //initMessageListener();
        messageRecyclerView = (RecyclerView) findViewById(R.id.nearbyList);
        datAdaptor = new MessageDatAdaptor();
        messageRecyclerView.setAdapter(datAdaptor);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        BackgroundSubscribeIntentService.setMessageDatAdaptor(datAdaptor);
        initGoogleApiClient();
    }

    private void subscribe() {
        if (mSubscribed)
            return;
        else
            mSubscribed = true;

        Log.i("NEARBY BLE", "Clicked on subscribe...");
        Toast.makeText(MainActivity.this, "Subscribing.", Toast.LENGTH_SHORT).show();
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .build();
        Nearby.Messages.subscribe(mGoogleApiClient, getPendingIntent(), options).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Toast.makeText(MainActivity.this, "Subscribed successfully.", Toast.LENGTH_SHORT).show();
                    startService(getBackgroundSubscribeServiceIntent());
                } else {
                    Toast.makeText(MainActivity.this, "Could not subscribe, status = " + status + " decoded value = "
                            + NearbyMessagesStatusCodes.getStatusCodeString(status.getStatusCode()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getService(this, 0,
                getBackgroundSubscribeServiceIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Intent getBackgroundSubscribeServiceIntent() {
        return new Intent(this, BackgroundSubscribeIntentService.class);
    }

    /*private void initMessageListener() {
        if (mMessageListener == null) {
            mMessageListener = new MessageListener() {
                @Override
                public void onFound(Message message) {
                    String messageAsString = new String(message.getContent());
                    Log.d("NEARBY BLE", "Found message: " + messageAsString);
                    Toast.makeText(MainActivity.this, "Found message: " + messageAsString, Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, new String(message.getContent()), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLost(Message message) {
                    String messageAsString = new String(message.getContent());
                    Log.d("NEARBY BLE", "Lost sight of message: " + messageAsString);
                    Toast.makeText(MainActivity.this, "Lost sight of message: " + messageAsString, Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, new String(message.getContent()), Toast.LENGTH_SHORT).show();
                }
            };
        }
    }*/

    private void initGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    Log.d("NEARBY BLE", "Connected");
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    subscribe();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.d("NEARBY BLE", "Connection suspended");
                    Toast.makeText(MainActivity.this, "Connection suspended", Toast.LENGTH_SHORT).show();
                }
            };

            mOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Log.d("NEARBY BLE", "Connection failed");
                    Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                }
            };

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Nearby.MESSAGES_API)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .enableAutoManage(this, mOnConnectionFailedListener)
                    .build();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
