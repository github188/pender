package com.vendor.thirdparty.tl.aipg.query;

import com.thoughtworks.xstream.XStream;
import com.vendor.thirdparty.tl.aipg.common.AipgReq;
import com.vendor.thirdparty.tl.aipg.common.AipgRsp;
import com.vendor.thirdparty.tl.aipg.common.XSUtil;
import com.vendor.thirdparty.tl.aipg.common.XStreamEx;

public class PGQueryUtil
{
	public static XStream buildXS(boolean isreq)
	{
		XStream xs=new XStreamEx();
		if(isreq)
		{
			xs.alias("AIPG",AipgReq.class);
			xs.aliasField("BODY",AipgReq.class,"trxData");
			xs.alias("QUERY_TRANS", PGQueryTrx.class);
		}
		else
		{
			xs.alias("AIPG", AipgRsp.class);
			xs.addImplicitCollection(AipgRsp.class,"trxData");
			xs.alias("BODY", PGQueryRsp.class);
	 		xs.aliasField("RET_DETAILS",PGQueryRsp.class, "details");
			xs.alias("RET_DETAIL",PGQueryRecord.class);
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
	private static XStream xsreq=buildXS(true);
	private static XStream xsrsp=buildXS(false);
}
