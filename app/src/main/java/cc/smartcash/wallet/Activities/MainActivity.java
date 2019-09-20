package cc.smartcash.wallet.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import cc.smartcash.wallet.Adapters.CoinSpinnerAdapter;
import cc.smartcash.wallet.Fragments.DashboardFragment;
import cc.smartcash.wallet.Fragments.ReceiveFragment;
import cc.smartcash.wallet.Fragments.SendFragment;
import cc.smartcash.wallet.Fragments.TransactionFragment;
import cc.smartcash.wallet.Models.Coin;
import cc.smartcash.wallet.Models.User;
import cc.smartcash.wallet.Models.Wallet;
import cc.smartcash.wallet.R;
import cc.smartcash.wallet.Utils.Keys;
import cc.smartcash.wallet.Utils.SmartCashApplication;
import cc.smartcash.wallet.ViewModels.CurrentPriceViewModel;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private BottomNavigationView mNavigationView;
    private ImageView btnExit;
    private ImageView btnSettings;
    private TextView walletTxt;
    private TextView walletConverted;
    private ArrayList<Coin> coins;
    private CoinSpinnerAdapter adapter;
    private Coin selectedCoin;
    private boolean withoutPin;
    private SmartCashApplication smartCashApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        if (smartCashApplication == null)
            smartCashApplication = new SmartCashApplication(getApplicationContext());

        withoutPin = smartCashApplication.getBoolean(this, Keys.KEY_WITHOUT_PIN);

        getCoins();

        setUI();

        setBtnSettingsClick();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setWalletValue();
    }

    private void setBtnSettingsClick() {
        btnSettings.setOnClickListener(v2 -> {

            AlertDialog.Builder settingsDialog = new AlertDialog.Builder(this);
            View settingsView = getLayoutInflater().inflate(R.layout.settings_modal, null);
            Button btnClose = settingsView.findViewById(R.id.button_close);
            Spinner currentPriceSpinner = settingsView.findViewById(R.id.current_price_spinner);
            TextView forgotPinBtn = settingsView.findViewById(R.id.forgot_pin_btn);
            TextView createPinBtn = settingsView.findViewById(R.id.create_pin);

            if (withoutPin) {
                forgotPinBtn.setVisibility(View.GONE);
            } else {
                createPinBtn.setVisibility(View.GONE);
            }

            createPinBtn.setOnClickListener(v4 -> {
                smartCashApplication.saveBoolean(this, false, Keys.KEY_WITHOUT_PIN);
                startActivity(new Intent(this, PinActivity.class));
            });

            forgotPinBtn.setOnClickListener(v3 -> {
                new AlertDialog.Builder(this)
                        .setTitle("Forgot the PIN?")
                        .setMessage("Are you sure you want to redefine your PIN?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> navigateToLogin())
                        .setNegativeButton(android.R.string.no, null).show();
            });

            setSpinner(currentPriceSpinner);
            settingsDialog.setView(settingsView);
            AlertDialog dialog = settingsDialog.create();
            btnClose.setOnClickListener(v3 -> {
                dialog.hide();
                setWalletValue();
            });
            dialog.show();
        });
    }

    private void setUI() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mNavigationView = findViewById(R.id.navigationView);
        mNavigationView.setOnNavigationItemSelectedListener(this);
        Fragment dashboardFragment = DashboardFragment.newInstance();
        openFragment(dashboardFragment);
        walletTxt = findViewById(R.id.wallet_txt);
        walletConverted = findViewById(R.id.wallet_converted_txt);
        btnExit = findViewById(R.id.button_exit);
        btnSettings = findViewById(R.id.button_settings);

        btnExit.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout?")
                    .setMessage("Are you sure you want to logout?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> navigateToLogin())
                    .setNegativeButton(android.R.string.no, null).show();
        });
    }

    private void navigateToLogin() {
        Toast.makeText(MainActivity.this, "Redirecting to login...", Toast.LENGTH_SHORT).show();
        smartCashApplication.deleteSharedPreferences(MainActivity.this);
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    private void setSpinner(Spinner currentPriceSpinner) {

        adapter = new CoinSpinnerAdapter(this, android.R.layout.simple_spinner_item, coins);
        currentPriceSpinner.setAdapter(adapter);

        setSelectedCoinOnSpinner(currentPriceSpinner, coins);

        currentPriceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                saveSelectedCoin(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    private void saveSelectedCoin(Coin coin) {
        smartCashApplication.saveActualSelectedCoin(MainActivity.this, coin);
        setWalletValue();
    }

    private void setSelectedCoinOnSpinner(Spinner currentPriceSpinner, ArrayList<Coin> coins) {
        getCoins();
        Coin selectedCoin = smartCashApplication.getActualSelectedCoin(this);
        if (selectedCoin != null) {
            for (int i = 0; i < coins.size(); i++) {
                if (selectedCoin.getValue().equals(coins.get(i).getValue()) && selectedCoin.getName().equals(coins.get(i).getName())) {
                    currentPriceSpinner.setSelection(i);
                }
            }
        }
        setWalletValue();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_dash: {
                Fragment dashFragment = DashboardFragment.newInstance();
                openFragment(dashFragment);
                setWalletValue();
                break;
            }
            case R.id.nav_receive: {
                Fragment receiveFragment = ReceiveFragment.newInstance();
                openFragment(receiveFragment);
                setWalletValue();
                break;
            }
            case R.id.nav_send: {
                Fragment sendFragment = SendFragment.newInstance();
                openFragment(sendFragment);
                setWalletValue();
                break;
            }
            case R.id.nav_trans: {
                Fragment transactionFragment = TransactionFragment.newInstance();
                openFragment(transactionFragment);
                setWalletValue();
                break;
            }
        }

        return true;
    }

    private void getCoins() {

        if (this.coins == null)
            this.coins = smartCashApplication.getCurrentPrice(this);

        if (this.coins == null) {
            getCurrentPrices();
        }

    }

    private void getCurrentPrices() {
        CurrentPriceViewModel model = ViewModelProviders.of(this).get(CurrentPriceViewModel.class);
        model.getCurrentPrices(MainActivity.this).observe(this, currentPrices -> {
            if (currentPrices != null) {
                this.coins = SmartCashApplication.convertToArrayList(currentPrices);
                smartCashApplication.saveCurrentPrice(this, this.coins);
                Log.d(TAG, "Prices OK");
            } else {
                Log.e(TAG, "Error to get current prices!");
            }
        });
    }

    public void setWalletValue() {

        User user = smartCashApplication.getUser(this);

        if (user == null) navigateToLogin();

        ArrayList<Wallet> wallets = user.getWallet();
        Double amount = 0.0;

        for (Wallet wallet : wallets) {
            amount += wallet.getBalance();
        }

        selectedCoin = smartCashApplication.getActualSelectedCoin(this);

        getCoins();

        if (selectedCoin != null && selectedCoin.getName().equalsIgnoreCase("USD")) {
            for (Coin auxcoin : this.coins) {
                if (auxcoin.getName().equalsIgnoreCase(selectedCoin.getName())) {
                    selectedCoin.setValue(auxcoin.getValue());
                    smartCashApplication.saveActualSelectedCoin(this, auxcoin);
                    break;
                }
            }
        }

        walletTxt.setText(getResources().getString(R.string.smartCash) + String.format("%.8f", amount));
        if (selectedCoin == null || selectedCoin.getName().equals("SMART")) {
            ArrayList<Coin> currentPrice = smartCashApplication.getCurrentPrice(this);
            walletConverted.setText("$ " + smartCashApplication.converterValue(amount, currentPrice.get(0).getValue()) + " " + currentPrice.get(0).getName());
        } else {
            //  walletConverted.setText("$ " + String.format("%.3f", amount / selectedCoin.getValue()));
            walletConverted.setText("$ " + smartCashApplication.converterValue(amount, selectedCoin.getValue()) + " " + selectedCoin.getName());
        }

    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}