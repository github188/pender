<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>经营分析</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/distpicker.data.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/distpicker.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/echarts.common.min.js"></script>
<style type="text/css">
	html, body {height: 100%; margin: 0;}
	.page-ctn {height:100%; background-color: #f5f5f5; padding: 0 10px; overflow:auto;}
	.data-item {width: 16.666%; float: left;}
	.data-panel {margin: 0 5px; background-color: #fff;}
	.data-panel .title {font-size: 15px; border-bottom: 1px solid #e5e5e5; text-align: center; line-height: 36px;}
	.data-panel .detail {font-size: 30px; text-align: center; line-height: 80px;}
	.sales-data {display: inline-block; padding: 0 25px; font-size: 13px; text-align: center; color: #666;}
	.sales-data .data {font-size: 25px; color: #009cd3;}
	.chart-panel {margin-bottom: 10px; background-color: #fff;}
	.chart-panel .title {line-height: 36px; border-bottom: 1px solid #e5e5e5; font-size: 15px; text-indent: 12px;}
	.chart-panel .body {padding: 10px;}
	.toolbar > div {display: inline-block; height: 36px; vertical-align: top;}
	.toolbar > span {vertical-align: top; margin-right: 15px;}
	.time-btn-ctn {font-size: 0; margin-right: 12px; height: 36px;}
	.time-btn {font-size: 12px; padding: 4px 12px; border: 1px solid #e5e5e5; margin-left: -1px; cursor: pointer; -webkit-user-select:none;-moz-user-select:none;-o-user-select:none;user-select:none;}
	.time-btn.active {background-color: rgba(0, 156, 211, 1); border-color: rgba(0, 156, 211, 1); color: #fff;}
	.area-select {width: 100px; height: 28px; border-color: #ddd; outline: none;}
	.form-item {vertical-align: bottom;}
	.s-btn {display: inline-block; float: left; line-height: 26px; border: 1px solid #ddd; padding: 0 5px; margin-left: -1px; font-size: 12px; cursor: pointer; background-color: #f5f5f5;}
	.s-btn:hover {background-color: #e9e9e9;}
	.c-btn {font-size: 12px; border: 1px solid #009cd3; color: #009cd3; padding: 2px 10px; border-radius: 3px; cursor: pointer;}
	.c-btn:hover {background-color: #009cd3; color: #fff;}
</style>
</head>
<body>
	<div class="page-ctn">
		<div style="background-color:#fff;padding:10px;margin-bottom:10px;">
			<form id="formOperate" class="search-form">
				<div class="form-item">
					<div class="text">选择组织</div>
					<input id="orgSelect" class="easyui-combotree" data-options="prompt:'请选择组织'" name="orgId">
				</div>
				<div class="form-item">
					<div class="text">包含管理组织</div>
					<select class="easyui-combobox" data-options="panelHeight:'auto',editable:false" name="containSubOrg">
						<option value="1">是</option>
						<option value="0">否</option>
					</select>
				</div>
				<div id="distpicker" class="form-item" style="width:310px;">
					<div class="text">所在区域</div>
			        <select class="area-select easyui-validatebox" name="prov"></select> 
			        <select class="area-select easyui-validatebox" name="city"></select>
			        <select class="area-select easyui-validatebox" name="dist"></select>
			    </div>
				<div class="form-item" style="width:236px;">
					<div class="text">选择店铺</div>
					<input id="q_pointId" type="hidden" name="id">
					<div class="clearfix">
						<input id="pointSelect" type="text" readonly class="textbox-text" placeholder="请选择店铺" style="float:left;width:150px;border:1px solid #ddd;">
						<span class="s-btn" onclick="selectPoint()">选择</span>
						<span class="s-btn" onclick="clearPoint()">清除</span>
					</div>
				</div>
			</form>
			<sec:authorize access="find">
			<div class="search-btn" onclick="updateAllData()">查询</div>
			<div class="search-btn" onclick="requestAllData()" style="width:78px;">查看汇总</div>
			</sec:authorize>
		</div>
		<div style="margin-bottom:10px;">
			<div class="clearfix" style="margin:0 -5px;">
				<div class="data-item">
					<div class="data-panel">
						<div class="title">总销售额(元)</div>
						<div id="allSaleAmount" class="detail">0</div>
					</div>
				</div>
				<div class="data-item">
					<div class="data-panel">
						<div class="title">总销售量(件)</div>
						<div id="allSaleVolume" class="detail">0</div>
					</div>
				</div>
				<div class="data-item">
					<div class="data-panel">
						<div class="title">平均单价(元)</div>
						<div id="allAverage" class="detail">0</div>
					</div>
				</div>
				<div class="data-item">
					<div class="data-panel">
						<div class="title">总退款金额(元)</div>
						<div id="allRefund" class="detail">0</div>
					</div>
				</div>
				<div class="data-item">
					<div class="data-panel">
						<div class="title">总店铺数量</div>
						<div id="allStoreCount" class="detail">0</div>
					</div>
				</div>
				<div class="data-item">
					<div class="data-panel">
						<div class="title">总设备数量</div>
						<div id="allDeviceCount" class="detail">0</div>
					</div>
				</div>
			</div>
		</div>
		<div class="chart-panel">
			<div class="title">
				<span>销售统计图</span>
				<div class="pull-right toolbar">
					<div class="date-ctn">
						<input type="text" class="easyui-datebox" style="width:120px;">
						-
						<input type="text" class="easyui-datebox" style="width:120px;">
						<span id="slSearchBtn" class="c-btn">查询</span>
					</div>
					<div id="slTimeMenu" class="time-btn-ctn">
						<span class="time-btn" data-near="1">今日</span>
						<span class="time-btn" data-near="1" data-pull="1">昨日</span>
						<span class="time-btn" data-near="7">最近7天</span>
						<span class="time-btn" data-near="30">最近30天</span>
					</div>
					<span id="exportSalesData" class="c-btn">导出</span>
				</div>
			</div>
			<div id="saleDatas" class="clearfix" style="padding:18px 20px 0;">
				<div class="pull-left">
					<div class="sales-data">
						<div>销售额(元)</div>
						<div class="data">0</div>
					</div>
					<div class="sales-data">
						<div>销售量(件)</div>
						<div class="data">0</div>
					</div>
					<div class="sales-data">
						<div>平均单价(元)</div>
						<div class="data">0</div>
					</div>
				</div>
				<div class="pull-right">
					<div class="sales-data">
						<div>购物车销售额(元)</div>
						<div class="data">0</div>
					</div>
					<div class="sales-data">
						<div>购物车销售量(件)</div>
						<div class="data">0</div>
					</div>
					<div class="sales-data">
						<div>购物车平均单价(元)</div>
						<div class="data">0</div>
					</div>
					<div class="sales-data">
						<div>购物车销售额占比</div>
						<div class="data">0</div>
					</div>
				</div>
			</div>
			<div class="body" id="product-sale" style="height:500px;"></div>
		</div>
		<div class="chart-panel">
			<div class="title">
				<span>商品销售占比分析图</span>
				<div class="pull-right toolbar">
					<div class="date-ctn">
						<input type="text" class="easyui-datebox" style="width:120px;">
						-
						<input type="text" class="easyui-datebox" style="width:120px;">
						<span id="rtSearchBtn" class="c-btn">查询</span>
					</div>
					<div id="rtTimeMenu" class="time-btn-ctn">
						<span class="time-btn" data-near="1">今日</span>
						<span class="time-btn" data-near="1" data-pull="1">昨日</span>
						<span class="time-btn" data-near="7">最近7天</span>
						<span class="time-btn" data-near="30">最近30天</span>
					</div>
				</div>
			</div>
			<div class="body" id="product-rate" style="height:500px;"></div>
		</div>
		<div class="chart-panel">
			<div class="title">
				<span>商品销售数据</span>
				<div class="pull-right toolbar">
					<div class="date-ctn">
						<input type="text" class="easyui-datebox" style="width:120px;">
						-
						<input type="text" class="easyui-datebox" style="width:120px;">
						<span id="pdSearchBtn" class="c-btn">查询</span>
					</div>
					<div id="pdTimeMenu" class="time-btn-ctn">
						<span class="time-btn" data-near="1">今日</span>
						<span class="time-btn" data-near="1" data-pull="1">昨日</span>
						<span class="time-btn" data-near="7">最近7天</span>
						<span class="time-btn" data-near="30">最近30天</span>
					</div>
					<span id="exportProductSalesData" class="c-btn">导出</span>
				</div>
			</div>
			<div class="body" style="height:558px;">
				<table id="productGrid" class="easyui-datagrid" data-options="fit:true,fitColumns:true,striped:true,pageSize:20">
					<thead>
						<tr>
							<th data-options="field:'productName',align:'center',width:150,sortable:true">商品名称</th>
							<th data-options="field:'totalOrdersNumber',align:'center',width:50,sortable:true">总订单数</th>
							<th data-options="field:'dealQuantity',align:'center',width:50,sortable:true">成交订单数</th>
							<th data-options="field:'totalOrderAmount',align:'center',width:50,formatter:fixedTwo,sortable:true">总订单金额</th>
							<th data-options="field:'clinchDealOrder',align:'center',width:50,formatter:fixedTwo,sortable:true">成交订单金额</th>
							<th data-options="field:'salesRate',align:'center',width:50,formatter:rateFixedTwo,sortable:true">销售额占比</th>
							<th data-options="field:'salesVolumeRate',align:'center',width:50,formatter:rateFixedTwo,sortable:true">销售量占比</th>
							<th data-options="field:'refundQty',align:'center',width:50,sortable:true">退货数量</th>
							<th data-options="field:'refundAmount',align:'center',width:50,formatter:fixedTwo,sortable:true">退款金额</th>
						</tr>
					</thead>
				</table>
			</div>
		</div>
	</div>
	<div id="winPoint" class="easyui-dialog" data-options="closed:true,buttons:'#pointBtns'" style="width:700px;">
		<form class="search-form" style="display:block;padding:10px;border-bottom:1px solid #ddd;">
			<div class="form-item">
				<div class="text">店铺名称</div>
				<input id="q_pointName" class="easyui-textbox" name="pointName" data-options="prompt:'店铺名称'">
			</div>
			<sec:authorize access="find">
			<div class="search-btn" onclick="searchPoint()" style="margin-left:0;">查询</div>
			</sec:authorize>
		</form>
		<table id="pointGrid" class="easyui-datagrid" data-options="fitColumns:true,striped:true,border:false,singleSelect:true" style="height:400px;">
			<thead>
				<tr>
					<th data-options="checkbox:true,field:''"></th>
					<th data-options="field:'pointName',align:'center',width:100">店铺名称</th>
					<th data-options="field:'pointAddress',align:'center',width:100">店铺地址</th>
					<th data-options="field:'orgName',align:'center',width:100">所属组织</th>
				</tr>
			</thead>
		</table>
		<div id="pointBtns">
            <a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="fillPoint()">保存</a>
            <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winPoint')">取消</a>
        </div>
	</div>
	<script type="text/javascript">
		//初始化图表容器
		var saleChart = echarts.init(document.getElementById('product-sale'));
		var rateChart = echarts.init(document.getElementById('product-rate'));

		//图表配置
		var saleOpt = {
			title: {
				show: false,
				text: '销售统计图'
			},
            tooltip: {
                trigger: 'axis'
                //formatter: '{b0}<br />{a0}: {c0}元<br />{a1}: {c1}件'
            },
            toolbox: {
                show: true,
                bottom: 0,
                right: 0,
                feature: {
                    dataView: { //数据视图
                        show: true
                    },
                    magicType: {//转换视图
                        show: true, 
                        type: ['line', 'bar']
                    },
                    restore: {//还原
                        show: true
                    },
                    saveAsImage: {//保存图片
                        show: true
                    }
                }
            },
            grid: {
            	top: 40,
            	bottom: 65,
            	left: 80,
            	right: 80
            },
            legend: {
                data:['销售额','销售量','平均单价'],
                bottom: 10
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
                    name: '销售额/销售量'
                },
                {
                    type: 'value',
                    name: '平均单价',
                    axisLabel: {
                        formatter: '{value}元'
                    }
                }
            ],
            series: [
                {
                    name:'销售额',
                    type:'bar',
                    barMaxWidth: 30,
                    smooth: true,
                    itemStyle: {
                        normal: {
                            color: '#aacc03'
                        }
                    },
                    data:[]
                },
                {
                    name:'销售量',
                    type:'bar',
                    barMaxWidth: 30,
                    smooth: true,
                    barGap: 0,
                    itemStyle: {
                        normal: {
                            color: '#f9bf11'
                        }
                    },
                    data:[]
                },
                {
                    name:'平均单价',
                    type:'line',
                    barMaxWidth: 30,
                    yAxisIndex: 1,
                    smooth: true,
                    lineStyle: {
                        normal: {
                            color: '#009cd3'
                        }
                    },
                    itemStyle: {
                        normal: {
                            color: '#009cd3'
                        }
                    },
                    data:[]
                }
            ]
	    };

	    var rateOpt = {
		    title : {
		        text: '商品销售额/销售量占比',
		        x: 'center',
		        //top: 10,
		        subtext: '左边为销售额占比图，右边为销售量占比图'
		    },
		    tooltip : {
		        trigger: 'item',
		        formatter: "{a} <br/>{b} : {c} ({d}%)"
		    },
		    toolbox: {
                show: true,
                bottom: 0,
                right: 0,
                feature: {
                    dataView: { //数据视图
                        show: true
                    },
                    saveAsImage: {//保存图片
                        show: true
                    }
                }
            },
		    series : [
		        {
		            name: '商品销售额占比',
		            type: 'pie',
		            radius : '70%',
		            center: ['25%', '50%'],
		            data:[],
		            itemStyle: {
		                emphasis: {
		                    shadowBlur: 10,
		                    shadowOffsetX: 0,
		                    shadowColor: 'rgba(0, 0, 0, 0.5)'
		                }
		            }
		        },
		        {
		            name: '商品销售量占比',
		            type: 'pie',
		            radius : '70%',
		            center: ['75%', '50%'],
		            data:[],
		            itemStyle: {
		                emphasis: {
		                    shadowBlur: 10,
		                    shadowOffsetX: 0,
		                    shadowColor: 'rgba(0, 0, 0, 0.5)'
		                }
		            }
		        }
		    ]
		};

		//格式化数字，保留小数点后两位并且四舍五入
		function fixedTwo(num) {
			return num.toFixed(2);
		}
		function rateFixedTwo(num) {
			return num.toFixed(2) + '%';
		}

		//格式化日期
		function getDateFormatter(d) {
			var year = d.getFullYear(),
				month = (d.getMonth()+1) > 9 ? (d.getMonth()+1) : '0'+(d.getMonth()+1);
				day = d.getDate() > 9 ? d.getDate() : '0' + d.getDate();
			
			return year + '-' + month + '-' + day;
		}

		//返回开始结束日期，near：开始结束日期相隔天数，pull：开始时间前推天数
		function getDateRange(near, pull) {
			var today = new Date;
			var end = pull ? new Date(today - pull*24*60*60*1000) : today;
			var start = new Date(end - (near-1)*24*60*60*1000);
			return {
				startDate: getDateFormatter(start),
				endDate: getDateFormatter(end)
			}
		}

		//店铺查询
		function selectPoint() {
			$('#q_pointName').textbox('setValue', '');
			openWin('winPoint', '请选择店铺');

			var params = $('#formOperate').getValues();
			delete params.id;

			$('#pointGrid').datagrid({
				url: '${pageContext.request.contextPath}/operation/analysis/findStores.json',
				queryParams: params
			})
		}
		function searchPoint() {
			var pointName = $('#q_pointName').textbox('getValue');
			$('#pointGrid').datagrid('options').queryParams.pointName = pointName;
			$('#pointGrid').datagrid('reload');
		}

		//选择店铺并保存
		function fillPoint() {
			var point = $('#pointGrid').datagrid('getSelected');

			if (!point) {
				infoMsg('请选择店铺');
				return;
			}

			$('#q_pointId').val(point.id);
			$('#pointSelect').val(point.pointName);

			closeWin('winPoint');
		}

		//清除店铺选择框
		function clearPoint() {
			$('#q_pointId').val('');
			$('#pointSelect').val('');
		}

		//重置查询表单并查询
		function requestAllData() {
			resetForm('formOperate');
			$('#q_pointId').val('');
			$('#pointSelect').val('');
			updateAllData();
		}

		//查询经营数据
		function updateAllData() {
			updateDataPanel();
			$('#slTimeMenu span:first-child').trigger('click');
			$('#rtTimeMenu span:first-child').trigger('click');
			$('#pdTimeMenu span:first-child').trigger('click');
		}

		//请求各种累计数据
		function updateDataPanel() {
			$.ajax({
				url: '${pageContext.request.contextPath}/operation/analysis/findSysData.json',
				data: $('#formOperate').getValues(),
				task: function(r) {
					$('#allSaleAmount').html(fixedTwo(r.salesAmount));
					$('#allSaleVolume').html(r.salesVolume);
					$('#allDeviceCount').html(r.deviceCount);
					$('#allStoreCount').html(r.storeCount);
					$('#allAverage').html(fixedTwo(r.averagePrice));
					$('#allRefund').html(fixedTwo(r.refundAmount));
				}
			})
		}

		//请求时间段内每日销售数据
		function updateSaleChart(dateRange) {
			var params = $.extend({}, $('#formOperate').getValues(), dateRange);

			$.ajax({
				url: '${pageContext.request.contextPath}/operation/analysis/findSalesData.json',
				data: params,
				task: function(r) {
					var $saleDatas = $('#saleDatas .data');
					$saleDatas.eq(0).html(fixedTwo(r.salesAmount));
					$saleDatas.eq(1).html(r.salesVolume);
					$saleDatas.eq(2).html(fixedTwo(r.averagePrice));
					$saleDatas.eq(3).html(fixedTwo(r.cartSalesAmount));
					$saleDatas.eq(4).html(r.cartSalesVolume);
					$saleDatas.eq(5).html(fixedTwo(r.cartAveragePrice));
					$saleDatas.eq(6).html(rateFixedTwo(r.cartSalesRate));

					var orders = r.orders;
					var dateList = [],
						amountList = [],
						volumeList = [],
						averageList = [];
					for (var i = 0; i < orders.length; i++) {
						dateList.push(orders[i].date.length <= 2 ? orders[i].date + '时' : orders[i].date.slice(5).split('-').join('月') + '日');
						amountList.push(orders[i].salesAmount);
						volumeList.push(orders[i].salesVolume);
						averageList.push(orders[i].averagePrice);
					}
					saleOpt.xAxis[0].data = dateList;
					saleOpt.series[0].data = amountList;
					saleOpt.series[1].data = volumeList;
					saleOpt.series[2].data = averageList;
					saleChart.setOption(saleOpt);
				}
			})
		}

		//请求时间段内商品销售额、销售量
		function updateRateChart(dateRange) {
			var params = $.extend({}, $('#formOperate').getValues(), dateRange);

			$.ajax({
				url: '${pageContext.request.contextPath}/operation/analysis/findProductSalesPieChartData.json',
				data: params,
				task: function(r) {
					var salesAmountList = r.salesAmountList,
						salesVolumeList = r.salesVolumeList;
					var amountList = [],
						volumeList = [];

					for (var i = 0; i < salesAmountList.length; i++) {
						amountList.push({name: salesAmountList[i].productName, value: salesAmountList[i].salesAmount});
					}
					for (var i = 0; i < salesVolumeList.length; i++) {
						volumeList.push({name: salesVolumeList[i].productName, value: salesVolumeList[i].salesVolume});
					}

					rateOpt.series[0].data = amountList;
					rateOpt.series[1].data = volumeList;
					rateChart.setOption(rateOpt);
				}
			})
		}

		//请求时间段内商品销售表格数据
		function updateProductGrid(dateRange) {
			var params = $.extend({}, $('#formOperate').getValues(), dateRange);

			$('#productGrid').datagrid({
				url: '${pageContext.request.contextPath}/operation/analysis/findProductSalesData.json',
				queryParams: params
			})
		}

		//刷新图表图饼
		function refreshCharts() {
			saleChart = echarts.init(document.getElementById('product-sale'));
			rateChart = echarts.init(document.getElementById('product-rate'));
			saleChart.setOption(saleOpt);
			rateChart.setOption(rateOpt);
		}

		$(function(){
			//初始化地区选择
			$('#distpicker').distpicker({autoSelect:false});

			//异步加载组织选择框子节点
			$('#orgSelect').combotree({
				url: '${pageContext.request.contextPath}/operation/analysis/findOrgnizationsByParentId.json',
				loadFilter: function(data) {
					var tree = [];
					var rows = data.rows;
					if (rows.length) {
						for (var i = 0; i < rows.length; i++) {
							tree.push({id:rows[i].id, text:rows[i].name, state:'closed'});
						}
					}
					return tree;
				}
			})

			//绑定时间修改操作
			$('#slTimeMenu, #rtTimeMenu, #pdTimeMenu').on('click', 'span', function() {
				$(this).addClass('active').siblings().removeClass('active');
				var near = $(this).data('near'),
					pull = $(this).data('pull');
				var range = getDateRange(near, pull);
				var parentId = $(this).parent().attr('id');
				if (parentId == 'slTimeMenu') {
					updateSaleChart(range);
				} else if (parentId == 'rtTimeMenu') {
					updateRateChart(range);
				} else if (parentId == 'pdTimeMenu') {
					updateProductGrid(range);
				}
			})
			
			//自选时间查询
			$('#slSearchBtn, #rtSearchBtn, #pdSearchBtn').on('click', function() {
				var $dateboxs = $(this).siblings('.easyui-datebox');
				var startDate = $dateboxs.eq(0).datebox('getValue'),
					endDate = $dateboxs.eq(1).datebox('getValue');

				if (!startDate || !endDate || startDate > endDate) {
					infoMsg('日期有误，请重新选择！');
					return;
				}

				$(this).parent().siblings('.time-btn-ctn').children('span').removeClass('active');

				var range = {startDate:startDate, endDate:endDate};

				var id = $(this).attr('id');
				if (id == 'slSearchBtn') {
					updateSaleChart(range);
				} else if (id == 'rtSearchBtn') {
					updateRateChart(range);
				} else if (id == 'pdSearchBtn') {
					updateProductGrid(range);
				}				
			})

			//导出表格
			$('#exportSalesData, #exportProductSalesData').on('click', function() {
				var $this = $(this);

				var $active = $(this).siblings('.time-btn-ctn').children('.active');
				if ($active.length != 0) {
					var near = $active.data('near'),
						pull = $active.data('pull');
					var range = getDateRange(near, pull);
				} else {
					var $dateboxs = $this.siblings('.date-ctn').children('.easyui-datebox');
					var range = {startDate:$dateboxs.eq(0).datebox('getValue'), endDate:$dateboxs.eq(1).datebox('getValue')};
				}

				var params = $.extend({}, $('#formOperate').getValues(), range);
				var api = $this.attr('id');

				window.location = '${pageContext.request.contextPath}/operation/analysis/' + api + '.xls?' + $.param(params);
			})

			//放大缩小浏览器窗口时重新渲染图表
			$(window).resize(refreshCharts);

			//先请求一次汇总的数据
			updateAllData();
		})
	</script>
</body>
</html>