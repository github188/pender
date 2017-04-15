<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>权限管理</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript">
var curRole;
$(function() {
	//showOrgCombo(false, true);
	$('#accessUserGrid').datagrid({
		url:'${pageContext.request.contextPath}/system/access/findUsers.json?enabled=1',
		onClickRow:function(rowIndex, rowData) {
			loadUserRoles(rowData.id);
		}
	});
	$('#accessRoleTree').tree({
        url: '${pageContext.request.contextPath}/system/access/findRoles.json',
		loadFilter: function(data, parentNode){
			if (data.rows) {
				var nodes = loadRoles(data.rows);
				if (!parentNode) {
					var rootNode = {};
					rootNode.id = 'root';
					rootNode.text = '角色管理';
					rootNode.state = 'closed';
					rootNode.children = nodes;
					return [rootNode];
				}
				return nodes;
			} else if ($.isArray(data)) {
				return data;
			}
			return [data];
		},
		onClick:function(node) {
			if (node.iconCls == 'icon-tip') {
				curRole = node.attributes;
				$('#accessTree').tree('options').url = '${pageContext.request.contextPath}/system/access/findMenusByParentId.json?roleId='+curRole.id;
				loadGrants(node.attributes.id);
			} else {
				curRole = undefined;
				$('#accessTree').tree('options').url = '${pageContext.request.contextPath}/system/access/findMenusByParentId.json';
				$('#accessTree').tree('uncheck', $('#accessTree').tree('getRoot').target);
			}
		},
		/*onDblClick:function(node) {
			if (node.iconCls != 'icon-tip') {
				$('#q_access_orgId').val(node.id);
				queryData('accessUserGrid','accessQueryForm');
			}
		},*/
		onCheck:function(node, checked) {
			if (checked)
				var selectNode = $('#accessRoleTree').tree('getSelected');
		},
		onLoadSuccess:function(node, data) {
			if (node == null) {
				var rootNode = $('#accessRoleTree').tree('getRoot');
				$('#accessRoleTree').tree('expand', rootNode.target);
			}
			$(this).tree('options').url = undefined;
		}
    });
	$('#accessTree').tree({
        url: '${pageContext.request.contextPath}/system/access/findMenusForAccess.json',
		loadFilter: function(data, parentNode){
			if (data.rows) {
				var nodes = loadAccess(data.rows);
				if (!parentNode) {
					var rootNode = {};
					rootNode.id = 'root';
					rootNode.text = '权限资源';					
					rootNode.state = 'closed';
					rootNode.children = nodes;
					return [rootNode];
				}
				return nodes;
			}
			return [data];
		},
		onCheck:function(node, checked) {
			if (checked) {
				if (!curRole) {
					$('#accessTree').tree('uncheck', node.target);
					infoMsg('请先选择需要更改权限的角色！');
				} else {
					$('#accessTree').tree('expand', node.target);
				}
			}
		},
		onExpand:function(node) {
			var nodes = $('#accessTree').tree('getChildren', node.target);
			for (var i = 0; i < nodes.length; i++) {
				if (node.checked) {
					$('#accessTree').tree('check', nodes[i].target);
					if (!$('#accessTree').tree('isLeaf', nodes[i].target))
						$('#accessTree').tree('expand', nodes[i].target);
				}
			}
		},
		onLoadSuccess:function(node, data) {
			if (node == null && data[0].id == 'root') {
				var rootNode = $('#accessTree').tree('getRoot');
				$('#accessTree').tree('expand', rootNode.target);
				var nodes = $('#accessTree').tree('getChildren', rootNode.target);
				var del = false;
				for (var i = 0; i < nodes.length; i++) {
					if ($('#accessTree').tree('isLeaf', nodes[i].target)) {
						var el = nodes[i].target;
						if (!$(el).parent().attr('style')) {
							$(el).parent().attr('style', 'display:inline');
							$(el).attr('style', 'display:inline');
							if (del) {
								var els = $(el).children();
								for (var j = 0; j < els.length; j++) {
									var cls = $(els[j]).attr('class');
									if (cls == 'tree-indent') {
										$(els[j]).remove();
									} else if (cls.indexOf('tree-line') != -1) {
										$(els[j]).attr('class', cls.replace(' tree-line', ''));
									}
								}
							}
						}
						del = true;
					} else {
						del = false;
					}
				}
			}
			$(this).tree('options').url = undefined;
		}
    });
});
function loadAccess(data) {
	var nodes = [];
	for (var i = 0; i < data.length; i++) {
		var node = {};
		node.id = data[i].id;
		node.text = data[i].name;
		node.attributes = data[i];					
		if (data[i].url) {
			node.iconCls = 'icon-tip';
			var roles = [];
			for (var j = 0; j < _rightsData.length; j++)
				if (checkAccess(data[i].rights, _rightsData[j][0]))
					roles.push({id:node.id+'_'+_rightsData[j][0], text:_rightsData[j][1],iconCls:'icon-sum',checked:checkAccess(data[i].grants, _rightsData[j][0]),style:{display:'inline'}});
			node.children = roles;
			node.state = 'open';
		} else {
			node.state = 'closed';
			if (data[i].menus) {
				node.children = loadAccess(data[i].menus);
			}
		}
		nodes.push(node);
	}
	return nodes;
}
function loadRoles(data) {
	var nodes = [];
	for (var i = 0; i < data.length; i++) {
		var node = {}, children = [];
		node.id = data[i].id;
		node.text = data[i].name;
		node.iconCls = 'icon-tip';
		node.state = 'open';
		node.attributes = data[i];
		nodes.push(node);
	}
	return nodes;
}
function loadUserRoles(userId) {
	$.ajax({
		url:'${pageContext.request.contextPath}/system/access/findUserRoles.json',
		data:{userId:userId},
		task:function(data, statusText, xhr) {
			var rootNode = $('#accessRoleTree').tree('getRoot');
			var children = $('#accessRoleTree').tree('getChildren', rootNode.target);
			for(var i = 0; i < children.length; i++) {
				if (children[i].iconCls == 'icon-tip') {
					var checked = false;
					for (var j = 0; j < data.rows.length; j++) {
						if (children[i].attributes.id == data.rows[j].roleId) {
							checked = true;
							data.rows.splice(j, 1);
							break;
						}
					}
					if (checked) {
						$('#accessRoleTree').tree('check', children[i].target);
					} else {
						$('#accessRoleTree').tree('uncheck', children[i].target);
					}
				}
			}
		}
	});
}
function loadGrants(roleId) {
	$.ajax({
		url:'${pageContext.request.contextPath}/system/access/findRights.json',
		data:{'roleId':roleId},
		task:function(data, statusText, xhr) {
			var rootNode = $('#accessTree').tree('getRoot');
			var children = $('#accessTree').tree('getChildren', rootNode.target);
			for(var i = 0; i < children.length; i++) {
				if (children[i].attributes && children[i].attributes.url) {
					var curRights = undefined;
					for (var j = 0; j < data.rows.length; j++) {
						if (children[i].attributes.id == data.rows[j].menuId) {
							curRights = data.rows[j].value;
							data.rows.splice(j, 1);
							break;
						}
					}
					var optNodes = $('#accessTree').tree('getChildren', children[i].target);
					for (var j = 0; j < optNodes.length; j++) {
						var check = checkAccess(curRights, parseInt(optNodes[j].id.split('_')[1]));
						if (check) {
							$('#accessTree').tree('check', optNodes[j].target);
						} else {
							$('#accessTree').tree('uncheck', optNodes[j].target);
						}
					}
				}
			}
		}
	});
}
function addRole() {
	openDialog('winRole', '新增角色');
	$('#role_orgId').val(getCurUser().companyId);
}
function updateRole() {	
	if (curRole) {
		var node = $('#accessRoleTree').tree('getSelected');
		doUpdateRole(node);
	} else {
		infoMsg('请选择需要修改的角色信息！');
	}
}
function doUpdateRole(node) {
	openDialog('winRole', '修改角色');
	$('#fomRole').form('load', node.attributes);
}
function saveRole() {
	postForm({
		form:'fomRole',
		url:'${pageContext.request.contextPath}/system/access/saveRole.json',
		info:'保存角色成功！',
		task:function(data, statusText, xhr) {
			if ($('#role_id').val()) {
				var node = $('#accessRoleTree').tree('getSelected');
				$('#accessRoleTree').tree('update', {target:node.target,text:data.role.name, attributes:data.role});
			} else {
				var node = $('#accessRoleTree').tree('getRoot');
				$('#accessRoleTree').tree('append', {parent:node.target,data:{id:data.role.id,text:data.role.name, iconCls:'icon-tip',attributes:data.role}});
				$('#accessRoleTree').tree('expand', node.target);
			}
		}
	});
}
function delRole() {	
	if (curRole) {
		var node = $('#accessRoleTree').tree('getSelected');
		confirmMsg('您确定要删除该所选角色吗？', doDelRole, [node]);
	} else {
		infoMsg('请选择需要删除的角色信息！');
	}	
}
function doDelRole(node) {
	$.ajax({
		url:'${pageContext.request.contextPath}/system/access/deleteRoles.json',
		data:$.param({ids:node.id}),
		info:'所选角色删除成功！',
		task:function(data, statusText, xhr) {
			$('#accessRoleTree').tree('remove', node.target);
		}
	});
}
function saveUserRoles() {
	var users = $('#accessUserGrid').datagrid('getChecked');
	if (users.length == 0) {
		infoMsg('请选择需要更改授权的用户！');
	} else {
		var orgType = 3;
		var roleIds = [];
		var nodes = $('#accessRoleTree').tree('getChecked');
		for(var i = 0; i < nodes.length; i++) {
			if (nodes[i].iconCls == 'icon-tip') {
				roleIds.push(nodes[i].attributes.id);
				if (orgType > nodes[i].attributes.orgId)
					orgType = nodes[i].attributes.orgId;
			}
		}
		var error = [], userIds = [];
		for(var i = 0; i < users.length; i++) {
			userIds.push(users[i].id);
			if (users[i].orgType > orgType)
				error.push(users[i].username);
		}
		if (error.length == 0) {
			if (roleIds.length == 0) {
				var msg = '您未选择任何授权角色！<br/>您确定要清除所选用户的所有授权吗？';
				confirmMsg(msg, doSaveUserRoles, [userIds, roleIds]);
			} else {
				doSaveUserRoles(userIds, roleIds);
			}
		} else {
			errorMsg('所选用户[' + error + ']存在越级授权，请重新选择用户授权！');
		}
	}
}
function doSaveUserRoles(userIds, roleIds) {
	$.ajax({
		url:'${pageContext.request.contextPath}/system/access/saveUserRoles.json',
		data:$.param({userIds:userIds,roleIds:roleIds}, true),
		info:'所选用户授权保存成功！'
	});
}
function copyRights() {
	if (curRole) {
		var roleIds = [], roleNames = [];
		var selectNode = $('#accessRoleTree').tree('getSelected');
		var nodes = $('#accessRoleTree').tree('getChecked');
		for(var i = 0; i < nodes.length; i++)
			if (nodes[i].iconCls == 'icon-tip' && (!selectNode || nodes[i].attributes.id != selectNode.attributes.id)) {
				roleIds.push(nodes[i].attributes.id);
				roleNames.push(nodes[i].attributes.name);
			}
		if (roleIds.length == 0) {
			infoMsg('请选择权限复制目标角色！');
		} else {
			var msg = '您确定要进行如下权限复制吗？</br>源角色： ';
			msg = msg.concat(curRole.name).concat('</br>目标角色： ').concat(roleNames);
			confirmMsg(msg, doCopyRights, [roleIds]);
		}
	} else {
		infoMsg('请选择权限复制源角色！');
	}
}
function doCopyRights(roleIds) {
	$.ajax({
		url:'${pageContext.request.contextPath}/system/access/copy.json',
		data:$.param({'roleId':curRole.id,'roleIds':roleIds}, true),
		info:curRole.name + '权限复制成功！'
	});
}
function calcRights() {
	var allRights = [];
	var rootNode = $('#accessTree').tree('getRoot');
	var children = $('#accessTree').tree('getChildren', rootNode.target);
	for(var i = 0; i < children.length; i++) {
		if (children[i].attributes && children[i].attributes.url) {
			var curRights = {}, ary = [];
			var optNodes = $('#accessTree').tree('getChildren', children[i].target);
			for (var j = 0; j < optNodes.length; j++)
				if (optNodes[j].checked)
					ary.push(parseInt(optNodes[j].id.split('_')[1]));
			curRights.roleId = curRole.id;
			curRights.menuId = children[i].attributes.id;
			curRights.value = grantRights(ary);
			if (curRights.value > 0)
				allRights.push(JSON.stringify(curRights));//JSON.stringify 需IE8以上版本
		}
	}
	return allRights;
}
function saveRights() {
	if (curRole) {
		var allRights = calcRights();
		if (allRights.length == 0) {
			var msg = '您未选择任何权限资源！<br/>您确定要清除' + curRole.name + '的所有权限吗？';
			confirmMsg(msg, doSaveRights, [JSON.stringify({'roleId':curRole.id})]);
		} else {
			doSaveRights(allRights);
		}
	} else {
			infoMsg('请选择需要更改权限的角色！');
	}
}
function doSaveRights(allRights) {
	$.ajax({
		url:'${pageContext.request.contextPath}/system/access/save.json',
		data:$.param({'authorities':allRights}, true),
		info:curRole.name + '权限保存成功！'
	});
}
function clearUserRoles() {
	var rootRole = $('#accessRoleTree').tree('getRoot');
	$('#accessRoleTree').tree('uncheck', rootRole.target);
	$('#accessUserGrid').datagrid('uncheckAll');
}
function clearRights() {
	var rootRole = $('#accessRoleTree').tree('getRoot');
	var rootRights = $('#accessTree').tree('getRoot');
	$('#accessRoleTree').tree('uncheck', rootRole.target);
	$('#accessTree').tree('uncheck', rootRights.target);
}
function showAccessState(value, row, index) {
	return value == 1 ? '启用' : '禁用';
}
</script>
</head>
<body class="easyui-layout">
<div id="accessUserGridBar" style="padding:5px;height:auto">
	<div><form id="accessQueryForm">
		所属机构&nbsp;<input class="easyui-textbox" style="width:160px" id="q_access_orgId" name="orgName">
		用户名&nbsp;<input class="easyui-textbox" style="width:80px" name="username">
		<a href="#" class="easyui-linkbutton" iconCls="icon-search" plain="true" onclick="queryData('accessUserGrid','accessQueryForm')">查询</a>
		<a href="#" class="easyui-linkbutton" iconCls="icon-no" plain="true" onclick="resetForm('accessQueryForm')">重置</a>
	</form></div>
