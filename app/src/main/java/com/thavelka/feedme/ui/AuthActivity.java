package com.thavelka.feedme.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.thavelka.feedme.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthActivity extends AppCompatActivity {

    AuthFragment authFragment;
    EmailAuthFragment emailAuthFragment;

    @BindView(R.id.auth_progress) ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        AuthFragment authFragment = new AuthFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.auth_container, authFragment)
                .commit();
    }
}
