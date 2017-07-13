package com.example.user.musicapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.media.MediaPlayer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Button btnJog, btnBike;
    MediaPlayer SongJog, SongBike;
    int playing;
    Sensor sensor;
    LocationManager locMan;
    LocationListener Spd;
    final static int RQS_OPEN_AUDIO_MP3 =1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnJog = (Button) findViewById(R.id.button);
        btnBike = (Button) findViewById(R.id.Bike);
        btnJog.setOnClickListener(bnJog);
        btnBike.setOnClickListener(bnBike);
        SongJog = new MediaPlayer();
        SongJog = MediaPlayer.create(this, R.raw.crazy);
        SongBike = new MediaPlayer();
        SongBike = MediaPlayer.create(this, R.raw.think);
        findViewById(R.id.toolbar);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Spd = new speed();
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, Spd);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission. ACCESS_FINE_LOCATION},1);
            return;
        }
        locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, Spd);
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(chooseMedia);

    }
    View.OnClickListener chooseMedia = new View.OnClickListener(){ //http://android-er.blogspot.de/2012/06/start-intent-to-choice-audiomp3-using.html

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(
                    intent, "Open Audio file"), RQS_OPEN_AUDIO_MP3);
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RQS_OPEN_AUDIO_MP3) {
                Uri audioFileUri = data.getData();

                SongJog=MediaPlayer.create(this,audioFileUri );
                SongBike=MediaPlayer.create(this,audioFileUri );
            }
        }
    }
    private class speed implements LocationListener{
        @Override
        public void onLocationChanged(Location loc) {
            Float MovSpeed =(loc.getSpeed()*3600)/1000;//convert to Km/h
            if (MovSpeed >=15){ //biking
                if(SongJog.isPlaying()){
                    SongJog.pause();
                }
                SongBike.start();
                playing = 1;
                btnJog.setText("Pause Crazy");
            }
            else if (MovSpeed >5 && MovSpeed <15){ //jogging speed
                if(SongBike.isPlaying()){
                    SongBike.pause();
                }
                SongJog.start();
                playing = 1;
                btnBike.setText("Pause Think");
            }
            else{

                if(SongJog.isPlaying()){
                    SongJog.pause();
                }else if(SongBike.isPlaying()){
                    SongBike.pause();
                }
                playing = 0;
            }
        }
        @Override
        public void onProviderDisabled(String arg0) {}
        @Override
        public void onProviderEnabled(String arg0) {}
        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

    }

    Button.OnClickListener bnJog = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (playing) {
                case 0:
                    if(SongBike.isPlaying()){
                        SongBike.pause();
                    }
                    SongJog.start();
                    playing = 1;
                    btnJog.setText("Pause Crazy");
                    break;
                case 1:
                    SongJog.pause();
                    playing = 0;
                    btnJog.setText("Play Crazy");
                    break;

            }
        }
    };

        Button.OnClickListener bnBike = new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (playing) {
                    case 0:
                        SongBike.start();
                        playing = 1;
                        btnBike.setText("Pause Think");
                        break;

                    case 1:
                        SongBike.pause();
                        playing = 0;
                        btnBike.setText("Play Think");
                        break;
                }

            }
        };

        //@Override
        //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        //return true;
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
        if (id == R.id.FFT) {
            startActivity(new Intent(getApplicationContext(),LiveView.class));
        }
        return super.onOptionsItemSelected(item);
    }
    public void onSensorChanged(SensorEvent event) {
        sensor = event.sensor;


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



