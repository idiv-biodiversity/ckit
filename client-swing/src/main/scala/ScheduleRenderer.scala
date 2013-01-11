/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                               *
 *  Copyright  ©  2012  Christian Krause                                                         *
 *                                                                                               *
 *  Christian Krause  <christian.krause@ufz.de>                                                  *
 *                    <kizkizzbangbang@googlemail.com>                                           *
 *                                                                                               *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                               *
 *  This file is part of 'ClusterKit'.                                                           *
 *                                                                                               *
 *  This project is free software: you can redistribute it and/or modify it under the terms      *
 *  of the GNU General Public License as published by the Free Software Foundation, either       *
 *  version 3 of the License, or any later version.                                              *
 *                                                                                               *
 *  This project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;    *
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    *
 *  See the GNU General Public License for more details.                                         *
 *                                                                                               *
 *  You should have received a copy of the GNU General Public License along with this project.   *
 *  If not, see <http://www.gnu.org/licenses/>.                                                  *
 *                                                                                               *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


package ckit
package client
package swing

import java.awt._
import java.awt.geom._
import java.io._

import org.jfree.chart.axis._
import org.jfree.chart.entity.EntityCollection
import org.jfree.chart.event.RendererChangeEvent
import org.jfree.chart.labels.CategoryItemLabelGenerator
import org.jfree.chart.plot._
import org.jfree.chart.renderer.AbstractRenderer
import org.jfree.chart.renderer.category._
import org.jfree.data.category.CategoryDataset
import org.jfree.data.gantt.GanttCategoryDataset
import org.jfree.io.SerialUtilities
import org.jfree.ui.RectangleEdge
import org.jfree.util.PaintUtilities

//class Schedule2Renderer extends AbstractRenderer {
//}

class ScheduleRenderer extends GanttRenderer {

  override protected def drawTasks(g2: Graphics2D,
                                   state: CategoryItemRendererState,
                                   dataArea: Rectangle2D,
                                   plot: CategoryPlot,
                                   domainAxis: CategoryAxis,
                                   rangeAxis: ValueAxis,
                                   dataset: GanttCategoryDataset,
                                   row: Int,
                                   column: Int) {

    val count = dataset.getSubIntervalCount(row, column)

    if (count == 0) {
      drawTask(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column)
    }

    val orientation = plot.getOrientation

    for ( subinterval ← 0 until count ) {
      val rangeAxisLocation = plot.getRangeAxisEdge

      val value0 = dataset.getStartValue(row, column, subinterval)
      if (value0 == null) return

      var translatedValue0 = rangeAxis.valueToJava2D(value0.doubleValue, dataArea, rangeAxisLocation)

      val value1 = dataset.getEndValue(row, column, subinterval)
      if (value1 == null) return

      var translatedValue1 = rangeAxis.valueToJava2D(value1.doubleValue(), dataArea, rangeAxisLocation)

      if (translatedValue1 < translatedValue0) {
        val temp = translatedValue1
        translatedValue1 = translatedValue0
        translatedValue0 = temp
      }

      val rectStart = calculateBarW0(plot, plot.getOrientation, dataArea, domainAxis, state, row, column)
      val rectLength = Math.abs(translatedValue1 - translatedValue0)
      val rectBreadth = state.getBarWidth

      val (bar,barBase) = orientation match {
        case PlotOrientation.HORIZONTAL ⇒ (new Rectangle2D.Double(translatedValue0, rectStart, rectLength, rectBreadth), RectangleEdge.LEFT)
        case PlotOrientation.VERTICAL   ⇒ (new Rectangle2D.Double(rectStart, translatedValue0, rectBreadth, rectLength), RectangleEdge.BOTTOM)
      }

      val percent = dataset.getPercentComplete(row, column, subinterval)

      if (percent != null) {
        val p = percent.doubleValue

        val start = getStartPercent
        val end = getEndPercent

        val (completeBar,incompleteBar) = orientation match {
          case PlotOrientation.HORIZONTAL ⇒
            (new Rectangle2D.Double(translatedValue0, rectStart + start * rectBreadth, rectLength * p, rectBreadth * (end - start)),
             new Rectangle2D.Double(translatedValue0 + rectLength * p, rectStart + start * rectBreadth, rectLength * (1 - p), rectBreadth * (end - start)))

          case PlotOrientation.VERTICAL ⇒
            (new Rectangle2D.Double(rectStart + start * rectBreadth, translatedValue0 + rectLength * (1 - p), rectBreadth * (end - start), rectLength * p),
             new Rectangle2D.Double(rectStart + start * rectBreadth, translatedValue0, rectBreadth * (end - start), rectLength * (1 - p)))
        }

        g2.setPaint(getCompletePaint)
        g2.fill(completeBar)

        g2.setPaint(getIncompletePaint)
        g2.fill(incompleteBar)
      }

      if (getShadowsVisible) {
        getBarPainter.paintBarShadow(g2, this, row, column, bar, barBase, true)
      }

      getBarPainter.paintBar(g2, this, row, column, bar, barBase)

      if (isDrawBarOutline && state.getBarWidth > BarRenderer.BAR_OUTLINE_WIDTH_THRESHOLD) {
        g2.setStroke(getItemStroke(row, column))
        g2.setPaint(getItemOutlinePaint(row, column))
        g2.draw(bar)
      }

      if (subinterval == count - 1) {
        val datasetIndex = plot.indexOf(dataset)
        val columnKey = dataset.getColumnKey(column)
        val rowKey = dataset.getRowKey(row)
        val xx = domainAxis.getCategorySeriesMiddle(columnKey, rowKey, dataset, getItemMargin, dataArea, plot.getDomainAxisEdge)
        updateCrosshairValues(state.getCrosshairState, dataset.getRowKey(row), dataset.getColumnKey(column), value1.doubleValue, datasetIndex, xx, translatedValue1, orientation)
      }

      if (state.getInfo != null) {
        val entities = state.getEntityCollection
        if (entities != null) {
          addItemEntity(entities, dataset, row, column, bar);
        }
      }
    }
  }

