package it.com.dragmenu.dragmenu;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.AttrRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.view.ViewHelper;

import static android.content.ContentValues.TAG;

/**
 * Created by tony on 2017/5/1.
 */
public class MenuLayout extends FrameLayout {
    private View mMenuView;
    private View mMainView;
    private ViewDragHelper mViewDragHelper;
    private int mWidth;
    private int mDragRange;
    private OnDragStateChangeListener mListener;
    private FloatEvaluator mFloatEvaluator;
    private ArgbEvaluator argbEvaluator;
    private DragState mCurrentState;

    enum DragState {
        CLOSE, OPEN;
    }

    public MenuLayout(Context context) {
        this(context, null);
    }

    public MenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuLayout(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mViewDragHelper = ViewDragHelper.create(this, callback);
        mFloatEvaluator = new FloatEvaluator();
        argbEvaluator = new ArgbEvaluator();
        mCurrentState = DragState.CLOSE;
    }

    /**
     * Finalize inflating a view from XML.  This is called as the last phase
     * of inflation, after all child views have been added.
     * <p>Even if the subclass overrides onFinishInflate, they should always be
     * sure to call the super method, so that we get called.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("MenuLayout must and only have 2 Childrens.");
        }
        mMenuView = getChildAt(0);
        mMainView = getChildAt(1);
        ((DragMainLinearLayout) mMainView).setDragMenu(this);
    }

    /**
     * 在onMeasure方法执行完成后执行,初始化父控件和子控件的宽高.
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mDragRange = (int) (mWidth * 0.6);
    }

    /**
     * 交由mViewDragHelper判断是否需要拦截事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    /**
     * 将事件传递给mViewDragHelper去处理
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        private int menuDx = 0;

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mMainView || child == mMenuView;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragRange;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mMainView) {
                if (left < getPaddingLeft()) {
                    left = getPaddingLeft();
                } else if (left > mDragRange) {
                    left = mDragRange;
                }
            }
            if (child == mMenuView) {
                menuDx = dx;
                return 0;
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mMenuView) {
                int newLeft = mMainView.getLeft() + menuDx;
                if (newLeft < getPaddingLeft()) {
                    newLeft = getPaddingLeft();
                } else if (newLeft > mDragRange) {
                    newLeft = mDragRange;
                }
                mMainView.layout(newLeft, mMainView.getTop(),
                        newLeft + mMainView.getMeasuredWidth(),
                        mMainView.getTop() + mMainView.getMeasuredHeight());
            }
            float fraction = mMainView.getLeft() * 1.0f / mDragRange;
            executeAnimation(fraction);
            Log.i(TAG, "--------------------------fraction: "+fraction);
            if (fraction == 0f && mCurrentState != DragState.CLOSE) {
                mCurrentState = DragState.CLOSE;
                if (mListener != null) mListener.onClosed();
            } else if (fraction == 1.0f && mCurrentState != DragState.OPEN) {
                mCurrentState = DragState.OPEN;
                if (mListener != null) mListener.onOpened();
            }
            if (mListener != null) {
                mListener.onDraging(fraction);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.i("clod", "-----------------" + xvel);
            if (Math.abs(xvel) > 600) {//如果用户的滑动速度超过了一定的界限的话,执行滑动到指定的位置.
                if (xvel < 0 && mCurrentState != DragState.CLOSE) {
                    closeMenu();
                    return;
                } else if (xvel > 0 && mCurrentState != DragState.OPEN) {
                    openMenu();
                    return;
                }
            }

            int mMainViewLeft = mMainView.getLeft();
            if (mMainViewLeft < mDragRange / 2) {
                closeMenu();
            } else {
                openMenu();
            }
        }
    };

    public void openMenu() {
        mViewDragHelper.smoothSlideViewTo(mMainView, mDragRange, mMainView.getTop());
        ViewCompat.postInvalidateOnAnimation(MenuLayout.this);
    }

    public void closeMenu() {
        mViewDragHelper.smoothSlideViewTo(mMainView, 0, mMainView.getTop());
        ViewCompat.postInvalidateOnAnimation(MenuLayout.this);
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(MenuLayout.this);
        }
    }

    //拖拽过程中menu和main跟随动画
    private void executeAnimation(float fraction) {
        ViewHelper.setAlpha(mMainView, mFloatEvaluator.evaluate(fraction, 1.0f, 0.5f));
        ViewHelper.setScaleX(mMainView, mFloatEvaluator.evaluate(fraction, 1.0f, 0.85f));
        ViewHelper.setScaleY(mMainView, mFloatEvaluator.evaluate(fraction, 1.0f, 0.85f));

        ViewHelper.setTranslationX(mMenuView, mFloatEvaluator.evaluate(fraction, -mMenuView.getMeasuredWidth() / 5, 0));
        ViewHelper.setScaleX(mMenuView, mFloatEvaluator.evaluate(fraction, 0.85f, 1.0f));
        ViewHelper.setScaleY(mMenuView, mFloatEvaluator.evaluate(fraction, 0.85f, 1.0f));
        ViewHelper.setAlpha(mMenuView, mFloatEvaluator.evaluate(fraction, 0.5f, 1.0f));

        //设置menu背景色的渐变,给背景蒙上一层颜色
        Integer color = (Integer) argbEvaluator.evaluate(fraction, Color.BLACK, Color.TRANSPARENT);
        getBackground().setColorFilter(color, PorterDuff.Mode.SRC_OVER);
    }

    public DragState getMenuState() {
        return mCurrentState;
    }

    public void setOnDragStateChangeListener(OnDragStateChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnDragStateChangeListener {
        void onDraging(float fraction);

        void onOpened();

        void onClosed();
    }
}








