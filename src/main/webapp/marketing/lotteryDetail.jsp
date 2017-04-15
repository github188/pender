<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>抽奖活动编辑</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-cellediting.js"></script>
<style type="text/css">
	html, body {margin: 0; height: 100%; font-size: 14px;}
	.edit {display: none; float: right; margin-right: 30px; color: #0075bf; font-weight: normal; cursor: pointer;}
	.edit:hover {text-decoration: underline;}

	.m-panel {border-bottom:5px solid #f5f5f5;}
	.m-panel:last-child {border-bottom: none;}
	.m-panel .head {line-height: 37px; border-bottom: 1px solid #e5e5e5; text-indent: 15px; font-weight: bold; color: #1389f9;}
	.m-panel .body {padding: 15px;}
	.message-row {padding: 10px 0;}
	.message-row label {color: #666;}

	.act-pro-item {margin: 0 -15px 15px; padding: 0 15px 15px; border-bottom: 1px solid #e5e5e5;}
	.act-pro-item:last-child {margin-bottom: 0; padding-bottom: 0; border-bottom: none;}
	.act-pro-item .mes {margin-bottom: 8px;}
	.act-pro-item .mes .item {float: left; margin-right: 50px;}
	.act-pro-item .mes .item label {color: #666;}
	.act-pro-item .img {margin-bottom: 8px;}
	.act-pro-item .img .item {float: left; margin-right: 10px; width: 150px; color: #666;}
	.act-pro-item .img .item img {width: 100%; height: 150px;}

	.img-uploader {display: inline-block; padding: 10px; border: 1px solid #e5e5e5; margin: 0 15px 10px 0;}
	.chances-label {cursor: pointer;}
	.chances-label:last-child {margin-left: 30px;}
	.chances-label input {position: relative; top: 1px;}
</style>
<script type="text/javascript">
	var canEdit = false;//是否可编辑
	var uploadedImgNum,//已上传图片数
		imgNum;//需要上传的图片总数
	var curDev,//当前选择的设备
		selectedDevs = [];//参与设备
	var pointTypes = [];//店铺类型

	//获取当天时间范围
	function getDateRange() {
    	var d = new Date;
    	var year = d.getFullYear();
    	var month = d.getMonth()+1;
    	var date = d.getDate();
		return {
			startTime: year + '-' + (month<10?'0'+month:month) + '-' + (date<10?'0'+date:date) + ' 00:00:00',
			endTime: year + '-' + (month<10?'0'+month:month) + '-' + (date<10?'0'+date:date) + ' 23:59:59'
		}
	}

	//初始化图片上传
	function initImageUpload(m) {
		m.contentWindow.initImageUploader(function() {
			uploadedImgNum++;
			if (uploadedImgNum == imgNum) doSaveActPro();		
		}, _token, 1);
	}

	//载入图片
	function showImage(iframe, image) {
		var fileIds = [], fileNames = [], realPaths = [];
		var vals = image.split(',');
		fileIds.push(vals[0]);
		fileNames.push(vals[1]);
		realPaths.push(vals[3]);
		iframe.contentWindow.loadUploadImage(fileIds, fileNames, realPaths);
	}

	//显示隐藏概率设置列
	function changeProCloumn(grid, type) {
		if (type == 'show') {
			grid.datagrid('showColumn', 'stock');
			grid.datagrid('showColumn', 'num');
		} else if (type == 'hide') {
			grid.datagrid('hideColumn', 'stock');
			grid.datagrid('hideColumn', 'num');
		}
	}

	//退出编辑状态
	function endEdit() {
		//未退出编辑状态时，编辑器内的值没有同步，因此先结束编辑状态
		var curCell = $('#fomPro .proList').datagrid('cell');
		if (curCell && curCell.field == 'num') {
			$('#fomPro .proList').datagrid('editCell', {index:curCell.index, field:'stock'});
		}
	}

	//生成活动基本设置
	function createActMes(lottery) {
		$('#act-panel').data('cache', lottery);
		var lottery = $.extend({}, lottery);
		lottery.isProbabil = lottery.isProbabil ? '可修改概率' : '不可修改概率(由系统智能算法实现出货)';
		var html = template("act-base-message", lottery);
		$('#act-base-message-ctn').html(html);

		var rows = [];
		$.each(lottery.deviceList, function(index, item) {
			if (item.sort === '1') rows.push(item);
		});
		$('#act-base-message-ctn .devList').datagrid({
			data: {total:rows.length, rows:rows}
		});
	}

	//更新活动基本设置
	function updateActMes(id) {
		$.ajax({
			url: '${pageContext.request.contextPath}/marketing/lottery/findOneLottery.json',
			data: {lotteryId: id},
			success: function(r) {
				var oldDevIds = [], newDevIds = [];
				var cache = $('#act-panel').data('cache');
				var oldDevs = cache ? cache.deviceList : [];
				var newDevs = r.lottery.deviceList;
				$.each(oldDevs, function(i, dev) {
					if (dev.sort == '1') oldDevIds.push(dev.id);
				});
				$.each(newDevs, function(i, dev) {
					if (dev.sort == '1') newDevIds.push(dev.id);
				});
				createActMes(r.lottery);
				if (oldDevIds.sort().toString() != newDevIds.sort().toString()) {//对比所选设备是否有变化，若有变化则更新所有活动内容
					updateActPro();
				}
			}
		})
	}

	//编辑活动基本设置
	function editAct() {
		openDialog('winAct', '设置活动基本信息');
		var lottery = $('#act-panel').data('cache');
		if (lottery) {
			$('#fomAct').form('load', lottery);
			$('#fomAct input[name="isProbabil"]').prop('disabled', true);
			$('#fomAct .devList').datagrid('loadData', {lottery:lottery});
		} else {
			$('#fomAct input[name="isProbabil"]').prop('disabled', false);
			$('#fomAct input[name="isProbabil"]').eq(0).prop('checked', true);
			var today = getDateRange();
			$('#warmUpTime, #startTime').datetimebox('setValue', today.startTime);
			$('#endTime').datetimebox('setValue', today.endTime);
			$('#fomAct .devList').datagrid({
				url: '${pageContext.request.contextPath}/marketing/lottery/findByOrgIdDevice.json'
			})
		}
	}

	//保存活动基本设置
	function saveAct() {
		if (!$('#fomAct').form('enableValidation').form('validate')) return;

		var lottery = $('#fomAct').getValues();
		lottery.isProbabil = $('#fomAct input[name="isProbabil"]:checked').val();

		var lotteryDevNoProductList = [];
		var selectDevs = $('#fomAct .devList').datagrid('getSelections');
		$.each(selectDevs, function(index, item) {
			lotteryDevNoProductList.push({factoryDevNo: item.factoryDevNo});
		});
		lottery.lotteryDevNoProductList = lotteryDevNoProductList;

		$.ajax({
			url: '${pageContext.request.contextPath}/marketing/lottery/saveLottery.json',
			dataType: 'json',
			contentType : 'application/json;charset=utf-8',
			data: JSON.stringify(lottery),
			success: function(r) {
				closeDialog('winAct');
				updateActMes(r.lottery.id);
			}
        });
	}

	//生成活动内容
	function createActPros(pros) {
		$.each(pros, function(index, item) {
			//生成DOM
			if (item.images) {
				var imgArr = [];
				$.each(item.images.split(';'), function(i, img) {
					var model = +(img.split(',')[2]);
					imgArr[model - 32] = img;
					switch (model) {
						case 32:
							item.img_m = getFileUrl(img.split(',')[3]);
							break;
						case 33:
							item.img_d = getFileUrl(img.split(',')[3]);
							break;
						case 34:
							item.img_s = getFileUrl(img.split(',')[3]);
							break;
					}
				})

				item.images = imgArr.join(';');				
			}
			var proHtml = template("act-pro-item", item);
			var $proItem = $(proHtml);
			$proItem.data('cache', item);

			if (!$('#act-pro-id').val()) {//添加
				$('#act-pro-list').append($proItem);
			} else {//编辑
				$('#act-pro-list .act-pro-item').each(function() {
					if ($(this).data('cache').id == item.id) {
						$(this).replaceWith($proItem);
					}
				})
			}

			//初始化datagrid
			var $devGrid = $proItem.find('.devList');
			var $proGrid = $proItem.find('.proList');
			$proGrid.datagrid();
			$devGrid.datagrid({
				data: {rows:item.deviceList},
				onLoadSuccess: function(data) {
					if (data.rows.length > 0) {
						$(this).datagrid('selectRow', 0);
					}
				},
				onSelect: function(index, row) {
					$proGrid.datagrid({
						url: '${pageContext.request.contextPath}/marketing/lottery/findByDevNoProduct.json',
						queryParams: {lotteryId:$('#act-panel').data('cache').id, lotteryProductId:item.id, factoryDevNo:row.factoryDevNo},
						loadFilter: function(data) {
							var rows = $.grep(data.rows, function(n, i) {
								return n.sort == 1;
							}); 
							return {rows:rows, total:rows.length};
						}
					})
				}
			});

			//根据状态显示按钮、隐藏概率设置列
			var lottery = $('#act-panel').data('cache');
			if (canEdit) {
				$proItem.find('.edit').show();
			}
			if (lottery.isProbabil == '1') {
				changeProCloumn($proGrid, 'show');
			} else {
				changeProCloumn($proGrid, 'hide');
			}
		})
	}

	//更新活动内容
	function updateActPro(id) {
		var params = {lotteryId:$('#act-panel').data('cache').id};
		if (id) {
			params.lotteryProductId = id;
		} else {
			//没有id传入为更新整个活动内容列表，先清空列表
			$('#act-pro-list').empty();
		}
		$.ajax({
			url: '${pageContext.request.contextPath}/marketing/lottery/findLotteryProduct.json',
			data: params,
			success: function(r) {
				createActPros(r.lotteryProductList);
			}
		})
	}

	//添加、编辑活动内容
	function editActPro(m) {
		var lottery = $('#act-panel').data('cache');
		var $frame = $('#fomPro iframe');
		$frame.each(function(i, item) {
			item.contentWindow.closeImageUploader();
		})
		if (!lottery) {
			confirmMsg('尚未设置活动基本信息，请点“确定”进行设置', editAct);
			return;
		}
		if (!m) {//新增
			if ($('#act-pro-list .act-pro-item').length === 3) {
				infoMsg('已有3个活动内容，无法再添加');
				return;
			}
			openDialog('winPro', '添加活动内容');
		} else {//编辑
			openDialog('winPro', '编辑活动内容');
			var pro = $(m).parents('.act-pro-item').data('cache');
			$('#fomPro').form('load', pro);

			if (lottery.state == 5 && lottery.isProbabil == 1) {
				$('#fomPro .easyui-textbox').textbox({readonly: true}).siblings('span').children('.textbox-text').css('background-color', '#f5f5f5');
				$('#fomPro .easyui-numberbox').numberbox({readonly: true}).siblings('span').children('.textbox-text').css('background-color', '#f5f5f5');
				$frame.each(function(i) {
					var imgUrlArr = ['img_m', 'img_d', 'img_s'];
					$(this).hide().siblings('img').attr('src', pro[imgUrlArr[i]] ? pro[imgUrlArr[i]] : '').show();
				});
			} else {
				if (pro.images) {
					var imgArr = pro.images.split(';');
					if(imgArr[0]) showImage($frame[0], imgArr[0]);
					if(imgArr[1]) showImage($frame[1], imgArr[1]);
					if(imgArr[2]) showImage($frame[2], imgArr[2]);
				}
			}
		}

		$('#act-pro-key').val(+(new Date));

		var $proGrid = $('#fomPro .proList');

		if (lottery.isProbabil == 1) {
			$proGrid.datagrid('disableCellEditing').datagrid('enableCellEditing');
			$proGrid.datagrid('hideColumn', 'ck');
			changeProCloumn($proGrid, 'show');
		} else {
			$proGrid.datagrid('disableCellEditing');
			$proGrid.datagrid('showColumn', 'ck');
			changeProCloumn($proGrid, 'hide');
		}

		selectedDevs = [];
		$.each(lottery.deviceList, function(i, item) {
			if(item.sort == '1') {
				selectedDevs.push($.extend({}, item));
			}
		})

		$('#fomPro .devList').datagrid('loadData', {total:selectedDevs.length, rows:selectedDevs});
	}

	//上传活动内容图片
	function saveActPro() {
		if (!$('#fomPro').form('enableValidation').form('validate')) return;

		imgNum = 0;
		uploadedImgNum = 0;
		$actPros = $('#fomPro iframe');

		$actPros.each(function() {
			imgNum += this.contentWindow.queueImages.length;
		})

		if (!imgNum) {
			doSaveActPro();
			return;
		}

		$actPros.each(function(index) {
			if (this.contentWindow.queueImages.length != 0) {
				this.contentWindow.uploadImages({key:$('#act-pro-key').val() ,module:index+32});
			}
		})
	}

	//保存活动内容信息
	function doSaveActPro() {
		endEdit();

		var isProbabil = $('#act-panel').data('cache').isProbabil;//概率类型

		var actPro = $('#fomPro').getValues();
		actPro.lotteryId = $('#act-panel').data('cache').id;
		delete actPro.ck;

		var deviceList = [];
		$.each(selectedDevs, function(i, dev) {
			if (!dev.productList || dev.productList.length == 0) return;
			var productList = [];
			$.each(dev.productList, function(i, pro) {
				if (isProbabil == 0 && pro.sort == 1) {
					productList.push({id:pro.id});
				} else if (isProbabil == 1 && (!!pro.num)) {
					productList.push({id:pro.id, num:pro.num});
				}
			});
			deviceList.push({factoryDevNo:dev.factoryDevNo, productList:productList});
		})
		actPro.deviceList = deviceList;

		var fileIds = [];
		$('#fomPro iframe').each(function() {
			fileIds = fileIds.concat(this.contentWindow.delImageIds);
		})
		actPro.fileIds = fileIds;

		$.ajax({
			url: '${pageContext.request.contextPath}/marketing/lottery/saveLotteryDevNoProduct.json',
			dataType: 'json',
			contentType : 'application/json;charset=utf-8',
			data: JSON.stringify(actPro),
			success: function(r) {
				updateActPro(r.lotteryProduct.id);
				closeDialog('winPro');
			}
        });
	}

	//删除活动内容
	function delActPro(m, id) {
		confirmMsg('确定删除该活动内容吗？', function() {
			$.ajax({
				url: '${pageContext.request.contextPath}/marketing/lottery/deleteLotteryProduct.json',
				data: {lotteryProductId: id},
				success: function() {
					$(m).parents('.act-pro-item').remove();
				}
			})
		})
	}

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
		if (!!actId) {
			$.ajax({
				url: '${pageContext.request.contextPath}/marketing/lottery/findByOrgIdDevice.json',
				data: {lotteryId: actId},
				async: false,
				success: function(r) {
					//如果活动是已开始或已下线或已结束状态则删除编辑和添加按钮
					var state = r.lottery.state;
					var proList = r.lottery.lotteryProductList;
					if (state == 0 && (proList.length == 0 || (proList[0] && proList[0].isSucessState == 3))) {
						$('#act-panel .edit, #pro-panel .edit').show();
						canEdit = true;
					}
					createActMes(r.lottery);
					createActPros(r.lottery.lotteryProductList);
				}
			})
		} else {
			$('#act-panel .edit, #pro-panel .edit').show();
			canEdit = true;
			editAct();
		}

		$('#fomAct .devList').datagrid({
			onLoadSuccess: function(data) {
				var $this = $(this);
				$.each(data.rows, function(index, item) {
					if (item.sort == '1') {
						$this.datagrid('checkRow', index);
					}
				})
			},
			loadFilter: function(r) {
				var devList = r.lottery.deviceList;
				return {total:devList.length, rows:devList};
			}
		});

		$('#fomPro .devList').datagrid({
			onLoadSuccess: function(r) {
				if (r.rows.length != 0) {
					$(this).datagrid('selectRow', 0);
				}
			},
			onSelect: function(index, row) {
				endEdit();
				curDev = row;
				if (!!curDev.productList && curDev.productList.length != 0) {
					$('#fomPro .proList').datagrid('loadData', {rows:curDev.productList});
				} else {
					var params = {factoryDevNo: row.factoryDevNo};
					var lotteryId = $('#act-panel').data('cache').id;
					params.lotteryId = lotteryId;
					var lotteryProductId = $('#act-pro-id').val();
					params.lotteryProductId = lotteryProductId;
					$('#fomPro .proList').datagrid({
						url: '${pageContext.request.contextPath}/marketing/lottery/findByDevNoProduct.json',
						queryParams: params
					})
				}
			}
		});

		$('#fomPro .proList').datagrid({
			onLoadSuccess: function(r) {
				var $this = $(this);
				curDev.productList = r.rows;
				$.each(r.rows, function(i, item) {
					if (item.sort == 1) {
						$this.datagrid('checkRow', i);
					} else {
						$this.datagrid('uncheckRow', i);
					}
				})
			},
			onCheck: function(i) {
				curDev.productList[i].sort = 1;
			},
			onUncheck: function(i) {
				curDev.productList[i].sort = 0;
			},
			onCheckAll: function() {
				$.each(curDev.productList, function(i, item) {
					item.sort = 1;
				})
			},
			onUncheckAll: function() {
				if (selectedDevs.length == 0) return;
				$.each(curDev.productList, function(i, item) {
					item.sort = 0;
				})
			}
		});
	})
</script>
</head>
<body>
<div style="height:100%; overflow:auto;">
	<div id="act-panel" class="m-panel">
		<div class="head">
			<span>基本信息</span>
			<span class="edit" onclick="editAct()">编辑</span>
		</div>
		<div id="act-base-message-ctn" class="body">
			
		</div>
	</div>
	<div id="pro-panel" class="m-panel">
		<div class="head">
			<span>活动内容</span><span style="font-weight:normal;font-size:12px;">（最多三个）</span>
			<span class="edit" onclick="editActPro()">添加</span>
		</div>
		<div id="act-pro-list" class="body">
			
		</div>
	</div>
</div>
<div id="winAct" class="easyui-dialog" data-options="closed:true,buttons:'#actBtns'" style="width:1000px;height:600px;padding:10px 20px;">
	<form id="fomAct">
		<input type="hidden" name="id">
		<div class="form-row">
			<div class="form-cloumn form-item">
				<div class="text">活动标题</div>
				<input name="lotteryName" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['length[1,64]']">
			</div>
		</div>
		<div class="form-row">
			<div class="form-cloumn form-item">
				<div class="text">预热时间</div>
				<input id="warmUpTime" name="warmUpTime" class="easyui-datetimebox" data-options="required:true,editable:false,prompt:'预热开始日期'">
			</div>
		</div>
		<div class="form-row">
			<div class="form-cloumn form-item" style="width:100%;">
				<div class="text">活动时间</div>
				<input id="startTime" name="startTime" class="easyui-datetimebox" data-options="required:true,editable:false,prompt:'活动开始时间'"> - <input id="endTime" name="endTime" class="easyui-datetimebox" data-options="required:true,editable:false,prompt:'活动结束时间'">
			</div>
		</div>
		<div class="form-row">
			<div class="form-cloumn form-item" style="width:100%;">
				<div class="text">概率设置</div>
				<label class="chances-label">
					<input type="radio" name="isProbabil" value="0">
					不可修改概率（推荐，由系统智能算法实现出货）
				</label>
				<label class="chances-label">
					<input type="radio" name="isProbabil" value="1">
					可修改概率
				</label>
			</div>
		</div>
		<div class="form-row">
			<div class="form-cloumn form-item" style="width:100%;">
				<div class="text">选择参加活动的设备</div>
				<table class="devList" data-options="striped:true,fitColumns:true,pagination:false,idField:'id'" style="height:500px;">
					<thead>
						<tr>
							<th data-options="field:'',checkbox:true"></th>
							<th data-options="field:'factoryDevNo',align:'center',width:10">设备组号</th>
							<th data-options="field:'typeStr',align:'center',width:45">设备类型</th>
							<th data-options="field:'pointAddress',align:'center',width:35">所属店铺地址</th>
							<th data-options="field:'pointType',align:'center',width:10,formatter:formatPointType">所属店铺类型</th>
						</tr>
					</thead>
				</table>
			</div>
		</div>
	</form>
	<div id="actBtns">
		<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveAct()">保存</a>
        <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winAct')">取消</a>
	</div>
</div>
<div id="winPro" class="easyui-dialog" data-options="closed:true,buttons:'#proBtns'" style="width:96%;height:700px;padding:10px 20px 20px;">
	<form id="fomPro">
		<input id="act-pro-id" type="hidden" name="id">
		<input id="act-pro-key" type="hidden" name="key">
		<div class="form-row">
			<div class="form-cloumn form-item">
				<div class="text">名称</div>
				<input name="productName" class="easyui-textbox" data-options="required:true,prompt:'必填项'">
			</div>
			<div class="form-cloumn form-item">
				<div class="text">零售价</div>
				<input name="productPrice" class="easyui-numberbox" data-options="required:true,prompt:'必填项',precision:2">
			</div>
			<div class="form-cloumn form-item">
				<div class="text">规格</div>
				<input name="productNorms" class="easyui-textbox">
			</div>
		</div>
		<div class="img-uploader">
			<div class="dsec">首页图片</div>
			<iframe width="162px" height="180px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initImageUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
			<img src="" style="display:none;width:150px;height:150px;">
		</div>
		<div class="img-uploader">
			<div class="dsec">详情页图片</div>
			<iframe width="162px" height="180px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initImageUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
			<img src="" style="display:none;width:150px;height:150px;">
		</div>
		<div class="img-uploader">
			<div class="dsec">图文描述</div>
			<iframe width="162px" height="180px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initImageUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
			<img src="" style="display:none;width:150px;height:150px;">
		</div>
		<div class="clearfix">
			<div style="float:left;width:50%;padding-right:10px;box-sizing:border-box;">
				<table class="devList" data-options="striped:true,fitColumns:true,singleSelect:true,pagination:false,idField:'id'" style="width:100%;height:600px;">
					<thead>
						<tr>
							<th data-options="field:'factoryDevNo',align:'center',width:10">设备组号</th>
							<th data-options="field:'typeStr',align:'center',width:45">设备类型</th>
							<th data-options="field:'pointAddress',align:'center',width:35">所属店铺地址</th>
							<th data-options="field:'pointType',align:'center',width:15,formatter:formatPointType">所属店铺类型</th>
						</tr>
					</thead>
				</table>
			</div>
			<div style="float:left;width:50%;">
				<table class="proList" data-options="fitColumns:true,pagination:false,idField:'id'" style="width:100%;height:600px;">
					<thead>
						<tr>
							<th data-options="field:'ck',checkbox:true"></th>
							<th data-options="field:'images',width:100,align:'center',formatter:formatProImgUrl">商品图片</th>
							<th data-options="field:'skuName',width:180,align:'center'">商品名称</th>
							<th data-options="field:'priceOnLine',width:50,align:'center'">零售价</th>
							<th data-options="field:'stock',width:50,align:'center'">库存</th>
							<th data-options="field:'num',width:50,align:'center',editor:{type:'numberbox',options:{precision:0,min:1}}">活动数量</th>
						</tr>
					</thead>
				</table>
			</div>
		</div>
	</form>
	<div id="proBtns">
		<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveActPro()">保存</a>
        <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winPro')">取消</a>
	</div>
</div>
<script id="act-base-message" type="text/html">
	<div class="message-row">
		<label>活动标题：</label>
		<span>{{lotteryName}}</span>
	</div>
	<div class="message-row">
		<label>预热时间：</label>
		<span>{{warmUpTime}}</span>
	</div>
	<div class="message-row">
		<label>活动时间：</label>
		<span>{{startTime}} ~ {{endTime}}</span>
	</div>
	<div class="message-row">
		<label>概率设置：</label>
		<span>{{isProbabil}}</span>
	</div>
	<div class="message-row">
		<label style="float:left;">参与设备：</label>
		<span style="display:block;margin-left:70px;">
			<table class="easyui-datagrid devList" data-options="striped:true,fitColumns:true,pagination:false,idField:'id'" style="width:900px;height:250px;">
				<thead>
					<tr>
						<th data-options="field:'factoryDevNo',align:'center',width:10">设备组号</th>
						<th data-options="field:'typeStr',align:'center',width:45">设备类型</th>
						<th data-options="field:'pointAddress',align:'center',width:35">所属店铺地址</th>
						<th data-options="field:'pointType',align:'center',width:10,formatter:formatPointType">所属店铺类型</th>
					</tr>
				</thead>
			</table>
		</span>
	</div>
</script>
<script id="act-pro-item" type="text/html">
	<div class="act-pro-item">
		<div class="mes clearfix">
			<div class="item"><label>名称：</label><span>{{productName}}</span></div>
			<div class="item"><label>零售价：</label><span>{{productPrice}}</span></div>
			<div class="item"><label>规格：</label><span>{{productNorms}}</span></div>
			<span class="edit" style="margin-right:15px;" onclick="delActPro(this, {{id}})">删除</span>
			<span class="edit" style="margin-right:15px;" onclick="editActPro(this)">编辑</span>
		</div>
		<div class="img clearfix">
			<div class="item"><span>首页图片</span><img src="{{img_m}}"></div>
			<div class="item"><span>详情页图片</span><img src="{{img_d}}"></div>
			<div class="item"><span>图文描述</span><img src="{{img_s}}"></div>
		</div>
		<div class="clearfix">
			<div style="float:left;width:50%;padding-right:10.5px;box-sizing:border-box;">
				<table class="easyui-datagrid devList" data-options="striped:true,fitColumns:true,singleSelect:true,pagination:false,idField:'id'" style="height:600px;">
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
			<div style="float:left;width:50%;padding-right:0.5px;box-sizing:border-box;">
				<table class="easyui-datagrid proList" data-options="singleSelect:true,fitColumns:true,pagination:false,idField:'id'" style="height:600px;">
					<thead>
						<tr>
							<th data-options="field:'images',width:100,align:'center',formatter:formatProImgUrl">商品图片</th>
							<th data-options="field:'skuName',width:180,align:'center'">商品名称</th>
							<th data-options="field:'priceOnLine',width:50,align:'center'">零售价</th>
							<th data-options="field:'stock',width:50,align:'center'">库存</th>
							<th data-options="field:'num',width:50,align:'center'">活动数量</th>
						</tr>
					</thead>
				</table>
			</div>
		</div>
	</div>
</script>
</body>
</html>