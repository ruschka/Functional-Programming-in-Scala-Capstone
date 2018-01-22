package observatory

import scala.collection.mutable

/**
  * 4th milestone: value-added information
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val cache = new mutable.HashMap[GridLocation, Temperature]()
    (gridLocation: GridLocation) => cache getOrElseUpdate (gridLocation, Visualization.predictTemperature(temperatures, gridLocation.toLocation))
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val yearlyGrids = temperaturess.map(makeGrid(_))
    val cache = new mutable.HashMap[GridLocation, Temperature]()

    (gridLocation: GridLocation) => {
      def doAverage(yg: Iterable[GridLocation => Temperature], acc: (Double, Int)): Temperature = {
        yg.headOption match {
          case Some(grid) => doAverage(yg.tail, (acc._1 + grid(gridLocation), acc._2 + 1))
          case None => acc._1 / acc._2.toDouble
        }
      }
      cache getOrElseUpdate (gridLocation, doAverage(yearlyGrids, (0d, 0)))
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val grid = makeGrid(temperatures)
    (gridLocation: GridLocation) => grid(gridLocation) - normals(gridLocation)
  }


}

