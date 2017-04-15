<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>个人信息</title>
	<%@ include file="/common.jsp"%>
	<style type="text/css">
		.info-ctn .panel-header {background-color: #009cd3; border-color: #009cd3;}
		.info-ctn .panel-title {height: 30px; line-height: 30px; font-size: 16px; color: #fff; text-indent: 10px; font-weight: normal;}
		.info-ctn .panel-body {border-color: #009cd3;}
		.info-list {font-size: 0; padding-left: 0;}
		.info-item {display: inline-block; width: 33.33%; margin: 10px 0; font-size: 14px;}
		.info-item .content {color: #000; font-weight: bold;}
		.info-item input {display: none; width: 120px; outline: none;}
		.info-item .edit {color: #169BD5; margin-left: 10px; cursor: pointer;}
		.info-item .edit:hover {text-decoration: underline;}
		.info-item .btns {display: none;}
		.password-reset-row {text-align: right; margin: 10px 0;}
		.u-btn {margin-right: 0;}
	</style>
</head>
<body style="margin:0; padding:15px;">
	<div class="info-ctn">
		<div class="easyui-panel" title="账号信息" style="padding:30px; margin-bottom:30px;">
			<ul class="info-list">
				<li class="info-item">
					<label>登录账户：</label>
					<span class="content" id="userName"></span>
					<span class="edit" onclick="openDialog('winPassword')">修改密码</span>
				</li>
				<li class="info-item">
					<label>账号角色：</label>
					<span class="content" id="roleName"></span>
				</li>
				<li class="info-item">
					<label>创建时间：</label>
					<span class="content" id="createTime"></span>
				</li>
			</ul>
		</div>
	</div>
	<div class="info-ctn">
		<div class="easyui-panel" title="基本信息" style="padding:30px;">
			<ul id="baseMes" class="info-list">
				<li class="info-item">
					<label>姓名：</label>
					<span class="content" id="realName"></span>
					<input class="input" type="text" name="realName">
					<span class="edit">修改</span>
					<span class="btns">
						<i class="u-btn confirm">确认</i>
						<i class="u-btn cancel">取消</i>
					</span>
				</li>
				<li class="info-item">
					<label>联系电话：</label>
					<span class="content" id="mobile"></span>
					<input class="input" type="text" name="mobile">
					<span class="edit">修改</span>
					<span class="btns">
						<i class="u-btn confirm">确认</i>
						<i class="u-btn cancel">取消</i>
					</span>
				</li>
			</ul>
		</div>
	</div>
	<div id="winPassword" class="easyui-dialog" data-options="closed:true,buttons:'#resetBtns'" title="修改密码" style="padding:10px 30px;">
		<form id="fomPassword" method="post">
			<div class="password-reset-row">
				原密码：<input id="oldPassword" class="easyui-textbox" type="password" name="oldPassword" data-options="required:true">
			</div>
			<div class="password-reset-row">
				新密码：<input id="password" class="easyui-textbox" type="password" name="password" data-options="required:true,validType:['length[6,16]','noBlank','allNum']">
			</div>
			<div class="password-reset-row">
				确认新密码：<input class="easyui-textbox" type="password" data-options="required:true" validType="equals['#password']">
			</div>
		</form>
		<div id="resetBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="updatePassword();">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winPassword');">取消</a>
		</div>
	</div>
	<script type="text/javascript">
		$.extend($.fn.validatebox.defaults.rules,{
		    equals: {
		        validator: function(value, param) {
		            return value == $(param[0]).val();  
		        },  
		        message: '两次输入密码不一致！'  
		    },
		    allNum: {
		    	validator: function(value) {
		    		if (+value === +value) {
		    			return value.length >= 9;
		    		} else {
		    			return true;
		    		}
		    	},
		    	message: '纯数字密码不得少于9位'
		    },
		    noBlank: {
		    	validator: function(value) {
		    		return value.indexOf(' ') === -1;
		    	},
		    	message: '密码不能包含空格'
		    }
		});

		function updatePassword() {
			postForm({
				form:'fomPassword',
				url:'${pageContext.request.contextPath}/system/user/savePassword.json',
				info:'密码修改成功！'
			});
		}

		$(function() {
			function telNumVaild(str) {
				var str = $.trim(str);
				return str.length > 6 && str.length < 13 && +str === +str;
			}

			$('#baseMes').on('click', '.edit', function() {
				var $this = $(this);
				$this.hide().siblings('.content').hide();
				$this.siblings('.input, .btns').show();
				$this.siblings('.input').val($this.siblings('.content').html());
			})
			$('#baseMes').on('click', '.confirm, .cancel', function() {
				var $this = $(this),
					$btns = $this.parent(),
					$input = $btns.siblings('.input');

				var viewChange = function() {
					$btns.hide().siblings('.input').hide().val('');
					$btns.siblings('.content, .edit').show();
				}

				if ($this.hasClass('cancel')) {
					viewChange();
				} else {
					var paramKey = $input.attr('name');
					var paramVal = $input.val();

					if ($.trim(paramVal) === '') {
						infoTip('不能为空');
						return;
					}

					if (paramKey === 'mobile') {
						if (!telNumVaild(paramVal)) {
							infoTip('联系电话长度或格式不正确');
							return;
						}
					}
					
					var param = {};
					param[paramKey] = paramVal;
					$.post('${pageContext.request.contextPath}/free/saveCurrentUser.json', param, function(r) {
						if (r.user) {
							$btns.siblings('.content').html(r.user[paramKey]);
							viewChange();
							infoTip('修改成功！');
						}
					})
				}
			})
			$('#baseMes').on('keyup', '.input', function(e) {
				if (e.keyCode === 13) {
					$(this).siblings('.btns').children('.confirm').trigger('click');
				}
			})

			$.post('${pageContext.request.contextPath}/free/findCurrentUser.json', function(r) {
				if (r.user) {
					$('#userName').html(r.user.username);
					$('#roleName').html(r.user.roleName || '超级管理员');
					$('#createTime').html(r.user.createTime);
					$('#realName').html(r.user.realName);
					$('#mobile').html(r.user.mobile);
				}
			})
		})
	</script>
</body>
</html>