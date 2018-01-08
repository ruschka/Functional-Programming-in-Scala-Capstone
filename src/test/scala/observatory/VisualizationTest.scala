package observatory


import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

class VisualizationTest extends FunSuite with Checkers {

  val white = Color(0, 0, 0)
  val red = Color(255, 0, 0)
  val yellow = Color(255, 255, 0)
  val lightBlue = Color(0, 255, 255)
  val darkBlue = Color(0, 0, 255)
  val pink = Color(255, 0, 255)
  val purple = Color(33, 0, 107)
  val black = Color(255, 255, 255)
  val points: Seq[(Temperature, Color)] = Seq((60d, white), (32d, red), (12d, yellow), (0d, lightBlue), (-15d, darkBlue), (-27d, pink), (-50d, purple), (-60d, black))

  test("linearInterpolation") {
    val li = Visualization.linearInterpolation(0, 0, 12, 255, 6)
    assert(li == 127.5)
  }

  test("interpolateColor_1") {
    val color = Visualization.interpolateColor(points, 6d)
    assert(color.red == 128)
    assert(color.green == 255)
    assert(color.blue == 128)
  }

  test("interpolateColor_2") {
    val color = Visualization.interpolateColor(points, 70d)
    assert(color == white)
  }

  test("interpolateColor_3") {
    val color = Visualization.interpolateColor(points, 32d)
    assert(color == red)
  }

  test("rounding") {
    assert(128 == Math.round(127.5))
  }

}
