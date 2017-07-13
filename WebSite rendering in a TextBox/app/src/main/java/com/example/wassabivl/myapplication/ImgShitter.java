package com.example.wassabivl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static android.content.ContentValues.TAG;

/**
 * Created by Wassabi.vl on 19/04/2017.
 * Taken From:
 * http://stackoverflow.com/questions/3758535/display-images-on-android-using-textview-and-html-imagegetter-asynchronously
 */

  class ImgShitter extends AsyncTask<TextView, Void, Bitmap> {
        private LevelListDrawable levelListDrawable;
        private Context context;
        private String source;
        private TextView t;

        ImgShitter(Context context, String source, LevelListDrawable levelListDrawable) {
            this.context = context;
            this.source = source;
            this.levelListDrawable = levelListDrawable;
        }

        @Override
        protected Bitmap doInBackground(TextView... params) {
            t = params[0];
            try {
                Log.d( TAG, "Downloading the image from: " + source);
                return Picasso.with(context).load(source).get();
            } catch (Exception e) {
                Log.i ( TAG, "ImgShitter Problem in background process:" + e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            try {
                Drawable d = new BitmapDrawable (context.getResources(), bitmap);
                Point size = new Point ();
                ((Activity ) context).getWindowManager().getDefaultDisplay().getSize(size);
               int multiplier;
                //Lets calculate the ratio according to the screen width in px
               if (bitmap.getWidth () != 0) {
                    multiplier = size.x / bitmap.getWidth ( );
                    Log.d(TAG, "multiplier: " + multiplier);
                }
                else { multiplier = size.x / 10;
                Log.d(TAG, "multiplier: " + multiplier);}
                levelListDrawable.addLevel(1, 1, d);
                // Set bounds width  and height according to the bitmap resized size
                levelListDrawable.setBounds(0, 0, bitmap.getWidth()* multiplier , bitmap.getHeight() * multiplier);
                levelListDrawable.setLevel(1);
                t.setText(t.getText()); // invalidate() doesn't work correctly...
            } catch (Exception e) {
                Log.i ( TAG, "ImgShitter Problem on execute:" + e);
                /* Like a null bitmap, etc. */ }
        }
    }