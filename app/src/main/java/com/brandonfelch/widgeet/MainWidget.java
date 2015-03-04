package com.brandonfelch.widgeet;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class MainWidget extends AppWidgetProvider {

    public static final String PREFS_NAME = "PixelPetPrefs";

    static float feedValue, playValue, washValue;
    static float feedTimesPerDay = 2f;
    static float playTimesPerDay = 4f;
    static float washTimesPerDay = 1f;

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
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        feedValue = prefs.getFloat("feed", 0.0f);
        playValue = prefs.getFloat("play", 0.0f);
        washValue = prefs.getFloat("wash", 0.0f);
        didInit = prefs.getBoolean("init", false);

        SharedPreferences.Editor edit = prefs.edit();

        // Update values
        if (didInit) {
            feedValue -= (1 / feedTimesPerDay) - .05f;
            playValue -= (1 / playTimesPerDay) - .05f;
            washValue -= (1 / washTimesPerDay) - .05f;

            Log.d("updateValues", "did Update");
        } else {
            feedValue = 1f;
            playValue = 1f;
            washValue = 1f;

            didInit = true;

            edit.putBoolean("init", didInit);

            Log.d("updateValues", "did Init");
        }

        edit.putFloat("feed", feedValue);
        edit.putFloat("play", playValue);
        edit.putFloat("wash", washValue);

        edit.commit();

        Log.d("updateValues", "in Update");

        views.setTextViewText(R.id.feedVal, String.format("%.2f", feedValue * 100) + "%");
        views.setTextViewText(R.id.playVal, String.format("%.2f", playValue * 100) + "%");
        views.setTextViewText(R.id.washVal, String.format("%.2f", washValue * 100) + "%");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


