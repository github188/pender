<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>登录</title>
    <meta http-equiv="Content-Type" content="text/html; charset=gb2312">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1,user-scalable=no">
    <meta name="format-detection" content="telephone=no" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/wechat/css/app.css" />
</head>
<body>
	<div id="login" class="login">
		<div id="error" class="error"></div>
		<img class="logo" src="img/logo.png">
        <form id="loginForm" class="form-area" onsubmit="return false;" method="POST" action="${pageContext.request.contextPath}/login">
            <div class="form-item name">
                <div class="icon"></div>
                <div class="del"></div>
                <input id="name" class="form-ctrl" type="text" name="username">
            </div>
            <div class="form-item password">
                <div class="icon"></div>
                <div class="del"></div>
                <input id="password" class="form-ctrl" type="password" name="password">
            </div>
        </form>
        <div class="tip">温馨提示：加入邦马特即可获取账号</div>
        <div id="loginBtn" class="login-btn">登 录</div>
	</div>
	<script type="text/javascript" src="${pageContext.request.contextPath}/wechat/lib/jquery.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/wechat/js/common.js"></script>
	<script type="text/javascript">
		$(function() {

			if ('${param.error}') {
                var msg = '${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}';
                if (msg.indexOf('Maximum sessions') != -1) {
                    $('#error').html('当前用户已登录！').show().fadeOut(1500);
                } else if (msg.indexOf('用户已失效') != -1) {                   
                    $('#error').html('用户已锁定，请联系管理员！').show().fadeOut(1500);
                } else {             
                    $('#error').html('用户名或密码错误！').show().fadeOut(3000);
                }
            }
        	if ('${param.user}') {
                $('#name').val('${param.user}');
                $('#password').focus();
            }

			$('#loginForm input').on('input', function() {
				var $name = $('#name');
				var $password = $('#password');
				$loginBtn = $('#loginBtn');
				if ($name.val() !== '') {
					$name.addClass('active').siblings('.icon').addClass('active');
				} else {
					$name.removeClass('active').siblings('.icon').removeClass('active');
				}
				if ($password.val() !== '') {
					$password.addClass('active').siblings('.icon').addClass('active');
				} else {
					$password.removeClass('active').siblings('.icon').removeClass('active');
				}
			})

			$('#loginForm .del').click(function() {
				$(this).siblings('input').val('').removeClass('active').siblings('.icon').removeClass('active');
			})

			$('#loginBtn').click(function() {
				if ($('#name').val() == '') {
					WE.showTip('请输入用户名！');
				} else if ($('#password').val() == '') {
					WE.showTip('请输入密码！');
				} else {
					document.getElementById('loginForm').submit();
				}
			})

		})
	</script>
</body>
</html>