</div>
<div data-options="region:'west',border:false,split:true" style="width:500px;padding:0px">
	<table id="accessUserGrid" title="用户列表" data-options="nowrap:false,striped:true,fit:true,border:false,idField:'id',toolbar:'#accessUserGridBar',tools:'#accessUserGridOpt'">
		<thead>
			<tr>
				<th data-options="field:'ck',checkbox:true,width:20"></th>
				<th data-options="field:'username',width:100">用户名称</th>
				<th data-options="field:'nickname',width:100">员工姓名</th>
				<th data-options="field:'orgName',width:100">所属机构</th>
				<th data-options="field:'mobile',width:100">手机号码</th>
				<th data-options="field:'enabled',width:35,formatter:showAccessState">状态</th>
			</tr>
		</thead>
	</table>
</div>
<div data-options="region:'center',border:false"><div class="easyui-layout" data-options="fit:true">
	<div id="divAccessRole" title="角色列表" style="width:300px;padding:0px" data-options="region:'west',border:false,split:true,tools:'#divAccessRoleOpt'">
		<ul id="accessRoleTree" data-options="checkbox:true,animate:true"></ul>
	</div>
	<div title="权限列表" style="padding:0px" data-options="region:'center',border:false,split:false">
		<ul id="accessTree" data-options="checkbox:true,animate:true"></ul>
	</div>
