package observatory

import java.io.File

object Main extends App {

  val white = Color(0, 0, 0)
  val red = Color(255, 0, 0)
  val yellow = Color(255, 255, 0)
  val lightBlue = Color(0, 255, 255)
  val darkBlue = Color(0, 0, 255)
  val pink = Color(255, 0, 255)
  val purple = Color(33, 0, 107)
  val black = Color(255, 255, 255)
  val points: Seq[(Temperature, Color)] = Seq((60d, white), (32d, red), (12d, yellow), (0d, lightBlue), (-15d, darkBlue), (-27d, pink), (-50d, purple), (-60d, black))

  val data = Extraction.locateTemperaturesRDD(2000, "/stations.csv", "/2000.csv")
  val temperatures = Extraction.locationYearlyAverageRecordsRDD(data).collect().toSeq
  val image = Visualization.visualize(temperatures, points)
  image.output(new File("output/map.png"))
  SparkCtx.ctx.stop()
}
