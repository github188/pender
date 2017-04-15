<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>资金统计</title>
<%@ include file="/common.jsp"%>
<style type="text/css">
	.form-cloumn {width: 100%;}
	.form-cloumn .text {float: left; width: 60px; margin: 5px 15px 5px 5px;}
	.form-cloumn .input {float: left; line-height: 28px;}
	#win_withdrawal .form-cloumn .text {width: 60px;}
	.orange-color {color: #FF9900;}

	.btn-df {margin: 14px 0 0 20px; border-radius: 4px;}
	.balance-text {color: #999; text-indent: 5px;}
	.balance-value {color: #f00;}
	.balance-value span {font-size: 30px;}
	.time-nav {font-size: 14px; color: #999; line-height: 30px;}
	.time-nav span {margin-right: 25px; cursor: pointer;}
	.time-nav span:hover, .time-nav span.active {color: #009cd3;}
	.statistics-data {font-size: 14px; line-height: 30px;}
</style>
<script type="text/javascript">
	$.extend($.fn.validatebox.defaults.rules,{
	    maxLength: {
	        validator: function(value, param) {
	            return value.length <= param[0];
	        },
	        message: '长度不能大于{0}位！'  
	    },
		minLength: {
		    validator: function(value, param) {
		        return value.length >= param[0];
		    },
		    message: '长度不能小于{0}位！'
		},
		mustNumber: {
			validator: function(value) {
				var reg = new RegExp("^[0-9]*$");  
		        return reg.test(value);
		    },
		    message: '只能包含数字！'
		}
	});

	//交易流水查询的开始时间和结束时间
	var startTime, endTime;
	// 是否已绑定卡
	var isBinding = false;
	// 可提现金额
	var withdrawalAmount = 0.00;
	// 最多可提现金额
	var maxAmount = 0.00;
	// 银行卡信息
	var card = null;

	var InterValObj; // timer变量，控制时间  
	var count = 60; // 间隔函数，1秒执行  
	var curCount; // 当前剩余秒数

	var WithdrawalInterValObj; // timer变量，控制时间  
	var withdrawalCount = 60; // 间隔函数，1秒执行  
	var withdrawalCurCount; // 当前剩余秒数

	var checkCode = ""; // 验证码

	/**
	 * 初始化数据/状态
	 */
	function initDataAndStatus(data) {
		// 基础数据
	    $("#withdrawal-amount").html(data.withdrawalAmount);
	    
	    // 状态
	    isBinding = data.isBinding;
	    withdrawalAmount = data.withdrawalAmount;

	    if (!isBinding) {
	    	// 没绑定银行卡
	    	$('#bindCard').html('绑定银行卡');
	    	// 1.绑卡Dialog
	    	$('#tbxCheckCardOwner, #tbxCheckCardNo').textbox({required:false});
	    	$('#tbxCheckCardOwner, #tbxCheckCardNo').parents('.form-row').hide();
	    	$('#tbxCardNo').textbox('textbox').validatebox({validType: ['minLength[16]', 'maxLength[19]', 'mustNumber']}); 
	    } else {
	    	// 已经绑定了银行卡
	    	$('#bindCard').html('解绑银行卡');
	    	card = data.card;
	    	var cardOwnerShow = "*" + card.cardOwner.substring(1);
	    	var cardNoShow = card.cardNo.substring(0, 4) + "********" + card.cardNo.substring(card.cardNo.length - 4);
	    	var mobileNoShow = card.mobileNo.substring(0, 3) + "****" + card.mobileNo.substring(card.mobileNo.length - 4);
	    	
	    	// 1.绑卡Dialog
	    	$("#span-card").text("解绑");
	    	$('#cmbCardType').next().hide();
	    	$("#span-card-type").text(formatCardType(card.cardType));
	    	$('#cmbMode').next().hide();
	    	$("#span-mode").text(formatMode(card.mode));
	        
	        $('#tbxCardOwner').textbox("setValue", cardOwnerShow); // 持卡人
	        $('#tbxCardNo').textbox("setValue", cardNoShow); // 卡号
	        $('#tbxMobileNo').textbox("setValue", mobileNoShow); // 手机号码
	        $('#tbxCardOwner, #tbxCardNo, #tbxMobileNo').textbox("readonly", "readonly");
	        
	        $('#tbxCheckCardOwner, #tbxCheckCardNo').textbox({required:true});
	        
	        // 2.提现Dialog
	        $("#withdrawal-card-type").text(formatCardType(card.cardType));
	        $("#withdrawalCardOwner").textbox("setValue", cardOwnerShow); // 持卡人
	        $("#withdrawalCardNo").textbox("setValue", cardNoShow); // 卡号
	        $("#withdrawalMobileNo").textbox("setValue", mobileNoShow); // 手机号码
	        $("#withdrawalAvailableAmount").text(withdrawalAmount); // 可用金额
	        
	        maxAmount = subFloat(withdrawalAmount, 1.5); // 最多可提现金额 = 可提现金额 - 提现费用
	        $("#withdrawalAmount").attr("data-options", "required:true, prompt:'最多可提现"+ maxAmount +"'"); // 提现金额
	        $("#withdrawalAmount").next().children().attr("placeholder", "最多可提现" + maxAmount);   
	    }
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
	
	function formatMode(mode) {
		switch(mode) {
		   case 0:
			return "个人账户";
		   case 1:
			return "公司账户";
		   default:
			return "";
		}
	}

	// 绑定/解绑 银行卡Dialog
	function bindCardDialog() {
	    $('#win_bindcard').dialog({
	        title: !isBinding ? '绑定银行卡' : '解绑银行卡',
	        closed: true,
	        cache: false,
	        modal: true
	    });
	    $('#win_bindcard').dialog('open');
	    $('#fom_bindcard').form('disableValidation');
	}

	// 绑定/解绑 业务处理
	function bindCard() {
		if (isBinding) $('#tbxCardNo').textbox('textbox').validatebox('disableValidation');
		if (!$('#fom_bindcard').form('enableValidation').form('validate')) {
			return;
		}
		
	    if (isBinding) {
	    	// 已绑定了银行卡，进行解绑操作
	    	var cardOwner = $("#tbxCheckCardOwner").textbox("getValue");
	    	var cardNo = $("#tbxCheckCardNo").textbox("getValue");
	    	if (cardOwner != card.cardOwner) {
	    		infoMsg("请输入正确的持卡人全名");
	            return;
	    	}
	    	if (cardNo != card.cardNo) {
	    		infoMsg("请输入正确的完整卡号");
	            return;
	    	}
	    	$("#input_cardId").val(card.id);
	    	$("#input_mobileNo").val(card.mobileNo);
	    } else {
		    var mobileNo = $("#tbxMobileNo").textbox("getValue");
	    	$("#input_mobileNo").val(mobileNo);
	    }
	    
	    // 验证码校验
	    var code = $("#tbxCheckCode").textbox("getValue");
	    if (code != checkCode) {
	    	infoMsg("验证码校验失败，请输入正确的验证码。");
	        return;
	    }
		
	    postForm({
	        form: 'fom_bindcard',
	        url: '${pageContext.request.contextPath}/fund/fundStatic/saveCard.json',
	        task:function(data) {
	        	if (!isBinding)
	       		   infoTip('绑定银行卡成功');
	        	else
	        	   infoTip('解绑银行卡成功');
	            // 清除数据
	            $("#tbxCheckCardOwner, #tbxCheckCardNo, #tbxCheckCode").textbox("setValue", "");
	            setTimeout(function() {
	                $(parent.frameBody).find('#main').attr('src', '${pageContext.request.contextPath}/fund/fundStatic/forward.do');
	            }, 2000);
	        }
	    });
		
	}

	// 提现 Dialog
	function withdrawalDialog() {
		if (!isBinding) {
	        // 没绑定银行卡
	        infoMsg("请先绑定银行卡，再做提现操作。");
	        return;
	    }
		// 校验可提现金额是否大于0
		if (withdrawalAmount <= 0) {
			infoMsg("没有可提现金额，无法提现。");
	        return;
		}
		$('#win_withdrawal').dialog({
	        title: '提现',
	        closed: true,
	        cache: false,
	        modal: true
	    });
	    $('#win_withdrawal').dialog('open');
	    $('#fom_withdrawal').form('disableValidation');
	}

	// 提现 业务处理
	function withdrawal() {
		if (!$('#fom_withdrawal').form('enableValidation').form('validate')) {
	        return;
	    }
		
		var tradeAmount = $("#withdrawalAmount").textbox("getValue");
		if (tradeAmount < 0) {
			infoMsg("请输入合法的提现金额！");
	        return;
		}
		if (tradeAmount > maxAmount) {
			infoMsg("最多可提现" + maxAmount + "元，请重新输入");
	        return;
		}
		
		// 验证码校验
	    var code = $("#withdrawalCheckCode").textbox("getValue");
	    if (code != checkCode) {
	        infoMsg("验证码校验失败，请输入正确的验证码。");
	        return;
	    }

		postForm({
	        form: 'fom_withdrawal',
	        url: '${pageContext.request.contextPath}/fund/fundStatic/save.json',
	        task:function(data) {
	        	infoTip('提现申请提交成功！');
	        	setTimeout(function() {
		            $(parent.frameBody).find('#main').attr('src', '${pageContext.request.contextPath}/fund/fundStatic/forward.do');
	        	}, 2000);
	        }
	    });
	}

	// 提现金额输入框change事件
	function withdrawalAmountChange() {
		var amount = $(this).textbox("getValue");
		if ("" === amount) {
		    $("#withdrawalDeduction").text(0); // 实扣金额
		    return;
		}
		if (amount > maxAmount) {
			infoMsg("最多可提现金额为" + maxAmount + "元，请重新输入");
			return;
		}
		$("#withdrawalDeduction").text(addFloat(amount, 1.5)); // 实扣金额
	}

	// 获取验证码点击事件
	function getCheckCodeHandler() {
		var phoneNumber = $("#tbxMobileNo").textbox("getValue");
		if (!isBinding) {
	        // 没绑定银行卡
	        // 1.合法性校验
			if (!phoneNumberCheck(phoneNumber)) {
				infoMsg("请输入合法的手机号码");
				return;
			}
		    // 2.向当前卡号预留手机号码发送验证码
		    getCheckCode(phoneNumber);
		} else {
			// 已经绑定过了银行卡
			// 1.向当前卡号预留手机号码发送验证码
			var cardOwner = $("#tbxCheckCardOwner").textbox("getValue");
	        var cardNo = $("#tbxCheckCardNo").textbox("getValue");
	        if (cardOwner != card.cardOwner) {
	            infoMsg("请输入正确的持卡人全名");
	            return;
	        }
	        if (cardNo != card.cardNo) {
	            infoMsg("请输入正确的完整卡号");
	            return;
	        }
			getCheckCode(null);
		}
	}

	//  获取验证码
	function getCheckCode(phoneNumber) {
		$.ajax({
	        url: '${pageContext.request.contextPath}/fund/fundStatic/saveCode.json',
	        type: 'post',
	        dataType: 'json',
	        data: {
	        	phoneNumber: phoneNumber,
	        	isBinding: isBinding
	        },
	        async: false,
	        success: function (data) {
	        	if (data.code == -1 || data.code == 230) {
	        		infoMsg("请输入合法的手机号码");
	        		return;
	            }
	        	if (data.code == 226 || data.code == 123) {
	        		infoMsg("获取验证码失败，请稍后重试。");
	        		return;
	            }
	        	if (data.state) {
		            // 短信验证码发送成功
		            checkCode = data.data;
		            // 【获取验证码】按钮点击后的样式调整
		            curCount = count;
		            $("#lnkCardGetCheckCode").removeAttr("href").attr("onclick", "return false;");
		            $("#lnkCardGetCheckCode").removeClass("l-btn:hover");
		            $("#lnkCardGetCheckCode .l-btn-text").text(curCount + "秒后重新获取");
		            InterValObj = window.setInterval(SetRemainTimes, 1000); // 启动计时器，1秒执行一次
	        	}
	        }
	    });
	}

	// 获取验证码点击事件(提现)
	function getWithdrawalCheckCodeHandler() {
		var tradeAmount = $("#withdrawalAmount").textbox("getValue");
	    if (tradeAmount > maxAmount) {
	        infoMsg("最多可提现" + maxAmount + "元，请重新输入");
	        return;
	    }
	    
		$.ajax({
	        url: '${pageContext.request.contextPath}/fund/fundStatic/saveCode.json',
	        type: 'post',
	        dataType: 'json',
	        data: {
	            isBinding: isBinding
	        },
	        async: false,
	        success: function (data) {
	            if (data.code == -1 || data.code == 230) {
	                infoMsg("请输入合法的手机号码");
	                return;
	            }
	            if (data.code == 226 || data.code == 123) {
	                infoMsg("获取验证码失败，请稍后重试。");
	                return;
	            }
	            if (data.state) {
	                // 短信验证码发送成功
	                checkCode = data.data;
	                // 【获取验证码】按钮点击后的样式调整
	                withdrawalCurCount = withdrawalCount;
	                $("#lnkWithdrawalCheckCode").removeAttr("href").attr("onclick", "return false;");
	                $("#lnkWithdrawalCheckCode").removeClass("l-btn:hover");
	                $("#lnkWithdrawalCheckCode .l-btn-text").text(withdrawalCurCount + "秒后重新获取");
	                WithdrawalInterValObj = window.setInterval(WithdrawalSetRemainTimes, 1000); // 启动计时器，1秒执行一次
	            }
	        }
	    });
	}

	//验证手机号码是否通过，true:通过; false:未通过
	function phoneNumberCheck(phoneNumber) {
		var phoneReg = /^(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/;
		return !!phoneNumber.match(phoneReg);
	}

	// timer处理函数
	function SetRemainTimes() {
	    if (curCount == 0) {
	        window.clearInterval(InterValObj);// 停止计时器  
	        $("#lnkCardGetCheckCode").attr("href", "javascript:void(0)").attr("onclick", "getCheckCodeHandler();");
	        $("#lnkCardGetCheckCode .l-btn-text").text("获取验证码");
	    } else {  
	        curCount--;
	        $("#lnkCardGetCheckCode .l-btn-text").text(curCount + "秒后重新获取");
	    }
	}

	// timer处理函数(提现)
	function WithdrawalSetRemainTimes() {
	    if (withdrawalCurCount == 0) {
	        window.clearInterval(WithdrawalInterValObj);// 停止计时器  
	        $("#lnkWithdrawalCheckCode").attr("href", "javascript:void(0)").attr("onclick", "getWithdrawalCheckCodeHandler();");
	        $("#lnkWithdrawalCheckCode .l-btn-text").text("获取验证码");
	    } else {  
	    	withdrawalCurCount--;
	        $("#lnkWithdrawalCheckCode .l-btn-text").text(withdrawalCurCount + "秒后重新获取");
	    }
	}

	//导出
	function exportTrade() {
		window.location.href = '${pageContext.request.contextPath}/fund/fundStatic/export.xls?startTime=' + formatTime(startTime) + '&endTime=' + formatTime(endTime);
	}

	//formatter
	function formatType(value) {
		switch(value) {
			case 1:
				return '提现';
			case 2:
				return '商品销售';
			case 3:
				return '退款'; 
			default:
				return value;
		}
	}
	function formatAmount(value, row) {
		if (row.tradeType == 2) {
			return value == 0 ? 0 : '+'+value;
		} else {
			return value == 0 ? 0 : '-'+value;
		}
	}
	function formatBalance(value, row) {
		return value == 0 ? 0 : parseFloat(value).toFixed(2);
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

	$(function(){
		$.ajax({
	        url:'${pageContext.request.contextPath}/fund/fundStatic/findSysData.json',
	        async:false,
	        success:function(data, status, xhr) {
	        	// 初始化数据/状态
	        	initDataAndStatus(data);
	        },
	        error:function(data, status, xhr) {
	            errorMsg("初始化数据异常");
	        }
	    });

		$('#tradeList').datagrid({
			url: '${pageContext.request.contextPath}/fund/fundStatic/find.json',
			onLoadSuccess: function(data) {
				$('#income').html(data.withdraw == 0 ? 0 : '+'+parseFloat(data.withdraw).toFixed(2));
				$('#cost').html(data.pay == 0 ? 0 : '-'+parseFloat(data.pay).toFixed(2));
			}
		})

		$('#time-nav').on('click', 'span', function(){
			$(this).addClass('active').siblings().removeClass('active');
			var index = $(this).index();
			var today = new Date;
			switch(index) {
				case 0:
					startTime = '', endTime = '';
					break;
				case 1:
					startTime = today.getFullYear() + '-' + (today.getMonth()+1) + '-' + (today.getDate()-7),
					endTime = today.getFullYear() + '-' + (today.getMonth()+1) + '-' + today.getDate();
					break;
				case 2:
					startTime = today.getFullYear() + '-' + today.getMonth() + '-' + today.getDate(),
					endTime = today.getFullYear() + '-' + (today.getMonth()+1) + '-' + today.getDate();
					break;
				case 3:
					startTime = today.getFullYear() + '-' + (today.getMonth()-2) + '-' + today.getDate(),
					endTime = today.getFullYear() + '-' + (today.getMonth()+1) + '-' + today.getDate();
					break;
				case 4:
					startTime = today.getFullYear() + '-' + (today.getMonth()-5) + '-' + today.getDate(),
					endTime = today.getFullYear() + '-' + (today.getMonth()+1) + '-' + today.getDate();
					break;
				default:
					startTime = '', endTime = '';
					break;
			}
			$('#tradeList').datagrid('load', {startTime: formatTime(startTime), endTime: formatTime(endTime)});
		})
	})
</script>
</head>
<body class="easyui-layout">
<div data-options="region:'north',border:false,split:true" style="padding:15px; height:92px;">
	<div class="pull-left" style="margin-right:30px;">
		<div class="balance-text">账户余额</div>
		<div class="balance-value"><span id="withdrawal-amount"></span>元</div>
	</div>
	<span id="bindCard" class="btn-df" onclick="bindCardDialog();"></span>
	<span class="btn-df" onclick="withdrawalDialog();">提现</span>
</div>
<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#fundTool'" class="easyui-layout" title="交易流水">
	<div id="fundTool">
		<a href="javascript:void(0)" class="icon-export easyui-tooltip" data-options="content:'导出交易流水'" onclick="exportTrade()"></a>
	</div>
	<div data-options="region:'north',border:false" style="height:40px;padding:5px 10px;">
		<div id="time-nav" class="time-nav pull-left">
			<span class="active">全部</span>
			<span>最近七天</span>
			<span>1个月</span>
			<span>3个月</span>
			<span>6个月</span>
		</div>
		<div class="statistics-data pull-right">
			<span style="margin-right:50px;">当前查询结果汇总</span>
			收：<span id="income" style="color:#f00;margin-right:40px;"></span>
			支：<span id="cost" style="color:#008000;margin-right:10px;"></span>
		</div>
	</div>
	<div data-options="region:'center',border:false" style="padding:0 10px 10px;">
		<table id="tradeList" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true">
			<thead>
    			<tr>
					<th data-options="field:'tradeTime',width:100,align:'center'">交易时间</th>
					<th data-options="field:'tradeType',width:100,align:'center',formatter:formatType">交易类型</th>
		            <th data-options="field:'tradeAmount',width:100,align:'center',formatter:formatAmount">交易金额</th>
		            <th data-options="field:'balance',width:100,align:'center',formatter:formatBalance">账户余额</th>
		            <th data-options="field:'tradeStatus',width:100,align:'center',formatter:formatStatus">状态</th>
		        </tr>
		    </thead>
		</table>
	</div>
	<%-- 绑定/解绑 银行卡 --%>
	<div id="win_bindcard"  class="easyui-dialog" data-options="closed:true,buttons:'#bindBtns'" style="width:420px;padding:10px 20px 20px 20px;">
	    <form id="fom_bindcard" method="post">
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">账户类型</div>
					<div class="input">
						<select id="cmbMode" name="mode" class="easyui-combobox isBinding" data-options="editable:false">
			                <option value="0">个人账户</option>
			                <option value="1">公司账户</option>
			            </select>
			            <span id="span-mode" class="unbind-card"></span>
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">开户行</div>
					<div class="input">
						<select id="cmbCardType" name="cardType" class="easyui-combobox isBinding" data-options="editable:false">
			                <option value="1">中国工商银行</option>
			                <option value="2">中国建设银行</option>
			                <option value="3">招商银行</option>
			                <option value="4">中国银行</option>
			                <option value="5">中国农业银行</option>
			                <option value="6">交通银行</option>
		                    <option value="7">中信银行</option>
		                    <option value="8">光大银行</option>
		                    <option value="9">华夏银行</option>
		                    <option value="10">中国民生银行</option>
		                    <option value="11">平安银行</option>
		                    <option value="12">兴业银行</option>
		                    <option value="13">浦发银行</option>
		                    <option value="14">广发银行</option>
		                    <option value="15">中国邮政储蓄银行</option>
		                    <option value="16">贵阳银行</option>
		                    <option value="17">北京银行</option>
		                    <option value="18">宁波银行</option>
		                    <option value="19">南京银行</option>
		                    <option value="20">东莞银行</option>
			            </select>
			            <span id="span-card-type" class="unbind-card"></span>
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">持卡人</div>
					<div class="input">
						<input class="easyui-textbox" id="tbxCardOwner" name="cardOwner" data-options="required:true, prompt:'请输入持卡人的姓名'">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">验证</div>
					<div class="input">
						<input class="easyui-textbox" id="tbxCheckCardOwner" data-options="required:true, prompt:'请输入持卡人全名'">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">卡号</div>
					<div class="input">
						<input class="easyui-textbox" id="tbxCardNo" name="cardNo" precision="0" data-options="required:true, prompt:'请输入卡号', validType:['minLength[16]', 'maxLength[19]']">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">验证</div>
					<div class="input">
						<input class="easyui-textbox" id="tbxCheckCardNo" precision="0" data-options="required:true, prompt:'请输入完整卡号'">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">手机号</div>
					<div class="input">
						<input class="easyui-numberbox" id="tbxMobileNo" name="mobileNo" precision="0" data-options="required:true, prompt:'请输入银行预留手机号', validType:['maxLength[11]']">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">验证码</div>
					<div class="input">
						<input class="easyui-numberbox" id="tbxCheckCode" name="checkCode" precision="0" data-options="required:true, prompt:'请输入验证码', validType:['maxLength[6]']">
						<a href="javascript:void(0)" id="lnkCardGetCheckCode" class="easyui-linkbutton" onclick="getCheckCodeHandler();" style="width:100px;">获取验证码</a>
			        </div>
		        </div>
			</div>
			<div style="margin-left:65px;"><span id="span-card">绑定</span>银行卡需要短信确认，请按提示操作。</div>
		    <input type="hidden" id="input_cardId" name="id">
		    <input type="hidden" id="input_mobileNo" name="mobileNo">
	    </form>
		<div id="bindBtns">
			 <a href="javascript:void(0)" id="lnk-bind-card" class="easyui-linkbutton" onclick="bindCard();" style="width:60px;">确定</a>
			 <a href="javascript:void(0)" class="easyui-linkbutton" onclick="$('#win_bindcard').dialog('close')" style="width:60px;">取消</a>
		</div>
	</div>
	<%-- 提现 --%>
	<div id="win_withdrawal" class="easyui-dialog" data-options="closed:true,buttons:'#drawalBtns'" style="width:420px;padding:10px 15px 15px">
	    <form id="fom_withdrawal" method="post">
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">开户行</div>
					<div class="input">
						<span id="withdrawal-card-type">中国工商银行</span>
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">持卡人</div>
					<div class="input">
						<input class="easyui-textbox" id="withdrawalCardOwner" data-options="required:true, prompt:'请输入持卡人的姓名'" readonly="readonly">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">卡号</div>
					<div class="input">
						<input class="easyui-textbox" id="withdrawalCardNo" precision="0" data-options="required:true, prompt:'请输入卡号', validType:['minLength[16]', 'maxLength[19]']" readonly="readonly">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">可用金额</div>
					<div class="input">
						<span class="orange-color" id="withdrawalAvailableAmount">0</span>元
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">提现金额</div>
					<div class="input">
						<input class="easyui-numberbox" id="withdrawalAmount" name="tradeAmount" precision="2" data-options="required:true, onChange:withdrawalAmountChange">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">提现费用</div>
					<div class="input">
						<span class="orange-color">1.5</span>元
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">实扣金额</div>
					<div class="input">
						<span class="orange-color" id="withdrawalDeduction">0</span>元
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">预计到账</div>
					<div class="input">
						<span>1-2个工作日（双休日和法定节假日除外）</span>
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">手机号</div>
					<div class="input">
						<input class="easyui-numberbox" id="withdrawalMobileNo" precision="0" data-options="required:true, prompt:'请输入银行预留手机号', validType:['maxLength[11]']" readonly="readonly">
			        </div>
		        </div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">验证码</div>
					<div class="input">
						<input class="easyui-numberbox" id="withdrawalCheckCode" precision="0" name="checkCode" data-options="required:true, prompt:'请输入验证码', validType:['maxLength[6]']">
						<a href="javascript:void(0)" class="easyui-linkbutton" id="lnkWithdrawalCheckCode" onclick="getWithdrawalCheckCodeHandler();" style="width:100px;">获取验证码</a>
			        </div>
		        </div>
			</div>
	         <!-- <table>
	             <tr>
	                 <td>开户行</td>
	                 <td colspan="2">
	                    <span id="withdrawal-card-type">中国工商银行</span>
	                 </td>
	             </tr>
	             <tr>
	                 <td>持卡人</td>
	                 <td colspan="2">
	                     <input class="easyui-textbox" id="withdrawalCardOwner" data-options="required:true, prompt:'请输入持卡人的姓名'" style="width:60%;" readonly="readonly">
	                 </td>
	             </tr>
	             <tr>
	                 <td>卡号</td>
	                 <td colspan="2">
	                     <input class="easyui-numberbox" id="withdrawalCardNo" precision="0" data-options="required:true, prompt:'请输入卡号', validType:['minLength[16]', 'maxLength[19]']" style="width:60%;" readonly="readonly">
	                 </td>
	             </tr>
	             <tr>
	                 <td>可用金额</td>
	                 <td colspan="2">
	                     <span class="orange-color" id="withdrawalAvailableAmount">0</span>元
	                 </td>
	             </tr>
	             <tr>
	                 <td>提现金额</td>
	                 <td colspan="2">
	                     <input class="easyui-numberbox" id="withdrawalAmount" name="tradeAmount" precision="2" data-options="required:true, onChange:withdrawalAmountChange" style="width:60%;">
	                 </td>
	             </tr>
	             <tr>
	                 <td>提现费用</td>
	                 <td colspan="2">
	                     <span class="orange-color">1.5</span>元
	                 </td>
	             </tr>
	             <tr>
	                 <td>实扣金额</td>
	                 <td colspan="2">
	                     <span class="orange-color" id="withdrawalDeduction">0</span>元
	                 </td>
	             </tr>
	             <tr>
	                 <td>预计到账日期</td>
	                 <td colspan="2">
	                     <span>1-2个工作日（双休日和法定节假日除外）</span>
	                 </td>
	             </tr>
	             <tr>
	                 <td>手机号</td>
	                 <td colspan="2">
	                     <input class="easyui-numberbox" id="withdrawalMobileNo" precision="0" data-options="required:true, prompt:'请输入银行预留手机号', validType:['maxLength[11]']" style="width:60%;" readonly="readonly">
	                 </td>
	             </tr>
	             <tr>
	                 <td>验证码</td>
	                 <td>
	                     <input class="easyui-numberbox" id="withdrawalCheckCode" precision="0" name="checkCode" data-options="required:true, prompt:'请输入验证码', validType:['maxLength[6]']" style="width:85%;">
	                 </td>
	                 <td>
	                    <a href="javascript:void(0)" class="easyui-linkbutton" id="lnkWithdrawalCheckCode" onclick="getWithdrawalCheckCodeHandler();" style="width:100px;">获取验证码</a>
	                </td>
	             </tr>
	         </table> -->
	    </form>
        <div id="drawalBtns">
             <a href="javascript:void(0)" class="easyui-linkbutton" onclick="withdrawal();" style="width:60px;">确认</a>
             <a href="javascript:void(0)" class="easyui-linkbutton" onclick="$('#win_withdrawal').dialog('close')" style="width:60px;">取消</a>
        </div>
	</div>
</div>
</body>
</html>