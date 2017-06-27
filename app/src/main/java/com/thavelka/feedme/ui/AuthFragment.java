package com.thavelka.feedme.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.thavelka.feedme.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class AuthFragment extends Fragment {

    Unbinder unbinder;
    AuthListener listener;
    CallbackManager callbackManager;

    @BindView(R.id.auth_btn_google) SignInButton googleButton;
    @BindView(R.id.auth_btn_facebook) LoginButton facebookButton;
    @BindView(R.id.auth_btn_email) Button signInButton;
    @BindView(R.id.auth_btn_register) Button registerButton;

    public AuthFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up google auth

        // Set up facebook auth
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                listener.facebookSignIn(loginResult);
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {
                Timber.e(error);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Configure facebook
        facebookButton.setReadPermissions("email");
        facebookButton.setFragment(this);
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
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick({R.id.auth_btn_email, R.id.auth_btn_register}) void onClickEmailAuth(Button b) {
        listener.onClickEmailAuth(b.getId() == R.id.auth_btn_register);
    }

    interface AuthListener {
        void googleSignIn();
        void facebookSignIn(LoginResult result);
        void onClickEmailAuth(boolean newUser);
    }
}
