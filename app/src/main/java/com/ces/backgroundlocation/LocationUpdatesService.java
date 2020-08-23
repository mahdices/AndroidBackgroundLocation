package com.ces.backgroundlocation;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class LocationUpdatesService extends Service {

    public final static int NOTIFICATION_ID = 1001;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback mLocationCallback;

    private static final int LOCATION_INTERVAL = 10 * 1000;
    Location mLocation;
    NotificationManager mNotificationManager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL / 2);
//        locationRequest.setSmallestDisplacement(100);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    mLocation = locationResult.getLastLocation();
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification());

                }

            }
        };
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkBackgroundLocation()) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    mLocationCallback, Looper.myLooper());
        }

        return START_STICKY;
    }

    public boolean checkBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        String text = mLocation == null ? "لوکیشن دریافت نشد" : mLocation.getLatitude() + "," +
                mLocation.getLongitude();

        NotificationCompat.Builder notification = new NotificationCompat.Builder                                                                                                                                                                                                                  (this, "RunLocation")
                .setContentTitle("Android Location")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);


        return notification.build();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "RunLocation",
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            mNotificationManager.createNotificationChannel(serviceChannel);
        }
    }


}