<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>订单信息</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<style type="text/css">
	.order-detail {padding: 10px 55px 10px 0;}
	.operate-btn {color: #009cd3; cursor: pointer;}
	.operate-btn:hover {color: #2ec1f5;}
	.win-row {font-size: 14px;}
	.win-row label {font-weight: bold; margin-right: 10px; line-height: 37px;}

	.product-tb {width: 100%; text-align: center; font-size: 13px; line-height: 2; border-collapse: collapse;}
	.product-tb td {border: 1px solid #e5e5e5;}
	.product-tb thead td {font-weight: bold;}
	.product-tb tr td:nth-child(1) {width: 30px;}
	.product-tb tr td:nth-child(2) {width: 23%;}
	.product-tb tr td:nth-child(3) {width: 27%;}

	#mainGridCtn .datagrid-row, #mainGridCtn .datagrid-td-rownumber {background-color: #f2f2f2;}

	.refundgrid-ctn {margin: 0 -15px; padding: 5px 15px 15px; border-bottom: 1px solid #e5e5e5;}
	.refundgrid-ctn .title {font-size: 14px; color: #999; font-weight: bold; margin-bottom: 5px;}

	.num-box {display: inline-block; border: 1px solid #e5e5e5; border-radius: 2px; vertical-align: middle;}
	.num-box i {font-style: normal; font-size: 20px; font-weight: bold; text-align: center; color: #666; display: block; float: left; width: 24px; height: 24px; line-height: 21px; background-color: #f5f5f5; cursor: pointer; user-select: none;}
	.num-box input {display: block; float: left; border: none; outline: none; width: 34px; height: 22px; text-align: center;}
	.num-box .minus {border-right: 1px solid #e5e5e5; font-size: 25px;}
	.num-box .add {border-left: 1px solid #e5e5e5;}
</style>
<script type="text/javascript">
	var orderStates = [];

	function showOrderState(value, row, index) {
		if (value || value == 0)
			for (var i = 0; i < orderStates.length; i++)
				if (orderStates[i].code == value)
					return orderStates[i].name;
		return value;
	}

	function getPayType(value) {
		switch(value) {
			case 6:
				return '微信支付';
			case 7:
				return '支付宝支付';
			default:
				return value;
		}
	}

	function refundState(value) {
		switch(value) {
			case -1:
				return '退款失败';
			case 1:
				return '退款成功';
			case 2:
				return '退款中';
			default:
				return value;
		}
	}

	function requestRefund(m) {
		var order = $(m).data('order');
		$('#formRefund').data('order', order);
		$('#rf-payCode').html(order.payCode);
		$('#rf-skuName').html(order.skuName);
		$('#rf-qty').html(order.qty);
		$('#rf-amount').html(order.price * order.qty);
		$('#rf-surplus').html(0);
		$('#rf-num').val(1);
		$('#rf-sum').html(order.price);
		$('#rf-minus, #rf-add, #rf-num').on('click input', function() {
			var num = +$('#rf-num').val();
			$('#rf-sum').html(order.price*num);
		})
		openDialog('winRefund', '退款');
		$('#refund-list').show();
		$('#refundGrid').datagrid({
			url: '${pageContext.request.contextPath}/operation/order/findRefundDetail.json',
			queryParams: {orderNo:order.orderNo, skuId: order.skuId},
			loadFilter: function(r) {
				return {rows:r.refundList, total:r.refundList.length};
			},
			onLoadSuccess: function(data) {
				var refundings = data.rows.filter(function(n) {//退款中的订单
					return n.state == 2;
				});
				var refundeds = data.rows.filter(function(n) {//退款成功的订单
					return n.state == 1;
				});
				var refundingNum = 0, refundedNum = 0;
				for (var i = 0; i < refundings.length; i++) {
					refundingNum += refundings[i].refundQty;
				}
				for (var i = 0; i < refundeds.length; i++) {
					refundedNum += refundeds[i].refundQty;
				}
				$('#rf-surplus').html(order.qty - refundingNum - refundedNum);//计算可退数量
				$(m).parent().siblings('.refundNum').html(refundeds.length);//更新退货数量
				if (data.rows.length == 0) {
					$('#refund-empty').show();
					$('#refund-list').hide();
				} else {
					$('#refund-list').show();
					$('#refund-empty').hide();
				}
			}
		})
	}

	function saveRefund() {
		if (!$('#formRefund').form('enableValidation').form('validate')) return;
		
		var order = $('#formRefund').data('order');
		var refundQty = +$('#rf-num').val();
		if (refundQty <= 0 || refundQty > +$('#rf-surplus').html()) {
			infoMsg('退货数量有误，请重新输入');
			return;
		}
		$.ajax({
			type: 'post',
			beforeSend: function() {
				$('#refundConfirmBtn').linkbutton('disable');
			},
			data: {
				orderNo: order.orderNo,
				refundQty: refundQty,
				skuId: order.skuId,
				reason: $('#rf-refundExplain').val()
			},
			url: '${pageContext.request.contextPath}/operation/order/auditOrderRefund.json',
			info: '已发起退款申请',
			task: function() {
				closeDialog('winRefund');
				//$('#orderGrid').datagrid('reload');
				$('#formRefund').form('clear');
			},
			complete: function() {
				$('#refundConfirmBtn').linkbutton('enable');
			}
		})
	}

	$(function() {
		//获取订单状态信息
		var queryOrderStates = [{'code':'', 'name':'所有'}];
		var cache = getAllSysTypes();
		for (var i = 0; i < cache.length; i++) {
			if (cache[i].type == 'ORDER_STATE') {
				orderStates.push(cache[i]);
				queryOrderStates.push(cache[i]);
			}
		}
		$('#q_order_state').combobox({data:queryOrderStates});

		$('#orderGrid').datagrid({
			url: '${pageContext.request.contextPath}/operation/order/find.json',
			view: detailview,
			detailFormatter:function(index,row) {
				return '<div id="order-detail'+index+'" class="order-detail"></div>';
			},
			onExpandRow: function(index,row) {
				var payCode = row.payCode;
				var orderState = row.state;
				var detailsHtml = '';
				if (row.orderDetails.length) {
					$.each(row.orderDetails, function(i, d) {
						d.payCode = payCode;
						d.state = orderState;
					})
					var detailsHtml = template('order-detail-tb', row);
				} else {
					detailsHtml = '<div style="font-size:13px;">暂无详细信息</div>';
				}

				$('#order-detail'+index).html(detailsHtml);
				$('#orderGrid').datagrid('fixDetailRowHeight',index);

				if (row.orderDetails.length) {
					$('#order-detail'+index).find('.operate-btn').each(function(i) {
						$(this).data('order', row.orderDetails[i]);
					})
				}
			},
			onLoadSuccess: function() {
				var $this = $(this);
				var len = $this.datagrid('getRows').length;
				for (var i = 0; i < len; i++) {
					$this.datagrid('expandRow', i);
				}
			}
		})

		$('#rf-num').on('input', function() {
			var val = $(this).val();
			if (val == '') return;
			$(this).val(val.replace(/[^\d]/g,''));
		})

		$('#rf-minus').click(function() {
			var val = +$('#rf-num').val();
			$('#rf-num').val(val-1);
		})

		$('#rf-add').click(function() {
			var val = +$('#rf-num').val();
			$('#rf-num').val(val+1);
		})
	})
	
	function exportOrder() {
		if ($("#orderGrid").datagrid('getRows').length === 0) {
			infoMsg("当前无订单记录");
			return;
		}
	    window.location.href = '${pageContext.request.contextPath}/operation/order/export.xls?' + $('#orderQueryForm').formSerialize();
	}
</script>
</head>
<body class="easyui-layout">
<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
	<form id="orderQueryForm" class="search-form">
		<input type="hidden" id="q_order_category" name="category">
		<div class="form-item">
			<div class="text">交易单号</div>
			<div class="input">
				<input id="q_order_code" name="payCode" class="easyui-textbox" data-options="prompt:'交易单号'">
			</div>
		</div>
		<!-- <div class="form-item">
			<div class="text">商品名称</div>
			<div class="input">
				<input id="q_order_skuName" name="skuName" class="easyui-textbox" data-options="prompt:'商品名称'">
			</div>
		</div> -->
		<div class="form-item">
			<div class="text">店铺名称</div>
			<div class="input">
				<input id="q_order_storeName" name="pointName" class="easyui-textbox" data-options="prompt:'店铺名称'">
			</div>
		</div>
		<div class="form-item" style="width:377px;">
			<div class="text">付款时间</div>
			<div class="input">
				<input id="q_order_startDate" class="easyui-datebox" name="startDate" data-options="editable:false"> - <input id="q_order_endDate" class="easyui-datebox" name="endDate" data-options="editable:false">
			</div>
		</div>
		<div class="form-item">
			<div class="text">订单状态</div>
			<div class="input">
				<input id="q_order_state" class="easyui-combobox" name="state" data-options="panelHeight:'auto',valueField:'code',textField:'name',editable:false">
			</div>
		</div>
	</form>
	<div class="search-btn" onclick="queryData('orderGrid','orderQueryForm')">查询</div>
	<div class="search-btn" onclick="resetForm('orderQueryForm')">重置</div>
	<sec:authorize access="export">
	   <div class="search-btn" onclick="exportOrder()">导出</div>
	</sec:authorize>
</div>
<div id="mainGridCtn" data-options="region:'center',border:false,headerCls:'list-head'" title="订单信息列表">
	<div style="box-sizing:border-box; height:100%; padding:10px;">
		<table id="orderGrid" data-options="nowrap:false,fit:true,fitColumns:true,singleSelect:true,idField:'id'">
			<thead>
				<tr>
					<th data-options="field:'payCode',align:'center',width:200,sortable:true">交易单号</th>
					<th data-options="field:'factoryDevNo',align:'center',width:60">设备组号</th>
					<th data-options="field:'pointName',align:'center',width:150">店铺名称</th>
					<th data-options="field:'amount',align:'center',width:60,sortable:true">实付金额</th>
					<th data-options="field:'refundAmount',align:'center',width:60,sortable:true">已退款金额</th>
					<th data-options="field:'payType',align:'center',width:80,formatter:getPayType">支付类型</th>
					<th data-options="field:'createTime',align:'center',width:130,sortable:true">创建时间</th>
					<th data-options="field:'payTime',align:'center',width:130,sortable:true">付款时间</th>
					<th data-options="field:'state',align:'center',width:60,formatter:showOrderState,sortable:true">状态</th>
					<!-- <sec:authorize access="update,save"><th data-options="field:'operate',align:'center',width:50,formatter:getOperateType">操作</th></sec:authorize> -->
				</tr>
			</thead>
		</table>
	</div>
</div>
<!-- 退款窗口 -->
<div id="winRefund" class="easyui-dialog" data-options="closed:true,buttons:'#refundBtns'" style="width:850px; height: 485px; padding:10px 15px;">
	<div id="refund-empty" class="refundgrid-ctn">
		<div class="title">暂无任何退款</div>
	</div>
	<div id="refund-list" class="refundgrid-ctn">
		<div class="title">退款明细</div>
		<table id="refundGrid" class="easyui-datagrid" data-options="fitColumns:true,singleSelect:true,pagination:false">
			<thead>
				<tr>
					<th data-options="field:'code',align:'center',width:20">退款单号</th>
					<th data-options="field:'refundQty',align:'center',width:10">退货数量</th>
					<th data-options="field:'feeRefund',align:'center',width:10">退款金额</th>
					<th data-options="field:'reason',align:'center',width:15">退款说明</th>
					<th data-options="field:'createTime',align:'center',width:20">退款提交时间</th>
					<th data-options="field:'updateTime',align:'center',width:20">退款完成时间</th>
					<th data-options="field:'state',align:'center',width:10,formatter:refundState">退款状态</th>
				</tr>
			</thead>
		</table>
	</div>
	<form id="formRefund">
		<div class="win-row">
			<label>交易单号</label>
			<span id="rf-payCode"></span>
		</div>
		<div class="win-row">
			<label>商品名称</label>
			<span id="rf-skuName"></span>
		</div>
		<div class="win-row">
			<label>商品数量</label>
			<span id="rf-qty"></span>
		</div>
		<div class="win-row">
			<label>实付金额</label>
			<span id="rf-amount"></span>
		</div>
		<div class="win-row">
			<label>退货数量</label>
			<div class="num-box">
				<i id="rf-minus" class="minus">-</i>
				<input id="rf-num" class="val" type="text">
				<i id="rf-add" class="add">+</i>
			</div>
			<span style="font-size:12px;">(最多<em id="rf-surplus" style="font-style:normal; color:red;"></em>件)</span>
		</div>
		<div class="win-row">
			<label>退款金额</label>
			<span id="rf-sum" style="color:red">0</span>
		</div>
		<div class="win-row">
			<label style="vertical-align:top">退款说明</label>
			<textarea id="rf-refundExplain" name="reason" style="margin-top: 12px; width:325px; height:50px; outline:none; resize:none;" class="easyui-validatebox" data-options="required:true,prompt:'必填项',validType:['length[1,125]']"></textarea>
		</div>
		<div id="refundBtns">
			<span id="refundConfirmBtn" class="easyui-linkbutton" data-options="iconCls:'icon-save'" onclick="saveRefund()">确定</span>
			<span class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" onclick="closeDialog('winRefund')">取消</span>
		</div>
	</form>
</div>
<!-- 查看窗口 -->

<script id="order-detail-tb" type="text/html">
	<table class="product-tb">
		<thead>
			<tr>
				<td></td>
				<td>SKU</td>
				<td>商品名称</td>
				<td>零售价</td>
				<td>数量</td>
				<td>规格</td>
				<td>退货数量</td>
				<td>退款金额</td>
				<td>类型</td>
				<td>折扣</td>
				<sec:authorize access="update,save"><td>操作</td></sec:authorize>
			</tr>
		</thead>
		<tbody>
			{{each orderDetails as item i}}
			<tr>
				<td>{{i+1}}</td>
				<td>{{item.sku}}</td>
				<td>{{item.skuName}}</td>
				<td>{{item.price.toFixed(2)}}</td>
				<td>{{item.qty}}</td>
				<td>{{item.spec}}</td>
				<td class="refundNum">{{item.refundQty}}</td>
				<td>{{item.refundAmount != 0 ? item.refundAmount.toFixed(2) : 0}}</td>
				<td>{{item.orderTypeDesc}}</td>
				<td>{{item.discount == 1 ? '不打折' : item.discount*10+'折'}}</td>
				<sec:authorize access="update,save">
				<td class="operate-ctn">
					{{if item.state == 8}}
					<span class="operate-btn" onclick="requestRefund(this)">退款</span>
					{{/if}}
				</td>
				</sec:authorize>
			</tr>
			{{/each}}
		</tbody>
	</table>
</script>
</body>
</html>