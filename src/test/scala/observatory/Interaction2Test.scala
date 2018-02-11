package observatory

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

class Interaction2Test extends FunSuite with Checkers {

  test("caption") {
    val layer = Signal(Interaction2.availableLayers(0))
    val year = Var(1990)
    val caption = Interaction2.caption(layer, year)
    assert(caption() == "Temperatures (1990)")
    year() = 1991
    assert(caption() == "Temperatures (1991)")
  }

}
