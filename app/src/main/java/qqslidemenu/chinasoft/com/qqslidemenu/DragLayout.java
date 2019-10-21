package qqslidemenu.chinasoft.com.qqslidemenu;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.nineoldandroids.view.ViewHelper;

public class DragLayout extends ViewGroup {
    private View redView;
    private View blueView;
    private ViewDragHelper viewDragHelper;
    private Scroller scroller;

    public DragLayout(Context context) {
        super(context);
        init();
    }

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, callback);
        scroller = new Scroller(getContext());
    }

    /*
    结束标签读取完毕后，执行该方法
    初始化ziView
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        redView = getChildAt(0);
        blueView = getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量自己的View
//        int size = (int) getResources().getDimension(R.dimen.width);
//        int sizeWidth = redView.getLayoutParams().width;
//        int measureSpecWidth = MeasureSpec.makeMeasureSpec(sizeWidth, MeasureSpec.EXACTLY);
//
//        int sizeHeight = redView.getLayoutParams().height;
//        int measureSpecHeight = MeasureSpec.makeMeasureSpec(sizeHeight, MeasureSpec.EXACTLY);
//
//        redView.measure(measureSpecWidth,measureSpecHeight);
//        blueView.measure(measureSpecWidth,measureSpecHeight);

        measureChild(redView, widthMeasureSpec, heightMeasureSpec);
        measureChild(blueView, widthMeasureSpec, heightMeasureSpec);


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        int left = getPaddingLeft() + getMeasuredWidth() / 2 - redView.getMeasuredWidth() / 2;
        int left = getPaddingLeft();
        int top = getPaddingTop();
        redView.layout(left, top, left + redView.getMeasuredWidth(), top + redView.getMeasuredHeight());
        blueView.layout(left + blueView.getRight(), top, left + blueView.getMeasuredWidth()+redView.getMeasuredWidth(),
                blueView.getMeasuredHeight());

    }

    /*
            View移动总结：
            A:
                1.layout
                2.scrollTo和scrollBy
                3.canvas
            B:ViewDragHelper

         */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         当前是否捕获子view的触摸事件
         child：当前触摸的子View
         return：true就是捕获并解析   false：不处理
         */
        @Override
        public boolean tryCaptureView(@NonNull View view, int i) {
            return true;
        }

        /**
         * 当view被开始捕获和解析的回调
         * capturedChild：当前被捕获的子view
         * @param capturedChild
         * @param activePointerId
         */
        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        /**
         *控制child在水平方向的移动
         * left:表示ViewDragHelper认为你想让当前child的left改变的值，left=child.getLeft() + dx
         * dx:本次child水平移动的距离
         * return：表示你想让child的left变成的值
         * @param child
         * @param left
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (left < 0) {
                left = 0;
            }
            int rightMaxPosition = getMeasuredWidth() - child.getMeasuredWidth();
            if (left > rightMaxPosition) {
                left = rightMaxPosition;
            }
            return left;
        }

        /**
         *
         * @param child
         * @param top
         * @param dy
         * @return
         */
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (top < 0) {
                top = 0;
            }
            int topMaxPosition = getMeasuredHeight() - child.getMeasuredHeight();
            if (top > topMaxPosition) {
                top = topMaxPosition;
            }
            return top;
        }

        /**
         * 获取view水平方向的拖拽范围，但是目前不能限制范围
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return getMeasuredWidth() - child.getMeasuredWidth();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return getMeasuredHeight() - child.getMeasuredHeight();
        }

        /**
         * 当前child的位置改变时执行
         * changeView：位置改变的child
         * left：child当前最新的left
         * top：child当前最新的top
         * dx：本次移动的水平距离
         * dy：本次移动的垂直方向的距离
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == blueView) {
                redView.layout(redView.getLeft() + dx, redView.getTop() + dy, redView.getLeft() + dx + redView.getMeasuredWidth(), redView.getTop() + dy + redView.getMeasuredHeight());
            }

            //1.计算view移动的百分比
            float fraction = changedView.getLeft() * 1.0f / (getMeasuredWidth() - changedView.getMeasuredWidth());
            Log.e("DragLayout", "fraction: " + fraction);
            //2.执行动画
            executeAnim(fraction);
        }

        /**
         * 手指抬起的时候会执行这个方法
         * releasedChild：当前抬起的view
         * xvel：水平方向的速度
         * yvel：垂直方向的速度
         * @param releasedChild
         * @param xvel
         * @param yvel
         */
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int centerLeft = getMeasuredWidth() / 2 - releasedChild.getMeasuredWidth() / 2;
            if (releasedChild.getLeft() < centerLeft) {
                viewDragHelper.smoothSlideViewTo(releasedChild, 0, releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);
            } else {
                viewDragHelper.smoothSlideViewTo(releasedChild, getMeasuredWidth() - releasedChild.getMeasuredWidth(), releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);
            }
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = viewDragHelper.shouldInterceptTouchEvent(ev);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(DragLayout.this);
        }
    }

    private void executeAnim(float fraction) {
//        ViewHelper.setScaleX(redView, 1+0.5f*fraction);
//        ViewHelper.setTranslationX(redView, 80f*fraction);
//        ViewHelper.setRotationX(redView, 1+0.5f*fraction);
//        ViewHelper.setAlpha(redView,fraction);

        //设置过度颜色的过度
//        redView.setBackground();

    }
}
