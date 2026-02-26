package com.example.armariovirtual.utils;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.example.armariovirtual.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utils {

    public static String formatTimestamp(Context context, long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return context.getString(R.string.just_now);
        } else if (minutes < 60) {
            return context.getString(R.string.minutes_ago, minutes);
        } else if (hours < 24) {
            return context.getString(R.string.hours_ago, hours);
        } else if (days < 7) {
            return context.getString(R.string.days_ago, days);
        } else {
            Calendar pubCal = Calendar.getInstance();
            pubCal.setTimeInMillis(timestamp);
            int day = pubCal.get(Calendar.DAY_OF_MONTH);
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
            String month = monthFormat.format(pubCal.getTime());
            return context.getString(R.string.day_of_month, day, month);
        }
    }

    public static void goBack(AppCompatActivity activity) {
        activity.getOnBackPressedDispatcher().onBackPressed();
    }
}
