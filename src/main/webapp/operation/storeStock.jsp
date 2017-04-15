<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>店铺库存</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery-list-dragsort.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<style type="text/css">
	html {height: 100%;}
	body {margin: 0; height: 100%;}
	.list-head {padding: 0 10px; height: 36px;}
	.list-head .panel-title {height: 36px; line-height: 36px;}
	.list-head .panel-tool {right: 25px; top: 0; margin-top: 0; height: 36px; line-height: 36px;}
	.tab-nav {padding-left: 20px; height: 35px; line-height: 35px; font-size: 15px; border-bottom: 1px solid #e5e5e5;}
	.tab-nav span {display: inline-block; padding: 0 10px; cursor: pointer;}
	.tab-nav span.active {border-bottom: 1px solid #009cd3; color: #009cd3;}
	.tab-content {width: 200%; height: 100%;}
	.tab-item {float: left; width: 50%; height: 100%;}
	.tab-item .l {width: 50%; padding: 10px 5px 10px 10px;}
	.tab-item .r {width: 50%; padding: 10px 10px 10px 5px;}
	.btn-df {margin-left: 15px; border-radius: 4px; line-height: 22px;}
	.btn-df:hover {color: #009cd3; background-color: #fff;}
	.set-sort {font-size: 13px; color: #009cd3; cursor: pointer;}
	.set-sort:hover {text-decoration: underline;}
	.pro-list {width: 1120px; list-style: none; padding: 0 5px; margin: 0 auto;}
	.pro-item {float: left; width: 130px; margin: 10px 5px;}
	.placeHolder {background: #eee; border: 1px dashed #999; box-sizing: border-box; line-height: 186px; font-size: 50px; text-align: center;}
	.pro-item .body {position: relative; padding: 15px; border: 1px solid #e5e5e5; cursor: move!important;}
	.pro-item .body.recom {border-color: red;}
	.pro-item .body img {width: 100%; height: 100px;}
	.pro-item .body .name {position: absolute; bottom: 0; left: 0; width: 100%; padding: 2px 5px; box-sizing: border-box; background: rgba(0,0,0,.6); color: #fff;}
	.pro-item .body .recommend {position: absolute; top: 0; left: 0; cursor: pointer;}
	.pro-item .body .recommend input {position: relative; top: 2px;}
	.pro-item .sort .num {margin-left: 10px; font-size: 14px; font-weight: bold; color: #009cd3;}
</style>
<script type="text/javascript">
	var pointTypes = [];
	var isDateChangeL = false, 
		isDateChangeR = false;//商品价格、状态是否改变过
	var pointPlace = {id: '', discount: '', deviceAisles: []},
		proSetData = {productId: '', productCode: '', pointPlaces: []};

	//解析店铺类型
	function formatPointType(value) {
		for (var i = 0; i < pointTypes.length; i++)
			if (pointTypes[i].code == value)
				return pointTypes[i].name;
		return value;
	}

	//解析商品图片路径
	function getProductImgUrl(value) {
		if (!value) return '<img style="width:70px;height:70px;margin:10px 0;" />';
		var path = value.split(';')[0].split(',')[3];
		if (!path) {
			return '<img style="width:70px;height:70px;margin:10px 0;" />';
		}
		return '<img src="'+getFileUrl(path)+'" style="width:70px;height:70px;margin:10px 0;" />';
	}

	//解析商品类型
	function getProductType(value) {
		if (value == 1) {
			return '自有';
		} else if (value == 2) {
			return '平台供货';
		} else {
			return value;
		}
	}

	//商品应补货数量
	function formatterSupply(value) {
		return '<span style="color:red">'+(typeof value != 'undefined' ? value : '-')+'</span>';
	}

	function formatDiscount(value) {
		return '<input class="discount" type="text" value="'+(typeof value == 'number' ? value : '')+'" style="width:55px;text-align:center;">%';
	}

	function formatInput(value) {
		return '<input class="price" type="text" value="'+(typeof value == 'number' ? value : '')+'" style="width:55px;text-align:center;">';
	}

	function formatInputSpec(value, row) {
		if (row.trueOrFalse) return '-';
		if ($('#storeList-L').datagrid('getSelected')) {
			return '<input class="price" type="text" value="'+(typeof value == 'number' ? value : '')+'" style="width:55px;text-align:center;">';
		} else {
			return value;
		}
	}

	function formatSaleState(value) {
		return '<input class="state" type="checkbox" ' + (value == 0 ? 'checked="checked"' : '') + '>';
	}

	function formatSaleStateSpec(value, row) {
		if (row.trueOrFalse) return '-';
		if ($('#storeList-L').datagrid('getSelected')) {
			return '<input class="state" type="checkbox" ' + (value == 0 ? 'checked="checked"' : '') + '>';
		} else {
			return value ? '否' : '是'; 
		}
	}

	function formatterSort(val, row) {
		if (row.cabinetNo == 1) {
			return '<span class="set-sort" onclick="getDevPro('+row.factoryDevNo+')">设置排序</span>';
		} else {
			return '';
		}
	}

	function formatterNull(val) {
		return typeof val != 'undefined' ? val : '-';
	}

	function getDevPro(factoryDevNo) {
		$('#winProSort').html('').data('factoryDevNo', factoryDevNo);
		openWin('winProSort', '商品列表<span style="color:red;">（推荐商品最多3个）</span>');
		$.ajax({
			url: '${pageContext.request.contextPath}/operation/storeStock/findProductByFactoryDevNo.json',
			data: {factoryDevNo: factoryDevNo},
			task: function(data) {
				$.each(data.rows, function(i, pro) {
					if (pro.images) {
						pro.images = pro.images.split(';')[0].split(',')[3];
					}
				})
				$('#winProSort').html(template('pro-list-tpl', data));
				$('#pro-list').dragsort({
					dragSelector: '.body',
					dragSelectorExclude: '.recommend, input',
					placeHolderTemplate: '<li class="pro-item placeHolder"></li>',
					dragEnd: function() {
						$('#pro-list .pro-item').each(function() {
							$(this).find('.num').html($(this).index() + 1)
						})
					}
				})
			}
		})
	}

	//设置排序
	function saveSort() {
		var factoryDevNo = $('#winProSort').data('factoryDevNo');
		var $proItems = $('#pro-list .pro-item');
		var sortingRecommendeds = [];
		$proItems.each(function() {
			var $this = $(this);
			var productId = $this.data('id');
			var serialNumber = $this.index()+1;
			var type = $this.find('.recommend input').prop('checked') ? 1 : 0;
			var pro = {factoryDevNo:factoryDevNo, productId:productId, serialNumber:serialNumber, type:type};
			sortingRecommendeds.push(JSON.stringify(pro));
		})
		$.ajax({
			url: '${pageContext.request.contextPath}/operation/storeStock/updateSortingRecommended.json',
			data: $.param({sortingRecommendeds:sortingRecommendeds}, true),
			info: '排序保存成功！',
			task: function(data) {
				closeWin('winProSort');
			}
		})
	}

	//店铺维度修改价格、状态
	function storeSetSave() {
		if (!pointPlace.id) return;
		openRequestMask('正在保存，请稍候...');
		var rows = $('#storeList-L').datagrid('getRows');
		for (var i = 0; i < rows.length; i++) {
			if (rows[i].id == pointPlace.id) {
				rows[i].discount = pointPlace.discount;
				break;
			}
		}
		pointPlace.deviceAisles = [];
		var rows = $('#productList-L').datagrid('getRows').filter(function(n) {
			return n.trueOrFalse == 0;
		});
		var $priceOnlines = $('#pro-panel-L').find('.price');
		$priceOnlines.css('color', 'red');
		var $saleables = $('#pro-panel-L').find('.state');
		for (var i = 0; i < rows.length; i++) {
			//获取商品折后价
			var priceOnLine = +($priceOnlines.eq(i).val());

			//商品上下架
			var saleable = $saleables.eq(i).prop('checked') ? 0 : 1;

			//对比价格和销售状态
			if (priceOnLine == rows[i].priceOnLine && saleable == rows[i].sellable) continue;

			var proData = {productId:rows[i].productId, productCode:rows[i].productCode, priceOnLine:priceOnLine, sellable:saleable};
			pointPlace.deviceAisles.push(proData);
		}
		if (pointPlace.deviceAisles.length == 0) {
			infoMsg('当前无任何修改');
			return;
		}
		$.ajax({
			url: '${pageContext.request.contextPath}/operation/storeStock/saveStoresPrice.json',
            dataType:"json",
            contentType : 'application/json;charset=utf-8', //设置请求头信息  
			data: JSON.stringify(pointPlace),
			info: '保存成功！',
			task: function() {
				isDateChangeL = false;
				$('#productList-L').datagrid('reload');
			}
		})
	}

	//商品维度修改价格、状态
	function proSetSave() {
		openRequestMask('正在保存，请稍候...');
		proSetData.pointPlaces = [];
		var rows = $('#storeList-R').datagrid('getRows');
		var $priceOnlines = $('#store-panel-R').find('.price');
		var $saleables = $('#store-panel-R').find('.state');
		for (var i = 0; i < rows.length; i++) {
			//获取商品售价
			var priceOnLine = $priceOnlines.eq(i).val();

			//商品上下架
			var saleable = $saleables.eq(i).prop('checked') ? 0 : 1;

			//对比价格和销售状态
			if (priceOnLine == rows[i].priceOnLine && saleable == rows[i].sellable) continue;

			var pointData = {id:rows[i].id, priceOnLine:priceOnLine, sellable:saleable};
			proSetData.pointPlaces.push(pointData);
		}
		if (proSetData.pointPlaces.length == 0) {
			infoMsg('当前无任何修改');
			return;
		}
		$.ajax({
			url: '${pageContext.request.contextPath}/operation/storeStock/saveProdsPrice.json',
            dataType:"json",
            contentType : 'application/json;charset=utf-8', //设置请求头信息  
			data: JSON.stringify(proSetData),
			info: '保存成功！',
			task: function() {
				isDateChangeR = false;
				$('#storeList-R').datagrid('reload');
			}
		})
	}

	$(function() {
		//获取店铺类型数据
		var pointTypesQuery = [{'code':'', 'name':'所有'}];
		var cache = getAllSysTypes();
		for (var i = 0; i < cache.length; i++) {
			if (cache[i].type == 'POINT_PLACE_TYPE') {
				pointTypes.push(cache[i]);
				pointTypesQuery.push({'code':cache[i].code, 'name':cache[i].name});
			}
		} 

		//查看维度切换
		$('#tab-nav').on('click', 'span', function(){
			$(this).addClass('active').siblings().removeClass('active');
			var index = $(this).index();
			$('#tab-content').animate({marginLeft: -index*100 + '%'});
		})

		//限制输入
		$('#tab-content').on('keyup', '.price, .discount', function() {
			var $this = $(this);
			var val = $this.val();
			val = val.replace(/[^\d.]/g,'');
			val = val.replace(/\.{2,}/g,".");
			var len = val.split('.').length;
			if (len > 2) val = val.substring(0, val.length-1);
			$this.val(val);
		})
	})

	$(function() {
		//监听折扣修改
		$('#store-panel-L').on('change keyup', '.discount', function(e){
			if (e.type == 'keyup' && e.keyCode != 13) return;

			var $this = $(this);
			var discount = $this.val();
			if (discount == '') $(this).val(100);
			if (discount.indexOf('.') != -1) $this.val((+discount).toFixed(2));

			discount = +discount;
			if (pointPlace.discount == discount) return;
			pointPlace.discount = discount;
			var rows = $('#productList-L').datagrid('getRows').filter(function(n) {
				return n.trueOrFalse == 0;
			});
			var $priceOnlines = $('#pro-panel-L').find('.price');
			$priceOnlines.css('color', 'red');
			for (var i = 0; i < rows.length; i++) {
				//计算商品折后价
				var priceOnLine = +((rows[i].price*discount/100).toFixed(2));
				$priceOnlines.eq(i).val(priceOnLine);
			}
		})

		//商品价格、状态修改
		$('#tab-content .tab-item').eq(0).on('change', '.discount, .price, .state', function(){
			isDateChangeL = true;
		})

		function getProductList(id, type) {
			isDateChangeL = false;
			if (type == 'store') {
				var url = '${pageContext.request.contextPath}/operation/storeStock/findReplenishProds.json';
				var queryParams = {ids: id};
			} else if (type == 'cabinet') {
				var url = '${pageContext.request.contextPath}/operation/storeStock/findReplenishProdsByCabId.json';
				var queryParams = {cabId: id};
			}
			$('#productList-L').datagrid({
				url: url,
				queryParams: queryParams
			});
		}

		$('#storeList-L').datagrid({
			url: '${pageContext.request.contextPath}/operation/storeStock/findStores.json',
			onLoadSuccess: function() {
				if ($('#storeList-L').datagrid('getRows').length > 0) {
					$('#storeList-L').datagrid('selectRow', 0);
				}
			},
			onSelect: function(rowIndex, rowData) {
				if (rowData.id == pointPlace.id) return;//如果点击的是当前选择的行
				if (isDateChangeL) {
					var save = confirm('当前修改尚未保存，是否保存？');
					if (save) {
						storeSetSave();
					} else {
						var index = $('#storeList-L').datagrid('getRowIndex', pointPlace.id);
						var $discountInput = $('#store-panel-L .discount').eq(index);
						var oldDiscount = $('#storeList-L').datagrid('getRows')[index].discount;
						$discountInput.val(oldDiscount);
					}
				}
				pointPlace.id = rowData.id;
				pointPlace.discount = rowData.discount;
				pointPlace.deviceAisles = [];
				$.each($('#store-panel-L .subDevice'), function(index, item) {
					if ($(item).siblings().length != 0) {
						$(item).datagrid('unselectAll');
					}
				})
				getProductList(rowData.id, 'store');
			},
			view: detailview,
	        detailFormatter:function(index, row){
	          return '<div style="padding:5px;background-color:#f5f5f5;"><table id="subDevice-' + index + '" class="subDevice"></table></div>';
	        },
	        onExpandRow: function(index, row) {
	        	$('#subDevice-'+index).datagrid({
	        		url:'${pageContext.request.contextPath}/operation/storeStock/findCabinetsByStoreId.json',
	        		queryParams:{storeId:row.id},
	        		fitColumns:true,
		            singleSelect:true,
		            pagination:false,
		            height:'auto',
		            columns:[[
		              	{field:'factoryDevNo', width:130, align:'center', title:'设备组号'},
		              	{field:'typeDesc', width:220, align:'center', title:'设备类型'},
		              	{field:'aisleCount', width:50, align:'center', title:'货道数量'},
		              	{field:'operate', width:80, align:'center', title:'操作', formatter:formatterSort}
		            ]],
		            onResize:function() {
		              	$('#storeList-L').datagrid('fixDetailRowHeight',index);
		            },
		            loadFilter:function(r) {
		            	var cabinetList = r.cabinetList;
		            	cabinetList = $.grep(cabinetList, function(n, i) {
		            		return n.bindState == 1;
		            	})
		            	return {total: cabinetList.length, rows: cabinetList};
		            },
		            onLoadSuccess:function() {
		              	setTimeout(function() {
		                	$('#storeList-L').datagrid('fixDetailRowHeight',index);
		              	},0);
		            },
		            onSelect:function(index, row) {
		            	if (isDateChangeL) {
							var save = confirm('当前修改尚未保存，是否保存？');
							if (save) {
								storeSetSave();
							} else {
								var index = $('#storeList-L').datagrid('getRowIndex', pointPlace.id);
								var $discountInput = $('#store-panel-L .discount').eq(index);
								var oldDiscount = $('#storeList-L').datagrid('getRows')[index].discount;
								$discountInput.val(oldDiscount);
							}
						}
		            	$.each($('#store-panel-L .subDevice').not(this), function(index, item) {
							if ($(item).siblings().length != 0) {
								$(item).datagrid('unselectAll');
							}
						})
		            	$('#storeList-L').datagrid('unselectAll');
		            	getProductList(row.id, 'cabinet');
		            	pointPlace.id = '';
		            }
	        	})

	        }
		})
	})

	$(function() {
		//监听商品售价修改
		$('#pro-panel-R').on('change keyup', '.price', function(e){
			if (e.type == 'keyup' && e.keyCode != 13) return;

			var $this = $(this);
			var priceOnline = $this.val();
			$this.val((+priceOnline).toFixed(2));

			var priceOnline = +priceOnline;
			var $priceOnlines = $('#store-panel-R').find('.price');
			$priceOnlines.css('color', 'red');
			for (var i = 0; i < $priceOnlines.length; i++) {
				$priceOnlines.eq(i).val(priceOnline.toFixed(2));
			}
		})

		//商品状态修改
		$('#pro-panel-R').on('change', '.state', function(e){
			var isCheck = $(this).prop('checked');
			var $checkboxs = $('#store-panel-R').find('.state');
			for (var i = 0; i < $checkboxs.length; i++) {
				$checkboxs.eq(i).prop('checked', isCheck);
			}
		})

		//商品价格、状态修改
		$('#tab-content .tab-item').eq(1).on('change', '.price, .state', function(){
			isDateChangeR = true;
		})

		function getStoreList() {
			isDateChangeR = false;
			var row = $('#productList-R').datagrid('getSelected');
			$('#storeList-R').datagrid({
				url: '${pageContext.request.contextPath}/operation/storeStock/findStoresByProdId.json',
				queryParams: {
					productIds: row.productId
				}
			});
		}

		$('#productList-R').datagrid({
			url: '${pageContext.request.contextPath}/operation/storeStock/findAllProds.json',
			onLoadSuccess: function() {
				if ($('#productList-R').datagrid('getRows').length > 0) {
					$('#productList-R').datagrid('selectRow', 0);
				}
			},
			onSelect: function(rowIndex, rowData) {
				if (rowData.productId == proSetData.productId) return;//如果点击的是当前选择的行
				if (isDateChangeR) {
					var save = confirm('当前修改尚未保存，是否保存？');
					if (save) {
						proSetSave();
					}
				}
				if (proSetData.productId) {
					var index = $('#productList-R').datagrid('getRowIndex', proSetData.productId);
					$('#productList-R').datagrid('refreshRow', index);
				}
				proSetData.productId = rowData.productId;
				proSetData.productCode = rowData.productCode;
				proSetData.pointPlaces = [];
				getStoreList();
			}
		})
	})

	$(function() {
		template.helper("getFilePath", function(images) {
			return getFileUrl(images);
		});

		//设置推荐位
		$('#winProSort').on('change', '.recommend input', function() {
			if (!$(this).prop('checked')) {
				$(this).parents('.body').removeClass('recom');
			} else {
				var $checkeds = $('#winProSort .recommend input:checked');
				if ($checkeds.length > 3) {
					$(this).prop('checked', false);
					infoMsg('已经有三个推荐商品，请取消其中一个再重新选择');
				} else {
					$(this).parents('.body').addClass('recom');
				}
			}
		})

		$('#winProSort').on('mousemove', '.pro-item', function() {
			var $placeHolder = $(this).siblings('.placeHolder');
			if ($placeHolder.length) {
				$placeHolder.html($('#pro-list .pro-item').not(this).index($placeHolder)+1);
			}
		})

		//导出表格
		$('#storeProds, #allProds').click(function() {
			var row = $('#storeList-L').datagrid('getSelected');
			if (!row) {
				infoMsg('请选择店铺！');
				return;
			};
			if ($(this).attr('id') == 'storeProds') {
				window.location.href = '${pageContext.request.contextPath}/operation/storeStock/exportStoreProds.xls?ids='+row.id;
			} else {
				window.location.href = '${pageContext.request.contextPath}/operation/storeStock/exportAllProds.xls?ids='+row.id;
			}
		})
	})
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north',border:false" style="height:36px;">
		<div id="tab-nav" class="tab-nav">
			<span class="active">按店铺查看</span>
			<span>按商品查看</span>
		</div>
	</div>
	<div data-options="region:'center',border:false" style="overflow:hidden;">
		<div id="tab-content" class="tab-content">
			<div class="tab-item easyui-layout">
				<div data-options="region:'north',border:false" style="padding:15px; height:84px;">
					<!-- 查询 -->
					<form id="storeQueryForm" class="search-form">
						<div class="form-item">
							<div class="text">店铺名称</div>
							<div class="input">
								<input name="pointName" class="easyui-textbox" data-options="prompt:'店铺名称'" />
							</div>
						</div>
					</form>
					<div class="search-btn" onclick="queryData('storeList-L','storeQueryForm')">查询</div>
				</div>
				<div data-options="region:'center',border:false" class="easyui-layout">
					<div id="store-panel-L" class="l" data-options="region:'west',border:false,headerCls:'list-head',collapsible:false,tools:'#storeOpt'" title="店铺信息"  style="width:48%;">
						<div id="storeOpt">
							<span id="storeProds" class="btn-df">店铺补货清单</span>
							<span id="allProds" class="btn-df">仓库发货总表</span>
							<!-- <span id="updateToPro" class="btn-df">应用至商品<img src="${pageContext.request.contextPath}/images/arrow-right.png" style="height:10px;"></span> -->
						</div>
						<table id="storeList-L" class="easyui-datagrid" data-options="fit:true,singleSelect:true,idField:'id'">
							<thead>
								<tr>
									<!-- <th data-options="checkbox:true,field:'',width:20" align="center"></th> -->
									<th data-options="field:'pointNo',width:100" align="center">店铺编号</th>
									<th data-options="field:'pointName',width:200">店铺名称</th>
									<th data-options="field:'pointAddress',width:200">店铺地址</th>
									<th data-options="field:'pointType',width:100,formatter:formatPointType" align="center">店铺类型</th>
									<th data-options="field:'discount',width:100,formatter:formatDiscount" align="center">店铺折扣</th>
								</tr>
							</thead>
						</table>
					</div>
					<div id="pro-panel-L" class="r" data-options="region:'center',border:false,headerCls:'list-head',tools:'#proOpt'" title="商品列表">
						<div id="proOpt">
							<span class="btn-df" style="width:90px;" onclick="storeSetSave()">保存修改</span>
						</div>
						<table id="productList-L" class="easyui-datagrid" data-options="fit:true,singleSelect:true,pagination:false">
							<thead>
								<tr>
									<th field="images" width="100" align="center" formatter="getProductImgUrl">商品图片</th>
									<th field="productCode" width="150" align="center">商品编码</th>
									<th field="productName" width="200" align="center">商品名称</th>
									<th field="totalStock" width="80" align="center" formatter="formatterNull">库存</th>
									<th field="totalSupplementNo" width="80" align="center" formatter="formatterSupply">应补货</th>
									<th field="price" width="80" align="center" formatter="formatterNull">标准价</th>
									<th field="priceOnLine" width="80" align="center" formatter="formatInputSpec">零售价</th>
									<th field="sellable" width="50" align="center" formatter="formatSaleStateSpec">下架</th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			</div>
			<div class="tab-item easyui-layout">
				<div data-options="region:'north',border:false" style="padding:15px; height:84px;">
					<!-- 查询 -->
					<form id="proQueryForm" class="search-form">
						<div class="form-item">
							<div class="text">商品名称</div>
							<div class="input">
								<input name="skuName" class="easyui-textbox" data-options="prompt:'商品名称'" />
							</div>
						</div>
					</form>
					<div class="search-btn" onclick="queryData('productList-R','proQueryForm')">查询</div>
				</div>
				<div data-options="region:'west',border:false" class="easyui-layout">
					<div id="pro-panel-R" class="l" data-options="region:'west',border:false,collapsible:false,headerCls:'list-head',tools:'#proOpt'" title="商品列表">
						<table id="productList-R" class="easyui-datagrid" data-options="fit:true,singleSelect:true,idField:'productId'">
							<thead>
								<tr>
									<!-- <th data-options="checkbox:true,field:'',width:20" align="center"></th> -->
									<th field="images" width="100" align="center" formatter="getProductImgUrl">商品图片</th>
									<th field="productCode" width="150" align="center">商品编码</th>
									<th field="productName" width="200" align="center">商品名称</th>
									<th field="price" width="80" align="center">标准价</th>
									<th field="priceOnLine" width="80" align="center" formatter="formatInput">零售价</th>
									<th field="sellable" width="50" align="center" formatter="formatSaleState">下架</th>
								</tr>
							</thead>
						</table>
					</div>
					<div id="store-panel-R" class="r" data-options="region:'center',border:false,headerCls:'list-head',tools:'#storeOption'" title="店铺信息">
						<div id="storeOption">
							<span class="btn-df" style="width:90px;" onclick="proSetSave()">保存修改</span>
						</div>
						<table id="storeList-R" class="easyui-datagrid" data-options="fit:true,singleSelect:true">
							<thead>
								<tr>
									<th data-options="field:'pointNo',width:100" align="center">店铺编号</th>
									<th data-options="field:'pointName',width:200">店铺名称</th>
									<th data-options="field:'pointAddress',width:200">店铺地址</th>
									<th data-options="field:'pointType',width:100,formatter:formatPointType" align="center">店铺类型</th>
									<th data-options="field:'priceOnLine',width:80,formatter:formatInput" align="center">商品售价</th>
									<th data-options="field:'sellable',width:50,align:'center',formatter:formatSaleState">下架</th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div id="winProSort" class="easyui-dialog" data-options="closed:true,buttons:'#proSortBtns'" style="width:1161px;height:90%;">
		<div id="proSortBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveSort()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winProSort');">取消</a>
		</div>
	</div>
	<script id="pro-list-tpl" type="text/html">
		<ul id="pro-list" class="pro-list clearfix">
			{{each rows as pro i}}
			<li class="pro-item" data-id="{{pro.productId}}">
				<div class="sort">排序<span class="num">{{i+1}}</span></div>
				<div class="body {{pro.stickTime ? 'recom' : ''}}">
					<label class="recommend"><input type="checkbox" {{pro.stickTime ? 'checked' : ''}}>推荐</label>
					<img src="{{pro.images ? getFilePath(pro.images) : ''}}">
					<div class="name">{{pro.productName}}</div>
				</div>
				<div class="record">库存：{{pro.totalStock}}</div>
				<div class="record">零售价格：{{pro.price}}</div>
			</li>
			{{/each}}
		</ul>
	</script>
</body>
</html>