package com.vendor.thirdparty.tl.aipg.qtd;

import java.util.ArrayList;
import java.util.List;

import com.vendor.thirdparty.tl.aipg.transquery.QTDetail;

public class QTDRsp {
	private String PGTAG;
	private List details = new ArrayList();
	
	public String getPGTAG() {
		return PGTAG;
	}
	public void setPGTAG(String pGTAG) {
		PGTAG = pGTAG;
	}
	public List getDetails() {
		return details;
	}
	public void setDetails(List details) {
		this.details = details;
	}	
	public void addDtl(QTDRspDetail dtl)
	{
		if(details==null) details=new ArrayList();
		details.add(dtl);
	}
}
