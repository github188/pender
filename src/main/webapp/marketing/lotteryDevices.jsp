<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>抽奖活动设备</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<style type="text/css">
	.oper-btn {padding: 0 5px; margin: 0 8px; background-color: #eee; border-radius: 3px; color: #fff; cursor: pointer;}
	.oper-btn.primary {background-color: #428bca;}
	.oper-btn.primary:hover {background-color: #3071a9;}
	.oper-btn.warn {background-color: #f0ad4e;}
	.oper-btn.warn:hover {background-color: #ec971f;}

	.lottery-detail {padding: 8px 57px 8px 0;}
	.detail-tb {width: 100%; text-align: center; line-height: 2.3; border-collapse: collapse;}
	.detail-tb td, .detail-tb th {border: 1px solid #e5e5e5;}
	.detail-tb th {color: #666;}

	.datagrid-row, .datagrid-td-rownumber {background-color: #f2f2f2;}
</style>
<script type="text/javascript">
	var actId, actState;

	function changeActProState(id, factoryDevNo, type) {
		var tipText = type == 2 ? '确定发布至设备吗？' : '确定从设备下线吗？';

		confirmMsg(tipText, function() {
			$.ajax({
				url: '${pageContext.request.contextPath}/marketing/lottery/updatepushMessageLottery.json',
				data: {lotteryProductId:id, factoryDevNo:factoryDevNo, type:type},
				success: function(r) {
					if (r.lottery.pushState) {
						errorMsg(r.lottery.pushState);
					}
				}
			})
		})
	}

	function updateDetail(i, row) {
		if ($('#lottery-detail-'+i).html() != '') return;
		var lottery = $.extend(true, {}, row);
		if (lottery.lotteryProductList.length == 0) {
			$('#lottery-detail-'+i).html('暂无活动内容');
		} else {
			var states = [
				'<span style="color:#32a000">发布成功</span>', 
				'<span style="color:red">发布失败</span>', 
				'执行中', 
				'未发布', 
				'<span style="color:#32a000">预热成功</span>',
				'<span style="color:red">预热失败</span>',
				'<span style="color:#32a000">活动成功</span>',
				'<span style="color:red">活动失败</span>',
				'<span style="color:#32a000">结束成功</span>',
				'<span style="color:red">结束失败</span>'
			];
			$.each(lottery.lotteryProductList, function(i, item) {
				item.productPrice = item.productPrice.toFixed(2);
				item.productNorms = item.productNorms == '' ? '无' : item.productNorms;
				item.stateStr = states[item.isSucessState];
				if (actState == 0 && (item.isSucessState == 1 || item.isSucessState == 3)) {
					item.operBtn = '<span class="oper-btn primary" onclick="changeActProState(' + item.id + ', ' + row.factoryDevNo + ', 2' +')">发布至设备</span>';
				} else if (item.isSucessState == 0 || item.isSucessState == 4 || item.isSucessState == 6 || item.isSucessState == 9) {
					item.operBtn = '<span class="oper-btn warn" onclick="changeActProState(' + item.id + ', ' + row.factoryDevNo + ', 3' +')">从设备下线</span>';
				}
			});
			template.config("escape", false);
			var tb = template('lotteryDetail', lottery);
			$('#lottery-detail-'+i).html(tb);
		}
		$('#devList').datagrid('fixDetailRowHeight',i);
	}

	//formatter
	function deviceState(val) {
		return val == 1 ? '在线' : '<span style="color:red;">离线</span>';
	}
	
	$(function() {
		var actArg = getUrlArg();
		actId = actArg.id;//获取活动id
		actState = actArg.state;//获取活动状态
		$('#devList').datagrid({
			url: '${pageContext.request.contextPath}/marketing/lottery/findLotteryDevNoProduct.json',
			queryParams: {lotteryId: actId},
			view: detailview,
			detailFormatter:function(i,row) {
				return '<div id="lottery-detail-'+i+'" class="lottery-detail"></div>';
			},
			onExpandRow: updateDetail,
			onLoadSuccess: function() {
				var $this = $(this);
				var len = $this.datagrid('getRows').length;
				for (var i = 0; i < len; i++) {
					$this.datagrid('expandRow', i);
				}
			},
			onSelect: function(i, row) {
				$(this).datagrid('unselectRow', i);
			}
		})	
	})
	
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'center',border:false,headerCls:'list-head'" title="活动设备列表">
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<!-- 活动设备列表 -->
			<table id="devList" class="easyui-datagrid" data-options="fit:true,fitColumns:true">
				<thead>
					<tr>
						<th data-options="field:'factoryDevNo',align:'center',width:10">设备组号</th>
						<th data-options="field:'typeStr',align:'center',width:45">设备类型</th>
						<th data-options="field:'pointName',align:'center',width:25">所属店铺</th>
						<th data-options="field:'pointAddress',align:'center',width:25">所属店铺地址</th>
						<th data-options="field:'deviceStatus',align:'center',width:10,formatter:deviceState">设备状态</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>

	<script id="lotteryDetail" type="text/html">
		<table class="detail-tb">
			<thead>
				<tr>
					<th style="width:28px;"></th>
					<th>名称</th>
					<th style="width:20%;">价格</th>
					<th style="width:20%;">规格</th>
					<th style="width:20%;">状态</th>
					<th style="width:100px;">操作</th>
				</tr>
			</thead>
			<tbody>
				{{each lotteryProductList as item i}}
				<tr>
					<td>{{i+1}}</td>
					<td>{{item.productName}}</td>
					<td>{{item.productPrice}}</td>
					<td>{{item.productNorms}}</td>
					<td>{{item.stateStr}}</td>
					<td>{{item.operBtn}}</td>
				</tr>
				{{/each}}
			</tbody>
		</table>
	</script>
</body>
</html>