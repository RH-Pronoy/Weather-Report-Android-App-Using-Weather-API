package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import com.android.volley.DefaultRetryPolicy;

public class MainActivity extends AppCompatActivity {
//add api key
    private EditText searchInput;
    private ImageButton searchButton;
    private TextView cityText, dateTime, temperatureText, conditionText;
    private ImageView weatherIcon;
    private TextView windValue, humidityValue, feelsLikeValue;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        requestQueue = Volley.newRequestQueue(this);

        searchButton.setOnClickListener(v -> {
            String city = searchInput.getText().toString().trim();
            if (!city.isEmpty()) {
                getWeatherData(city);
            }
        });

        // Load default city
        getWeatherData("London");
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        cityText = findViewById(R.id.cityText);
        dateTime = findViewById(R.id.dateTime);
        temperatureText = findViewById(R.id.temperatureText);
        conditionText = findViewById(R.id.conditionText);
        weatherIcon = findViewById(R.id.weatherIcon);
        windValue = findViewById(R.id.windLayout).findViewById(R.id.detailValue);
        humidityValue = findViewById(R.id.humidityLayout).findViewById(R.id.detailValue);
        feelsLikeValue = findViewById(R.id.feelsLikeLayout).findViewById(R.id.detailValue);

        // Set detail labels
        ((TextView) findViewById(R.id.windLayout).findViewById(R.id.detailLabel)).setText("Wind Speed");
        ((TextView) findViewById(R.id.humidityLayout).findViewById(R.id.detailLabel)).setText("Humidity");
        ((TextView) findViewById(R.id.feelsLikeLayout).findViewById(R.id.detailLabel)).setText("Feels Like");
    }

    private void getWeatherData(String city) {
        String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=yes", API_KEY, city);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject location = response.getJSONObject("location");
                        JSONObject current = response.getJSONObject("current");
                        JSONObject condition = current.getJSONObject("condition");

                        cityText.setText(String.format("%s, %s", location.getString("name"), location.getString("country")));
                        dateTime.setText(location.getString("localtime"));
                        temperatureText.setText(String.format("%.1f°C", current.getDouble("temp_c")));
                        conditionText.setText(condition.getString("text"));

                        String iconUrl = "https:" + condition.getString("icon");
                        Picasso.get().load(iconUrl)
                                .error(R.drawable.ic_error)
                                .into(weatherIcon);

                        windValue.setText(String.format("%.1f km/h", current.getDouble("wind_kph")));
                        humidityValue.setText(String.format("%d%%", current.getInt("humidity")));
                        feelsLikeValue.setText(String.format("%.1f°C", current.getDouble("feelslike_c")));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "Error parsing weather data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    String errorMessage = "Error fetching weather data";
                    if (error.networkResponse != null) {
                        errorMessage += " (Status: " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }


} 