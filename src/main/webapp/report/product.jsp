<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>商品销量统计</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/base64.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<script type="text/javascript">
	$(function() {
		showDateCombo();
		var now = new Date();
		$('#cityPartnerGrid').datagrid({
			queryParams:$('#cityPartnerQueryForm').getValues(),
			url:'${pageContext.request.contextPath}/report/product/find.json'
		});
	});

	function exportSendOrder() {
		window.location.href = '${pageContext.request.contextPath}/report/product/export.xls?' + $('#cityPartnerQueryForm').formSerialize();
	}

	function showApplyImage(value, row, index) {
		if (value) {
			var rows = value.split(';');
			var path = getFileUrl(rows[0].split(',')[3]);
			return '<img src="' + path + '" style="width:60px;height:60px;">';
		}
		return value;
	}

</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north',border:false">
		<table>
			<tr>
				<td>
					<form id="cityPartnerQueryForm">
						<input type="hidden" id="q_user_orgId" name="orgId" value="">
						<table style="border:1px solid #ccc;">
							<tr>
								<td style="padding:5px 5px 5px 10px;">商品名称</td><td style="padding:5px 0px 5px 0px;"><input id="q_vender_productName"  name="productName" class="easyui-textbox" style="width:150px"></td>
								<td style="padding:3px 5px 6px 10px;">查询时间</td>
								<td colspan="3" style="padding:3px 0px 6px 0px;">
									<table cellspacing="0" cellpadding="0">
										<tr>
											<td style="padding:0px 0px 0px 0px;"><input id="q_order_startDate" class="easyui-datebox" style="width:100px;" name="startDate">
											<td style="padding:0px 3px 0px 3px;"><div style="width:5px;">-</div></td>
											<td style="padding:0px 0px 0px 0px;"><input id="q_order_endDate" class="easyui-datebox" style="width:100px;" name="endDate"></td>
											<td style="padding:0px 10px 0px 2px;"><div class="date-combo" data-options="start:'q_order_startDate',end:'q_order_endDate'" style="width:80px;"></div></td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</form>
				</td>
				<sec:authorize access="find">
					<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-search'" href="javascript:void(0)" onclick="queryData('cityPartnerGrid','cityPartnerQueryForm')">查询</a></td>
					<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-no'" href="javascript:void(0)" onclick="resetForm('cityPartnerQueryForm')">重置</a></td>
					<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-redo'" href="javascript:void(0)" onclick="exportSendOrder()">导出</a></td>
				</sec:authorize>
			</tr>
		</table>
	</div>
	<div data-options="region:'center',border:false">
		<table id="cityPartnerGrid" data-options="nowrap:false,striped:true,fit:true,border:false,idField:'id'" title="商品销量统计信息列表">
			<thead>
				<tr>
					<th data-options="field:'images',width:100,align:'center',formatter:showApplyImage">商品图片</th>
					<th data-options="field:'productName',width:200">商品名称</th>
					<th data-options="field:'price',width:150">零售价</th>
					<th data-options="field:'sales',width:100">销量</th>
					<th data-options="field:'salesVolume',width:200">销售额</th>
				</tr>
			</thead>
		</table>
	</div>
</body>
</html>