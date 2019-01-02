package com.tao.chinachuclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class Preference extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("設定");
        actionbar.setDisplayHomeAsUpEnabled(true);
    }

    public static class MyPreferencesFragment extends PreferenceFragment{
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            android.preference.CheckBoxPreference checkStreaming = (CheckBoxPreference)findPreference("streaming");
            android.preference.CheckBoxPreference checkEncode = (CheckBoxPreference)findPreference("encStreaming");
            android.preference.CheckBoxPreference oldCateColor = (CheckBoxPreference)findPreference("oldCategoryColor");

            android.preference.Preference addServer = findPreference("addServer");
            android.preference.Preference settingActivity = findPreference("settingActivity");
            android.preference.Preference delServer = findPreference("delServer");

            checkStreaming.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

                @Override
                public boolean onPreferenceChange(android.preference.Preference preference, Object newValue){
                    SQLiteDatabase db = new ServerSQLHelper(getActivity()).getWritableDatabase();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String chinachuAddress = pref.getString("chinachuAddress", "");
                    db.execSQL("update servers set streaming='" + String.valueOf((boolean)newValue) + "' "
                            + "where chinachuAddress='" + chinachuAddress + "'");
                    return true;
                }
            });
            checkEncode.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

                @Override
                public boolean onPreferenceChange(android.preference.Preference preference, Object newValue){
                    SQLiteDatabase db = new ServerSQLHelper(getActivity()).getWritableDatabase();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String chinachuAddress = pref.getString("chinachuAddress", "");
                    db.execSQL("update servers set encStreaming='" + String.valueOf((boolean)newValue) + "' "
                            + "where chinachuAddress='" + chinachuAddress + "'");
                    if((boolean)newValue){
                        new AlertDialog.Builder(getActivity())
                                .setTitle("設定の確認")
                                .setMessage("エンコードの設定を確認してからご使用ください")
                                .setNegativeButton("キャンセル", null)
                                .setPositiveButton("OK", new OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which){
                                        startActivity(new Intent(getActivity(), SettingActivity.class));
                                    }
                                }).show();
                    }
                    return true;
                }
            });
            oldCateColor.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

                @Override
                public boolean onPreferenceChange(android.preference.Preference preference, Object newValue){
                    SQLiteDatabase db = new ServerSQLHelper(getActivity()).getWritableDatabase();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String chinachuAddress = pref.getString("chinachuAddress", "");
                    db.execSQL("update servers set oldCategoryColor='" + String.valueOf((boolean)newValue) + "' "
                            + "where chinachuAddress='" + chinachuAddress + "'");
                    return true;
                }
            });

            addServer.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(android.preference.Preference preference){
                    startActivity(new Intent(getActivity(), AddServer.class));
                    return false;
                }
            });
            settingActivity.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(android.preference.Preference preference){
                    startActivity(new Intent(getActivity(), SettingActivity.class));
                    return false;
                }
            });
            delServer.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(android.preference.Preference preference){
                    final SQLiteDatabase db = new ServerSQLHelper(getActivity()).getWritableDatabase();
                    final ArrayList<String> address = new ArrayList<String>();
                    final ArrayList<String> rowid = new ArrayList<String>();
                    Cursor result = db.rawQuery("select chinachuAddress, ROWID from servers", null);
                    boolean mov = result.moveToFirst();
                    while(mov){
                        address.add(result.getString(0));
                        rowid.add(result.getString(1));
                        mov = result.moveToNext();
                    }
                    new AlertDialog.Builder(getActivity())
                            .setTitle("削除するサーバーを選択")
                            .setItems((String[])address.toArray(new String[0]), new OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, final int which){
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle("削除の確認")
                                            .setMessage("以下のサーバーを削除しますか？\n" + address.get(which))
                                            .setNegativeButton("キャンセル", null)
                                            .setPositiveButton("OK", new OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int w){
                                                    String delRowid = rowid.get(which);
                                                    db.execSQL("delete from servers where ROWID=" + delRowid);
                                                    Toast.makeText(getActivity(), "削除しました", Toast.LENGTH_SHORT).show();
                                                }
                                            }).show();
                                }
                            }).show();
                    return false;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
