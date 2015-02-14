package com.standapp.activity.error;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.standapp.R;
import com.standapp.activity.MainActivity;
import com.standapp.activity.common.StandAppBaseActivity;
import com.standapp.google.googlefitapi.GoogleFitAPIHelper;
import com.standapp.logger.LogConstants;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ChromeExtErrorActivity extends StandAppBaseActivity {

    @Inject
    GoogleFitAPIHelper googleFitAPIHelper;

    @InjectView(R.id.tv_chromeext_error)
    TextView textInstructions;


    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LogConstants.LOG_ID, "Google Fit connected");
            googleFitAPIHelper.revokeFitPermissions(ChromeExtErrorActivity.this);
            replaceThisActivity(new Intent(ChromeExtErrorActivity.this, MainActivity.class));
        }

        @Override
        public void onConnectionSuspended(int i) {
            Toast.makeText(ChromeExtErrorActivity.this, ChromeExtErrorActivity.this.getString(R.string.toast_googlefit_disconnect_failed), Toast.LENGTH_LONG).show();
            if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                Log.i(LogConstants.LOG_ID, "Connection lost.  Cause: Network Lost.");
            } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                Log.i(LogConstants.LOG_ID, "Connection lost.  Reason: Service Disconnected");
            }

        }
    };

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.i(LogConstants.LOG_ID, "Connection failed. Cause: " + result.toString());
            if (!result.hasResolution()) {
                GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), ChromeExtErrorActivity.this, 0).show();
                return;
            } else {
                Toast.makeText(ChromeExtErrorActivity.this, ChromeExtErrorActivity.this.getString(R.string.toast_googlefit_disconnect_failed), Toast.LENGTH_SHORT).show();
                Toast.makeText(ChromeExtErrorActivity.this, getResources().getString(R.string.toast_googlefit_restart_app), Toast.LENGTH_LONG).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chrome_ext_error);
        ButterKnife.inject(this);

        setTextInstructions();
        googleFitAPIHelper.buildFitnessClient(connectionCallbacks, onConnectionFailedListener);
    }

    private void setTextInstructions() {
        Intent myIntent = getIntent(); // gets the previously created intent
        String userEmail = myIntent.getStringExtra(MainActivity.INTENT_PARAM_USER_EMAIL);
        textInstructions.setText(getString(R.string.chrome_ext_error, userEmail));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chrome_ext_error, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_chromeext_error_link)
    public void onClickLink(Button button) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.chrome_ext_error_link)));
        startActivity(browserIntent);
    }

    @OnClick(R.id.btn_already_have_ext)
    public void onClickAlreadyHaveExt(Button button) {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    @OnClick(R.id.btn_sign_in_different_account)
    public void onClickSignInDifferentAccount(Button button) {
        googleFitAPIHelper.connect();
    }

    private void replaceThisActivity(Intent intent) {
        this.startActivity(intent);
        this.finish();
    }

}
