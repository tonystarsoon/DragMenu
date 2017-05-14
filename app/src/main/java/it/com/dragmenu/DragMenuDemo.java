package it.com.dragmenu;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.view.ViewHelper;


/**
 * Created by tony on 2017/5/1.
 */
public class DragMenuDemo extends ViewGroup {
    private View mRedChild;
    private View mBlueChild;
    private ViewDragHelper mViewDragHelper;//处理ViewGroup中对子view的拖拽;

    public DragMenuDemo(Context context) {
        this(context, null);
    }

    public DragMenuDemo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragMenuDemo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mViewDragHelper = ViewDragHelper.create(this, callBack);
    }

    /**
     * Finalize inflating a view from XML.  This is called as the last phase
     * of inflation, after all child views have been added.
     * <p>Even if the subclass overrides onFinishInflate, they should always be
     * sure to call the super method, so that we get called.
     */
    @Override
    protected void onFinishInflate() {
        mRedChild = getChildAt(0);
        mBlueChild = getChildAt(1);
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
     /*
        int redWidthSpec = MeasureSpec.makeMeasureSpec(mRedChild.getLayoutParams().width, MeasureSpec.EXACTLY);
        int redHeightSpec = MeasureSpec.makeMeasureSpec(mRedChild.getLayoutParams().height, MeasureSpec.EXACTLY);
        mRedChild.measure(redWidthSpec, redHeightSpec);

        int blueWidthSpec = MeasureSpec.makeMeasureSpec(mBlueChild.getLayoutParams().width, MeasureSpec.EXACTLY);
        int blueHeightSpec = MeasureSpec.makeMeasureSpec(mBlueChild.getLayoutParams().height, MeasureSpec.EXACTLY);
        mBlueChild.measure(blueWidthSpec, blueHeightSpec);
    */

        //2.如果没有对子view的特殊的测量的话,可以使用如下的方法进行测量
        measureChild(mRedChild, widthMeasureSpec, heightMeasureSpec);
        measureChild(mBlueChild, widthMeasureSpec, heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = left + mRedChild.getMeasuredWidth();
        int bottom = top + mRedChild.getMeasuredHeight();
        mRedChild.layout(left, top, right, bottom);
        mBlueChild.layout(left, top + mBlueChild.getMeasuredHeight(), right, bottom + mBlueChild.getMeasuredHeight());
    }

    private ViewDragHelper.Callback callBack = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mBlueChild || child == mRedChild;
        }

        //不能用于控制水平方向的边界,主要用于当手抬起的时候,child缓慢移动到指定位置的动画的计算上面
        @Override
        public int getViewHorizontalDragRange(View child) {
            return getMeasuredWidth() - getPaddingRight() - child.getMeasuredWidth();
        }

        //不能用于控制垂直方向的边界,主要用于当手抬起的时候,child缓慢移动到指定位置的动画的计算上面
        @Override
        public int getViewVerticalDragRange(View child) {
            return getMeasuredHeight() - child.getMeasuredHeight();
        }

        /**
         * 控制水平方向的移动距离
         * @param child
         * @param left  想要view水平方向移动的到的位置
         * @param dx    计算出的view本次水平方向可移动的距离为dx
         * @return 返回想要移动的距离;
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left < getPaddingLeft()) {
                left = getPaddingLeft();
            } else if (left > getMeasuredWidth() - getPaddingRight() - child.getMeasuredWidth()) {
                left = getMeasuredWidth() - getPaddingRight() - child.getMeasuredWidth();
            }
            return left;
        }

        /**
         * 控制垂直方向的移动距离
         * @param child
         * @param top  想要view垂直方向移动的到的位置
         * @param dy    计算出的view本次垂直方向可移动的距离为dy
         * @return 返回想要移动的距离;
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (top < getPaddingTop()) {
                top = getPaddingTop();
            } else if (top > getMeasuredHeight() - getPaddingBottom() - child.getMeasuredHeight()) {
                top = getMeasuredHeight() - getPaddingBottom() - child.getMeasuredHeight();
            }
            return top;
        }

        /**
         * 位置改变的时候
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mRedChild) {
                mBlueChild.layout(mBlueChild.getLeft() + dx,
                        mBlueChild.getTop() + dy,
                        mBlueChild.getRight() + dx,
                        mBlueChild.getBottom() + dy);
            } else if (changedView == mBlueChild) {
                mRedChild.layout(mRedChild.getLeft() + dx,
                        mRedChild.getTop() + dy,
                        mRedChild.getRight() + dx,
                        mRedChild.getBottom() + dy);
            }
            int realTotalWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - mRedChild.getMeasuredWidth();
            int currentWidth = mRedChild.getLeft() - getPaddingRight();
            float fraction = currentWidth * 1.0f / realTotalWidth;

            executeAnimation(mRedChild, fraction);
            Log.i("fraction:", "--------" + fraction);
        }

        /**
         * 触摸事件被释放,即手指抬起.
         * @param releasedChild
         * @param xvel
         * @param yvel
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int centerHorizontal = getMeasuredWidth() / 2 - releasedChild.getMeasuredWidth() / 2;

            //如果靠近左边,缓慢移动到左边
            if (releasedChild.getLeft() < centerHorizontal) {
                mViewDragHelper.smoothSlideViewTo(releasedChild
                        , getPaddingLeft()
                        , releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragMenuDemo.this);
            }
            //如果靠近的是右边,缓慢移动的到右边
            else {
                mViewDragHelper.smoothSlideViewTo(releasedChild,
                        getMeasuredWidth() - getPaddingRight() - releasedChild.getWidth()
                        , releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragMenuDemo.this);
            }
        }
    };

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(DragMenuDemo.this);
        }
    }

    /**
     * 使用nineoldandroids中的动画
     *
     * @param targetView
     * @param mFraction
     */
    private void executeAnimation(View targetView, final float mFraction) {
        ViewHelper.setAlpha(targetView, 1 - mFraction * 0.5f);
        ViewHelper.setRotationX(targetView, 720 * mFraction);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件交由mViewDragHelper去处理
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        //有mViewDragHelper去判断是否需要拦截事件
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }
}
