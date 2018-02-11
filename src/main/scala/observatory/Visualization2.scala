package observatory

import java.util.concurrent.ForkJoinPool

import com.sksamuel.scrimage.{Image, Pixel}
import org.apache.log4j.Logger

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {

  val logger = Logger.getLogger(this.getClass)

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(Runtime.getRuntime.availableProcessors() - 1))

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    d00 * (1 - point.x) * (1 - point.y) +
      d10 * point.x * (1 - point.y) +
      d01 * (1 - point.x) * point.y +
      d11 * point.x * point.y
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(grid: GridLocation => Temperature, colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    //doVisualizeGrid(grid, colors, tile, 6)
    ???
  }

  def doVisualizeGrid(grid: GridLocation => Temperature, colors: Iterable[(Temperature, Color)], tile: Tile, zoom: Int): Image = {
    val width = 1 << zoom
    val height = 1 << zoom
    val scaleFactor = 256 / width
    val subTiles = new Array[Future[Pixel]](width * height)
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        subTiles(x + y * width) = Future {
          val subTile = Tile(width * tile.x + x, height * tile.y + y, zoom + tile.zoom)
          val location = subTile.toLocation
          val cellPoint = location.toCellPoint
          val d00 = grid(location.toTopLeft)
          val d10 = grid(location.toTopRight)
          val d01 = grid(location.toBottomLeft)
          val d11 = grid(location.toBottomRight)
          val temperature = bilinearInterpolation(cellPoint, d00, d01, d10, d11)
          val color = Visualization.interpolateColor(colors, temperature)
          //logger.info(subTile)
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

}
