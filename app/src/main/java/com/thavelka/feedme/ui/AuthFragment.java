package com.thavelka.feedme.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.thavelka.feedme.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AuthFragment extends Fragment {

    Unbinder unbinder;
    AuthListener listener;

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

    @OnClick({R.id.auth_btn_email, R.id.auth_btn_register}) void onClickEmailAuth(Button b) {
        listener.emailAuth(b.getId() == R.id.auth_btn_register);
    }

    interface AuthListener {
        void googleSignIn();
        void facebookSignIn();
        void emailAuth(boolean newUser);
    }
}
