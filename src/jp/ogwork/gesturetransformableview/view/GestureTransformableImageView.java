package jp.ogwork.gesturetransformableview.view;

import jp.ogwork.gesturetransformableview.gesture.DragGestureDetector;
import jp.ogwork.gesturetransformableview.gesture.DragGestureDetector.DragGestureListener;
import jp.ogwork.gesturetransformableview.gesture.PinchGestureDetector;
import jp.ogwork.gesturetransformableview.gesture.PinchGestureDetector.PinchGestureListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

@SuppressLint("ClickableViewAccessibility")
public class GestureTransformableImageView extends ImageView implements OnTouchListener{

	public static final int GESTURE_DRAGGABLE = 0x0001;

	public static final int GESTURE_ROTATABLE = 0x0002;

	public static final int GESTURE_SCALABLE = 0x0004;

	public static final String TAG = GestureTransformableImageView.class.getName();

	public static final float DEFAULT_LIMIT_SCALE_MAX = 3.5f;

	public static final float DEFAULT_LIMIT_SCALE_MIN = 1f;

	private float limitScaleMax = DEFAULT_LIMIT_SCALE_MAX;

	private float limitScaleMin = DEFAULT_LIMIT_SCALE_MIN;

	private float scaleFactor = 1.0f;

	private DragGestureDetector dragGestureDetector;

	private PinchGestureDetector pinchGestureDetector;

	private float angle;

	public GestureTransformableImageView(Context context){
		super(context);
		setOnTouchListener(this);
		dragGestureDetector = new DragGestureDetector(new DragListener());
		pinchGestureDetector = new PinchGestureDetector(new ScaleListener());
	}

	public GestureTransformableImageView(Context context, AttributeSet attrs){
		super(context, attrs);
		setOnTouchListener(this);
		dragGestureDetector = new DragGestureDetector(new DragListener());
		pinchGestureDetector = new PinchGestureDetector(new ScaleListener());
	}

	public void setLimitScaleMax(float limit){
		this.limitScaleMax = limit;
	}

	public void setLimitScaleMin(float limit){
		this.limitScaleMin = limit;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event){
		if(dragGestureDetector != null) {
			dragGestureDetector.onTouchEvent(event);
		}

		if(pinchGestureDetector != null) {
			pinchGestureDetector.onTouchEvent(event);
		}

		return true;
	}

	private PointF rotateXY(float centerX, float centerY, float angle, float x, float y){
		float resultX = 0;
		float resultY = 0;

		double rad = Math.toRadians(angle);

		resultX = (float)((x - centerX) * Math.cos(rad) - (y - centerY) * Math.sin(rad) + centerX);
		resultY = (float)((x - centerX) * Math.sin(rad) + (y - centerY) * Math.cos(rad) + centerY);

		return new PointF(resultX, resultY);
	}

	private class ScaleListener implements PinchGestureListener{
		@Override
		public void onPinchGestureListener(PinchGestureDetector dragGestureDetector){
			float scale = dragGestureDetector.getDistance() / dragGestureDetector.getPreDistance();
			float tmpScale = scaleFactor * scale;

			if(limitScaleMin <= tmpScale && tmpScale <= limitScaleMax) {
				scaleFactor = tmpScale;
				setScaleX(scaleFactor);
				setScaleY(scaleFactor);
				return;
			}

		}
	}

	private class DragListener implements DragGestureListener{
		@Override
		synchronized public void onDragGestureListener(DragGestureDetector dragGestureDetector){
			float dx = dragGestureDetector.getDeltaX();
			float dy = dragGestureDetector.getDeltaY();
			PointF pf = rotateXY(0, 0, angle, dx, dy);

			dx = pf.x;
			dy = pf.y;

			setX(getX() + dx * scaleFactor);
			setY(getY() + dy * scaleFactor);
		}
	}
}
