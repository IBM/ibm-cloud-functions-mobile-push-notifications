package com.developer.serverlessmobilepush;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WeatherPickerActivity extends AppCompatActivity {

    TextView rainText, cloudyText, snowText, timeLabel;
    LinearLayout rainContainer, cloudyContainer, snowContainer;
    SeekBar chooseTime;
    Switch allTypesSwitch;
    Boolean isSubscribedToAllTypes;
    ArrayList<String> weatherTypes;
    Button subscribeButton, selectNewCityButton;
    String cityName;
    Double latitude;
    Double longitude;
    MFPPush push;
    MFPPushNotificationListener notificationListener;
    List<String> subscribedTags;
    int intTimeChosen;
    AlertDialog successAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_picker);

        Intent intent = getIntent();
        cityName = intent.getStringExtra("cityName");
        latitude = intent.getDoubleExtra("latitude",0);
        longitude = intent.getDoubleExtra("longitude", 0);

        Log.d("City name from geo is", cityName);
        Log.d("Latitude is", String.valueOf(latitude));
        Log.d("Longitude is", String.valueOf(longitude));

        String appGuid = getResources().getString(R.string.PUSH_NOTIFICATION_APP_GUID);
        String clientSecret = getResources().getString(R.string.PUSH_NOTIFICATION_CLIENT_SECRET);
        String serverlessApiBackendUrl = getResources().getString(R.string.SERVERLESS_API_URL);

        // Initialize the SDK
        BMSClient.getInstance().initialize(this, BMSClient.REGION_US_SOUTH);
        //Initialize client Push SDK
        push = MFPPush.getInstance();
        push.initialize(getApplicationContext(), appGuid, clientSecret);
        // Initialize Serverless Helper
        ServerlessHelper.initialize(serverlessApiBackendUrl, getApplicationContext());

        // register push notification
        push.registerDevice(new MFPPushResponseListener<String>() {

            @Override
            public void onSuccess(String response) {
                //handle successful device registration here
                Log.d("MFP success", response);

                // get subscriptions
                getSubscriptions();
            }

            @Override
            public void onFailure(MFPPushException ex) {
                //handle failure in device registration here
                Log.d("MFP error", ex.getErrorMessage());
            }
        });

        // set notification listener
        notificationListener = notificationListener();

