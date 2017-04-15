<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>商品库存管理</title>
<%@ include file="/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/kindeditor/themes/default/default.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/kindeditor/kindeditor-min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/kindeditor/lang/zh_TW.js"></script>
<style type="text/css">
	.form-cloumn { width: 33.33%; }
	.form-cloumn.form-item { margin-right: 0; }
	.form-cloumn.form-item [class^="easyui-"] { width: 218px; }
	.product-edit { padding: 15px; }
	.product-img { width: 162px; float: left; }
	.product-detail { margin-left: 177px; }
	.p-btn {font-size: 13px; color: #009cd3; vertical-align: top; margin-right: 10px; cursor: pointer;}
	.p-btn:hover {text-decoration: underline;}
	.img-show {display: none; width: 150px; height: 150px;}
</style>
<script type="text/javascript">

$.extend($.fn.validatebox.defaults.rules,{
    skuLength: {
        validator: function(value, param) {
        	var reg = new RegExp('^\\d{'+param[0]+'}$');
            return reg.test(value);
        },
        message: '请输入{0}位数字'  
    },
    noEmpty: {
    	validator: function(value) {
    		return $.trim(value) !== '';
    	},
    	message: '该输入项为必填项'
    }
});

var productEditor, productVrEditor, key;
var categories, Cate;
var productCategorys;//商品类目
var imgNum,//需要上传的图片数量
	uploadedImgNum;//已上传完毕的图片数量

//类目筛选
function getCate() {
	var Cate = {};
	Cate.first = (function(){
		var first = [];
		for (var i = 0; i < categories.length; i++) {
			if (!categories[i].parentId) {
				first.push(categories[i]);
			}
		}
		return first;
	}());
	Cate.second = (function(){
		var first = Cate.first;
		var second = [];
		for (var i = 0; i < categories.length; i++) {
			for (var j = 0; j < first.length; j++) {
				if (categories[i].parentId == first[j].id) {
					second.push(categories[i]);
					break;
				}
			}
		}
		return second;
	}());
	Cate.third = (function(){
		var second = Cate.second;
		var third = [];
		for (var i = 0; i < categories.length; i++) {
			for (var j = 0; j < second.length; j++) {
				if (categories[i].parentId == second[j].id) {
					third.push(categories[i]);
					break;
				}
			}
		}
		return third;
	}());
	return Cate;
}

//初始化商品图片上传
function initProductUpload(m) {
	m.contentWindow.initImageUploader(function() {
		uploadedImgNum++;
		if (uploadedImgNum == imgNum) {
			if ($(m).parents('#winProduct').length) {
				doSavePro(key);	
			} else {
				doSaveProVr(key);	
			}	
		}
	}, _token, 1);
}

//选择一级类目
function loadProductCategory1(record) {
	var newValue = record.code;
	$('#product_sku1, #product_vr_sku1').textbox('setValue', newValue);
	var second = Cate.second;
	var secondCate = [];
	for (var i = 0; i < second.length; i++) {
		if (second[i].parentId == record.id) {
			secondCate.push(second[i]);
		}
	}
	$('#product_category2, #product_vr_category2').combobox('loadData', secondCate);
	if (secondCate.length == 0) {
		$('#product_category2, #product_vr_category2').combobox('clear');
		$('#product_sku2, #product_vr_sku2').textbox('setValue', '');
		return;
	}
	$('#product_category2, #product_vr_category2').combobox('select', secondCate[0].code);
	$('#product_sku2, #product_vr_sku2').textbox('setValue', secondCate[0].code)
}
//选择二级类目
function loadProductCategory2(record) {
	var newValue = record.code;
	$('#product_sku2, #product_vr_sku2').textbox('setValue', newValue);
	var third = Cate.third;
	var thirdCate = [];
	for (var i = 0; i < third.length; i++) {
		if (third[i].parentId == record.id) {
			thirdCate.push(third[i]);
		}
	}
	$('#product_category3, #product_vr_category3').combobox('loadData', thirdCate);
	if (thirdCate.length == 0) {
		$('#product_category3, #product_vr_category3').combobox('clear');
		$('#product_sku3, #product_vr_sku3').textbox('setValue', '');
		return;
	}
	$('#product_category3, #product_vr_category3').combobox('select', thirdCate[0].code);
	$('#product_sku3, #product_vr_sku3').textbox('setValue', thirdCate[0].code);
}
//选择三级类目
function loadProductCategory3(record) {
	var newValue = record.code;
	$('#product_sku3, #product_vr_sku3').textbox('setValue', newValue);
}

//载入商品图片
function loadProductImage(row) {
	if (row.trueOrFalse) {
		var $iframes = $('#formProductVR .iframe-img');
		if (row.virtualImage) showImage($iframes[2], row.virtualImage);
	} else {
		var $iframes = $('#formProduct .iframe-img');
	}
	if (row.images) showImage($iframes[0], row.images);
	if (row.imagesDetail) showImage($iframes[1], row.imagesDetail);	
}
function showImage(iframe, image) {
	var fileIds = [], fileNames = [], realPaths = [];
	var vals = image.split(';')[0].split(',');
	fileIds.push(vals[0]);
	fileNames.push(vals[1]);
	realPaths.push(vals[3]);
	iframe.contentWindow.loadUploadImage(fileIds, fileNames, realPaths);
}

//禁用启用输入框
function changeInputState(type) {
	var $formProduct = $('#formProduct');
	var $textboxs = $formProduct.find('.easyui-textbox'),
		$numberbox = $formProduct.find('.easyui-numberbox'),
		$combobox = $formProduct.find('.easyui-combobox');
	if (type == 'enable') {
		$textboxs.textbox('enable');
		$numberbox.numberbox('enable');
		$combobox.combobox('enable');
	} else if (type == 'disable') {
		$textboxs.textbox('disable');
		$numberbox.numberbox('disable');
		$combobox.combobox('disable');
	}
}

//新增实物商品
function addPro() {
	key = new Date().getTime();
	openWin('winProduct', '新增实物商品');
	changeInputState('enable');
	$('#product_sku4').textbox('setValue', getCurUser().orgCode);
}

//编辑实物商品
function editPro(proId) {
	var $proList = $('#productList');
	key = new Date().getTime();
	openWin('winProduct', '编辑实物商品');
	var row = $proList.datagrid('getRows')[$proList.datagrid('getRowIndex', proId)];
	if (row.type == 2) {//平台供货
		changeInputState('disable');
		$('#product_skuName').textbox('enable');
		$('#product_price, #product_priceCombo').numberbox('enable');
		$('#product_category').combobox('enable');
	} else {//自有
		changeInputState('enable');
	}
	$('#formProduct').form('load', row);
	$('#product_category1').combobox('select', row.sku.split('-')[0]);
	$('#product_sku4').textbox('setValue', getCurUser().orgCode);
	$('#product_sku5').textbox('setValue', row.sku.split('-')[4]);
	loadProductImage(row);
	productEditor.html(row.description);
}

//添加实物商品规格
function addSpec(proId) {
	var $proList = $('#productList');
	key = new Date().getTime();
	openWin('winProduct', '添加规格');
	var row = $.extend({}, $proList.datagrid('getRows')[$proList.datagrid('getRowIndex', proId)]);
	if (!row.productCombination) {
		row.productCombination = row.code;
	}
	delete row.id;
	delete row.code;
	delete row.stock;
	delete row.spec;
	changeInputState('enable');
	if (row.type == 1) {//自有商品
		$('#product_category, #product_category1, #product_category2, #product_category3').combobox('disable');
	}
	$('#formProduct').form('load', row);
	$('#product_category1').combobox('select', row.sku.split('-')[0]);
	$('#product_sku4').textbox('setValue', getCurUser().orgCode);
	loadProductImage(row);
	productEditor.html(row.description);
	$('#formProduct .iframe-img').eq(0).hide().prev().show().attr('src', getFileUrl(row.images.split(';')[0].split(',')[3]));
}

//保存实物商品
function savePro() {
	if (!$('#formProduct').form('enableValidation').form('validate')) return;
	var $iframes = $('#formProduct .iframe-img');
	uploadedImgNum = 0;
	imgNum = 0;
	$iframes.each(function() {
		imgNum += this.contentWindow.queueImages.length;
	})
	if (!imgNum) {
		doSavePro(key);
		return;
	}
	if ($iframes[0].contentWindow.queueImages.length != 0) {
		$iframes[0].contentWindow.uploadImages({key:key,module:_fileType.product});
	}
	if ($iframes[1].contentWindow.queueImages.length != 0) {
		$iframes[1].contentWindow.uploadImages({key:key,module:_fileType.product_detail});
	}
}
function doSavePro(key, info) {
	var $iframes = $('#formProduct .iframe-img');

	productEditor.sync();

	var row = $('#formProduct').getValues();

	row.key = key;

	row.sku = $('#product_sku1').textbox('getValue') + '-' + $('#product_sku2').textbox('getValue') + '-' + $('#product_sku3').textbox('getValue') + '-' + $('#product_sku4').textbox('getValue') + '-' + $('#product_sku5').textbox('getValue');

	var delImageIds = [];
	$iframes.each(function() {
		delImageIds = delImageIds.concat(this.contentWindow.delImageIds)
	})
	row.fileIds = delImageIds;

	$.ajax({
		url:'${pageContext.request.contextPath}/product/productStock/save.json',
		data:$.param(row, true),
		info:'保存商品信息成功！' + (info == undefined ? '' : info),
		task:function(data, statusText, xhr) {
			if (!row.id && row.productCombination != '') {//判断是否是添加规格
				$('#product_sku5, #product_code, #product_spec, #product_stock').textbox('setValue', '');
			} else {
				resetProductForm();
			}
			$("#productList").datagrid("reload");
		}
	});
}

//新增虚拟商品
function addProVr() {
	key = new Date().getTime();
	openWin('winProductVR', '新增虚拟商品');
	$('#product_vr_sku4').textbox('setValue', getCurUser().orgCode);
}

//编辑虚拟商品
function editProVr(proId) {
	var $proList = $('#productList');
	key = new Date().getTime();
	openWin('winProductVR', '编辑虚拟商品');
	var row = $proList.datagrid('getRows')[$proList.datagrid('getRowIndex', proId)];
	$('#formProductVR').form('load', row);
	$('#product_vr_category1').combobox('select', row.sku.split('-')[0]);
	$('#product_vr_sku4').textbox('setValue', getCurUser().orgCode);
	$('#product_vr_sku5').textbox('setValue', row.sku.split('-')[4]);
	loadProductImage(row);
	productVrEditor.html(row.description);
}

//保存虚拟商品
function saveProVr() {
	if (!$('#formProductVR').form('enableValidation').form('validate')) return;
	var $iframes = $('#formProductVR .iframe-img');

	var qrcodeLength = $iframes.eq(2).contents().find('#dndArea').children().length;
	var url = $('#product_vr_url').textbox('getValue');
	if (!qrcodeLength && !url) {
		infoMsg('请上传二维码图片或者填写链接地址');
		return;
	} else if (qrcodeLength && url) {
		$('#product_vr_url').textbox('setValue', '');//如果存在二维码则删除链接
	}

	uploadedImgNum = 0;
	imgNum = 0;
	$iframes.each(function() {
		imgNum += this.contentWindow.queueImages.length;
	})
	if (!imgNum) {
		doSaveProVr(key);
		return;
	}
	if ($iframes[0].contentWindow.queueImages.length != 0) {
		$iframes[0].contentWindow.uploadImages({key:key,module:_fileType.product});
	}
	if ($iframes[1].contentWindow.queueImages.length != 0) {
		$iframes[1].contentWindow.uploadImages({key:key,module:_fileType.product_detail});
	}
	if ($iframes[2].contentWindow.queueImages.length != 0) {
		$iframes[2].contentWindow.uploadImages({key:key,module:_fileType.product_qrcode});
	}
}
function doSaveProVr(key, info) {
	var $iframes = $('#formProductVR .iframe-img');

	productVrEditor.sync();

	var row = $('#formProductVR').getValues();

	row.key = key;

	row.sku = $('#product_vr_sku1').textbox('getValue') + '-' + $('#product_vr_sku2').textbox('getValue') + '-' + $('#product_vr_sku3').textbox('getValue') + '-' + $('#product_vr_sku4').textbox('getValue') + '-' + $('#product_vr_sku5').textbox('getValue');

	var delImageIds = [];
	$iframes.each(function() {
		delImageIds = delImageIds.concat(this.contentWindow.delImageIds)
	})
	row.fileIds = delImageIds;

	$.ajax({
		url:'${pageContext.request.contextPath}/product/productStock/save.json',
		data:$.param(row, true),
		info:'保存商品信息成功！' + (info == undefined ? '' : info),
		task:function(data, statusText, xhr) {
			resetProductVrForm();
			$("#productList").datagrid("reload");
		}
	});
}

//删除商品
function delPro(proId) {
	var ids = [];
	if (proId) {
		ids.push(proId);
	} else {
		var rows = $('#productList').datagrid('getSelections');
		if (rows.length == 0) {
			infoMsg('请选择需要删除的商品！');
			return;
		}
		for (var i = 0; i < rows.length; i++) {
			ids.push(rows[i].id);
		}
	}
	confirmMsg('您确定要删除所选商品吗?', function() {
		$.ajax({
		  	url : '${pageContext.request.contextPath}/product/productStock/delete.json',
		  	data : $.param({
				'ids' : ids
		  	}, true),
		  	info : '所选删除成功！',
		  	task : function(data, statusText, xhr) {
				queryData('productList','productsSearch')
			}
		});
	})
}

//清空表单
function resetProductForm() {
	resetForm('formProduct');
	$('#formProduct input[type="hidden"]').not('#product_isVR, .textbox-value').val('');//reset无法清空隐藏域，要手动清除
	productEditor.html('');
	$('#formProduct').form('disableValidation');
	$('#product_sku4').textbox('setValue', getCurUser().orgCode);
	$('#formProduct .iframe-img').eq(0).show().prev().hide().attr('src', '');
	$('#formProduct .iframe-img').each(function() {
		this.contentWindow.closeImageUploader();
	})
}
function resetProductVrForm() {
	resetForm('formProductVR');
	$('#formProductVR input[type="hidden"]').not('#product_vr_isVR, .textbox-value').val('');//reset无法清空隐藏域，要手动清除
	productVrEditor.html('');
	$('#formProductVR').form('disableValidation');
	$('#product_vr_sku4').textbox('setValue', getCurUser().orgCode);
	$('#formProductVR .iframe-img').each(function() {
		this.contentWindow.closeImageUploader();
	})
}

//导出商品列表
function exportProducts() {
	window.location.href = '${pageContext.request.contextPath}/product/productStock/export.xls?' + $('#productsSearch').formSerialize();
}

//操作按钮
function formatterOperate(value, row) {
	var edit = '<span class="p-btn" onclick="editPro('+row.id+')">编辑</span>',
		editVr = '<span class="p-btn" onclick="editProVr('+row.id+')">编辑</span>',
		del = '<span class="p-btn" onclick="delPro('+row.id+')">删除</span>',
		add = '<span class="p-btn" onclick="addSpec('+row.id+')">添加规格</span>';

	return (row.trueOrFalse ? editVr : edit) + '<br>' + del + '<br>' + (row.trueOrFalse ? '' : add);
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
//解析商品库存
function getProductStock(value) {
	if (value == 0) {
		return '<span style="color:red">0</span>';
	} else {
		return value;
	}
}

$(function(){
	//商品列表
	$('#productList').datagrid({
		url: '${pageContext.request.contextPath}/product/productStock/find.json',
		onClickCell: function(index, field, value) {
			if (field === 'operate') {
				setTimeout(function() {
					$('#productList').datagrid('uncheckRow', index);
				}, 0);
			}
		}
	})

	//获取商品类别
	productCategorys = [];
	var cache = getAllSysTypes();
    for (var i = 0; i < cache.length; i++) {
        if (cache[i].type == 'PRODUCT_CATEGORY') {
        	productCategorys.push(cache[i]);
        }
    }
    $('#product_category, #product_vr_category').combobox('loadData', productCategorys);

	//图文描述
	var KindEditorConfig = {
		allowUpload:true,
    	dir: 'image',
    	filePostName:'files',
    	extraFileUploadParams:{module:_fileType.productDesc,key:new Date().getTime()},
    	uploadJson: '${pageContext.request.contextPath}/session/data/saveUploadFile.json'
	}
	productEditor = KindEditor.create('#product_description', KindEditorConfig);
	productVrEditor = KindEditor.create('#product_vr_description', KindEditorConfig);

	//请求商品类目
    $.ajax({
		url: '${pageContext.request.contextPath}/product/categoryMaintain/find.json',
		async: false,
		success: function(data) {
			categories = data.rows;
			Cate = getCate();
			$('#product_category1, #product_vr_category1').combobox('loadData', Cate.first);
		}
	})
})
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="productsSearch" class="search-form">
			<div class="form-item">
				<div class="text">商品名称</div>
				<div class="input">
					<input id="q_apply_skuName" name="skuName" class="easyui-textbox" data-options="prompt:'商品名称'" />
				</div>
			</div>
			<div class="form-item">
				<div class="text">商品编码</div>
				<div class="input">
					<input id="q_apply_code" name="code" class="easyui-textbox" data-options="prompt:'商品编码'" />
				</div>
			</div>
			<div class="form-item">
				<div class="text">商品类型</div>
				<div class="input">
					<select id="q_apply_type" class="easyui-combobox" name="type" data-options="panelHeight:'auto'">
					    <option value="">请选择</option>
					    <option value="1">自有</option>
					    <option value="2">平台供货</option>
					</select>
				</div>
			</div>
		</form>
		<div class="search-btn" onclick="queryData('productList','productsSearch')">查询</div>
		<div class="search-btn" onclick="resetForm('productsSearch')">重置</div>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#productListTools'" title="商品信息列表">
		<!-- 工具栏 -->
		<div id="productListTools">
			<span class="p-btn" onclick="addPro()">新增实物商品</span>
			<span class="p-btn" onclick="addProVr()">新增虚拟商品</span>
			<span class="p-btn" onclick="exportProducts()">导出商品</span>
			<!-- <a href="javascript:void(0)" class="icon-export easyui-tooltip" data-options="content:'导出商品信息'" onclick="exportProducts()"></a>
			<a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增商品'" onclick="openProductEditWin()"></a>
			<a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除商品'" onclick="openProductDelWin()"></a> -->
		</div>
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<!-- 商品列表 -->
			<table id="productList" class="easyui-datagrid" data-options="fit:true,fitColumns:true,singleSelect:true,idField:'id'">
				<thead>
					<tr>
						<th field="ck" checkbox="true"></th>
						<th field="images" width="70" align="center" formatter="getProductImgUrl">商品图片</th>
						<th field="code" width="130" align="center">商品编码</th>
						<th field="skuName" width="150" align="center">商品名称</th>
						<th field="createTime" width="100" align="center">创建时间</th>
						<th field="brand" width="50" align="center">品牌</th>
						<th field="origin" width="50" align="center">原产地</th>
						<th field="spec" width="50" align="center">规格名称</th>
						<th field="price" width="50" align="center">标准价</th>
						<th field="priceCombo" width="50" align="center">组合价</th>
						<th field="stock" width="70" align="center" formatter="getProductStock">库存</th>
						<th field="type" width="70" align="center" formatter="getProductType">商品类型</th>
						<th field="operate" width="80" align="center" formatter="formatterOperate">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<!-- 新增、编辑实物商品 -->
	<div id="winProduct" class="easyui-dialog" data-options="closed:true, onClose:resetProductForm, buttons:'#productBtns'" style="width:925px;height:600px;">
		<form id="formProduct" method="post">
			<input type="hidden" name="id">
			<input type="hidden" name="trueOrFalse" id="product_isVR" value="0">
			<input type="hidden" name="productCombination">
			<div class="product-edit">
				<div class="product-img">
					<div>首页图片</div>
					<img class="img-show" src="">
					<iframe class="iframe-img" width="162px" height="200px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initProductUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
					<div>详情页图片</div>
					<iframe class="iframe-img" width="162px" height="200px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initProductUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
				</div>
				<div class="product-detail">
					<div class="form-row" style="margin-top:0;">
						<div class="form-cloumn form-item" style="width:100%;">
							<div class="text">类目</div>
							<input id="product_category1" class="easyui-combobox" data-options="prompt:'一级类目',panelHeight:'auto',editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory1">
							<input id="product_category2" class="easyui-combobox" data-options="prompt:'二级类目',panelHeight:'auto',editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory2">
							<input id="product_category3" class="easyui-combobox" data-options="prompt:'三级类目',panelHeight:'auto',editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory3">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn" style="width:100%;">
							<div style="font-weight:bold;">SKU</div>
							<input id="product_sku1" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项'" readonly="readonly"> -
							<input id="product_sku2" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项'" readonly="readonly"> -
							<input id="product_sku3" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项'" readonly="readonly"> -
							<input id="product_sku4" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项'" readonly="readonly"> -
							<input id="product_sku5" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项',validType:['skuLength[4]']">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn form-item">
							<div class="text">商品名称</div>
							<input id="product_skuName" name="skuName" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['noEmpty','length[1,32]']">
						</div>
						<div class="form-cloumn form-item">
							<div class="text">品牌</div>
							<input id="product_brand" name="brand" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['noEmpty']">
						</div>
						<div class="form-cloumn form-item">
							<div class="text">原产地</div>
							<input id="product_origin" name="origin" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['noEmpty']">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn form-item">
							<div class="text">商品周长(单位:mm)</div>
							<input id="product_perimeter" name="perimeter" class="easyui-numberbox" data-options="required:true,prompt:'必填项'">
						</div>
						<div class="form-cloumn form-item">
							<div class="text">商品类别</div>
							<input id="product_category" name="category" class="easyui-combobox" data-options="required:true,prompt:'必填项',panelHeight:'auto',editable:false,valueField:'code',textField:'name'">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn form-item">
							<div class="text">规格名称(10个字以内)</div>
							<input id="product_spec" name="spec" class="easyui-textbox" maxlength="8" data-options="required:true,prompt:'必填项',validType:['noEmpty', 'length[1,6]']">
						</div>
						<div class="form-cloumn form-item">
							<div class="text">商品编码</div>
							<input id="product_code" name="code" class="easyui-numberbox" data-options="required:true,prompt:'必填项',validType:['length[1,16]']">
						</div>
						<div class="form-cloumn form-item">
							<div class="text">标准价</div>
							<input id="product_price" name="price" class="easyui-numberbox" precision="2" max="99999.99" size="8" maxlength="8" data-options="required:true,prompt:'必填项'">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn form-item">
							<div class="text">关联推荐优惠价</div>
							<input id="product_priceCombo" name="priceCombo" class="easyui-numberbox" precision="2" max="99999.99" size="8" maxlength="8" data-options="required:true,prompt:'必填项'">
						</div>
						<div class="form-cloumn form-item">
							<div class="text">库存</div>
							<input id="product_stock" name="stock" class="easyui-numberbox" data-options="required:true,prompt:'必填项',validType:['noEmpty', 'length[1,8]']">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn form-item">
							<div class="text">图文描述</div>
							<div>
								<textarea id="product_description" name="description" style="width:669px;height:300px;"></textarea>
							</div>
						</div>
					</div>
				</div>
			</div>
		</form>
		<div id="productBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="savePro()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeWin('winProduct')">取消</a>
		</div>
	</div>
	<!-- 新增、编辑虚拟商品 -->
	<div id="winProductVR" class="easyui-dialog" data-options="closed:true, onClose:resetProductVrForm, buttons:'#productVrBtns'" style="width:925px;height:600px;">
		<form id="formProductVR" method="post">
			<input type="hidden" name="id">
			<input type="hidden" name="trueOrFalse" id="product_vr_isVR" value="1">
			<div class="product-edit">
				<div class="product-img">
					<div>首页图片</div>
					<iframe class="iframe-img" width="162px" height="200px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initProductUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
					<div>详情页图片</div>
					<iframe class="iframe-img" width="162px" height="200px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initProductUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
					<div>二维码(与链接二选一)</div>
					<iframe class="iframe-img" width="162px" height="200px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" onload="initProductUpload(this)" src="${pageContext.request.contextPath}/free/uploadImage.html"></iframe>
				</div>
				<div class="product-detail">
					<div class="form-row" style="margin-top:0;">
						<div class="form-cloumn form-item" style="width:100%;">
							<div class="text">类目</div>
							<input id="product_vr_category1" class="easyui-combobox" data-options="prompt:'一级类目',panelHeight:'auto',editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory1">
							<input id="product_vr_category2" class="easyui-combobox" data-options="prompt:'二级类目',panelHeight:'auto',editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory2">
							<input id="product_vr_category3" class="easyui-combobox" data-options="prompt:'三级类目',panelHeight:'auto',editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory3">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn" style="width:100%;">
							<div style="font-weight:bold;">SKU</div>
							<input id="product_vr_sku1" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项'" readonly="readonly"> -
							<input id="product_vr_sku2" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项'" readonly="readonly"> -
							<input id="product_vr_sku3" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项'" readonly="readonly"> -
							<input id="product_vr_sku4" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项'" readonly="readonly"> -
							<input id="product_vr_sku5" class="easyui-textbox" style="width:100px;" data-options="required:true,prompt:'必填项',validType:['skuLength[4]']">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn form-item">
							<div class="text">商品名称</div>
							<input id="product_vr_skuName" name="skuName" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['noEmpty','length[1,32]']">
						</div>
						<div class="form-cloumn form-item">
							<div class="text">商品类别</div>
							<input id="product_vr_category" name="category" class="easyui-combobox" data-options="required:true,prompt:'必填项',panelHeight:'auto',editable:false,valueField:'code',textField:'name'">
						</div>
						<div class="form-cloumn form-item">
							<div class="text">链接地址(与二维码二选一)</div>
							<input id="product_vr_url" name="virtualUrl" class="easyui-textbox" data-options="prompt:'与二维码二选一',validType:'url'">
						</div>
					</div>
					<div class="form-row">
						<div class="form-cloumn form-item">
							<div class="text">图文描述</div>
							<div>
								<textarea id="product_vr_description" name="description" style="width:669px;height:300px;"></textarea>
							</div>
						</div>
					</div>
				</div>
			</div>
		</form>
		<div id="productVrBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveProVr()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeWin('winProductVR')">取消</a>
		</div>
	</div>
</body>
</html>