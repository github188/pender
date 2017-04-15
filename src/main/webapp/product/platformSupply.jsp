<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>平台供货</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript">
$(function() {
	//商品列表
	$('#productList').datagrid({
		fit:true,
		fitColumns: true,
		url: '${pageContext.request.contextPath}/product/platformSupply/find.json'
	});

	//供货列表
	$('#supplyList').datagrid({
		fit:true,
		fitColumns: true,
		onSelect: function(rowIndex) {
			$('#supplyList').datagrid('unselectRow', rowIndex);
		}
	});

	//供货对象
	$('#supply-org').combobox({
		url: '${pageContext.request.contextPath}/product/platformSupply/findSupplyObject.json',
		loadFilter: function(data) {
			data.unshift({id:'', name:'请选择'});
			return data;
		}
	})
})

//打开供货窗口，生成供货列表
function openSupplyList() {
	var rows = $('#productList').datagrid('getSelections');
	rows = $.extend([], rows);
	if (rows.length == 0) {
		infoMsg('请选择需要供货的商品！');
		return;
	}
	$('#supplyList').datagrid('loadData', {total:rows.length, rows:rows});
	openDialog('winSupply', '确认供货单');
}

//确认供货
function saveSupply() {
	var reg = new RegExp('^[1-9]\d*|0$');//匹配非负整数
	var $supplyNum = $('#winSupply').find('input[name="supplyNum"]');
	var rows = $('#supplyList').datagrid('getRows');
	var supplyOrgId = $('#supply-org').combobox('getValue');
	if (!supplyOrgId) {
		infoMsg('请选择供货对象');
		return;
	}
	var products = [];
	for (var i = 0; i < rows.length; i++) {
		var supplyNum = $supplyNum.eq(i).val();
		if (!reg.test(supplyNum) || +supplyNum > rows[i].stock) {
			infoMsg('第'+(i+1)+'个商品供货数量有误！');
			return;
		} else {
			if (+supplyNum == 0) continue;
			var product = {id: rows[i].id, supplyCount: +supplyNum};
			products.push(JSON.stringify(product));
		}
	}
	$.ajax({
		url: '${pageContext.request.contextPath}/product/platformSupply/savePlatformSupply.json',
		data: $.param({
			'supplyOrgId':supplyOrgId,
			'products':products
		}, true),
		task: function() {
			$('#productList').datagrid('reload');
			closeDialog('winSupply');
			infoMsg('供货成功！');
			$('#supply-org').combobox('setValue', '');
		}
	})
}

//解析商品图片路径
function getProductImgUrl(value) {
	var path = value.split(';')[0].split(',')[3];
	if (!path) {
		return '<img style="width:70px;height:70px;margin:15px 0;" />';
	}
	return '<img src="'+getFileUrl(path)+'" style="width:70px;height:70px;margin:15px 0;" />';
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
//供货数量
function getSupplyNumInput(value) {
	return '<input type="number" name="supplyNum" min="0" value="0" style="width:60px;" />';
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
			<div class="form-item">
				<div class="text">商品类型</div>
				<div class="input">
					<select id="q_apply_type" class="easyui-combobox" name="type" data-options="panelHeight:'auto'">
					    <option value="">请选择</option>
					    <option value="1">自有</option>
					    <option value="2">平台供货</option>
					</select>
				</div>
			</div>
		</form>
		<div class="search-btn" onclick="queryData('productList','productsSearch')">查询</div>
		<div class="search-btn" onclick="resetForm('productsSearch')">重置</div>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#productListTools'" title="商品信息列表">
		<!-- 工具栏 -->
		<div id="productListTools">
			<a href="javascript:void(0)" class="icon-cart easyui-tooltip" data-options="content:'马上供货'" onclick="openSupplyList()" style="opacity:1;"></a>
		</div>
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<!-- 商品列表 -->
			<table id="productList" class="easyui-datagrid">
				<thead>
					<tr>
						<th field="ck" checkbox="true"></th>
						<th field="images" width="100" align="center" formatter="getProductImgUrl">商品图片</th>
						<th field="code" width="150" align="center">商品编码</th>
						<th field="skuName" width="200" align="center">商品名称</th>
						<th field="brand" width="80" align="center">品牌</th>
						<th field="origin" width="80" align="center">原产地</th>
						<th field="spec" width="100" align="center">规格</th>
						<th field="price" width="50" align="center">标准价</th>
						<th field="priceCombo" width="50" align="center">组合价</th>
						<th field="stock" width="70" align="center">库存</th>
						<th field="type" width="70" align="center" formatter="getProductType">商品类型</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="winSupply" class="easyui-dialog" data-options="closed:true, buttons:'#supplyBtns'" style="width:900px;height:600px;">
		<div style="padding:15px;">
			<div class="form-item">
				<div class="text">请选择供货对象</div>
				<input id="supply-org" class="easyui-combobox" name="supply-org" value="" data-options="valueField: 'id',textField: 'name',panelHeight:'auto'">
			</div>
			<!-- 供货列表 -->
			<div style="height:435px; margin-top:10px;">
				<table id="supplyList" class="easyui-datagrid">
					<thead>
						<tr>
							<th field="images" width="100" align="center" formatter="getProductImgUrl">商品图片</th>
							<th field="code" width="150" align="center">商品编码</th>
							<th field="skuName" width="200" align="center">商品名称</th>
							<th field="spec" width="100" align="center">规格</th>
							<th field="price" width="50" align="center">标准价</th>
							<th field="priceCombo" width="50" align="center">组合价</th>
							<th field="stock" width="70" align="center">库存</th>
							<th field="supplyNum" width="90" align="center" formatter="getSupplyNumInput">供货数量</th>
						</tr>
					</thead>
				</table>
			</div>
			<div id="supplyBtns">
				<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveSupply()">保存</a>
				<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winSupply')">取消</a>
			</div>
		</div>
	</div>
</body>
</html>