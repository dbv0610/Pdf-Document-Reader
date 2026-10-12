/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wxiwei.office.thirdpart.achartengine

import com.wxiwei.office.thirdpart.achartengine.chart.AbstractChart
import com.wxiwei.office.thirdpart.achartengine.chart.BubbleChart
import com.wxiwei.office.thirdpart.achartengine.chart.ColumnBarChart
import com.wxiwei.office.thirdpart.achartengine.chart.CombinedXYChart
import com.wxiwei.office.thirdpart.achartengine.chart.DialChart
import com.wxiwei.office.thirdpart.achartengine.chart.DoughnutChart
import com.wxiwei.office.thirdpart.achartengine.chart.LineChart
import com.wxiwei.office.thirdpart.achartengine.chart.PieChart
import com.wxiwei.office.thirdpart.achartengine.chart.RangeBarChart
import com.wxiwei.office.thirdpart.achartengine.chart.ScatterChart
import com.wxiwei.office.thirdpart.achartengine.chart.TimeChart
import com.wxiwei.office.thirdpart.achartengine.model.CategorySeries
import com.wxiwei.office.thirdpart.achartengine.model.MultipleCategorySeries
import com.wxiwei.office.thirdpart.achartengine.model.XYMultipleSeriesDataset
import com.wxiwei.office.thirdpart.achartengine.renderers.DefaultRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.DialRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer

/**
 * Utility methods for creating chart views or intents.
 */
object ChartFactory {
    /** The key for the chart data.  */
    const val CHART: String = "chart"

    /** The key for the chart graphical activity title.  */
    const val TITLE: String = "title"

    /**
     * Creates a line chart.
     * 
     * @param dataset the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @return a line chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset and the renderer don't include the same number of
     * series
     */
    @JvmStatic
    fun getLineChart(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer
    ): AbstractChart {
        checkParameters(dataset, renderer)
        return LineChart(dataset, renderer)
    }

    /**
     * Creates a scatter chart.
     * 
     * @param dataset the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @return a scatter chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset and the renderer don't include the same number of
     * series
     */
    @JvmStatic
    fun getScatterChart(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer
    ): AbstractChart {
        checkParameters(dataset, renderer)
        return ScatterChart(dataset, renderer)
    }

    /**
     * Creates a bubble chart.
     * 
     * @param dataset the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @return a bubble chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset and the renderer don't include the same number of
     * series
     */
    fun getBubbleChart(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer
    ): AbstractChart {
        checkParameters(dataset, renderer)
        return BubbleChart(dataset, renderer)
    }

    /**
     * Creates a time chart.
     * 
     * @param dataset the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @param format the date format pattern to be used for displaying the X axis
     * date labels. If null, a default appropriate format will be used.
     * @return a time chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset and the renderer don't include the same number of
     * series
     */
    fun getTimeChart(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer, format: String?
    ): AbstractChart {
        checkParameters(dataset, renderer)
        val chart = TimeChart(dataset, renderer)
        chart.dateFormat = format
        return chart
    }

    /**
     * Creates a bar chart.
     * 
     * @param dataset the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @param type the bar chart type
     * @return a bar chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset and the renderer don't include the same number of
     * series
     */
    @JvmStatic
    fun getColumnBarChart(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer, type: ColumnBarChart.Type?
    ): AbstractChart {
        checkParameters(dataset, renderer)
        return ColumnBarChart(dataset, renderer, type)
    }

    /**
     * Creates a range bar chart.
     * 
     * @param dataset the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @param type the range bar chart type
     * @return a bar chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset and the renderer don't include the same number of
     * series
     */
    fun getRangeBarChart(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer, type: ColumnBarChart.Type?
    ): AbstractChart {
        checkParameters(dataset, renderer)
        return RangeBarChart(dataset, renderer, type)
    }

    /**
     * Creates a combined XY chart.
     * 
     * @param dataset the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @param types the chart types (cannot be null)
     * @return a combined XY chart
     * @throws IllegalArgumentException if dataset is null or renderer is null
     * or if a dataset number of items is different than the number of
     * series renderers or number of chart types
     */
    fun getCombinedXYChart(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer, types: Array<String?>
    ): AbstractChart {
        require(dataset.seriesCount == types.size) { "Dataset, renderer and types should be not null and the datasets series count should be equal to the types length" }
        checkParameters(dataset, renderer)
        return CombinedXYChart(dataset, renderer, types)
    }

    /**
     * Creates a pie chart
     * 
     * @param dataset the category series dataset (cannot be null)
     * @param renderer the series renderer (cannot be null)
     * @return a pie chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset number of items is different than the number of
     * series renderers
     */
    @JvmStatic
    fun getPieChart(
        dataset: CategorySeries,
        renderer: DefaultRenderer
    ): AbstractChart {
        checkParameters(dataset, renderer)
        return PieChart(dataset, renderer)
    }

    /**
     * Creates a dial chart
     * 
     * @param dataset the category series dataset (cannot be null)
     * @param renderer the dial renderer (cannot be null)
     * @return a pie chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset number of items is different than the number of
     * series renderers
     */
    fun getDialChartView(
        dataset: CategorySeries,
        renderer: DialRenderer
    ): AbstractChart {
        checkParameters(dataset, renderer)
        return DialChart(dataset, renderer)
    }

    /**
     * Creates a doughnut chart
     * 
     * @param dataset the multiple category series dataset (cannot be null)
     * @param renderer the series renderer (cannot be null)
     * @return a pie chart
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset number of items is different than the number of
     * series renderers
     */
    fun getDoughnutChartView(
        dataset: MultipleCategorySeries,
        renderer: DefaultRenderer
    ): AbstractChart {
        checkParameters(dataset, renderer)
        return DoughnutChart(dataset, renderer)
    }

    /**
     * Checks the validity of the dataset and renderer parameters.
     * 
     * @param dataset the multiple series dataset (cannot be null)
     * @param renderer the multiple series renderer (cannot be null)
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset and the renderer don't include the same number of
     * series
     */
    private fun checkParameters(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer
    ) {
        require(dataset.seriesCount == renderer.seriesRendererCount) { "Dataset and renderer should be not null and should have the same number of series" }
    }

    /**
     * Checks the validity of the dataset and renderer parameters.
     * 
     * @param dataset the category series dataset (cannot be null)
     * @param renderer the series renderer (cannot be null)
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset number of items is different than the number of
     * series renderers
     */
    private fun checkParameters(dataset: CategorySeries, renderer: DefaultRenderer) {
        require(dataset.itemCount == renderer.seriesRendererCount) { "Dataset and renderer should be not null and the dataset number of items should be equal to the number of series renderers" }
    }

    /**
     * Checks the validity of the dataset and renderer parameters.
     * 
     * @param dataset the category series dataset (cannot be null)
     * @param renderer the series renderer (cannot be null)
     * @throws IllegalArgumentException if dataset is null or renderer is null or
     * if the dataset number of items is different than the number of
     * series renderers
     */
    private fun checkParameters(dataset: MultipleCategorySeries, renderer: DefaultRenderer) {
        require(
            checkMultipleSeriesItems(
                dataset,
                renderer.seriesRendererCount
            )
        ) { "Titles and values should be not null and the dataset number of items should be equal to the number of series renderers" }
    }

    private fun checkMultipleSeriesItems(dataset: MultipleCategorySeries, value: Int): Boolean {
        val count = dataset.categoriesCount
        var equal = true
        var k = 0
        while (k < count && equal) {
            equal = dataset.getValues(k).size == dataset.getTitles(k).size
            k++
        }
        return equal
    }
}
