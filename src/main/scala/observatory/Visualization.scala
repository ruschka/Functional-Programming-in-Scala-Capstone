package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  val EarthRadius = 6371d

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    ???
  }

  def greatCircleDistance(loc1: Location, loc2: Location): Double = {
    val delta = Math.acos(
      Math.sin(radians(loc1.lat)) * Math.sin(radians(loc2.lat)) +
        Math.cos(radians(loc1.lat)) * Math.cos(radians(loc2.lat)) * Math.cos(radians(loc1.lon) - radians(loc2.lon)))
    EarthRadius * delta
  }

  def radians(degrees: Double): Double = {
    degrees * (Math.PI / 180)
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    val sorted = points.toSeq.sortBy(_._1)
    if (value <= sorted.head._1) {
      sorted.head._2
    } else if (value >= sorted.last._1) {
      sorted.last._2
    } else {
      val colors = findColorsBetweenTemperature(sorted, value)
      val red = linearInterpolation(colors._1._1, colors._1._2.red, colors._2._1, colors._2._2.red, value)
      val green = linearInterpolation(colors._1._1, colors._1._2.green, colors._2._1, colors._2._2.green, value)
      val blue = linearInterpolation(colors._1._1, colors._1._2.blue, colors._2._1, colors._2._2.blue, value)
      Color(Math.round(red).toInt, Math.round(green).toInt, Math.round(blue).toInt)
    }
  }

  def findColorsBetweenTemperature(points: Seq[(Temperature, Color)], value: Temperature): ((Temperature, Color), (Temperature, Color)) = {
    if (points.head._1 <= value && points.tail.head._1 > value) {
      (points.head, points.tail.head)
    } else {
      findColorsBetweenTemperature(points.tail, value)
    }
  }

  def linearInterpolation(x1: Double, y1: Double, x2: Double, y2: Double, x: Double): Double = {
    (y1 * (x2 - x) + y2 * (x - x1)) / (x2 - x1)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    ???
  }

}

