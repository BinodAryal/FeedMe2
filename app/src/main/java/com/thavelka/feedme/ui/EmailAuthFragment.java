package com.thavelka.feedme.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.thavelka.feedme.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EmailAuthFragment extends Fragment {

    Unbinder unbinder;

    @BindView(R.id.email_auth_edittext_email) EditText emailField;
    @BindView(R.id.email_auth_edittext_password) EditText passwordField;
    @BindView(R.id.email_auth_edittext_confirm) EditText confirmPasswordField;
    @BindView(R.id.email_auth_btn_submit) Button submit;

    public EmailAuthFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email_auth, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
