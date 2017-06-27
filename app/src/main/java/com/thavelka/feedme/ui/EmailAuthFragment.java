package com.thavelka.feedme.ui;


import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.thavelka.feedme.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class EmailAuthFragment extends Fragment {

    public static final String ARG_NEW_USER = "newUser";
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern;
    private Unbinder unbinder;
    private boolean newUser;

    EditText emailField;
    EditText passwordField;
    EditText confirmPasswordField;

    @BindView(R.id.email_auth_textinput_email) TextInputLayout emailWrapper;
    @BindView(R.id.email_auth_textinput_password) TextInputLayout passwordWrapper;
    @BindView(R.id.email_auth_textinput_confirm) TextInputLayout confirmPasswordWrapper;
    @BindView(R.id.email_auth_btn_submit) Button submit;

    public static EmailAuthFragment newInstance(boolean newUser) {
        EmailAuthFragment fragment = new EmailAuthFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NEW_USER, newUser);
        fragment.setArguments(args);
        return fragment;
    }

    public EmailAuthFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email_auth, container, false);

        pattern = Pattern.compile(EMAIL_PATTERN);

        unbinder = ButterKnife.bind(this, view);
        emailField = emailWrapper.getEditText();
        passwordField = passwordWrapper.getEditText();
        confirmPasswordField = confirmPasswordWrapper.getEditText();

        Bundle args = getArguments();
        newUser = args != null && args.getBoolean(ARG_NEW_USER);
        confirmPasswordWrapper.setVisibility(newUser ? View.VISIBLE : View.GONE);
        submit.setText(newUser ? "Sign up" : "Sign in");
        if (newUser) {
            emailWrapper.setErrorEnabled(true);
            passwordWrapper.setErrorEnabled(true);
            confirmPasswordWrapper.setErrorEnabled(true);
            emailField.addTextChangedListener(textWatcher(emailField));
            passwordField.addTextChangedListener(textWatcher(passwordField));
            confirmPasswordField.addTextChangedListener(textWatcher(confirmPasswordField));
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.email_auth_btn_submit) void onClickSubmit() {
        if (!newUser) {
            // Sign in
            return;
        } else if (validateEmail(emailField.getText().toString())
                && passwordField.getText().length() >= 8
                && TextUtils.equals(passwordField.getText(), confirmPasswordField.getText())) {
            // Sign up
            return;
        }
    }

    private TextWatcher textWatcher(EditText v) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (v == emailField) {
                    emailWrapper.setError(validateEmail(v.getText().toString())
                            ? null : "Invalid email address");
                } else if (v == passwordField) {
                    passwordWrapper.setError(v.getText().length() >= 8
                            ? null : "Password must have at least 8 characters");
                } else if (v == confirmPasswordField) {
                    confirmPasswordWrapper.setError(TextUtils.equals(v.getText(), passwordField.getText())
                            ? null : "Passwords do not match");
                }
            }
        };
    }

    public boolean validateEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
