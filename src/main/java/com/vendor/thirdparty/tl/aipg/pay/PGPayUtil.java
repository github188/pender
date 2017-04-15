package com.vendor.thirdparty.tl.aipg.pay;

import com.thoughtworks.xstream.XStream;
import com.vendor.thirdparty.tl.aipg.common.AipgReq;
import com.vendor.thirdparty.tl.aipg.common.AipgRsp;
import com.vendor.thirdparty.tl.aipg.common.XSUtil;
import com.vendor.thirdparty.tl.aipg.common.XStreamEx;

public class PGPayUtil
{
	private static XStream xsreq=buildXS(true);
	private static XStream xsrsp=buildXS(false);
	public static XStream buildXS(boolean isreq)
	{
		XStream xs=new XStreamEx();
		if(isreq)
		{
			xs.alias("AIPG",AipgReq.class);
			xs.addImplicitCollection(AipgReq.class,"trxData");
			xs.alias("BODY", PGPayReq.class);
			xs.aliasField("TRANS_DETAILS", PGPayReq.class, "details");
			xs.alias("TRANS_DETAIL", PGPayReqRecord.class);
			
		}
		else
		{
			xs.alias("AIPG", AipgRsp.class);
			xs.addImplicitCollection(AipgRsp.class,"trxData");
			xs.alias("BODY",PGPayRsp.class);
			xs.aliasField("RET_DETAILS", PGPayRsp.class, "details");
			xs.alias("RET_DETAIL",PGPayRspRecord.class);
		}
		return xs;
	}
	public static AipgReq parseReq(String xml)
	{
		return (AipgReq) xsreq.fromXML(xml);
	}
	public static AipgRsp parseRsp(String xml)
	{
		return (AipgRsp) xsrsp.fromXML(xml);
	}
	public static String toXml(Object o)
	{
		boolean isreq=(o instanceof AipgReq);
		if(isreq) return XSUtil.toXml(xsreq, o);
		else return XSUtil.toXml(xsrsp, o);
	}	
}
