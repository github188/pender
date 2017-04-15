<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>设备销量统计</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/base64.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<script type="text/javascript">
	var deviceTypes =[];
	$(function() {
		showDateCombo();
		$('#deviceGrid').datagrid({
			queryParams:$('#deviceQueryForm').getValues(),
			url:'${pageContext.request.contextPath}/report/device/find.json',
			view: detailview,
			detailFormatter:function(index,row){
				return '<div style="padding:2px"><table id="ddv-' + index + '"></table></div>';
			},
			onExpandRow: function(index,row){
				var gridBtns = [];
				$('#ddv-'+index).datagrid({
					url:'${pageContext.request.contextPath}/report/device/findSellerDevice.json?deviceId='+row.id,
					queryParams: {
				    	"startDate": $("#q_order_startDate").datebox('getValue'),
				    	"endDate": $("#q_order_endDate").datebox('getValue')
				    },
					fitColumns:true,
					rownumbers:true,
					loadMsg:'',
					height:'auto',
				   	columns:[[
		                      {field:'productName',title:'商品名称',width:40},
		                      {field:'price',title:'零售价',width:40},
		                      {field:'sales',title:'销量',width:40},
		                      {field:'salesVolume',title:'销售额',width:60}
					]],
					onResize:function(){
						$('#deviceGrid').datagrid('fixDetailRowHeight',index);
					},
					onLoadSuccess:function(){
						setTimeout(function(){
							$('#deviceGrid').datagrid('fixDetailRowHeight',index);
						},0);
					}
				});
				$('#deviceGrid').datagrid('fixDetailRowHeight',index);
			}
		});
		var cache = getAllSysTypes();
		for (var i = 0; i < cache.length; i++) {
			if (cache[i].type == 'DEVICE_TYPE') {
				deviceTypes.push(cache[i]);
			}
		}
// 		$('#natrue').combobox({data:deviceTypes});
// 		$('#q_device_state').combobox({data:deviceStatesQuery}); 
	});

	function exportSendOrder() {
		window.location.href = '${pageContext.request.contextPath}/report/device/export.xls?' + $('#deviceQueryForm').formSerialize();
	}

	function deviceType(value) {
		for (var i = 0; i < deviceTypes.length; i++)
			if (deviceTypes[i].code == value)
				return deviceTypes[i].name;
		return value;
	}

	function formatSalePrice(value) {
		return value === '' || value === undefined ? 0 : parseFloat(value).toFixed(2);
	}
</script>
</head>
<body class="easyui-layout" >
	<div data-options="region:'north',border:false">
		<table>
			<tr>
				<td>
					<form id="deviceQueryForm"><input type="hidden" id="q_user_orgId" name="orgId" value="">
						<table style="border:1px solid #ccc;">
							<tr>
								<td style="padding:5px 5px 5px 10px;">设备编号</td><td style="padding:5px 0px 5px 0px;"><input id="q_device_devNo"  name="devNo" class="easyui-textbox" style="width:150px"></td>
								<td style="padding:5px 5px 5px 15px;">设备地址</td><td style="padding:5px 0px 5px 0px;"><input id="q_device_address" name="pointAddress" class="easyui-textbox" style="width:150px"></td>
								<td style="padding:5px 5px 5px 15px;">所属人</td><td style="padding:5px 10px 5px 0px;"><input id="q_device_orgName" name="orgName" class="easyui-textbox" style="width:150px"></td>
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
					<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-search'" href="javascript:void(0)" onclick="queryData('deviceGrid','deviceQueryForm')">查询</a></td>
					<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-no'" href="javascript:void(0)" onclick="resetForm('deviceQueryForm')">重置</a></td>
					<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-redo'" href="javascript:void(0)" onclick="exportSendOrder()">导出</a></td>
				</sec:authorize>
			</tr>
		</table>
	</div>
	<div data-options="region:'center',border:false">
		<table id="deviceGrid" data-options="nowrap:false,striped:true,fit:true,border:false,idField:'id'" title="设备销量统计列表">
			<thead>
				<tr>
					<th data-options="field:'devNo',width:100">设备编号</th>
					<th data-options="field:'pointAddress',width:150">设备地址</th>
					<th data-options="field:'orgName',width:150">所属人</th>
					<th data-options="field:'natrue',width:150,formatter:deviceType">设备性质</th>
					<th data-options="field:'salePrice',width:100,formatter:formatSalePrice">销售总额</th>
				</tr>
			</thead>
		</table>
	</div>
</body>
</html>