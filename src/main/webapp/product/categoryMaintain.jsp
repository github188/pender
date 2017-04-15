<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>类目维护</title>
<%@ include file="/common.jsp"%>
<style type="text/css">
	.cate-wrap {box-sizing: border-box; height: 100%; padding: 10px;}
</style>
<script type="text/javascript">

	//类目筛选
	function getCate() {
		var Cate = {};
		Cate.first = (function(){
			var first = [];
			for (var i = 0; i < categories.length; i++) {
				if (!categories[i].parentId) {
					first.push($.extend({}, categories[i]));
				}
			}
			return first;
		}());
		Cate.second = (function(){
			var first = Cate.first;
			var second = [];
			for (var i = 0; i < categories.length; i++) {
				for (var j = 0; j < first.length; j++) {
					if (categories[i].parentId == first[j].id) {
						second.push($.extend({}, categories[i]));
						break;
					}
				}
			}
			return second;
		}());
		Cate.third = (function(){
			var second = Cate.second;
			var third = [];
			for (var i = 0; i < categories.length; i++) {
				for (var j = 0; j < second.length; j++) {
					if (categories[i].parentId == second[j].id) {
						third.push($.extend({}, categories[i]));
						break;
					}
				}
			}
			return third;
		}());
		return Cate;
	}

	//从服务器获取类目数据
	function getCateData() {
		$.ajax({
			url: '${pageContext.request.contextPath}/product/categoryMaintain/find.json',
			async: false,
			success: function(data) {
				categories = data.rows;
				Cate = getCate();
			}
		})
	}

	//获取筛选后的分级类目对象
	var Cate, categories;

	getCateData();

	$(function(){

		//表格初始化配置
		var gridOpt = {
			singleSelect: true,
			fitColumns: true,
			pagination: false,
			striped: true
			
		}

		//一级类目
		$('#cateFirst').datagrid(
			$.extend({}, gridOpt, {
				onSelect: function(rowIndex, rowData) {
					var id = rowData.id;
					var secondCate = Cate.second;
					var secondFilter = [];
					for (var i = 0; i < secondCate.length; i++) {
						if (secondCate[i].parentId == id) {
							secondFilter.push(secondCate[i]);
						}
					}
					if (secondFilter.length == 0) {
						$('#cateSecond').datagrid('loadData', {page:1, total:0, rows:[]});
						$('#cateThird').datagrid('loadData', {page:1, total:0, rows:[]});
						return;
					};
					$('#cateSecond').datagrid('loadData', {page:1, total:secondFilter.length, rows:secondFilter});
					$('#cateSecond').datagrid('selectRow', 0);
				}
			})
		);

		//二级类目
		$('#cateSecond').datagrid(
			$.extend({}, gridOpt, {
				onSelect: function(rowIndex, rowData) {
					var id = rowData.id;
					var thirdCate = Cate.third;
					var thirdFilter = [];
					for (var i = 0; i < thirdCate.length; i++) {
						if (thirdCate[i].parentId == id) {
							thirdFilter.push(thirdCate[i]);
						}
					}
					if (thirdFilter.length == 0) {
						$('#cateThird').datagrid('loadData', {page:1, total:0, rows:[]});
						return;
					};
					$('#cateThird').datagrid('loadData', {page:1, total:thirdFilter.length, rows:thirdFilter});
					$('#cateThird').datagrid('selectRow', 0);
				}
			})
		);

		//三级类目
		$('#cateThird').datagrid(gridOpt);

		//载入一级类目数据	
		$('#cateFirst').datagrid('loadData', {page:1, total:Cate.first.length, rows:Cate.first});
		if (Cate.first.length != 0) {
			$('#cateFirst').datagrid('selectRow', 0);
		}
	})

	//新增、编辑类目
	function openWinCate(param, level) {
		if (param == 'add') {//新增
			openDialog('winCate', '新增类目');
			if (level == 2) {//新建二级类目
				var parent = $('#cateFirst').datagrid('getSelected');
				if (!parent) {
					infoMsg('请选择所属一级类目');
					return;
				}
				$('#fomCate').find('input[name="parentId"]').val(parent.id);
			}
			if (level == 3) {//新建三级类目
				var parent = $('#cateSecond').datagrid('getSelected');
				if (!parent) {
					infoMsg('请选择所属二级类目');
					return;
				}
				$('#fomCate').find('input[name="parentId"]').val(parent.id);
			}
		} else {//编辑
			openDialog('winCate', '编辑类目');
			for (var i = 0; i < categories.length; i++) {
				if (categories[i].id == param) {
					var row = categories[i];
					break;
				}
			}
			$('#fomCate').form('load', row);
		}
	}

	//保存类目
	function saveCate() {
		if(!$('#fomCate').form('enableValidation').form('validate')) return;
		var data = $('#fomCate').serialize();
		$.ajax({
			url: '${pageContext.request.contextPath}/product/categoryMaintain/save.json',
			data: data,
			info: '保存成功！',
			success: function(r) {
				closeDialog('winCate');
				resetForm('fomCate');
				getCateData();
		  		$('#cateFirst').datagrid('loadData', {page:1, total:Cate.first.length, rows:Cate.first});
				$('#cateFirst').datagrid('selectRow', 0);
			}
		})
	}

	//删除类目
	function openWinCateDel(id) {
		var ids = [id];
		confirmMsg('您确定要删除该类目吗?', cateDel, [ ids ]);
	}
	function cateDel(ids) {
		if (ids.length !== 0) {
			$.ajax({
			  	url : '${pageContext.request.contextPath}/product/categoryMaintain/delete.json',
			  	data : $.param({
					'ids' : ids
			  	}, true),
			  	info : '删除成功！',
			  	success: function(r) {
			  		closeDialog('winCate');
			  		getCateData();
			  		$('#cateFirst').datagrid('loadData', {page:1, total:Cate.first.length, rows:Cate.first});
					$('#cateFirst').datagrid('selectRow', 0);
			  	}
			});
		}
	}

	function formatOperate(value, row) {
		return '<a href="javascript:void(0)" class="icon-edit easyui-tooltip" data-options="content:\'编辑\'" onclick="openWinCate('+row.id+')" style="display:inline-block;width:16px;height:16px;margin-right:5px;"></a>'
				+'<a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:\'删除\'" onclick="openWinCateDel('+row.id+')" style="display:inline-block;width:16px;height:16px;"></a>';
	}
