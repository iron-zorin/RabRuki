package pro.handswork.rabruki;


import java.io.DataOutputStream;
import java.io.File;
import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.Toast;

import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

public class MainActivity extends Activity {

    ProgressDialog progressDialog;
    ProgressBar progressBar;
    boolean progressDialogIsStarted = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 10001;
    // объявляем разрешение, которое нам нужно получить
    private static final String ACCESS_COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int FILECHOOSER_RESULTCODE   = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    public static String uid;

    private WebView mWebView;
    final Activity activity = this;
    public Uri imageUri;
//    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        // Make sure this is before calling super.onCreate
        String ua = new WebView(this).getSettings().getUserAgentString();
        //Log.d(TAG, "## onCreate entered");
        //Log.d(TAG, ua);
        super.onCreate(icicle);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();


        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        mHandler = new Handler();


        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setWebViewClient(new GeoWebViewClient());
        // Enabling JS
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setUserAgentString(ua+"/Handswork");
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(false);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setPluginState(PluginState.ON);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setSupportZoom(true);



        // You can create external class extends with WebChromeClient
        // Taking WebViewClient as inner class
        // we will define openFileChooser for select file from camera or sdcard

        mWebView.setWebChromeClient(new WebChromeClient() {

            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){

                // Update message
                mUploadMessage = uploadMsg;

                try{

                    // Create AndroidExampleFolder at sdcard

                    File imageStorageDir = new File(
                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES)
                            , "AndroidExampleFolder");

                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }

                    // Create camera captured image file path and name
                    File file = new File(
                            imageStorageDir + File.separator + "IMG_"
                                    + String.valueOf(System.currentTimeMillis())
                                    + ".jpg");

                    mCapturedImageURI = Uri.fromFile(file);

                    // Camera capture image intent
                    final Intent captureIntent = new Intent(
                            android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");

                    // Create file chooser intent
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

                    // Set camera intent to file chooser
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                            , new Parcelable[] { captureIntent });

                    // On select image call onActivityResult method of activity
                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                }
                catch(Exception e){
                    Toast.makeText(getBaseContext(), "Exception:"+e,
                            Toast.LENGTH_LONG).show();
                }

            }

            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                openFileChooser(uploadMsg, "");
            }

            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {

                openFileChooser(uploadMsg, acceptType);
            }



            // The webPage has 2 filechoosers and will send a
            // console message informing what action to perform,
            // taking a photo or updating the file

            public boolean onConsoleMessage(ConsoleMessage cm) {

                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }

            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                //Log.d("androidruntime", "Show console messages, Used for debugging: " + message);

            }
        });   // End setWebChromeClient

        // Java and Javascript interfacing
        mWebView.addJavascriptInterface(new JavascriptInterface(), "JsInterface");

        // for debugging, this will handle the console.log() in javascript
/*         mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG, cm.message() + " #" + cm.lineNumber() + " --" + cm.sourceId() );
                return true;
            }
        }); */

//        mWebView.loadUrl("https://handswork.pro/google-mestopolozhenie/");
//        mWebView.loadUrl("file:///android_asset/locyandex.html");
//        mWebView.loadUrl("file:///android_asset/loc2gis.html");
//        mWebView.loadUrl("file:///android_asset/locgoogle.html");
        mWebView.loadUrl("https://handswork.pro");
