package pro.handswork.rabruki;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class MyService extends Service {

    Handler mHandler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHandler.postDelayed(ToastRunnable, 5000);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Runnable ToastRunnable = new Runnable() {
        public void run() {




  /*          Context context = getApplicationContext();

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(context.getApplicationInfo().icon)
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle("Ваши координаты")
                            .setTicker("Ticker")
                            .setContentText(MainActivity.lat_in + " " + MainActivity.lng_in)
                            .setNumber(1)
                            .setAutoCancel(true);

            mNotificationManager.notify("App Name", 228, mBuilder.build());
            new SendData().execute();*/
            mHandler.postDelayed( ToastRunnable, 5000);
        }
    };

}