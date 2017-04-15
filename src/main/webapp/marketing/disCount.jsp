<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>限时打折</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<style type="text/css">
	.oper-btn {padding: 0 5px; margin: 0 8px; background-color: #eee; border-radius: 3px; color: #fff; cursor: pointer;}
	.oper-btn.danger {background-color: #d9534f;}
	.oper-btn.danger:hover {background-color: #c9302c;}
	.oper-btn.primary {background-color: #428bca;}
	.oper-btn.primary:hover {background-color: #3071a9;}
	.oper-btn.warn {background-color: #f0ad4e;}
	.oper-btn.warn:hover {background-color: #ec971f;}
	.oper-btn.info {background-color: #5bc0de;}
	.oper-btn.info:hover {background-color: #31b0d5;}
	.product-ctn {padding: 10px;}
	.product-item {width: 12.5%; float: left; cursor: pointer;}
	.product-item .inner {position: relative; margin: 5px; padding: 5px; border: 2px solid #ccc;}
	.product-item.active .inner {border-color: rgb(255, 168, 168);}
	.product-item .inner img {display: block; width: 100%; height: 113px;}
	.product-item .inner span {display: block; position: absolute; bottom: 0; left: 0; box-sizing: border-box; width: 100%; padding: 5px; color: #fff; background-color: rgba(204, 204, 204, .7);}
	.product-item.active .inner span {background-color: rgba(255, 168, 168, .8);}
	.del-timer {display: none; float:right; line-height:28px; font-size: 13px; cursor:pointer; padding: 0 5px; border: 1px solid #e5e5e5; border-radius: 4px;}
	.del-timer:hover {background-color: #eee;}
	.add-timer {color:#0f92f3; cursor:pointer; font-weight:normal;}
	.add-timer:hover {color: #0572c3;} 
	.activity-view .row {margin: 15px 0; font-size: 15px;}
	.activity-view .row label {float: left; width: 75px; font-weight: bold; text-align: right; color: #888;}
	.activity-view .row span {display: block; margin-left: 75px;}
	.add-pro {display: inline-block; font-size: 15px; color: #0075bf; line-height: 1; margin-top: 8px; margin-left: 10px; cursor: pointer;}
	.add-pro:hover {color: #055080;}
	#per-timers .numberbox {margin-left: 10px;}
	.act-data-tb {width: 100%; text-align: center; line-height: 38px; font-size: 15px; border-collapse: collapse;}
	.act-data-tb td {width: 10%; border: 1px solid #e5e5e5;}
	.act-data-tb tr:last-child td {font-weight: bold; font-size: 16px;}
	.act-data-title {font-size: 18px; margin-bottom: 10px; color: #5bc0de;}
</style>
<script type="text/javascript">
	var weekDate = [{id:1, text:'每周一'},{id:2, text:'每周二'},{id:3, text:'每周三'},{id:4, text:'每周四'},{id:5, text:'每周五'},{id:6, text:'每周六'},{id:7, text:'每周日'}];
	var pointTypes = [];//保存店铺类型
	var productsToStores = [];//保存商品列表以及关联店铺列表
	var activeProIndex;//当前选中商品的index

	$(function(){
		$("#activityList").datagrid({
			url: '${pageContext.request.contextPath}/marketing/discount/findDisCount.json',
			onClickCell: function(index, field) {
				if (field === 'operate') {
					var self = this;
					setTimeout(function(){
						$(self).datagrid('unselectRow', index);
					},0)	
				}
			}
		});

		//初始化周选择器
		$('#cycle-select').combotree('loadData', weekDate);

		//获取店铺类型
		var cache = getAllSysTypes();
		for (var i = 0; i < cache.length; i++) {
			if (cache[i].type == 'POINT_PLACE_TYPE') {
				pointTypes.push(cache[i]);
			}
		}

		//增加、删除时段
		$('#per-timers').on('click', '.add-timer', addTimer);

		$('#per-timers').on('click', '.del-timer', function() {
			$(this).parent().remove();
			var $dels = $('#per-timers .del-timer');
			if ($dels.length === 1) {
				$dels.hide();
			}
		})

		$('#product-ctn').on('click', '.inner', function() {
			$(this).parent().toggleClass('active');
		})
	});

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

	//添加时段
	function addTimer(times) {
		var $timer = $('<div class="timer" style="margin-bottom:5px;"><input class="easyui-timespinner" data-options="required:true,prompt:\'开始时间\'" style="width:120px"> - <input class="easyui-timespinner" data-options="required:true,prompt:\'结束时间\'" style="width:120px"><input class="easyui-numberbox" data-options="required:true,min:0.1,max:10.0,precision:1" style="width:50px;"> 折<span class="del-timer">删除<span></div>');
		$('#per-timers').append($timer);
		$numInput = $timer.find('input.easyui-numberbox');
		$timeInputs = $timer.find('input.easyui-timespinner');
		$numInput.numberbox();
		$timeInputs.timespinner();
		if (times) {
			$numInput.numberbox('setValue', times.discountValue);
			$timeInputs.eq(0).timespinner('setValue', times.startTime);
			$timeInputs.eq(1).timespinner('setValue', times.endTime);
		}
		if ($('#per-timers .del-timer').length > 1) {
			$('#per-timers .del-timer').show();
		}
	}

	//更改打折方式
	function transDiscountWay(record) {
		if (record.value === '1') {
			$('#alldaylong').show();
			$('#periodtime').hide();
			$('#per-timers .timer').remove();
			$('#alldaydiscount').numberbox({disabled: false});
		} else {
			$('#alldaylong').hide();
			$('#periodtime').show();
			addTimer();
			$('#alldaydiscount').numberbox({disabled: true});
		}
	}

	//新增、编辑活动
	function openActivityWin(id) {
		if (typeof id === 'undefined') {
			openDialog('winDiscount', '新增打折活动');
			$('#cycle-select').combotree('setValues', [1,2,3,4,5,6,7]);
			$('#discountWay').combobox('select', '1');
			var today = getDateRange();
			$('#discountStartTime').datetimebox('setValue', today.startTime);
			$('#discountEndTime').datetimebox('setValue', today.endTime);
		} else {
			openDialog('winDiscount', '修改打折活动');
			var rows = $('#activityList').datagrid('getRows');
			var index = $('#activityList').datagrid('getRowIndex', id);
			var row = rows[index];
			$('#discountType').combobox({readonly:true});
			$('#fomDiscount').form('load', row);
			if (row.discountWay === '1') {
				transDiscountWay({value:'1'});
				$('#alldaydiscount').numberbox('setValue', row.discountPeriodList[0].discountValue);
			} else {
				var periodList = row.discountPeriodList;
				for (var i = 0; i < periodList.length; i++) {
					transDiscountWay({value:'2'});
					$('#per-timers .timer').eq(i).find('.easyui-timespinner').eq(0).timespinner('setValue', periodList[i].discount_start);
					$('#per-timers .timer').eq(i).find('.easyui-timespinner').eq(1).timespinner('setValue', periodList[i].discount_end);
					$('#per-timers .timer').eq(i).find('.easyui-numberbox').numberbox('setValue', periodList[i].discountValue);
				}
			}
		}
	}

	//保存活动基本信息
	function saveActivity() {
		if (!$('#fomDiscount').form('enableValidation').form('validate')) return;
		var row = $('#fomDiscount').getValues();
		if (row.startTime > row.endTime) {
			infoMsg('活动开始时间不应大于结束时间');
			return;
		}
		row.cycle = $('#cycle-select').combotree('getValues').join(',');
		row.discountPeriodList = [];
		if (row.discountWay === '1') {
			row.discountPeriodList.push({discountValue:$('#alldaydiscount').textbox('getValue')});
		} else {
			var $timers = $('#per-timers .timer');
			for (var i = 0; i < $timers.length; i++) {
				var startTime = $timers.eq(i).find('.easyui-timespinner').eq(0).timespinner('getValue') + ':00';
				var endTime = $timers.eq(i).find('.easyui-timespinner').eq(1).timespinner('getValue') + ':00';
				var discountValue = $timers.eq(i).find('.easyui-numberbox').numberbox('getValue');
				row.discountPeriodList.push({discount_start: startTime, discount_end: endTime, discountValue: discountValue});
				if (startTime > endTime) {
					infoMsg('第'+(i+1)+'个打折时段有误，开始时间大于结束时间');
					return;
				}
			}
			row.discountPeriodList.sort(function(a, b) {
				return a.discount_start > b.discount_start;
			})
			for (var i = 0; i < row.discountPeriodList.length-1; i++) {
				if (row.discountPeriodList[i+1].discount_start < row.discountPeriodList[i].discount_end) {
					infoMsg('打折时段有时间交叠，请修正');
					return;
				}
			}
		}
		$.ajax({
			url: '${pageContext.request.contextPath}/marketing/discount/saveDiscount.json',
			dataType: 'json',
			contentType : 'application/json;charset=utf-8',
			data: JSON.stringify(row),
			success: function(r) {
				var discount = r.discount;
				closeDialog('winDiscount');
				$('#activitySearch').form('reset');
				queryData('activityList', 'activitySearch');
				if (!row.id) {//如果是新建
					if (row.discountType === '1') {
						storeForAct(discount.id, discount.state);
					} else {
						productForAct(discount.id, discount.state);
						unSelectProductForAct();
					}
				}
			}
        });
    }
	
	//删除活动、活动下线
	function delActivity(type, id) {
		var ids = [];
		if (!id) {//批量删除
			var selects = $('#activityList').datagrid('getSelections');
			$.each(selects, function(index, item) {
				ids.push(item.id);
			})
		} else {
			ids.push(id);
		}

		if (ids.length === 0) {
			infoMsg('请选择需要删除的活动');
			return;
		}

		confirmMsg('确定将所选打折活动'+(type===3?'下线':'删除')+'吗？', function() {
			$.ajax({
				url: '${pageContext.request.contextPath}/marketing/discount/deleteDiscount.json',
				data: $.param({index: type, ids: ids}, true),
				success: function() {
					queryData('activityList', 'activitySearch');
				}
			})
		})
	}

	//重置表单
    function resetDiscountForm() {
    	$('#fomDiscount').form('clear');
    	$('#per-timers .timer').remove();
    	$('#discountType').combobox({readonly:false});
    }

    //查看活动信息
    function viewActivity(id) {
    	openDialog('winViewDiscount', '打折活动信息');

		var rows = $('#activityList').datagrid('getRows');
		var index = $('#activityList').datagrid('getRowIndex', id);
		var row = $.extend({}, rows[index]);

		var dayArr = row.cycle.split(',');
		var dayStr = '';
		if (dayArr.length === 7) {
			dayStr = '每天';
		} else {
			$.each(dayArr, function(index, item) {
				dayStr += weekDate[+item-1].text + '、';
			})
			dayStr = dayStr.substring(0, dayStr.length-1);
		}
		row.dayStr = dayStr;

		$('#activity-view').html(template("view-discount", row));
    }

    //选择、查看店铺
    function storeForAct(id, state) {
    	if (state == '0') {//未开始的活动
    		$('#store-act-id').val(id);
    		$('#pointplaceBtns a').eq(0).show();
	    	$('#pointplaceBtns a').eq(1).find('.l-btn-text').html('取消');
	    	openDialog('winStore', '请选择参与店铺');
	    	$('#storeList').datagrid('showColumn', 'checked');
	    } else {
	    	$('#pointplaceBtns a').eq(0).hide();
	    	$('#pointplaceBtns a').eq(1).find('.l-btn-text').html('关闭');
	    	openDialog('winStore', '参与活动店铺');
	    	$('#storeList').datagrid('hideColumn', 'checked');
	    }
	    $('#storeList').datagrid({
			queryParams: {id: id, state: state},
			url: '${pageContext.request.contextPath}/marketing/discount/findPointPlacePage.json',
			onLoadSuccess: function(data) {
				if (state != '0') return;
				$.each(data.rows, function(index, item) {
					if (item.checked) {
						$('#storeList').datagrid('checkRow', index);
					}
				})
			}
		});
    }

    //保存店铺设置
    function saveActStore() {
    	var pointNos = [];
    	var selections = $('#storeList').datagrid('getSelections');
    	$.each(selections, function(index, item) {
    		pointNos.push(item.pointNo);
    	})

    	$.ajax({
    		url: '${pageContext.request.contextPath}/marketing/discount/savePointPlaceProduct.json',
    		data: $.param({
    			id: $('#store-act-id').val(),
    			type: 'pointPlace',
    			pointPlaceList: pointNos
    		}, true),
    		info: '保存成功',
    		task: function() {
    			closeDialog('winStore');
    		}
    	})
    }

    //选择、查看商品及所属店铺
    function productForAct(id, state) {
    	if (state == '0') {//未开始的活动
	    	$('#product-act-id').val(id);
	    	$('#productBtns a').eq(0).show();
	    	$('#productBtns a').eq(1).find('.l-btn-text').html('取消');
	    	$('#add-pro').show();
	    	openDialog('winProductStore', '请选择参与商品');
	    	$('#productList').datagrid('showColumn', 'operate');
	    	$('#pointList').datagrid('showColumn', 'checked');
	    } else {
	    	$('#productBtns a').eq(0).hide();
	    	$('#productBtns a').eq(1).find('.l-btn-text').html('关闭');
	    	$('#add-pro').hide();
	    	openDialog('winProductStore', '参与活动商品');
	    	$('#productList').datagrid('hideColumn', 'operate');
	    	$('#pointList').datagrid('hideColumn', 'checked');
	    }

		$('#productList').datagrid({
			queryParams: {id: id, type: 1},
			url: '${pageContext.request.contextPath}/marketing/discount/findProductPage.json',
			onLoadSuccess: function(data) {
				activeProIndex = null;
				productsToStores = data.rows;

				if (data.rows.length === 0) {
					$('#pointList').datagrid('loadData', {total:0, rows:[], clear:true});
					return;
				};
				$(this).datagrid('selectRow', 0);
			},
			onSelect: function(index, row) {
				if (index === activeProIndex) return;
				activeProIndex = index;
				if (productsToStores[activeProIndex].stores) {
					var rows = productsToStores[activeProIndex].stores;
					$('#pointList').datagrid('loadData', {total: rows.length, rows: rows});
					return;
				}
				var productId = row.id;
				$('#pointList').datagrid({
					queryParams: {id: id, productId: productId, state:state},
					url: '${pageContext.request.contextPath}/marketing/discount/findPointPlacePage.json',
					onLoadSuccess: function(data) {
						if(!data.clear) productsToStores[activeProIndex].stores = data.rows;
						if (state != '0') return;
						$.each(data.rows, function(index, item) {
							if (item.checked) {
								$('#pointList').datagrid('checkRow', index);
							}
						})
					},
					onSelect: function(index) {
						productsToStores[activeProIndex].stores[index].checked = 1;
					},
					onUnselect: function(index) {
						productsToStores[activeProIndex].stores[index].checked = 0;
					},
					onSelectAll: function() {
						$.each(productsToStores[activeProIndex].stores, function(index, item) {
							item.checked = 1;
						})
					},
					onUnselectAll: function() {
						$.each(productsToStores[activeProIndex].stores, function(index, item) {
							item.checked = 0;
						})
					}
				})
			}
		});
    }

    //保存商品所属店铺设置
    function saveActProduct() {
    	var products = [];
    	$.each(productsToStores, function(index, item) {
    		if (!item.stores) return;
    		var product = {id: item.id, pointPlaceList: []};
    		$.each(item.stores, function(index, item) {
    			if(item.checked) product.pointPlaceList.push(item.pointNo);
    		})
    		product.pointPlaceList = product.pointPlaceList.join(',');
    		products.push(product);
    	})

    	if (products.length === 0) {
    		closeDialog('winProductStore');
    		return;
    	}

    	var params = {id: $('#product-act-id').val(), productList: products};
    	$.ajax({
			url: '${pageContext.request.contextPath}/marketing/discount/updateProduct.json',
			dataType: 'json',
			contentType : 'application/json;charset=utf-8',
			data: JSON.stringify(params),
			info: '保存成功',
			task: function(r) {
				closeDialog('winProductStore');
			}
        });
    }

    //未选商品列表
    function unSelectProductForAct() {
    	$('#product-ctn').html('');
    	openDialog('winProduct', '请选择商品');
    	var	id = $('#product-act-id').val();
    	$.ajax({
    		url: '${pageContext.request.contextPath}/marketing/discount/findProductPage.json',
    		data: {id: id, type: 2},
    		success: function(data) {
    			var html = template('productItem', data);
    			$('#product-ctn').html(html == '' ? '<div style="font-size:16px;color:red;text-align:center;">暂无商品可选择</div>' : html);
    		}
    	})
    }

    //保存活动商品
    function saveProductForAct() {
		var $selectedPro = $('#product-ctn .product-item.active');
		if ($selectedPro.length === 0) {
			closeDialog('winProduct');
			return
		}
		var productIds = [];
		$.each($selectedPro, function(index, item) {
			productIds.push($(this).data('pid'));
		})
		$.ajax({
			url: '${pageContext.request.contextPath}/marketing/discount/savePointPlaceProduct.json',
			data: $.param({
				id: $('#product-act-id').val(),
				type: 'product',
				productList: productIds,
			}, true),
			success: function() {
				closeDialog('winProduct');
				$('#productList').datagrid('reload');
			}
		})
    }

    //删除活动商品
    function delProductForAct(pid) {
    	$.ajax({
    		url: '${pageContext.request.contextPath}/marketing/discount/deleteProductPointPlace.json',
    		data: {discountId: $('#product-act-id').val(), productId: pid},
    		success: function(data) {
    			//$('#productList').datagrid('reload');
    			var index = $('#productList').datagrid('getRowIndex', pid);
    			productsToStores.splice(index, 1);
    			$('#productList').datagrid('loadData', {total:productsToStores.length, rows:productsToStores});
    			if (productsToStores.length === 0) return;
    			$('#productList').datagrid('selectRow', 0);
    		}
    	})
    }

    //查看活动数据
    function viewActData(id) {
    	var $orderDatas = $('#act-order-data td').html(0),
    		$userDatas = $('#act-user-data td').html(0);

    	var rows = $('#activityList').datagrid('getRows');
		var index = $('#activityList').datagrid('getRowIndex', id);
		var row = rows[index];
		openDialog('winData', row.title);

    	$.ajax({
    		url: '${pageContext.request.contextPath}/marketing/discount/findOrderPage.json',
    		data: {discountId: id},
    		success: function(r) {
    			var data = r.order;
    			$orderDatas.eq(0).html(data.totalOrdersNumber);
    			$orderDatas.eq(1).html(data.dealQuantity);
    			$orderDatas.eq(2).html(data.totalOrderAmount);
    			$orderDatas.eq(3).html(data.clinchDealOrder);
    			$orderDatas.eq(4).html(data.newClinchDealOrders);
    			$orderDatas.eq(5).html(data.newClinchDealAmount);
    			$userDatas.eq(0).html(data.newUserNumber);
    			$userDatas.eq(1).html(data.capitaOrders);
    			$userDatas.eq(2).html(data.capitaProducts);
    			$userDatas.eq(3).html(data.capitaAmount);
    		}
    	})
    }

    //template helper
    template.helper("getFilePath", function(images) {
		if (!images) return '';
		var path = images.split(';')[0].split(',')[3];
		if (!path) return '';
		return getFileUrl(path);
	});

	//formatters
	function getProductImgUrl(value) {
		if (!value) return '<img style="width:70px;height:70px;margin:5px 0;" />';
		var path = value.split(';')[0].split(',')[3];
		if (!path) {
			return '<img style="width:70px;height:70px;margin:5px 0;" />';
		}
		return '<img src="'+getFileUrl(path)+'" style="width:70px;height:70px;margin:5px 0;" />';
	}

	function formatterDelPro(value, row) {
		return '<span class="easyui-tooltip icon-remove" style="display:inline-block;width:16px;height:16px;cursor:pointer;" onclick="delProductForAct('+row.id+')"></span>';
	}
	
	function formatterStoreType(value) {
		for (var i = 0; i < pointTypes.length; i++) {
			if (value == pointTypes[i].code) {
				return pointTypes[i].name;
			}
		}
	}
	
	function formatterWay(value) {
		switch(value) {
			case '1':
				return '全天打折';
			case '2':
				return '时段打折';
			default:
				return value;
		}
	}

	function formatterState(value) {
		switch(value) {
			case '0':
				return '未开始';
			case '1':
				return '进行中';
			case '2':
				return '已结束';
			case '3':
				return '已下线';
			default:
				return value;
		}
	}
	
	function formatterOperate(value, row) {
		var del = '<span class="oper-btn danger" onclick="delActivity(9,'+row.id+')">删除</span>',
			edit = '<span class="oper-btn primary" onclick="openActivityWin('+row.id+')">设置</span>',
			view = '<span class="oper-btn primary" onclick="viewActivity('+row.id+')">查看</span>',
			down = '<span class="oper-btn warn" onclick="delActivity(3,'+row.id+')">下线</span>',
			data = '<span class="oper-btn info" onclick="viewActData('+row.id+')">数据</span>',
			store = '<span class="oper-btn info" onclick="storeForAct('+row.id+', '+row.state+')">店铺</span>',
			product = '<span class="oper-btn info" onclick="productForAct('+row.id+', '+row.state+')">商品</span>'

		switch(row.state+row.discountType) {
			case '01':
				return edit + store + del + data;
			case '11':
				return view + store + down + data;
			case '21':
				return view + store + del + data;
			case '31':
				return view + store + del + data;
			case '02':
				return edit + product + del + data;
			case '12':
				return view + product + down + data;
			case '22':
				return view + product + del + data;
			case '32':
				return view + product + del + data;
			default:
				return value;
		}
	}
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="activitySearch" class="search-form">
			<div class="form-item">
				<div class="text">活动名称</div>
				<div class="input">
					<input name="title" class="easyui-textbox" data-options="prompt:'活动名称'" />
				</div>
			</div>
		</form>
		<div class="search-btn" onclick="queryData('activityList','activitySearch')">查询</div>
	</div>

	<!-- 活动列表 -->
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#activityListTools'" title="打折活动列表">
		<div id="activityListTools">
			<a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增打折活动'" onclick="openActivityWin()"></a>
			<!-- <a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除打折活动'" onclick="delActivity(9)"></a> -->
		</div>
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<!-- 活动列表 -->
			<table id="activityList" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,idField:'id'">
				<thead>
					<tr>
						<th data-options="field:'',checkbox:true"></th>
						<th data-options="field:'title',width:120,align:'center'">活动名称</th>
						<th data-options="field:'startTime',width:80,align:'center'">活动开始时间</th>
						<th data-options="field:'endTime',width:80,align:'center'">活动结束时间</th>
						<th data-options="field:'discountWay',width:50,align:'center',formatter:formatterWay">打折方式</th>
						<th data-options="field:'state',width:50,align:'center',formatter:formatterState">状态</th>
						<th data-options="field:'operate',width:120,align:'center',formatter:formatterOperate">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	
	<!-- 编辑/新增 -->
	<div id="winDiscount" class="easyui-dialog" data-options="closed:true,buttons:'#marketingBtns',onClose:resetDiscountForm" style="width:446px;height:442px;padding:10px 20px;">
		<form id="fomDiscount" method="post">
			<input type="hidden" id="marketing_id" name="id">
			<input type="hidden" id="marketing_state" name="state">
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">活动名称</div>
					<input name="title" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['length[1,64]']">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item" style="width:100%;">
					<div class="text">活动时间</div>
					<input id="discountStartTime" name="startTime" class="easyui-datetimebox" data-options="required:true,editable:false,prompt:'活动开始日期'"> - <input id="discountEndTime" name="endTime" class="easyui-datetimebox" data-options="required:true,editable:false,prompt:'活动结束日期'">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<input id="cycle-select" name="cycle" class="easyui-combotree" data-options="panelHeight:'auto',editable:false,multiple:true">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">打折类型</div>
					<select id="discountType" name="discountType" class="easyui-combobox" data-options="required:true,panelHeight:'auto',editable:false">
						<option value="1">店铺打折</option>
						<option value="2">商品打折</option>
					</select>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">打折方式</div>
					<select id="discountWay" name="discountWay" class="easyui-combobox" data-options="required:true,panelHeight:'auto',editable:false,onSelect:transDiscountWay">
						<option value="1">全天打折</option>
						<option value="2">时段打折</option>
					</select>
				</div>
			</div>
			<div id="alldaylong" class="form-row" style="display:none;">
				<div class="form-cloumn form-item">
					<div class="text">折扣数</div>
					<input id="alldaydiscount" class="easyui-numberbox" data-options="required:true,min:0.1,max:10.0,precision:1" style="width:100px;"> 折
				</div>
			</div>
			<div id="periodtime" class="form-row" style="display:none;">
				<div id="per-timers" class="form-cloumn form-item" style="width:100%;">
					<div class="text">打折时段 (<span class="add-timer">添加</span>)</div>
				</div>
			</div>
		</form>
		<div id="marketingBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveActivity()">保存</a>
            <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winDiscount')">取消</a>
		</div>
	</div>

	<!-- 查看活动 -->
	<div id="winViewDiscount" class="easyui-dialog" data-options="closed:true" style="width:446px;height:442px;padding:10px 20px;">
		<div id="activity-view" class="activity-view"></div>
	</div>
		
	<!-- 店铺选择列表 -->
	<div id="winStore" class="easyui-dialog" data-options="closed:true,buttons:'#pointplaceBtns'" style="width:750px;">
        <!-- <div style="padding:10px;">
            <form id="storeSearch" class="search-form">
                <input type="hidden" id="q_device_unbind_Id" name="Id" value=""> 
                <span>店铺类型：</span>
                <div class="form-item">
					<input class="easyui-combobox" id="pointType" name="pointType" data-options="textField:'name',valueField:'code'">
                </div>                
            </form>
            <div class="search-btn" onclick="queryData('storeList','storeSearch')">查询</div>
        </div> -->
        <input id="store-act-id" type="hidden">
        <div style="padding:10px;height:406px;">
            <table id="storeList" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,pagination:false,rownumbers:false">
                <thead>
                    <tr>
                        <th data-options="checkbox:true,field:'checked',width:20"></th>
                        <th data-options="field:'pointNo',width:100,align:'center'">店铺编号</th>
                        <th data-options="field:'pointName',width:200,align:'center'">店铺名称</th>
                        <th data-options="field:'pointAddress',width:200,align:'center'">店铺地址</th>
                        <th data-options="field:'pointType',width:100,align:'center',formatter:formatterStoreType">店铺类型</th>
                    </tr>
                </thead>
            </table>
        </div>
        <div id="pointplaceBtns">
            <a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveActStore()">保存</a>
            <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winStore')">取消</a>
        </div>
    </div>

    <!-- 未选商品列表 -->
    <div id="winProduct" class="easyui-dialog" data-options="closed:true,buttons:'#unselectProductBtns'" style="width:1100px;height:612px;">
		<div id="product-ctn" class="product-ctn clearfix"></div>
		<div id="unselectProductBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveProductForAct()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winProduct')">取消</a>
		</div>
    </div>
	
	<!-- 商品店铺列表 -->
	<div id="winProductStore" class="easyui-dialog" data-options="closed:true,buttons:'#productBtns'" style="width:1100px;">
		<input id="product-act-id" type="hidden">
		<div id="add-pro" class="add-pro" onclick="unSelectProductForAct()">添加活动商品</div>
		<div style="padding:10px; height:500px">
			<div style="float:left; width:30%; height:100%;">
				<table id="productList" class="easyui-datagrid" data-options="fit:true,fitColumns:true,singleSelect:true,pagination:false,rownumbers:false,idField:'id'">
					<thead>
						<tr>
							<th data-options="field:'images',width:100,align:'center',formatter:getProductImgUrl">商品图片</th>
							<th data-options="field:'skuName',width:180,align:'center'">商品名称</th>
							<th data-options="field:'operate',width:30,align:'center',formatter:formatterDelPro"></th>
						</tr>
					</thead>
				</table>
			</div>
			<div style="box-sizing:border-box; float:right; width:70%; height:100%; padding-left:10px;">
				<table id="pointList" class="easyui-datagrid" data-options="striped:true,fit:true,fitColumns:true,pagination:false,rownumbers:false">
					<thead>
						<tr>
							<th data-options="checkbox:true,field:'checked'"></th>
	                        <th data-options="field:'pointNo',width:100,align:'center'">店铺编号</th>
	                        <th data-options="field:'pointName',width:200,align:'center'">店铺名称</th>
	                        <th data-options="field:'pointAddress',width:200,align:'center'">店铺地址</th>
	                        <th data-options="field:'pointType',width:100,align:'center',formatter:formatterStoreType">店铺类型</th>
	                        <!-- <th data-options="field:'priceOnLine',width:60,align:'center'">零售价</th> -->
						</tr>
					</thead>
				</table>
			</div>
		</div>
		<div id="productBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveActProduct()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winProductStore')">取消</a>
		</div>
	</div>

	<!-- 活动数据 -->
	<div id="winData" class="easyui-dialog" data-options="closed:true" style="width:1000px;padding:20px 20px 40px;">
		<div class="act-data-title">订单汇总数据</div>
		<table class="act-data-tb">
			<tbody>
				<tr>
					<td>下单总数</td>
					<td>成交订单数</td>
					<td>下单总金额</td>
					<td>成交总金额</td>
					<td>新用户成交订单数</td>
					<td>新用户成交总金额</td>
				</tr>
				<tr id="act-order-data">
					<td>0</td>
					<td>0</td>
					<td>0</td>
					<td>0</td>
					<td>0</td>
					<td>0</td>
				</tr>
			</tbody>
		</table>
		<div class="act-data-title" style="margin-top:30px;">用户汇总数据</div>
		<table class="act-data-tb">
			<tbody>
				<tr>
					<td>新用户数</td>
					<td>人均成交订单数</td>
					<td>人均成交商品数</td>
					<td>人均成交额</td>
				</tr>
				<tr id="act-user-data">
					<td>0</td>
					<td>0</td>
					<td>0</td>
					<td>0</td>
				</tr>
			</tbody>
		</table>
	</div>
	
	<script id="view-discount" type="text/html">
		<div class="row">
			<label>活动标题：</label>
			<span id="v-dc-title">{{title}}</span>
		</div>
		<div class="row">
			<label>活动时间：</label>
			<span id="v-dc-time">{{startTime}} - {{endTime}} ({{dayStr}})</span>
		</div>
		<div class="row">
			<label>打折类型：</label>
			<span id="v-dc-type">{{discountType == '1' ? '店铺打折' : '商品打折'}}</span>
		</div>
		<div class="row">
			<label>打折方式：</label>
			<span id="v-dc-way">{{discountWay == '1' ? '全天打折' : '时段打折'}}</span>
		</div>
		{{if discountWay === '1'}}
		<div class="row">
			<label>折扣数：</label>
			<span id="v-dc-value">{{discountPeriodList[0].discountValue}}</span>
		</div>
		{{else}}
		<div class="row">
			<label>打折时段：</label>
			<span id="v-dc-period">
			{{each discountPeriodList as item i}}
				{{item.discount_start}} - {{item.discount_end}} ({{item.discountValue.toFixed(1)}}折) <br>
			{{/each}}
			</span>
		</div>
		{{/if}}
	</script>
	<script id="productItem" type="text/html">
	{{each rows as row i}}
		<div class="product-item" data-pid="{{row.id}}">
			<div class="inner">
				<img src="{{getFilePath(row.images)}}">
				<span class="name">{{row.skuName}}</span>
			</div>
		</div>
	{{/each}}
	</script>	
</body>
</html>