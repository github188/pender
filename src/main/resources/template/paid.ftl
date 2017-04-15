<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>新的發貨訂單</title>
</head>
<body>
	<br /><br /><br />
	<table style="margin-left:auto;margin-right:auto;cellpadding:0;cellspacing:0,border:0;width:720px;padding:20px;border:1px solid green;"><tbody>
		<tr><td><h1 style="color:green;font-weight:bold;">自游邦旅遊購物平台</h1></td></tr>
		<tr><td><div style="width:700px;height:5px;margin:0px auto;padding:0px;background-color:green;overflow:hidden;"></div></td></tr>	
		<tr><td style="height:20px;"></td></tr>	
		<tr><td><p>${orgName}：</p></td></tr>
		<tr>
			<td><table style="width:700px;margin-top:0px;margin-right:auto;margin-bottom:auto;margin-left:auto;cellpadding:0;cellspacing:0;font-size:14px;border:green solid 1px;">			
			<#assign orderNo="" />
			<#list details as detail>
				<#if (orderNo != "${detail.orderNo}")>
					<#assign orderNo = "${detail.orderNo}"/>
					<tr><td colspan="4" style="font-weight:bold;color:green">訂單號：${detail.orderNo}</td></tr>
					<tr><td colspan="4" style="font-weight:bold;color:green">收貨人：${detail.consignee}&nbsp;&nbsp;${detail.idCard}</td></tr>
					<tr><td colspan="4" style="font-weight:bold;color:green">電話：${detail.phone}&nbsp;&nbsp;郵編：${detail.zip!}</td></tr>
					<tr><td colspan="4" style="font-weight:bold;color:green">地址：${detail.address}</td></tr>
					<tr><td style="width:220px;color:green">SKU</td><td style="width:250px;color:green">商品名稱</td><td style="width:50px;color:green">數量</td><td style="width:180px;color:green">規格</td></tr>
				</#if>
				<tr><td>${detail.sku}</td><td>${detail.skuName}</td><td>${detail.qty}</td><td>${detail.productModel!}</td></tr>
			</#list>
			</table></td>
		</tr>
		<tr>
			<td><p>想查詢更多詳情，請訪問&nbsp;<a href="http://admin.ziyoubang.cn" target="_self">自游邦商家管理後台</a></p></td>
		</tr>
	</tbody></table>
</body>
</html>