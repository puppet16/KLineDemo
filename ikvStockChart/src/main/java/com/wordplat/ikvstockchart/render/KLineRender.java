/*
 * Copyright (C) 2017 WordPlat Open Source Project
 *
 *      https://wordplat.com/InteractiveKLineView/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wordplat.ikvstockchart.render;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.wordplat.ikvstockchart.drawing.CandleDrawing;
import com.wordplat.ikvstockchart.drawing.EmptyDataDrawing;
import com.wordplat.ikvstockchart.drawing.HighlightDrawing;
import com.wordplat.ikvstockchart.drawing.IDrawing;
import com.wordplat.ikvstockchart.drawing.KLineGridAxisDrawing;
import com.wordplat.ikvstockchart.drawing.MADrawing;
import com.wordplat.ikvstockchart.entry.EntrySet;
import com.wordplat.ikvstockchart.entry.StockIndex;
import com.wordplat.ikvstockchart.marker.IMarkerView;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>KLineRender K线图</p>
 */

public class KLineRender extends AbstractRender {
    private static final String TAG = "KLineRender";
    private static final boolean DEBUG = true;
    private static final float ZOOM_IN_FACTOR = 1.4f;//放大倍率
    private static final float ZOOM_OUT_FACTOR = 0.7f;//缩小倍率
    private static final int ZOOM_DURATION = 1000;

    private static final float LANDSCAPE_PORTRAIT_FACTOR = 1.8235294f;

    private final Context context;
    private final RectF kLineRect = new RectF(); // K 线图显示区域

    private final float[] extremumY = new float[2];
    private final float[] contentPts = new float[2];

    /**
     * 当前缩放下显示的 entry 数量
     */
    private int currentVisibleCount = -1;

    /**
     * 竖屏时各级别缩放下显示的 entry 数量，7=最多放大位数+最多缩小位数+1
     */
    private int[] portraitVisibleCountBuffer = new int[7];

    /**
     * 横屏时各级别缩放下显示的 entry 数量
     */
    private int[] landscapeVisibleCountBuffer = new int[7];

    /**
     * 用户手势控制的缩放次数。
     * 此值为 0 时，表示无缩放，为正值时，表示放大了 zoomTimes 倍，为负值时，表示缩小了 zoomTimes 倍
     * 此值受 {@link #zoomInTimes} 和 {@link #zoomOutTimes} 限制
     */
    private int zoomTimes = 0;

    /**
     * 最多放大倍数
     */
    private int zoomInTimes = 3;

    /**
     * 最多缩小倍数
     */
    private int zoomOutTimes = 3;

    private int maxZoomTimes = 0;//缩放级别总数
    /**
     * 缩放动画
     */
    private final ValueAnimator zoomAnimator = new ValueAnimator();
    private float zoomPivotX;
    private float zoomPivotY;

    private int minVisibleIndex;
    private int maxVisibleIndex;

    private final List<IDrawing> kLineDrawingList = new ArrayList<>();
    private final KLineGridAxisDrawing kLineGridAxisDrawing = new KLineGridAxisDrawing();
    private final CandleDrawing candleDrawing = new CandleDrawing();
    private final MADrawing maDrawing = new MADrawing();
    private final EmptyDataDrawing emptyDataDrawing = new EmptyDataDrawing();
    private final HighlightDrawing highlightDrawing = new HighlightDrawing();

    private final List<StockIndex> stockIndexList = new ArrayList<>(); // 股票指标列表

