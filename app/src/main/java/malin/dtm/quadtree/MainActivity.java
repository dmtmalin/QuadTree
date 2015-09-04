package malin.dtm.quadtree;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.text.DateFormat;
import java.util.Date;

import controllers.QuadController;
import controllers.RabbitController;

/**
 * Created by dmt on 30.08.2015.
 */
public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private static final int QUAD_TREE_DEPTH = 25;
    private static final int QUAD_TREE_ACCURACY = 7;
    private static final int INTERVAL_UPDATE = 10000;
    /**
    *  Quad tree logic
    */
    protected QuadController mQuadController;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    /**
     * index for point
     */
    protected String mIndexPoint;
    /**
     * index for quad
     */
    protected String mIndexQuad;
    /**
     * Rabbit MQ controller;
     */
    protected RabbitController mRabbitController;
    /**
     * Handler for view Rabbit message
     */
    protected Handler mIncomingHandler;
    //UI widgets
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected TextView mLastUpdateText;
    protected TextView mIndexPointText;
    protected TextView mIndexQuadText;
    protected TextView mRedisText;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mLatitudeText = (TextView) findViewById(R.id.latitude_text);
        mLongitudeText = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateText = (TextView) findViewById(R.id.lastUpdate_text);
        mIndexPointText = (TextView) findViewById(R.id.indexPoint_text);
        mIndexQuadText = (TextView) findViewById(R.id.indexQuad_text);
        mRedisText = (TextView) findViewById(R.id.rabbit_text);

        setupIncomingHandler();

        mQuadController = new QuadController(QUAD_TREE_DEPTH, QUAD_TREE_ACCURACY);
        mRabbitController = new RabbitController();

        setupPublishButton();
        setupAccuracyEdit();

        mIndexQuad = "";
        mIndexPoint = "";
        mLastUpdateTime = "";
        mRequestingLocationUpdates = false;

        buildGoogleApiClient();
    }

    void setupAccuracyEdit() {
        final EditText accuracyEdit = (EditText) findViewById(R.id.accuracy_edit);
        accuracyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = accuracyEdit.getText().toString();
                int accuracy = text.isEmpty() ? QUAD_TREE_ACCURACY : Integer.parseInt(text);
                mQuadController.setAccuracy(accuracy);
            }

        });
    }

    void setupPublishButton() {
        Button button = (Button) findViewById(R.id.push_button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText et = (EditText) findViewById(R.id.push_edit);
                String message = et.getText().toString();
                if (!message.isEmpty()) {
                    mRabbitController.publish(message);
                    et.setText("");
                }
            }
        });
    }

    void setupIncomingHandler() {
        mIncomingHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                TextView tv = (TextView) findViewById(R.id.rabbit_text);
                tv.append(DateFormat.getTimeInstance().format(new Date()) + ' ' + message + '\n');
                return true;
            }
        });
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL_UPDATE);
        mLocationRequest.setFastestInterval(INTERVAL_UPDATE / 2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        mRabbitController.destroy();
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if(mLastLocation == null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateControllers();
            updateUI();
        }
        if(!mRequestingLocationUpdates) {
            //start update location
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            mLastUpdateText.setText(mLastUpdateTime);
            mIndexPointText.setText(mIndexPoint);
            mIndexQuadText.setText(mIndexQuad);
        }
    }

    /**
     * Updates quad tree and if quad change recreate rabbit controller
     */
    private void updateControllers() {
        if(mLastLocation != null) {
            updateQuadTree();
            if (mQuadController.changed())
                updateRabbit(mQuadController.getIndexQuad());
        }
    }

    private void updateQuadTree() {
        mQuadController.update(mLastLocation);
        mIndexPoint = mQuadController.getIndexPoint();
        mIndexQuad = mQuadController.getIndexQuad();
    }

    private void updateRabbit(String routingKey) {
        mRabbitController.destroy();
        mRabbitController.buildPublish(routingKey);
        mRabbitController.buildSubscribe(routingKey, mIncomingHandler);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        mLatitudeText.setText("Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateControllers();
        updateUI();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
}