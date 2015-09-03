package com.tao.chinachuclient;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

public class Preference extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
		ActionBar actionbar = getActionBar();
		actionbar.setHomeButtonEnabled(true);
		actionbar.setTitle("設定");
	}

	public class MyPreferencesFragment extends PreferenceFragment{
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);

			android.preference.Preference settingActivity = findPreference("settingActivity");
			android.preference.Preference settingEncode = findPreference("settingEncode");

			android.preference.CheckBoxPreference checkEncode = (CheckBoxPreference)findPreference("encStreaming");

			settingActivity.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(android.preference.Preference preference){
					startActivity(new Intent(getActivity(), SettingActivity.class));
					return false;
				}
			});

			settingEncode.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(android.preference.Preference preference){
					startActivity(new Intent(getActivity(), SettingEncodeActivity.class));
					return false;
				}
			});

			checkEncode.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(android.preference.Preference preference, Object newValue){
					if((boolean)newValue) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("設定の確認")
						.setMessage("エンコードの設定を確認してからご使用ください")
						.setNegativeButton("キャンセル", null)
						.setPositiveButton("OK", new OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which){
								startActivity(new Intent(getActivity(), SettingEncodeActivity.class));
							}
						});
						builder.create().show();
					}
					return true;
				}
			});
		}
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		ApplicationClass appClass = (ApplicationClass)getApplicationContext();
		appClass.setStreaming(pref.getBoolean("streaming", false));
		appClass.setEncStreaming(pref.getBoolean("encStreaming", false));
	}
}