    public KLineRender(Context context) {
        this.context = context;

        kLineDrawingList.add(kLineGridAxisDrawing);
        kLineDrawingList.add(candleDrawing);
        kLineDrawingList.add(maDrawing);
        kLineDrawingList.add(emptyDataDrawing);
        kLineDrawingList.add(highlightDrawing);

        zoomAnimator.setDuration(ZOOM_DURATION);
        zoomAnimator.setInterpolator(new LinearInterpolator());
        zoomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int count = (int) animation.getAnimatedValue();

                zoom(kLineRect, count, zoomPivotX, zoomPivotY);
            }
        });
    }

    public void addDrawing(IDrawing drawing) {
        kLineDrawingList.add(drawing);
    }

    public void clearDrawing() {
        kLineDrawingList.clear();
    }

    public void addStockIndex(StockIndex stockIndex) {
        stockIndexList.add(stockIndex);
    }

    public void removeStockIndex(StockIndex stockIndex) {
        stockIndexList.remove(stockIndex);
    }

    public void clearStockIndex() {
        stockIndexList.clear();
    }

    public int getZoomTimes() {
        return zoomTimes;
    }

    public void setZoomTimes(int zoomTimes) {
        this.zoomTimes = zoomTimes;
    }

    public RectF getKLineRect() {
        return kLineRect;
    }

    public void addMarkerView(IMarkerView markerView) {
        highlightDrawing.addMarkerView(markerView);
    }

    @Override
    public void setEntrySet(EntrySet entrySet) {
        super.setEntrySet(entrySet);

        computeVisibleCount();

        postMatrixTouch(kLineRect.width(), currentVisibleCount);

        computeExtremumValue(extremumY, entrySet.getMinY(), entrySet.getDeltaY());
        postMatrixValue(kLineRect.width(), kLineRect.height(), extremumY[0], extremumY[1]);

        postMatrixOffset(kLineRect.left, kLineRect.top);
    }

    @Override
    public void onViewRect(RectF viewRect) {
        final float candleBottom = viewRect.bottom - sizeColor.getXLabelViewHeight();
        final int remainHeight = (int) (candleBottom - viewRect.top);

        int calculateHeight = 0;
        for (StockIndex stockIndex : stockIndexList) {
            if (stockIndex.isEnable()) {
                stockIndex.setEnable(stockIndex.getHeight() > 0
                        && calculateHeight + stockIndex.getHeight() < remainHeight);

                calculateHeight += stockIndex.getHeight();
            }
        }

        kLineRect.set(viewRect.left, viewRect.top, viewRect.right, candleBottom - calculateHeight);

        initDrawingList(kLineRect, kLineDrawingList);

        calculateHeight = 0;
        for (StockIndex stockIndex : stockIndexList) {
            if (stockIndex.isEnable()) {
                calculateHeight += stockIndex.getHeight();

                float top = kLineRect.bottom + sizeColor.getXLabelViewHeight() + calculateHeight - stockIndex.getHeight();
                float bottom = kLineRect.bottom + sizeColor.getXLabelViewHeight() + calculateHeight;

                stockIndex.setRect(
                        viewRect.left + stockIndex.getPaddingLeft(),
                        top + stockIndex.getPaddingTop(),
                        viewRect.right - stockIndex.getPaddingRight(),
                        bottom - stockIndex.getPaddingBottom());

                initDrawingList(stockIndex.getRect(), stockIndex.getDrawingList());
            }
        }
    }

    /**
     * 放大
     *
     * @param x 在点(x, y)上放大
     * @param y 在点(x, y)上放大
     */
    @Override
    public void zoomIn(float x, float y) {
        if (entrySet.getEntryList().size() == 0) {
            return;
        }
//        Log.d(TAG, "zoomIn：" + zoomTimes);
        final int visibleCount = getCurrentVisibleCount(++zoomTimes);

        if (visibleCount != -1) {
            currentVisibleCount = visibleCount;

            zoom(kLineRect, currentVisibleCount, x, y);
        } else {
            zoomTimes = zoomOutTimes;
        }
    }

    /**
     * 缩小
     *
     * @param x 在点(x, y)上缩小
     * @param y 在点(x, y)上缩小
     */
    @Override
    public void zoomOut(float x, float y) {
        if (entrySet.getEntryList().size() == 0) {
            return;
        }
        final int visibleCount = getCurrentVisibleCount(--zoomTimes);

        if (visibleCount != -1) {
            currentVisibleCount = visibleCount;

            zoom(kLineRect, currentVisibleCount, x, y);
        } else {
            zoomTimes = -zoomInTimes;
        }
    }

    @Override
    public void zoomIn(float x, float y, float scaleFactor) {
        if (entrySet.getEntryList().size() == 0) {
            return;
        }
        final int visibleCount = getZoomVisibleCount(scaleFactor);
        if (visibleCount != -1) {
            currentVisibleCount = visibleCount;
            zoom(kLineRect, currentVisibleCount, x, y);
        }
    }

    @Override
    public void zoomOut(float x, float y, float scaleFactor) {
        if (entrySet.getEntryList().size() == 0) {
            return;
        }
        final int visibleCount = getZoomVisibleCount(scaleFactor);

        if (visibleCount != -1) {
            currentVisibleCount = visibleCount;

            zoom(kLineRect, currentVisibleCount, x, y);
        }
    }

    @Override
    public void render(Canvas canvas) {
        final int count = entrySet.getEntryList().size();

        computeVisibleIndex();

        final float minY = count > 0 ? entrySet.getEntryList().get(entrySet.getMinYIndex()).getLow() : Float.NaN;
        final float maxY = count > 0 ? entrySet.getEntryList().get(entrySet.getMaxYIndex()).getHigh() : Float.NaN;
        renderDrawingList(canvas, kLineDrawingList, minY, maxY);

        for (StockIndex stockIndex : stockIndexList) {
            if (stockIndex.isEnable()) {
                float deltaY = stockIndex.getDeltaY();

                if (deltaY > 0) {
                    computeExtremumValue(extremumY,
                            stockIndex.getMinY(),
                            deltaY,
                            stockIndex.getExtremumYScale(),
                            stockIndex.getExtremumYDelta());
                    postMatrixValue(stockIndex.getMatrix(), stockIndex.getRect(), extremumY[0], extremumY[1]);

                    renderDrawingList(canvas, stockIndex.getDrawingList(), stockIndex.getMinY(), stockIndex.getMaxY());

                } else {
                    postMatrixValue(stockIndex.getMatrix(), stockIndex.getRect(), Float.NaN, Float.NaN);

                    renderDrawingList(canvas, stockIndex.getDrawingList(), Float.NaN, Float.NaN);
                }
            }
        }
    }

    private void zoomAnimate(int visibleCount, float pivotX, float pivotY) {
        zoomAnimator.setIntValues(currentVisibleCount, visibleCount);
        zoomPivotX = pivotX;
        zoomPivotY = pivotY;

        currentVisibleCount = visibleCount;

        zoomAnimator.start();
    }

    private void initDrawingList(RectF rect, List<IDrawing> drawingList) {
        for (IDrawing drawing : drawingList) {
            drawing.onInit(rect, this);
        }
    }

    private void renderDrawingList(Canvas canvas, List<IDrawing> drawingList, float minY, float maxY) {
        for (int i = minVisibleIndex; i < maxVisibleIndex; i++) {
            for (IDrawing drawing : drawingList) {
                drawing.computePoint(minVisibleIndex, maxVisibleIndex, i);
            }
        }

        for (IDrawing drawing : drawingList) {
            drawing.onComputeOver(canvas, minVisibleIndex, maxVisibleIndex, minY, maxY);
        }

        for (IDrawing drawing : drawingList) {
            drawing.onDrawOver(canvas);
        }
    }

    /**
     * 计算全部缩放条件下的 visibleCount 数值
     */
    private void computeVisibleCount() {
        zoomInTimes = Math.abs(sizeColor.getZoomInTimes() == 0 ? 3 : sizeColor.getZoomInTimes());
        zoomOutTimes = Math.abs(sizeColor.getZoomOutTimes() == 0 ? 3 : sizeColor.getZoomOutTimes());
        maxZoomTimes = zoomInTimes + zoomOutTimes + 1;

        if (portraitVisibleCountBuffer.length < maxZoomTimes) {
            portraitVisibleCountBuffer = new int[maxZoomTimes];
        }

        if (currentVisibleCount == -1) {
            currentVisibleCount = sizeColor.getPortraitDefaultVisibleCount();
            portraitVisibleCountBuffer[zoomOutTimes] = currentVisibleCount;
            //3

            //下标012，i值321
            for (int i = zoomInTimes; i > 0; i--) {
                portraitVisibleCountBuffer[zoomOutTimes - i] = getZoomOutVisibleCount(currentVisibleCount, i);
            }
            //下标654，i值321
            for (int i = zoomOutTimes; i > 0; i--) {
                portraitVisibleCountBuffer[zoomOutTimes + i] = getZoomInVisibleCount(currentVisibleCount, i);
            }
            //最后计算出来的范围从大到小，即从缩小到放大
            if (DEBUG) {
                for (int i : portraitVisibleCountBuffer) {
                    Log.d(TAG, "七级缩放范围：" + i);
                }
            }
        }

        // 横屏时应该改变显示的 entry 数量，否则蜡烛图太粗了，不好看
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (landscapeVisibleCountBuffer.length < maxZoomTimes) {
                landscapeVisibleCountBuffer = new int[maxZoomTimes];
            }

            for (int i = 0; i <= zoomOutTimes + zoomInTimes; i++) {
                landscapeVisibleCountBuffer[i] = (int) (portraitVisibleCountBuffer[i] * LANDSCAPE_PORTRAIT_FACTOR);
            }

            currentVisibleCount = getCurrentVisibleCount(zoomTimes);
        }
    }

    /**
     * 计算当前显示区域内的 X 轴范围
     */
    private void computeVisibleIndex() {
        contentPts[0] = kLineRect.left;
        contentPts[1] = 0;
        invertMapPoints(contentPts);

        minVisibleIndex = contentPts[0] <= 0 ? 0 : (int) contentPts[0];
        maxVisibleIndex = minVisibleIndex + currentVisibleCount + 1;
        if (maxVisibleIndex > entrySet.getEntryList().size()) {
            maxVisibleIndex = entrySet.getEntryList().size();
        }

        // 计算当前显示区域内 entry 在 Y 轴上的最小值和最大值
        entrySet.computeMinMax(minVisibleIndex, maxVisibleIndex, stockIndexList);

        computeExtremumValue(extremumY, entrySet.getMinY(), entrySet.getDeltaY());
        postMatrixValue(kLineRect.width(), kLineRect.height(), extremumY[0], extremumY[1]);
    }

    //判断
    private int getCurrentVisibleCount(int zoomTimes) {
        final int index = zoomOutTimes + zoomTimes;
        if (0 <= index && index <= zoomOutTimes + zoomInTimes) {
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return landscapeVisibleCountBuffer[index];
            } else {
                return portraitVisibleCountBuffer[index];
            }
        }
        return -1;
    }

    //放大可见数据
    private int getZoomInVisibleCount(int currentVisibleCount, int nZoomInTimes) {
        if (nZoomInTimes > 1) {
            return (int) (getZoomInVisibleCount(currentVisibleCount, nZoomInTimes - 1) / ZOOM_IN_FACTOR) + 1;
        } else {
            return (int) (currentVisibleCount / ZOOM_IN_FACTOR) + 1;
        }
    }

    //缩小可见数据
    private int getZoomOutVisibleCount(int currentVisibleCount, int nZoomOutTimes) {
        if (nZoomOutTimes > 1) {
            return (int) (getZoomOutVisibleCount(currentVisibleCount, nZoomOutTimes - 1) / ZOOM_OUT_FACTOR) + 1;
        } else {
            return (int) (currentVisibleCount / ZOOM_OUT_FACTOR) + 1;
        }
    }

    //可见数据,用于双指缩放
    private int getZoomVisibleCount(float scaleFactor) {
        int result;
        //当前已是最大缩放率不能再放大
        if (currentVisibleCount >= getCurrentVisibleCount(-zoomInTimes) && scaleFactor < 1) {
            return -1;
        }
        //当前已是最小缩放率不能再缩小
        if (currentVisibleCount <= getCurrentVisibleCount(zoomInTimes) && scaleFactor > 1) {
            return -1;
        }
        //缩放比率为0，这肯定有问题了返回正常级别的可见范围
        if (scaleFactor <= 0) {
            return getCurrentVisibleCount(0);
        }

        result = (int) (currentVisibleCount / scaleFactor) + 1;

        //计算后的数值大于七个级别中的最大数值则返回级别里的最大值
        if (result >= getCurrentVisibleCount(-zoomInTimes)) {
            return getCurrentVisibleCount(-zoomInTimes);
        }
        //计算后的数值小于七个级别中的最小数值则返回级别里的最小值
        if (result <= getCurrentVisibleCount(zoomInTimes)) {
            return getCurrentVisibleCount(zoomInTimes);
        }
        //设置双指缩放后的当前缩放等级，为了配合双击放大效果
        setZoomTimes(getZoomTimesByNearValue(result));
        if (DEBUG) {
            Log.d(TAG, "当前显示数量：" + currentVisibleCount + "   缩放率：" + scaleFactor + "  结果：" + result + "  接近级别：" + portraitVisibleCountBuffer[getZoomTimesByNearValue(result) + 3] + " 级别：" + zoomTimes);
        }
        return result;
    }

    //根据显示范围获取与之相近的缩放等级
    private int getZoomTimesByNearValue(int visibleCount) {
        List<Integer> dValue = new ArrayList<>();
        int[] temp;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            temp = landscapeVisibleCountBuffer.clone();
        } else {
            temp = portraitVisibleCountBuffer.clone();
        }
        for (int value : temp) {
            dValue.add(Math.abs(value - visibleCount));
        }
        int minValue = Integer.MAX_VALUE, index = -1;
        for (int i = 0; i < dValue.size(); i++) {
            if (minValue > dValue.get(i)) {
                minValue = dValue.get(i);
                index = i;
            }
        }
        if (index != -1) {
            return index - zoomInTimes;
        }
        return zoomInTimes;
    }
}
