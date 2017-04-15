<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>抽奖活动数据统计</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<style type="text/css">
	.act-pro-wrap {float: left; width: 33.33%;}
	.act-pro-item {margin: 0 5px; border: 1px solid #e5e5e5; border-radius: 2px; font-size: 12px; overflow: hidden; cursor: pointer;}
	.act-pro-item .head {border-bottom: 1px solid #e5e5e5; line-height: 30px; padding: 0 10px; position: relative; z-index: 1;}
	.act-pro-item .head .title {font-size: 14px;}
	.act-pro-item .head .export {float: right; color: #009cd3;}
	.act-pro-item .head .export:hover {color: #0678a0;}
	.act-pro-item .body {margin: -1px -2px -1px -1px;}
	.act-pro-item.active {background-color: #f5f5f5;}

	.data-tb {width: 100%; text-align: center; line-height: 38px; border-collapse: collapse;}
	.data-tb td {border: 1px solid #e5e5e5;}
</style>
<script type="text/javascript">
	var pointTypes = [];//店铺类型

	//formatter
	function formatPointType(value) {
		for (var i = 0; i < pointTypes.length; i++)
			if (pointTypes[i].code == value)
				return pointTypes[i].name;
		return value;
	}

	function formatProImgUrl(value) {
		var imgUrl = '';
		if (value) {
			var path = value.split(';')[0].split(',')[3];
			if (path) {
				imgUrl = getFileUrl(path);
			}
		}
		return '<img src="'+imgUrl+'" style="width:70px;height:70px;margin:10px 0;" />';
	}

	$(function() {
		var cache = getAllSysTypes();
		for (var i = 0; i < cache.length; i++) {
			if (cache[i].type == 'POINT_PLACE_TYPE') {
				pointTypes.push(cache[i]);
			}
		}

		var actId = getUrlArg('id');//获取活动id
		$.post('${pageContext.request.contextPath}/marketing/lottery/findOrderDetail.json', {lotteryId: actId}, function(r) {
			var html = template("act-pro-item", r);
			$('#act-pro-list').append(html);
			$('#act-pro-list .act-pro-item').each(function(i) {
				$(this).data('cache', r.orderList[i]);
			})
			$('#act-pro-list .act-pro-item').eq(0).trigger('click');
		})

		//选择活动内容
		$('#act-pro-list').on('click', '.act-pro-item', function() {
			var $this = $(this);
			if ($this.hasClass('active')) return;
			$('#act-pro-list .act-pro-item').removeClass('active');
			$this.addClass('active');
			var rows = $(this).data('cache').deviceList;
			$('#devList').datagrid('loadData', {total:rows.length, rows:rows});
		})

		$('#devList').datagrid({
			onLoadSuccess: function(data) {
				if (data.total != 0) {
					$(this).datagrid('selectRow', 0);
				}
			},
			onSelect: function(i, row) {
				var lotteryProductId = $('#act-pro-list .act-pro-item.active').data('cache').lotteryProductId;
				$('#proList').datagrid({
					url: '${pageContext.request.contextPath}/marketing/lottery/findByLotteryIdProduct.json',
					queryParams: {lotteryProductId:lotteryProductId, factoryDevNo:row.factoryDevNo}
				})
			}
		})

		//导出订单
		$('#act-pro-list').on('click', '.export', function(e) {
			e.stopPropagation();
			var id = $(this).parents('.act-pro-item').data('cache').lotteryProductId;
			window.location = '${pageContext.request.contextPath}/marketing/lottery/findOneExportExcel.xls?lotteryProductId='+id;
		})
	})
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north',border:false,split:true" style="padding:10px;height:140px;">
		<div id="act-pro-list" style="margin: 0 -5px;">
			
		</div>
	</div>

	<div data-options="region:'center',border:false" style="padding:10px;">
		<div style="float:left;width:50%;height:100%;padding-right:10.5px;box-sizing:border-box;">
			<table id="devList" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,singleSelect:true,pagination:false,idField:'id'">
				<thead>
					<tr>
						<th data-options="field:'factoryDevNo',align:'center',width:10">设备组号</th>
						<th data-options="field:'typeStr',align:'center',width:45">设备类型</th>
						<th data-options="field:'pointAddress',align:'center',width:35">所属店铺地址</th>
						<th data-options="field:'pointType',align:'center',width:10,formatter:formatPointType">所属店铺类型</th>
					</tr>
				</thead>
			</table>
		</div>
		<div style="float:left;width:50%;height:100%;padding-right:0.5px;box-sizing:border-box;">
			<table id="proList" class="easyui-datagrid" data-options="singleSelect:true,fit:true,fitColumns:true,pagination:false,idField:'id'">
				<thead>
					<tr>
						<th data-options="field:'images',width:100,align:'center',formatter:formatProImgUrl">商品图片</th>
						<th data-options="field:'skuName',width:180,align:'center'">商品名称</th>
						<th data-options="field:'priceOnLine',width:50,align:'center'">零售价</th>
						<th data-options="field:'num',width:50,align:'center'">活动数量</th>
						<th data-options="field:'qty',width:50,align:'center'">已售数量</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<script id="act-pro-item" type="text/html">
		{{each orderList as item i}}
		<div class="act-pro-wrap">
			<div class="act-pro-item">
				<div class="head">
					<span class="title">{{item.skuName}}</span>
					<span class="export">导出订单</span>
				</div>
				<div class="body">
					<table class="data-tb">
						<tbody>
							<tr>
								<td>零售价</td>
								<td>平均零售价</td>
								<td>订单总数</td>
								<td>成交订单数</td>
								<td>下单总金额</td>
								<td>成交总金额</td>
							</tr>
							<tr>
								<td>{{item.lotteryPrice ? item.lotteryPrice : 0}}</td>
								<td>{{item.lotteryMeanPrice ? item.lotteryMeanPrice : 0}}</td>
								<td>{{item.totalOrdersNumber ? item.totalOrdersNumber : 0}}</td>
								<td>{{item.dealQuantity ? item.dealQuantity : 0}}</td>
								<td>{{item.totalOrderAmount ? item.totalOrderAmount : 0}}</td>
								<td>{{item.clinchDealOrder ? item.clinchDealOrder : 0}}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>
		{{/each}}
	</script>
</body>
</html>