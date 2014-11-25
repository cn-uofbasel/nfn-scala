package nfn.service

import akka.actor.ActorRef
import ccn.packet.{CCNName, Content, MetaInfo}
import nfn.NFNApi

class Publish() extends NFNService {
  override def function: (Seq[NFNValue], ActorRef) => NFNValue = { (args, nfnServer) =>
    args match {
      case Seq(NFNContentObjectValue(contentName, contentData), NFNContentObjectValue(_, publishPrefixNameData), _) => {
        val nameOfContentWithoutPrefixToAdd = CCNName(new String(publishPrefixNameData).split("/").tail:_*)
        nfnServer ! NFNApi.AddToLocalCache(Content(nameOfContentWithoutPrefixToAdd, contentData, MetaInfo.empty), prependLocalPrefix = true)
        NFNEmptyValue()
      }
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName can only be applied to arguments of type CCNNameValue and not: $args")
    }
  }
}