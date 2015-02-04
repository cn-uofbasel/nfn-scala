package runnables.evaluation

import ccn.packet._
import com.typesafe.config.{Config, ConfigFactory}
import lambdacalculus.parser.ast.Expr
import monitor.Monitor
import nfn.service.FilterNorthpole
import node.LocalNodeFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


object FilterNorthpoleTest extends App {

  implicit val conf: Config = ConfigFactory.load()

  /*
  *
  * SETUP:
  *  Network with node1 and node2
  *  Service "FilterNorthpole" on node1
  *  Sample data "track" on node1
  *
  * SCENARIO:
  *  node2 requests "track" filtered with "FilterNorthpole"
  *
  * */

  // network setup
  val node1 = LocalNodeFactory.forId(1)
  val node2 = LocalNodeFactory.forId(2)
  val nodes = List(node1, node2)
  node1 <~> node2

  // service setup
  val northpoleServ = new FilterNorthpole()
  node1.publishServiceLocalPrefix(northpoleServ)
  val northpole = node1.localPrefix.append(northpoleServ.ccnName)

  // data setup
  val trackname = node1.localPrefix.append("track")
  val trackdata = "3 4 6 4 4 6 4 5 6 4 6 7 6 6 5 5 6 5".getBytes
  node1 += Content(trackname, trackdata)

  // --------------------------------------------------------------

  Thread.sleep(1000)
  import lambdacalculus.parser.ast.LambdaDSL._
  import nfn.LambdaNFNImplicits._
  implicit val useThunks: Boolean = false

  val interest1: Interest = northpole call("3 4 6 4 4 6 4 5 6 4 6 7 6 6 5 5 6 5")
  val interest2: Interest = northpole call(trackname.name)
  // Question: Difference between "Interest" and "Expr"? Both seem to work..

  // send interest1 or interest2?
  val interest = interest2

  // send interest...
  val startTime = System.currentTimeMillis
  println(s" | Send interest: " + interest1)
  node2 ? interest onComplete {
    // ... and reveive content
    case Success(resultContent) => {
      println(s" | Result:        " + new String(resultContent.data))
      println(s" | Time:          " + (System.currentTimeMillis-startTime) + "ms")
      Monitor.monitor ! Monitor.Visualize()
      nodes foreach { _.shutdown() }
    }
    // ... but do not get content
    case Failure(e) =>
      println(" | No content reveived.")
      Monitor.monitor ! Monitor.Visualize()
      nodes foreach { _.shutdown() }
  }

}
