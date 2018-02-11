package observatory

/**
  * 6th (and last) milestone: user interface polishing
  */
object Interaction2 {

  val white = Color(0, 0, 0)
  val red = Color(255, 0, 0)
  val yellow = Color(255, 255, 0)
  val lightBlue = Color(0, 255, 255)
  val darkBlue = Color(0, 0, 255)
  val pink = Color(255, 0, 255)
  val purple = Color(33, 0, 107)
  val black = Color(255, 255, 255)
  val temperatures: Seq[(Temperature, Color)] = Seq((60d, white), (32d, red), (12d, yellow), (0d, lightBlue), (-15d, darkBlue), (-27d, pink), (-50d, purple), (-60d, black))
  val deviations: Seq[(Temperature, Color)] = Seq((7d, black), (4d, red), (2d, yellow), (0d, white), (-2, lightBlue), (-7, darkBlue))

  /**
    * @return The available layers of the application
    */
  def availableLayers: Seq[Layer] = {
    Seq(
      Layer(LayerName.Temperatures, temperatures, 1975 to 2015),
      Layer(LayerName.Deviations, deviations, 1990 to 2004)
    )
  }

  /**
    * @param selectedLayer A signal carrying the layer selected by the user
    * @return A signal containing the year bounds corresponding to the selected layer
    */
  def yearBounds(selectedLayer: Signal[Layer]): Signal[Range] = {
    Signal(selectedLayer().bounds)
  }

  /**
    * @param selectedLayer The selected layer
    * @param sliderValue The value of the year slider
    * @return The value of the selected year, so that it never goes out of the layer bounds.
    *         If the value of `sliderValue` is out of the `selectedLayer` bounds,
    *         this method should return the closest value that is included
    *         in the `selectedLayer` bounds.
    */
  def yearSelection(selectedLayer: Signal[Layer], sliderValue: Signal[Year]): Signal[Year] = {
    Signal(math.min(math.max(sliderValue(), selectedLayer().bounds.start), selectedLayer().bounds.end))
  }

  /**
    * @param selectedLayer The selected layer
    * @param selectedYear The selected year
    * @return The URL pattern to retrieve tiles
    */
  def layerUrlPattern(selectedLayer: Signal[Layer], selectedYear: Signal[Year]): Signal[String] = {
    Signal(s"target/${selectedLayer().layerName.id}/${selectedYear()}/{z}/{x}/{y}.png")
  }

  /**
    * @param selectedLayer The selected layer
    * @param selectedYear The selected year
    * @return The caption to show
    */
  def caption(selectedLayer: Signal[Layer], selectedYear: Signal[Year]): Signal[String] = {
    Signal(s"${selectedLayer().layerName.id.capitalize} (${selectedYear()})")
  }

}

sealed abstract class LayerName(val id: String)
object LayerName {
  case object Temperatures extends LayerName("temperatures")
  case object Deviations extends LayerName("deviations")
}

/**
  * @param layerName Name of the layer
  * @param colorScale Color scale used by the layer
  * @param bounds Minimum and maximum year supported by the layer
  */
case class Layer(layerName: LayerName, colorScale: Seq[(Temperature, Color)], bounds: Range)

