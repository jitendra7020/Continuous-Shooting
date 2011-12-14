package org.g_okuyama.capture;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ngigroup.adstir.AdstirView;
import com.ngigroup.adstir.AdstirTerminate;

public class ContShooting extends Activity {
    private static final String TAG = "ContShooting";
    public static final String URL_JP = "http://www.yahoo.co.jp";
    public static final String URL_OTHER = "http://www.yahoo.com";
    
    static final int REQUEST_CODE = 1;
    static final int RESPONSE_COLOR_EFFECT = 1;
    static final int RESPONSE_SCENE_MODE = 2;
    static final int RESPONSE_WHITE_BALANCE = 3;
    static final int RESPONSE_PICTURE_SIZE = 4;
    static final int RESPONSE_SHOOT_NUM = 5;
    static final int RESPONSE_INTERVAL = 6;

    SurfaceHolder mHolder;
    private int mCount = 0;
    private TextView mText;
    private CameraPreview mPreview = null;
    //撮影中か否か（0:停止中、1：撮影中）
    public int mMode = 0;
    private boolean mMaskFlag = false;
    
    private Button mButton = null;
    private Button mMaskButton = null;
    private String mNum = null;
    private ContentResolver mResolver;
    private final int MENU_DISP_GALLERY = 1;
    private final int MENU_DISP_SETTING = 2;
    
    private AdstirView mAdstirView;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//Log.d(TAG, "enter ContShooting#onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mNum = getString(R.string.sc_number);
        mResolver = getContentResolver();
        
        //設定値の取得
        String effect = ContShootingPreference.getCurrentEffect(this);
        String scene = ContShootingPreference.getCurrentSceneMode(this);
        String white = ContShootingPreference.getCurrentWhiteBalance(this);
        String size = ContShootingPreference.getCurrentPictureSize(this);
        
        //Log.d(TAG, "picsize = " + size);
        
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        int width = disp.getWidth();
        int height = disp.getHeight();
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SurfaceView sv = (SurfaceView)findViewById(R.id.camera);
        mHolder = sv.getHolder();

        mPreview = new CameraPreview(this);
        mPreview.setField(effect, scene, white, size, width, height);
        mHolder.addCallback(mPreview);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mText = (TextView)findViewById(R.id.text1);
    	mText.setText(mNum + System.getProperty("line.separator") + "0");

    	//連写枚数設定
        String num = ContShootingPreference.getCurrentShootNum(this);
        if(!num.equals("0")){
            mPreview.setShootNum(Integer.valueOf(num));
        }
        
        //連写間隔設定
        String interval = ContShootingPreference.getCurrentInterval(this);
        if(!interval.equals("0")){
            mPreview.setInterval(Integer.valueOf(interval));
        }

