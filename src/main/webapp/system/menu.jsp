<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>权限资源</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript">
var curRole;
$(function() {
	$('#menuGrid').treegrid({
		pagination:false,
		url:'${pageContext.request.contextPath}/system/menu/findByCycle.json',
		loadFilter: function(data, parentNode){
			if (data.rows)
				return loadMenus(data.rows);
			return [data];
		},
		onContextMenu: function(e, node) {
			if ($('#menuGridMenu').children().length > 1) {
				e.preventDefault();
				$(this).treegrid('select',node.id);
				$('#menuGridMenu').menu('show',{
					left: e.pageX,
					top: e.pageY
				});
			}
		}
	});
	var roleTypes = [{'id':'', 'name':'所有'}];
	var div = '', index = 1;
	for (var i = 0; i < _rightsData.length; i++) {
		div = div + '<span onclick="showRightsCheck(' + _rightsData[i][0] + ')" disabled="disabled"><span id="chk' +_rightsData[i][0] + '" class="tree-checkbox tree-checkbox0"></span>';
		div = div + '<span style="cursor:pointer;vertical-align:middle">' + _rightsData[i][1] + '</span></span>&nbsp;';
		if ((i + 1) % 6 == 0) {
			$('#divRights' + index).append($(div.substr(0, div.length - 6)));
			div = '';
			index++;
		}
	}
	if (index == 3) {
		var el = $('#divRights2').html() + '&nbsp;' + div.substr(0, div.length - 6);
		$('#divRights2').children().remove();
		$('#divRights2').empty();
		$('#divRights2').append($(el));
	}
	numberTextBox('menu_sort');
});
function showRightsCheck(value) {
	if ($('#chk' + value).attr('class') == 'tree-checkbox tree-checkbox0') {
		$('#chk' + value).attr('class', 'tree-checkbox tree-checkbox1');
	} else {
		$('#chk' + value).attr('class', 'tree-checkbox tree-checkbox0');
	}
}
function loadMenus(data) {
	var nodes = [];
	for (var i = 0; i < data.length; i++) {
		var node = data[i];		
		if (data[i].menus)
			node.children = loadMenus(data[i].menus);
		nodes.push(node);
	}
	return nodes;
}
function loadRights(node) {
	for (var attr in _rights) {
		if (node && checkAccess(node.rights, _rights[attr])) {
			$('#chk' + _rights[attr]).attr('class', 'tree-checkbox tree-checkbox1');
		} else {
			$('#chk' + _rights[attr]).attr('class', 'tree-checkbox tree-checkbox0');
		}
	}
}
function calcRights() {
	var ary = [];
	for (var attr in _rights)
		if ($('#chk' + _rights[attr]).attr('class') == 'tree-checkbox tree-checkbox1')
			ary.push(_rights[attr]);
	return grantRights(ary);
}
function addMenu() {
	openDialog('winMenu', '新增菜单');
	enableMenuElement();
	loadRights();
	$('#menu_enable').combobox('setValue', 1);
	$('#menu_limited').combobox('setValue', 0);
	var node = $('#menuGrid').treegrid('getSelected');
	if (node) {
		$('#menu_parentId').textbox('setValue', node.id);
		$('#menu_parentId').textbox('setText', node.name);
		var size = $('#menuGrid').treegrid('getChildren', node.id).length;
		$('#menu_sort').textbox('setValue', node.sort + formatNumber(size + 1, 2));
	} else {
		var size = $('#menuGrid').tree('getData').length;
		$('#menu_sort').textbox('setValue', formatNumber(size, 4));
	}
}
function updateMenu() {
	openDialog('winMenu', '修改菜单');
	var node = $('#menuGrid').treegrid('getSelected');
	var parentNode = $('#menuGrid').treegrid('getParent', node.id);
	if ($('#menuGrid').treegrid('getChildren', node.id).length == 0) {
		enableMenuElement();
	} else {
		disableMenuElement();
	}
	if (node) {
		$('#fomMenu').form('load', node);
		loadRights(node);
		if (parentNode) {
			$('#menu_parentId').textbox('setText', parentNode.name);
		}
	}
}
function saveMenu() {
	var msg = '';
	$('#menu_rights').val(calcRights());
	var node = $('#menuGrid').treegrid('getSelected');
	if (node.url) {
		if ($('#menu_id').val()) {
			if ($('#menuGrid').treegrid('getChildren', node.id) != 0)
				msg = '父菜单的URL值已被忽略，';
		} else {
			msg = '父菜单的URL值已被清空，';
		}
	}
	postForm({
		form:'fomMenu',
		url:'${pageContext.request.contextPath}/system/menu/save.json',
		info:msg + '保存菜单成功！',
		task:function(data, statusText, xhr) {
			if (node) {
				if ($('#menu_id').val()) {
					$('#menuGrid').treegrid('update', {id:node.id,row:data.menu});
				} else {
					$('#menuGrid').treegrid('append', {parent:node.id,data:data.menu});
					if (node.url) {
						delete node.url;
						$('#menuGrid').treegrid('update', {id:node.id,row:node});
					}
				}
			}
		}
	});
}
function delMenu() {
	confirmMsg('您确定要删除该所选菜单及其子菜单吗？', doDelMenu);
}
function doDelMenu() {
	var node = $('#menuGrid').treegrid('getSelected');
	var parentNode = $('#menuGrid').treegrid('getParent', node.id);
	if (node) {
		$.ajax({
			url:'${pageContext.request.contextPath}/system/menu/delete.json',
			data:$.param({ids:node.id}),
			info:'所选菜单删除成功！',
			task:function(data, statusText, xhr) {
				$('#menuGrid').treegrid('remove', node.id);
			}
		});
	}
}
function disableMenuElement() {
	$('#menu_url').textbox('disable');
	$('#divRights1').hide();
	$('#divRights2').hide();
}
function enableMenuElement() {
	$('#menu_url').textbox('enable');
	$('#divRights1').show();
	$('#divRights2').show();
}
function showMenuState(value, row, index) {
	return value == true ? '启用' : '禁用';
}
function showRightsView(value, row, index) {
	var rightsView = '';
	if (row.rights) {
		for (var i = 0; i < _rightsData.length; i++)
			if (checkAccess(row.rights, _rightsData[i][0]))
				rightsView = rightsView + '<span class="tree-checkbox tree-checkbox0"></span>' + _rightsData[i][1];
	}
	return rightsView;
}
function findMenusBySearch() {
	var param = $.extend($('#menuGrid').treegrid('options').queryParams, $('#menuQueryForm').getValues());
	$('#menuGrid').treegrid('load', param);
}
</script>
</head>
<body class="easyui-layout">
<div data-options="region:'north',border:false"><table><tr><td>
	<form id="menuQueryForm"><input type="hidden" id="q_user_orgId" name="orgId" value="">
	<table style="border:1px solid #ccc;"><tr>
		<td style="padding:5px 5px 5px 10px;">菜单名称</td><td style="padding:5px 0px 5px 0px;"><input class="easyui-textbox" name="name" style="width:80px"></td>
		<td style="padding:5px 5px 5px 15px;">序号</td><td style="padding:5px 0px 5px 0px;"><input class="easyui-textbox" name="sort" style="width:80px"></td>
		<td style="padding:5px 5px 5px 15px;">URL</td><td style="padding:5px 0px 5px 0px;"><input class="easyui-textbox" name="url" style="width:80px"></td>
		<td style="padding:5px 5px 5px 15px;">状态</td><td style="padding:5px 10px 5px 0px;"><select id="q_menu_enable" name="enable" class="easyui-combobox" style="width:60px;" data-options="editable:false">
			<option value="" selected>所有</option>
			<option value="1">启用</option>
			<option value="0">禁用</option>
		</select></td>
	</tr></table></form></td><sec:authorize access="find">
		<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-search'" href="javascript:void(0)" onclick="findMenusBySearch()">查询</a></td>
		<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-no'" href="javascript:void(0)" onclick="resetForm('menuQueryForm')">重置</a></td>
	</sec:authorize></tr>
