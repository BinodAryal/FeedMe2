package com.thavelka.feedme.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.thavelka.feedme.R;
import com.thavelka.feedme.auth.FacebookAuth;
import com.thavelka.feedme.auth.GoogleAuth;
import com.thavelka.feedme.ui.ListingsActivity;
import com.thavelka.feedme.utils.Constants;
import com.thavelka.feedme.utils.UserManager;

import java.net.ConnectException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.ObjectServerError;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import timber.log.Timber;

public class AuthActivity extends AppCompatActivity implements AuthFragment.AuthListener,
        EmailAuthFragment.EmailAuthListener, SyncUser.Callback {

    private GoogleAuth googleAuth;
    private FacebookAuth facebookAuth;

    @BindView(R.id.auth_container) FrameLayout container;
    @BindView(R.id.auth_progress) ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        // Check if current user exists
        final SyncUser user = SyncUser.currentUser();
        if (user != null) {
            finishLogin(user);
            return;
        }

        // Show auth options
        configureAuth();
        AuthFragment authFragment = new AuthFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.auth_container, authFragment)
                .commit();
    }

    @Override
    public void onClickGoogleSignIn() {
        googleAuth.signIn();
    }

    @Override
    public void onClickEmailAuth(boolean newUser) {
        EmailAuthFragment fragment = EmailAuthFragment.newInstance(newUser);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.auth_container, fragment)
                .addToBackStack("onClickEmailAuth")
                .commit();
    }

    @Override
    public void signInWithEmail(String email, String password) {
        SyncCredentials credentials = SyncCredentials.usernamePassword(email, password, false);
        attemptLogin(credentials);
    }

    @Override
    public void registerWithEmail(String email, String password) {
        SyncCredentials credentials = SyncCredentials.usernamePassword(email, password, true);
        attemptLogin(credentials);
    }

    @Override
    public void onSuccess(SyncUser user) {
        showProgress(false);
        finishLogin(user);
    }

    @Override
    public void onError(ObjectServerError error) {
        showProgress(false);
        Timber.e(error);
        String msg;
        switch (error.getErrorCode()) {
            case IO_EXCEPTION:
                msg = getString(R.string.auth_error_network);
                break;
            case UNKNOWN_ACCOUNT:
                msg = getString(R.string.auth_error_unknown_account);
                break;
            case INVALID_CREDENTIALS:
                msg = getString(R.string.auth_error_credentials);
                break;
            default:
                Throwable t = error.getException();
                if (t != null && t instanceof ConnectException) {
                    msg = getString(R.string.auth_error_network);
                    break;
                }
                msg = getString(R.string.auth_error_default);
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleAuth.onActivityResult(requestCode, resultCode, data);
        facebookAuth.onActivityResult(requestCode, resultCode, data);
    }

    private void configureAuth() {
        // Set up google auth
        googleAuth = new GoogleAuth(this) {
            @Override
            public void onRegistrationComplete(GoogleSignInResult result) {
                UserManager.setAuthMode(UserManager.AUTH_MODE.GOOGLE);
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null && acct.getIdToken() != null) {
                    SyncCredentials credentials = SyncCredentials.google(acct.getIdToken());
                    attemptLogin(credentials);
                }
            }

            @Override
            public void onError(String s) {
                Timber.e(s);
                Toast.makeText(AuthActivity.this, R.string.auth_error_google,
                        Toast.LENGTH_SHORT).show();
            }
        };

        // Configure facebook auth
        facebookAuth = new FacebookAuth() {
            @Override
            public void onRegistrationComplete(LoginResult loginResult) {
                UserManager.setAuthMode(UserManager.AUTH_MODE.FACEBOOK);
                SyncCredentials credentials =
                        SyncCredentials.facebook(loginResult.getAccessToken().getToken());
                attemptLogin(credentials);
            }

            @Override
            public void onAuthError(FacebookException error) {
                Timber.e(error);
                Toast.makeText(AuthActivity.this, R.string.auth_error_facebook,
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void attemptLogin(SyncCredentials credentials) {
        showProgress(true);
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, this);
    }

    private void finishLogin(SyncUser user) {
        UserManager.setActiveUser(user);
        Intent intent = new Intent(this, ListingsActivity.class);
        startActivity(intent);
        finish();
    }

    private void showProgress(final boolean show) {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        container.setVisibility(show ? View.VISIBLE : View.GONE);
        container.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        container.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

        progressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBar.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }
}
