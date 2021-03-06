package observatory

import java.io.File

import org.apache.log4j.Logger

import scala.collection.mutable


object Main extends App {

  val logger = Logger.getLogger(this.getClass)

  val grids = new mutable.HashMap[Year, GridLocation => Temperature]()

  val white = Color(0, 0, 0)
  val red = Color(255, 0, 0)
  val yellow = Color(255, 255, 0)
  val lightBlue = Color(0, 255, 255)
  val darkBlue = Color(0, 0, 255)
  val pink = Color(255, 0, 255)
  val purple = Color(33, 0, 107)
  val black = Color(255, 255, 255)
  val points: Seq[(Temperature, Color)] = Seq((60d, white), (32d, red), (12d, yellow), (0d, lightBlue), (-15d, darkBlue), (-27d, pink), (-50d, purple), (-60d, black))

  val yearlyData = (1975 to 1975).map {year =>
    val data = Extraction.locateTemperaturesRDD(year, "/stations.csv", s"/$year.csv")
    val temperatures = Extraction.locationYearlyAverageRecordsRDD(data).collect().toIterable
    (year, temperatures)
  }
  Interaction.generateTiles[Iterable[(Location, Temperature)]](yearlyData, (year, tile, data) => {
    val root = new File("target/deviations")
    if (!root.exists()) root.mkdir()
    val yearFolder = new File(root, s"$year")
    if (!yearFolder.exists()) yearFolder.mkdir()
    val zoomFolder = new File(yearFolder, s"${tile.zoom}")
    if (!zoomFolder.exists()) zoomFolder.mkdir()
    val imageFile = new File(zoomFolder, s"${tile.x}-${tile.y}.png")
    if (!imageFile.exists()) {
      val grid = grids.getOrElseUpdate(year, Manipulation.makeGrid(data))
      val image = Visualization2.doVisualizeGrid(grid, points, tile, 4)
      image.output(new File(s"target/deviations/$year/${tile.zoom}/${tile.x}-${tile.y}.png"))
      logger.info(s"target/deviations/$year/${tile.zoom}/${tile.x}-${tile.y}.png")
    } else {
      logger.info(s"Skipping image file $imageFile")
    }
  })
  SparkCtx.ctx.stop()
}
