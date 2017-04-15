<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>用户管理</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript">
$.extend($.fn.validatebox.defaults.rules, {
    admin: {
        validator: function(value, param) {
            return !value.match(/^admin\d*$/);
        },
        message: '禁止使用系统内置用户名！'  
    }
});
var userOrgs = [];
$(function() {
	$('#orgTree').tree({
		checkbox: false,  
        url: '${pageContext.request.contextPath}/system/user/findOrgnizationsByParentId.json',
        onContextMenu: function(e,node) {
			if ($('#orgTreeMenu').children().length > 1) {
				if (node.attributes.type == 1) {
					e.preventDefault();
					$(this).tree('select',node.target);
					$('#orgTreeMenu').menu('show',{
						left: e.pageX,
						top: e.pageY
					});
					if (node.id == 'root') {
						<sec:authorize access="update,save?system/org">
						$('#orgTreeMenu').menu('disableItem', $('#mnAddOrg')[0]);
						$('#orgTreeMenu').menu('disableItem', $('#mnUpdateOrg')[0]);
						</sec:authorize>
						<sec:authorize access="delete?system/org">$('#orgTreeMenu').menu('disableItem', $('#mnDelOrg')[0]);</sec:authorize>
					} else {
						<sec:authorize access="update,save?system/org">
						$('#orgTreeMenu').menu('enableItem', $('#mnAddOrg')[0]);
						$('#orgTreeMenu').menu('enableItem', $('#mnUpdateOrg')[0]);
						</sec:authorize>
						<sec:authorize access="delete?system/org">$('#orgTreeMenu').menu('enableItem', $('#mnDelOrg')[0]);</sec:authorize>
					}
				}
			}
		},
		loadFilter: function(data, parentNode){
			if (data.rows) {
				var nodes = [];
				for (var i = 0; i < data.rows.length; i++) {
					var node = {};
					node.id = data.rows[i].id;
					node.text = data.rows[i].name;
					node.attributes = data.rows[i];
					node.state = 'closed';
					nodes.push(node);
				}
				<sec:authorize access="hq">
				if (!parentNode) {
					var rootNode = {};
					rootNode.id = 'root';
					rootNode.text = '组织机构';					
					rootNode.state = 'closed';
					rootNode.children = nodes;
					return [rootNode];
				}
				</sec:authorize>
				return nodes;
			}
			return [data];
		},
		onClick:function(node) {
			$('#q_user_orgId').val(node.id);
			queryData('userGrid','userQueryForm');
		},
		onLoadSuccess:function(node, data) {
			if (node == null) {
				var rootNode = $('#orgTree').tree('getRoot');
				$('#orgTree').tree('expand', rootNode.target);
				//if (rootNode.attributes)
					//rootNode.id = 'root';
			}
		}
    });
	$('#userGrid').datagrid({
		url:'${pageContext.request.contextPath}/system/user/find.json'
	});
	numberTextBox('org_code');
	numberTextBox('user_mobile');
	upperTextBox('user_idCard');
	upperTextBox('user_passport');
	upperTextBox('user_visa');
	upperTextBox('user_codeHK');
	upperTextBox('user_codeTW');
	upperTextBox('user_visaTW');
});
function showUserState(value, row, index) {
	return value == 1 ? '启用' : '禁用';
}
function addOrg() {
	openDialog('winOrg', '新增机构');
	var node = $('#orgTree').tree('getSelected');
	if (node) {
		$('#org_parentId').textbox('setValue', node.id);
		$('#org_parentId').textbox('setText', node.attributes ? node.attributes.name : node.text);
		var size = $('#orgTree').tree('getChildren', node.target).length;
		if (node.id == 'root') {
			$('#org_code').textbox('setValue', formatNumber(size, 6));
		} else {
			$('#org_code').textbox('setValue', node.attributes.code + formatNumber(size + 1, 2));
		}
	}
	$('#org_sort').val(1);
	$('#org_type').val(1);
	$('#org_countryId').val(1);
	$('#org_state').val(1);
}
function updateOrg() {
	openDialog('winOrg', '修改机构');
	var node = $('#orgTree').tree('getSelected');
	var parentNode = $('#orgTree').tree('getParent', node.target);
	if (node) {
		$('#fomOrg').form('load', node.attributes);
		if (parentNode) {
			$('#org_parentId').textbox('setValue', parentNode.id);
			$('#org_parentId').textbox('setText', parentNode.attributes ? parentNode.attributes.name : parentNode.text);
		}
	}
}
function saveOrg() {
	postForm({
		form:'fomOrg',
		url:'${pageContext.request.contextPath}/system/org/save.json',
		info:'保存机构成功！',
		task:function(data, statusText, xhr) {
			var node = $('#orgTree').tree('getSelected');
			if (node) {
				if ($('#org_id').val()) {
					var parentNode = $('#orgTree').tree('getParent', node.target);
					if (parentNode) {
						$('#orgTree').tree('reload', parentNode.target);
					} else {
						$('#orgTree').tree('reload', node.target);
					}
				} else {
					$('#orgTree').tree('reload', node.target);
				}
			}
		}
	});
}
function delOrg() {
	confirmMsg('您确定要删除该所选机构吗？', doDelOrg);
}
function doDelOrg() {
	var node = $('#orgTree').tree('getSelected');
	var parentNode = $('#orgTree').tree('getParent', node.target);
	if (node) {
		$.ajax({
			url:'${pageContext.request.contextPath}/system/org/delete.json',
			data:$.param({ids:node.id}),
			info:'所选机构删除成功！',
			task:function(data, statusText, xhr) {
				if (parentNode)
					$('#orgTree').tree('reload', parentNode.target);
				for (var i = 0; i < orgs.length; i++) {
					if (orgs[i].id == node.id) {
						orgs.splice(i, 1);
						break;
					}
				}
			}
		});
	}
}
function addUser() {
	var node = $('#orgTree').tree('getSelected');
	if (node && node.id != 'root'  && node.id != 3  && node.id != 4) {
		openDialog('winUser', '新增用户');
		$('#user_orgId').textbox('setValue', node.id);
		$('#user_orgId').textbox('setText', node.attributes ? node.attributes.name : node.text);
		$('#user_enable').combobox('setValue', 1);
	} else {
		infoMsg('请选择所属机构！');
	}
}
function updateUser(row) {
	if (!row)
		row = $('#userGrid').datagrid('getSelected');
	if (row) {
		openDialog('winUser', '修改用户');
		$('#fomUser').form('load', row);
		$('#user_orgId').textbox('setText', getOrgNameByOrgId(row.orgId));
	} else {
		infoMsg('请选择需要修改的用户！');
	}
}
function saveUser() {
	postForm({
		form:'fomUser',
		url:'${pageContext.request.contextPath}/system/user/save.json',
		info:'保存用户成功！',
		task:function(data) {
			queryData('userGrid','userQueryForm');
		}
	});
}
function delUsers() {
	var rows = $('#userGrid').datagrid('getSelections');
	if (rows.length == 0) {
		infoMsg('请选择需要删除的用户！');
	} else {
		confirmMsg('您确定要删除该所选用户吗？', doDelUsers, [rows]);
	}
}
function doDelUsers(rows) {
	var ids = [];
	for (var i = 0; i < rows.length; i++)
		ids.push(rows[i].id);
	$.ajax({
		url:'${pageContext.request.contextPath}/system/user/delete.json',
		data:$.param({'ids':ids}, true),
		info:'所选用户删除成功！',
		task:function(data, statusText, xhr) {			
			queryData('userGrid','userQueryForm');
		}
	});
}
function resetPasswd() {
	var row = $('#userGrid').datagrid('getSelected');
	if (row) {
		confirmMsg('您确定要重置该用户的密码吗？', doResetPasswd, [row]);		
	} else{
		infoMsg("请选择需要重置密码的用户！");
	}
}
function doResetPasswd(row) {
	$.ajax({
		url:'${pageContext.request.contextPath}/system/user/saveResetPassword.json',
		data:{'userId':row.id},
		info:'重置用户密码成功！'
	});
}
</script>
</head>
<body class="easyui-layout">
<div id="userWest" data-options="region:'west',border:false,split:true" title="机构信息" style="width:250px;padding:0px">
	<ul id="orgTree" data-options="animate:true"></ul>
	<div id="orgTreeMenu" class="easyui-menu" style="width:120px;">
		<sec:authorize access="add,save?system/org"><div  id="mnAddOrg"onclick="addOrg()" data-options="iconCls:'icon-add'">新增机构</div></sec:authorize>
		<sec:authorize access="update,save?system/org"><div  id="mnUpdateOrg"onclick="updateOrg()" data-options="iconCls:'icon-edit'">修改机构</div></sec:authorize>
		<sec:authorize access="delete?system/org"><div  id="mnDelOrg"onclick="delOrg()" data-options="iconCls:'icon-remove'">删除机构</div></sec:authorize>
	</div>
