package com.angcyo.uiview.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.bumptech.glide.request.GenericRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：根据图片的数量, 自动排列.
 * 1张图片时,文本在左边, 图片在右边. 2或3张图片时, 文本在上面, 图片在下面.最多显示3张图片
 * <p>
 * 创建人员：Robi
 * 创建时间：2017/03/27 14:51
 * 修改人员：Robi
 * 修改时间：2017/03/27 14:51
 * 修改备注：
 * Version: 1.0.0
 */
public class RTextImageLayout extends ViewGroup {

    public static final int MAX_IMAGE_SIZE = 3;
    int space = 6;//dp, 间隙
    int textSpace = 10;//文本与图片之间的空隙
    ConfigCallback mConfigCallback;
    private TextView mTextView;
    private List<ImageView> mImageViews = new ArrayList<>();
    private List<String> mImages;
    private boolean isAttachedToWindow;

    public RTextImageLayout(Context context) {
        super(context);
        initLayout();
    }

    public RTextImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    private void initLayout() {
        float density = getResources().getDisplayMetrics().density;
        space = (int) (density * space);
        textSpace = (int) (density * textSpace);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width_UNSPECIFIED = MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED);
        int height_UNSPECIFIED = MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED);
        if (mImageViews.isEmpty()) {
            if (mTextView == null) {
                setMeasuredDimension(width, getPaddingTop() + getPaddingBottom());
            } else {
                mTextView.measure(width_UNSPECIFIED, height_UNSPECIFIED);
                setMeasuredDimension(width, getPaddingTop() + getPaddingBottom() + mTextView.getMeasuredHeight());
            }
        } else {
            int[] imageSize = null;
            if (mConfigCallback != null) {
                imageSize = mConfigCallback.getImageSize(0);
            }
            if (imageSize == null) {
                int size = MeasureSpec.makeMeasureSpec((width - space * 2) / 3 - getPaddingLeft() + getPaddingRight(), MeasureSpec.EXACTLY);
                imageSize = new int[]{size, size};
            }

            ImageView imageView = null;
            for (int i = 0; i < mImageViews.size(); i++) {
                imageView = mImageViews.get(i);
                imageView.measure(imageSize[0], imageSize[1]);
            }

            if (mImageViews.size() == 1) {
                if (mTextView == null) {
                    setMeasuredDimension(width, getPaddingTop() + getPaddingBottom() + imageView.getMeasuredHeight());
                } else {
                    mTextView.measure(MeasureSpec.makeMeasureSpec(width - imageView.getMeasuredWidth() - textSpace - getPaddingLeft() - getPaddingRight(),
                            MeasureSpec.EXACTLY), heightMeasureSpec);
                    setMeasuredDimension(width, getPaddingTop() + getPaddingBottom() + Math.max(mTextView.getMeasuredHeight(), imageView.getMeasuredHeight()));
                }
            } else if (mImageViews.size() > 1) {
                if (mTextView == null) {
                    setMeasuredDimension(width, getPaddingTop() + getPaddingBottom() + imageView.getMeasuredHeight());
                } else {
                    mTextView.measure(width_UNSPECIFIED, height_UNSPECIFIED);
                    setMeasuredDimension(width, getPaddingTop() + getPaddingBottom() + mTextView.getMeasuredHeight() + imageView.getMeasuredHeight());
                }
            } else {
                setMeasuredDimension(width, getPaddingTop() + getPaddingBottom());
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        l += getPaddingLeft();
        t += getPaddingTop();
        r -= getPaddingRight();
        b -= getPaddingBottom();

        if (mImageViews.isEmpty()) {
            if (mTextView != null) {
                mTextView.layout(l, t, r, b);
            }
        } else {

            if (mImageViews.size() == 1) {
                if (mTextView != null) {
                    mTextView.layout(l, t, l + mTextView.getMeasuredWidth(), t + mTextView.getMeasuredHeight());
                }
                ImageView imageView = mImageViews.get(0);
                imageView.layout(r - imageView.getMeasuredWidth(), t, r, t + imageView.getMeasuredHeight());

                //displayImage(mImageViews.get(0), mImages.get(0));
            } else if (mImageViews.size() > 1) {
                int offsetTop = 0;
                if (mTextView != null) {
                    offsetTop = mTextView.getMeasuredHeight();
                    mTextView.layout(l, t, r, t + offsetTop);
                }

                ImageView imageView;
                for (int i = 0; i < Math.min(mImageViews.size(), MAX_IMAGE_SIZE); i++) {
                    imageView = mImageViews.get(i);
                    int measuredWidth = imageView.getMeasuredWidth();
                    int left = l + i * (measuredWidth + space);
                    int top = t + offsetTop + textSpace;
                    imageView.layout(left, top, left + measuredWidth, top + imageView.getMeasuredHeight());

                    //displayImage(imageView, mImages.get(i));
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        notifyLoadImage();
    }

    private void notifyLoadImage() {
        ImageView imageView;
        for (int i = 0; i < Math.min(mImageViews.size(), MAX_IMAGE_SIZE); i++) {
            imageView = mImageViews.get(i);
            //cancelRequest(imageView);

            String url = mImages.get(i);
            imageView.setTag(R.id.tag_url, url);
            displayImage(imageView, url);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;

        for (View view : mImageViews) {
            cancelRequest(view);
        }
    }

    private void cancelRequest(View view) {
        if (view == null) {
            return;
        }
        Object tag = view.getTag();
        if (tag instanceof GenericRequest) {
            ((GenericRequest) tag).clear();
            L.d("onDetachedFromWindow() -> " + this.getClass().getSimpleName() + " GenericRequest Clear");
        }
    }

    /**
     * 设置配置
     */
    public void setConfigCallback(ConfigCallback configCallback) {
        mConfigCallback = configCallback;
    }

    /**
     * 设置文本
     */
    public void setText(String text) {
        ensureTextView();
        mTextView.setText(text);
        if (TextUtils.isEmpty(text)) {
            removeView(mTextView);
            mTextView = null;
        }
    }

    public void setImage(String image) {
        List<String> list = new ArrayList<>();
        list.add(image);
        setImages(list);
    }

    /**
     * 设置图片
     */
    public void setImages(List<String> images) {
        mImages = images;
        int imageSize = mImages.size();
        int imageViewSize = mImageViews.size();

        for (int i = imageViewSize - 1; i >= imageSize; i--) {
            removeView(mImageViews.remove(i));
        }

        //最大显示3张图片
        for (int i = mImageViews.size(); i < Math.min(MAX_IMAGE_SIZE, imageSize); i++) {
            ImageView imageView = createImageView();
            addViewInLayout(imageView, i, new LayoutParams(-2, -2));
            mImageViews.add(imageView);
        }

        int newImageViewSize = mImageViews.size();

        if (newImageViewSize <= 0) {
            return;
        }

        if (imageViewSize == newImageViewSize ||
                (imageViewSize >= MAX_IMAGE_SIZE && newImageViewSize >= MAX_IMAGE_SIZE)) {
            notifyLoadImage();
        } else {
            requestLayout();
        }
    }

    private void displayImage(ImageView imageView, String url) {
        if (mConfigCallback != null) {
            mConfigCallback.displayImage(imageView, url);
        }
    }

    private void ensureTextView() {
        if (mTextView == null) {
            mTextView = new TextView(getContext());
            addView(mTextView, new LayoutParams(-2, -2));
            if (mConfigCallback != null) {
                mConfigCallback.onCreateTextView(mTextView);
            }
        }
    }

    private ImageView createImageView() {
        ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (mConfigCallback != null) {
            mConfigCallback.onCreateImageView(imageView);
        }
        return imageView;
    }

    public interface ConfigCallback {
        int[] getImageSize(int position);

        void onCreateImageView(ImageView imageView);

        void onCreateTextView(TextView textView);

        void displayImage(ImageView imageView, String url);
    }
}
