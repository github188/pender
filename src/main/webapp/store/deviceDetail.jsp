<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>设备监控详情</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/echarts.common.min.js"></script>
<style type="text/css">
	html, body {height: 100%; margin: 0;}
	.page-ctn {box-sizing: border-box; height:100%; padding: 10px 10px 0; overflow:auto;}
	.panel-block {margin-bottom: 10px; border: 1px solid #009cd3; border-radius: 2px 2px 0 0;}
	.panel-block .title {line-height: 36px; padding: 0 10px; font-size: 15px; color: #fff; background-color: #009cd3;}
	.panel-block .body {padding: 2px 10px; font-size: 14px;}
	.state-row {margin: 10px 0; height: 20px; line-height: 20px;}
	.state-row label {float: left; width: 100px;}
	.state-row span {display: block; margin-left: 100px; word-break: break-all;}
	.devNetworkState {margin-left: 15px; font-style: normal;}
	.device-state {border-top: 1px solid #e5e5e5;}
	.current-state {margin: 5px 0;}
	.current-state span {margin-right: 20px;}
	.current-state span i {margin-left: 5px; color: #32a000; font-style: normal;}
</style>
</head>
<body>
	<div class="page-ctn">
		<div style="float:left;width:440px;">
			<div class="panel-block">
				<div class="title">
					<span>设备详情</span>
				</div>
				<div class="body" id="devDetail">
					
				</div>
			</div>
			<div class="panel-block">
				<div class="title">
					<span>硬件详情</span>
				</div>
				<div class="body" id="hardwareDetail">
					
				</div>
			</div>
			<!-- <div class="panel-block">
				<div class="title">
					<span>开门记录(保留最近30天)</span>
				</div>
				<div class="body" style="height:300px;padding:0;">
					<table class="easyui-datagrid" data-options="fit:true,fitColumns:true,border:false">
						<thead>
							<tr>
								<th data-options="field:'a',width:50">开关门</th>
								<th data-options="field:'b',width:50">开关门</th>
							</tr>
						</thead>
					</table>
				</div>
			</div> -->
		</div>
		<div style="margin-left:450px;">
			<div class="panel-block">
				<div class="title">
					<span>网络状态</span>
				</div>
				<div class="body">
					<div class="current-state">
						<span>当前网络类型<i id="netWorkType"></i></span>
						<span>当前下载速度<i id="downloadSpeed"></i></span>
						<span>当前上传速度<i id="uploadSpeed"></i></span>
						<span>延迟<i id="delay"></i></span>
					</div>
					<div style="height:400px;" id="netWorkChart"></div>
				</div>
			</div>
			<div class="panel-block">
				<div class="title">
					<span>丢包率</span>
				</div>
				<div class="body">
					<div class="current-state">
						<span>当前丢包率<i id="packetLossRate"></i></span>
					</div>
					<div style="height:400px;" id="packetLossChart"></div>
				</div>
			</div>
			<div class="panel-block">
				<div class="title">
					<span>CPU负载率</span>
				</div>
				<div class="body">
					<div class="current-state">
						<span>当前负载率<i id="loadFactor"></i></span>
					</div>
					<div style="height:400px;" id="cpuLoadChart"></div>
				</div>
			</div>
			<div class="panel-block">
				<div class="title">
					<span>网络状态</span>
				</div>
				<div class="body">
					<div class="current-state">
						<span>当前温度<i id="temperature"></i></span>
					</div>
					<div style="height:400px;" id="temperatureChart"></div>
				</div>
			</div>
		</div>
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
	<script id="devDetailTmpl" type="text/html">
		<div class="state-row">
			<label>设备组号：</label>
			<span>{{factoryDevNo}}<i class="devNetworkState" style="color:{{deviceStatus == 1 ? '#32a000' : 'red'}};">{{deviceStatus == 1 ? '在线' : '网络异常'}}</i></span>
		</div>
		<div class="state-row">
			<label>所属店铺：</label>
			<span>{{pointName}}</span>
		</div>
		<div class="state-row">
			<label>所属店铺地址：</label>
			<span>{{pointAddress}}</span>
		</div>
		<div class="state-row">
			<label>厂商：</label>
			<span>{{manufacturer}}</span>
		</div>
		<div class="state-row">
			<label>当前版本号：</label>
			<span>{{softwareVersion}}</span>
		</div>
		<div class="state-row">
			<label>异常日志：</label>
			<span><i class="u-btn" onclick="showLogGrid(this)" data-type="1">下载异常日志</i>(保留最近30天)</span>
		</div>
		<div class="state-row">
			<label>日常日志：</label>
			<span><i class="u-btn" onclick="showLogGrid(this)" data-type="0">下载日常日志</i>(保留最近30天)</span>
		</div>
	</script>
	<script id="hardwareDetailTmpl" type="text/html">
		<div class="state-row">
			<label>固件版本号：</label>
			<span>{{firmwareVersion}}</span>
		</div>
		<div class="state-row">
			<label>占用内存：</label>
			<span>{{memory}}G</span>
		</div>
		<!-- <div class="device-state">
			<div class="state-row">智能饮料机</div>
			<div class="state-row">
				<label>压缩机状态：</label>
				<span>制热 40℃</span>
			</div>
			<div class="state-row">
				<label>压缩机节能：</label>
				<span>关闭</span>
			</div>
		</div> -->
	</script>
	<script type="text/javascript">
		function getFactoryDevNo() {
			return window.location.search.split('=')[1];
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

		function showLogGrid(m) {
			var type = $(m).data('type');
			openWin('winLog', type == 1 ? '异常日志' : '日常日志');
			$('#logGrid').datagrid({
				url: '${pageContext.request.contextPath}/platform/monitoring/findDeviceMonitoringAndDeviceLog.json',
				queryParams: {
					logType: type,
					factoryDevNo: getFactoryDevNo()
				}
			})
		}

		function downloadSelectLogs() {
			var rows = $('#logGrid').datagrid('getSelections');
			for (var i = 0; i < rows.length; i++) {
				window.location = rows[i].downloadUrl
			}
		}

		var lineOpt = {
			tooltip: {
                trigger: 'axis'
            },
            grid: {
            	top: 20,
            	bottom: 40,
            	left: 65,
            	right: 20
            },
            xAxis: [
                {
                    type: 'category',
                    axisTick: {
		                alignWithLabel: true
		            },
		            data: []
                }
            ],
            yAxis: [
            	{
					type: 'value',
                    axisLabel: {}
				}
            ],
            series: []
		}

		var devDetail = {
			el: '#devDetail',
			tmpl: '#devDetailTmpl',
			data: {},
			updateData: function(data) {
				this.data = data;
			},
			updateView: function() {
				$(this.el).html(template(this.tmpl.slice(1), this.data));
			}
		}

		var hardwareDetail = {
			el: '#hardwareDetail',
			tmpl: '#hardwareDetailTmpl',
			data: {},
			updateData: function(data) {
				this.data = data;
			},
			updateView: function() {
				$(this.el).html(template(this.tmpl.slice(1), this.data));
			}
		}

		var charts = {
			curData: {},
			updateCurData: function(data) {
				this.curData = data;
			},
			seriesBase: {
                type:'line',
                barMaxWidth: 30,
                smooth: true
            },
			chartsOpt: {
				netWork: {
					chart: echarts.init(document.getElementById('netWorkChart')),
					tooltipFormatter: '{b0}<br />{a0}: {c0}KB/S',
					yAxisFormatter: '{value}KB/S',
					series: [
						{
							name:'下载速度',
		                    data:[]
						}
					]
				},
				packetLoss: {
					chart: echarts.init(document.getElementById('packetLossChart')),
					tooltipFormatter: '{b0}<br />{a0}: {c0}%',
					yAxisFormatter: '{value}%',
					series: [
						{
							name:'丢包率',
		                    data:[]
						}
					]
				},
				cpuLoad: {
					chart: echarts.init(document.getElementById('cpuLoadChart')),
					tooltipFormatter: '{b0}<br />{a0}: {c0}%',
					yAxisFormatter: '{value}%',
					series: [
						{
							name:'cpu负载率',
		                    data:[]
						}
					]
				},
				temperature: {
					chart: echarts.init(document.getElementById('temperatureChart')),
					tooltipFormatter: '{b0}<br />{a0}: {c0}℃',
					yAxisFormatter: '{value}℃',
					series: [
						{
							name:'温度',
		                    data:[]
						}
					]
				}
			},
			updateChartsOpt: function(data) {
				var c,
					chartsOpt = this.chartsOpt;

				for (c in chartsOpt) {
					for (var i = 0; i < chartsOpt[c].series.length; i++) {
						Array.prototype.push.apply(chartsOpt[c].series[i].data, data[c].series[i]);
					}
				}
			},
			updateView: function() {
				var c,
					chartsOpt = this.chartsOpt;

				for (c in chartsOpt) {
					lineOpt.series = [];
					lineOpt.yAxis[0].axisLabel.formatter = chartsOpt[c].yAxisFormatter;
					lineOpt.tooltip.formatter = chartsOpt[c].tooltipFormatter;
					for (var i = 0; i < chartsOpt[c].series.length; i++) {
						lineOpt.series[i] = $.extend(true, {}, this.seriesBase, chartsOpt[c].series[i]);
					}

					chartsOpt[c].chart.setOption(lineOpt);
				}

				if (!$.isEmptyObject(this.curData)) {
					$('#netWorkType').html(formatNetwork(this.curData.netWorkType));
					$('#downloadSpeed').html(this.curData.downloadSpeed + 'KB/S');
					$('#uploadSpeed').html(this.curData.uploadSpeed + 'KB/S');
					$('#delay').html(this.curData.delay + 'ms');
					$('#packetLossRate').html(this.curData.packetLossRate + '%');
					$('#loadFactor').html(this.curData.loadFactor + '%');
					$('#temperature').html(this.curData.temperature + '℃');
				}
			}
		}

		$(function() {
			var lastTime;//记录最后一次请求发起的时间

			function updateDevMes() {
				$.ajax({
					url: '${pageContext.request.contextPath}/platform/monitoring/findDeviceMonitoringDateils.json',
					data: {factoryDevNo: getFactoryDevNo()}
				}).done(function(data) {
					devDetail.updateData(data.deviceMonitoring);
					devDetail.updateView();
					hardwareDetail.updateData(data.deviceMonitoring);
					hardwareDetail.updateView();
				})
			}

			function updateCharts() {
				var params = {
					factoryDevNo: getFactoryDevNo(), 
					startTime: lastTime ? lastTime : formatTime(new Date).slice(0, 11) + '00:00:00', 
					endTime: formatTime(new Date)
				}

				lastTime = params.endTime;

				$.ajax({
					url: '${pageContext.request.contextPath}/platform/monitoring/findDeviceMoitoringNetworkList.json',
					data: params
				}).done(function(data) {
					var list = data.deviceMonitoringNetworkList;

					if (list.length == 0) {
						charts.updateView();
						return;
					}

					var chartsData = {
						netWork: {series: [[]]},
						packetLoss: {series: [[]]},
						cpuLoad: {series: [[]]},
						temperature: {series: [[]]}
					}

					for (var i = 0; i < list.length; i++) {
						lineOpt.xAxis[0].data.push(list[i].createTime.slice(11, 16));
						chartsData.netWork.series[0].push(list[i].downloadSpeed);
						chartsData.packetLoss.series[0].push(list[i].packetLossRate);
						chartsData.cpuLoad.series[0].push(list[i].loadFactor);
						chartsData.temperature.series[0].push(list[i].temperature);
					}

					charts.updateChartsOpt(chartsData);
					charts.updateCurData(list[0]);
					charts.updateView();
				})
			}

			function updateAll() {
				updateDevMes();
				updateCharts();
				setTimeout(updateAll, 5*60*1000);
			}

			//获取数据
			updateAll();
		})
	</script>
</body>
</html>