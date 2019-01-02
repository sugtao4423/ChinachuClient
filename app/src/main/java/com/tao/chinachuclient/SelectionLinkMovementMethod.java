package com.tao.chinachuclient;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

public class SelectionLinkMovementMethod extends ArrowKeyMovementMethod{

    private static Context movementContext;
    private static SelectionLinkMovementMethod linkMovementMethod = new SelectionLinkMovementMethod();

    public static MovementMethod getInstance(Context c){
        movementContext = c;
        return linkMovementMethod;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event){
        int action = event.getAction();

        if(action == MotionEvent.ACTION_UP){
            int x = (int)event.getX();
            int y = (int)event.getY();
            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if(link.length != 0){
                String url = link[0].getURL();
                if(url.contains("https") | url.contains("tel") | url.contains("mailto") | url.contains("http") | url.contains("https") | url.contains("www")){
                    movementContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                return true;
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }
}
