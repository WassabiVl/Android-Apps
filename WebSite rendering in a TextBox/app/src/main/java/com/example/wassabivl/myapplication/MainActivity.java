package com.example.wassabivl.myapplication;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS;

public class MainActivity extends AppCompatActivity {
    public static String a; //Global string to save the URL for mulitple uses
    private String TAG = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
        Context context = this;
        //https://developer.android.com/reference/android/net/http/HttpResponseCache.html
        try { //to create a cache
            File httpCacheDir = new File ( context.getCacheDir ( ), "http" );
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            Class.forName ( "android.net.http.HttpResponseCache" )
                    .getMethod ( "install", File.class, long.class )
                    .invoke ( null, httpCacheDir, httpCacheSize );
        } catch (Exception httpResponseCacheNotAvailable) {
            Log.i ( TAG, "HTTP response cache installation failed:" );
        }
    }
    //https://developer.android.com/reference/android/net/http/HttpResponseCache.html
    protected void onStop() { //to keep the cache functioning
        super.onStop ();
        HttpResponseCache cache = HttpResponseCache.getInstalled ( );
        if (cache != null) {
            cache.flush ( );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendMessage(View view) {
        // Do something in response to button
        EditText editText = ( EditText ) findViewById ( R.id.editText2 ); // call on the edittext widget
        String myURL = editText.getText ( ).toString ( ) ;
        if (!myURL.contains("http://www." ) || !myURL.contains("https://www." )){
            myURL = "http://www." + myURL;
        }//to add the standard http line
        if (URLUtil.isValidUrl ( myURL )) {
            new HTMLRender ( ).execute ( myURL ); //calls upon the background program to execute url from edittext
            MainActivity.a = (myURL); //used later to open  web browser
        }
        else { //to stop the program from running if the URL is not valid
            AlertDialog dialog = new AlertDialog.Builder ( this ).create ();
            dialog.setMessage ( "enter a valid URL" );
            dialog.show ();
        }
    }

    private class HTMLRender extends AsyncTask<String, Void, String> { //the background call to open the website
        @Override
        protected String doInBackground(String... urls) { //background task
        //https://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
            try {
                URL url = new URL ( urls[0] ); //takes the URL and creates a string
                URLConnection urlConnection = url.openConnection ( ); //opens a connection to the web server
                HttpURLConnection connection; //set a variable
                if (urlConnection instanceof HttpURLConnection) {
                    connection = ( HttpURLConnection ) urlConnection;
                    connection.setRequestMethod ( "GET" );
                }
                else {
                    return ("Please enter an HTTP URL.");
                }
                BufferedReader in = new BufferedReader (new InputStreamReader(connection.getInputStream()));
                String urlString = "";
                String current;
                while ((current = in.readLine ( )) != null) { // reads the website line by line
                    urlString += current; //adds each line to the string
                }
                in.close (); //to close the connection
                return urlString;
            }
            catch (MalformedURLException e) {
                Log.d( TAG, "Malformed URL in HTML reader: " + e);
                return ( "Malformed URLin HTML reader " + e.getMessage ());
            }
            catch (ConnectException e) {
                Log.d( TAG, "connection problem in HTML reader: " + e);
                return ("connection problem in HTML reader:" +e.getMessage ());
            }
            catch (IOException e) {
                Log.d( TAG, "IO problem in HTML reader: " + e);
                return  ( "IO problem in HTML reader: " + e.getMessage ());
            }
        }
        TextView textView = ( TextView ) findViewById ( R.id.textView2 );
        @Override
        protected void onPostExecute(String result) {
            this.textView = ( TextView ) findViewById ( R.id.textView2 );
            Spanned SpanHTML;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { //to handle depreciation
                SpanHTML = Html.fromHtml(result, FROM_HTML_OPTION_USE_CSS_COLORS, imgGetter , null );
            }
            else {
                SpanHTML = Html.fromHtml(result, imgGetter , null);
            }
            textView.setText ( SpanHTML ); //insert to textview
            textView.setMovementMethod ( new ScrollingMovementMethod () ); //allows scrolling
            // to open a webbrowser and view the page taken From Developer.android.com
            Intent i = new Intent (Intent.ACTION_VIEW); //intent to call on the method to open the web browser
            i.setData ( (Uri.parse(MainActivity.a)) ); //set the url
            startActivity ( i ); //start the activity
        }
        //Taken From http://stackoverflow.com/questions/3758535/display-images-on-android-using-textview-and-html-imagegetter-asynchronously
        //not properly functioning
        private ImageGetter imgGetter = new ImageGetter (){
            @Override
            public Drawable getDrawable(String source) {
                LevelListDrawable d = new LevelListDrawable();
                Drawable empty = ContextCompat.getDrawable(MainActivity.this, R.drawable.abc_btn_check_material);
                //Drawable empty = getResources().getDrawable(R.drawable.abc_btn_check_material); //this is the depreciated method
                d.addLevel(0, 0, empty);
                d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
                new ImgShitter ( MainActivity.this, source, d).execute(textView);
                return d;
            }

        };
    }
}
