</div></div>
<div id="accessUserGridOpt">
	<sec:authorize access="add,update,save"><a href="javascript:void(0)" class="icon-save easyui-tooltip" data-options="content:'保存用户授权'" onclick="saveUserRoles()"></a></sec:authorize>
	<a href="javascript:void(0)" class="icon-cancel easyui-tooltip" data-options="content:'清除用户授权'" onclick="clearUserRoles()"></a>
</div>
<div id="divAccessRoleOpt">
	<sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增角色'" onclick="addRole()"></a></sec:authorize>
	<sec:authorize access="update,save"><a href="javascript:void(0)" class="icon-edit easyui-tooltip" data-options="content:'修改角色'" onclick="updateRole()"></a></sec:authorize>
	<sec:authorize access="delete"><a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除角色'" onclick="delRole()"></a></sec:authorize>
	<sec:authorize access="add,update,save"><a href="javascript:void(0)" class="icon-save easyui-tooltip" data-options="content:'保存角色权限'" onclick="saveRights()"></a></sec:authorize>
	<a href="javascript:void(0)" class="icon-cancel easyui-tooltip" data-options="content:'清除角色权限'" onclick="clearRights()"></a>
</div>
<div id="winRole" class="easyui-dialog" data-options="closed:true" style="width:480px;height:210px;padding:5px">
	<div class="easyui-layout" data-options="fit:true">
	<div data-options="region:'center',border:false" style="padding:10px 10px;background:#fff;border:1px solid #ccc;">
	<form id="fomRole" method="post">
	<input type="hidden" id="role_id" name="id">
	<input type="hidden" id="role_orgId" name="orgId">
	<input type="hidden" id="role_editable" name="editable">
	<input type="hidden" id="role_sysType" name="sysType">
	<input type="hidden" id="role_createUser" name="createUser">
	<input type="hidden" id="role_createTime" name="createTime">
	<input type="hidden" id="role_updateUser" name="updateUser">
	<input type="hidden" id="role_updateTime" name="updateTime">
	<table><tr>
		<td width="50px">角色名称</td>
		<td><input id="role_name" name="name" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项'"></td>
	</tr><tr>
		<td>备注</td>
		<td><textarea class="easyui-textbox" data-options="multiline:true" id="role_remark" name="remark" style="width:372px;height:70px;"></textarea></td>
	</tr></table></form></div>
	<div data-options="region:'south',border:false" style="height:38px;text-align:right;padding:5px 0;">
		<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveRole()">保存</a>
		<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winRole')">取消</a>
	</div></div>
</div>
</body>
</html>