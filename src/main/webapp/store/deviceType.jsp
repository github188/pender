<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>设备类型</title>
<%@ include file="/common.jsp"%>
<style type="text/css">
	.device-nav span {display: block; float: left; padding: 3px 10px; border: 1px solid #009cd3; color: #009cd3; font-size: 14px; border-radius: 4px; margin: 5px; cursor: pointer;}
	.device-nav span:hover, .device-nav span.active {background-color: #009cd3; color: #fff;}
</style>
</head>
<body class="easyui-layout" >
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form class="search-form">
			<div class="form-item">
				<div class="text">商品类型</div>
				<div class="input">
					<select id="q_apply_type" class="easyui-combobox" name="type" data-options="panelHeight:'auto'">
					    <option value="">请选择</option>
					    <option value="1">易触</option>
					    <option value="2">中控</option>
					</select>
				</div>
			</div>
		</form>
		<div class="search-btn" onclick="">查询</div>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head'" title="设备信息">
		<div style="padding:10px;">
			<div class="device-nav clearfix">
				<span class="active">智能饮料机</span>
				<span>小型智能饮料机</span>
				<span>64门格子柜</span>
				<span>40门格子柜</span>
				<span>综合机辅机</span>
			</div>
			<div class="device-detail">
				<div class="view" style="float:left">
					<span>设备外观</span>
					<img style="display:block" src="${pageContext.request.contextPath}/images/u5218.png">
				</div>
				<div class="prop" style="float:left">
					<span>货道布局</span>
					<img style="display:block; width:600px;" src="${pageContext.request.contextPath}/images/u5220.png">
				</div>
			</div>
		</div>
	</div>
</body>
</html>