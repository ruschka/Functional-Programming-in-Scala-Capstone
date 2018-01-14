package observatory

import java.util.concurrent.{Executors, ForkJoinPool}

import com.sksamuel.scrimage.{Image, Pixel}
import org.apache.log4j.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  val EarthRadius = 6371d

  val logger = Logger.getLogger(this.getClass)

  implicit val executionCtx: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(Runtime.getRuntime.availableProcessors()))

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {

    def idw(temperatures: Iterable[(Location, Temperature)], acc: (Double, Double)): Temperature = {
      temperatures.headOption match {
        case Some((temperatureLocation, temperature)) =>
          val distance = greatCircleDistance(temperatureLocation, location)
          if (distance < 1) {
            temperature
          } else {
            val weight = weightingFunction(distance)
            idw(temperatures.tail, (acc._1 + weight * temperature, acc._2 + weight))
          }
        case None => acc._1 / acc._2
      }
    }
    idw(temperatures, (0d, 0d))
  }

  /**
  def predictTemperatureRDD(temperatures: RDD[(Location, Temperature)], location: Location): Temperature = {
    val found = temperatures.filter(t => t._1 == location).collect().toSeq
    if (found.nonEmpty) {
      found(0)._2
    } else {
      val aggregated = temperatures.aggregate((0d, 0d))((acc, temperature) => {
        val weight = weightingFunction(location, temperature._1)
        (acc._1 + weight * temperature._2, acc._2 + weight)
      }, (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2))
      aggregated._1 / aggregated._2
    }
  }
    */

  def weightingFunction(distance: Double): Double = {
    1 / Math.pow(distance, 2)
  }

  def greatCircleDistance(loc1: Location, loc2: Location): Double = {
    if (loc1 == loc2) {
      0
    } else {
      EarthRadius * Math.acos(
        Math.sin(radians(loc1.lat)) * Math.sin(radians(loc2.lat)) +
          Math.cos(radians(loc1.lat)) * Math.cos(radians(loc2.lat)) * Math.cos(radians(loc1.lon) - radians(loc2.lon)))
    }
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
    val width = 360
    val height = 180
    val pixels = new Array[Future[Pixel]](width * height)
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        pixels(x + y * 360) = Future {
          val lat = 90 - y
          val lon = x - 180
          val temperature = predictTemperature(temperatures, Location(lat, lon))
          val color = interpolateColor(colors, temperature)
          logger.info(s"point $lat, $lon, temperature $temperature, color $color")
          Pixel(color.red, color.green, color.blue, 255)
        }
      }
    }
    val fPixels = Future.sequence(pixels.toSeq)
    val fImage = fPixels.map(pixels => Image(width, height, pixels.toArray))
    Await.result(fImage, Duration.Inf)
  }

}

