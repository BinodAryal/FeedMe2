package com.thavelka.feedme.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.thavelka.feedme.R;
import com.thavelka.feedme.auth.FacebookAuth;
import com.thavelka.feedme.auth.GoogleAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class AuthFragment extends Fragment {

    private Unbinder unbinder;
    private AuthListener listener;
    private GoogleAuth googleAuth;
    private FacebookAuth facebookAuth;

    @BindView(R.id.auth_btn_google) SignInButton googleButton;
    @BindView(R.id.auth_btn_facebook) LoginButton facebookButton;
    @BindView(R.id.auth_btn_email) Button signInButton;
    @BindView(R.id.auth_btn_register) Button registerButton;

    public AuthFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Set up google auth
        googleAuth = new GoogleAuth(googleButton, getActivity(), this) {
            @Override
            public void onRegistrationComplete(GoogleSignInResult result) {
                listener.googleSignIn(result);
            }

            @Override
            public void onError(String s) {
                Timber.e(s);
            }
        };

        // Configure facebook auth
        facebookButton.setFragment(this);
        facebookAuth = new FacebookAuth(facebookButton) {
            @Override
            public void onRegistrationComplete(LoginResult loginResult) {
                listener.facebookSignIn(loginResult);
            }

            @Override
            public void onAuthError(FacebookException error) {
                Timber.e(error);
            }
        };
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = ((AuthListener) context);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AuthListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleAuth.onActivityResult(requestCode, resultCode, data);
        facebookAuth.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick({R.id.auth_btn_email, R.id.auth_btn_register}) void onClickEmailAuth(Button b) {
        listener.onClickEmailAuth(b.getId() == R.id.auth_btn_register);
    }

    interface AuthListener {
        void googleSignIn(GoogleSignInResult result);
        void facebookSignIn(LoginResult result);
        void onClickEmailAuth(boolean newUser);
    }
}
