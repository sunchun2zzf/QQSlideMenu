package qqslidemenu.chinasoft.com.qqslidemenu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.view.ViewHelper;

public class SlideMenu extends FrameLayout {
    private View menuView; //菜单View
    private View mainView; //主界面View
    private ViewDragHelper viewDragHelper;
    private int width;
    private float dragRange;
    private FloatEvaluator floatEvaluator;
    private OnDragStateChangeListener listener;

    enum DrageState {
        OPEN, CLOSE;
    }

    private DrageState currentState = DrageState.CLOSE;

    public SlideMenu(@androidx.annotation.NonNull Context context) {
        super(context);
        init();
    }

    public SlideMenu(@androidx.annotation.NonNull Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenu(@androidx.annotation.NonNull Context context, @androidx.annotation.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, callback);
        floatEvaluator = new FloatEvaluator();
    }

    public DrageState getCurrentState() {
        return currentState;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //判断总个数是否不是两个
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("只能有两个子View");
        }
        menuView = getChildAt(0);
        mainView = getChildAt(1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 该方法在onMeasure方法初始化之后执行
     * 初始化控件的宽高
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        dragRange = width * 0.6f;
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View view, int i) {
            return view == menuView || view == mainView;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == menuView) {
                menuView.layout(0, 0, menuView.getRight(), menuView.getBottom());
                int newLeft = mainView.getLeft() + dx;
                if (newLeft < 0) newLeft = 0;
                if (newLeft > dragRange) newLeft = (int) dragRange;
                mainView.layout(newLeft, mainView.getTop() + dy, newLeft + menuView.getMeasuredWidth(), mainView.getBottom() + dy);
            }

            //1.计算滑动的百分比
            float fraction = mainView.getLeft() / dragRange;
            //2.执行伴随动画
            excuteAnim(fraction);
            //3.更改状态，回调listener
            if (fraction == 0 && currentState != DrageState.CLOSE) {
                currentState = DrageState.CLOSE;
                if (listener != null) {
                    listener.onClose();
                }
            } else if (fraction == 1 && currentState != DrageState.OPEN) {
                currentState = DrageState.OPEN;
                if (listener != null) {
                    listener.onOpen();
                }
            }
            if (listener != null) {
                listener.onDraging(fraction);
            }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (mainView.getLeft() < dragRange / 2) {
                //左
                Log.e("SlideMenu: ", "onViewReleased:左");
                closeSlideMenu();
            } else {
                //右
                Log.e("SlideMenu: ", "onViewReleased:右");
                openSlideMenu();
            }

            //优化用户体验
            Log.e("onViewReleased: ", "xvel:" + xvel);
            if (xvel > 200 && currentState != DrageState.OPEN) {
                openSlideMenu();
            } else if (xvel < -200 && currentState != DrageState.CLOSE) {
                closeSlideMenu();
            }
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return (int) dragRange;
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return super.getViewVerticalDragRange(child);
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (child == mainView) {
                if (left < 0) {
                    left = 0;
                }
                if (left > dragRange) {
                    left = (int) dragRange;
                }
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        }
    };

    private void excuteAnim(float fraction) {
        // mainView的动画
        Float evaluate = floatEvaluator.evaluate(fraction, 1f, 0.8f);
        ViewHelper.setScaleX(mainView, evaluate);
        ViewHelper.setScaleY(mainView, evaluate);

        // menuView的动画
        ViewHelper.setTranslationX(menuView, floatEvaluator.evaluate(fraction, -menuView.getMeasuredWidth() / 2f, 0f));
        ViewHelper.setScaleX(menuView, floatEvaluator.evaluate(fraction, 0.5f, 1));

        //改变menuView的透明度
        ViewHelper.setAlpha(menuView, floatEvaluator.evaluate(fraction, 0.3f, 1f));

        //给SlideMenu的背景添加黑色的遮罩
        getBackground().setColorFilter((int) ColorUtil.evaluateColor(fraction, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    public void closeSlideMenu() {
        viewDragHelper.smoothSlideViewTo(mainView, 0, 0);
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    public void openSlideMenu() {
        viewDragHelper.smoothSlideViewTo(mainView, (int) dragRange, 0);
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    public void setOnDragListener(OnDragStateChangeListener listener) {
        this.listener = listener;
    }

    public interface OnDragStateChangeListener {
        void onOpen();

        void onClose();

        void onDraging(float fraction);
    }
}
