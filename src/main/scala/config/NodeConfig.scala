package config

import java.util.concurrent.TimeUnit

import ccn.packet.CCNName
import ccnliteinterface._
import com.typesafe.config.ConfigException.BadValue
import com.typesafe.config.{Config, ConfigFactory}
import monitor.Monitor.NodeLog

import scala.concurrent.duration.Duration

case class ConfigException(msg: String) extends Exception(msg)

object StaticConfig {

  private var maybeConfig: Option[Config] = None

  def config: Config = maybeConfig match {
    case Some(config) => config
    case None =>
      val conf = ConfigFactory.load()
      maybeConfig = Some(conf)
      conf
  }

  def isNackEnabled =  config.getBoolean("nfn-scala.usenacks")

  def isThunkEnabled = config.getBoolean("nfn-scala.usethunks")

  def defaultTimeoutDuration = Duration(config.getInt("nfn-scala.defaulttimeoutmillis"), TimeUnit.MILLISECONDS)

  def debugLevel = config.getString("nfn-scala.debuglevel")

  def packetformat: CCNLiteWireFormat = {
    val path = "nfn-scala.packetformat"
    val wfName = config.getString(path)
    CCNLiteWireFormat.fromName(wfName) match {
      case Some(wf) => wf
      case None => throw new BadValue(path,
        s"""
        | can only be "ccnb" or "ndn" and not "$wfName"
        """.stripMargin)
    }
  }

  def ccnlitelibrarytype: CCNLiteInterfaceType = {
    val path = "nfn-scala.ccnlitelibrarytype"
    val ltName = config.getString(path)
    CCNLiteInterfaceType.fromName(ltName) match {
      case Some(lt) => lt
      case None => throw new BadValue(path,
        s"""
        | can only be "jni" or "native" and not "$ltName"
      """.stripMargin)
    }
  }
}

object SystemEnvironment {
  val ccnLiteEnv: String = {
    val maybeCcnLiteEnv = System.getenv("CCNL_HOME")
    if(maybeCcnLiteEnv == null) {
      throw new Exception("CCNL_HOME system variable is not set. Set it to the root directory of your local ccn-lite copy and compile it.")
    }
    maybeCcnLiteEnv
  }
}

trait NodeConfig {
  def host: String
  def port: Int
  def prefix: CCNName
  def toNodeLog: NodeLog


}

case class CombinedNodeConfig(maybeNFNNodeConfig: Option[RouterConfig], maybeComputeNodeConfig: Option[ComputeNodeConfig])

case class RouterConfig(host: String, port: Int, prefix: CCNName, isCCNOnly: Boolean = false) extends NodeConfig {
  def toNodeLog: NodeLog = NodeLog(host, port, Some(if(isCCNOnly) "CCNNode" else "NFNNode"), Some(prefix.toString))
}


case class ComputeNodeConfig(host: String, port: Int, prefix: CCNName, withLocalAM: Boolean = false) extends NodeConfig {
  def toNodeLog: NodeLog = NodeLog(host, port, Some("ComputeNode"), Some(prefix + "compute"))
}
