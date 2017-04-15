<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>抽奖活动</title>
<%@ include file="/common.jsp"%>
<!-- <script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script> -->
<style type="text/css">
	.oper-btn {padding: 0 5px; margin: 0 8px; background-color: #eee; border-radius: 3px; color: #fff; cursor: pointer;}
	.oper-btn.default {background-color: #fff; color: #333; border: 1px solid #e5e5e5;}
	.oper-btn.default:hover {background-color: #f2f2f2;}
	.oper-btn.danger {background-color: #d9534f;}
	.oper-btn.danger:hover {background-color: #c9302c;}
	.oper-btn.primary {background-color: #428bca;}
	.oper-btn.primary:hover {background-color: #3071a9;}
	.oper-btn.warn {background-color: #f0ad4e;}
	.oper-btn.warn:hover {background-color: #ec971f;}
	.oper-btn.info {background-color: #5bc0de;}
	.oper-btn.info:hover {background-color: #31b0d5;}

	.datagrid-row-over {background-color: transparent;}
</style>
<script type="text/javascript">
	function actDownOrDel(id, type) {
		var text = type === 1 ? '删除' : '下线';
		confirmMsg('确定将该活动' + text +'吗？', function() {
			$.post('${pageContext.request.contextPath}/marketing/lottery/deleteByIdLottery.json', {lotteryId:id, type:type}, function(r) {
				queryData('activityList','activitySearch');
			})
		})
	}

	function changeActState(id, type) {
		var tipText = type == 2 ? '确定发布该活动吗？' : '确定取消该活动吗？';

		confirmMsg(tipText, function() {
			$.ajax({
				url: '${pageContext.request.contextPath}/marketing/lottery/updatepushMessageLottery.json',
				data: {lotteryId:id, type:type},
				success: function(r) {
					if (r.lottery.pushState) {
						errorMsg(r.lottery.pushState);
					}
					queryData('activityList', 'activitySearch');
				}
			})
		})
	}
	
	//formatter
	function actState(value) {
		switch(value) {
			case '0':
				return '未开始';
			case '1':
				return '已开始';
			case '2':
				return '已结束';
			case '5':
				return '预热中';
			default:
				return value;
		}
	}

	function actPublish(value) {
		switch(value) {
			case 0:
				return '未发布';
			case 1:
				return '已发布';
			default:
				return value;
		}
	}
	
	function actOperate(value, row) {
		var detailUrl = '${pageContext.request.contextPath}/marketing/lotteryDetail.jsp?id='+row.id,
			dataUrl = '${pageContext.request.contextPath}/marketing/lotteryStatistics.jsp?id='+row.id;
			deviceUrl = '${pageContext.request.contextPath}/marketing/lotteryDevices.jsp?id='+row.id+'&state='+row.state;

		var	publish = '<span class="oper-btn primary" onclick="changeActState('+row.id+', 2)">发布活动</span>',
			cancelPublish = '<span class="oper-btn warn" onclick="changeActState('+row.id+', 3)">取消活动</span>',
			device = '<span class="oper-btn default" onclick="parent.addTab(\''+row.lotteryName+'\', \''+deviceUrl+'\')">查看设备</span>',
			view = '<span class="oper-btn default" onclick="parent.addTab(\''+row.lotteryName+'\', \''+detailUrl+'\')">详情</span>',
			data = '<span class="oper-btn default" onclick="parent.addTab(\''+row.lotteryName+'\', \''+dataUrl+'\')">数据</span>',
			del = '<span class="oper-btn danger" onclick="actDownOrDel('+row.id+', 1)">删除</span>';

		var state = row.state
			isPublish = row.isPublish;
		var pubBtn = '', delBtn = '';

		if (isPublish == 0 && state == 0) {
			pubBtn = publish;
		} else if (isPublish == 1 && state != 2) {
			pubBtn = cancelPublish;
		} else {
			pubBtn = '<span style="display:inline-block;width:74px;"></span>';
		}
		if (isPublish == 0 || state == 2) {
			delBtn = del;
		} else {
			delBtn = '<span style="display:inline-block;width:50px;"></span>';
		}
		
		return pubBtn + device + view + data + delBtn;
	}

	$(function() {
		$('#activityList').datagrid({
			url: '${pageContext.request.contextPath}/marketing/lottery/findLotteryPage.json',
			onSelect: function(i, row) {
				$(this).datagrid('unselectRow', i);
			}
		})
	})
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="activitySearch" class="search-form">
			<div class="form-item">
				<div class="text">活动名称</div>
				<div class="input">
					<input name="lotteryName" class="easyui-textbox" data-options="prompt:'活动名称'" />
				</div>
			</div>
		</form>
		<div class="search-btn" onclick="queryData('activityList','activitySearch')">查询</div>
	</div>

	<!-- 活动列表 -->
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#activityListTools'" title="抽奖活动列表">
		<div id="activityListTools">
			<a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增抽奖活动'" onclick="parent.addTab('新建抽奖活动', '${pageContext.request.contextPath}/marketing/lotteryDetail.jsp')"></a>
		</div>
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<!-- 活动列表 -->
			<table id="activityList" class="easyui-datagrid" data-options="fit:true,fitColumns:true,idField:'id'">
				<thead>
					<tr>
						<th data-options="field:'lotteryName',align:'center',width:25">活动标题</th>
						<th data-options="field:'warmUpTime',align:'center',width:15">预热开始时间</th>
						<th data-options="field:'startTime',align:'center',width:15">活动开始时间</th>
						<th data-options="field:'endTime',align:'center',width:15">活动结束时间</th>
						<th data-options="field:'isPublish',align:'center',width:6,formatter:actPublish">是否发布</th>
						<th data-options="field:'state',align:'center',width:6,formatter:actState">活动状态</th>
						<th data-options="field:'operate',align:'center',width:30,formatter:actOperate">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</body>
</html>