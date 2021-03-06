package com.angcyo.uiview.design;

import android.content.Context;
import android.support.annotation.Px;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import com.angcyo.uiview.recycler.RRecyclerView;
import com.angcyo.uiview.utils.Reflect;

/**
 * Created by angcyo on 2017-03-15.
 */

public class StickLayout2 extends RelativeLayout {

    View mFloatView;
    int floatTopOffset = 0;
    int floatTop = 0;//
    float downY, downX, lastX;
    StickLayout.CanScrollUpCallBack mScrollTarget;
    boolean inTopTouch = false;
    boolean isFirst = true;
    StickLayout.OnScrollListener mOnScrollListener;
    private OverScroller mOverScroller;
    private GestureDetectorCompat mGestureDetectorCompat;
    private int maxScrollY, topHeight;
    private RRecyclerView.OnFlingEndListener mOnFlingEndListener;
    private boolean handleTouch = true;
    private float lastOffsetY;
    private float mLastVelocity = 0f;
    private boolean isFling;

    /**
     * 滚动顶部后, 是否可以继续滚动, 增益效果
     */
    private boolean edgeScroll = false;
    private int viewMaxHeight;

    public StickLayout2(Context context) {
        this(context, null);
    }

    public StickLayout2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    public void setEdgeScroll(boolean edgeScroll) {
        this.edgeScroll = edgeScroll;
    }