        //register UI Listener
    	setListener();        
    }
    
    private void setListener(){
        mButton = (Button)findViewById(R.id.button1);
        mButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(mPreview != null){
					if(mMode == 0){
						mPreview.resumePreview();
						mMode = 1;
					}
					else{
						mPreview.stopPreview();
						mMode = 0;
					}
				}
			}
        });
        
        mMaskButton = (Button)findViewById(R.id.mask_btn);
        mMaskButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(mPreview != null){
                    if(mMaskFlag){
                        SurfaceView sv = (SurfaceView)findViewById(R.id.camera);
                        sv.setLayoutParams(new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        displayNormalMode();
                        mMaskFlag = false;
                        setTitle(R.string.app_name);
                    }
                    else{
                        SurfaceView sv = (SurfaceView)findViewById(R.id.camera);
                        sv.setLayoutParams(new LinearLayout.LayoutParams(54, 36));
                        displayHideMode();
                        mMaskFlag = true;
                        setTitle(R.string.sc_hidden);
                        
                        WebView view = (WebView)findViewById(R.id.review);
                        view.setWebViewClient(new WebViewClient());
                        view.getSettings().setJavaScriptEnabled(true);
                        view.getSettings().setBuiltInZoomControls(true);
                        
                        if(Locale.getDefault().equals(Locale.JAPAN)){
                        	view.loadUrl(URL_JP);
                        }
                        else{
                        	view.loadUrl(URL_OTHER);
                        }
                    }
				}
			}
        });
        
        //adstir設定
        LinearLayout layout = (LinearLayout)findViewById(R.id.adspace);
        mAdstirView = new AdstirView(this);
        layout.addView(mAdstirView);
        
        /*
        AdMaker = (libAdMaker)findViewById(R.id.admakerview);
        AdMaker.siteId = "1401";
        AdMaker.zoneId = "6127";
        AdMaker.setUrl("http://images.ad-maker.info/apps/263ved0b78ef.html");
        AdMaker.setBackgroundColor(Color.TRANSPARENT);
        AdMaker.setAdMakerListener(new AdMakerListener(){
			public void onFailedToReceiveAdMaker(String arg0) {
				Log.d(TAG, "enter AdMaker#onFailedToReceiveAdMaker");
				//AdMakerの枠を消す
				AdMaker.stop();
				AdMaker.setVisibility(libAdMaker.GONE);
				AdMaker = null;

				//AdVision表示
				Log.d(TAG, "AdVision start");
                mAdVision  = (AdVision)findViewById(R.id.advision);
                mAdVision.setVisibility(AdVision.VISIBLE);
                mAdVision.setAdVisionListener(new AdVisionListener(){
                    public void onNoReceiveAd(String arg0) {
                        Log.d(TAG, "enter AdVision#onNoReceiveAd");
                    }

                    public void onReceiveAd() {
                        Log.d(TAG, "enter AdVision#onReceiveAd");
                    }
                });
                mAdVision.AdStart("20000000623");
			}

			public void onReceiveAdMaker() {
				Log.d(TAG, "enter AdMaker#onReceiveAdMaker");
				//nothing to do
			}
        });
        AdMaker.start();
        */
        
        /*
        mAdVision  = (AdVision)findViewById(R.id.advision);
        mAdVision.setAdVisionListener(new AdVisionListener(){
            public void onNoReceiveAd(String arg0) {
                //nothing to do
            }

            public void onReceiveAd() {
                //nothing to do
            }
        });
        mAdVision.AdStart("20000000623");
        */
        
		/*
        ImageButton plus = (ImageButton)findViewById(R.id.plus);
        plus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mPreview != null){
					mPreview.setZoom(true);
				}
			}
        });
        
        ImageButton minus = (ImageButton)findViewById(R.id.minus);
        minus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mPreview != null){
					mPreview.setZoom(false);
				}
			}
        });
        */
        
        /*
        Button focus = (Button)findViewById(R.id.focus);
        focus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mPreview != null){
					mPreview.changeFocus();
				}
			}
        });
        */    	
    }
    
    public void onStart(){
    	//Log.d(TAG, "enter ContShooting#onStart");
    	
        super.onStart();
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            new AlertDialog.Builder(this)
            .setTitle(R.string.sc_alert_title)
            .setMessage(getString(R.string.sc_alert_sd))
            .setPositiveButton(R.string.sc_alert_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(RESULT_OK);
                }
            })
            .show();
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //オプションメニュー(ギャラリー)
        MenuItem prefGallery = menu.add(0, MENU_DISP_GALLERY, 0, R.string.sc_menu_gallery);
        prefGallery.setIcon(android.R.drawable.ic_menu_gallery);

        //オプションメニュー(設定)
        MenuItem prefSetting = menu.add(0, MENU_DISP_SETTING, 0, R.string.sc_menu_setting);
        prefSetting.setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }
    
    //オプションメニュー選択時のリスナ
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_DISP_GALLERY:        	
        	startGallery();
            break;
            
        case MENU_DISP_SETTING:
            displaySettings();
        	break;
            
        default:
            //何もしない
        }

        return true;
    }
    
    private void startGallery(){
    	//ギャラリーへのintent
    	//Intent intent = new Intent(Intent.ACTION_PICK);
    	//intent.setType("image/*");
    	//startActivityForResult(intent, REQUEST_PICK_CONTACT);
    	//startActivity(intent);
    	
    	// ギャラリー表示
    	Intent intent = null;
    	try{
    	    // for Honeycomb
    	    intent = new Intent();
    	    intent.setClassName("com.android.gallery3d", "com.android.gallery3d.app.Gallery");
    	    startActivity(intent);
    	    return;
    	}
    	catch(Exception e){
    	    try{
    	        // for Recent device
    	        intent = new Intent();
    	        intent.setClassName("com.cooliris.media", "com.cooliris.media.Gallery");
    	        startActivity(intent);
    	    }
    	    catch(ActivityNotFoundException e1){
    	        try
    	        {
    	            // for Other device except HTC
    	            intent = new Intent(Intent.ACTION_VIEW);
    	            intent.setData(Uri.parse("content://media/external/images/media"));
    	            startActivity(intent);
    	        }
    	        catch (ActivityNotFoundException e2){
    	        	try{
    	        		// for HTC
    	        		intent = new Intent();
    	        		intent.setClassName("com.htc.album", "com.htc.album.AlbumTabSwitchActivity");
    	        		startActivity(intent);
    	        	}
    	        	catch(ActivityNotFoundException e3){
    	            	Toast.makeText(this, R.string.sc_menu_gallery_ng, Toast.LENGTH_SHORT).show();
    	        	}
    	        }
    	    }
    	}
    }
    
    private void displaySettings(){
        Intent pref_intent = new Intent(this, ContShootingPreference.class);

        //色合い設定のリストを作成する
        List<String> effectList = null;
        if(mPreview != null){
            effectList = mPreview.getEffectList();
        }
        if(effectList != null){
        	//Log.d(TAG, "effect = " + (String[])effectList.toArray(new String[0]));
            pref_intent.putExtra("effect", (String[])effectList.toArray(new String[0]));
        }

        //シーン
        List<String> sceneList = null;
        if(mPreview != null){
            sceneList = mPreview.getSceneModeList();
        }
        if(sceneList != null){
            //Log.d(TAG, "scene = " + (String[])sceneList.toArray(new String[0]));
            pref_intent.putExtra("scene", (String[])sceneList.toArray(new String[0]));
        }

        //ホワイトバランス
        List<String> whiteList = null;
        if(mPreview != null){
            whiteList = mPreview.getWhiteBalanceList();
        }
        if(whiteList != null){
            //Log.d(TAG, "white = " + (String[])whiteList.toArray(new String[0]));
            pref_intent.putExtra("white", (String[])whiteList.toArray(new String[0]));
        }
        
        //画像サイズ
        List<String> sizeList = null;
        if(mPreview != null){
            sizeList = mPreview.getSizeList();
        }
        if(sizeList != null){
            //Log.d(TAG, "size = " + (String[])sizeList.toArray(new String[0]));
            pref_intent.putExtra("size", (String[])sizeList.toArray(new String[0]));
        }
        
        startActivityForResult(pref_intent, REQUEST_CODE);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(data == null){
            return;
        }
        
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESPONSE_COLOR_EFFECT){
                if(mPreview != null){
                    mPreview.setColorValue(data.getStringExtra("effect"));
                }            	
            }
            if(resultCode == RESPONSE_SCENE_MODE){
            	if(mPreview != null){
                    mPreview.setSceneValue(data.getStringExtra("scene"));
                }            	            	
            }
            if(resultCode == RESPONSE_WHITE_BALANCE){
            	if(mPreview != null){
                    mPreview.setWhiteValue(data.getStringExtra("white"));
                }            	
            }
            if(resultCode == RESPONSE_PICTURE_SIZE){
                if(mPreview != null){
                    mPreview.setSizeValue(data.getIntExtra("size", 0));
                }
            }
            if(resultCode == RESPONSE_SHOOT_NUM){
                if(mPreview != null){
                    mPreview.setShootNum(data.getIntExtra("shoot", 0));
                }
            }
            if(resultCode == RESPONSE_INTERVAL){
                if(mPreview != null){
                    mPreview.setInterval(data.getIntExtra("interval", 0));
                }
            }
        }
    }
    
    public void count(){
    	mText.setText(mNum + System.getProperty("line.separator") + Integer.toString(++mCount));
    }
    
    public void displayStart(){
    	//sButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_camera, 0, 0, 0);
    	mButton.setTextColor(0xffffffff);
    	mButton.setText(R.string.sc_start);
    }
    
    public void displayStop(){
    	//sButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
    	mButton.setTextColor(0xffffffff);
    	mButton.setText(R.string.sc_stop);
    }
    
    void displayHideMode(){
        mMaskButton.setTextColor(0xffffffff);
        mMaskButton.setText(R.string.sc_back);
    }
    
    void displayNormalMode(){
        mMaskButton.setTextColor(0xffffffff);
        mMaskButton.setText(R.string.sc_mask);        
    }
    
    public void saveGallery(ContentValues values){
		mResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
    }
    
    public void setMode(int mode){
        mMode = mode;
    }
    
    protected void onPause(){
        //Log.d(TAG, "enter ContShooting#onPause");    	
    	super.onPause();
    	mAdstirView.stop();
    }
    
    protected void onDestroy(){
        //Log.d(TAG, "enter ContShooting#onDestroy");
    	super.onDestroy();
        new AdstirTerminate(this);
    	if(mPreview != null){
    	    mPreview.release();
    	}
    }
    
    protected void onRestart(){
    	//Log.d(TAG, "enter ContShooting#onRestart");
    	super.onRestart();
    }
    
    public void finish(){
        //Log.d(TAG, "enter ContShooting#finish");
    	/*
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.pi_finish)
    	.setMessage(getString(R.string.pi_finish_confirm))
    	.setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				System.exit(RESULT_OK);
			}
		})
		.setNegativeButton(R.string.pi_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		})
		.show();
		*/
    	
		System.exit(RESULT_OK);
    }
}