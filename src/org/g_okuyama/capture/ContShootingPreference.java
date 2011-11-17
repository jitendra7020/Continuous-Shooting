package org.g_okuyama.capture;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class ContShootingPreference extends PreferenceActivity implements OnPreferenceChangeListener{
    public static final String TAG = "ContShooting";

    public static final String DEFAULT_SHOOT_NUM = "0";
    
    static final int COLOR_EFFECT = 1;
    static final int SCENE_MODE = 2;
    static final int WHITE_BALANCE = 3;
    static final int PICTURE_SIZE = 4;
    static final int SHOOT_NUM = 5;
    static String[] sSizeList = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preference);
        
        Bundle extras = getIntent().getExtras();
        String[] effectList = null;
        String[] whiteList = null;
        String[] sceneList = null;
        if(extras != null){
            effectList = extras.getStringArray("effect");
            sceneList = extras.getStringArray("scene");
            whiteList = extras.getStringArray("white");
            sSizeList = extras.getStringArray("size");
        }

        //�F����
        ListPreference colorPref = (ListPreference)this.findPreference("color_effect");
        if(effectList != null){
        	colorPref.setOnPreferenceChangeListener(this);
        	colorPref.setSummary(getCurrentEffect(this));
        	colorPref.setEntries(effectList);
        	colorPref.setEntryValues(effectList);
        }
        else{
        	colorPref.setEnabled(false);
        }
                
        //�V�[��
        ListPreference scenePref = (ListPreference)this.findPreference("scene_mode");
        if(sceneList != null){
        	scenePref.setOnPreferenceChangeListener(this);
        	scenePref.setSummary(getCurrentSceneMode(this));
        	scenePref.setEntries(sceneList);
        	scenePref.setEntryValues(sceneList);
        }
        else{
        	scenePref.setEnabled(false);
        }

        //�z���C�g�o�����X
        ListPreference whitePref = (ListPreference)this.findPreference("white_balance");
        if(whiteList != null){
        	whitePref.setOnPreferenceChangeListener(this);
        	whitePref.setSummary(getCurrentWhiteBalance(this));
        	whitePref.setEntries(whiteList);
        	whitePref.setEntryValues(whiteList);
        }
        else{
        	whitePref.setEnabled(false);
        }
        
        //�摜�T�C�Y
        ListPreference sizePref = (ListPreference)this.findPreference("picture_size");
        String size = getCurrentPictureSize(this);
        if(sSizeList != null){
            sizePref.setOnPreferenceChangeListener(this);
            if(!size.equals("0")){
                sizePref.setSummary(size);
            }
            else{
                sizePref.setSummary(getString(R.string.picture_size_summary));                
            }
            sizePref.setEntries(sSizeList);

            /*
            String[] valueList = new String[sSizeList.length];
            for(int i=0; i<sSizeList.length; i++){
                valueList[i] = String.valueOf(i);
            }
            Log.d(TAG, "size = " + valueList);
            */
            sizePref.setEntryValues(sSizeList);
        }
        else{
            sizePref.setEnabled(false);
        }
        
        //�A�ʖ���
        ListPreference shootPref = (ListPreference)this.findPreference("shoot_num");
        shootPref.setOnPreferenceChangeListener(this);
        String str = getCurrentShootNum(this);
        if(str.equals("0")){
            shootPref.setSummary((CharSequence)getString(R.string.shoot_num_unlimited));
        }
        else{
            shootPref.setSummary(str);
        }
    }
    
    public static String getCurrentEffect(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString("color_effect", /*default*/Camera.Parameters.EFFECT_NONE);
    }

    public static String getCurrentWhiteBalance(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString("white_balance", /*default*/Camera.Parameters.WHITE_BALANCE_AUTO);
    }

    public static String getCurrentSceneMode(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString("scene_mode", /*default*/Camera.Parameters.SCENE_MODE_AUTO);
    }

    public static String getCurrentPictureSize(Context c){
    	return /*String str = */PreferenceManager.getDefaultSharedPreferences(c)
    			.getString("picture_size", /*default*/"0");

    	/*
    	//����N����
    	if(str.equals("0")){
    		return -1;
    	}
    	
    	for(int i=0; i<sSizeList.length; i++){
    		if(sSizeList[i].equals(str)){
    			return i;
    		}
    	}
    	
    	return 0;
    	*/
    }
    
    public static String getCurrentShootNum(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString("shoot_num", /*default*/DEFAULT_SHOOT_NUM);
    }

	public boolean onPreferenceChange(Preference pref, Object newValue) {
		final CharSequence value = (CharSequence)newValue;
		if(value == null){
			return false;
		}
		
		if(pref.getKey().equals("color_effect")){
		    //�I�����ꂽ��A�ݒ��ʂ��I�����A�����f������
            Intent intent = new Intent();
            intent.putExtra("effect", value);
            this.setResult(COLOR_EFFECT, intent);
            finish();
		}
		else if(pref.getKey().equals("scene_mode")){
		    Intent intent = new Intent();
		    intent.putExtra("scene", value);
		    this.setResult(SCENE_MODE, intent);
		    finish();
		}
        else if(pref.getKey().equals("white_balance")){
            Intent intent = new Intent();
            intent.putExtra("white", value);
            this.setResult(WHITE_BALANCE, intent);
            finish();
        }
		else if(pref.getKey().equals("picture_size")){
            Intent intent = new Intent();
            
            for(int i=0; i<sSizeList.length; i++){
            	if(sSizeList[i].equals(value)){
                    intent.putExtra("size", i);
                    
                    //Log.d(TAG, "result data = " + i);
                    
                    break;
            	}
            }
            this.setResult(PICTURE_SIZE, intent);
            finish();
		}
        else if(pref.getKey().equals("shoot_num")){
            Intent intent = new Intent();
            intent.putExtra("shoot", Integer.valueOf((String)value));
            this.setResult(SHOOT_NUM, intent);
            finish();
        }
		
		return true;
	}
}