  override protected def drawTask(g2: Graphics2D,
                                  state: CategoryItemRendererState,
                                  dataArea: Rectangle2D,
                                  plot: CategoryPlot,
                                  domainAxis: CategoryAxis,
                                  rangeAxis: ValueAxis,
                                  dataset: GanttCategoryDataset,
                                  row: Int,
                                  column: Int) {

    val orientation = plot.getOrientation
    val rangeAxisLocation = plot.getRangeAxisEdge

    val value0 = dataset.getEndValue(row, column)
    if (value0 == null) {
      return
    }

    var java2dValue0 = rangeAxis.valueToJava2D(value0.doubleValue, dataArea, rangeAxisLocation);

    var value1 = dataset.getStartValue(row, column)
    if (value1 == null) {
      return
    }

    var java2dValue1 = rangeAxis.valueToJava2D(value1.doubleValue, dataArea, rangeAxisLocation)

    if (java2dValue1 < java2dValue0) {
      val temp = java2dValue1
      java2dValue1 = java2dValue0
      java2dValue0 = temp
      value1 = value0
    }

    val rectStart = calculateBarW0(plot, orientation, dataArea, domainAxis, state, row, column)
    val rectBreadth = state.getBarWidth()
    val rectLength = Math.abs(java2dValue1 - java2dValue0)

    val (bar,barBase) = orientation match {
      case PlotOrientation.HORIZONTAL ⇒ (new Rectangle2D.Double(java2dValue0, rectStart, rectLength, rectBreadth), RectangleEdge.LEFT)
      case PlotOrientation.VERTICAL   ⇒ (new Rectangle2D.Double(rectStart, java2dValue1, rectBreadth, rectLength), RectangleEdge.BOTTOM)
    }

    val percent = dataset.getPercentComplete(row, column)

    if (percent != null) {
      val start = getStartPercent
      val end = getEndPercent

      val p = percent.doubleValue

      val (completeBar,incompleteBar) = orientation match {
        case PlotOrientation.HORIZONTAL ⇒
          (new Rectangle2D.Double(java2dValue0, rectStart + start * rectBreadth, rectLength * p, rectBreadth * (end - start)),
           new Rectangle2D.Double(java2dValue0 + rectLength * p, rectStart + start * rectBreadth, rectLength * (1 - p), rectBreadth * (end - start)))

        case PlotOrientation.VERTICAL ⇒
          (new Rectangle2D.Double(rectStart + start * rectBreadth, java2dValue1 + rectLength * (1 - p), rectBreadth * (end - start), rectLength * p),
           new Rectangle2D.Double(rectStart + start * rectBreadth, java2dValue1, rectBreadth * (end - start), rectLength * (1 - p)))
      }

      g2.setPaint(getCompletePaint)
      g2.fill(completeBar)

      g2.setPaint(getIncompletePaint)
      g2.fill(incompleteBar)
    }

    if (getShadowsVisible) {
      getBarPainter.paintBarShadow(g2, this, row, column, bar, barBase, true)
    }

    getBarPainter.paintBar(g2, this, row, column, bar, barBase)


    if (isDrawBarOutline && state.getBarWidth > BarRenderer.BAR_OUTLINE_WIDTH_THRESHOLD) {
      val stroke = getItemOutlineStroke(row, column)
      val paint = getItemOutlinePaint(row, column)
      if (stroke != null && paint != null) {
        g2.setStroke(stroke);
        g2.setPaint(paint);
        g2.draw(bar);
      }
    }

    val generator = getItemLabelGenerator(row, column)
    if (generator != null && isItemLabelVisible(row, column)) {
      drawItemLabel(g2, dataset, row, column, plot, generator, bar, false)
    }

    val datasetIndex = plot.indexOf(dataset)
    val columnKey = dataset.getColumnKey(column)
    val rowKey = dataset.getRowKey(row)
    val xx = domainAxis.getCategorySeriesMiddle(columnKey, rowKey, dataset, getItemMargin, dataArea, plot.getDomainAxisEdge)
    updateCrosshairValues(state.getCrosshairState, dataset.getRowKey(row), dataset.getColumnKey(column), value1.doubleValue, datasetIndex, xx, java2dValue1, orientation)

    val entities = state.getEntityCollection
    if (entities != null) {
      addItemEntity(entities, dataset, row, column, bar);
    }
  }

}
