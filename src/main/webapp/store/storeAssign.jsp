<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>店铺分配</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript">
	var pointTypes = [];

	//formatter
	function formatPointType(value) {
		for (var i = 0; i < pointTypes.length; i++)
			if (pointTypes[i].code == value)
				return pointTypes[i].name;
		return value;
	}

	function formatOperate(val, row) {
		return '<a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:\'删除\'" onclick="saveUnbindPoint(\''+row.id+'\')" style="display:inline-block;width:16px;height:16px;"></a>'
	}

	function formatterState(val) {
		if (val) {
			return '启用';
		} else {
			return '禁用';
		}
	}

	//添加用户
	function addAccount() {
		openWin('winAccount');
	}

	//清空选择的用户
	function clearSelectedAccount() {
		var $tree = $('#accountTree');
		var roots = $tree.tree('getRoots');
		for (var i = 0; i < roots.length; i++) {
			$tree.tree('uncheck', roots[i].target);
		}
	}

	//确认添加用户
	function doAddAccount() {
		var $tree = $('#accountTree'),
			$grid = $('#accountGrid');

		var rows = $grid.datagrid('getRows');
		var checkeds = $tree.tree('getChecked');

		for (var i = 0; i < checkeds.length; i++) {
			if ($tree.tree('isLeaf', checkeds[i].target)) {
				var isExist = rows.filter(function(n) {
					return n.id == checkeds[i].id;
				}).length ? true : false;

				if (!isExist) {
					$grid.datagrid('appendRow', checkeds[i]);
				}
			}
		}
		
		closeWin('winAccount');
	}

	//分配店铺
	function bindPoint() {
		var selected = $('#accountGrid').datagrid('getSelected');

		if (!selected) {
			infoMsg('请选中要分配店铺的用户');
			return;
		}

		if (!selected.enabled) {
			infoMsg('所选用户已被禁用，若要为其分配店铺，请先启用该用户');
			return;
		}

		openWin('winBindPoint');

		$('#pointBindGrid').datagrid({
			url: '${pageContext.request.contextPath}/store/storeAssign/findNotAssignedStore.json',
			loadFilter: function(data) {
				data.rows = data.pointPlaceList;
				return data;
			}
		})
	}

	//确认分配
	function saveBindPoint() {
		var account = $('#accountGrid').datagrid('getSelected');
		var points = $('#pointBindGrid').datagrid('getSelections');

		if (points.length == 0) {
			infoMsg('请选择要分配的店铺');
			return;
		}

		var pointIds = [];
		$.each(points, function(i, point) {
			pointIds.push(point.id);
		})

		$.ajax({
			url: '${pageContext.request.contextPath}/store/storeAssign/saveAssignStoresToAccount.json',
			data: $.param({userId:account.id, storeIds:pointIds}, true),
			info: '店铺分配成功！',
			task: function(r) {
				closeWin('winBindPoint');
				$('#pointGrid').datagrid('reload');
			}
		})
	}

	//解绑店铺
	function saveUnbindPoint(id) {
		var ids = [];

		if (id) {
			ids.push(id);
		} else {
			var points = $('#pointGrid').datagrid('getSelections');
			$.each(points, function(i, point) {
				ids.push(point.id);
			})
		}

		if (ids.length == 0) {
			infoMsg('请选择店铺');
			return;
		}

		confirmMsg('确定解除所选店铺吗？', function() {
			$.ajax({
				url: '${pageContext.request.contextPath}/store/storeAssign/saveReleaseStoresAssign.json',
				data: $.param({storeIds:ids}, true),
				info: '店铺解除成功！',
				task: function(r) {
					$('#pointGrid').datagrid('reload');
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

		$('#accountGrid').datagrid({
			url: '${pageContext.request.contextPath}/store/storeAssign/findAccountsAssignedStores.json',
			loadFilter: function(data) {
				data.rows = data.userList;
				return data;
			},
			onSelect: function(index, row) {
				$('#pointGrid').datagrid({
					url: '${pageContext.request.contextPath}/store/storeAssign/findStoresAssignedToAccount.json',
					queryParams: {userId:row.id},
					loadFilter: function(data) {
						data.rows = data.pointPlaceList;
						return data;
					}
				})
			}
		})

		$('#accountTree').tree({
			checkbox: true,
			url: '${pageContext.request.contextPath}/store/storeAssign/findAccountsNotAssignedStore.json',
			loadFilter: function(data) {
				var roleList = data.roleList.filter(function(role) {
					return role.users.length != 0;
				});
				for (var i = 0; i < roleList.length; i++) {
					roleList[i].id = roleList[i].user_id;
					roleList[i].text = roleList[i].name;
					roleList[i].children = roleList[i].users;
					var users = roleList[i].children;
					for (var j = 0; j < users.length; j++) {
						users[j].text = users[j].realName;
					}
				}
				return roleList;
			}
		})
	})
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'west',border:false,headerCls:'list-head',split:true,tools:'#accountOpt'" title="已分配店铺用户" style="width:30%;padding:10px;">
		<div id="accountOpt">
			<a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'添加用户'" onclick="addAccount()"></a>
		</div>
		<table id="accountGrid" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,singleSelect:true,pagination:false,idField:'id'">
			<thead>
				<tr>
					<th data-options="field:'realName',width:120,align:'center'">用户姓名</th>
					<th data-options="field:'mobile',width:120,align:'center'">联系方式</th>
					<th data-options="field:'enabled',width:50,align:'center',formatter:formatterState">状态</th>
				</tr>
			</thead>
		</table>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#storeOpt'" title="用户店铺列表" style="width:70%;padding:10px;">
		<div id="storeOpt">
			<sec:authorize access="add,delete">
			<a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'分配店铺'" onclick="bindPoint()"></a>
			<a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'解除店铺'" onclick="saveUnbindPoint()"></a>
			</sec:authorize>
		</div>
		<table id="pointGrid" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,pagination:false">
			<thead>
				<tr>
					<th data-options="checkbox:true,field:'',width:20" align="center"></th>
					<th data-options="field:'pointNo',width:80" align="center">店铺编号</th>
					<th data-options="field:'pointName',width:150" align="center">店铺名称</th>
					<th data-options="field:'pointAddress',width:150" align="center">店铺地址</th>
					<th data-options="field:'pointType',width:100,formatter:formatPointType" align="center">店铺类型</th>
					<sec:authorize access="delete"><th data-options="field:'operate',width:50,formatter:formatOperate" align="center">操作</th></sec:authorize>
				</tr>
			</thead>
		</table>
	</div>
	<div id="winAccount" class="easyui-dialog" data-options="closed:true,buttons:'#accountBtns',onClose:clearSelectedAccount" title="用户列表" style="width:400px;height:600px;padding:15px;">
		<ul id="accountTree"></ul>
		<div id="accountBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="doAddAccount()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeWin('winAccount');">取消</a>
		</div>
	</div>
	<div id="winBindPoint" class="easyui-dialog" data-options="closed:true,buttons:'#pointBindBtns'" title="分配店铺" style="width:900px;height:600px;">
		<table id="pointBindGrid" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,border:false">
			<thead>
				<tr>
					<th data-options="checkbox:true,field:'',width:20" align="center"></th>
					<th data-options="field:'pointNo',width:80" align="center">店铺编号</th>
					<th data-options="field:'pointName',width:150" align="center">店铺名称</th>
					<th data-options="field:'pointAddress',width:150" align="center">店铺地址</th>
					<th data-options="field:'pointType',width:100,formatter:formatPointType" align="center">店铺类型</th>
				</tr>
			</thead>
		</table>
		<div id="pointBindBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveBindPoint()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeWin('winBindPoint');">取消</a>
		</div>
	</div>
</body>
</html>