</div>
<div id="userMain" data-options="region:'center',border:false"><div class="easyui-layout" data-options="fit:true">
	<div data-options="region:'north',border:false"><table><tr><td>
		<form id="userQueryForm"><input type="hidden" id="q_user_orgId" name="orgId" value="">
		<table style="border:1px solid #ccc;"><tr>
		<td style="padding:5px 5px 5px 10px;">类型</td><td style="padding:5px 0px 5px 0px;"><select id="q_user_companyId" name="companyId" class="easyui-combobox" style="width:80px;" data-options="editable:false">
				<option value="" selected>所有</option>
				<option value="2">平台用户</option>
				<option value="3">商家用户</option>
				<option value="4">合作公司</option>
				<option value="-1">注册用户</option>
			</select></td>
			<td style="padding:5px 5px 5px 15px;">用户名</td><td style="padding:5px 0px 5px 0px;"><input class="easyui-textbox" name="username" style="width:80px"></td>
			<td style="padding:5px 5px 5px 15px;">状态</td><td style="padding:5px 0px 5px 0px;"><select id="q_user_enable" name="enable" class="easyui-combobox" style="width:60px;" data-options="editable:false">
				<option value="" selected>所有</option>
				<option value="1">启用</option>
				<option value="0">禁用</option>
			</select></td>
			<td style="padding:5px 5px 5px 15px;">创建日期</td><td style="padding:5px 10px 5px 0px;"><input id="q_user_startDate" class="easyui-datebox" style="width:100px;" name="startDate">&nbsp;-&nbsp;<input id="q_user_endDate" class="easyui-datebox" style="width:100px;" name="endDate"></td>
		</tr></table></form></td><sec:authorize access="find">
			<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-search'" href="javascript:void(0)" onclick="queryData('userGrid','userQueryForm')">查询</a></td>
			<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-no'" href="javascript:void(0)" onclick="resetForm('userQueryForm')">重置</a></td>
	</sec:authorize></tr></table></div>
	<div data-options="region:'center',border:false"><table id="userGrid" data-options="nowrap:false,striped:true,fit:true,border:false,idField:'id',tools:'#userOpt'" title="用户列表">
		<thead>
			<tr>
				<th data-options="field:'ck',checkbox:true,width:20"></th>
				<th data-options="field:'username',width:100">用户名称</th>
				<th data-options="field:'nickname',width:100">昵称</th>
				<th data-options="field:'realName',width:100">真实姓名</th>
				<th data-options="field:'mobile',width:100">手机号码</th>
				<th data-options="field:'email',width:150">电子邮箱</th>
				<th data-options="field:'lastLoginTime',width:130">最后登录时间</th>
				<th data-options="field:'pwdUpdateTime',width:130">密码修改时间</th>
				<th data-options="field:'orgName',width:160">所属机构</th>
				<th data-options="field:'enable',width:35,formatter:showUserState">状态</th>
				<th data-options="field:'remark',width:300">备注</th>
			</tr>
		</thead>
	</table></div>
