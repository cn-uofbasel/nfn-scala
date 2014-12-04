package nfn.service

import akka.actor.ActorRef
import ccn.packet.{Content, MetaInfo}

import scala.util.Try


/**
 * The map service is a generic service which transforms n [[NFNValue]] into a [[NFNListValue]] where each value was applied by a given other service of type [[NFNServiceValue]].
 * The first element of the arguments must be a [[NFNServiceValue]] and remaining n arguments must be a [[NFNListValue]].
 * The result of service invocation is a [[NFNListValue]].
 */
class MapService() extends NFNService {

  override def function(args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    args match {
      case Seq(NFNContentObjectValue(servName, servData), args @ _*) => {
        val tryExec = NFNService.serviceFromContent(Content(servName, servData, MetaInfo.empty)) map { (serv: NFNService) =>
          NFNListValue(
            (args map { arg =>
              val execTime = serv.executionTimeEstimate flatMap { _ => this.executionTimeEstimate }
              serv.instantiateCallable(serv.ccnName, Seq(arg), ccnApi, execTime).get.exec
            }).toList
          )
        }
        tryExec.get
      }
      case _ =>
        throw new NFNServiceArgumentException(s"A Map service must match Seq(NFNServiceValue, NFNValue*), but it was: $args ")
    }
  }
}

/**
 * The reduce service is a generic service which transforms a [[NFNListValue]] into a single [[NFNValue]] with another given service.
 * The first element of the arguments must be a [[NFNServiceValue]] and the second argument must be a [[NFNListValue]].
 * The result of service invocation is a [[NFNValue]].
 */
class ReduceService() extends NFNService {

  override def function(args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    args match {
      case Seq(fun: NFNServiceValue, argList: NFNListValue) => {
        // TODO exec time
        fun.serv.instantiateCallable(fun.serv.ccnName, argList.values, ccnApi, None).get.exec
      }
      case Seq(NFNContentObjectValue(servName, servData), args @ _*) => {
        val tryExec: Try[NFNValue] = NFNService.serviceFromContent(Content(servName, servData, MetaInfo.empty)) flatMap {
          (serv: NFNService) =>
            // TODO exec time
            serv.instantiateCallable(serv.ccnName, args, ccnApi, None) map {
              callableServ =>
                callableServ.exec
            }
        }
        tryExec.get
      }
      case _ =>
        throw new NFNServiceArgumentException(s"A Reduce service must match Seq(NFNServiceValue, NFNListValue), but it was: $args")
    }
  }
}
