package com.findbulous.pos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SplashScreen extends Activity {

    ImageView splash;
    Thread splashTread;

    String data_notification = "";

    LoginPage login_page;
    String statuslogin;
    Context contextpage;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contextpage = SplashScreen.this;
        login_page = new LoginPage();
        statuslogin = login_page.getPreferences(contextpage, "status");
        Log.d("status", statuslogin);
        setContentView(R.layout.splashscreen);

        splash = (ImageView) findViewById(R.id.splash);

        getScreenSize();

        printHashKey(contextpage);

        SharedPreferences prefs = getSharedPreferences("ScreenResolution", Context.MODE_PRIVATE);
        int screenWidth = prefs.getInt("Width", 1600);
        android.view.ViewGroup.LayoutParams layoutParams = splash.getLayoutParams();
        int width_new = (int) (screenWidth);
        layoutParams.width = width_new;
        int height_new = (int) ((screenWidth) * 1.778666666666667);
        layoutParams.height =  height_new;
        splash.setAdjustViewBounds(true);

        splash.setImageResource(R.drawable.splashscreen_pos);

        SharedPreferences prefs_device = getSharedPreferences("DeviceToken", Context.MODE_PRIVATE);
        String device_token = prefs_device.getString("device_token", "null");
        System.out.println(" Saved from FireIDService : " + device_token);

        StartAnimations();
    }

//    private void hardcodePerson()
//    {
//        SharedPreferences preferences = contextpage.getSharedPreferences("LoginItemUser", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("agent", "b89c2bfbee2227e44546928c4b4e6fc2"); //red
//        editor.putString("type_role", contextpage.getResources().getString(R.string.user_type_staff)); //red
//
//
//
//
//        editor.putString("company_id", "49"); //red
//        editor.putString("company_name", "ABC"); //red
//        editor.apply();
//    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l = (LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim.reset();
        splash.clearAnimation();
        splash.startAnimation(anim);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 100) {
                        sleep(100);
                        waited += 100;
                    }
                    sleep(3000);

//                    final int min = 0;
//                    final int max = 3;
//                    int random = new Random().nextInt((max - min) + 1) + min;

//                    SharedPreferences preferences = getSharedPreferences("AppColorTheme", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = preferences.edit();
//                    editor.putString("primary", "#000000"); //red
//                    editor.putString("secondary", "#dbad57"); // yellow
//                    editor.putString("tertiary", "#f8f4e5" );
//                    editor.putString("quaternary", "#584e45");
//                    editor.putString("quinary", "#478a9b");
//                    editor.putString("button", "#ffffff");
//                    editor.putString("title", "#ffffff");
//                    editor.putString("logo", "#ffffff" );
//                    editor.putString("dashboard", "#ffffff" );
//                    editor.putString("nav_top", "#000000" );
//                    editor.putString("icon", "#ffffff");
//                    editor.putInt("transparent", 1 );
//                    editor.putString("toolbar", "#dbad57");
//                    editor.putString("outgoing_chat", "#f7e9d2");
//                    editor.apply();

                    if(statuslogin.equals("1")) {
//                        if (data_notification.length() > 5) {
//                            String redirect = "none";
//                            String voucher_id = "";
//                            String company_id = "";
//                            String company_name = "";
//
//                            try {
//                                JSONObject json = new JSONObject(data_notification);
//
//                                redirect = json.getString("redirect");
//
//                                if (redirect.equals("Voucher")) {
//                                    if (json.has("voucher_id")) {
//                                        voucher_id = json.getString("voucher_id");
//                                    }
//                                }
//                                else {
//                                    redirect = "none";
//                                }
//                            } catch (JSONException e) {
//                                Log.e("error", "cannot fetch data 0" + e);
//                                redirect = "none";
//                            }
//
//                            if (redirect.equals("Voucher") && statuslogin.equals("1")) {
//                                if (!voucher_id.equals("")) {
//                                    Intent i = new Intent(contextpage, Voucher_Details_V2.class);
//                                    i.putExtra("voucher_id", voucher_id);
//                                    i.putExtra("view", "shared");
//                                    startActivity(i);
//                                    finish();
//                                }
//                                else {
//                                    Intent i = new Intent(contextpage, Voucher_List_V3.class);
//                                    i.putExtra("action_received", "");
//                                    startActivity(i);
//                                    finish();
//                                }
//                            }
//                        }
//                        else {
//                            System.out.println("zzzzzzzzzzzzzzz 1 " + statuslogin);
//                            SharedPreferences preferenceslogin = getSharedPreferences("LoginItemUser", Context.MODE_PRIVATE);
//                            String agent = preferenceslogin.getString("agent", "null");
//                            String user_type = preferenceslogin.getString("type_role", "null");
//                            String platform_id = preferenceslogin.getString("platform_id", "");
//
//                            if (user_type.equals(getResources().getString(R.string.user_type_management)))
//                            {
//                                new GetUserModule_BE(contextpage, agent, platform_id).execute();
//                            }
//                            else
//                            {
//                                new Staff_CheckRole(agent, contextpage, "splashscreen").execute();
//                            }
//                        }
                        Intent a = new Intent(contextpage, HomePage.class);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(a);
                    }
                    else {
                        Intent a = new Intent(contextpage, LoginPage.class);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(a);
                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {

                }
            }
        };
        splashTread.start();
    }

    public void getScreenSize() {

        /*System.out.println("SCREEN WIDTH " +  Resources.getSystem().getDisplayMetrics().widthPixels );

        screenWidthx = Resources.getSystem().getDisplayMetrics().widthPixels;
        */
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();

        System.out.println("SCREEN WIDTH " +  width + " " + height );

        SharedPreferences preferences = getSharedPreferences("ScreenResolution", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("Width", width);
        editor.putInt("Height", height);
        editor.apply();
    }

    public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(" FB HASHKEY ", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(" FB HASHKEY ", "printHashKey()", e);
        } catch (Exception e) {
            Log.e(" FB HASHKEY ", "printHashKey()", e);
        }
    }
}