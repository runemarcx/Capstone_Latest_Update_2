package com.hcdc.capstone.taskprocess;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.Homepage;
import com.hcdc.capstone.R;
import com.hcdc.capstone.TimerViewModel;
import com.hcdc.capstone.network.ApiClient;
import com.hcdc.capstone.network.ApiService;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

public class TaskProgress extends BaseActivity {
    private TimerViewModel timerViewModel;
    private static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    private static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    private static final String TIMER_PREFS = "TimerPrefs";
    private static final String PREF_START_TIME = "startTime";
    private static final String PREF_REMAINING_TIME = "remainingTime";
    private static final String PREF_TIMER_RUNNING = "timerRunning";
    private FirebaseFirestore db;
    private Button startButton;
    private Button doneButton;
    private ImageView uploadButton;
    private  ImageButton cancelButton;
    private TextView timerTextView;
    private ProgressDialog progressDialog;
    private static final int CAMERA_CAPTURE_REQUEST_CODE = 200;
    private String currentUserUID;
    private boolean timerRunning = false;
    private long startTime = 0L;
    private long taskDurationMillis;
    private final Handler handler = new Handler();
    private Uri selectedImageUri;
    public static HashMap<String, String> remoteMsgHeaders = null;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (timerRunning) {
                long millis = System.currentTimeMillis() - startTime;
                long remainingMillis = taskDurationMillis - millis;
                if (remainingMillis <= 0) {
                    timerTextView.setText("Time's up!");
                    timerRunning = false;
                    doneButton.setVisibility(View.VISIBLE);
                } else {
                    int seconds = (int) (remainingMillis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    int hours = minutes / 60;
                    minutes = minutes % 60;

                    timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                    handler.postDelayed(this, 1000);
                }
            }
        }
    };
    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAnnDyey0:APA91bEFvAHQXeK_dpb0GbX8K27RVhe7mJX45_pPnaumLhhoiazeVAythGSSRxNYSS2JAalYA3dfDyqfprJk_TrN6gYyslGR6bsPJ_BeRZVAv4_pvDKqAN1mHq1Wh-5AhJwcurC6nRE5"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }

    private void sendNotification(String taskName, String location) {
        getDeviceTokens(new DeviceTokenCallback() {
            @Override
            public void onDeviceTokensReceived(List<String> deviceTokens) {
                try {
                    JSONObject data = new JSONObject();
                    data.put("title", taskName);
                    data.put("body", location);

                    for (String deviceToken : deviceTokens) {
                        JSONObject payload = new JSONObject();
                        payload.put("to", deviceToken);
                        payload.put("notification", data);
                        sendNotificationToFCM(payload.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void getDeviceTokens(final DeviceTokenCallback callback) {
        List<String> deviceTokens = new ArrayList<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = firestore.collection("users");
        Query query = usersRef.whereGreaterThanOrEqualTo("fcmToken_admin", "");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    String deviceToken = documentSnapshot.getString("fcmToken_admin");
                    if (deviceToken != null) {
                        deviceTokens.add(deviceToken);
                    }
                }
                callback.onDeviceTokensReceived(deviceTokens);
            } else {
                callback.onError("Failed to retrieve device tokens: " + task.getException());
            }
        });
    }
    private interface DeviceTokenCallback {
        void onDeviceTokensReceived(List<String> deviceTokens);
        void onError(String errorMessage);
    }

    private void sendNotificationToFCM(String payload) {
        String authorizationKey = "key=AAAAnnDyey0:APA91bEFvAHQXeK_dpb0GbX8K27RVhe7mJX45_pPnaumLhhoiazeVAythGSSRxNYSS2JAalYA3dfDyqfprJk_TrN6gYyslGR6bsPJ_BeRZVAv4_pvDKqAN1mHq1Wh-5AhJwcurC6nRE5";
        String contentType = "application/json";

        ApiClient.getClient().create(ApiService.class).sendMessage(
                createHeaders(authorizationKey, contentType),
                payload
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray results = responseJSON.getJSONArray("results");
                            if (responseJSON.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("ERROR: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    @NonNull
    private HashMap<String, String> createHeaders(String authorization, String contentType) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(REMOTE_MSG_AUTHORIZATION, authorization);
        headers.put(REMOTE_MSG_CONTENT_TYPE, contentType);
        return headers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_test);

        timerViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(TimerViewModel.class);
        boolean doneButtonNotClicked = !timerViewModel.isDoneButtonClicked();

        startButton = findViewById(R.id.startButton);
        doneButton = findViewById(R.id.doneButton);
        timerTextView = findViewById(R.id.timerTextView);
        TextView taskNameTextView = findViewById(R.id.taskTitle4);
        TextView taskPointsTextView = findViewById(R.id.taskPoint);
        TextView taskDescriptionTextView = findViewById(R.id.taskDesc);
        TextView taskLocationTextView = findViewById(R.id.taskLocation);
        TextView taskTimeFrameTextView = findViewById(R.id.taskTimeFrame);
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String taskName = getIntent().getStringExtra("taskName");
        String taskPoints = getIntent().getStringExtra("taskPoints");
        String taskDescription = getIntent().getStringExtra("taskDescription");
        String taskLocation = getIntent().getStringExtra("taskLocation");
        currentUserUID = auth.getCurrentUser().getUid();
        int timeFrameHours = getIntent().getIntExtra("timeFrameHours", 0);
        int timeFrameMinutes = getIntent().getIntExtra("timeFrameMinutes", 0);

        progressDialog = new ProgressDialog(TaskProgress.this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        taskNameTextView.setText(taskName);
        taskPointsTextView.setText(taskPoints);
        taskDescriptionTextView.setText(taskDescription);
        taskLocationTextView.setText(taskLocation);
        long timeFrameMillis = (timeFrameHours * 60L + timeFrameMinutes) * 60 * 1000;
        taskDurationMillis = timeFrameMillis;
        int initialSeconds = (int) ((timeFrameMillis % (60 * 1000)) / 1000);
        timerTextView.setText(String.format("%02d:%02d:%02d", timeFrameHours, timeFrameMinutes, initialSeconds));
        String taskTimeFrame = timeFrameHours + " hours " + timeFrameMinutes + " minutes";
        taskTimeFrameTextView.setText(taskTimeFrame);

        View tasksubmitCustomDialog = LayoutInflater.from(TaskProgress.this).inflate(R.layout.tasksubmit_dialog, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TaskProgress.this);
        alertDialog.setView(tasksubmitCustomDialog);
        cancelButton = tasksubmitCustomDialog.findViewById(R.id.closeSubmission);
        Button submitButton = tasksubmitCustomDialog.findViewById(R.id.taskSubmission);
        uploadButton = tasksubmitCustomDialog.findViewById(R.id.upload_submission);
        final AlertDialog dialog = alertDialog.create();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                if (selectedImageUri == null) {
                    showToast("Please capture an image before submitting.");
                    progressDialog.dismiss();
                    return; // Return early to prevent further execution
                } else {
                    stopTimer();

                    // Get the current date and time in 12-hour format
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a MM/dd/yy", Locale.US);
                    String currentDateTime = dateFormat.format(new Date());

                    WriteBatch batch = db.batch();

                    db.collection("user_acceptedTask")
                            .whereEqualTo("acceptedBy", currentUserUID)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        String documentId = documentSnapshot.getId();
                                        Map<String, Object> acceptedTaskData = documentSnapshot.getData();
                                        batch.update(db.collection("user_acceptedTask").document(documentId), "isCompleted", true);
                                        batch.commit()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        long remainingMillis = taskDurationMillis - (System.currentTimeMillis() - startTime);
                                                        int remainingSeconds = (int) (remainingMillis / 1000);
                                                        int remainingMinutes = remainingSeconds / 60;
                                                        remainingSeconds = remainingSeconds % 60;
                                                        int remainingHours = remainingMinutes / 60;
                                                        remainingMinutes = remainingMinutes % 60;
                                                        String remainingTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);
                                                        clearTimerSharedPreferences();

                                                        acceptedTaskData.put("isCompleted", true);
                                                        acceptedTaskData.put("remainingTime", remainingTime);
                                                        acceptedTaskData.put("completedDateTime", currentDateTime);

                                                        if (selectedImageUri != null) {
                                                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                                            StorageReference imageRef = storageRef.child("images/" + currentUserUID + "_" + System.currentTimeMillis());

                                                            imageRef.putFile(selectedImageUri)
                                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                            progressDialog.dismiss();
                                                                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                @Override
                                                                                public void onSuccess(Uri uri) {
                                                                                    String imageUrl = uri.toString();
                                                                                    storeImageUrlInFirestore(imageUrl);
                                                                                    acceptedTaskData.put("imageUrl", imageUrl);
                                                                                    db.collection("completed_task")
                                                                                            .add(acceptedTaskData)
                                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                                @Override
                                                                                                public void onSuccess(DocumentReference documentReference) {
                                                                                                    db.collection("user_acceptedTask")
                                                                                                            .document(documentId)
                                                                                                            .delete()
                                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onSuccess(Void aVoid) {
                                                                                                                    Intent intent = new Intent(TaskProgress.this, Homepage.class);
                                                                                                                    startActivity(intent);
                                                                                                                    Toast.makeText(getApplicationContext(), "Task completed and is now in review.", Toast.LENGTH_SHORT).show();
                                                                                                                    finish();
                                                                                                                }
                                                                                                            })
                                                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                                                @Override
                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                    // Handle failure
                                                                                                                }
                                                                                                            });
                                                                                                }
                                                                                            })
                                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                    // Handle failure
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            // Handle failure
                                                                        }
                                                                    });
                                                        } else {
                                                            db.collection("completed_task")
                                                                    .add(acceptedTaskData)
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentReference documentReference) {
                                                                            db.collection("user_acceptedTask")
                                                                                    .document(documentId)
                                                                                    .delete()
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            Intent intent = new Intent(TaskProgress.this, taskFragment.class);
                                                                                            startActivity(intent);
                                                                                            finish();
                                                                                        }
                                                                                    })
                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            // Handle failure
                                                                                        }
                                                                                    });
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            // Handle failure
                                                                        }
                                                                    });
                                                        }
                                                        sendNotification(taskName, taskLocation);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle failure
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure
                                }
                            });
                }
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                if (!timerRunning) {
                    timerRunning = true;
                    startTime = System.currentTimeMillis();
                    handler.postDelayed(timerRunnable, 1000);
                    startTimer();
                    startButton.setVisibility(View.GONE);
                    doneButton.setVisibility(View.VISIBLE);
                    db.collection("user_acceptedTask")
                            .whereEqualTo("acceptedBy", currentUserUID)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        String documentId = documentSnapshot.getId();
                                        db.collection("user_acceptedTask")
                                                .document(documentId)
                                                .update("isStarted", true)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle failure
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                startTimer();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_CAPTURE_REQUEST_CODE);
                }
            }
        });
    }

    private void storeImageUrlInFirestore(String imageUrl) {
        db.collection("images")
                .add(new HashMap<String, Object>() {{
                    put("imageUrl", imageUrl);
                }})
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            selectedImageUri = getImageUri(imageBitmap);

            uploadButton.setImageResource(R.drawable.ic_check);
        }
    }

    private Uri getImageUri(@NonNull Bitmap imageBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "Title", null);
        return Uri.parse(path);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        timerViewModel.setStartTime(startTime);
        timerViewModel.setTimerRunning(timerRunning);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        startTime = timerViewModel.getStartTime();
        timerRunning = timerViewModel.isTimerRunning();
        if (timerRunning) {
            handler.post(timerRunnable);
            startButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (timerRunning) {
            Toast.makeText(this, " Task is in progress. Cannot go back. ", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing() && timerRunning) {
            // Start the TimerService as a foreground service
            Intent serviceIntent = new Intent(this, TimerService.class);
            serviceIntent.putExtra("taskDurationMillis", taskDurationMillis);
            serviceIntent.putExtra("startTime", startTime);
            startForegroundService(serviceIntent);
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            long remainingTime = taskDurationMillis - elapsedTime;

            // Save the current timer state in shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences(TIMER_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(PREF_START_TIME, currentTime);
            editor.putLong(PREF_REMAINING_TIME, remainingTime);
            editor.putBoolean(PREF_TIMER_RUNNING, true);
            editor.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(TIMER_PREFS, MODE_PRIVATE);
        if (sharedPreferences.getBoolean(PREF_TIMER_RUNNING, false)) {
            startTime = sharedPreferences.getLong(PREF_START_TIME, 0);
            taskDurationMillis = sharedPreferences.getLong(PREF_REMAINING_TIME, 0);
            if (taskDurationMillis > 0) {
                startTimer();
            }
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TimerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isMyServiceRunning()) {
            Intent broadcastIntent = new Intent("StopTimerService");
            sendBroadcast(broadcastIntent);
        }
    }

    private void startTimer() {
        if (!timerRunning) {
            timerRunning = true;
            startTime = System.currentTimeMillis();
            handler.postDelayed(timerRunnable, 0);
            startButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.VISIBLE);

            // Save the timer state
            saveTimerState(true);
        }
    }


    private void stopTimer() {
        if (timerRunning) {
            handler.removeCallbacks(timerRunnable);
            timerRunning = false;

            // Save the timer state
            saveTimerState(false);
        }
    }


    private void saveTimerState(boolean isRunning) {
        SharedPreferences sharedPreferences = getSharedPreferences(TIMER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_TIMER_RUNNING, isRunning);
        editor.apply();
    }

    private void clearTimerSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(TIMER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }


    public static class TimerService extends Service {
        private final Handler handler = new Handler();
        private Runnable timerRunnable;
        private long startTime;
        private long taskDurationMillis;
        private static final int NOTIFICATION_ID = 1;
        private static final String CHANNEL_ID = "TimerServiceChannel";
        private static final String SILENT_CHANNEL_ID = "SilentTimerChannel";
        private String currentTimerValue = "00:00:00";
        private NotificationCompat.Builder builder;
        private NotificationManagerCompat notificationManager;

        private final BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, @NonNull Intent intent) {
                if ("StopTimerService".equals(intent.getAction())) {
                    stopForeground(true);
                    stopSelf();
                }
            }
        };

        @SuppressLint("MissingPermission")
        @Override
        public void onCreate() {
            super.onCreate();
            IntentFilter filter = new IntentFilter("StopTimerService");
            registerReceiver(stopServiceReceiver, filter);
            createNotificationChannels();
            builder = createNotification(currentTimerValue);
            notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        private void createNotificationChannels() {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationChannel silentChannel = new NotificationChannel(
                    SILENT_CHANNEL_ID,
                    "Silent Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            manager.createNotificationChannel(silentChannel);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (intent != null) {
                taskDurationMillis = intent.getLongExtra("taskDurationMillis", 0);
                startTime = intent.getLongExtra("startTime", 0);
                timerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        long millis = System.currentTimeMillis() - startTime;
                        long remainingMillis = taskDurationMillis - millis;
                        if (remainingMillis > 0) { // Update the notification only if there's remaining time
                            int seconds = (int) (remainingMillis / 1000);
                            int minutes = seconds / 60;
                            seconds %= 60;
                            int hours = minutes / 60;
                            minutes %= 60;
                            currentTimerValue = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                            updateNotification(currentTimerValue);
                            handler.postDelayed(this, 1000);
                        }
                    }
                };
                if (taskDurationMillis > 0) {
                    NotificationCompat.Builder builder = createNotification(currentTimerValue);
                    Notification notification = builder.build();
                    startForeground(NOTIFICATION_ID, notification);
                    handler.post(timerRunnable);
                } else {
                    stopSelf();
                }
            }
            return START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(stopServiceReceiver);
            handler.removeCallbacks(timerRunnable);
        }

        @SuppressLint("MissingPermission")
        private void updateNotification(String timerValue) {
            RemoteViews customView = new RemoteViews(getPackageName(), R.drawable.custom_notification_layout);
            customView.setTextViewText(R.id.notification_text, "Time Remaining: " + timerValue);
            builder.setCustomContentView(customView);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        @NonNull
        private NotificationCompat.Builder createNotification(String timerValue) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SILENT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_timer)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true);
            builder.setDefaults(0);
            RemoteViews customView = new RemoteViews(getPackageName(), R.drawable.custom_notification_layout);
            customView.setTextViewText(R.id.notification_text, "Time Remaining: " + timerValue);
            builder.setCustomContentView(customView);
            Intent notificationIntent = new Intent(this, TaskProgress.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);
            return builder;
        }
    }
}