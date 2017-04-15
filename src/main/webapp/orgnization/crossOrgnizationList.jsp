<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>跨组织管理</title>
	<%@ include file="/common.jsp"%>
	<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/distpicker.data.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/distpicker.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/areaNum.js"></script>
	<style type="text/css">
		.area-select {height: 28px; border-color: #ddd; outline: none;}
	</style>
</head>
<body class="easyui-layout">
	<div data-options="region:'west',border:false,headerCls:'list-head',split:true" title="组织结构" style="width:20%;">
		<ul id="orgTree" style="margin:15px;"></ul>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#orgOpt'" title="组织列表" style="padding:10px;">
		<div id="orgOpt">
			<a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增组织'" onclick="addOrg()"></a>
			<a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除组织'" onclick="delOrg()"></a>
		</div>
		<table id="orgList" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,idField:'id'">
			<thead>
				<tr>
					<th data-options="checkbox:true,field:'',width:20,align:'center'"></th>
					<th data-options="field:'code',width:60,align:'center'">组织编号</th>
					<th data-options="field:'name',width:100,align:'center'">组织名称</th>
					<th data-options="field:'orgType',width:40,align:'center',formatter:foamatterOrgType">组织类型</th>
					<th data-options="field:'mode',width:40,align:'center',formatter:foamatterMode">合作方式</th>
					<th data-options="field:'settledTime',width:50,align:'center'">合作起始时间</th>
					<th data-options="field:'manager',width:50,align:'center'">联系人</th>
					<th data-options="field:'phone',width:60,align:'center'">联系电话</th>
					<th data-options="field:'operate',width:130,align:'center',formatter:formatterOperate">操作</th>
				</tr>
			</thead>
		</table>
	</div>
	<div id="winOrg" class="easyui-dialog" data-options="closed:true,buttons:'#orgBtns'" style="padding:15px 25px 25px;">
		<form id="formOrg">
			<input type="hidden" name="id">
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">组织名称</div>
					<input class="easyui-textbox" name="name" data-options="prompt:'组织名称',required:true">
				</div>
				<div class="form-cloumn form-item">
					<div class="text">组织别名</div>
					<input class="easyui-textbox" name="alias" data-options="prompt:'组织别名可用作登录账号',required:true">
				</div>
				<div class="form-cloumn form-item">
					<div class="text">组织类型</div>
					<select id="type" class="easyui-combobox" name="orgType" data-options="prompt:'请选择',required:true,editable:false,panelHeight:'auto',value:''">
						<option value="1">管理者</option>
						<option value="2">经营者</option>
					</select>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">合作方式</div>
					<select class="easyui-combobox" name="mode" data-options="prompt:'请选择',required:true,editable:false,panelHeight:'auto',value:''">
						<option value="1">合作</option>
						<option value="2">联营</option>
						<option value="3">加盟</option>
						<option value="4">自营</option>
					</select>
				</div>
				<div class="form-cloumn form-item">
					<div class="text">合作时间</div>
					<input class="easyui-datebox" name="settledTime" data-options="prompt:'起始时间',required:true,editable:false">
				</div>
				<div class="form-cloumn form-item">
					<div class="text">是否建立管理关系</div>
					<select class="easyui-combobox" name="isRelate" data-options="required:true,editable:false,panelHeight:'auto'">
						<option value="1">是</option>
						<option value="2">否</option>
					</select>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item" style="width:100%;">
					<div class="text">所在区域</div>
					<div id="distpicker" data-toggle="distpicker">
				        <select class="area-select easyui-validatebox" data-options="required:true" data-province="广东省" id="province" name="prov"></select> 
				        <select class="area-select easyui-validatebox" data-options="required:true" data-city="深圳市" id="city" name="city"></select>
				        <select class="area-select easyui-validatebox" data-options="required:true" data-district="南山区" id="district" name="dist"></select>
				    </div>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">联系人</div>
					<input class="easyui-textbox" name="manager" data-options="prompt:'联系人姓名',required:true">
				</div>
				<div class="form-cloumn form-item">
					<div class="text">联系电话</div>
					<input class="easyui-textbox" name="phone" data-options="prompt:'联系人电话号码',required:true,validType:['length[7,12]','allNum']">
				</div>
			</div>
			<div style="color:red;">提示：建立管理关系后，可看到对方的经营数据</div>
		</form>
		<div id="orgBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveOrg()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeWin('winOrg');">取消</a>
		</div>
	</div>
	<!-- 设备列表 -->
    <div id="winDev" class="easyui-dialog" data-options="closed:true,buttons:'#devBtns'" style="width:750px;height:500px;">
        <div style="padding:10px;">   
            <form id="deviceQueryForm" class="search-form">
                <input type="hidden" id="q_device_orgId" name="orgId" value="">       
                <span>设备组号：</span>
                <div class="form-item" style="width:380px;">
					<input class="easyui-textbox" name="startFactoryDevNo">
					至
					<input class="easyui-textbox" name="endFactoryDevNo">
                </div>
            </form>
            <sec:authorize access="find">
                <div class="search-btn" onclick="queryData('deviceGrid','deviceQueryForm')">查询</div>
                <div class="search-btn" onclick="resetForm('deviceQueryForm')">重置</div>
            </sec:authorize>
        </div>
        <div style="padding:0 10px 10px 10px;height:368px;">
	        <table id="deviceGrid" class="easyui-datagrid" style="height:400px;" data-options="nowrap:false,striped:true,fit:true,fitColumns:true,idField:'id'">
	            <thead>
	                <tr>
	                    <th data-options="checkbox:true,field:'',width:20"></th>
	                    <th data-options="field:'factoryDevNo',width:100">设备组号</th>
	                    <th data-options="field:'type',width:300,formatter:deviceType">设备类型</th>
	                </tr>
	            </thead>
	        </table>
	    </div>
	    <div id="devBtns">
	        <a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveBindOrUnbindDev()">保存</a>
	        <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winDev')">取消</a>
	    </div>
    </div>
	<script type="text/javascript">
		var deviceTypes = []; 

		$.extend($.fn.validatebox.defaults.rules,{
		    allNum: {
		    	validator: function(value) {
		    		return +value === +value;
		    	},
		    	message: '必须全为数字'
		    }
		});

		function deviceType(value) {
			for (var i = 0; i < deviceTypes.length; i++)
	            if (deviceTypes[i].code == value)
	                return deviceTypes[i].name;
	        return value;
	    }

		function foamatterOrgType(value) {
			switch (value) {
				case 1:
					return '管理者';
				case 2:
					return '经营者';
				default:
					return value;
			}
		}

		function foamatterMode(value) {
			switch (value) {
				case 1:
					return '合作';
				case 2:
					return '联营';
				case 3:
					return '加盟';
				case 4:
					return '自营';
				default:
					return value;
			}
		}

		function formatterOperate(value, row) {
			var edit = '<span class="u-btn" onclick="updateOrg('+row.id+')">编辑</span>',
				manage = '<span class="u-btn" onclick="changeRelate('+row.id+')">建立管理</span>',
				cancelManage = '<span class="u-btn" onclick="changeRelate('+row.id+')">取消管理</span>',
				waitReply = '<span class="u-btn disabled">等待确认</span>',
				bindDev = '<span class="u-btn" onclick="bindOrUnbindDev('+row.id+',1'+')">绑定设备</span>',
				unbindDev = '<span class="u-btn" onclick="bindOrUnbindDev('+row.id+',0'+')">解绑设备</span>';

			if (row.isRelate == 2 && row.applyRelate == 1) {
				var marageBtn = waitReply;
			} else if (row.isRelate == 1) {
				var marageBtn = cancelManage;
			} else {
				var marageBtn = manage;
			}

			return edit + marageBtn + bindDev + unbindDev;
		}

		function addOrg() {
			openWin('winOrg', '新增组织');
			$("#distpicker").distpicker('reset');
			$('#formOrg input[name="id"]').val('');
		}

		function updateOrg(id) {
			openWin('winOrg', '编辑组织');
			var row = $('#orgList').datagrid('getRows')[$('#orgList').datagrid('getRowIndex', id)];
			$('#formOrg').form('load', row);
			$('#province').val(row.prov).trigger('change');
			$('#city').val(row.city).trigger('change');
			$('#district').val(row.dist);
		}

		function saveOrg() {
			if (!validateForm('formOrg')) return;
			var row = $('#formOrg').getValues();
			row.areaCode = areaNum[row.city] || '8888';

			var selectedNode = $('#orgTree').tree('getSelected');
			if (!row.id && selectedNode) {
				row.parentId = selectedNode.id;
			}

			$.ajax({
				url: '${pageContext.request.contextPath}/orgnization/orgnizationList/save.json',
				data: row,
				info: '保存成功！',
				task: function() {
					closeWin('winOrg');
					$('#orgList').datagrid('reload');
					$('#orgTree').tree('reload', $('#orgTree').tree('getSelected').target);
				}
			})
		}

		function delOrg() {
			confirmMsg('确定删除所选组织吗？', function() {
				var selects = $('#orgList').datagrid('getSelections');
				var ids = [];
				for (var i = 0; i < selects.length; i++) {
					ids.push(selects[i].id);
				}
				$.ajax({
					url: '${pageContext.request.contextPath}/orgnization/orgnizationList/delete.json',
					data: $.param({ids: ids}, true),
					task: function() {
						$('#orgList').datagrid('reload');
						$('#orgTree').tree('reload', $('#orgTree').tree('getSelected').target);
					}
				})
			})
		}

		function changeRelate(id) {
			var row = $('#orgList').datagrid('getRows')[$('#orgList').datagrid('getRowIndex', id)];
			var applyRelate = row.isRelate == 1 ? 2 : 1;
			confirmMsg('确定' + (applyRelate == 1 ? '建立' : '取消') + '组织间的管理关系吗？', function() {
				$.ajax({
					url: '${pageContext.request.contextPath}/orgnization/orgnizationList/saveRelation.json',
					data: {orgId: id, applyRelate: applyRelate},
					task: function() {
						$('#orgList').datagrid('reload');
					}
				})
			})
		}

		function bindOrUnbindDev(id, type) {
			openWin('winDev', type ? '绑定设备' : '解绑设备');
			$('#winDev').data('id', id).data('type', type);
			if (type) {
				var queryParams = {};
			} else {
				var queryParams = {orgId: id};
			}
			$('#deviceGrid').datagrid({
				url: '${pageContext.request.contextPath}/orgnization/orgnizationList/findDevices.json',
				queryParams: queryParams
			})
		}

		function saveBindOrUnbindDev() {
			var orgId = $('#winDev').data('id');
			var type = $('#winDev').data('type');
			var devNos = [];
			var devSelections = $('#deviceGrid').datagrid('getSelections');
			if (devSelections.length) {
				for (var i = 0; i < devSelections.length; i++) {
					devNos.push(devSelections[i].devNo);
				}
			} else {
				infoMsg('请选择需要' + (type ? '绑定' : '解绑') + '的设备！');
				return;
			}

			$.ajax({
				url: '${pageContext.request.contextPath}/orgnization/orgnizationList/saveBindOrUnBindDevices.json',
				data: $.param({bindingFlag: type, orgId: orgId, devNos: devNos}, true),
				info: (type ? '绑定' : '解绑') + '设备成功！',
				task: function() {
					closeWin('winDev');
					$('#deviceGrid').datagrid('clearSelections');
				}
			})
		}

		$(function() {
			var cache = getAllSysTypes();
	        for (var i = 0; i < cache.length; i++) {
	            if (cache[i].type == 'DEVICE_TYPE2') {
	            	deviceTypes.push(cache[i]);
	            }
	        }

	        $('#orgTree').tree({
				url: '${pageContext.request.contextPath}/orgnization/orgnizationList/findOrgnizationsByParentId.json',
				loadFilter: function(data) {
					var tree = [];
					var rows = data.rows;
					if (rows.length) {
						for (var i = 0; i < rows.length; i++) {
							tree.push({id:rows[i].id, text:rows[i].name, state:'closed'});
						}
					}
					return tree;
				},
				onClick: function(node) {
					$('#orgList').datagrid({
						url: '${pageContext.request.contextPath}/orgnization/orgnizationList/find.json',
						queryParams: {id:node.id}
					});
				}
			})

			$('#orgList').datagrid({
				onClickCell: function(index, field) {
					if (field === 'operate') {
						var self = this;
						setTimeout(function(){
							$(self).datagrid('unselectRow', index);
						},0)	
					}
				}
			})
		})
	</script>
</body>
</html>