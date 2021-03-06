package com.angcyo.uiview.recycler.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.angcyo.uiview.R;
import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.recycler.widget.ILoadMore;
import com.angcyo.uiview.recycler.widget.IShowState;
import com.angcyo.uiview.recycler.widget.ItemShowStateLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angcyo on 16-01-18-018.
 */
public abstract class RBaseAdapter<T> extends RecyclerView.Adapter<RBaseViewHolder>
        implements RecyclerView.OnChildAttachStateChangeListener {

    public static final int ITEM_TYPE_LOAD_MORE = 666;
    public static final int ITEM_TYPE_SHOW_STATE = 667;
    protected List<T> mAllDatas;
    protected Context mContext;
    /**
     * 是否激活加载更多
     */
    protected boolean mEnableLoadMore = false;
    protected ILoadMore mLoadMore;
    protected OnAdapterLoadMoreListener mLoadMoreListener;
    /**
     * 是否激活布局状态显示, 可以在Item中显示,空布局, 无网络布局, 加载中布局,和错误布局
     */
    protected boolean mEnableShowState = true;
    protected IShowState mIShowState;
    /**
     * 当前加载状态
     */
    int mLoadState = ILoadMore.NORMAL;
    /**
     * 当前显示的状态
     */
    int mShowState = IShowState.NORMAL;
    /**
     * 切换显示状态, 是否执行动画
     */
    boolean animToShowState = false;


    public RBaseAdapter(Context context) {
        mAllDatas = new ArrayList<>();
        this.mContext = context;
    }

    public RBaseAdapter(Context context, List<T> datas) {
        this.mAllDatas = datas == null ? new ArrayList<T>() : datas;
        this.mContext = context;
    }

    public static int getListSize(List list) {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public RBaseAdapter setOnLoadMoreListener(OnAdapterLoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
        return this;
    }

    /**
     * 返回是否激活加载更多
     */
    public boolean isEnableLoadMore() {
        return mEnableLoadMore;
    }

    //--------------标准的方法-------------//

    /**
     * 启用加载更多功能
     */
    public void setEnableLoadMore(boolean enableLoadMore) {
        boolean loadMore = mEnableLoadMore;
        mEnableLoadMore = enableLoadMore;

        if (isStateLayout()) {
            return;
        }

        if (enableLoadMore && !loadMore) {
            notifyItemInserted(getLastPosition());
        } else if (!enableLoadMore && loadMore) {
            notifyItemRemoved(getLastPosition());
        }
    }

    @Override
    final public int getItemViewType(int position) {
        if (isStateLayout()) {
            return ITEM_TYPE_SHOW_STATE;
        }
        if (mEnableLoadMore && isLast(position)) {
            return ITEM_TYPE_LOAD_MORE;
        }
        return getItemType(position);
    }

    //是否该显示状态布局
    protected boolean isStateLayout() {
        return mEnableShowState && mShowState != IShowState.NORMAL;
    }

    @Override
    public RBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (mEnableShowState && viewType == ITEM_TYPE_SHOW_STATE) {
            itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.base_item_show_state_layout, parent, false);
            mIShowState = (IShowState) itemView;
        } else if (mEnableLoadMore && viewType == ITEM_TYPE_LOAD_MORE) {
            itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.base_item_load_more_layout, parent, false);
            mLoadMore = (ILoadMore) itemView;
        } else {
            int itemLayoutId = getItemLayoutId(viewType);
            if (itemLayoutId == 0) {
                itemView = createContentView(parent, viewType);
            } else {
                itemView = LayoutInflater.from(mContext).inflate(itemLayoutId, parent, false);
            }
        }
//        if (itemView == null) {
//            return createItemViewHolder(parent, viewType);
//        }
        return createBaseViewHolder(viewType, itemView);
    }

    @NonNull
    protected RBaseViewHolder createBaseViewHolder(int viewType, View itemView) {
        return new RBaseViewHolder(itemView, viewType);
    }

