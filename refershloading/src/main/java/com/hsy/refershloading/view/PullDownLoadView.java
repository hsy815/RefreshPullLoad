package com.hsy.refershloading.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.hsy.refershloading.R;


/**
 * Created by hsy on 16/9/28.
 */

public class PullDownLoadView extends ViewGroup {

    // 事件监听接口
    private onRefreshListener MoveListener;
    // 是否允许下拉刷新
    private boolean mLoadDown;
    // 是否允许上拉加载
    private boolean mLoadPull;
    private LayoutInflater inflater;
    private Scroller LayoutScroller;
    // 最小有效滑动距离(滑动超过该距离才触发刷新/加载操作)
    private static int MinEffectiveScroll;
    // 最后一个content-child-view的index
    private int lastChildIndex;
    //头部布局
    private RelativeLayout LayoutHeader;
    //头部textview
    private TextView pdl_tv_pull;
    //头部加载动画
    private GhostLoadingView pdl_glv_pull;
    //底部布局
    private RelativeLayout LayoutFooter;
    //底部textview
    private TextView pdl_tv_down;
    //底部加载动画
    private EatBeanLoadingView pdl_elv_down;
    // 内容高度(不包括header与footer的高度)
    private int LayoutContentHeight;
    // 当滚动到内容最底部时Y轴所需要滑动的距离
    private int ReachBottomScroll;
    // 用于计算滑动距离的Y坐标中介
    private int LastYMoved;
    // Layout状态
    private int status = NORMAL;
    // 普通状态
    private static final int NORMAL = 0;
    // 意图刷新
    private static final int TRY_REFRESH = 1;
    // 刷新状态
    private static final int REFRESH = 2;
    // 意图加载
    private static final int TRY_LOAD_MORE = 3;
    // 加载状态
    private static final int LOAD_MORE = 4;
    // Scroller的滑动速度
    private static final int SCROLL_SPEED = 100;
    //停止刷新
    private static final int STOP_REFRESH = 1;
    //停止加载
    private static final int STOP_LOAD_MORE = 2;
    // 用于判断是否拦截触摸事件的Y坐标中介
    private int LastYIntercept;
    //头部滑动距离
    private int MoveDistanceTop = 0;
    //底部滑动距离
    private int MoveDistanceBtn = 0;
    //滑动到全屏
    public static final int MoveDistanceAll = -1;
    //滑动到半屏
    public static final int MoveDistanceIn = -2;

    public PullDownLoadView(Context context) {
        super(context);
    }

