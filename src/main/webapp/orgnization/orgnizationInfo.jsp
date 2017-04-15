<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>组织信息</title>
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
		.reply {color: #009cd3; cursor: pointer;}
		.u-btn {margin-right: 0;}
		.reply-row {margin: 15px; font-size: 14px;}
		.reply-row label {display: inline-block; width: 75px; color: #666;}
		.reply-row span {color: #000;}
	</style>
</head>
<body style="margin:0; padding:15px;">
	<div class="info-ctn">
		<div class="easyui-panel" title="基本组织信息" style="padding:30px; margin-bottom:30px;">
			<ul class="info-list">
				<li class="info-item">
					<label>平台组织编号：</label>
					<span class="content" id="code"></span>
				</li>
				<li class="info-item">
					<label>平台组织别名：</label>
					<span class="content" id="alias"></span>
					<input class="input" type="text" name="alias">
					<span class="edit">修改</span>
					<span class="btns">
						<i class="u-btn confirm">确认</i>
						<i class="u-btn cancel">取消</i>
					</span>
				</li>
				<li class="info-item">
					<label>组织类型：</label>
					<span class="content" id="orgType"></span>
				</li>
				<li class="info-item">
					<label>合作起始时间：</label>
					<span class="content" id="settledTime"></span>
				</li>
				<li class="info-item">
					<label>管理组织：</label>
					<span class="content" id="parentName"></span>
				</li>
			</ul>
		</div>
	</div>
	<div class="info-ctn">
		<div class="easyui-panel" title="联系信息" style="padding:30px;">
			<ul class="info-list">
				<li class="info-item">
					<label>联系人：</label>
					<span class="content" id="manager"></span>
					<input class="input" type="text" name="manager">
					<span class="edit">修改</span>
					<span class="btns">
						<i class="u-btn confirm">确认</i>
						<i class="u-btn cancel">取消</i>
					</span>
				</li>
				<li class="info-item" style="width:66.66%;">
					<label>联系电话：</label>
					<span class="content" id="phone"></span>
					<input class="input" type="text" name="phone">
					<span class="edit">修改</span>
					<span class="btns">
						<i class="u-btn confirm">确认</i>
						<i class="u-btn cancel">取消</i>
					</span>
				</li>
			</ul>
		</div>
	</div>
	<div id="winReply" class="easyui-dialog" data-options="closed:true,buttons:'#replyBtns'" style="padding:7px;width:330px;">
		<div class="reply-row"><label>发起方：</label><span></span></div>
		<div class="reply-row"><label>发起时间：</label><span></span></div>
		<div id="replyBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)">同意</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)">拒绝</a>
		</div>
	</div>
	<script type="text/javascript">
		$(function() {
			function telNumVaild(str) {
				var str = $.trim(str);
				return str.length > 6 && str.length < 13 && +str === +str;
			}

			$(document).on('click', '.edit', function() {
				var $this = $(this);
				$this.hide().siblings('.content').hide();
				$this.siblings('.input, .btns').show();
				$this.siblings('.input').val($this.siblings('.content').html());
			})
			$(document).on('click', '.confirm, .cancel', function() {
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

					if (paramKey === 'tel') {
						if (!telNumVaild(paramVal)) {
							infoTip('联系电话长度或格式不正确');
							return;
						}
					}

					var param = {};
					param[paramKey] = paramVal;
					$.post('${pageContext.request.contextPath}/orgnization/orgnizationInfo/saveCurrentOrgnization.json', param, function(r) {
						if (r.orgnization) {
							$btns.siblings('.content').html(r.orgnization[paramKey]);
							viewChange();
							infoTip('修改成功！');
						}
					})
				}
			})
			$(document).on('keyup', '.input', function(e) {
				if (e.keyCode === 13) {
					$(this).siblings('.btns').children('.confirm').trigger('click');
				}
			})

			$(document).on('click', '.reply', function() {
				openWin('winReply', '建立管理关系');
			})

			$('#replyBtns').on('click', '.easyui-linkbutton', function() {
				var index = $(this).index();
				var applyRelate = index == 0 ? 3 : 2;
				$.ajax({
					url: '${pageContext.request.contextPath}/orgnization/orgnizationInfo/saveOrgRelation.json',
					data: {applyRelate: applyRelate},
					task: function() {
						closeWin('winReply');
						if (applyRelate == 3) {
							$('#parentName').html($('#parentName').children('.orgName'));
						} else {
							$('#parentName').html('无');
						}
					}
				})
			})

			$.post('${pageContext.request.contextPath}/orgnization/orgnizationInfo/findCurrentOrgnization.json', function(r) {
				var org = r.orgnization;
				if (org) {
					$('#code').html(org.code);
					$('#alias').html(org.alias);
					$('#orgType').html(org.orgType == 1 ? '管理者' : '经营者');
					$('#settledTime').html(org.settledTime.slice(0,10).replace('-', '年').replace('-', '月') + '日');
					$('#manager').html(org.manager);
					$('#phone').html(org.phone);
					if (org.isRelate == 1) {
						$('#parentName').html(org.parentName || '无');
					} else {
						if (org.applyRelate == 1) {
							$('#parentName').html('<span class="orgName">' + org.parentName + '</span>申请建立管理关系，<span class="reply">请点击查看详情</span>');
							var $rows = $('#winReply .reply-row');
							$rows.eq(0).children('span').html(org.parentName);
							$rows.eq(1).children('span').html(org.applyTime);
						} else {
							$('#parentName').html('无');
						}
					}
				}
			})
		})
	</script>
</body>
</html>