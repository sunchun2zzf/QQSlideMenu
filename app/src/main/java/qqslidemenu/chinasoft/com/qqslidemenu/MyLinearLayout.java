package qqslidemenu.chinasoft.com.qqslidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * 当slideMenu打开时，拦截并消费掉事件
 */
public class MyLinearLayout extends LinearLayout {
    private SlideMenu slideMenu;
    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (slideMenu != null && slideMenu.getCurrentState()==SlideMenu.DrageState.OPEN) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("MyLinearLayout", "onTouchEvent: slideMenu:state: " + slideMenu.getCurrentState());
        if (slideMenu != null && slideMenu.getCurrentState()==SlideMenu.DrageState.OPEN) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                slideMenu.closeSlideMenu();
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void setSlideMenu (SlideMenu slideMenu) {
        this.slideMenu = slideMenu;
    }

}