    public PullDownLoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.PullDownLoadLayout);
        try {
            mLoadDown = array.getBoolean(R.styleable.PullDownLoadLayout_loadDown, true);
            mLoadPull = array.getBoolean(R.styleable.PullDownLoadLayout_loadPull, true);
        } finally {
            array.recycle();
        }

        // 实例化布局填充器
        inflater = LayoutInflater.from(context);
        // 实例化Scroller
        LayoutScroller = new Scroller(context);
        // 最小有效滑动距离
        MinEffectiveScroll = (int) (context.getResources().getDimension(R.dimen.pdl_effective_scroll));
    }

    public PullDownLoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        lastChildIndex = getChildCount() - 1;

        // 添加上拉刷新部分
        if (mLoadDown)
            addHeader();

        // 添加下拉加载部分
        if (mLoadPull)
            addFooter();
    }

    private void addFooter() {
        // 通过LayoutInflater获取从布局文件中获取footer的view对象
        LayoutFooter = (RelativeLayout) inflater.inflate(R.layout.pdl_footer, null);
        LayoutFooter.setBackgroundColor(Color.WHITE);

        // 上拉提示
        pdl_tv_down = (TextView) LayoutFooter.findViewById(R.id.pdl_tv_down);
        // 上拉loading动画
        pdl_elv_down = (EatBeanLoadingView) LayoutFooter.findViewById(R.id.pdl_elv_down);
        // 设置布局参数(宽度为MATCH_PARENT,高度为MATCH_PARENT)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        // 将footer添加进Layout当中
        addView(LayoutFooter, params);
    }

    /**
     * 添加头部布局
     */
    private void addHeader() {
        // 通过LayoutInflater获取从布局文件中获取header的view对象
        LayoutHeader = (RelativeLayout) inflater.inflate(R.layout.pdl_header, null);
        LayoutHeader.setBackgroundColor(Color.WHITE);

        // 获取上拉刷新的文字描述
        pdl_tv_pull = (TextView) LayoutHeader.findViewById(R.id.pdl_tv_pull);
        // 获取上拉刷新的loading-view
        pdl_glv_pull = (GhostLoadingView) LayoutHeader.findViewById(R.id.pdl_glv_pull);
        // 设置布局参数(宽度为MATCH_PARENT,高度为MATCH_PARENT)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        // 将Header添加进Layout当中
        addView(LayoutHeader, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 遍历进行子视图的测量工作
        for (int i = 0; i < getChildCount(); i++) {
            // 通知子视图进行测量
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        if (MoveDistanceBtn == 0)
            MoveDistanceBtn = (pdl_tv_down.getMeasuredHeight() / 5) * 6;
        if (MoveDistanceTop == 0)
            MoveDistanceTop = (pdl_tv_pull.getMeasuredHeight() / 5) * 6;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 重置(避免重复累加)
        LayoutContentHeight = 0;

        // 遍历进行子视图的置位工作
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child == LayoutHeader) { // 头视图隐藏
                child.layout(0, 0 - child.getMeasuredHeight(), child.getMeasuredWidth(), 0);
            } else if (child == LayoutFooter) { // 尾视图隐藏在ViewGroup所有内容视图之后
                child.layout(0, LayoutContentHeight, child.getMeasuredWidth(), LayoutContentHeight + child.getMeasuredHeight());
            } else { // 内容视图根据定义(插入)顺序,按由上到下的顺序在垂直方向进行排列
                child.layout(0, LayoutContentHeight, child.getMeasuredWidth(), LayoutContentHeight + child.getMeasuredHeight());
                if (index <= lastChildIndex) {
                    if (child instanceof ScrollView) {
                        LayoutContentHeight += getMeasuredHeight();
                        continue;
                    }
                    LayoutContentHeight += child.getMeasuredHeight();
                }
            }
        }
        // 计算到达内容最底部时ViewGroup的滑动距离
        ReachBottomScroll = LayoutContentHeight - getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // 计算本次滑动的Y轴增量(距离)
                int dy = LastYMoved - y;
                //计算延迟效果比例
                int move_factor = (Math.abs(getScrollY()) / 200 + 1);
                // 如果滑动增量小于0，即下拉操作
//                Log.e("dy===>", dy + "---" + y + "---" + move_factor + "---" + getScrollY());
                if (dy < 0) {
                    if (mLoadDown) {
                        // 如果下拉的距离小于mLayoutHeader的高度,则允许滑动
                        if (getScrollY() > 0 || Math.abs(getScrollY()) <= getMoveDistanceTop()) {
                            if (status != TRY_LOAD_MORE && status != LOAD_MORE) {
                                scrollBy(0, dy / move_factor);//最终偏移量
                                if (status != REFRESH) {
                                    if (getScrollY() <= 0) {
                                        if (status != TRY_REFRESH)
                                            updateStatus(TRY_REFRESH);

                                        if (Math.abs(getScrollY()) > MinEffectiveScroll)
                                            updateStatus(REFRESH);
                                    }
                                }
                            } else {
                                if (getScrollY() > 0) {
                                    dy = dy > 30 ? 30 : dy;
                                    scrollBy(0, dy / move_factor);
                                    if (getScrollY() < ReachBottomScroll + MinEffectiveScroll) {
                                        updateStatus(TRY_LOAD_MORE);
                                    }
                                }
                            }
                        }
                    }
                } else if (dy > 0) {
                    if (mLoadPull) {
                        if (getScrollY() <= ReachBottomScroll + getMoveDistanceBtn()) {
                            // 进行Y轴上的滑动
                            if (status != TRY_REFRESH && status != REFRESH) {
                                scrollBy(0, dy / move_factor);
                                if (status != LOAD_MORE) {
                                    if (getScrollY() >= ReachBottomScroll) {
                                        if (status != TRY_LOAD_MORE)
                                            updateStatus(TRY_LOAD_MORE);

                                        if (getScrollY() >= ReachBottomScroll + MinEffectiveScroll)
                                            updateStatus(LOAD_MORE);
                                    }
                                }
                            } else {
                                if (getScrollY() <= 0) {
                                    dy = dy > 30 ? 30 : dy;
                                    scrollBy(0, dy / move_factor);
                                    if (Math.abs(getScrollY()) < MinEffectiveScroll)
                                        updateStatus(TRY_REFRESH);
                                }
                            }
                        }
                    }
                }
                // 记录y坐标
                LastYMoved = y;
                break;
            case MotionEvent.ACTION_UP:
                // 判断本次触摸系列事件结束时,Layout的状态
                switch (status) {
                    case NORMAL: {
                        upWithStatusNormal();
                        break;
                    }

                    case TRY_REFRESH: {
                        upWithStatusTryRefresh();
                        break;
                    }

                    case REFRESH: {
                        upWithStatusRefresh();
                        break;
                    }

                    case TRY_LOAD_MORE: {
                        upWithStatusTryLoadMore();
                        break;
                    }

                    case LOAD_MORE: {
                        upWithStatusLoadMore();
                        break;
                    }
                }

        }
        LastYIntercept = 0;
        return true;
    }

    private void updateStatus(int status) {
        switch (status) {
            case NORMAL:
                break;
            case TRY_REFRESH: {
                this.status = TRY_REFRESH;
                break;
            }
            case REFRESH: {
                this.status = REFRESH;
                pdl_tv_pull.setText(R.string.release_to_pull);
                break;
            }
            case TRY_LOAD_MORE: {
                this.status = TRY_LOAD_MORE;
                break;
            }
            case LOAD_MORE:
                this.status = LOAD_MORE;
                pdl_tv_down.setText(R.string.release_to_down);
                break;
        }
    }

    private void upWithStatusNormal() {

    }

    private void upWithStatusTryRefresh() {
        // 取消本次的滑动
        LayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
        pdl_tv_pull.setText(R.string.continue_to_pull);
        status = NORMAL;
    }

    private void upWithStatusRefresh() {
        LayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - (-MinEffectiveScroll)), SCROLL_SPEED);
        pdl_tv_pull.setVisibility(View.GONE);
        pdl_glv_pull.setVisibility(View.VISIBLE);
        pdl_glv_pull.startAnim();
        // 通过Listener接口执行刷新时的监听事件
        if (MoveListener != null)
            MoveListener.onRefresh();
    }

    private void upWithStatusTryLoadMore() {
        LayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - ReachBottomScroll), SCROLL_SPEED);
        pdl_tv_down.setText(R.string.continue_to_down);
        status = NORMAL;
    }

    private void upWithStatusLoadMore() {
        LayoutScroller.startScroll(0, getScrollY(), 0, -((getScrollY() - MinEffectiveScroll) - ReachBottomScroll), SCROLL_SPEED);
        pdl_tv_down.setVisibility(View.GONE);
        pdl_elv_down.setVisibility(View.VISIBLE);
        pdl_elv_down.startAnim();
        // 通过Listener接口执行加载时的监听事件
        if (MoveListener != null)
            MoveListener.onLoadMore();
    }

    public void setOnRefreshListener(onRefreshListener listener) {
        MoveListener = listener;
    }

    public interface onRefreshListener {
        void onRefresh();

        void onLoadMore();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        // 记录此次触摸事件的y坐标
        int y = (int) ev.getY();
        // 判断触摸事件类型
        switch (ev.getAction()) {
            // Down事件
            case MotionEvent.ACTION_DOWN: {
                // 记录下本次系列触摸事件的起始点Y坐标
                LastYMoved = y;
                // 不拦截ACTION_DOWN，因为当ACTION_DOWN被拦截，后续所有触摸事件都会被拦截
                intercept = false;
                break;
            }
            // Move事件
            case MotionEvent.ACTION_MOVE: {
                if (y > LastYIntercept) { // 下滑操作
                    // 获取最顶部的子视图
                    View child = getChildAt(0);
                    if (child instanceof AdapterView) {
                        intercept = avPullDownIntercept(child);
                    } else if (child instanceof ScrollView) {
                        intercept = svPullDownIntercept(child);
                    } else if (child instanceof RecyclerView) {
                        intercept = rvPullDownIntercept(child);
                    }
                } else if (y < LastYIntercept) { // 上拉操作
                    // 获取最底部的子视图
                    View child = getChildAt(lastChildIndex);
                    if (child instanceof AdapterView) {
                        intercept = avPullUpIntercept(child);
                    } else if (child instanceof ScrollView) {
                        intercept = svPullUpIntercept(child);
                    } else if (child instanceof RecyclerView) {
                        intercept = rvPullUpIntercept(child);
                    }
                } else {
                    intercept = false;
                }
                break;
            }
            // Up事件
            case MotionEvent.ACTION_UP: {
                intercept = false;
                break;
            }
        }

        LastYIntercept = y;
        return intercept;
    }

    private boolean avPullDownIntercept(View child) {
        boolean intercept = true;
        AdapterView adapterChild = (AdapterView) child;
        // 判断AbsListView是否已经到达内容最顶部
        if (adapterChild.getFirstVisiblePosition() != 0
                || adapterChild.getChildAt(0).getTop() != 0) {
            // 如果没有达到最顶端，则仍然将事件下放
            intercept = false;
        }
        return intercept;
    }

    private boolean avPullUpIntercept(View child) {
        boolean intercept = false;
        AdapterView adapterChild = (AdapterView) child;

        // 判断AbsListView是否已经到达内容最底部
        if (adapterChild.getLastVisiblePosition() == adapterChild.getCount() - 1
                && (adapterChild.getChildAt(adapterChild.getChildCount() - 1).getBottom() == getMeasuredHeight())) {
            // 如果到达底部，则拦截事件
            intercept = true;
        }
        return intercept;
    }

    private boolean svPullDownIntercept(View child) {
        boolean intercept = false;
        if (child.getScrollY() <= 0) {
            intercept = true;
        }
        return intercept;
    }

    private boolean svPullUpIntercept(View child) {
        boolean intercept = false;
        ScrollView scrollView = (ScrollView) child;
        View scrollChild = scrollView.getChildAt(0);

        if (scrollView.getScrollY() >= (scrollChild.getHeight() - scrollView.getHeight())) {
            intercept = true;
        }
        return intercept;
    }

    private boolean rvPullDownIntercept(View child) {
        boolean intercept = false;

        RecyclerView recyclerChild = (RecyclerView) child;
        if (recyclerChild.computeVerticalScrollOffset() <= 0)
            intercept = true;

        return intercept;
    }

    private boolean rvPullUpIntercept(View child) {
        boolean intercept = false;

        RecyclerView recyclerChild = (RecyclerView) child;
        if (recyclerChild.computeVerticalScrollExtent() + recyclerChild.computeVerticalScrollOffset()
                >= recyclerChild.computeVerticalScrollRange())
            intercept = true;

        return intercept;
    }

    /**
     * 回执
     */
    public void resetLayoutLocation() {
        status = NORMAL;
        scrollTo(0, 0);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (LayoutScroller.computeScrollOffset()) {
            scrollTo(0, LayoutScroller.getCurrY());
        }
        postInvalidate();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_REFRESH: {
                    LayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
                    pdl_tv_pull.setText(R.string.continue_to_pull);
                    pdl_tv_pull.setVisibility(View.VISIBLE);
                    pdl_glv_pull.stopAnim();
                    pdl_glv_pull.setVisibility(View.GONE);
                    status = NORMAL;
                    break;
                }

                case STOP_LOAD_MORE: {
                    LayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - ReachBottomScroll), SCROLL_SPEED);
                    pdl_tv_down.setText(R.string.continue_to_down);
                    pdl_tv_down.setVisibility(View.VISIBLE);
                    pdl_elv_down.stopAnim();
                    pdl_elv_down.setVisibility(View.GONE);
                    status = NORMAL;
                    break;
                }
            }
        }
    };

    public void stopRefresh() {
        Message msg = handler.obtainMessage(STOP_REFRESH);
        handler.sendMessage(msg);
    }

    public void stopLoadMore() {
        Message msg = handler.obtainMessage(STOP_LOAD_MORE);
        handler.sendMessage(msg);
    }

    public int getMoveDistanceBtn() {
        if (MoveDistanceBtn == MoveDistanceAll) {
            return LayoutHeader.getMeasuredHeight();
        } else if (MoveDistanceBtn == MoveDistanceIn) {
            return LayoutHeader.getMeasuredHeight() / 2;
        }
        return MoveDistanceBtn;
    }

    /**
     * 设置底部可滑动到距离
     *
     * @param moveDistanceBtn
     */
    public void setMoveDistanceBtn(int moveDistanceBtn) {
        MoveDistanceBtn = moveDistanceBtn;
    }

    public int getMoveDistanceTop() {
        if (MoveDistanceTop == MoveDistanceAll) {
            return LayoutFooter.getMeasuredHeight();
        } else if (MoveDistanceTop == MoveDistanceIn) {
            return LayoutFooter.getMeasuredHeight() / 2;
        }
        return MoveDistanceTop;
    }

    /**
     * 设置头部可滑动到距离
     *
     * @param moveDistanceTop
     */
    public void setMoveDistanceTop(int moveDistanceTop) {
        MoveDistanceTop = moveDistanceTop;
    }
}