//        cityName = "San Francisco";
//        latitude = 37.7749;
//        longitude = 122.4194;

        rainText = findViewById(R.id.rainText);
        cloudyText = findViewById(R.id.cloudyText);
        snowText = findViewById(R.id.snowText);
        allTypesSwitch = findViewById(R.id.allTypesSwitch);
        subscribeButton = findViewById(R.id.subscribeButton);
        selectNewCityButton = findViewById(R.id.selectNewCityButton);
        rainContainer = findViewById(R.id.rainContainer);
        cloudyContainer = findViewById(R.id.cloudyContainer);
        snowContainer = findViewById(R.id.snowContainer);
        chooseTime = findViewById(R.id.chooseTime);
        timeLabel = findViewById(R.id.timeLabel);

        // set title
        getSupportActionBar().setTitle(cityName + " - Weather Alerts");

        // initial time is 7:00 PM
        timeLabel.setText("7:00 PM");

        // select new city button listener
        selectNewCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LocalData.getInstance().clearData()) {
                    Intent cityPickerIntent = new Intent(getApplicationContext(), CityPickerActivity.class);
                    startActivity(cityPickerIntent);
                }
            }
        });

        isSubscribedToAllTypes = false;
        weatherTypes = new ArrayList<>();

        setOnClick(rainContainer, rainText, "rain");
        setOnClick(cloudyContainer, cloudyText, "cloud");
        setOnClick(snowContainer, snowText, "snow");

        allTypesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rainContainer.setOnClickListener(onClickWhileAllTypesIsTrue());
                    cloudyContainer.setOnClickListener(onClickWhileAllTypesIsTrue());
                    snowContainer.setOnClickListener(onClickWhileAllTypesIsTrue());
                    rainContainer.setAlpha(0.5f);
                    cloudyContainer.setAlpha(0.5f);
                    snowContainer.setAlpha(0.5f);
                } else {
                    setOnClick(rainContainer, rainText, "rain");
                    setOnClick(cloudyContainer, cloudyText, "cloud");
                    setOnClick(snowContainer, snowText, "snow");
                    rainContainer.setAlpha(1f);
                    cloudyContainer.setAlpha(1f);
                    snowContainer.setAlpha(1f);
                    if (weatherTypes.isEmpty()) {
                        rainContainer.callOnClick();
                    }
                }
                isSubscribedToAllTypes = b;
            }
        });

        intTimeChosen = 2;
        chooseTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                intTimeChosen = i;
                switch (i) {
                    case 0:
                        timeLabel.setText("5:00 PM");
                        break;
                    case 1:
                        timeLabel.setText("600 PM");
                        break;
                    case 2:
                        timeLabel.setText("7:00 PM");
                        break;
                    case 3:
                        timeLabel.setText("8:00 PM");
                        break;
                    case 4:
                        timeLabel.setText("900 PM");
                        break;
                    case 5:
                        timeLabel.setText("10:00 PM");
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // set initial to all types
        allTypesSwitch.setChecked(true);

        // set alert dialog
        successAlertDialog = new AlertDialog.Builder(WeatherPickerActivity.this)
            .setTitle("Push Notification Tag")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            }).create();

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String tagBuilt;
                if (isSubscribedToAllTypes) {
                    Log.d("will subscribe to all", buildTagName(cityName));
                    // create tag and trigger then ->
                    tagBuilt = buildTagName(cityName);
                } else {
                    Collections.sort(weatherTypes);
                    Log.d("will subscribe to types", buildTagName(cityName, weatherTypes));
                    // create tag and trigger then ->
                    tagBuilt = buildTagName(cityName, weatherTypes);
                }
                if (tagBuilt != null && !tagBuilt.equals("")) {
                    tagFromService(tagBuilt, new Tags() {
                        @Override
                        public void isExisting(Boolean exists) {
                            if (exists != null) {
                                if (exists) {
                                    subscribeToTag(tagBuilt);
                                } else {
                                    // create new tag
                                    ServerlessHelper.getInstance().createTagAndTrigger(tagBuilt, latitude, longitude, isSubscribedToAllTypes, weatherTypes, intTimeChosen, new ServerlessHelper.OnCompletion() {
                                        @Override
                                        public void onSuccess(JSONObject response) {
                                            subscribeToTag(tagBuilt.concat("-" + String.valueOf(intTimeChosen)));
                                        }

                                        @Override
                                        public void onError(VolleyError error) {

                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(push != null) {
            push.listen(notificationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (push != null) {
            push.hold();
        }
    }

    private MFPPushNotificationListener notificationListener() {
        final AlertDialog alertDialog = new android.app.AlertDialog.Builder(WeatherPickerActivity.this)
                .setTitle("Weather Notifications")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).create();

        return new MFPPushNotificationListener() {
            @Override
            public void onReceive (final MFPSimplePushNotification message){
                // Handle Push Notification
                runOnUiThread(new Runnable() {
                    public void run() {
                        alertDialog.setMessage(message.getAlert());
                        alertDialog.show();
                    }
                });
            }
        };
    }

    private void getSubscriptions() {
        push.getSubscriptions(new MFPPushResponseListener<List<String>>() {
            @Override
            public void onSuccess(List<String> response) {
                Log.d("subscrpitions", response.toString());
                subscribedTags = response;
            }

            @Override
            public void onFailure(MFPPushException exception) {
                Log.d("exception ", "error");
            }
        });
    }

    private String buildTagName(String cityName) {
        return cityName.toLowerCase().replace(" ", "-").concat("-any");
    }

    private String buildTagName(String cityName, ArrayList<String> types) {
        String tagName = cityName.toLowerCase().replace(" ", "-");
        for (String type: types) {
            tagName = tagName.concat("-").concat(type);
        }
        return tagName;
    }

    private void subscribeToTag(final String tag) {
        push.getSubscriptions(new MFPPushResponseListener<List<String>>() {
            @Override
            public void onSuccess(List<String> response) {
                Log.d("subscrpitions", response.toString());
                boolean alreadySubscribed = false;
                for (String subscribedTag: response) {
                    if (!subscribedTag.equals("Push.ALL") && !subscribedTag.equals(tag)) {
                        unsubscribe(subscribedTag);
                    }
                    if (subscribedTag.equals(tag)) {
                        alreadySubscribed = true;
                    }
                }
                if (!alreadySubscribed) {
                    subscribe(tag);
                }
            }

            @Override
            public void onFailure(MFPPushException exception) {
                Log.d("exception ", "error");
            }
        });
    }

    public void tagFromService(final String tag, final Tags tags) {
        push.getTags(new MFPPushResponseListener<List<String>>() {
            @Override
            public void onSuccess(List<String> response) {
                Log.d("Existing tags are", response.toString());
                tags.isExisting(response.contains(tag));
            }

            @Override
            public void onFailure(MFPPushException exception) {
                Log.d("Failed to get tags", exception.getErrorMessage());
                tags.isExisting(null);
            }
        });
    }

    private void subscribe(final String tag) {
        push.subscribe(tag, new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d("Subscribed to", tag);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        successAlertDialog.setMessage("Successfully subscribed to tag: " + tag);
                        successAlertDialog.show();
                    }
                });
            }

            @Override
            public void onFailure(final MFPPushException exception) {
                Log.d("FAILED to subscribe", tag + " - " + exception.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        successAlertDialog.setMessage("Failed to subscribe to " + tag + ". " + exception.toString());
                        successAlertDialog.show();
                    }
                });
            }
        });
    }

    private void unsubscribe(final String tag) {
        push.unsubscribe(tag, new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d("Unsubscribed from", tag + " - " + response);
            }

            @Override
            public void onFailure(MFPPushException exception) {
                Log.d("FAILED to unsubscribe", tag + " - " + exception.toString());
            }
        });
    }

    private View.OnClickListener onClickWhileAllTypesIsTrue() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOnClick(rainContainer, rainText, "rain");
                setOnClick(cloudyContainer, cloudyText, "cloud");
                setOnClick(snowContainer, snowText, "snow");
                rainContainer.setAlpha(1f);
                cloudyContainer.setAlpha(1f);
                snowContainer.setAlpha(1f);
                view.callOnClick();
                allTypesSwitch.setChecked(false);
            }
        };
    }

    private void setOnClick(final LinearLayout layout, final TextView textView, final String keyword) {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layout.getBackground() != null) {
                    layout.setBackground(null);
                    textView.setTextColor(getResources().getColor(R.color.black));
                    weatherTypes.remove(keyword);
                } else {
                    layout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    textView.setTextColor(getResources().getColor(R.color.white));
                    weatherTypes.add(keyword);
                }
                if (weatherTypes.isEmpty()) {
                    allTypesSwitch.setChecked(true);
                }
            }
        });
    }

    interface Tags {
        void isExisting(Boolean exists);
    }
}
