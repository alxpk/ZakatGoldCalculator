package com.example.goldzakat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    // Toolbar
    private Toolbar myToolbar;

    // Zakat Calculator UI
    private EditText etWeight, etPrice;
    private Spinner spinnerType;
    private Button btnCalculate, btnReset;
    private CardView cardOutput;
    private TextView tvTotalValue, tvZakatPayableValue, tvTotalZakat;

    // Zakat Constants
    private static final double NISAB_KEEPING = 85.0; // grams
    private static final double NISAB_WEARING = 200.0; // grams
    private static final double ZAKAT_RATE = 0.025; // 2.5%
    private static final DecimalFormat RM_FORMATTER = new DecimalFormat("RM #,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar setup
        myToolbar = findViewById(R.id.my_Toolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // Initialize Zakat Calculator views
        etWeight = findViewById(R.id.et_weight);
        etPrice = findViewById(R.id.et_price);
        spinnerType = findViewById(R.id.spinner_type);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnReset = findViewById(R.id.btn_reset);
        cardOutput = findViewById(R.id.card_output);
        tvTotalValue = findViewById(R.id.tv_total_value);
        tvZakatPayableValue = findViewById(R.id.tv_zakat_payable_value);
        tvTotalZakat = findViewById(R.id.tv_total_zakat);

        // Spinner setup (entries already in XML)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gold_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // Button listeners
        btnCalculate.setOnClickListener(v -> calculateZakat());
        btnReset.setOnClickListener(v -> resetFields());
    }

    // Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.item_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TITLE, "Gold Zakat Calculator App");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://github.com/alxpk/ZakatGoldCalculator");
            startActivity(Intent.createChooser(shareIntent, "Share using"));
            return true;
        } else if (id == R.id.item_about) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Zakat Calculator Logic
    private void calculateZakat() {
        if (etWeight.getText().toString().isEmpty() || etPrice.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter all required values.", Toast.LENGTH_LONG).show();
            cardOutput.setVisibility(View.GONE);
            return;
        }

        double goldWeight, goldPrice;
        try {
            goldWeight = Double.parseDouble(etWeight.getText().toString());
            goldPrice = Double.parseDouble(etPrice.getText().toString());
            if (goldWeight <= 0 || goldPrice <= 0) throw new IllegalArgumentException();
        } catch (Exception e) {
            Toast.makeText(this, "Weight and Price must be positive numbers.", Toast.LENGTH_LONG).show();
            cardOutput.setVisibility(View.GONE);
            return;
        }

        // Determine Nisab
        int selectedIndex = spinnerType.getSelectedItemPosition();
        double nisab = (selectedIndex == 0) ? NISAB_KEEPING : NISAB_WEARING;
        String nisabType = (selectedIndex == 0) ? "Keeping (85g)" : "Wearing (200g)";

        double totalGoldValue = goldWeight * goldPrice;
        double zakatPayableWeight = goldWeight - nisab;

        if (zakatPayableWeight <= 0) {
            displayResults(totalGoldValue, 0.0, 0.0);
            Toast.makeText(this, "Zakat NOT payable. Weight below Nisab for " + nisabType, Toast.LENGTH_LONG).show();
            return;
        }

        double zakatPayableValue = zakatPayableWeight * goldPrice;
        double totalZakatAmount = zakatPayableValue * ZAKAT_RATE;

        displayResults(totalGoldValue, zakatPayableValue, totalZakatAmount);
    }

    private void displayResults(double totalGoldValue, double zakatPayableValue, double totalZakatAmount) {
        tvTotalValue.setText("Total Gold Value: " + RM_FORMATTER.format(totalGoldValue));
        tvZakatPayableValue.setText("Zakat Payable Value: " + RM_FORMATTER.format(zakatPayableValue));
        tvTotalZakat.setText("Total Zakat Amount (2.5%): " + RM_FORMATTER.format(totalZakatAmount));
        cardOutput.setVisibility(View.VISIBLE);
    }

    private void resetFields() {
        etWeight.setText("");
        etPrice.setText("");
        spinnerType.setSelection(0);
        cardOutput.setVisibility(View.GONE);
        etWeight.clearFocus();
        etPrice.clearFocus();
        Toast.makeText(this, "Inputs cleared.", Toast.LENGTH_SHORT).show();
    }
}
