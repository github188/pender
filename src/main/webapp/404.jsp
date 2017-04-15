<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>自游邦管理平台</title>
<style type="text/css">
#error {
	font-size:18px;
	color:#29566b;
	background-image: url(${pageContext.request.contextPath}/images/error_page.png);
	background-repeat: no-repeat;
	height:390px;
	width:700px;
	margin-top:10%;
	margin-right: auto;
	margin-bottom: auto;
	margin-left: auto;
}
.back_page{margin-right:14px;line-height:32px;display:inline-block;padding:0px 26px;border-radius:4px;background:#4d9358;font-size:15px;text-decoration:none;color:#ffffff;border:1px solid #478a52;}
</style>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo_12.png"/>
</head>
<body>
	<div id="error"><table style="width:700px;">
		<tr><td colspan="3" style="height:60px"></td></tr>
		<tr><td style="width:20px"></td><td align="left" valign="top" style="width:360px;height:120px"></td><td></td></tr>
		<tr><td colspan="2"><td style="height:20px"><strong>您访问的页面不存在</strong></td></tr>
	</table></div>	
</body>
</html>