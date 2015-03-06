package com.brandonfelch.widgeet;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;


/**
 * Implementation of App Widget functionality.
 */
public class MainWidget extends AppWidgetProvider {

    static String debugTag = "debugWidget";

    public static final String WIDGET_FEED_BUTTON = "WidgetFeedButton";
    public static final String WIDGET_PLAY_BUTTON = "WidgetPlayButton";
    public static final String WIDGET_WASH_BUTTON = "WidgetWashButton";

    public static final String WIDGET_RAND_BUTTON = "WidgetRandButton";

    static float feedValue, playValue, washValue;
    static float feedTimesPerDay = 2f;
    static float playTimesPerDay = 4f;
    static float washTimesPerDay = 1f;

    static float numUpdatesPerDay = 24f * 2; // updates every half hour

    static boolean didInit;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        didInit = false;
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();

        edit.clear();
        edit.commit();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();

        feedValue = prefs.getFloat("feed", 0.0f);
        playValue = prefs.getFloat("play", 0.0f);
        washValue = prefs.getFloat("wash", 0.0f);

        switch (intent.getAction()) {
            case WIDGET_FEED_BUTTON:
                feedValue += incrementValue();

                edit.putFloat("feed", feedValue);

                edit.commit();

                Log.d(debugTag, "feed pressed: " + feedValue);
                break;
            case WIDGET_PLAY_BUTTON:
                playValue += incrementValue();

                edit.putFloat("play", playValue);

                edit.commit();

                Log.d(debugTag, "play pressed: " + playValue);
                break;
            case WIDGET_WASH_BUTTON:
                washValue += incrementValue();

                edit.putFloat("wash", washValue);

                edit.commit();

                Log.d(debugTag, "wash pressed: " + washValue);
                break;
            case WIDGET_RAND_BUTTON:
                randomValues(edit);

                Log.d(debugTag, "rand pressed");
                break;
        }

        Log.d(debugTag, "button pressed");
        super.onReceive(context, intent);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);
        updateWidgetViews(views, AppWidgetManager.getInstance(context), prefs.getInt("appId", 0));
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);

        Intent feedIntent = new Intent(WIDGET_FEED_BUTTON);
        PendingIntent pendingFeedIntent = PendingIntent.getBroadcast(context, 0, feedIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.feedButton, pendingFeedIntent);

        Intent playIntent = new Intent(WIDGET_PLAY_BUTTON);
        PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.playButton, pendingPlayIntent);

        Intent washIntent = new Intent(WIDGET_WASH_BUTTON);
        PendingIntent pendingWashIntent = PendingIntent.getBroadcast(context, 0, washIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.washButton, pendingWashIntent);

        Intent randIntent = new Intent(WIDGET_RAND_BUTTON);
        PendingIntent pendingRandIntent = PendingIntent.getBroadcast(context, 0, randIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.randButton, pendingRandIntent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        feedValue = prefs.getFloat("feed", 0.0f);
        playValue = prefs.getFloat("play", 0.0f);
        washValue = prefs.getFloat("wash", 0.0f);
        didInit = prefs.getBoolean("init", false);

        SharedPreferences.Editor edit = prefs.edit();

        edit.putInt("appId", appWidgetId);

        // Update values
        if (didInit) {
            feedValue -= feedTimesPerDay / numUpdatesPerDay;
            playValue -= playTimesPerDay / numUpdatesPerDay;
            washValue -= washTimesPerDay / numUpdatesPerDay;

            Log.d("updateValues", "did Update");
        } else {
            feedValue = 1f;
            playValue = 1f;
            washValue = 1f;

            didInit = true;

            edit.putBoolean("init", didInit);

            Log.d(debugTag, "did Init");
        }

        edit.putFloat("feed", feedValue);
        edit.putFloat("play", playValue);
        edit.putFloat("wash", washValue);

        updateWidgetViews(views, appWidgetManager, appWidgetId);

        edit.commit();

        Log.d(debugTag, "in Update");
    }

    static void updateWidgetViews(RemoteViews views, AppWidgetManager appWidgetManager, int appWidgetId) {
        views.setTextViewText(R.id.feedVal, String.format("%.2f", feedValue * 100) + "%");
        views.setTextViewText(R.id.playVal, String.format("%.2f", playValue * 100) + "%");
        views.setTextViewText(R.id.washVal, String.format("%.2f", washValue * 100) + "%");

        views.setTextColor(R.id.feedVal, Color.parseColor(getTextColor(getState(feedValue))));
        views.setTextColor(R.id.playVal, Color.parseColor(getTextColor(getState(playValue))));
        views.setTextColor(R.id.washVal, Color.parseColor(getTextColor(getState(washValue))));

        switch (getState(feedValue)) {
            case 0:
                views.setImageViewResource(R.id.feedImgView, R.drawable.eyes_wide);
                break;
            case 1:
                views.setImageViewResource(R.id.feedImgView, R.drawable.eyes_neutral);
                break;
            case 2:
                views.setImageViewResource(R.id.feedImgView, R.drawable.eyes_heavy);
                break;
        }

        switch (getState(playValue)) {
            case 0:
                views.setImageViewResource(R.id.playImgView, R.drawable.mouth_smile);
                break;
            case 1:
                views.setImageViewResource(R.id.playImgView, R.drawable.mouth_neutral);
                break;
            case 2:
                views.setImageViewResource(R.id.playImgView, R.drawable.mouth_frown);
                break;
        }

        switch (getState(washValue)) {
            case 0:
                views.setImageViewResource(R.id.washImgView, R.drawable.stink_none);
                break;
            case 1:
                views.setImageViewResource(R.id.washImgView, R.drawable.stink_light);
                break;
            case 2:
                views.setImageViewResource(R.id.washImgView, R.drawable.stink_heavy);
                break;
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static int getState(float value) {
         if (value < 1.05f && value > .67f) {
            return 0;
        } else if (value > .33f) {
            return 1;
        } else {
            return 2;
        }
    }

    static String getTextColor(int state) {
        switch (state) {
            case 0:
                return "#FFFFFF";
            case 1:
                return "#FFCC00";
            case 2:
                return "#CC0000";
        }

        return "#000000";
    }

    static void randomValues(SharedPreferences.Editor edit) {
        Random rand = new Random();
        feedValue = 1.2f * rand.nextFloat();
        playValue = 1.2f * rand.nextFloat();
        washValue = 1.2f * rand.nextFloat();

        edit.putFloat("feed", feedValue);
        edit.putFloat("play", playValue);
        edit.putFloat("wash", washValue);

        edit.commit();
    }

    static float incrementValue() {
        return .2f + (.2f * new Random().nextFloat());
    }
}


