package be.kul.app;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.facebook.appevents.codeless.ViewIndexingTrigger;

public class ShakeService extends Service {
    private ShakeDetector mShaker;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {

        super.onCreate();
        this.mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        this.mAccelerometer = this.mSensorManager.getDefaultSensor(1);
        mShaker = new ShakeDetector(this);
        mShaker.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                Log.d("Shake Service callback", "Device shaken!");
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ShakeService.this.getApplicationContext(),"Device shaken!",Toast.LENGTH_SHORT).show();
                        final MediaPlayer mp = MediaPlayer.create(ShakeService.this, R.raw.cheering3);
                        mp.start();

                    }
                });
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;

    }


}