</table></div>
<div data-options="region:'center',border:false"><table id="menuGrid" data-options="nowrap:false,striped:true,fit:true,border:false,idField:'id',treeField:'name'" title="权限资源">
	<thead>
		<tr>
			<th data-options="field:'name',width:300">菜单名称</th>
			<th data-options="field:'sort',width:60">序号</th>
			<th data-options="field:'enable',width:40,formatter:showMenuState">状态</th>
			<th data-options="field:'url',width:200">URL</th>
			<th data-options="field:'rightsView',width:400,formatter:showRightsView">权限可选范围</th>
		</tr>
	</thead>
</table></div>
<div id="menuGridMenu" class="easyui-menu" style="width:120px;">
	<sec:authorize access="add,save"><div id="mnuAddMenu" onclick="addMenu()" data-options="iconCls:'icon-add'">新增菜单</div></sec:authorize>
	<sec:authorize access="update,save"><div id="mnuUpdateMenu" onclick="updateMenu()" data-options="iconCls:'icon-edit'">修改菜单</div></sec:authorize>
	<sec:authorize access="delete"><div id="mnuDelMenu" onclick="delMenu()" data-options="iconCls:'icon-remove'">删除菜单</div></sec:authorize>
</div>
<div id="winMenu" class="easyui-dialog" data-options="closed:true" style="width:475px;height:280px;padding:5px">
	<div class="easyui-layout" data-options="fit:true">
	<div data-options="region:'center',border:false" style="padding:10px 10px;background:#fff;border:1px solid #ccc;">
	<form id="fomMenu" method="post">
	<input type="hidden" id="menu_id" name="id"><input type="hidden" id="menu_rights" name="rights">
	<table><tr>
		<td width="50px">菜单名称</td>
		<td><input id="menu_name" name="name" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项'"></td><td width="20px"></td>
		<td width="50px">菜单序号</td>
		<td><input id="menu_sort" name="sort" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项'"></td>
	</tr><tr>
		<td width="50px">上级菜单</td>
		<td><input id="menu_parentId" name="parentId" class="easyui-textbox" style="width:145px" readonly="readonly"></td><td width="20px"></td>
		<td width="50px">URL</td>
		<td><input id="menu_url" name="url" class="easyui-textbox" style="width:145px"></td>
	</tr><tr>
		<td width="50px">是否显示</td>
		<td><select id="menu_enable" name="enable" class="easyui-combobox" style="width:145px;" data-options="editable:false">
			<option value="1" selected>显示</option>
			<option value="0">隐藏</option>
		</select></td><td width="20px"></td>
		<td width="50px">是否限制</td>
		<td><select id="menu_limited" name="limited" class="easyui-combobox" style="width:145px;" data-options="editable:false">
			<option value="0" selected>开放</option>
			<option value="1">限制</option>
		</select></td>
	</tr><tr>
		<td colspan="5" style="height:40px" id="divRightsTitle">权限可选范围</td>
	</tr><tr>
		<td colspan="5" id="divRights1"></td>
	</tr><tr>
		<td colspan="5" id="divRights2"></td>
	</tr></table></form></div>
	<div data-options="region:'south',border:false" style="height:38px;text-align:right;padding:5px 0;">
		<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveMenu()">保存</a>
		<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winMenu')">取消</a>
	</div></div>
</div>
</body>
</html>