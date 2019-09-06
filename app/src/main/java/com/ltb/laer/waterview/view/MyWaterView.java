package com.ltb.laer.waterview.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ltb.laer.waterview.R;
import com.ltb.laer.waterview.listener.WaterClickListener;
import com.ltb.laer.waterview.model.Water;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static android.animation.ValueAnimator.INFINITE;


/**
 * 描述:  蚂蚁森林模拟
 * 处理思路：
 * ->将森林水滴作为一个总体而不是单个的view，自定义一个ViewGroup容器
 * ->循环创建view
 * ->为view随机设置位置(在一些固定的集合中随机选取，尽量保证水滴不重合)
 * ->为view设置一个初始的运动方向（注：由于每个view的运动方向不同，所以我选择将方向绑定到view的tag中）
 * ->为view设置一个初始的速度（同理：将初始速度绑定到view的tag中）
 * ->添加view到容器中，并缩放伴随透明度显示
 * ->开启handler达到view上下位移动画（注意点：这里我们需要定一个临界值来改变view的速度，到达view时而快时而慢的目的）
 * ->点击view后，缩放、透明度伴随位移移除水滴
 * ->界面销毁时停止调用handler避免内存泄漏，空指针等异常
 */
public class MyWaterView extends FrameLayout {

    private Random mRandom = new Random();
    private LayoutInflater mInflater;
    private int mChildViewRes = R.layout.water_item;
    private List<Integer> mChildViewResList = new ArrayList<>();
    //    private List<View> mViewList = new ArrayList<>();
    private List<Animator> mViewAnimatorList = new ArrayList<>();
    private int maxX, maxY;//子view的x坐标和y坐标的最大取值
    private Object[][] locationXY = new Object[][]{{0.41f, 0.01f}, {0.1f, 0.11f}, {0.21f, 0.01f}, {0.31f, 0.01f}, {0.51f, 0.01f}, {0.61f, 0.01f}, {0.71f, 0.01f}, {0.81f, 0.01f}, {0.81f, 0.11f}, {0.41f, 0.41f}};
    private List<Integer> durationList = Arrays.asList(1500, 1600, 1800, 2000, 2300, 2500);
    private WaterClickListener mWaterClickListener = null;//水滴点击监听
    private Animation animationDis = null;
    private boolean palyAcq;
    private boolean isUp = mRandom.nextBoolean();

    public MyWaterView(@NonNull Context context) {
        super(context);
    }

    public MyWaterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(getContext());
    }

    public MyWaterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyWaterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxX = w;
        maxY = h;
    }

    /**
     * 界面销毁时回调
     */
    @Override
    protected void onDetachedFromWindow() {
        onDestroy();
        super.onDetachedFromWindow();
    }

    public void restartAnim() {
        for (Animator animator : mViewAnimatorList) {
            animator.start();
        }
    }

    public void onDestroy() {
        for (Animator animator : mViewAnimatorList) {
            animator.cancel();
        }
    }

    /**
     * 设置水滴
     *
     * @param waters
     */
    public MyWaterView setWaters(final List<Water> waters) {
        if (waters == null || waters.isEmpty()) {
            return this;
        }
        //确保初始化完成
        post(new Runnable() {
            @Override
            public void run() {
                setDates(waters);
            }
        });
        return this;
    }

    private void setDates(List<Water> waters) {
        addWaterView(waters);
    }

    /**
     * 添加水滴view
     */
    private void addWaterView(List<Water> waters) {
        for (int i = 0; i < waters.size(); i++) {
            final Water water = waters.get(i);
            View view;
            if (null == mChildViewResList || 0 == mChildViewResList.size()) {
                view = mInflater.inflate(mChildViewRes, this, false);
            } else {
                view = mInflater.inflate(i < mChildViewResList.size() ? mChildViewResList.get(i) : mChildViewRes, this, false);
            }
            TextView tvWater = view.findViewById(R.id.tv_water);
            view.setTag(water);
            tvWater.setText(water.getNumber() + "g");
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    handViewClick(view);
                }
            });
            //随机设置view动画的方向
            view.setTag(R.string.isUp, mRandom.nextBoolean());
            setChildViewLocation(view, i);

            addShowViewAnimation(view);
        }
    }

    private void addShowViewAnimation(View view) {
        addView(view);
        view.setAlpha(0);
        view.setScaleX(0);
        view.setScaleY(0);
        view.animate().alpha(1).scaleX(1).scaleY(1).setDuration(500).start();

        startAnimate(view);
    }

    private void startAnimate(View view) {
        if (view.getAnimation() != null) {
            return;
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", view.getY(), isUp ? view.getY() + 10 : view.getY() - 10, view.getY(), isUp ? view.getY() - 10 : view.getY() + 10, view.getY());
        animator.setDuration(durationList.get(mRandom.nextInt(durationList.size())));
        animator.setInterpolator(new LinearInterpolator());
        // 设置插值器
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(INFINITE);
        animator.start();
//        mViewList.add(view);
        mViewAnimatorList.add(animator);
    }

    private void setChildViewLocation(View view, int i) {
        if (i > 9) {
            return;
        }
        view.setX((maxX * (float) locationXY[i][0]));
        view.setY((maxY * (float) locationXY[i][1]));
    }

    private void handViewClick(View view) {
        Object tag = view.getTag();
        if (tag instanceof Water && null != mWaterClickListener) {
            Water waterTag = (Water) tag;
            mWaterClickListener.onWaterClick(waterTag);
        }
        if (animationDis == null) {
            disAnimate(view);
        } else {
            view.setAnimation(animationDis);
            if (palyAcq) {
                disAnimate(view);
            }
        }
    }

    private void disAnimate(View view) {
        view.animate()
//                .translationY(viewDisappearY).translationX(viewDisappearX)
                .alpha(0).scaleX(0).scaleY(0).setDuration(1000).start();
//        mViewList.remove(view);
    }

    /**
     * 设置自定义水滴布局 必须含有id为tv_water的TextView
     *
     * @param childViewLayout
     * @return
     */
    public MyWaterView setChildView(int childViewLayout) {
        this.mChildViewRes = childViewLayout;
        return this;
    }

    /**
     * 设置自定义水滴布局list 必须含有id为tv_water的TextView
     *
     * @param childViewLayout
     * @return
     */
    public MyWaterView setChildView(List<Integer> childViewLayout) {
        this.mChildViewResList = childViewLayout;
        return this;
    }

    /**
     * 设置水滴监听
     *
     * @param waterClickListener
     */
    public MyWaterView setCallBack(WaterClickListener waterClickListener) {
        mWaterClickListener = waterClickListener;
        return this;
    }

    /**
     * 设置水滴消失动画
     *
     * @param animate
     * @return
     */
    public MyWaterView setWaterDisAnimate(Animation animate, boolean playAcq) {
        this.animationDis = animate;
        this.palyAcq = playAcq;
        return this;
    }


}