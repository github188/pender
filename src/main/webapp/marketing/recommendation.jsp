<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>关联推荐</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-cellediting.js"></script>
<script type="text/javascript">
	$(function() {
		$('#recommendGrid').datagrid({
			url: '${pageContext.request.contextPath}/marketing/recommendation/findProductRecommends.json',
			onEndEdit: function(index, row, changes) {
				if (typeof changes.relevancy === 'undefined') return;
				$.ajax({
		            data: {id: row.id, relevancy: changes.relevancy},
		            url: '${pageContext.request.contextPath}/marketing/recommendation/saveRecommendRelevancy.json',
		            info: '修改成功'
		        })
			}
		}).datagrid('enableCellEditing');
	});
</script>
</head>
<body class="easyui-layout" >
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="recommendQueryForm" class="search-form">
			<div class="form-item" style="margin-right:0px;">
				<div class="text">商品名称</div>
				<div class="input">
					<input name="skuName" class="easyui-textbox">
				</div>
			</div>
			<span>--</span>
			<div class="form-item">
				<div class="text">关联商品名称</div>
				<div class="input">
					<input name="recommendSkuName" class="easyui-textbox">
				</div>
			</div>
		</form>
		<sec:authorize access="find">
			<div class="search-btn" onclick="queryData('recommendGrid','recommendQueryForm')">查询</div>
			<div class="search-btn" onclick="resetForm('recommendQueryForm')">重置</div>
		</sec:authorize>
	</div>
	<div data-options="region:'center',border:false,split:true,headerCls:'list-head',tools:'#recommendOpt'" title="关联详情">
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<table id="recommendGrid" data-options="nowrap:false,striped:true,fit:true,fitColumns:true,idField:'id',singleSelect:true">
				<thead>
					<tr>
						<!-- <th data-options="checkbox:true,field:'',width:20,align:'center'"></th> -->
						<th data-options="field:'skuName',width:300,align:'center'">商品名称</th>
						<th data-options="field:'recommendSkuName',width:300,align:'center'">关联商品名称</th>
						<th data-options="field:'relevancy',width:100,align:'center',editor:{type:'numberbox',options:{required:true,precision:0,min:0}}">当前关联度</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</body>
</html>