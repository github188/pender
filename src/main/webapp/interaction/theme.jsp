<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>主题应用</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<style type="text/css">
	.list-head .panel-tool {margin-top: -12px; height: 24px;}
	.theme-list {width: 828px; margin: 8px auto 0;}
	.theme-item {float: left; width: 120px; border: 1px solid #e5e5e5; border-radius: 4px; margin: 8px;}
	.theme-item.active {border-color: #cc2222;}
	.theme-item img {display: block; width: 100%; height: 120px; border-radius: 4px 4px 0 0; border-bottom: 1px solid #eee; cursor: pointer;}
	.theme-item .name {white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-size: 13px; margin: 2px 7px;}
	.t-btn {font-size: 13px; color: #009cd3; cursor: pointer;}
	.t-btn:hover {text-decoration: underline;}
</style>
</head>
<body class="easyui-layout">
   <div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="deviceQueryForm" class="search-form">
			<div class="form-item">
				<div class="text">设备组号</div>
				<div class="input">
					<input name="factoryDevNo" class="easyui-textbox" data-options="prompt:'设备编号'" />
				</div>
			</div>
			<div class="form-item">
				<div class="text">所属店铺名称</div>
				<div class="input">
					<input name="pointName" class="easyui-textbox" data-options="prompt:'所属店铺名称'" />
				</div>
			</div>
			<div class="form-item">
				<div class="text">所属店铺地址</div>
				<div class="input">
					<input name="pointAddress" class="easyui-textbox" data-options="prompt:'所属店铺地址'" />
				</div>
			</div>
			<div class="form-item">
				<div class="text">执行状态</div>
				<div class="input">
					<select name="executingState" class="easyui-combobox" data-options="editable:false,panelHeight:'auto'">
						<option value="">请选择</option>
						<option value="0">未执行</option>
						<option value="1">执行中</option>
						<option value="3">执行成功</option>
						<option value="4">执行失败</option>
						<option value="2">已取消</option>
					</select>
				</div>
			</div>
			<div class="form-item">
				<div class="text">屏幕类型</div>
				<div class="input">
					<select name="themeType" class="easyui-combobox" data-options="editable:false,panelHeight:'auto'">
						<option value="">请选择</option>
						<option value="0">横屏</option>
						<option value="1">竖屏</option>
					</select>
				</div>
			</div>
		</form>
		<sec:authorize access="find">
		<div class="search-btn" onclick="queryData('deviceGrid','deviceQueryForm')">查询</div>
		<div class="search-btn" onclick="resetForm('deviceQueryForm')">重置</div>
		</sec:authorize>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#deviceOpt'" title="设备列表" style="padding:10px;">
		<div id="deviceOpt">
			<span class="u-btn" onclick="pushDevToQueue()">添加至队列</span>
			<span class="u-btn" onclick="themePublish()">主题发布队列</span>
			<span class="u-btn" onclick="parent.addOrSelectTab(this)" data-title="我的主题库" data-href="${pageContext.request.contextPath}/interaction/themeLib.jsp">我的主题库</span>
		</div>
		<table id="deviceGrid" class="easyui-datagrid" data-options="fit:true,fitColumns:true,striped:true,idField:'factoryDevNo'">
			<thead>
				<tr>
					<th data-options="checkbox:true,field:'',width:20"></th>
					<th data-options="field:'factoryDevNo',width:80,align:'center'">设备组号</th>
					<th data-options="field:'pointName',width:150,align:'center'">所属店铺名称</th>
					<th data-options="field:'pointAddress',width:150,align:'center'">所属店铺地址</th>
					<th data-options="field:'themeName',width:150,align:'center'">当前主题名称</th>
					<th data-options="field:'toenableName',width:150,align:'center'">待执行主题名称</th>
					<th data-options="field:'startTime',width:120,align:'center'">开始时间</th>
					<th data-options="field:'executingState',width:60,align:'center',formatter:formatExecState">执行状态</th>
					<th data-options="field:'deviceStatus',width:60,align:'center',formatter:formatDevState">设备状态</th>
					<th data-options="field:'operate',width:50,align:'center',formatter:formatThemeOper">操作</th>
				</tr>
			</thead>
		</table>
	</div>
	<div id="winThemePub" class="easyui-dialog" data-options="closed:true,buttons:'#themePubBtns'" title="主题发布队列" style="width:1000px;height:650px;padding:10px;">
		<form id="formThemePub">
			<div class="form-row" style="margin-top:0;">
				<div class="form-cloumn form-item" style="width:200px;">
					<div class="text">开始时间</div>
					<input id="startTime" name="startTime" class="easyui-datetimebox" data-options="required:true,prompt:'开始时间',editable:false">
				</div>
				<div class="form-cloumn form-item" style="width:280px;">
					<div class="text">横屏主题</div>
					<input id="horizontalTheme" class="easyui-textbox" data-options="prompt:'横屏主题',readonly:true">
					<input type="hidden" name="hThemeId">
					<span class="u-btn" style="margin-right:0;" onclick="selectTheme(0)">选择</span>
					<span class="u-btn" onclick="clearTheme(0)">清空</span>
				</div>
				<div class="form-cloumn form-item" style="width:280px;">
					<div class="text">竖屏主题</div>
					<input id="verticalTheme" class="easyui-textbox" data-options="prompt:'竖屏主题',readonly:true">
					<input type="hidden" name="vThemeId">
					<span class="u-btn" style="margin-right:0;" onclick="selectTheme(1)">选择</span>
					<span class="u-btn" onclick="clearTheme(1)">清空</span>
				</div>
				<div class="u-btn" style="float:right;margin-top:30px;" onclick="delDevFormQueue()">移除所有</div>
			</div>
		</form>
		<table id="devicePubGrid" data-options="fitColumns:true,striped:true,pagination:false,idField:'factoryDevNo'" style="height:497px;">
			<thead>
				<tr>
					<!-- <th data-options="checkbox:true,field:'',width:20"></th> -->
					<th data-options="field:'factoryDevNo',width:80,align:'center'">设备编号</th>
					<th data-options="field:'pointName',width:150,align:'center'">所属店铺名称</th>
					<th data-options="field:'pointAddress',width:150,align:'center'">所属店铺地址</th>
					<th data-options="field:'pointType',width:80,align:'center',formatter:formatPointType">所属店铺类型</th>
					<th data-options="field:'themeName',width:150,align:'center'">当前主题名称</th>
					<th data-options="field:'deviceStatus',width:50,align:'center',formatter:formatDevState">设备状态</th>
					<th data-options="field:'operate',width:50,align:'center',formatter:formatDelDev">操作</th>
				</tr>
			</thead>
		</table>
		<div id="themePubBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveThemeQueue()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winThemePub');">取消</a>
		</div>
	</div>
	<div id="winThemeList" class="easyui-dialog" data-options="closed:true,buttons:'#themeBtns'" style="width:870px;height:550px;">
		<div class="theme-list clearfix" style="display:none;"></div>
		<div class="theme-list clearfix" style="display:none;"></div>
		<div id="themeBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="doSelectTheme()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winThemeList');">取消</a>
		</div>
	</div>
	<script id="theme-item-tpl" type="text/html">
		{{each themes as theme i}}
		<div class="theme-item" data-id="{{theme.id}}" data-type="{{theme.themeType}}" data-name="{{theme.themeName}}">
			<img src="{{theme.thumb}}">
			<div class="name">{{theme.themeName}}</div>
		</div>
		{{/each}}
	</script>
	<script type="text/javascript">
		var pointTypes = [];

		//formatter
		function formatDelDev(val, row) {
			return '<a href="javascript:void(0)" class="easyui-tooltip icon-remove" style="display:inline-block;width:16px;height:16px;cursor:pointer;" onclick="delDevFormQueue('+row.factoryDevNo+')"></a>';
		}

		function formatThemeOper(val, row) {
			if (row.executingState == 0) {
				return '<span class="t-btn" onclick="cancelThemePub('+row.factoryDevNo+')">取消</span>';
			} else if (row.executingState == 4) {
				return '<span class="t-btn" onclick="confirmThemePub('+row.factoryDevNo+')">发布</span>';
			} else {
				return '';
			}
		}

		function formatDevState(val) {
			switch (val) {
				case '1':
					return '在线';
				case '2':
					return '<span style="color:red;">离线</span>';
				default:
					return val;
			}
		}

		function formatExecState(val) {
			switch (val) {
				case 0:
					return '未执行';
				case 1:
					return '执行中';
				case 2:
					return '已取消';
				case 3:
					return '执行成功';
				case 4:
					return '执行失败';
				default:
					return val;
			}
		}

		function formatPointType(value) {
			for (var i = 0; i < pointTypes.length; i++)
				if (pointTypes[i].code == value)
					return pointTypes[i].name;
			return value;
		}

		//添加设备至队列
		function pushDevToQueue() {
			var selections = $('#deviceGrid').datagrid('getSelections');

			if (!selections.length) {
				infoMsg('请选择设备');
				return;
			}

			var themePubQueue = JSON.parse(localStorage.getItem('themePubQueue'));

			if (!themePubQueue) {
				themePubQueue = selections;
			} else {
				$.each(selections, function(i, dev) {
					var isExist = themePubQueue.filter(function(n) {
						return n.factoryDevNo == dev.factoryDevNo;
					}).length ? true : false;

					if (!isExist) {
						themePubQueue.push(dev);
					}
				})
			}

			localStorage.setItem('themePubQueue', JSON.stringify(themePubQueue));

			$('#deviceGrid').datagrid('uncheckAll');

			infoTip('已将设备添加至队列');
		}

		//从队列中删除设备
		function delDevFormQueue(factoryDevNo) {
			if (!factoryDevNo) {
				localStorage.removeItem('themePubQueue');
				$('#devicePubGrid').datagrid({
					data: {
						rows: []
					}
				})
			} else {
				var themePubQueue = JSON.parse(localStorage.getItem('themePubQueue'));
				for (var i = 0; i < themePubQueue.length; i++) {
					if (themePubQueue[i].factoryDevNo == factoryDevNo) {
						themePubQueue.splice(i, 1);
						break;
					}
				}
				localStorage.setItem('themePubQueue', JSON.stringify(themePubQueue));

				var index = $('#devicePubGrid').datagrid('getRowIndex', factoryDevNo);
				$('#devicePubGrid').datagrid('deleteRow', index);
			}
		}

		//打开主题发布队列窗口
		function themePublish() {
			openWin('winThemePub');

			$('#winThemePub input[name="hThemeId"], #winThemePub input[name="vThemeId"]').val('');

			var themePubQueue = JSON.parse(localStorage.getItem('themePubQueue'));

			$('#devicePubGrid').datagrid({
				data: {
					rows: themePubQueue ? themePubQueue : []
				}
			})
		}

		//获取主题
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
					$('#winThemeList .theme-list').eq(0).html(template('theme-item-tpl', wrap));

					wrap.themes = themes.filter(function(t) {
						return t.themeType == 1;
					}).sort(function(a, b) {
						return a.defaultTheme - b.defaultTheme;
					})
					$('#winThemeList .theme-list').eq(1).html(template('theme-item-tpl', wrap));
				}
			})
		}

		//选择主题
		function selectTheme(type) {
			var types = ['横屏', '竖屏'];
			openWin('winThemeList', '选择'+types[type]+'主题');

			$('#winThemeList .theme-list').eq(type).show().siblings().hide();
		}

		//确认主题
		function doSelectTheme() {
			var $activeList = $('#winThemeList .theme-list:visible');
			var $selectedTheme = $activeList.children('.active');

			if ($selectedTheme.length == 0) {
				infoMsg('请选择主题');
				return
			}

			var type = $selectedTheme.data('type'),
				id = $selectedTheme.data('id'),
				name = $selectedTheme.data('name');

			if (type == 0) {
				$('#horizontalTheme').textbox('setValue', name).siblings('input[name="hThemeId"]').val(id);
			} else if (type == 1) {
				$('#verticalTheme').textbox('setValue', name).siblings('input[name="vThemeId"]').val(id);
			}

			$selectedTheme.removeClass('active');

			closeWin('winThemeList');
		}

		//清空主题
		function clearTheme(type) {
			if (type == 0) {
				$('#horizontalTheme').textbox('setValue', '').siblings('input[name="hThemeId"]').val('');
			} else if (type == 1) {
				$('#verticalTheme').textbox('setValue', '').siblings('input[name="vThemeId"]').val('');
			}
		}

		//主题队列发布
		function saveThemeQueue() {
			if (!$('#formThemePub').form('enableValidation').form('validate')) return;

			var data = $('#formThemePub').getValues();

			if (!data.hThemeId && !data.vThemeId) {
				infoMsg('请选择主题');
				return;
			}

			var devList = $('#devicePubGrid').datagrid('getRows');

			data.themeDevices = [];
			$.each(devList, function(i, dev) {
				data.themeDevices.push(dev.factoryDevNo);
			})

			$.ajax({
				url: '${pageContext.request.contextPath}/interaction/theme/saveThemeAndDivce.json',
				data: $.param(data, true),
				info: '发布成功！',
				task: function(r) {
					$('#deviceGrid').datagrid('reload');
					closeWin('winThemePub');
				}
			})
		}

		//实时推送主题
		function confirmThemePub(factoryDevNo) {
			$.ajax({
				url: '${pageContext.request.contextPath}/interaction/theme/updateThemeRealTimePush.json',
				data: {factoryDevNo: factoryDevNo},
				info: '已向设备推送主题！',
				task: function() {
					$('#deviceGrid').datagrid('reload');
				}
			})
		}

		//取消定时推送主题任务
		function cancelThemePub(factoryDevNo) {
			confirmMsg('取消后，到了开始时间该设备的主题将不再更换成待执行主题', function() {
				$.ajax({
					url: '${pageContext.request.contextPath}/interaction/theme/updateCancelTheme.json',
					data: {factoryDevNo: factoryDevNo},
					info: '已取消主题更换！',
					task: function() {
						$('#deviceGrid').datagrid('reload');
					}
				})
			})
		}

		$(function() {
			var cache = getAllSysTypes();
			for (var i = 0; i < cache.length; i++) {
				if (cache[i].type == 'POINT_PLACE_TYPE') {
					pointTypes.push(cache[i]);
				}
			}

			$('#winThemeList').on('click', 'img', function() {
				$(this).parent().toggleClass('active').siblings().removeClass('active');
			})

			$('#deviceGrid').datagrid({
				url: '${pageContext.request.contextPath}/interaction/theme/findDeviceThemeSkinList.json',
				onLoadSuccess: function(data) {
					//更新localstorage中缓存的设备
					var themePubQueue = JSON.parse(localStorage.getItem('themePubQueue'));

					if (!themePubQueue) return;

					var factoryDevNos = themePubQueue.map(function(dev) {
						return dev.factoryDevNo;
					})

					themePubQueue = [];
					$.each(data.rows, function(i, dev) {
						if (factoryDevNos.indexOf(dev.factoryDevNo) != -1) {
							themePubQueue.push(dev);
						}
					})

					localStorage.setItem('themePubQueue', JSON.stringify(themePubQueue));
				}
			})

			getThemes();
		})
	</script>
</body>
</html>