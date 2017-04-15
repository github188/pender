<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>类型参数</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript">
var curRole, sysTypesByCache = [];
$(function() {
	showSysTypeCombo();
	<sec:authorize access="hq">showOrgCombo(false, true);</sec:authorize>
	<sec:authorize access="!hq">$('#q_sysType_orgId').combobox({editable:false,data:[{value:'',text:'所有'},{value:1,text:'系统平台'},{value:getCurUser().companyId,text:'自定义'}]});</sec:authorize>
	$('#sysTypeGrid').datagrid({
		url:'${pageContext.request.contextPath}/system/type/find.json'
	});
	var sysTypes = [], querySysTypes = [{'type':'', 'name':'所有'}];
	var cache = getAllSysTypes();
	for (var i = 0; i < cache.length; i++)
		sysTypesByCache.push(cache[i]);
	sysTypesByCache.push({'type':'SYS_TYPE', 'code':'SYS_TYPE', 'name':'系统类型'});
	for (var i = 0; i < sysTypesByCache.length; i++) {
		if (sysTypesByCache[i].type == 'SYS_TYPE') {
			querySysTypes.push({'type':sysTypesByCache[i].code, 'name':sysTypesByCache[i].name});
			sysTypes.push({'type':sysTypesByCache[i].code, 'name':sysTypesByCache[i].name});
		}
	}
	$('#q_sysType_type').combobox({data:querySysTypes});
	$('#sysType_type').combobox({data:sysTypes});
	upperTextBox('q_sysType_refId');
	upperTextBox('sysType_code');
	upperTextBox('sysType_refId');
});
function showSysTypeName(value, row, index) {
	for (var i = 0; i < sysTypesByCache.length; i++)
		if (value && sysTypesByCache[i].id == value)
			return sysTypesByCache[i].name;
	return value;
}
function showSysTypeDisplay(value, row, index) {
	return value == true ? '是' : '否';
}
function showSysTypeEdit(value, row, index) {
	return value == true ? '允许' : '禁止';
}
function showSysTypeOrg(value, row, index) {
	return value == 1 ? '系统平台' : '自定义';
}
function addSysType() {
	openDialog('winSysType', '新增类型参数');
	$('#sysType_refId').val('');
	<sec:authorize access="hq">
	$('#sysType_displayable').combobox('setValue', 1);
	$('#sysType_editable').combobox('setValue', 1);
	</sec:authorize>
	var type = $('#q_sysType_type').combobox('getValue');
	if (type)
		$('#sysType_type').combobox('setValue', type);
}
function updateSysType(row) {
	if (!row)
		row = $('#sysTypeGrid').datagrid('getSelected');
	if (row) {
		openDialog('winSysType', '修改类型参数');
		$('#fomSysType').form('load', row);
	} else {
		infoMsg('请选择需要修改的类型参数！');
	}
}
function saveSysType() {
	var row=$('#fomSysType').getValues();
	postForm({
		form:'fomSysType',
		url:'${pageContext.request.contextPath}/system/type/save.json',
		info:'保存类型参数成功！',
		task:function(data, statusText, xhr) {
			queryData('sysTypeGrid','sysTypeQueryForm');
		}
	});
}
function delSysTypes() {
	var rows = $('#sysTypeGrid').datagrid('getSelections');
	if (rows.length == 0) {
		infoMsg('请选择需要删除的类型参数！');
	} else {
		confirmMsg('您确定要删除该所选类型参数吗？', doDelSysTypes, [rows]);
	}
}
function doDelSysTypes(rows) {
	var ids = [];
	for (var i = 0; i < rows.length; i++)
		ids.push(rows[i].id);
	$.ajax({
		url:'${pageContext.request.contextPath}/system/type/delete.json',
		data:$.param({'ids':ids}, true),
		info:'所选类型参数删除成功！',
		task:function(data, statusText, xhr) {
			queryData('sysTypeGrid','sysTypeQueryForm');
		}
	});
}
</script>
</head>
<body class="easyui-layout">
<div data-options="region:'north',border:false"><table><tr><td>
	<form id="sysTypeQueryForm"><input type="hidden" id="q_user_orgId" name="orgId" value="">
	<table style="border:1px solid #ccc;"><tr>
		<td style="padding:5px 5px 5px 10px;">类型分类</td><td style="padding:5px 0px 5px 0px;"><div id="q_sysType_type" name="type" class="easyui-combobox" style="width:100px" data-options="valueField:'type',textField:'name',editable:false"></div></td>
		<td style="padding:5px 5px 5px 15px;">关联类型</td><td style="padding:5px 0px 5px 0px;"><input id="q_sysType_refId" name="refId" class="systype-combo" style="width:100px"></td>
		<sec:authorize access="hq"><td style="padding:5px 5px 5px 15px;">是否显示</td><td style="padding:5px 0px 5px 0px;"><select id="q_sysType_displayable" name="displayable" class="easyui-combobox" style="width:60px;" data-options="editable:false">
			<option value="" selected>所有</option>
			<option value="1">是</option>
			<option value="0">否</option>
		</select></td>
		<td style="padding:5px 5px 5px 15px;">允许编辑</td><td style="padding:5px 0px 5px 0px;"><select id="q_sysType_editable" name="editable" class="easyui-combobox" style="width:60px;" data-options="editable:false">
			<option value="" selected>所有</option>
			<option value="1">允许</option>
			<option value="0">禁止</option>
		</select></td></sec:authorize>
		<sec:authorize access="!hq"><td style="padding:5px 5px 5px 15px;">所属类型</td><td style="padding:5px 10px 5px 0px;"><input id="q_sysType_orgId" name="orgId" class="easyui-combobox" style="width:80px"></td></sec:authorize>
		<sec:authorize access="hq"><td style="padding:5px 5px 5px 15px;">所属机构</td><td style="padding:5px 10px 5px 0px;"><input id="q_sysType_orgId" name="orgId" class="org-combo" style="width:150px"></td></sec:authorize>
	</tr></table></form></td><sec:authorize access="find">
		<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-search'" href="javascript:void(0)" onclick="queryData('sysTypeGrid','sysTypeQueryForm')">查询</a></td>
		<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-no'" href="javascript:void(0)" onclick="resetForm('sysTypeQueryForm')">重置</a></td>
	</sec:authorize></tr>
