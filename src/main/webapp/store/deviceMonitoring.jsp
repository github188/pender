<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>设备监控</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/distpicker.data.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/distpicker.min.js"></script>
<style type="text/css">
	.form-item {vertical-align: bottom;}
	.area-select {width: 100px; height: 28px; border-color: #ddd; outline: none;}
	.abnormal-dev {font-size: 15px; line-height: 40px; margin-bottom: 8px;}
	.abnormal-dev span {margin-right: 50px;}
	.abnormal-dev i {color: red; font-size: 30px; font-style: normal; vertical-align: -2px;}
	.abnormal-dev em {margin-left: 10px;}
</style>
</head>
<body class="easyui-layout" >
	<div data-options="region:'north',border:false,split:true" style="height:132px;padding:10px 15px 15px;">
		<div class="abnormal-dev">
			<span>丢包率异常设备：<i id="packetLoss">0</i>台<em class="u-btn" data-type="0" onclick="viewAbnormalDev(this)">查看</em></span>
			<span>CPU负载率异常设备：<i id="cpuLoad">0</i>台<em class="u-btn" data-type="1" onclick="viewAbnormalDev(this)">查看</em></span>
		</div>
		<!-- 查询 -->
		<div>
		<form id="deviceQueryForm" class="search-form">
			<div class="form-item">
				<div class="text">设备组号</div>
				<div class="input">
					<input name="factoryDevNo" class="easyui-textbox" data-options="prompt:'设备组号'">
				</div>
			</div>
			<div class="form-item">
				<div class="text">所属店铺名称</div>
				<div class="input">
					<input name="pointName" class="easyui-textbox" data-options="prompt:'所属店铺名称'">
				</div>
			</div>
			<div id="distpicker" class="form-item" style="width:310px;">
				<div class="text">所在区域</div>
		        <select class="area-select easyui-validatebox" name="prov"></select>
		        <select class="area-select easyui-validatebox" name="city"></select>
		        <select class="area-select easyui-validatebox" name="dist"></select>
		    </div>
			<div class="form-item">
				<div class="text">设备状态</div>
	            <div class="input">
	                <select class="easyui-combobox" data-options="panelHeight:'auto',editable:false" name="deviceStatus">
	                    <option value="" selected="selected">请选择</option>
	                    <option value="1">在线</option>
	                    <option value="2">网络异常</option>
	                </select>
	            </div>
            </div>
		</form>
		<sec:authorize access="find">
		<div class="search-btn" onclick="queryDev()">查询</div>
		<div class="search-btn" onclick="resetForm('deviceQueryForm')">重置</div>
		</sec:authorize>
		</div>
	</div>
	<div data-options="region:'center',border:false,split:true,headerCls:'list-head'" title="设备列表" style="padding:10px;">
		<table id="deviceGrid" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,idField:'id'">
			<thead>
				<tr>
					<th data-options="field:'factoryDevNo',width:100,align:'center'">设备组号</th>
					<th data-options="field:'pointName',width:200,align:'center'">所属店铺名称</th>
					<th data-options="field:'pointAddress',width:200,align:'center'">所属店铺地址</th>
					<th data-options="field:'manufacturer',width:80,align:'center'">厂商</th>
					<th data-options="field:'netWorkType',width:80,align:'center',formatter:formatNetwork">网络类型</th>
					<th data-options="field:'softwareVersion',width:80,align:'center'">软件版本号</th>
					<th data-options="field:'firmwareVersion',width:80,align:'center'">硬件版本号</th>
					<th data-options="field:'deviceStatus',width:80,align:'center',formatter:formatDevState">设备状态</th>
					<th data-options="field:'operate',width:150,align:'center',formatter:formatOperate">操作</th>
				</tr>
			</thead>
		</table>
	</div>
	<div id="winLog" class="easyui-dialog" data-options="closed:true,buttons:'#logBtns'">
		<table id="logGrid" class="easyui-datagrid" data-options="border:false,pagination:false,striped:true,fitColumns:true,idField:'id'" style="width:300px;height:500px;">
			<thead>
				<tr>
					<th data-options="checkbox:true,field:''"></th>
					<th data-options="field:'createTime',width:50,align:'center'">上传时间</th>
					<th data-options="field:'operate',width:20,align:'center',formatter:formatDownload"></th>
				</tr>
			</thead>
		</table>
		<div id="logBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-add'" href="javascript:void(0)" onclick="">更新日志</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="downloadSelectLogs()">下载</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeWin('winLog');">关闭</a>
		</div>
	</div>
	<script type="text/javascript">
		//formatter
		function formatOperate(val, row) {
			var href = '${pageContext.request.contextPath}/store/deviceDetail.jsp?factoryDevNo=' + row.factoryDevNo;
			return '<span class="u-btn" onclick="viewLogs(' + row.factoryDevNo + ')">下载异常日志</span>'
				 + '<span class="u-btn" onclick="parent.addTab(' + row.factoryDevNo + ', \'' + href +'\')" d>查看详情</span>';
		}

		function formatDevState(val) {
			switch (val) {
				case 1:
					return '在线';
				case 2:
					return '<span style="color:red;">网络异常</span>';
				default:
					return val;
			}
		}

		function formatNetwork(val) {
			switch (val) {
				case 0:
					return 'WIFI';
				case 1:
					return '4G';
				case 2:
					return '3G';
				case 3:
					return '2G';
				case 4:
					return '网线';
				default:
					return val;
			}
		}

		function formatDownload(value, row) {
			return '<a class="u-btn" href="' + row.downloadUrl + '">下载</a>';
		}

		//条件查询设备
		function queryDev() {
			$('#deviceGrid').datagrid('options').queryParams = null;
			queryData('deviceGrid','deviceQueryForm');
		}

		//查看异常设备
		function viewAbnormalDev(m) {
			var type = $(m).data('type');
			$('#deviceGrid').datagrid('reload', {monitoringType: type});
		}

		//查看异常日志
		function viewLogs(factoryDevNo) {
			openWin('winLog', factoryDevNo + '的异常日志');
			$('#logGrid').datagrid({
				url: '${pageContext.request.contextPath}/platform/monitoring/findDeviceMonitoringAndDeviceLog.json',
				queryParams: {
					logType: 1,
					factoryDevNo:factoryDevNo
				}
			})
		}

		//批量下载日志
		function downloadSelectLogs() {
			var rows = $('#logGrid').datagrid('getSelections');
			for (var i = 0; i < rows.length; i++) {
				window.location = rows[i].downloadUrl
			}
		}

		$(function() {
			//初始化地区选择
			$('#distpicker').distpicker({autoSelect:false});

			$('#deviceGrid').datagrid({
				url: '${pageContext.request.contextPath}/platform/monitoring/findDeviceMonitoringPage.json',
				onBeforeSelect: function(i) {
					return false;
				}
			})

			$.ajax({
				url: '${pageContext.request.contextPath}/platform/monitoring/findDetailsAndNetworkCount.json',
				task: function(data) {
					$('#packetLoss').html(data.deviceMonitoring.packetLossRateNumber);
					$('#cpuLoad').html(data.deviceMonitoring.cpuRateNumber);
				}
			})
		})
	</script>
</body>
</html>