//    /**用来实现...*/
//    @NonNull
//    protected RBaseViewHolder createItemViewHolder(ViewGroup parent, int viewType) {
//        return null;
//    }

    @Override
    final public void onBindViewHolder(RBaseViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(RBaseViewHolder holder, int position) {
//        L.e("call: onBindViewHolder([holder, position])-> " + position);
        if (isStateLayout()) {
            if (mIShowState != null) {
                mIShowState.setShowState(mShowState);
            }
        } else if (mEnableLoadMore && isLast(position)) {
            /**如果第一个就是加载更多的布局, 需要调用加载更多么?*/
            onBindLoadMore(position);
        } else {
            onBindView(holder, position, mAllDatas.size() > position ? mAllDatas.get(position) : null);
        }
    }

    @Override
    public void onViewAttachedToWindow(RBaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
//        L.w("onViewAttachedToWindow");
    }

    @Override
    public void onViewDetachedFromWindow(RBaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        L.w("onViewDetachedFromWindow");
    }

    private void onBindLoadMore(int position) {
        if (mLoadState == ILoadMore.NORMAL
                || mLoadState == ILoadMore.LOAD_ERROR) {

            /**如果第一个就是加载更多的布局, 需要调用加载更多么?*/
            if (position != 0) {
                mLoadState = ILoadMore.LOAD_MORE;
                onLoadMore();
                if (mLoadMoreListener != null) {
                    mLoadMoreListener.onAdapterLodeMore(this);
                }
            }
        }

        updateLoadMoreView();
    }

    private void updateLoadMoreView() {
        if (mLoadMore != null) {
            mLoadMore.setLoadState(mLoadState);
        }
    }

    /**
     * 重写此方法, 实现加载更多功能
     */
    protected void onLoadMore() {

    }

    /**
     * 结束加载更多的标识, 方便下一次回调
     */
    public void setLoadMoreEnd() {
        mLoadState = ILoadMore.NORMAL;
        setEnableLoadMore(true);
        updateLoadMoreView();
    }

    public void setLoadError() {
        mLoadState = ILoadMore.LOAD_ERROR;
        setEnableLoadMore(true);
        updateLoadMoreView();
    }

    public void setNoMore() {
        setNoMore(false);
    }

    public void setNoMore(boolean refresh) {
        mLoadState = ILoadMore.NO_MORE;
        setEnableLoadMore(true);
        if (refresh) {
            updateLoadMoreView();//不需要及时刷新
        }
    }

    public boolean isLast(int position) {
        return position == getLastPosition();
    }

    private int getLastPosition() {
        return getItemCount() - 1;
    }

    /**
     * 根据position返回Item的类型.
     */
    public int getItemType(int position) {
        return 0;
    }

    //--------------需要实现的方法------------//

    @Override
    public int getItemCount() {
        if (isStateLayout()) {
            return 1;
        }

        int size = mAllDatas == null ? 0 : mAllDatas.size();
        if (mEnableLoadMore) {
            size += 1;
        }
        return size;
    }

    /**
     * 当 {@link #getItemLayoutId(int)} 返回0的时候, 会调用此方法
     */
    protected View createContentView(ViewGroup parent, int viewType) {
        return null;
    }

    protected abstract int getItemLayoutId(int viewType);

    //---------------滚动事件的处理--------------------//

    protected abstract void onBindView(RBaseViewHolder holder, int position, T bean);

    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    }

    //----------------Item 数据的操作-----------------//

    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    }

    /**
     * 在最后的位置插入数据
     */
    public void addLastItem(T bean) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        int startPosition = mAllDatas.size();
        mAllDatas.add(bean);
        notifyItemInserted(startPosition);
        notifyItemRangeChanged(startPosition, getItemCount());
    }

    /**
     * 解决九宫格添加图片后,添加按钮消失时崩溃的bug
     */
    public void addLastItemSafe(T bean) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }

        int startPosition = mAllDatas.size();
        mAllDatas.add(bean);
        int itemCount = getItemCount();
        if (itemCount > startPosition + 1) {
            notifyItemInserted(startPosition);
            notifyItemRangeChanged(startPosition, getItemCount());
        } else {
            notifyItemChanged(itemCount - 1);//
        }
    }

    public void addFirstItem(T bean) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        mAllDatas.add(0, bean);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * delete item with object
     */
    public void deleteItem(T bean) {
        if (mAllDatas != null) {
            int indexOf = mAllDatas.indexOf(bean);
            if (indexOf > -1) {
                deleteItem(indexOf);
            }
        }
    }

    public void deleteItem(int position) {
        if (mAllDatas != null) {
            int size = getItemCount();
            if (size > position) {
                if (onDeleteItem(position)) {
                    mAllDatas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, size - position);
                }
            }
        }
    }

    /**
     * 是否可以删除bean
     */
    protected boolean onDeleteItem(int position) {
        return true;
    }

    public void removeFirstItem() {
        mAllDatas.remove(0);
        notifyItemRemoved(0);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void removeLastItem() {
        int last = mAllDatas.size() - 1;
        mAllDatas.remove(last);
        notifyItemRemoved(last);
        notifyItemRangeChanged(last, getItemCount());
    }

    /**
     * 重置数据
     */
    public void resetData(List<T> datas) {
        int oldSize = getListSize(mAllDatas);
        int newSize = getListSize(datas);

        if (datas == null) {
            this.mAllDatas = new ArrayList<>();
        } else {
            this.mAllDatas = datas;
        }
        if (oldSize == newSize) {
            if (isEnableLoadMore()) {
                oldSize += 1;
            }
            notifyItemRangeChanged(0, oldSize);
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     * 追加数据
     */
    public void appendData(List<T> datas) {
        if (datas == null || datas.size() == 0) {
            return;
        }
        if (this.mAllDatas == null) {
            this.mAllDatas = new ArrayList<>();
        }
        int startPosition = this.mAllDatas.size();
        this.mAllDatas.addAll(datas);
        notifyItemRangeInserted(startPosition, datas.size());
        notifyItemRangeChanged(startPosition, getItemCount());
    }

    public List<T> getAllDatas() {
        return mAllDatas;
    }


    public void setEnableShowState(boolean enableShowState) {
        mEnableShowState = enableShowState;
    }

    /**
     * 设置布局显示状态
     *
     * @see IShowState
     */
    public void setShowState(int showState) {
        if (mShowState == showState) {
            return;
        }
        mShowState = showState;

        if (mIShowState == null || showState == IShowState.NORMAL) {
            if (mIShowState != null && mIShowState instanceof ItemShowStateLayout) {
                if (animToShowState) {
                    ((ItemShowStateLayout) mIShowState).animToHide(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    notifyDataSetChanged();
                }
            } else {
                notifyDataSetChanged();
            }
        } else {
            mIShowState.setShowState(showState);
        }
    }

    /**
     * 使用布局局部刷新
     */
    public void localRefresh(RecyclerView recyclerView, OnLocalRefresh localRefresh) {
        if (recyclerView == null || localRefresh == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            for (int i = 0; i < layoutManager.getChildCount(); i++) {
                int position = firstVisibleItemPosition + i;
                RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                if (vh != null) {
                    localRefresh.onLocalRefresh(vh, position);
                }
            }
        }
    }

    @Override
    public void onChildViewAttachedToWindow(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof RecyclerView.LayoutParams) {
            int viewAdapterPosition = ((RecyclerView.LayoutParams) layoutParams).getViewAdapterPosition();
            int viewLayoutPosition = ((RecyclerView.LayoutParams) layoutParams).getViewLayoutPosition();
            onChildViewAttachedToWindow(view, viewAdapterPosition, viewLayoutPosition);
        }
    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof RecyclerView.LayoutParams) {
            int viewAdapterPosition = ((RecyclerView.LayoutParams) layoutParams).getViewAdapterPosition();
            int viewLayoutPosition = ((RecyclerView.LayoutParams) layoutParams).getViewLayoutPosition();
            onChildViewDetachedFromWindow(view, viewAdapterPosition, viewLayoutPosition);
        }
    }

    protected void onChildViewAttachedToWindow(View view, int adapterPosition, int layoutPosition) {
        //L.v("call: onChildViewAttachedToWindow -> " + adapterPosition + " " + layoutPosition + " " + view);
    }

    protected void onChildViewDetachedFromWindow(View view, int adapterPosition, int layoutPosition) {
        //L.v("call: onChildViewDetachedFromWindow -> " + adapterPosition + " " + layoutPosition + " " + view);
    }


    public interface OnAdapterLoadMoreListener {
        void onAdapterLodeMore(RBaseAdapter baseAdapter);
    }

    public interface OnLocalRefresh {
        void onLocalRefresh(RBaseViewHolder viewHolder, int position);
    }
}
