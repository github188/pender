<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>销售计划</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/deviceLayoutCreator.js"></script>
<style type="text/css">
	.layout-ctn {}
	.layout-item {float: left; box-sizing: border-box; padding: 5px;}
	.aisle-wrapper {float: left;}
	.aisle-block {margin: 5px; padding: 5px; border: 1px solid #f1f1f1; box-shadow: 0 0 5px #eee; background-color: #f9f9f9;}
	.aisle-block.active {background-color: #dddddd; border-color: #cc2222;}
	.prod-item {max-width: 120px; margin: 0 auto;}
	.prod-item .body {position: relative; cursor: pointer;}
	.prod-item .img {}
	.prod-item .name {display: none; position: absolute; bottom: 0; left: 0; width: 100%; padding: 2px 5px; box-sizing: border-box; background: rgba(0,0,0,.6); color: #fff;}
	.multi-up {position:absolute; margin:0; right:0; top:0; z-index: 99; height: 15px; width: 15px; -webkit-appearance:none; -moz-appearance:none; border:1px solid #c0c0c0; outline: none; cursor: pointer;}
	.multi-up:checked {background: #0075bf url(${pageContext.request.contextPath}/images/icon-yes.png) no-repeat center/80%; border-color: #0075bf;}

	.operate-ctn {text-align: center; color: #169BD5;}
	.operate-ctn span {margin-right: 15px; cursor: pointer;}
	.operate-ctn span:last-child {margin-right: 0;}
	.btn-df {width: 80px; border-radius: 4px;}
	
	.pro-upload-btns {position: absolute; z-index: 100; width: 100%;}
	.pro-upload-btns .inner {margin-right:17px; padding: 10px 15px; border-bottom: 1px solid #ddd; background-color:#fff;}

    .pro-vr-list {padding: 60px 10px 10px; font-size: 0;}
    .pro-vr-list .item {width: 20%; display: inline-block; font-size: 12px;}
    .pro-vr-list .inner {margin: 7px; border: 1px solid #f1f1f1; box-shadow: 0 0 5px #eee; background-color: #f9f9f9; cursor: pointer;}
    .pro-vr-list .inner.active {background-color: #dddddd; border-color: #cc2222;}
    .pro-vr-list .body {position: relative; padding: 10px;}
    .pro-vr-list img {width: 100%;}
    .pro-vr-list .name {display: none; position: absolute; bottom: 0; left: 0; width: 100%; padding: 2px 5px; box-sizing: border-box; background: rgba(0,0,0,.6); color: #fff;}
</style>
<script type="text/javascript">
	//全局变量
	var prodIndexs = [];//记录货道排序号
    var productVrs = [];//保存虚拟商品列表
	var pointTypes = [],
		deviceTypes = [];

	//formatter
	function formatPointType(value) {
		for (var i = 0; i < pointTypes.length; i++)
			if (pointTypes[i].code == value)
				return pointTypes[i].name;
		return value;
	}

	function deviceType(value) {
    	for (var i = 0; i < deviceTypes.length; i++)
            if (deviceTypes[i].code == value)
                return deviceTypes[i].name;
        return value;
    }

    function showApplyImage(value, row, index) {
        if (value) {
            var rows = value.split(';');
            var path = getFileUrl(rows[0].split(',')[3]);
            return '<img src="' + path + '" style="width:60px;height:60px;">';
        }
        return value;
    }
    
    function bindState(value, row) {
        return row.bindState == 0 ? '' : value;
    }

    function setPriceOnline(value, row) {
        return '<input class="pro-price-set" type="number" value="'+row.price+'" style="width:50px;text-align:right;">';
    }
    
    function getImgPath(value) {
        if (value) {
            var rows = value.split(';');
            return getFileUrl(rows[0].split(',')[3]);
        }
        return value;
    }

    function operateCreate(value, row) {
        //多媒体控制柜(model:3)无货道，不能上架实物商品
        //虚拟商品绑定到一组设备，只能在主控设备上架
    	var str = '<div class="operate-ctn">'
    	            + (row.model != 3 ? '<span onclick="initDeviceProds('+row.deviceId+','+row.cabinetNo+','+row.model+')">实物商品上架</span>' : '')
                    + (row.cabinetNo == 1 ? '<span onclick="getProductVrs('+row.factoryDevNo+','+row.model+')">虚拟商品上架</span>' : '')
    	        + '</div>';
    	return str;
    }

	function getProductType(value) {
		if (value == 1) {
			return '自有';
		} else if (value == 2) {
			return '平台供货';
		} else {
			return value;
		}
	}
	
	// 查询当前设备下的设备商品信息
    function initDeviceProds(deviceId, cabinetNo, model) {
    	// 初始化货柜tab
		$.ajax({
            url : '${pageContext.request.contextPath}/product/salesPlan/findDeviceProds.json',
            data: {
            	deviceId:deviceId,
            	cabinetNo:cabinetNo
            },
            async: false,
            task : function(data) {
            	if (!data.deviceAisleList || data.deviceAisleList.length == 0) {
            		infoMsg("该设备下暂无货道信息！");
            		return;
            	}
            	
            	openDialog('winDeviceProds', deviceType(model));
                $('#device_id').val(data.deviceAisleList[0].deviceId);
                $('#device_orgId').val(orgId);
                $('#cabinet_id').val(data.deviceAisleList[0].cabinetId);

                if (deviceAisleOpt[model]) {
                	var aisleOpt = deviceAisleOpt[model];
                	if (model == 5 || model == 6 || model == 7) {//格子柜货道号第一位数字不确定，所以必须重新注入货道号
                		$.each(aisleOpt[0], function(i, item) {
                			aisleOpt[0][i].aisle = data.deviceAisleList[i].aisleNum;
                		})
                	}
                } else {
                	var aisleOpt = [[]];
                	$.each(data.deviceAisleList, function(i, item) {
                		aisleOpt[0][i] = {aisle:item.aisleNum, width:1/8};
                	})
                }
                var $layout = createDeviceLayout(aisleOpt); //生成设备货道布局
                $('#shelf-ctn').html($layout);

                var $aisles = $('#shelf-ctn .aisle-block');
                $.each(data.deviceAisleList, function(i, item) {
                	item.index = i;
                	var $ctn = $aisles.filter('[aisle="'+item.aisleNum+'"]');
                	$ctn.html(template("prod-item-tpl", item));
                })
                var $img = $('#shelf-ctn').find('.img');
                $img.height($img.width());

                $('#shelf-ctn').find('.easyui-linkbutton').linkbutton();
            }
        });		
    }
	
    // 保存货道信息
	function saveDeviceProds() {
		$.ajax({
            url : '${pageContext.request.contextPath}/product/salesPlan/save.json',
            data: $("#fomDeviceProds").serializeJson(),
            async: false,
            info : '商品设置成功！',
            task : function(data) {
            	closeDialog('winDeviceProds');
            }
        });
	}

	// 货道全选
	function checkAllProds() {
		$('#shelf-ctn .aisle-block').addClass('active');
        $('#proVrList .inner').addClass('active');
	}

	// 货道全不选
	function uncheckAllProds() {
		$('#shelf-ctn .aisle-block').removeClass('active');
        $('#proVrList .inner').removeClass('active');
	}
	
	// 设置实物商品
    function uploadProds(m) {
    	prodIndexs = [];
    	var index = $('#shelf-ctn .aisle-block').index($(m).parents('.aisle-block'));
    	prodIndexs.push(index);
        openDialog('winUploadProds', '设置商品');
        //根据pointId判断是否需要重新加载商品列表
        if ($('#saleGrid').datagrid('options').queryParams.pointId !== $('#storeList').datagrid('getSelected').id) {
        	$('#saleGrid').datagrid({
        		url : '${pageContext.request.contextPath}/product/salesPlan/find.json',
        		queryParams: {
        			pointId: $('#storeList').datagrid('getSelected').id
        		}
        	});
        }
    }
    // 批量设置实物商品
    function multiUploadProds() {
    	prodIndexs = [];
    	var $prodItems = $('#shelf-ctn .aisle-block');
    	var $selecteds = $('#shelf-ctn .aisle-block.active');
    	if ($selecteds.length == 0) {
    		infoMsg('请选择需要设置的商品！');
    		return;
    	}
    	for (var i = 0; i < $selecteds.length; i++) {
    		var index = $prodItems.index($selecteds.eq(i));
    		prodIndexs.push(index);
    	}
    	openDialog('winUploadProds', '设置商品');
    	//根据pointId判断是否需要重新加载商品列表
    	if ($('#saleGrid').datagrid('options').queryParams.pointId !== $('#storeList').datagrid('getSelected').id) {
        	$('#saleGrid').datagrid({
        		url : '${pageContext.request.contextPath}/product/salesPlan/find.json',
        		queryParams: {
        			pointId: $('#storeList').datagrid('getSelected').id
        		}
        	});
        }
    }

    // 设置实物商品Dialog中的【保存】按钮
    function saveUploadProds() {
        var row = $('#saleGrid').datagrid('getSelected');
        if (!row) {
            infoMsg('请选择需要设置的商品！');
            return;
        }
        doUploadProds(row);
    } 
    function doUploadProds(row) {
    	var index = $('#saleGrid').datagrid('getRowIndex', row);
    	var priceOnline = $('#winUploadProds .pro-price-set').eq(index).val();
    	var reg = new RegExp("^\\d+(\\.\\d+)?$");//只包含小数点和数字
    	if (!reg.test(priceOnline)) {
    		infoMsg('零售价有误，请重新设置');
    		$('#winUploadProds .pro-price-set').eq(index).val(row.price);
    		return;
    	}

    	var $prodItems = $('#shelf-ctn .aisle-block');
    	$.each(prodIndexs, function(i, n) {
    		var $prod = $prodItems.eq(n);
    		//商品id
    		$prod.find('.pid').val(row.id);
    		//销售状态
    		$prod.find('.saleable').val(1);
			$prod.find('.saleable-show').html('可销售').css('color', 'green');
			//商品图片
    		$prod.find('.img').attr("src", getImgPath(row.images)).css('opacity', '1');
    		//商品名称
    		$prod.find('.name').html(row.skuName);
    		//货道容量
    		var $cacty = $prod.find('.cacty');
            var roadLength = +($prod.find('.prod-item').data('roadlength'));
    		if (!$cacty.is(':hidden') && roadLength) {
    			var capacity = Math.floor(roadLength/(row.perimeter/Math.PI));//计算货道容量
    			$cacty.val(capacity);
    		}
    		//零售价
    		$prod.find('.price').val(priceOnline);
    		$prod.find('.price-show').html((+priceOnline).toFixed(2));
    	})
        
        resetForm('applyQueryForm');
        $('#saleGrid').datagrid('clearSelections');
        closeDialog('winUploadProds');
        $prodItems.removeClass('active');
    }

    // 删除实物商品
    function soldOutProds(m) {
    	prodIndexs = [];
    	var index = $('#shelf-ctn .aisle-block').index($(m).parents('.aisle-block'));
    	prodIndexs.push(index);
    	confirmMsg('确定要删除该商品吗？', doSoldOutProds);
    }
    // 批量删除实物商品
    function multiSoldOutProds() {
    	prodIndexs = [];
    	var $prodItems = $('#shelf-ctn .aisle-block');
    	var $selecteds = $('#shelf-ctn .aisle-block.active');
    	if ($selecteds.length == 0) {
    		infoMsg('请选择需要删除的商品！');
    		return;
    	}
    	for (var i = 0; i < $selecteds.length; i++) {
    		var index = $prodItems.index($selecteds.eq(i));
    		prodIndexs.push(index);
    	}
    	confirmMsg('确定要删除所选商品吗？', doSoldOutProds);
    }
    function doSoldOutProds() {
    	var $prodItems = $('#shelf-ctn .aisle-block');
    	$.each(prodIndexs, function(i, n) {
    		var $prod = $prodItems.eq(n);
    		$prod.find('.pid').val('');
    		$prod.find('.saleable').val(1);
			$prod.find('.saleable-show').html('可销售').css('color', 'green');
			$prod.find('.img').attr('src', '').css('opacity', '1');
			$prod.find('.name').html('');
			var $cacty = $prod.find('.cacty');
            var roadLength = +($prod.find('.prod-item').data('roadlength'));
    		if (!$cacty.is(':hidden') && roadLength) {
    			$cacty.val('');
    		}
    		$prod.find('.price').val('');
    		$prod.find('.price-show').html('');
    	})
    	$prodItems.removeClass('active');
    }

    //获取虚拟商品初始化已上架虚拟商品列表和未上架商品列表
    function getProductVrs(factoryDevNo, model) {
        $.ajax({
            url: '${pageContext.request.contextPath}/product/salesPlan/findDeviceVirtualProduct.json',
            data: {devNo: factoryDevNo},
            task: function(data) {
                $('#devFactoryDevNo').val(factoryDevNo);
                openDialog('winProductVr', deviceType(model));
                productVrs = data.rows;
                var bindProVrs = productVrs.filter(function(n) {
                    return n.sort != 0;
                })
                $('#proVrList').html('');
                $.each(bindProVrs, function(i, pro) {
                    var $pro = template('pro-vr-tpl', pro);
                    $('#proVrList').append($pro);
                })
            }
        })
    }

    //下架虚拟商品
    function soldOutProVr() {
        $('#proVrList .inner.active').each(function() {
            var $pro = $(this).parent('.item');
            $pro.remove();
            productVrs.filter(function(n) {
                return n.id == $pro.data('id');
            })[0].sort = 0;
        })
    }

    //上架虚拟商品
    function uploadProVr() {
        openDialog('winUploadProductVr', '设置虚拟商品');
        var rows = productVrs.filter(function(n) {
            return n.sort == 0;
        })
        $('#proVrGrid').datagrid({
            data: {total: rows.length, rows: rows}
        })
    }

    //确认上架虚拟商品
    function saveUploadProVr() {
        var selections = $('#proVrGrid').datagrid('getSelections');
        $.each(selections, function(i, pro) {
            pro.sort = pro.id;
            var $pro = template('pro-vr-tpl', pro);
            $('#proVrList').append($pro);
        })
        closeDialog('winUploadProductVr');
    }

    //保存虚拟商品上架信息
    function saveProVr() {
        var $pros = $('#proVrList .item');
        var productIds = [];
        for (var i = 0; i < $pros.length; i++) {
            productIds.push($pros.eq(i).data('id'));
        }
        $.ajax({
            url: '${pageContext.request.contextPath}/product/salesPlan/saveDeviceVirtualProduct.json',
            data: $.param({devNo:$('#devFactoryDevNo').val(), productIds:productIds}, true),
            task: function(data) {
                closeDialog('winProductVr');
            }
        })
    }
    

	$(function() {
		orgId = '${_orgId}';

		//获取店铺类型数据
		var pointTypesQuery = [{'code':'', 'name':'所有'}];
		var cache = getAllSysTypes();
		for (var i = 0; i < cache.length; i++) {
			if (cache[i].type == 'POINT_PLACE_TYPE') {
				pointTypes.push(cache[i]);
				pointTypesQuery.push({'code':cache[i].code, 'name':cache[i].name});
			} else if (cache[i].type == 'DEVICE_TYPE2') {
                deviceTypes.push(cache[i]);
            }
		} 

		//设备列表
		$('#deviceList').datagrid({
			fit: true,
			fitColumns: true,
			onSelect: function(rowIndex, rowData) {
				$('#deviceList').datagrid('uncheckRow', rowIndex);
			}
		})

		//店铺列表
		$('#storeList').datagrid({
			fit: true,
			fitColumns: true,
			singleSelect: true,
			url: '${pageContext.request.contextPath}/product/salesPlan/findStores.json',
			onSelect: function(rowIndex, rowData) {
				$('#deviceList').datagrid({
					url : '${pageContext.request.contextPath}/store/overview/findBindDevCombination.json',
					queryParams: {
						'ORG_ID': rowData.orgId,
						'pointplaceId' : rowData.id
					}
				})
			},
			onLoadSuccess: function() {
                if ($(this).datagrid('getRows').length != 0) {
                    $(this).datagrid('selectRow', 0);
                }
			}
		})

		template.helper("getFilePath", function(images) {
			return getFileUrl(images);
		});

		$('#shelf-ctn, #proVrList').on('mouseover mouseout', '.body', function(e) {
			var $proName = $(this).children('.name');
			if (e.type === 'mouseover' && $proName.html() !== '') {
				$proName.stop().fadeIn();
			} else {
				$proName.stop().fadeOut();
			}
		})

		$('#shelf-ctn, #proVrList').on('click', '.body', function() {
			$(this).parents('.aisle-block, .inner').toggleClass('active');
		})
	})
</script>
</head>
<body class="easyui-layout" >
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="storeQueryForm" class="search-form">
			<div class="form-item">
				<div class="text">店铺名称</div>
				<div class="input">
					<input name="pointName" class="easyui-textbox" data-options="prompt:'店铺名称'" />
				</div>
			</div>
		</form>
		<div class="search-btn" onclick="queryData('storeList','storeQueryForm')">查询</div>
	</div>
	<div data-options="region:'center',border:false" class="easyui-layout">
		<div data-options="region:'west',border:false,split:true,headerCls:'list-head',collapsible:false" style="width:50%;" title="店铺信息">
			<div style="box-sizing:border-box; height:100%; padding:10px;">
				<table id="storeList">
					<thead>
						<tr>
							<th data-options="checkbox:true,field:'',width:20" align="center"></th>
							<th data-options="field:'pointNo',width:80" align="center">店铺编号</th>
							<th data-options="field:'pointName',width:150" align="center">店铺名称</th>
							<th data-options="field:'pointAddress',width:150" align="center">店铺地址</th>
							<th data-options="field:'pointType',width:50,formatter:formatPointType" align="center">店铺类型</th>
						</tr>
					</thead>
				</table>
			</div>
		</div>
		<div data-options="region:'center',border:false,headerCls:'list-head'" style="width:50%;" title="设备信息">
			<div style="box-sizing:border-box; height:100%; padding:10px;">
				<table id="deviceList">
					<thead>
						<tr>
							<th data-options="field:'factoryDevNo',width:120,formatter:bindState" align="center">设备组号</th>
							<th data-options="field:'model',width:200,formatter:deviceType" align="center">设备类型</th>
							<th data-options="field:'aisleCount',width:50" align="center">货道数量</th>
							<th data-options="field:'operate',width:300,formatter:operateCreate" align="center">操作</th>
						</tr>
					</thead>
				</table>
			</div>
		</div>
	</div>
	<!-- 实物商品上架 -->
	<div id="winDeviceProds" class="easyui-dialog" data-options="closed:true,buttons:'#prodBtns'" style="position:relative;width:90%;height:90%;">
		<div class="pro-upload-btns">
			<div class="inner">
				<span class="btn-df" onclick="checkAllProds()">全选</span>
				<span class="btn-df" onclick="uncheckAllProds()">全不选</span>
				<span class="btn-df" onclick="multiUploadProds()">批量设置</span>
				<span class="btn-df" onclick="multiSoldOutProds()">批量删除</span>
			</div>
		</div>
		<div style="height:100%;overflow:auto;">
			<form id="fomDeviceProds" method="post" style="margin:59px 10px 0 10px;">
				<input type="hidden" id="device_orgId" name="orgId">
			    <input type="hidden" id="device_id" name="deviceId">
			    <input type="hidden" id="cabinet_id" name="id">
	            <!-- 货柜tab -->
				<div id="shelf-ctn"></div>
			</form>
		</div>
		<div id="prodBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveDeviceProds();">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winDeviceProds');">取消</a>
		</div>
	</div>
	<!-- 设置实物商品 -->
    <div id="winUploadProds" class="easyui-dialog" data-options="closed:true,buttons:'#uploadPropsBtns'" style="width:900px;height:650px;padding:10px">
    	<div class="easyui-layout" style="height:100%;">
			<div data-options="region:'north',border:false" style="height:60px;">
				<form id="applyQueryForm" class="search-form">
			    	<div class="form-item">
						<div class="text">商品名称</div>
						<input id="q_apply_skuName" name="skuName" class="easyui-textbox">
			    	</div>
			    </form>
			    <sec:authorize access="find">
		            <div class="search-btn" onclick="queryData('saleGrid','applyQueryForm')">查询</div>
		            <div class="search-btn" onclick="resetForm('applyQueryForm')">重置</div>
		        </sec:authorize>
			</div>
			<div data-options="region:'center',border:false">
				<table id="saleGrid" class="easyui-datagrid" data-options="nowrap:false,striped:true,fit:true,fitColumns:true,singleSelect:true" title="实物商品列表">
		            <thead>
		                <tr>
		                    <th data-options="field:'ck',checkbox:true,width:20"></th>
		                    <th data-options="field:'images',width:60,align:'center',formatter:showApplyImage">商品图片</th>
		                    <th data-options="field:'code',width:100,align:'center'">商品编码</th>
		                    <th data-options="field:'skuName',width:200,align:'center'">商品名称</th>
		                    <th data-options="field:'spec',width:60,align:'center'">规格</th>
		                    <th data-options="field:'price',width:60,align:'center'">标准价</th>
		                    <th data-options="field:'priceOnline',width:60,align:'center',formatter:setPriceOnline">零售价</th>
		                    <th data-options="field:'stock',width:60,align:'center'">商品库存</th>
		                    <th data-options="field:'type',width:80,align:'center',formatter:getProductType">商品类型</th>
		                </tr>
		            </thead>
		        </table>
			</div>
    	</div>
        <div id="uploadPropsBtns">
            <a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveUploadProds()">保存</a>
            <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winUploadProds')">取消</a>
        </div>
    </div>
    <!-- 虚拟商品上架 -->
    <div id="winProductVr" class="easyui-dialog" data-options="closed:true,buttons:'#proVrBtns'" style="width:900px;height:600px;">
        <input type="hidden" id="devFactoryDevNo">
        <div class="pro-upload-btns">
            <div class="inner">
                <span class="btn-df" onclick="checkAllProds()">全选</span>
                <span class="btn-df" onclick="uncheckAllProds()">全不选</span>
                <span class="btn-df" onclick="uploadProVr()">上架</span>
                <span class="btn-df" onclick="soldOutProVr()">下架</span>
            </div>
        </div>
        <div id="proVrList" class="pro-vr-list">
            
        </div>
        <div id="proVrBtns">
            <a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveProVr()">保存</a>
            <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winProductVr');">取消</a>
        </div>
    </div>
    <!-- 设置虚拟商品 -->
    <div id="winUploadProductVr" class="easyui-dialog" data-options="closed:true,buttons:'#uploadProVrBtns'" style="width:500px;height:650px;">
        <table id="proVrGrid" class="easyui-datagrid" data-options="nowrap:false,striped:true,border:false,fit:true,fitColumns:true,pagination:false">
            <thead>
                <tr>
                    <th data-options="field:'ck',checkbox:true,width:20"></th>
                    <th data-options="field:'images',width:60,align:'center',formatter:showApplyImage">商品图片</th>
                    <th data-options="field:'skuName',width:200,align:'center'">商品名称</th>
                    <th data-options="field:'type',width:60,align:'center',formatter:getProductType">商品类型</th>
                </tr>
            </thead>
        </table>
        <div id="uploadProVrBtns">
            <a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveUploadProVr()">保存</a>
            <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winUploadProductVr');">取消</a>
        </div>
    </div>
    <script type="text/html" id="prod-item-tpl">
		<div class="prod-item" data-roadlength="{{roadLength}}">
			<input type="hidden" name="deviceAisles[{{index}}].aisleNum" value="{{aisleNum}}">
			<input class="pid" type="hidden" name="deviceAisles[{{index}}].productId" value="{{productId}}">
			<div>
				<span>货道 <em style="font-weight:bold;font-style:normal;font-size:15px;color:#0a8cea;">{{aisleNum}}</em></span>
				<span class="saleable-show" style="float:right; {{sellable ? 'color:green;' : 'color:red;'}}">{{sellable ? '可销售' : '不可售'}}</span>
				<input class="saleable" type="hidden" name="deviceAisles[{{index}}].sellable" value="{{sellable}}">
			</div>
			<div class="body">
				<img class="img" src="{{images ? getFilePath(images) : ''}}" style="display:block; width:100%; height:100%; {{sellable ? '' : 'opacity:0.3;'}}">
				<div class="name">{{productName}}</div>
			</div>
     		<div style="margin-top:2px;">
				{{if model.indexOf('CVM-SPG') == -1}}
     			货道容量：<input class="cacty" type="number" name="deviceAisles[{{index}}].capacity" value="{{capacity == 0 ? '' : capacity}}" style="width:40px">
     			{{else}}
     			<input class="cacty" type="hidden" name="deviceAisles[{{index}}].capacity" value="{{capacity}}">
	     		货道容量：{{capacity}}
     			{{/if}}
     		</div>
     		<div style="margin-top:2px;">
				零售价格：<span class="price-show">{{typeof priceOnLine == 'undefined' ? '' : priceOnLine.toFixed(2)}}</span>
             	<input class="price" type="hidden" name="deviceAisles[{{index}}].priceOnLine" value="{{typeof priceOnLine == 'undefined' ? '' : priceOnLine}}">
     		</div>
     		<div style="margin-top:2px;">
     			<a href="javascript:void(0)" data-options="iconCls:'icon-no'" class="easyui-linkbutton pull-right" onclick="soldOutProds(this);" style="float:right;border:none;height:22px;"></a>
				<a href="javascript:void(0)" class="easyui-linkbutton" onclick="uploadProds(this);" style="display:block;max-width:115px;height:22px;border-radius:4px;">设置商品</a>
     		</div>
		</div>
    </script>
    <script type="text/html" id="pro-vr-tpl">
        <div class="item" data-id="{{id}}">
            <div class="inner">
                <div class="body">
                    <img src="{{images ? getFilePath(images.split(',')[3]) : ''}}" alt="{{productName}}">
                    <div class="name">{{skuName}}</div>
                </div>
            </div>
        </div>
    </script>
</body>
</html>