package com.vendor.thirdparty.tl.aipg.pay;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.vendor.thirdparty.tl.aipg.common.AipgReq;
import com.vendor.thirdparty.tl.aipg.common.AipgRsp;
import com.vendor.thirdparty.tl.aipg.common.InfoRsp;
import com.vendor.thirdparty.tl.aipg.common.XSUtil;
import com.vendor.thirdparty.tl.aipg.query.PGQueryRecord;
import com.vendor.thirdparty.tl.aipg.query.PGQueryTrx;

@SuppressWarnings("rawtypes")
public class PGPayReq
{
	public PGPayReqSum getTRANS_SUM()
	{
		return TRANS_SUM;
	}
	public void setTRANS_SUM(PGPayReqSum tRANS_SUM)
	{
		TRANS_SUM = tRANS_SUM;
	}
	public List getDetails()
	{
		return details;
	}
	public void setDetails(List details)
	{
		this.details = details;
	}
	private PGPayReqSum TRANS_SUM;
	private List details;
	public static void main(String[] args)
	{
		XStream xs=new XStream();
		xs.alias("AIPG", AipgRsp.class);
		xs.alias("details",List.class, ArrayList.class);
		xs.aliasField("BODY",AipgRsp.class,"trxData");
		xs.alias("QUERY_TRANS", PGQueryTrx.class);
		xs.alias("RET_DETAIL", PGQueryRecord.class);
		AipgRsp rsp=new AipgRsp();
		InfoRsp hrsp=InfoRsp.packRsp(XSUtil.makeReq("111", "abc"),"0000","OK");
		rsp.setINFO(hrsp);
		PGQueryTrx qt=new PGQueryTrx();
		qt.setQUERY_SN("query_sn");
		List lsr=new ArrayList();
		PGQueryRecord qr=new PGQueryRecord();
		qr.setACCOUNT("account");
		lsr.add(qr);
		rsp.addTrx(qt);
		rsp.addTrx(lsr);
		String xml=xs.toXML(rsp);
		String xml2=xs.toXML(xs.fromXML(xml));
		System.out.println(xml);
		System.out.println("--------------");
		System.out.println(xml2);
	}
}
