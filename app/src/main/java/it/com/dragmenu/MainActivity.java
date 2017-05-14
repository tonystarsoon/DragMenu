package it.com.dragmenu;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.animation.CycleInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.com.dragmenu.adapter.AnswersAdapter;
import it.com.dragmenu.dragmenu.MenuLayout;
import it.com.dragmenu.model.Item;
import it.com.dragmenu.model.SOAnswersResponse;
import it.com.dragmenu.remote.RxService;
import it.com.dragmenu.util.ApiUtils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {
    @BindView(R.id.menu_listview)
    RecyclerView mMenuListview;
    @BindView(R.id.iv_head)
    ImageView mIvHead;
    @BindView(R.id.main_listview)
    RecyclerView mRecyclerView;
    @BindView(R.id.my_layout)
    LinearLayout mMyLayout;
    @BindView(R.id.dragMenu)
    MenuLayout mDragMenuLayout;
    private RxService mRxService;
    private AnswersAdapter mAnswersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meun_main);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        ButterKnife.bind(this);
        init();
        loadAnswersWithRxJava();
    }

    private void loadAnswersWithRxJava() {
        mRxService = ApiUtils.getRxJavaSOService();
        mRxService.getAnswers().subscribeOn(Schedulers.io())//请求数据的事件发生在io线程
                .observeOn(AndroidSchedulers.mainThread())
                // subscribeOn()    指定的是上游发送事件的线程,
                // observeOn()      指定的是下游接收事件的线程.
                .subscribe(new Subscriber<SOAnswersResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.i("onCompleted", "--------onCompleted-------");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("onError", "--------onError-------");
                    }

                    @Override
                    public void onNext(final SOAnswersResponse soAnswersResponse) {
                        mAnswersAdapter.updateAnswers(soAnswersResponse.getItems());
                        Log.i("onNext", "--------onNext-------");
                    }
                });
    }

    private void init() {
        mAnswersAdapter = new AnswersAdapter(this, new ArrayList<Item>(0),
                new AnswersAdapter.PostItemListener() {
                    @Override
                    public void onPostClick(long id) {
                        Toast.makeText(MainActivity.this, "Post id is" + id, Toast.LENGTH_SHORT).show();
                    }
                });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAnswersAdapter);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        mDragMenuLayout.setOnDragStateChangeListener(new MenuLayout.OnDragStateChangeListener() {
            @Override
            public void onDraging(float fraction) {
                ViewHelper.setAlpha(mIvHead, 1 - fraction);
            }
            @Override
            public void onClosed() {
                ViewPropertyAnimator viewPropertyAnimator = ViewPropertyAnimator
                        .animate(mIvHead)
                        .translationXBy(20)
                        .setDuration(50)
                        .setInterpolator(new CycleInterpolator(5));
                viewPropertyAnimator.start();
            }
            @Override
            public void onOpened() { }
        });
    }
}



