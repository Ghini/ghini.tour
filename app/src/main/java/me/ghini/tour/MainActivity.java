package me.ghini.tour;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    MapView map = null;
    MediaPlayer mediaPlayer = null;
    ItemizedOverlayWithFocus<OverlayItem> POIOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        setContentView(R.layout.activity_fullscreen);

        map = findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(final ZoomEvent e) {
                POIOverlay.setEnabled(e.getZoomLevel() > 16);
                return true;
            }
        }, 250 ));

        // my location position - it's an overlay
        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_location_dot);
        locationOverlay.setPersonIcon(icon);
        locationOverlay.setPersonHotspot(icon.getHeight()/2,icon.getHeight()/2);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);

        // POIs is an other overlay - points should come from a database
        TaxonomyDatabase db = new TaxonomyDatabase(context);

        List<OverlayItem> items = db.getPOIs();

        // the POI overlay
        POIOverlay = new ItemizedOverlayWithFocus<OverlayItem>(context, items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            private void playback(final int index, final OverlayItem item, boolean start){
                TextView text = findViewById(R.id.text);
                text.setText(item.getTitle());
                try {
                    int resID=getResources().getIdentifier(String.format(Locale.ENGLISH, "a%04d", index), "raw", getPackageName());
                    startPlayback(resID, start);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                playback(index, item, false);
                return false;
            }
            @Override
            public boolean onItemLongPress(final int index, final OverlayItem item) {
                playback(index, item, true);
                return false;
            }
        });
        POIOverlay.setFocusItemsOnTap(false);
        map.getOverlays().add(POIOverlay);
        POIOverlay.setEnabled(false);

        /* the initial centre point */
        IMapController mapController = map.getController();
//        GeoPoint startPoint = new GeoPoint(5.5, -74.5);
//        mapController.setZoom(5);
        mapController.setZoom(16);
        GeoPoint startPoint = new GeoPoint(7.5925, -80.9625);
        mapController.setCenter(startPoint);

        /* the scale bar */
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
        scaleBarOverlay.setCentred(true);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        // play around with these values to get the location on
        // screen in the right place for your application
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        map.getOverlays().add(scaleBarOverlay);

        map.getOverlays().add(new CopyrightOverlay(context));
    }

    void startPlayback(int resID, boolean start){
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), resID);
        ImageButton button = findViewById(R.id.playPauseButton);
        if (start) {
            mediaPlayer.start();
            button.setImageResource(R.drawable.ic_media_pause);
        } else {
            button.setImageResource(R.drawable.ic_media_play);
        }
    }

    public void onPlayPause(View view) {
        if (mediaPlayer != null) {
            ImageButton button = (ImageButton)view;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                button.setImageResource(R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                button.setImageResource(R.drawable.ic_media_pause);
            }
        }
    }

    public void onPlayStop(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            ImageButton button = findViewById(R.id.playPauseButton);
            button.setImageResource(R.drawable.ic_media_play);
        }
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    public void onPause(){
        super.onPause();
    }

}

class TaxonomyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "poi.db";
    private static final int DATABASE_VERSION = 4;

    TaxonomyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    List<OverlayItem> getPOIs() {
        List<OverlayItem> items = new ArrayList<OverlayItem>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        c = db.rawQuery("select title, description, lat, lon from poi;", new String[] {});
        try {
            while (c.moveToNext()) {
                items.add(new OverlayItem(c.getString(0), c.getString(1), new GeoPoint(c.getDouble(2), c.getDouble(3))));
            }
        } finally {
            c.close();
        }
        return items;
    }
}