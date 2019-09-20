package cc.smartcash.wallet.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.smartcash.wallet.Models.Coin;
import cc.smartcash.wallet.Models.User;
import cc.smartcash.wallet.Models.UserRegisterRequest;
import cc.smartcash.wallet.R;
import cc.smartcash.wallet.Receivers.NetworkReceiver;
import cc.smartcash.wallet.Utils.Keys;
import cc.smartcash.wallet.Utils.NetworkUtil;
import cc.smartcash.wallet.Utils.SmartCashApplication;
import cc.smartcash.wallet.ViewModels.CurrentPriceViewModel;
import cc.smartcash.wallet.ViewModels.UserViewModel;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = RegisterActivity.class.getSimpleName();

    @BindView(R.id.network_status)
    Switch networkSwitch;
    @BindView(R.id.txt_user)
    EditText txtUser;
    @BindView(R.id.txt_password)
    EditText txtPassword;
    @BindView(R.id.txt_confirm_password)
    EditText txtConfirmPassword;
    @BindView(R.id.txt_pin)
    EditText txtPin;
    @BindView(R.id.txt_confirm_pin)
    EditText txtConfirmPin;
    @BindView(R.id.loader)
    ProgressBar loader;
    @BindView(R.id.login_content)
    ConstraintLayout loginContent;
    private SmartCashApplication smartCashApplication;
    private NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_main);
        ButterKnife.bind(this);

        getCurrentPrices();

        this.smartCashApplication = new SmartCashApplication(getApplicationContext());

        String useruuid = UUID.randomUUID().toString();
        txtUser.setText(useruuid + "@testeandroidmobile.com");
        txtPassword.setText("123456");
        txtConfirmPassword.setText("123456");
        txtPin.setText("1234");
        txtConfirmPin.setText("1234");

        networkReceiver = new NetworkReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "The status of the network has changed");
                String status = NetworkUtil.getConnectivityStatusString(context);

                boolean internetAvailable = NetworkUtil.getInternetStatus(context);
                networkSwitch.setChecked(internetAvailable);
                networkSwitch.setText(status);

            }

        };

        String token = smartCashApplication.getToken(this);
        User user = smartCashApplication.getUser(this);

        if (token != null && token != "" && user != null) {

            Log.e("token", token);

            this.setVisibility();

        } else {

            loginContent.setVisibility(View.VISIBLE);
            loader.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Error to retreive the Token or the user", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");

        registerReceiver(networkReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    @OnClick(R.id.btn_save)
    public void onViewClicked() {

        String username = txtUser.getText().toString();
        String password = txtPassword.getText().toString();
        String confirm_password = txtConfirmPassword.getText().toString();
        String pin = txtPin.getText().toString();
        String confirm_pin = txtConfirmPin.getText().toString();

        boolean hasError = false;

        if (username.isEmpty()) {
            txtUser.setError("The USERNAME can't be empty");
            hasError = true;
        }
        if (password.isEmpty()) {
            txtPassword.setError("The PASSWORD can't be empty");
            hasError = true;
        }
        if (confirm_password.isEmpty()) {
            txtConfirmPassword.setError("The confirmation of the PASSWORD can't be empty");
            hasError = true;
        }
        if (pin.isEmpty()) {
            txtPin.setError("The PIN can't be empty");
            hasError = true;
        }
        if (confirm_pin.isEmpty()) {
            txtConfirmPin.setError("The confirmation of the PIN can't be empty");
            hasError = true;
        }
        if (!pin.equalsIgnoreCase(confirm_pin)) {
            txtConfirmPin.setError("The PIN does not match");
            hasError = true;
        }
        if (!password.equalsIgnoreCase(confirm_password)) {
            txtPassword.setError("The PASSWORD does not match");
            hasError = true;
        }

        if (hasError) return;

        UserRegisterRequest newUser = new UserRegisterRequest();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(username);

        this.setVisibility();

        UserViewModel model = new ViewModelProvider(this).get(UserViewModel.class);

        model.setUser(newUser, this).observe(this, user -> {
            if (user != null) {

                if (user.getWallet() == null) {

                    model.getToken(user.getUsername(), user.getPassword(), this).observe(this, t -> {

                        model.getUser(t, this).observe(this, user1 -> {
                            user1.setRecoveryKey(user.getRecoveryKey());
                            smartCashApplication.saveUser(RegisterActivity.this, user1);
                            navigateToPinActivity();
                        });

                    });

                } else {
                    setVisibility();
                    Log.e(TAG, "It was not possible to register the user");
                }

            } else {
                setVisibility();
                Log.e(TAG, "It was not possible to register the user");
            }
        });
    }

    private void navigateToPinActivity() {
        Intent intent = new Intent(getApplicationContext(), PinActivity.class);
        intent.putExtra(Keys.KEY_PASSWORD, txtPassword.getText().toString());
        intent.putExtra(Keys.KEY_PIN, txtPin.getText().toString());
        startActivity(intent);
    }
    public void setVisibility() {
        if (loader.getVisibility() == View.VISIBLE) {
            loginContent.setVisibility(View.VISIBLE);
            loader.setVisibility(View.GONE);
        } else {
            loginContent.setVisibility(View.GONE);
            loader.setVisibility(View.VISIBLE);
        }
    }

    public void getCurrentPrices() {
        CurrentPriceViewModel model = ViewModelProviders.of(this).get(CurrentPriceViewModel.class);
        model.getCurrentPrices(RegisterActivity.this).observe(this, currentPrices -> {
            if (currentPrices != null) {
                ArrayList<Coin> coins = SmartCashApplication.convertToArrayList(currentPrices);
                smartCashApplication.saveCurrentPrice(this, coins);
                Log.i(TAG, "Prices OK");

            } else {

                Toast.makeText(getApplicationContext(), "Error to get the current prices!", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error to get current prices!");
            }
        });
    }

}