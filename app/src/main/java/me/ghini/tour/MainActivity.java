package me.ghini.tour;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * ghini.tour is a osmdroid-based tourist data browser.
 */
public class MainActivity extends AppCompatActivity {
    final private int NONE_SELECTED = -1;
    final private int LOCATION_TYPE = 1;
    final private int PANEL_TYPE = 2;

    GeoPoint chosenCentre = null;
    Integer chosenZoom = null;
    MapView map = null;
    MediaPlayer mediaPlayer = null;
    ItemizedOverlayWithFocus<OverlayItem> POIsOverlay;
    private int RSS_DOWNLOAD_REQUEST_CODE = 12392192;
    ItemizedOverlayWithFocus<OverlayItem> locationsOverlay;
    private int selectedType = NONE_SELECTED;
    private int selectedId = NONE_SELECTED;
    private String selectedTitle = "";
    private int chosenLocationId = NONE_SELECTED;
    private String chosenLocationTitle = "";
    private Bundle myState = new Bundle();

    private void updateBottomLine() {
        switch (selectedType) {
            case NONE_SELECTED: {
                findViewById(R.id.playPauseButton).setVisibility(View.INVISIBLE);
                findViewById(R.id.playStopButton).setVisibility(View.INVISIBLE);
                findViewById(R.id.chooseLocationButton).setVisibility(View.INVISIBLE);
                selectedTitle = chosenLocationTitle;
            }
            break;
            case PANEL_TYPE: {
                findViewById(R.id.playPauseButton).setVisibility(View.VISIBLE);
                findViewById(R.id.playStopButton).setVisibility(View.VISIBLE);
                findViewById(R.id.chooseLocationButton).setVisibility(View.INVISIBLE);
            }
            break;
            case LOCATION_TYPE: {
                findViewById(R.id.playPauseButton).setVisibility(View.INVISIBLE);
                findViewById(R.id.playStopButton).setVisibility(View.INVISIBLE);
                findViewById(R.id.chooseLocationButton).setVisibility(View.VISIBLE);
            }
            break;
        }
        TextView text = findViewById(R.id.text);
        text.setText(selectedTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void zoomToWorld() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(chosenLocationId != NONE_SELECTED) {
            selectedId = chosenLocationId;
            selectedTitle = chosenLocationTitle;
            selectedType = LOCATION_TYPE;
        } else {
            selectedType = NONE_SELECTED;
            selectedId = NONE_SELECTED;
        }
        updateBottomLine();

        map.getController().setZoom(5);
        map.getController().setCenter(new GeoPoint(10.0, -79.8));
        setZoom(5);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_zoom_world) {
            zoomToWorld();
            return true;
        } else if (id == R.id.action_zoom_gps) {
            try {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return false;
                }
                GeoPoint currentLocation;
                Location location = null;
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    map.getController().setCenter(currentLocation);
                }
            } catch (Exception e) {
                Toast.makeText(this, R.string.noGPSError, Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (id == R.id.action_zoom_location) {
            if (chosenLocationId == NONE_SELECTED) {
                Toast.makeText(this, "please first choose a location", Toast.LENGTH_LONG).show();
            } else {
                zoomToChosenLocation();
            }
            return true;
        } else if (id == R.id.action_get_locations) {
            URL ghiniWeb = null;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void zoomToChosenLocation() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        selectedId = chosenLocationId;
        selectedTitle = chosenLocationTitle;
        selectedType = LOCATION_TYPE;
        map.getController().setZoom(chosenZoom);
        map.getController().setCenter(chosenCentre);
        setZoom(chosenZoom);
        updateBottomLine();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            myState.putAll(savedInstanceState);
        } else {
            myState.putInt("worldZoom", 5);
            myState.putDouble("worldCentreLat", 10.0);
            myState.putDouble("worldCentreLon", -79.8);
            myState.putString("chosenLocationTitle", "");
        }

        Context context = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
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
                setZoom(e.getZoomLevel());
                return true;
            }
        }, 250 ));

        // my location position - it's an overlay
        MyLocationNewOverlay gpsLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_location_dot);
        gpsLocationOverlay.setPersonIcon(icon);
        gpsLocationOverlay.setPersonHotspot(icon.getHeight()/2,icon.getHeight()/2);
        gpsLocationOverlay.enableMyLocation();
        map.getOverlays().add(gpsLocationOverlay);

        // POIs and locations are two distinct overlays - both come from the database
        TaxonomyDatabase db = new TaxonomyDatabase(context);

        // the POI overlay
        POIsOverlay = new ItemizedOverlayWithFocus<>(context, db.getPOIs(),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            private void playback(final int index, final OverlayItem item, boolean start) {
                selectedType = PANEL_TYPE;
                selectedId = index;
                selectedTitle = item.getTitle();
                updateBottomLine();
                try {
                    int resID=getResources().getIdentifier(String.format(Locale.ENGLISH, "a%04d", index), "raw", getPackageName());
                    prepareMediaPlayer(resID, start);
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
        POIsOverlay.setFocusItemsOnTap(false);

        // the locations overlay
        locationsOverlay = new ItemizedOverlayWithFocus<>(context, db.getLocations(),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        selectedTitle = item.getTitle();
                        selectedId = Integer.parseInt(item.getSnippet());
                        selectLocation(false);
                        return false;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        selectedTitle = item.getTitle();
                        selectedId = Integer.parseInt(item.getSnippet());
                        selectLocation(true);
                        return false;
                    }
                    // local shared code
                    private void selectLocation(boolean choose) {
                        selectedType = LOCATION_TYPE;
                        if (choose) {
                            chosenLocationId = selectedId;
                            chosenLocationTitle = selectedTitle;
                            onChooseLocation(null);
                        }
                        updateBottomLine();
                    }
                });
        locationsOverlay.setFocusItemsOnTap(false);
        locationsOverlay.setEnabled(true);

        /* the scale bar */
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
        scaleBarOverlay.setCentred(true);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        // play around with these values to get the location on
        // screen in the right place for your application
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        map.getOverlays().add(scaleBarOverlay);

        map.getOverlays().add(new CopyrightOverlay(context));

        zoomToWorld();
    }

    private void setZoom(int zoomLevel) {
        List<Overlay> overlays = map.getOverlays();
        overlays.remove(locationsOverlay);
        overlays.remove(POIsOverlay);
        if (zoomLevel > 15) {
            if (selectedType == LOCATION_TYPE) {
                selectedType = NONE_SELECTED;
            }
            overlays.add(POIsOverlay);
        } else {
            if (selectedType == PANEL_TYPE) {
                selectedType = NONE_SELECTED;
            }
            overlays.add(locationsOverlay);
        }
        updateBottomLine();
    }

    void prepareMediaPlayer(int resID, boolean start){
        findViewById(R.id.playPauseButton).setVisibility(View.VISIBLE);
        findViewById(R.id.playStopButton).setVisibility(View.VISIBLE);
        findViewById(R.id.chooseLocationButton).setVisibility(View.INVISIBLE);
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), resID);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onPlayStop(findViewById(R.id.playStopButton));
            }
        });
        ImageButton playButton = findViewById(R.id.playPauseButton);
        if (start) {
            mediaPlayer.start();
            playButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            playButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    public void onPlayPause(View view) {
        if (mediaPlayer == null) {
            Toast.makeText(this, R.string.noPanelSelected, Toast.LENGTH_LONG).show();
            return;
        }
        ImageButton button = (ImageButton)view;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            button.setImageResource(R.drawable.ic_media_play);
        } else {
            mediaPlayer.start();
            button.setImageResource(R.drawable.ic_media_pause);
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

    public void onChooseLocation(View view) {
        if(selectedId == NONE_SELECTED){
            Toast.makeText(this, R.string.noLocationSelected, Toast.LENGTH_LONG).show();
        } else {
            TaxonomyDatabase db = new TaxonomyDatabase(getApplicationContext());
            selectedType = LOCATION_TYPE;
            chosenLocationId = selectedId;
            chosenLocationTitle = db.getLocationTitle(selectedId);
            chosenCentre = db.getLocationCentre(selectedId);
            chosenZoom = db.getLocationZoom(selectedId);
            zoomToChosenLocation();
        }
    }
}

class TaxonomyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "poi.db";
    private static final int DATABASE_VERSION = 5;

    TaxonomyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    List<OverlayItem> getPOIs() {
        List<OverlayItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select title, description, lat, lon from poi;", new String[] {});
        try {
            while (c.moveToNext()) {
                items.add(new OverlayItem(c.getString(0), c.getString(1), new GeoPoint(c.getDouble(2), c.getDouble(3))));
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
