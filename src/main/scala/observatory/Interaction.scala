package observatory

import java.util.concurrent.ForkJoinPool

import com.sksamuel.scrimage.{Image, Pixel}
import org.apache.log4j.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  val logger = Logger.getLogger(this.getClass)

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(Runtime.getRuntime.availableProcessors()))

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    tile.toLocation
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    doTile(temperatures, colors, tile, 8)
  }

  def doTile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile, zoom: Int): Image = {
    val width = 1 << zoom
    val height = 1 << zoom
    val scaleFactor = 256 / width
    val subTiles = new Array[Future[Pixel]](width * height)
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        subTiles(x + y * width) = Future {
          val subTile = Tile(width * tile.x + x, height * tile.y + y, zoom + tile.zoom)
          if (x == 0) logger.info(subTile)
          val location = subTile.toLocation
          val temperature = Visualization.predictTemperature(temperatures, location)
          val color = Visualization.interpolateColor(colors, temperature)
          Pixel(color.red, color.green, color.blue, 127)
        }
      }
    }
    val fPixels = Future.sequence(subTiles.toSeq)
    val fImage = fPixels map { pixels =>
      val image = Image(width, height, pixels.toArray)
      image.scale(scaleFactor)
    }
    Await.result(fImage, Duration.Inf)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit
  ): Unit = {
    for (zoom <- 0 to 3) {
      val n = 1 << zoom
      for (y <- 0 until n) {
        for (x <- 0 until n) {
          val tile = Tile(x, y, zoom)
          yearlyData.foreach(d => {
            generateImage(d._1, tile, d._2)
          })
        }
      }
    }
  }

}
