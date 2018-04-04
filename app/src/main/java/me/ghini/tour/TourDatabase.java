package me.ghini.tour;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by mario on 3/11/18.
 */


class TourDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "poi.db";
    private static final int DATABASE_VERSION = 7;

    TourDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    List<OverlayItem> getPOIs() {
        List<OverlayItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select title, location_id, sub_id, lat, lon from poi;",
                new String[] {});
        try {
            while (c.moveToNext()) {
                String snippet = String.format(
                        Locale.ENGLISH, "g%03d_%04d",
                        c.getInt(1), c.getInt(2));
                items.add(new OverlayItem(
                        c.getString(0), snippet,
                        new GeoPoint(c.getDouble(3), c.getDouble(4))));
            }
        } finally {
            c.close();
        }
        return items;
    }

    List<OverlayItem> getLocations() {
        List<OverlayItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String q = "select title, id, lat, lon, zoom from location";
        Cursor c = db.rawQuery(q, new String[] {});
        try {
            while (c.moveToNext()) {
                items.add(new OverlayItem(c.getString(0), c.getString(1), new GeoPoint(c.getDouble(2), c.getDouble(3))));
            }
        } finally {
            c.close();
        }
        return items;
    }

    GeoPoint getLocationCentre(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String q = "select title, description, lat, lon, zoom from location where id=?";
        Cursor c = db.rawQuery(q, new String[] {Integer.toString(id)});
        GeoPoint result = null;
        if(c.moveToFirst()) {
            result = new GeoPoint(c.getDouble(2), c.getDouble(3));
        }
        c.close();
        return result;
    }

    Integer getLocationZoom(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String q = "select title, description, lat, lon, zoom from location where id=?";
        Cursor c = db.rawQuery(q, new String[] {Integer.toString(id)});
        Integer result = null;
        if(c.moveToFirst()) {
            result = c.getInt(4);
        }
        c.close();
        return result;
    }

    String  getLocationTitle(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String q = "select title, description, lat, lon, zoom from location where id=?";
        Cursor c = db.rawQuery(q, new String[] {Integer.toString(id)});
        String result = null;
        if(c.moveToFirst()) {
            result = c.getString(0);
        }
        c.close();
        return result;
    }
}
