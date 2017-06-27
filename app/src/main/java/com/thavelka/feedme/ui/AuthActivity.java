package com.thavelka.feedme.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.facebook.login.LoginResult;
import com.thavelka.feedme.R;
import com.thavelka.feedme.utils.Constants;
import com.thavelka.feedme.utils.UserManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.ObjectServerError;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import timber.log.Timber;

public class AuthActivity extends AppCompatActivity implements AuthFragment.AuthListener,
        EmailAuthFragment.EmailAuthListener {

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
        AuthFragment authFragment = new AuthFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.auth_container, authFragment)
                .commit();
    }

    @Override
    public void googleSignIn() {

    }

    @Override
    public void facebookSignIn(LoginResult result) {
        UserManager.setAuthMode(UserManager.AUTH_MODE.FACEBOOK);
        SyncCredentials credentials = SyncCredentials.facebook(result.getAccessToken().getToken());
        attemptLogin(credentials);
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

    private void attemptLogin(SyncCredentials credentials) {
        showProgress(true);
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                showProgress(false);
                finishLogin(user);
            }

            @Override
            public void onError(ObjectServerError error) {
                showProgress(false);
                Timber.e(error);
            }
        });
    }

    private void finishLogin(SyncUser user) {
        UserManager.setActiveUser(user);
        Intent intent = new Intent(this, ListingsActivity.class);
        startActivity(intent);
        finish();
    }

    private void showProgress(final boolean show) {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        container.setVisibility(show ? View.GONE : View.VISIBLE);
        container.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
