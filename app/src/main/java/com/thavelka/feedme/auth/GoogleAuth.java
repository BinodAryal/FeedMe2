package com.thavelka.feedme.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.thavelka.feedme.utils.Constants;

/**
 * Provide authentication using users Google account registered with the device.
 * https://developers.google.com/identity/sign-in/android/start-integrating
 */
public abstract class GoogleAuth implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 10;


    public GoogleAuth(final SignInButton btnSignIn, final FragmentActivity fragmentActivity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                .enableAutoManage(fragmentActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btnSignIn.setOnClickListener(v -> {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            fragmentActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    public GoogleAuth(final SignInButton btnSignIn, final FragmentActivity fragmentActivity, final Fragment fragment) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                .enableAutoManage(fragmentActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btnSignIn.setOnClickListener(v -> {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            fragment.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    /**
     * Notify this class about the {@link FragmentActivity#onResume()} event.
     */
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if(!connectionResult.hasResolution()) {
            onError("Connection failed and has no resolution. code:" + connectionResult.getErrorCode());
        }
    }

    /**
     * Called once we obtain a token from Google Sign In API.
     * @param result contains the token obtained from Google Sign In API.
     */
    public abstract void onRegistrationComplete(final GoogleSignInResult result);

    /**
     * Called in case of authentication or other errors.
     *
     * Adapter method, developer might want to override this method  to provide
     * custom logic.
     */
    public void onError(String s) {}

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            onRegistrationComplete(result);
        } else {
            onError(String.format("Google sign-in failed (status = %s message = %s)", result.getStatus(),
                    result.getStatus().getStatusMessage()));
        }
    }
}