package ccnliteinterface

object CCNLiteWireFormat {
  def fromName(possibleFormatName: String): Option[CCNLiteWireFormat] = {
    possibleFormatName match {
      case "ccnb" => Some(CCNBWireFormat())
      case "ndntlv" => Some(NDNTLVWireFormat())
      case _ => None
    }
  }
}

trait CCNLiteWireFormat
case class CCNBWireFormat() extends CCNLiteWireFormat {
  override def toString = "ccnb"
}
case class NDNTLVWireFormat() extends CCNLiteWireFormat {
  override def toString = "ndntlv2013"
}

