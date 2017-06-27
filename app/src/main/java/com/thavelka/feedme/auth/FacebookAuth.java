package com.thavelka.feedme.auth;

import android.app.Activity;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Provide authentication using users Facebook account.
 * https://developers.facebook.com/docs/facebook-login/android
 */
public abstract class FacebookAuth {
    private final LoginButton loginButton;
    private final CallbackManager callbackManager;

    public FacebookAuth(final LoginButton loginBtn) {
        callbackManager = CallbackManager.Factory.create();
        this.loginButton = loginBtn;
        loginButton.setReadPermissions("email");

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                onRegistrationComplete(loginResult);
            }

            @Override
            public void onCancel() {
                onAuthCancelled();
            }

            @Override
            public void onError(FacebookException exception) {
                onAuthError(exception);
            }
        });

    }

    /**
     * Called if the authentication is cancelled by the user.
     *
     * Adapter method, developer might want to override this method  to provide
     * custom logic.
     */
    public void onAuthCancelled() {}

    /**
     * Called if the authentication fails.
     *
     * Adapter method, developer might want to override this method  to provide
     * custom logic.
     */
    public void onAuthError (FacebookException error) {}

    /**
     * Notify this class about the {@link Activity#onResume()} event.
     */
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Called once we obtain a token from Facebook API.
     * @param loginResult contains the token obtained from Facebook API.
     */
    public abstract void onRegistrationComplete(final LoginResult loginResult);
}
