package com.tao.chinachuclient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tenthbit.view.ZoomImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShowImage extends AppCompatActivity{

    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 810;

    private String programId;
    private int pos;
    private byte[] byteImage;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image);
        ZoomImageView image = (ZoomImageView)findViewById(R.id.show_image_image);

        Intent i = getIntent();
        final String base64 = i.getStringExtra("base64");
        pos = i.getIntExtra("pos", -1);
        programId = i.getStringExtra("programId");
        byteImage = Base64.decode(base64, Base64.DEFAULT);

        image.setImageBitmap(BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length));
    }

    public void image_option_click(View v){
        new AlertDialog.Builder(this)
                .setMessage(R.string.is_save)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        saveImage();
                    }
                }).show();
    }

    public void saveImage(){
        if(!hasWriteExternalStoragePermission()){
            requestWriteExternalStoragePermission();
            return;
        }

        final String fileName;
        if(pos == -1)
            fileName = programId;
        else
            fileName = programId + "-" + pos;
        final String type = ".jpg";
        final String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;
        final String imgPath = saveDir + "/" + fileName + type;

        if(new File(imgPath).exists()){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error_file_already_exists)
                    .setItems(getResources().getStringArray(R.array.file_already_exists_fix_suggestion), new OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            if(which == 0){
                                save(imgPath);
                            }else if(which == 1){
                                final EditText edit = new EditText(ShowImage.this);
                                edit.setText(fileName);
                                new AlertDialog.Builder(ShowImage.this)
                                        .setTitle(R.string.assign_file_name)
                                        .setView(edit)
                                        .setNegativeButton(R.string.cancel, null)
                                        .setPositiveButton(R.string.ok, new OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which){
                                                String newPath = saveDir + "/" + edit.getText().toString() + type;
                                                if(new File(newPath).exists())
                                                    saveImage();
                                                else
                                                    save(newPath);
                                            }
                                        }).show();
                            }
                        }
                    }).show();
        }else{
            save(imgPath);
        }
    }

    public void save(String imgPath){
        FileOutputStream fos;
        try{
            fos = new FileOutputStream(imgPath, true);
            fos.write(byteImage);
            fos.close();
        }catch(IOException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(R.string.saved) + "\n" + imgPath, Toast.LENGTH_LONG).show();
    }

    public boolean hasWriteExternalStoragePermission(){
        int writeExternalStorage = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (writeExternalStorage == PackageManager.PERMISSION_GRANTED);
    }

    public void requestWriteExternalStoragePermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != REQUEST_CODE_WRITE_EXTERNAL_STORAGE){
            return;
        }
        if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            saveImage();
        }else{
            Toast.makeText(getApplicationContext(), R.string.fail_permission, Toast.LENGTH_LONG).show();
        }
    }

}