package uk.ac.aston.cs3mdd.whichdayapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

  private GoogleMap googleMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    // Initialize the map
    FragmentManager fragmentManager = getSupportFragmentManager();
    SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.mapFragmentContainer);

    if (mapFragment == null) {
      mapFragment = SupportMapFragment.newInstance();
      fragmentManager.beginTransaction()
              .replace(R.id.mapFragmentContainer, mapFragment)
              .commit();
    }

    // Get notified when the map is ready to be used
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull GoogleMap map) {
    this.googleMap = map;

    // Load bookmarks and add markers to the map
    loadAndDisplayBookmarks();
  }

  private void loadAndDisplayBookmarks() {
    // Use Room database to fetch bookmarks
    AppDatabase db = AppDatabase.getInstance(this);

    Executors.newSingleThreadExecutor().execute(() -> {
      List<Bookmark> bookmarks = db.bookmarkDao().getAllBookmarks();

      // Update the UI on the main thread
      runOnUiThread(() -> {
        if (googleMap != null) {
          for (Bookmark bookmark : bookmarks) {
            LatLng location = new LatLng(bookmark.getLatitude(), bookmark.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(bookmark.getCityName()));
          }

          // Center the map on the first bookmark (if available)
          if (!bookmarks.isEmpty()) {
            LatLng firstLocation = new LatLng(bookmarks.get(0).getLatitude(), bookmarks.get(0).getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10));
          }
        }
      });
    });
  }
}