//        mWebView.loadUrl("https://codepen.io/iron-zorin/full/eEbXLX/");


        mWebView.setWebViewClient(new MyAppWebViewClient(){


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //hide loading image
//                findViewById(R.id.progressBar1).setVisibility(View.GONE);
                //show webview
                findViewById(R.id.activity_main_webview).setVisibility(View.VISIBLE);
                if (!progressDialogIsStarted) {
//                    progressDialog = ProgressDialog.show(MainActivity.this, "", "Загрузка. Пожалуйста подождите...", true);
                    progressDialogIsStarted = true;
                    super.onPageStarted(view, url, favicon);
                    progressBar.setVisibility(View.VISIBLE);
                    mWebView.setAlpha(0.3f);
                    new SendData().execute();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialogIsStarted) {
                    progressDialogIsStarted = false;
                    //progressDialog.dismiss();
                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.INVISIBLE);
                    mWebView.setAlpha(1.0f);
                }
            }

         });

        // проверяем разрешения: если они уже есть,
        // то приложение продолжает работу в нормальном режиме
        if (isPermissionGranted(ACCESS_COARSE_LOCATION_PERMISSION)) {
//            Toast.makeText(this, "Разрешения есть, можно работать", Toast.LENGTH_SHORT).show();
        } else {
            // иначе запрашиваем разрешение у пользователя
            requestPermission(ACCESS_COARSE_LOCATION_PERMISSION, REQUEST_ACCESS_COARSE_LOCATION);
        }

    }


    public void onClickJavaButton (View mView) {
        mWebView.loadUrl("javascript:changeBackgroundColor()");
    }

    private class JavascriptInterface {

        JavascriptInterface() {}


        @android.webkit.JavascriptInterface
        public void onLoadJavascriptButton() {
            startService(new Intent(MainActivity.this, MyService.class));
            Log.d(TAG, "## onLoadJavascriptButton entered");
        }


        @android.webkit.JavascriptInterface
        public void Notification(String title, String ticker, String contentText, short number)
        {
            Context context = getApplicationContext();
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(context.getApplicationInfo().icon)
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle(title)
                            .setTicker(ticker)
                            .setContentText(contentText)
                            .setNumber(number)
                            .setAutoCancel(true);

            mNotificationManager.notify("App Name", 228, mBuilder.build());

        }


        @android.webkit.JavascriptInterface
        public void CurrentUser(String user)
        {
            uid =user;
            Log.i("XXX user", user);
            Log.i("XXX uid", uid);
        }

        @android.webkit.JavascriptInterface
        public void MsgBox(String msg)
        {
            Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    /**
     * WebChromeClient subclass handles UI-related calls
     * Note: think chrome as in decoration, not the Chrome browser
     */
    private class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }
    }


    private class GeoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // When user clicks a hyperlink, load in the existing WebView
            view.loadUrl(url);
            return true;
        }

    }

    private class MyAppWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(Uri.parse(url).getHost().endsWith("handswork.pro")) {
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        // Pop the browser back stack or exit the activity
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    private boolean isPermissionGranted(String permission) {
        // проверяем разрешение - есть ли оно у нашего приложения
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(MainActivity.this, "Разрешения получены", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Разрешения не получены", Toast.LENGTH_LONG).show();
                showPermissionDialog(MainActivity.this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermission(String permission, int requestCode) {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private void showPermissionDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = getResources().getString(R.string.app_name);
        builder.setTitle(title);
        builder.setMessage(title + " требует разрешение на доступ к местоположению");

        String positiveText = "Настройки";
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppSettings();
            }
        });

        String negativeText = "Выход";
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_ACCESS_COARSE_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            requestApplicationConfig();
        }

        if(requestCode==FILECHOOSER_RESULTCODE)
        {
            if (null == this.mUploadMessage) {
                return;
            }
            Uri result=null;
            try{
                if (resultCode != RESULT_OK) {
                    result = null;
                } else {
                    // retrieve from the private variable if the intent is null
                    result = intent == null ? mCapturedImageURI : intent.getData();
                }
            }
            catch(Exception e)
            {
                Toast.makeText(getApplicationContext(), "activity :"+e,
                        Toast.LENGTH_LONG).show();
            }
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    private void requestApplicationConfig() {
        if (isPermissionGranted(ACCESS_COARSE_LOCATION_PERMISSION)) {
            Toast.makeText(MainActivity.this, "Теперь уже разрешения получены", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Пользователь снова не дал нам разрешение", Toast.LENGTH_LONG).show();
            requestPermission(ACCESS_COARSE_LOCATION_PERMISSION, REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    class SendData extends AsyncTask<Void, Void, Void> {

        String resultString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String myURL = "https://handswork.pro/one-signal-id/";
                OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
                boolean isEnabled = status.getPermissionStatus().getEnabled();

                String userID = status.getSubscriptionStatus().getUserId();
                String parammetrs = "osid="+userID+"&uid="+ MainActivity.uid;
                Log.i("XXX", parammetrs);
                Log.i("XXX", myURL);
                Log.i("XXX", "XXX");
                byte[] data = null;
                InputStream is = null;

                try {
                    URL url = new URL(myURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
//                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
//                    conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    // конвертируем передаваемую строку в UTF-8
                    data = parammetrs.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();

                    // передаем данные на сервер
                    os.write(data);
                    os.flush();
                    os.close();
                    data = null;
                    conn.connect();
                    int responseCode = conn.getResponseCode();

                    // передаем ответ сервер
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    if (responseCode == 200) {    // Если все ОК (ответ 200)
                        is = conn.getInputStream();

                        byte[] buffer = new byte[8192]; // размер буфера

                        // Далее так читаем ответ
                        int bytesRead;

                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }

                        data = baos.toByteArray();
                        resultString = new String(data, "UTF-8");  // сохраняем в переменную ответ сервера, у нас "OK"
                        Log.i("XXX", resultString);
                    } else {
                    }

                    conn.disconnect();

                } catch (MalformedURLException e) {

                    //resultString = "MalformedURLException:" + e.getMessage();
                } catch (IOException e) {

                    //resultString = "IOException:" + e.getMessage();
                } catch (Exception e) {

                    //resultString = "Exception:" + e.getMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Toast toast = Toast.makeText(getApplicationContext(), "Данные переданы!", Toast.LENGTH_SHORT);

        }
    }

}