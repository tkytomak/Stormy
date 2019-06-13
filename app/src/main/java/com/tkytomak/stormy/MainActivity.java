package com.tkytomak.stormy;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tkytomak.stormy.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather currentWeather;

    private ImageView iconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this,
                R.layout.activity_main);
        TextView darkSky = findViewById(R.id.darkSkyAttribution);

        darkSky.setMovementMethod(LinkMovementMethod.getInstance());

        iconImageView = findViewById(R.id.iconImageView);

        String apiKey = getString(R.string.apiKey);
        double latitude = 37.8267;
        double longitude = -122.4233;
        String forecastURL = "https://api.darksky.net/forecast/"
                + apiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            currentWeather = getCurrentDetails(jsonData);
                            CurrentWeather displayWeather = new CurrentWeather(
                                currentWeather.getLocationLabel(),
                                currentWeather.getIcon(),
                                currentWeather.getTime(),
                                currentWeather.getTemperature(),
                                currentWeather.getHumidity(),
                                currentWeather.getPrecipChance(),
                                currentWeather.getSummary(),
                                currentWeather.getTimeZone()
                            );
                            displayWeather.setFormattedTime();

                            binding.setWeather(displayWeather);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Drawable drawable = getResources().getDrawable(displayWeather.getIconId());
                                    iconImageView.setImageDrawable(drawable);
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Exception caught: ", e);
                    }
                }
            });
        }
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{
            JSONObject forecast = new JSONObject(jsonData);
            String timeZone = forecast.getString("timezone");
            JSONObject currently = forecast.getJSONObject("currently");
            CurrentWeather currentWeather = new CurrentWeather();
            currentWeather.setHumidity(currently.getDouble("humidity"));
            currentWeather.setTime(currently.getLong("time"));
            currentWeather.setIcon(currently.getString("icon"));
            currentWeather.setLocationLabel("Alcatraz Island, CA");
            currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
            currentWeather.setSummary(currently.getString("summary"));
            currentWeather.setTemperature(currently.getDouble("temperature"));
            currentWeather.setTimeZone(timeZone);

            Log.i(TAG, "Time: " + currentWeather.getFormattedTime());
            return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        else {
            Toast.makeText(this, getString(R.string.network_unavailable_message),
                Toast.LENGTH_LONG).show();
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }
}
