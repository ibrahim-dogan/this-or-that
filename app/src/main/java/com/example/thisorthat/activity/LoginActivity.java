package com.example.thisorthat.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.thisorthat.R;
import com.example.thisorthat.activity.main.MainActivity;
import com.example.thisorthat.model.User;
import com.example.thisorthat.network.RetrofitClient;
import com.example.thisorthat.network.UserApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    Button loginButton;
    ScrollView scrollView;
    EditText edUsername, edPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkUserLoggedIn();

        initialize();
    }

    private void initialize() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        attachKeyboardListeners();

        loginButton = findViewById(R.id.loginButton);
        scrollView = findViewById(R.id.scrollView);
        edUsername = findViewById(R.id.edLoginUsername);
        edPassword = findViewById(R.id.edLoginPassword);

    }

    public void login(View view) {
        if (!checkFormIsValid()) {
        } else {
            callLoginApi();
        }

    }

    private boolean checkFormIsValid() {
        if (TextUtils.isEmpty(edUsername.getText())) {
            edUsername.setError("Username is required.");
            return false;
        } else if (TextUtils.isEmpty(edPassword.getText())) {
            edPassword.setError("Password is required.");
            return false;
        } else return true;
    }

    private void callLoginApi() {
        showProgressDialog();
        UserApi userApi = RetrofitClient.getClient().create(UserApi.class);
        Call<User> userCall = userApi.login(edUsername.getText().toString(), edPassword.getText().toString());
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    progressDialog.dismiss();
                    SharedPreferences sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("sessionToken", response.body().getSessionToken());
                    editor.putString("objectId", response.body().getObjectId());
                    editor.putString("username", response.body().getUsername());
                    editor.commit();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);

                } else {//TODO: Get messages like this user already exists.
                    Toast.makeText(LoginActivity.this, "Login failed: Invalid username or password.", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }

    @Override
    protected void onShowKeyboard(int keyboardHeight) {
        // do things when keyboard is shown
        scrollView.smoothScrollTo(0, loginButton.getTop());
    }

    @Override
    protected void onHideKeyboard() {
        // do things when keyboard is hidden
    }

    public void signup(View view) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    public void checkUserLoggedIn() {//Todo: check this later
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
        if (!sharedPreferences.getString("sessionToken", "").isEmpty()) {
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Logging in, please wait.");
            progressDialog.show();
        }
    }
}