    private void initLayout() {
        mOverScroller = new OverScroller(getContext());
        mGestureDetectorCompat = new GestureDetectorCompat(getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, final float velocityY) {
                        if (Math.abs(velocityX) > Math.abs(velocityY)) {
                            return false;
                        }

                        if (isFloat() /*&& velocityY < 0*/) {
                            return false;
                        }
                        fling(velocityY);
                        return true;
                    }
                });
    }

    private void fling(float velocityY) {
        isFling = true;
        int maxY = viewMaxHeight;
        final RecyclerView recyclerView = mScrollTarget.getRecyclerView();
        if (recyclerView != null) {
            maxY = Math.max(maxY, recyclerView.computeVerticalScrollRange());
        }
        mOverScroller.fling(0, getScrollY(), 0, (int) -velocityY, 0, 0, 0, maxY);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            int currY = mOverScroller.getCurrY();
            if (currY - maxScrollY >= 0) {
                if (isFling) {
                    final RecyclerView recyclerView = mScrollTarget.getRecyclerView();
                    if (recyclerView != null) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                //recyclerView.fling(0, Math.max(0, 1000));


                                final float lastVelocity = getLastVelocity();
                                recyclerView.fling(0, (int) Math.max(0, Math.abs(lastVelocity)));

//                                int velocityDecay = getChildAt(0).getMeasuredHeight() * 3;//速度衰减值
//                                if (lastVelocity < velocityDecay) {
//                                    recyclerView.fling(0, Math.max(0, (int) Math.abs(lastVelocity)));
//                                } else {
//                                    recyclerView.fling(0, Math.max(0, (int) lastVelocity - velocityDecay));
//                                }
                            }
                        });
                    }
                }
                isFling = false;
            }
            scrollTo(0, currY);
            postInvalidate();
        }
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        int offset = Math.min(maxScrollY, edgeScroll ? y : Math.max(0, y));
        boolean layout = false;
        if (getScrollY() != offset) {
            layout = true;
        }
        super.scrollTo(0, offset);
        if (layout) {
            requestLayout();
        }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollTo(offset);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        View topView = getChildAt(0);
        View scrollView = getChildAt(1);
        if (scrollView instanceof StickLayout.CanScrollUpCallBack) {
            mScrollTarget = (StickLayout.CanScrollUpCallBack) scrollView;
        } else {
            if (mScrollTarget == null) {
                mScrollTarget = new StickLayout.CanScrollUpCallBack() {
                    @Override
                    public boolean canChildScrollUp() {
                        return false;
                    }

                    @Override
                    public RecyclerView getRecyclerView() {
                        return null;
                    }
                };
            }
        }
        mFloatView = getChildAt(2);

        measureChild(topView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED));
        measureChild(mFloatView, widthMeasureSpec, heightMeasureSpec);
        measureChild(scrollView, widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(heightSize - mFloatView.getMeasuredHeight() - floatTopOffset, MeasureSpec.EXACTLY));

        floatTop = topView.getMeasuredHeight();
        maxScrollY = floatTop - floatTopOffset;
        topHeight = floatTop + mFloatView.getMeasuredHeight();

        viewMaxHeight = floatTop + mFloatView.getMeasuredHeight() + scrollView.getMeasuredHeight();
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //super.onLayout(changed, l, t, r, b);
        //L.w("onLayout() -> " + changed + " l:" + l + " t:" + t + " r:" + r + " b:" + b);
        View firstView = getChildAt(0);
        firstView.layout(0, 0, r, firstView.getMeasuredHeight());

        View lastView = getChildAt(1);
        lastView.layout(0, firstView.getMeasuredHeight() + mFloatView.getMeasuredHeight(), r,
                firstView.getMeasuredHeight() + mFloatView.getMeasuredHeight() + lastView.getMeasuredHeight());

        if (mFloatView != null) {
            int scrollY = getScrollY();
            if (isFloat()) {
                mFloatView.layout(mFloatView.getLeft(), scrollY + floatTopOffset, r,
                        scrollY + floatTopOffset + mFloatView.getMeasuredHeight());
            } else {
                mFloatView.layout(mFloatView.getLeft(), firstView.getMeasuredHeight(), r,
                        firstView.getMeasuredHeight() + mFloatView.getMeasuredHeight());
            }
        }

        initScrollTarget();
    }

    private void initScrollTarget() {
        if (mScrollTarget != null && mScrollTarget.getRecyclerView() instanceof RRecyclerView) {
            if (mOnFlingEndListener == null) {
                mOnFlingEndListener = new RRecyclerView.OnFlingEndListener() {
                    @Override
                    public void onScrollTopEnd(float currVelocity) {
//                        if (!(currVelocity > 0)) {
//                            //向下滑动产生的fling操作, 才处理
//                            fling(currVelocity);
//                        }
                        fling(currVelocity);
                    }
                };
            }
            ((RRecyclerView) mScrollTarget.getRecyclerView()).setOnFlingEndListener(mOnFlingEndListener);
        }
    }

    private boolean isFloat() {
        return getScrollY() >= (floatTop - floatTopOffset);
    }

    public void setFloatTopOffset(int floatTopOffset) {
        this.floatTopOffset = floatTopOffset;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            onTouchUp();
            onTouchEnd();
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (!inTopTouch) {
//            return super.dispatchTouchEvent(ev);
//        }
        boolean event = mGestureDetectorCompat.onTouchEvent(ev);
        if (event) {
            ev.setAction(MotionEvent.ACTION_CANCEL);
            //return super.dispatchTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!handleTouch) {
                    break;
                }

                float moveY = ev.getY() + 0.5f;
                float moveX = ev.getX() + 0.5f;
                float offsetY = downY - moveY;
                float offsetX = downX - moveX;

                downY = moveY;
                downX = moveX;

                boolean wantV;
                if (Math.abs(offsetX) > Math.abs(offsetY)) {
                    wantV = false;
                } else if (Math.abs(offsetY) > Math.abs(offsetX)) {
                    wantV = true;
                } else {
                    break;
                }

                boolean first = isFirst;
                isFirst = false;

                //L.e("dispatchTouchEvent() -> " + offsetX + " " + offsetY + " w:" + wantV + "  f:" + first);

                if (first) {
                    if (!wantV) {
                        handleTouch = false;
                        break;
                    }
                } else {
                    if (!inTopTouch) {
                        ev.setLocation(lastX, moveY);
                    }
                }
                offsetTo(offsetY);

                lastOffsetY = offsetY;
                //L.e("call: dispatchTouchEvent([ev])-> move..." + ensureOffset(offsetY));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onTouchUp();
                onTouchEnd();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private float ensureOffset(float offsetY) {
        int scrollY = getScrollY();
        int maxScrollY = this.maxScrollY;
        float scrollOffset = 0;

        int maxOffset = maxScrollY - scrollY;
        int minOffset = 0 - scrollY;
        scrollOffset = Math.max(minOffset, Math.min(maxOffset, offsetY));

        return scrollOffset;
    }

    private void onTouchUp() {
        downY = 0;
        downX = 0;
        isFirst = true;
        handleTouch = true;
    }

    private void onTouchEnd() {
        if (getScrollY() < 0) {
            startScrollTo(0);
        }
    }

    private void startScrollTo(int to) {
        int scrollY = getScrollY();
        mOverScroller.startScroll(0, scrollY, 0, to - scrollY);
        postInvalidate();
    }

    private boolean offsetTo(float offsetY) {
        if (Math.abs(offsetY) > 0) {
            if (offsetY < 0) {
                //手指下滑
                boolean scrollVertically = mScrollTarget.canChildScrollUp();

                if (getScrollY() < 0) {
                    offsetY *= (1 - 0.4 - Math.abs(getScrollY()) * 1.f / getMeasuredHeight());
                }

                if (!scrollVertically) {
                    scrollBy(0, (int) (offsetY));
                } else {
                    return true;
                }
            } else {
                if (isFloat()) {
                    return true;
                }
                scrollBy(0, (int) (offsetY));
            }
        }
        return false;
    }

    private void onTouchDown(MotionEvent ev) {
        onTouchUp();

        downY = ev.getY() + 0.5f;
        lastX = downX = ev.getX() + 0.5f;
        int scrollY = getScrollY();

        mOverScroller.abortAnimation();

        isFling = false;

        if (isFloat()) {
            if (mFloatView.getMeasuredHeight() + floatTopOffset > downY) {
                inTopTouch = true;
            } else {
                inTopTouch = false;
            }
        } else {
            if (topHeight - scrollY > downY) {
                inTopTouch = true;
            } else {
                inTopTouch = false;
            }
        }
        isFirst = true;

        initScrollTarget();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return true;
    }

    /**
     * 滚动结束后时的速率
     */
    public float getLastVelocity() {
        Object mScrollerY = Reflect.getMember(OverScroller.class, mOverScroller, "mScrollerY");
        float currVelocity = (float) Reflect.getMember(mScrollerY, "mCurrVelocity");
        if (Float.isNaN(currVelocity)) {
            currVelocity = mLastVelocity;
        } else {
            mLastVelocity = currVelocity;
        }
        return currVelocity;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
        //L.e("call: onNestedPreScroll([target, dx, dy, consumed])-> scroll..." + dy);
        if (dy > 0) {
            consumed[1] = (int) Math.min(dy, ensureOffset(lastOffsetY));
        }
    }

//    @Override
//    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        if (Math.abs(velocityX) > Math.abs(velocityY)) {
//            return false;
//        }
//
//        if (isFloat() && velocityY > 0) {
//            //L.e("call: onFling return");
//            return super.onNestedPreFling(target, velocityX, velocityY);
//        }
//        fling(velocityY);
//        return super.onNestedPreFling(target, velocityX, velocityY);
//    }

    public void setOnScrollListener(StickLayout.OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }
}
