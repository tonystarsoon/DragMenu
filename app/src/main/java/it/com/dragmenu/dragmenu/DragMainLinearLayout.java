package it.com.dragmenu.dragmenu;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by tony on 2017/5/1.
 */

public class DragMainLinearLayout extends LinearLayout {
    private MenuLayout mMenuLayout;

    public DragMainLinearLayout(Context context) {
        super(context, null);
    }

    public DragMainLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragMainLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void setDragMenu(MenuLayout menuLayout) {
        this.mMenuLayout = menuLayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mMenuLayout != null && mMenuLayout.getMenuState() == MenuLayout.DragState.OPEN) {
            Log.i("lanjie", "-----------------拦截");
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMenuLayout != null
                && mMenuLayout.getMenuState() == MenuLayout.DragState.OPEN
                && event.getAction() == MotionEvent.ACTION_UP) {
            Log.i("lanjie", "-----------------onTouchEvent");
            mMenuLayout.closeMenu();
        }
        return true;
    }
}

