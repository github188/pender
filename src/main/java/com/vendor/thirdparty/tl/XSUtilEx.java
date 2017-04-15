package com.vendor.thirdparty.tl;

import com.thoughtworks.xstream.XStream;
import com.vendor.thirdparty.tl.aipg.accttrans.AcctTransferReq;
import com.vendor.thirdparty.tl.aipg.acctvalid.ValbSum;
import com.vendor.thirdparty.tl.aipg.acctvalid.ValidBReq;
import com.vendor.thirdparty.tl.aipg.acctvalid.VbDetail;
import com.vendor.thirdparty.tl.aipg.acquery.AcNode;
import com.vendor.thirdparty.tl.aipg.acquery.AcQueryRep;
import com.vendor.thirdparty.tl.aipg.acquery.AcQueryReq;
import com.vendor.thirdparty.tl.aipg.agrmqx.XQSignReq;
import com.vendor.thirdparty.tl.aipg.agrmsync.SignInfoDetail;
import com.vendor.thirdparty.tl.aipg.agrmsync.SignInfoSync;
import com.vendor.thirdparty.tl.aipg.ahquery.AHQueryRep;
import com.vendor.thirdparty.tl.aipg.ahquery.AHQueryReq;
import com.vendor.thirdparty.tl.aipg.ahquery.BalNode;
import com.vendor.thirdparty.tl.aipg.cash.CashRep;
import com.vendor.thirdparty.tl.aipg.cash.CashReq;
import com.vendor.thirdparty.tl.aipg.common.AipgReq;
import com.vendor.thirdparty.tl.aipg.common.AipgRsp;
import com.vendor.thirdparty.tl.aipg.common.InfoReq;
import com.vendor.thirdparty.tl.aipg.common.XSUtil;
import com.vendor.thirdparty.tl.aipg.downloadrsp.DownRsp;
import com.vendor.thirdparty.tl.aipg.etdtlquery.EtQReq;
import com.vendor.thirdparty.tl.aipg.etquery.EtNode;
import com.vendor.thirdparty.tl.aipg.etquery.EtQueryRep;
import com.vendor.thirdparty.tl.aipg.etquery.EtQueryReq;
import com.vendor.thirdparty.tl.aipg.loginrsp.LoginRsp;
import com.vendor.thirdparty.tl.aipg.notify.Notify;
import com.vendor.thirdparty.tl.aipg.payreq.Body;
import com.vendor.thirdparty.tl.aipg.payreq.Trans_Detail;
import com.vendor.thirdparty.tl.aipg.pos.PinVerifyReq;
import com.vendor.thirdparty.tl.aipg.pos.PinVerifyRsp;
import com.vendor.thirdparty.tl.aipg.pos.QPTrans;
import com.vendor.thirdparty.tl.aipg.pos.QPTransRet;
import com.vendor.thirdparty.tl.aipg.pos.QPTrf;
import com.vendor.thirdparty.tl.aipg.pos.QPTrfret;
import com.vendor.thirdparty.tl.aipg.pos.Trfer;
import com.vendor.thirdparty.tl.aipg.pos.Trfret;
import com.vendor.thirdparty.tl.aipg.qtd.QTDReq;
import com.vendor.thirdparty.tl.aipg.qtd.QTDRsp;
import com.vendor.thirdparty.tl.aipg.qtd.QTDRspDetail;
import com.vendor.thirdparty.tl.aipg.qvd.QVDReq;
import com.vendor.thirdparty.tl.aipg.refund.Refund;
import com.vendor.thirdparty.tl.aipg.rev.TransRev;
import com.vendor.thirdparty.tl.aipg.rev.TransRevRsp;
import com.vendor.thirdparty.tl.aipg.rnp.Rnp;
import com.vendor.thirdparty.tl.aipg.rnp.Rnpa;
import com.vendor.thirdparty.tl.aipg.rnp.RnpaRet;
import com.vendor.thirdparty.tl.aipg.rnp.Rnpc;
import com.vendor.thirdparty.tl.aipg.rnp.Rnpr;
import com.vendor.thirdparty.tl.aipg.rtreq.Trans;
import com.vendor.thirdparty.tl.aipg.rtrsp.TransRet;
import com.vendor.thirdparty.tl.aipg.signquery.NSignReq;
import com.vendor.thirdparty.tl.aipg.signquery.QSignDetail;
import com.vendor.thirdparty.tl.aipg.signquery.QSignReq;
import com.vendor.thirdparty.tl.aipg.signquery.QSignRsp;
import com.vendor.thirdparty.tl.aipg.singleacctvalid.ValidR;
import com.vendor.thirdparty.tl.aipg.syncex.SyncReqEx;
import com.vendor.thirdparty.tl.aipg.syncex.SyncReqExDetail;
import com.vendor.thirdparty.tl.aipg.syncex.SyncRspEx;
import com.vendor.thirdparty.tl.aipg.syncex.SyncRspExDetail;
import com.vendor.thirdparty.tl.aipg.synreq.SCloseReq;
import com.vendor.thirdparty.tl.aipg.synreq.SCloseRsp;
import com.vendor.thirdparty.tl.aipg.synreq.SvrfReq;
import com.vendor.thirdparty.tl.aipg.synreq.Sync;
import com.vendor.thirdparty.tl.aipg.synreq.SyncDetail;
import com.vendor.thirdparty.tl.aipg.transfer.TransferReq;
import com.vendor.thirdparty.tl.aipg.transquery.BalReq;
import com.vendor.thirdparty.tl.aipg.transquery.BalRet;
import com.vendor.thirdparty.tl.aipg.transquery.QTDetail;
import com.vendor.thirdparty.tl.aipg.transquery.QTransRsp;
import com.vendor.thirdparty.tl.aipg.transquery.TransQueryReq;
import com.vendor.thirdparty.tl.aipg.tunotify.TUNotifyRep;
import com.vendor.thirdparty.tl.aipg.tunotify.TUNotifyReq;