</script>
</head>
<body class="easyui-layout">
	<!-- 一级类目 -->
	<div data-options="region:'west',border:false,split:true,collapsible:false,headerCls:'list-head',tools:'#firstCateBtns'" title="一级类目" style="width:33.33%">
		<div id="firstCateBtns">
			<sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增类目'" onclick="openWinCate('add', 1);"></a></sec:authorize>
		</div>
		<div class="cate-wrap">
			<table id="cateFirst" class="easyui-datagrid">
				<thead>
					<tr>
						<th field="ck" checkbox="true"></th>
						<th field="code" width="100" align="center">类目编码</th>
						<th field="name" width="100" align="center">类目名称</th>
						<sec:authorize access="update,save,delete"><th field="operate" align="center" formatter="formatOperate">操作</th></sec:authorize>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<!-- 二级类目 -->
	<div data-options="region:'center',border:false,split:true,collapsible:false,headerCls:'list-head',tools:'#secondCateBtns'" title="二级类目" style="width:33.33%">
		<div id="secondCateBtns">
			<sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增类目'" onclick="openWinCate('add', 2);"></a></sec:authorize>
		</div>
		<div class="cate-wrap">
			<table id="cateSecond" class="easyui-datagrid">
				<thead>
					<tr>
						<th field="ck" checkbox="true"></th>
						<th field="code" width="100" align="center">类目编码</th>
						<th field="name" width="100" align="center">类目名称</th>
						<sec:authorize access="update,save,delete"><th field="operate" align="center" formatter="formatOperate">操作</th></sec:authorize>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<!-- 三级类目 -->
	<div data-options="region:'east',border:false,split:true,collapsible:false,headerCls:'list-head',tools:'#thirdCateBtns'" title="三级类目" style="width:33.33%">
		<div id="thirdCateBtns">
			<sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增类目'" onclick="openWinCate('add', 3);"></a></sec:authorize>
		</div>
		<div class="cate-wrap">
			<table id="cateThird" class="easyui-datagrid">
				<thead>
					<tr>
						<th field="ck" checkbox="true"></th>
						<th field="code" width="100" align="center">类目编码</th>
						<th field="name" width="100" align="center">类目名称</th>
						<sec:authorize access="update,save,delete"><th field="operate" align="center" formatter="formatOperate">操作</th></sec:authorize>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="winCate" class="easyui-dialog" data-options="closed:true, buttons:'#cateBtns'" style="width:264px;height:155px;">
		<div style="padding:15px 35px;">
			<form id="fomCate">
				<input type="hidden" name="code">
				<input type="hidden" name="countOrg">
				<input type="hidden" name="countSKU">
				<input type="hidden" name="countSale">
				<input type="hidden" name="id">
				<input type="hidden" name="logo">
				<input type="hidden" name="level">
				<input type="hidden" name="createTime">
				<input type="hidden" name="createUser">
				<input type="hidden" name="taxRate">
				<input type="hidden" name="parentId">
				<input type="hidden" name="parentCode">
				<div class="form-row">
					<div class="form-cloumn form-item" style="width:100%;">
						<input id="cate-name" name="name" class="easyui-textbox" data-options="required:true,prompt:'类目名称'">
					</div>
				</div>
				<div id="cateBtns">
					<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveCate()">保存</a>
					<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winCate')">取消</a>
				</div>
			</form>
		</div>
	</div>
</body>
</html>