</table></div>
<div id="sysTypeOpt">
	<sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增类型参数'" onclick="addSysType()"></a></sec:authorize>
	<sec:authorize access="update,save"><a href="javascript:void(0)" class="icon-edit easyui-tooltip" data-options="content:'修改类型参数'" onclick="updateSysType()"></a></sec:authorize>
	<sec:authorize access="delete"><a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除类型参数'" onclick="delSysTypes()"></a></sec:authorize>
</div>
<div data-options="region:'center',border:false"><table id="sysTypeGrid" data-options="nowrap:false,striped:true,fit:true,border:false,idField:'id',tools:'#sysTypeOpt'" title="类型参数列表">
	<thead>
		<tr>
			<th data-options="field:'ck',checkbox:true,width:20"></th>
			<th data-options="field:'type',width:100">类型分类</th>
			<th data-options="field:'code',width:100">类型编码</th>
			<th data-options="field:'name',width:200">类型名称</th>
			<th data-options="field:'value',width:100">类型值</th>
			<th data-options="field:'refId',width:100,formatter:showSysTypeName">关联类型</th>
			<sec:authorize access="hq"><th data-options="field:'displayable',width:60,formatter:showSysTypeDisplay">是否显示</th>
			<th data-options="field:'editable',width:60,formatter:showSysTypeEdit">允许编辑</th></sec:authorize>
			<sec:authorize access="!hq"><th data-options="field:'orgId',width:100,formatter:showSysTypeOrg">所属类别</th></sec:authorize>
			<sec:authorize access="hq"><th data-options="field:'orgName',width:100">所属机构</th></sec:authorize>
			<th data-options="field:'remark',width:300">备注</th>
		</tr>
	</thead>
</table></div>
<div id="winSysType" class="easyui-dialog" data-options="closed:true" style="width:478px;height:315px;padding:5px">
	<div class="easyui-layout" data-options="fit:true">
	<div data-options="region:'center',border:false" style="padding:10px 10px;background:#fff;border:1px solid #ccc;">
	<form id="fomSysType" method="post">
	<input type="hidden" id="sysType_id" name="id">
	<input type="hidden" id="sysType_sysType" name="sysType">
	<input type="hidden" id="sysType_orgId" name="orgId">
	<input type="hidden" id="sysType_createTime" name="createTime">
	<input type="hidden" id="sysType_createUser" name="createUser">
	<input type="hidden" id="sysType_del" name="del">
	<table><tr>
		<td width="50px">类型分类</td>
		<td><input id="sysType_type" name="type" class="easyui-combobox" style="width:145px" data-options="valueField:'type',textField:'name',required:true,prompt:'必填项'"></td><td width="20px"></td>
		<td width="50px">类型编码</td>
		<td><input id="sysType_code" name="code" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项'"></td>
	</tr><tr>
		<td width="50px">类型名称</td>
		<td><input id="sysType_name" name="name" class="easyui-textbox" style="width:145px" data-options="required:true,prompt:'必填项'"></td><td width="20px"></td>
		<td width="50px">关联类型</td>
		<td><input id="sysType_refId" name="refId" class="systype-combo" style="width:145px"></td>
	</tr><sec:authorize access="hq"><tr>
		<td width="50px">是否显示</td>
		<td><select id="sysType_displayable" name="displayable" class="easyui-combobox" style="width:145px"  data-options="editable:false">
			<option value="1" selected>是</option>
			<option value="0">否</option>
		</select></td><td width="20px"></td>
		<td width="50px">允许编辑</td>
		<td><select id="sysType_editable" name="editable" class="easyui-combobox" style="width:145px" data-options="editable:false">
			<option value="1" selected>允许</option>
			<option value="0">禁止</option>
		</select></td>
	</tr></sec:authorize><tr>
		<td>参数值</td>
		<td colspan="4"><textarea class="easyui-textbox" data-options="multiline:true" id="sysType_value" name="value" style="width:372px;height:60px;"></textarea></td>
	</tr><tr>
		<td>备注</td>
		<td colspan="4"><textarea class="easyui-textbox" data-options="multiline:true" id="sysType_remark" name="remark" style="width:372px;height:60px;"></textarea></td>
	</tr></table></form></div>
	<div data-options="region:'south',border:false" style="height:38px;text-align:right;padding:5px 0;">
		<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveSysType()">保存</a>
		<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winSysType')">取消</a>
	</div></div>
</div>
</body>
</html>