public class XSUtilEx
{
	private static final String HEAD = "<?xml version=\"1.0\" encoding=\"GBK\"?>";
	public static AipgReq makeNotify(String qsn)
	{
		AipgReq req=new AipgReq();
		Notify notify=new Notify();
		notify.setNOTIFY_SN(qsn);
		req.setINFO(XSUtilEx.makeReq("200003",""+System.currentTimeMillis()));
		req.addTrx(notify);
		return req;
	}
	public static AipgReq makeNSignReq(String agrno,String contractno,String acct,String status)
	{
		return makeNSignReq(agrno,contractno,acct,status,"1");
	}
	public static AipgReq makeNSignReq(String agrno,String contractno,String acct,String status,String signtype)
	{
		AipgReq req=new AipgReq();
		QSignDetail qsd=new QSignDetail();
		qsd.setAGREEMENTNO(agrno);
		qsd.setACCT(acct);
		qsd.setCONTRACTNO(contractno);
		qsd.setSTATUS(status);
		//qsd.setSIGNTYPE(signtype);
		NSignReq nsr=new NSignReq();
		nsr.addDtl(qsd);
		req.setINFO(XSUtilEx.makeReq("210003",""+System.currentTimeMillis()));
		req.addTrx(nsr);
		return req;		
	}
	public static AipgReq xmlReq(String xmlMsg)
	{
		XStream xs=new XStreamIg();
		XSUtilEx.initXStream(xs, true);
		AipgReq req=(AipgReq) xs.fromXML(xmlMsg);
		return req;
	}
	public static AipgRsp xmlRsp(String xmlMsg)
	{
		XStream xs=new XStreamIg();
		XSUtilEx.initXStream(xs, false);
		AipgRsp rsp=(AipgRsp) xs.fromXML(xmlMsg);
		return rsp;
	}
	public static String reqXml(AipgReq req)
	{
		XStream xs=new XStreamIg();
		XSUtilEx.initXStream(xs, true);
		String xml=HEAD+xs.toXML(req);
		xml=xml.replace("__", "_");
		return xml;
	}
	public static String rspXml(AipgRsp rsp)
	{
		XStream xs=new XStreamIg();
		XSUtilEx.initXStream(xs, false);
		String xml=HEAD+xs.toXML(rsp);
		xml=xml.replace("__", "_");
		return xml;
	}
	public static void initXStream(XStream xs,boolean isreq)
	{
		if(isreq) 
			xs.alias("AIPG", AipgReq.class); 
		else 
			xs.alias("AIPG", AipgRsp.class);
		xs.alias("INFO", InfoReq.class);
		xs.addImplicitCollection(AipgReq.class, "trxData");
		xs.addImplicitCollection(AipgRsp.class, "trxData");
		xs.alias("BODY", Body.class) ;
		xs.alias("TRANS_DETAIL", Trans_Detail.class);
		xs.aliasField("TRANS_DETAILS", Body.class, "details");
		
		xs.alias("VALIDBREQ", ValidBReq.class) ;
		xs.alias("VALBSUM", ValbSum.class) ;
		xs.alias("VBDETAIL", VbDetail.class);
		xs.aliasField("VALIDBD", ValidBReq.class, "VALIDBD");
		xs.alias("VALIDR", ValidR.class) ;
		
		xs.alias("QTRANSREQ", TransQueryReq.class);
		xs.alias("QVDREQ", QVDReq.class);
		xs.alias("QTRANSRSP", QTransRsp.class);
		xs.alias("QTDETAIL", QTDetail.class);
		xs.alias("DOWNRSP", DownRsp.class);
		xs.alias("NOTIFY", Notify.class);
		xs.alias("SYNC", Sync.class);
		xs.alias("QSIGNRSP", QSignRsp.class);
		xs.alias("QSDETAIL", QSignDetail.class);
		xs.alias("NSIGNREQ", NSignReq.class);
		xs.alias("QSIGNREQ", QSignReq.class);
		xs.alias("TRANS", Trans.class);
		xs.alias("TRANSRET", TransRet.class);
		xs.alias("REVREQ", TransRev.class);
		xs.alias("REVRSP", TransRevRsp.class);
		xs.alias("LOGINRSP", LoginRsp.class);
		xs.alias("BALREQ", BalReq.class);
		xs.alias("BALRET", BalRet.class);
		xs.alias("SVRFREQ", SvrfReq.class);
		xs.alias("SCLOSEREQ", SvrfReq.class);
		xs.alias("SCLOSERSP", SCloseRsp.class);
		xs.alias("PINSIGNREQ", PinVerifyReq.class);
		xs.alias("PINSIGNRSP", PinVerifyRsp.class);
		
		//退款
		xs.alias("REFUND", Refund.class);
		xs.alias("ACCTTRANSFERREQ", AcctTransferReq.class);
		xs.alias("RNPA", Rnpa.class);
		xs.alias("RNPR", Rnpr.class);
		xs.alias("RNPC", Rnpc.class);
		xs.alias("RNPARET", RnpaRet.class);
		xs.alias("RNP", Rnp.class);
		
		xs.alias("TRFER", Trfer.class);
		xs.alias("TRFRET", Trfret.class);
		xs.alias("QPTRF", QPTrf.class);
		xs.alias("QPTRFRET", QPTrfret.class);
		xs.alias("QPTRANS", QPTrans.class);
		xs.alias("QPTRANSRET", QPTransRet.class);
		
		xs.addImplicitCollection(Sync.class, "details");
		xs.addImplicitCollection(QTransRsp.class, "details");
		xs.addImplicitCollection(QSignRsp.class, "details");
		xs.alias("SYNCDETAIL", SyncDetail.class);
		
		xs.alias("SYNCREQEX", SyncReqEx.class);
		xs.alias("SYNCRSPEX", SyncRspEx.class);
		xs.alias("SYNCREQEXDETAIL", SyncReqExDetail.class);
		xs.alias("SYNCRSPEXDETAIL", SyncRspExDetail.class);
		xs.addImplicitCollection(SyncReqEx.class, "details");
		xs.addImplicitCollection(SyncRspEx.class, "details");
		xs.alias("QTDREQ", QTDReq.class);
		xs.alias("QTDRSP", QTDRsp.class);
		xs.alias("QTDRSPDETAIL", QTDRspDetail.class);
		xs.addImplicitCollection(QTDRsp.class, "details");	
		xs.alias("ACQUERYREQ", AcQueryReq.class);
		xs.alias("ACQUERYREP", AcQueryRep.class);
		xs.alias("ACNODE", AcNode.class);
		xs.addImplicitCollection(AcQueryRep.class, "details");
		xs.alias("AHQUERYREQ", AHQueryReq.class);
		xs.alias("AHQUERYREP", AHQueryRep.class);
		xs.alias("BALNODE", BalNode.class);
		xs.addImplicitCollection(AHQueryRep.class, "details");
		xs.alias("TUNOTIFYREQ", TUNotifyReq.class);
		xs.alias("TUNOTIFYREP", TUNotifyRep.class);
		xs.alias("CASHREQ", CashReq.class);
		xs.alias("CASHREP", CashRep.class);
//		xs.alias("TUQNOTIFYREQ", NoticeReq.class);
//		xs.alias("TUNOTIFYREP", NoticeRep.class);
		xs.alias("ETQUERYREQ", EtQueryReq.class);
		xs.alias("ETQUERYREP", EtQueryRep.class);
		xs.alias("ETNODE", EtNode.class);
		xs.alias("ETQREQ", EtQReq.class);
		xs.addImplicitCollection(EtQueryRep.class, "details");
		xs.alias("SIGNINFODETAIL", SignInfoDetail.class);
		xs.alias("SIGNINFOSYNC", SignInfoSync.class);
		xs.alias("SCLOSEREQ", SCloseReq.class);
		xs.addImplicitCollection(SignInfoSync.class, "details");
		xs.alias("XQSIGNREQ", XQSignReq.class);

		/**
		 * 转账
		 */
		xs.alias("TRANSFERREQ",TransferReq.class);
	}
	public static InfoReq makeReq(String trxcod, String sn)
	{
		InfoReq ir=new InfoReq();
		ir.setTRX_CODE(trxcod);
		ir.setDATA_TYPE("2");
		ir.setVERSION("03");
		ir.setSIGNED_MSG("");
		ir.setREQ_SN(sn);
		ir.setLEVEL(null);
		ir.setUSER_NAME(null);
		ir.setUSER_PASS(null);
		return ir;
	}
	public static InfoReq makeReq(String trxcod, String sn,String user,String pass,int level)
	{
		InfoReq ir=new InfoReq();
		ir.setTRX_CODE(trxcod);
		ir.setDATA_TYPE("2");
		ir.setVERSION("03");
		ir.setSIGNED_MSG("");
		ir.setREQ_SN(sn);
		ir.setLEVEL(""+level);
		ir.setUSER_NAME(user);
		ir.setUSER_PASS(pass);
		return ir;
	}
	
	public static Object parseXml(String xml)
	{
		XStream xs=new XStreamIg();
		XSUtil.initXStream(xs,true);
		return xs.fromXML(xml);
	}
}
