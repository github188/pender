<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>我的主题库</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<style type="text/css">
	html {height: 100%;}
	body {margin: 0; height: 100%;}
	.theme-ctn {margin: 10px;}
	.theme-ctn .header {padding: 0 10px; line-height: 35px; font-size: 15px; color: #fff; background-color: #358491;}
	.theme-ctn .body {padding: 10px; border-width: 0 1px 1px 1px; border-style: solid; border-color: #e5e5e5;}
	.theme-ctn .u-btn {float: right; line-height: 20px; margin-top: 6px; color: #fff; border-color: #fff;}
	.theme-item {float: left; width: 145px; margin: 19px; border-radius: 5px; border: 1px solid #e5e5e5;}
	.theme-item img {display: block; width: 100%; height: 145px; border-radius: 4px 4px 0 0; border-bottom: 1px solid #eee;}
	.theme-item .name {white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-size: 13px; margin: 2px 7px;}
	.theme-item .btns {font-size: 12px; color: #999; text-align: center; margin: 3px 0;}
	.theme-item .btns span {margin-right: 13px; cursor: pointer;}
	.theme-item .btns span:last-child {margin-right: 0;}
	.theme-item .btns span:hover {color: #333;}
</style>
</head>
<body>
	<div style="height:100%;overflow: auto;">
		<div class="theme-ctn">
			<div class="header">
				<span>横屏主题</span>
				<span class="u-btn" onclick="addTheme(0)">新增横屏主题</span>
			</div>
			<div id="h-theme" class="body clearfix"></div>
		</div>
		<div class="theme-ctn">
			<div class="header">
				<span>竖屏主题</span>
				<span class="u-btn" onclick="addTheme(1)">新增竖屏主题</span>
			</div>
			<div id="v-theme" class="body clearfix"></div>
		</div>
	</div>
	<div id="winTheme" class="easyui-dialog" data-options="closed:true,onClose:resetUpload,buttons:'#themeBtns'" style="width:400px;height:450px;padding:15px;">
		<form id="formTheme">
			<input id="themeId" type="hidden" name="id">
			<input id="themeType" type="hidden" name="themeType">
			<div class="form-row">
				<div class="form-cloumn form-item" style="width:100%;">
					<label class="text" style="margin-right:10px;">主题名称</label>
					<input class="easyui-textbox" data-options="required:true,prompt:'请输入主题名称'" name="themeName">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item" style="width:100%;">
					<label class="text" style="margin-right:10px;vertical-align:top;">预览图片</label>
					<iframe id="themeImg" width="162px" height="180px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initImageUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item" style="width:100%;">
					<label class="text" style="margin-right:10px;vertical-align:top;">主题文件</label>
					<iframe id="themeFile" width="250px" height="80px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initCompresserUpload(this)" src="${pageContext.request.contextPath}/free/uploadCompresser.html"></iframe>
				</div>
			</div>
		</form>
		<div id="themeBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveTheme()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winTheme');">取消</a>
		</div>
	</div>
	<script id="theme-item-tpl" type="text/html">
		{{each themes as theme i}}
			<div class="theme-item">
				<img src="{{theme.thumb}}">
				<div class="name">
					{{if theme.defaultTheme == 0}}
					<span style="color:#009cd3;font-size:12px;">(默认)</span>
					{{/if}}
					<span>{{theme.themeName}}</span>
				</div>
				<div class="btns">
					{{if theme.defaultTheme == 0}}
					<span class="auto">取消默认</span>
					{{else}}
					<span class="auto">设为默认</span>
					{{/if}}
					<span class="edit">编辑</span>
					<span class="del">删除</span>
				</div>
			</div>
		{{/each}}
	</script>
	<script type="text/javascript">
		var key;

		//初始化上传
		function initImageUpload(m) {
			m.contentWindow.initImageUploader(function() {
				if ($('#themeId').val() != '') {
					doSaveTheme();
				}
			}, _token, 1);
		}
		function initCompresserUpload(m) {
			m.contentWindow.initUploader(doSaveTheme, _token);
		}

		//载入图片
		function showImage(iframe, image) {
			var fileIds = [], fileNames = [], realPaths = [];
			var vals = image.split(',');
			fileIds.push(vals[0]);
			fileNames.push(vals[1]);
			realPaths.push(vals[3]);
			iframe.contentWindow.loadUploadImage(fileIds, fileNames, realPaths);
		}

		//新增主题
		function addTheme(type) {
			key = +(new Date);
			$('#themeId').val('');
			$('#themeType').val(type);
			$('#formTheme .form-row:last-child').show();
			openWin('winTheme', '新增'+(type ? '竖屏' : '横屏')+'主题');
		}

		//保存主题
		function saveTheme() {
			if (!$('#formTheme').form('enableValidation').form('validate')) return;

			var id = $('#themeId').val();
			var imgWin = $('#themeImg')[0].contentWindow;
			var fileWin = $('#themeFile')[0].contentWindow;

			if (id && imgWin.queueImages.length == 0) {//编辑、无缩略图上传
				doSaveTheme();
			}

			if (id && imgWin.queueImages.length != 0) {//编辑、有缩略图上传
				imgWin.uploadImages({key:key ,module:16});
			}

			if (!id) {//新建
				if (fileWin.uploader.getFiles('inited').length == 0) {
					infoMsg('请上传主题文件');
					return;
				}
				if (imgWin.queueImages.length != 0) {
					imgWin.uploadImages({key:key ,module:16});
				}
				fileWin.uploadCompresser({key:key ,module:17});
			}
		}
		function doSaveTheme() {
			var theme = $('#formTheme').getValues();
			theme.key = key;
			theme.fileIds = $('#themeImg')[0].contentWindow.delImageIds;
			$.ajax({
				url: '${pageContext.request.contextPath}/interaction/theme/saveSkinEntity.json',
				data: $.param(theme, true),
				info: '保存成功！',
				task: function(r) {
					closeWin('winTheme');
					getThemes();
				}
			})
		}

		function resetUpload() {
			$('#themeFile')[0].contentWindow.resetUpload();
			$('#themeImg')[0].contentWindow.closeImageUploader();
		}

		function getThemes() {
			$.ajax({
				url: '${pageContext.request.contextPath}/interaction/theme/findThemeSkinList.json',
				task: function(data) {
					var themes = data.rows;
					$.each(themes, function(i, t) {
						if (t.images) {
							t.thumb = getFileUrl(t.images.split(',')[3]);
						}
					})

					var wrap = {};

					wrap.themes = themes.filter(function(t) {
						return t.themeType == 0;
					}).sort(function(a, b) {
						return a.defaultTheme - b.defaultTheme;
					})
					$('#h-theme').html(template('theme-item-tpl', wrap));
					$('#h-theme .theme-item').each(function(i) {
						$(this).data('theme', wrap.themes[i]);
					})

					wrap.themes = themes.filter(function(t) {
						return t.themeType == 1;
					}).sort(function(a, b) {
						return a.defaultTheme - b.defaultTheme;
					})
					$('#v-theme').html(template('theme-item-tpl', wrap));
					$('#v-theme .theme-item').each(function(i) {
						$(this).data('theme', wrap.themes[i]);
					})
				}
			})
		}

		$(function() {
			$(document).on('click', '.edit', function() {
				key = +(new Date);
				var theme = $(this).parents('.theme-item').data('theme');
				$('#formTheme .form-row:last-child').hide();
				openWin('winTheme', theme.themeName);
				$('#formTheme').form('load', theme);
				if (theme.images) {
					showImage($('#themeImg')[0], theme.images);
				}
			})

			$(document).on('click', '.del', function() {
				var id = $(this).parents('.theme-item').data('theme').id;
				confirmMsg('确定删除该主题吗？', function() {
					$.ajax({
						url: '${pageContext.request.contextPath}/interaction/theme/deleteThemeSkin.json',
						data: {themeId: id},
						task: function() {
							getThemes();
						}
					})
				})
			})

			$(document).on('click', '.auto', function() {
				var id = $(this).parents('.theme-item').data('theme').id;
				confirmMsg('确定将该主题设为默认主题吗？', function() {
					$.ajax({
						url: '${pageContext.request.contextPath}/interaction/theme/updateThemeSkinDefaultTheme.json',
						data: {themeId: id},
						task: function() {
							getThemes();
						}
					})
				})
			})

			getThemes();
		})
	</script>
</body>
</html>