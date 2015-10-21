package com.tao.chinachuclient;

import jp.ogwork.gesturetransformableview.view.GestureTransformableImageView;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

public class Show_Image extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		final String base64 = getIntent().getStringExtra("base64");
		byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);

		final GestureTransformableImageView image = new GestureTransformableImageView(this,
				GestureTransformableImageView.GESTURE_DRAGGABLE | GestureTransformableImageView.GESTURE_SCALABLE);
		image.setLimitScaleMin(1F);
		image.setLimitScaleMax(3.5F);
		setContentView(image);
		image.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
	}
}