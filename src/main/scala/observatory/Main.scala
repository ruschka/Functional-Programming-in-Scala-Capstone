package observatory

object Main extends App {

  val res1 = Extraction.locateTemperatures(2000, "/stations.csv", "/2000.csv")
  res1.foreach(l => println(l))
  val res2 = Extraction.locateTemperatures(2001, "/stations.csv", "/2001.csv")
  res2.foreach(l => println(l))
}
