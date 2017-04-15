<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>提现处理</title>
<%@ include file="/common.jsp"%>
<style type="text/css">
	.dg-title {font-size: 18px; text-align: center; font-weight: bold; margin-bottom: 10px;}
	.dg-tip {font-size: 16px; color: red; text-align: center; font-weight: bold; margin-bottom: 20px;}
	.dg-row {font-size: 15px; line-height: 25px;}
	.dg-row label {display: inline-block; width: 62px; text-align: right; margin-right: 10px;}
</style>
<script type="text/javascript">
	var tradeFlowId;

    $(function() {
    	$('#withdrawList').datagrid({
            url:'${pageContext.request.contextPath}/fund/withdraw/find.json',
            onSelect: function(rowIndex, rowData) {
                tradeFlowId = rowData.id;
            }
        });
    });
    
    // 通联提现
    function saveWithdraw() {
    	$.ajax({
    		url: '${pageContext.request.contextPath}/fund/fundStatic/saveWithdrawTradeFlow.json',
    		data: {id: tradeFlowId},
    		async: false,
    		success: function(r) {
    			closeDialog('winWithdraw');
    			queryData('withdrawList','withdrawSearch');
    			infoTip('提现操作成功！');
    		}
    	});
    }
    
    function formatCardType(cardType) {
        switch(cardType) {
           case 1:
            return "中国工商银行";
           case 2:
            return "中国建设银行";
           case 3:
            return "招商银行";
           case 4:
            return "中国银行";
           case 5:
            return "中国农业银行";
           case 6:
            return "交通银行";
           case 7:
            return "中信银行";
           case 8:
            return "光大银行";
           case 9:
            return "华夏银行";
           case 10:
            return "中国民生银行";
           case 11:
            return "平安银行";
           case 12:
            return "兴业银行";
           case 13:
            return "浦发银行";
           case 14:
            return "广发银行";
           case 15:
            return "中国邮政储蓄银行";
           case 16:
            return "贵阳银行";
           case 17:
            return "北京银行";
           case 18:
            return "宁波银行";
           case 19:
            return "南京银行";
           case 20:
            return "东莞银行";
           default:
            return "";
        }
    }
    
	function formatStatus(value) {
	    switch(value) {
	        case 1:
	            return '<span style="color:#0075bf;">待处理</span>';
	        case 2:
	            return '交易成功';
	        case 3:
	            return '交易失败';
	        default:
	            return value;
	    }
	}
	
	function formatOperation(value, row) {
		var tradeStatus = row.tradeStatus;
		var title = '';
		switch(tradeStatus) {
	        case 1://待处理
	        	title = '确认';
	        	break;
	        case 3://交易失败
	            title = '再次确认';
	            break;
	        default:
	            break;
	    }
		return (tradeStatus === 1 || tradeStatus === 3) ? '<a href="javascript:void(0)" onclick="openDialog(\'winWithdraw\')" >' + title + '</a>' : '';
	}
</script>
</head>
<body class="easyui-layout">
<div data-options="region:'north',border:false,split:true" style="padding:15px; height:92px;">
	<!-- 查询 -->
	<form id="withdrawSearch" class="search-form">
		<div class="form-item">
			<div class="text">所属机构</div>
			<div class="input">
				<input name="orgName" class="easyui-textbox" data-options="prompt:'所属机构'" />
			</div>
		</div>
		<div class="form-item">
			<div class="text">状态</div>
			<div class="input">
				<select class="easyui-combobox" name="tradeStatus" data-options="panelHeight:'auto',editable:false">
				    <option value="">请选择</option>
				    <option value="1">待处理</option>
				    <option value="2">成功</option>
				    <option value="3">失败</option>
				</select>
			</div>
		</div>
	</form>
	<div class="search-btn" onclick="queryData('withdrawList','withdrawSearch')">查询</div>
	<div class="search-btn" onclick="resetForm('withdrawSearch')">重置</div>
</div>
<div data-options="region:'center',border:false,headerCls:'list-head'" title="提现处理" style="padding:10px;">
	<table id="withdrawList" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,singleSelect:true">
		<thead>
			<tr>
				<th data-options="field:'realName',width:100,align:'center'">真实姓名</th>
				<th data-options="field:'orgName',width:300,align:'center'">所属机构</th>
	            <th data-options="field:'cardType',width:200,align:'center',formatter:formatCardType">开户行</th>
	            <th data-options="field:'cardOwner',width:100,align:'center'">持卡人</th>
	            <th data-options="field:'cardNo',width:300,align:'center'">卡号</th>
	            <th data-options="field:'tradeAmount',width:100,align:'center'">提现金额</th>
	            <th data-options="field:'tradeTime',width:200,align:'center'">申请时间</th>
	            <th data-options="field:'tradeStatus',width:100,align:'center',formatter:formatStatus">状态</th>
	            <th data-options="field:'operate',width:100,align:'center',formatter:formatOperation">操作</th>
	        </tr>
	    </thead>
	</table>
</div>

<!-- 确认处理 -->
<div id="winWithdraw" title="温馨提醒" class="easyui-dialog" data-options="closed:true,buttons:'#withdrawBtns'" style="width:440px; padding:20px 30px;">
	<div class="dg-title">确 认</div>
	<div class="dg-tip">请确认已将提现金额充值到通联备付金账户</div>
	<div class="dg-row">
		<label>开户银行</label>
		<span>中国建设银行广州越秀支行</span>
	</div>
	<div class="dg-row">
		<label>账户名</label>
		<span>通联支付网络服务股份有限公司客户备付金</span>
	</div>
	<div class="dg-row">
		<label>银行账号</label>
		<span>44001400101053011988</span>
	</div>
	<div id="withdrawBtns">
		<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveWithdraw();">确认</a>
		<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winWithdraw')">取消</a>
	</div>
</div>
</body>
</html>