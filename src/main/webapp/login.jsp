<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>自游邦运营平台</title>
<style type="text/css">
html, body {height: 100%;}
body {margin: 0; background-color: #467ba9; font-family: "Microsoft YaHei","Helvetica Neue",Helvetica,Arial,sans-serif;}
.logo {height: 110px; display: block; margin: 0 auto; margin-top: -110px;}
.login-wrapper {display: table; width: 100%; height: 100%;}
.login-inner {display: table-cell; vertical-align: middle;}
.login-body .title {color: #fff; font-size: 28px; text-align: center;}
.login-body .body {box-sizing: border-box; width: 420px; margin: 20px auto 0; padding: 40px 50px; border-radius: 4px; background-color: rgba(255,255,255,.4);}
.login-body .body .row {position: relative; margin-bottom: 15px;}
.login-body .body .row .icon {position: absolute; left: 0; top: 0; height: 100%; width: 34px; background-repeat: no-repeat; background-position: center;}
.login-btn {width: 130px; margin: 10px auto 0; border-radius: 4px; line-height: 2; text-align: center; color: #333; cursor: pointer; background-color: rgba(255,255,255,.7);}
.login-btn:hover {background-color: rgba(255,255,255,.9);}
.login-error {font-size: 15px; color: red; height: 18px; line-height: 18px; margin-top: 6px;}
.clear-cache {float: right; margin-top: 11px; font-size: 15px; color: #555; cursor: pointer;}
.clear-cache:hover {color: #222;}
.input-ctrl {box-sizing: border-box; display: block; width: 100%; height: 34px; padding: 6px 12px 6px 34px; font-size: 14px; line-height: 1.5; color: #555; background-color: #fff; border: 1px solid #eee; border-radius: 4px; outline: none;}
.tip-box {text-align: center; color: rgba(255,255,255,.7); font-size: 13px;}
.tip-box img {vertical-align: middle;}
</style>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo_12.png"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/jquery.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.indexeddb.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/cache.js"></script>
<script type="text/javascript">
$(function() {
	if ('${param.error}') {
		var msg = '${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}';
		if (msg.indexOf('Maximum sessions') != -1) {
			$('#divError').text('当前用户已登录！');
		} else if (msg.indexOf('用户已失效') != -1) {
			$('#divError').text('用户已锁定，请联系管理员！');
		} else {
			$('#divError').text('用户名或密码错误！');
		}
	} else {
		$('#divError').text('');
	}
	if ('${param.user}') {
		$('#txtUser').val('${param.user}');
		$('#txtPass').focus();
	}
	$(window).keydown(function(event) {
		if (event.keyCode == 13)
			login();
	});
});
function login() {
	if ($.trim($('#txtUser').val()) == '') {
		$('#divError').text('请输入用户名！');
	} else if ($('#txtPass').val() == '') {
		$('#divError').text('请输入密码！');
	} else {
		$('#loginForm').submit();
	}		
}
function clearData() {
	if (confirm('您确定要清空本地数据？')) {
		clearCacheDataByCache();
		alert('清空本地数据成功！');
	}
}
</script>
</head>
<body>
	<div class="login-wrapper">
		<div class="login-inner">
			<div class="login-body">
				<img src="images/logo.png" class="logo">
				<div class="title">邦马特智能零售终端管理系统</div>
				<div class="body">
					<form id="loginForm" action="${pageContext.request.contextPath}/login" method="post">
						<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
						<div class="row">
							<div class="icon" style="background-image:url(images/icon-user.png);"></div>
							<input type="text" id="txtUser" name="username" class="input-ctrl" placeholder="请输入登录账号">
						</div>
						<div class="row" style="margin-bottom:0;">
							<div class="icon" style="background-image:url(images/icon-password.png);"></div>
							<input type="password" id="txtPass" name="password" class="input-ctrl" placeholder="请输入登录密码">
						</div>
						<div id="divError" class="login-error"></div>
						<div  class="clear-cache" onclick="clearData()">清空缓存</div>
						<div class="login-btn" onclick="login()">登 录</div>
					</form>
				</div>
			</div>
			<div class="tip-box" style="margin-top:15px;">深圳市自游邦信息科技有限公司&copy;2015 - 2025 All Rights Reserved.</div>
			<div class="tip-box" style="margin-top:5px;">本系统支持浏览器：<img src="images/ie.png">IE 11+，&nbsp;<img src="images/firefox.png">FireFox 36+，&nbsp;推荐使用 <img src="images/chrome.png">Chrome 41+ 获取最佳体验</div>
		</div>
	</div>
</body>
</html>