</div></div>
<div id="userOpt">
	<sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增用户'" onclick="addUser()"></a></sec:authorize>
	<sec:authorize access="update,save"><a href="javascript:void(0)" class="icon-edit easyui-tooltip" data-options="content:'修改用户'" onclick="updateUser()"></a></sec:authorize>
	<sec:authorize access="delete"><a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除用户'" onclick="delUsers()"></a></sec:authorize>
	<sec:authorize access="update,save"><a href="javascript:void(0)" class="icon-help easyui-tooltip" data-options="content:'重置用户密码'" onclick="resetPasswd()"></a></sec:authorize>
</div>
<div id="winOrg" class="easyui-dialog" data-options="closed:true" style="width:480px;height:295px;padding:5px">
	<div class="easyui-layout" data-options="fit:true">
	<div data-options="region:'center',border:false" style="padding:10px 10px;background:#fff;border:1px solid #ccc;">
	<form id="fomOrg" method="post">
	<input type="hidden" id="org_id" name="id">
	<input type="hidden" id="org_sort" name="sort">
	<input type="hidden" id="org_type" name="type">
	<input type="hidden" id="org_state" name="state">
	<input type="hidden" id="org_companyId" name="companyId">
	<input type="hidden" id="org_createUser" name="createUser">
	<input type="hidden" id="org_createTime" name="createTime">
	<table><tr>
		<td width="50px">机构编码</td>
		<td><input id="org_code" name="code" class="easyui-textbox" style="width:145px;" data-options="required:true,prompt:'必填项'"></td><td width="20px"></td>
		<td width="50px">机构名称</td>
		<td><input id="org_name" name="name" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项'"></td>
	</tr><tr>
		<td width="50px">负责人</td>
		<td><input id="org_manager" name="manager" class="easyui-textbox" style="width:145px"></td><td width="20px"></td>
		<td width="50px">办公电话</td>
		<td><input id="org_phone" name="phone" class="easyui-textbox" style="width:145px"></td>
	</tr><tr>
		<td width="50px">所属机构</td>
		<td><input id="org_parentId" name="parentId" class="easyui-textbox" style="width:145px" readonly="readonly"></td><td width="20px"></td>
	</tr><tr>
		<td>办公地址</td>
		<td colspan="4"><textarea class="easyui-textbox" data-options="multiline:true" id="org_office" name="office" style="width:372px;height:40px;"></textarea></td>
	</tr><tr>
		<td>机构备注</td>
		<td colspan="4"><textarea class="easyui-textbox" data-options="multiline:true" id="org_remark" name="remark" style="width:372px;height:60px;"></textarea></td>
	</tr></table></form></div>
	<div data-options="region:'south',border:false" style="height:38px;text-align:right;padding:5px 0;">
		<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveOrg()">保存</a>
		<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winOrg')">取消</a>
	</div></div>
