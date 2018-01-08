package observatory

import java.time.{LocalDate, Month, MonthDay}

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
  * 1st milestone: data extraction
  */
object Extraction {

  lazy val conf: SparkConf = new SparkConf().setMaster("local").setAppName("observatory")
  lazy val ctx: SparkContext = new SparkContext(conf)

  var stationsFilesMap: Map[String, RDD[(ObservatoryId, StationRow)]] = Map()
  var temperaturesFilesMap: Map[String, RDD[(ObservatoryId, TemperatureRow)]] = Map()

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    locateTemperaturesRDD(year, stationsFile, temperaturesFile).collect().toIterable
  }

  def locateTemperaturesRDD(year: Year, stationsFile: String, temperaturesFile: String): RDD[(LocalDate, Location, Temperature)] = {
    stationsRDD(stationsFile).join(temperaturesRDD(temperaturesFile)).map { joined =>
      (joined._2._2.monthDay.atYear(year), joined._2._1.location, joined._2._2.temp)
    }.cache()
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    locationYearlyAverageRecordsRDD(ctx.makeRDD(records.toSeq)).collect().toIterable
  }

  def locationYearlyAverageRecordsRDD(records: RDD[(LocalDate, Location, Temperature)]): RDD[(Location, Temperature)] = {
    val mappedRDD = records.map(record => (record._2, record._3))
    val aggRDD = mappedRDD.aggregateByKey((0d, 0))((acc, record) => (acc._1 + record, acc._2 + 1), (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2))
    aggRDD.mapValues(x => x._1 / x._2.toDouble)
  }

  private def stationsRDD(stationsFile: String): RDD[(ObservatoryId, StationRow)] = {
    if (!stationsFilesMap.contains(stationsFile)) {
      stationsFilesMap = stationsFilesMap + (stationsFile -> filterStations(linesToStations(ctx.textFile(getResourcePath(stationsFile)))).cache())
    }
    stationsFilesMap.getOrElse(stationsFile, throw new IllegalStateException(s"Stations RDD $stationsFile is missing."))
  }

  private def linesToStations(lines: RDD[String]): RDD[(ObservatoryId, StationRow)] = {
    lines.map { line => {
      val arr = line.split(",")
      (
        ObservatoryId(
          if (arr.nonEmpty) arr(0) else "",
          if (arr.length > 1) arr(1) else ""
        ),
        StationRow(
          Location(
            if (arr.length > 2 && arr(2) != "") arr(2).toDouble else Double.MaxValue,
            if (arr.length > 3 && arr(3) != "") arr(3).toDouble else Double.MaxValue))
      )
    }}
  }

  private def filterStations(stations: RDD[(ObservatoryId, StationRow)]) =
    stations.filter(station => {
      (station._1.stnId != "" || station._1.wbanId != "") && station._2.location.lat != Double.MaxValue && station._2.location.lon != Double.MaxValue
    })

  private def temperaturesRDD(temperaturesFile: String): RDD[(ObservatoryId, TemperatureRow)] = {
    if (!temperaturesFilesMap.contains(temperaturesFile)) {
      // cached?
      temperaturesFilesMap = temperaturesFilesMap + (temperaturesFile -> filterTemperatures(linesToTemperatures(ctx.textFile(getResourcePath(temperaturesFile)))))
    }
    temperaturesFilesMap.getOrElse(temperaturesFile, throw new IllegalStateException(s"Temperatures RDD $temperaturesFile is missing."))
  }

  private def linesToTemperatures(lines: RDD[String]): RDD[(ObservatoryId, TemperatureRow)] = {
    lines.map { line => {
      val splitted = line.split(",")
      (
        ObservatoryId(
          splitted(0),
          splitted(1)
        ),
        TemperatureRow(
          MonthDay.of(Month.of(splitted(2).toInt), splitted(3).toInt),
          fahrenheitToCelcius(splitted(4).toDouble))
      )
    }}
  }

  private def fahrenheitToCelcius(f: Double) = {
    (f - 32) / 1.8
  }

  private def filterTemperatures(temperatures: RDD[(ObservatoryId, TemperatureRow)]) =
    temperatures.filter(temperature => {
      temperature._1.stnId != "" || temperature._1.wbanId != ""
    })

  private def getResourcePath(resource: String) = {
    this.getClass.getResource(resource).toString
  }
}
