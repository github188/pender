<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>商品下线</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript">
	$(function() {
		$('#productList').datagrid({
			url: '${pageContext.request.contextPath}/product/productOffLine/find.json'
		})
	})
	function showConfirm() {
		confirmMsg('确定要将该商品下线吗？', offlinePro);
	}
	function offlinePro() {
		var row = $('#productList').datagrid('getSelected');
		$.ajax({
			data: {id: row.id, code: row.code},
			url: '${pageContext.request.contextPath}/product/productOffLine/save.json',
			info: '操作成功！',
			task: function() {
				row.offline = true;
				var index = $('#productList').datagrid('getRowIndex');
				$('#productList').datagrid('refreshRow', index);
			}
		})
	}

	//解析商品图片路径
	function getProductImgUrl(value) {
		if (!value) return '<img style="width:70px;height:70px;margin:10px 0;" />';
		var path = value.split(';')[0].split(',')[3];
		if (!path) {
			return '<img style="width:70px;height:70px;margin:10px 0;" />';
		}
		return '<img src="'+getFileUrl(path)+'" style="width:70px;height:70px;margin:10px 0;" />';
	}
	//解析商品类型
	function getProductType(value) {
		if (value == 1) {
			return '自有';
		} else if (value == 2) {
			return '平台供货';
		} else {
			return value;
		}
	}
	function formatterOperate(value, row) {
		if (row.offline) return '';
		return '<span style="color:#0075bf;padding:5px 10px;cursor:pointer;" onclick="showConfirm()">下线</span>'
	}
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="productsSearch" class="search-form">
			<div class="form-item">
				<div class="text">商品名称</div>
				<div class="input">
					<input id="q_apply_skuName" name="skuName" class="easyui-textbox" data-options="prompt:'商品名称'" />
				</div>
			</div>
			<div class="form-item">
				<div class="text">商品编码</div>
				<div class="input">
					<input id="q_apply_code" name="code" class="easyui-textbox" data-options="prompt:'商品编码'" />
				</div>
			</div>
		</form>
		<div class="search-btn" onclick="queryData('productList','productsSearch')">查询</div>
		<div class="search-btn" onclick="resetForm('productsSearch')">重置</div>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head'" title="商品信息列表">
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<!-- 商品列表 -->
			<table id="productList" class="easyui-datagrid" data-options="fit:true,fitColumns:true,singleSelect:true">
				<thead>
					<tr>
						<th field="images" width="90" align="center" formatter="getProductImgUrl">商品图片</th>
						<th field="code" width="150" align="center">商品编码</th>
						<th field="skuName" width="200" align="center">商品名称</th>
						<th field="orgName" width="200" align="center">所属人</th>
						<th field="type" width="70" align="center" formatter="getProductType">商品类型</th>
						<th field="operate" width="70" align="center" formatter="formatterOperate">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</body>
</html>