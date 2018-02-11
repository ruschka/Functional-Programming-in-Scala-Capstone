package observatory

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

class Visualization2Test extends FunSuite with Checkers {

  test("location to cell point") {
    val location = Location(10.12345, 21.98705)
    val cellPoint = location.toCellPoint
    assert(cellPoint == CellPoint(0.12345000000000006, 0.98705))
  }

}