</div>
<div id="winUser" class="easyui-dialog" data-options="closed:true" style="width:706px;height:330px;padding:5px">
	<div class="easyui-layout" data-options="fit:true">
	<div data-options="region:'center',border:false" style="padding:10px 10px;background:#fff;border:1px solid #ccc;">
	<form id="fomUser" method="post">
	<input type="hidden" id="user_id" name="id">
	<input type="hidden" id="user_companyId" name="companyId">
	<input type="hidden" id="user_retry" name="retry">
	<input type="hidden" id="user_editable" name="editable">
	<input type="hidden" id="user_lastLoginMachine" name="lastLoginMachine">
	<input type="hidden" id="user_lastLoginTime" name="lastLoginTime">
	<input type="hidden" id="user_pwdUpdateTime" name="pwdUpdateTime">
	<input type="hidden" id="user_createUser" name="createUser">
	<input type="hidden" id="user_createTime" name="createTime">
	<input type="hidden" id="user_updateUser" name="updateUser">
	<input type="hidden" id="user_updateTime" name="updateTime">
	<table><tr>
		<td width="60px">用户名称</td>
		<td><input id="user_username" name="username" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项',validType:['admin']"></td><td width="20px"></td>
		<td width="50px">用户昵称</td>
		<td><input id="user_nickname" name="nickname" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项'"></td><td width="20px"></td>
		<td width="50px">真实姓名</td>
		<td><input id="user_realName" name="realName" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项'"></td>
	</tr><tr>
		<td width="60px">手机号码</td>
		<td><input id="user_mobile" name="mobile" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项',validType:'length[1,16]'"></td><td width="20px"></td>
		<td width="50px">电子邮箱</td>
		<td><input id="user_email" name="email" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项',validType:'email'"></td><td width="20px"></td>
		<td width="50px">所属机构</td>
		<td><input id="user_orgId" name="orgId" class="easyui-textbox" style="width:145px" readonly="readonly"></td><td></td>
	</tr><tr>
		<td width="60px">状态</td>
		<td colspan="5"><select id="user_enable" name="enable" class="easyui-combobox" style="width:60px;height:22px" data-options="editable:false">
			<option value="1" selected>启用</option>
			<option value="0">禁用</option>
		</select></td>
	</tr><tr>
<!-- 		<td>头像地址</td> -->
<!-- 		<td colspan="9"><textarea class="easyui-textbox" id="user_portrait" name="portrait" style="width:599px;"></textarea></td> -->
<!-- 	</tr><tr> -->
		<td width="60px">备注</td>
		<td colspan="9"><textarea class="easyui-textbox" data-options="multiline:true" id="user_remark" name="remark" style="width:599px;height:60px;"></textarea></td>
	</tr></table></form></div>
	<div data-options="region:'south',border:false" style="height:38px;text-align:right;padding:5px 0;">
		<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveUser()">保存</a>
		<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winUser')">取消</a>
	</div></div>
</div>
</body>
</html>