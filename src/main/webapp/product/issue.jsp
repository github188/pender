<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>商品發佈</title>
<%@ include file="/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/kindeditor/themes/default/default.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/kindeditor/kindeditor-min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/kindeditor/lang/zh_TW.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>

<script type="text/javascript">

$.extend($.fn.validatebox.defaults.rules,{
    skuLength: {
        validator: function(value, param) {
            return value.length == param[0];
        },
        message: '请输入{0}位的数字！'  
    }
});

var modelNames = {0:'propertyName',1:'price',2:'stock'}, updateProduct, issueEditor;
var key = 0;
$(function(){
	var orgType='${orgType}';
	if(orgType){
		if(orgType==1){
		}else if(orgType==2){
		    $("#issue_price_one").remove();
			$("#issue_price_one2").remove();
			$("#th_price_one").remove();
		}else if(orgType==3){
		    $("#issue_price_one").remove();
			$("#issue_price_one2").remove();
			$("#issue_price_two").remove();
			$("#issue_price_two2").remove();
			$("#th_price_one").remove();
			$("#th_price_two").remove();
		}
	}
})
$(function() {
	$('#saleGrid').datagrid({
		url:'${pageContext.request.contextPath}/product/issue/find.json',
		view: detailview,
        detailFormatter:function(index, row) {
        	var content = '<table style="margin:0px;padding:5px;cellpadding:0;cellspacing:0;font-size:13px;border:0px;background:#FFFFE0"><tr><td colspan="10" style="border:0px;">';
        	content = content + '<table style="margin:0px;padding:0px;cellpadding:0;cellspacing:0;border:0px;width:100%;"><tr>';
        	if (row.images) {
            	var rows = row.images.split(';');
            	for (var i = 0; i < rows.length; i++) {
            		var vals = rows[i].split(',');
            		var path = getFileUrl(vals[3]);
            		content = content + '<td style="border:0px;width:150px;"><img src="' +path + '" style="width:150px;height:150px;cursor:pointer;" name=' + vals[1] + ' onclick="previewSaleImage(event)"></td>';
            	}        		
        	} else {
        		content = content + '<td style="border:0px;"></td>';
        	}
        	content = content + '<td align="right" valign="top" style="border:0px;font-weight:bold"><a href="javascript:showDescription(' + index + ')">圖文描述</a></td>';
        	content = content + '</tr></table></td></tr><tr><td style="font-weight:bold;border:0px;width:70px;">SKU:</td><td style="border:0px;width:200px;">' + row.sku + '</td>';
//         	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:60px;">長(mm):</td><td style="border:0px;width:80px;">' + getValue(row.length) + '</td>';
//         	content = content + '<td style="font-weight:bold;border:0px;">寬(mm):</td><td style="border:0px;width:80px;">' + getValue(row.width) + '</td>';
//         	content = content + '<td style="font-weight:bold;border:0px;">高(mm):</td><td style="border:0px;width:80px;">' + getValue(row.height) + '</td>';
        	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:70px;">商品名稱:</td><td style="border:0px;width:300px;">' + getValue(row.skuName) + '</td>';
//         	content = content + '</tr><tr><td style="font-weight:bold;border:0px;width:60px;">備案名稱:</td><td style="border:0px;width:300px;">' + getValue(row.registName) + '</td>';
//         	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:60px;">副標題:</td><td colspan="5" style="border:0px;width:300px;">' + getValue(row.title) + '</td>';
//         	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:60px;">內部編碼:</td><td style="border:0px;width:300px;">' + getValue(row.code) + '</td>';
//         	content = content + '</tr><tr><td style="font-weight:bold;border:0px;width:60px;">幣別:</td><td style="border:0px;width:300px;">' + getValue(row.currency) + '</td>';
        	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:70px;">價格:</td><td style="border:0px;width:300px;">' + getValue(row.price) + '</td>';
        	content = content + '</tr><tr><td style="font-weight:bold;border:0px;width:70px;">商品庫存:</td><td style="border:0px;width:300px;">' + getValue(row.stock) + '</td>';
//         	content = content + '</tr><tr><td style="font-weight:bold;border:0px;width:60px;">限購數量:</td><td style="border:0px;width:300px;">' + getValue(row.buyLimit) + '</td>';
        	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:70px;">净含量:</td><td style="border:0px;width:300px;">' + getValue(row.weight) + '</td>';
        	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:70px;">品牌:</td><td style="border:0px;width:300px;">' + getValue(row.brand) + '</td>';
        	content = content + '</tr><tr><td style="font-weight:bold;border:0px;width:70px;">原產地:</td><td style="border:0px;width:300px;">' + getValue(row.origin) + '</td>';
        	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:70px;">保質期:</td><td style="border:0px;width:300px;">' + getValue(row.expiration) + '</td>';
//         	content = content + '<td style="padding-left:30px;font-weight:bold;border:0px;width:60px;">配送大陸:</td><td style="border:0px;width:300px;">' + getValue(row.area) + '</td>';
//         	content = content + '</tr><tr><td style="font-weight:bold;border:0px;width:60px;">成分:</td><td style="border:0px;width:300px;">' + getValue(row.ingredient) + '</td>';
//         	content = content + '</tr><tr><td style="padding-left:30px;font-weight:bold;border:0px;width:60px;">備註:</td><td colspan="7" style="border:0px;width:660px;">' + getValue(row.remark) + '</td>';
//         	content = content + '</tr><tr><td colspan="10" style="font-weight:bold;border:0px;">商品規格:</td><tr><td colspan="10" style="border:0px;">';
        	content = content + '</tr><td colspan="10" style="border:0px;"><table style="margin:0px;padding:0px;cellpadding:0;cellspacing:0;border:0px;">';
        	if (row.models) {
            	var rows = row.models.split(';');
            	for (var i = 0; i < rows.length; i++) {
            		content = content + '<tr>';
            		var cols = rows[i].split(',');
            		for (var j = 1; j < cols.length; j++) {
            			if (j == 1) {
            				content = content + '<td style="padding-left:60px;font-weight:bold;color:#15428B;border:0px;width:40px;">' + modelTitles[j - 1] + ':</td>';
            			} else {
            				content = content + '<td style="padding-left:20px;font-weight:bold;color:#15428B;border:0px;width:40px;">' + modelTitles[j - 1] + ':</td>';
            			}
            			content = content + '<td style="border:0px;width:160px;">' + cols[j] + '</td>';
            		}
            		content = content + '</tr>';
            	}        		
        	} else {
        		content = content + '<tr><td style="border:0px;"></td></tr>';
        	}
        	content = content + '</tr></table></td></tr></table>';
            return content;
        },
        onExpandRow: function(index, row) {
            //var ddv = $(this).datagrid('getRowDetail',index).find('div.ddv');
            $('#saleGrid').datagrid('fixDetailRowHeight', index);
        }
	});
	numberTextBox('q_sale_code');
	saleEditor = KindEditor.create('#txtDescription');
});
$(function() {
	var categories = getAllCategories();
	$('#issue_category1').combobox('loadData', categories);
	$('#issue_sku4').textbox('setValue', getCurUser().orgCode);
	$('#issue_buyLimit').textbox('setValue', 0);
	$('#fomIssue').form('disableValidation');
	$('#frmIssueUpload').attr('src', '${pageContext.request.contextPath}/free/uploadImage.html');
	numberTextBox('issue_sku5');
	numberTextBox('issue_code');
	var currency, product = '${_product}', description = '${_description}';
	var currencyCode = getCurUser().currency;
	currencyCode = currencyCode ? currencyCode : 'CNY';
	currency = getCurrencyByCode(currencyCode);
// 	if (currency) {
// 		$('#issue_currency').textbox('setValue', currency.code);
// 		$('#issue_currency').textbox('setText', currency.name);
// 	}
	issueEditor = KindEditor.create('#issue_description', {
    	allowUpload:true,
    	dir: 'image',
    	filePostName:'files',
    	extraFileUploadParams:{module:_fileType.productDesc,key:new Date().getTime()},
    	uploadJson: '${pageContext.request.contextPath}/session/data/saveUploadFile.json'
    });
});
function initIssueUploader() {
	if ($('#frmIssueUpload').attr('src')) {
		$('#frmIssueUpload')[0].contentWindow.initImageUploader(function(key, info) {
			doSaveIssueProduct(key, '</br>' + info);			
		}, _token, 1);
	}
}

function loadProductCategory1(record) {
	var newValue = record.code;
	$('#issue_sku1').textbox('setValue', newValue);
	$('#issue_sku2').textbox('setValue', '');
	$('#issue_sku3').textbox('setValue', '');
	$('#issue_barcode').textbox('setValue', newValue + '-');
	getCategoriesByCache(function(data, el) {
		$('#issue_category2').combobox('loadData', data);
		if (data.length == 0) {
			$('#issue_category2').combobox('setValue', '');
			$('#issue_category2').combobox('loadData', []);
			$('#issue_category3').combobox('setValue', '');
		} else {
			if (updateProduct == undefined) {
				$('#issue_category2').combobox('setValue', data[0].code);
				loadProductCategory2(data[0]);
			}
		}
	}, record.id);
}
function loadProductCategory2(record) {
	var newValue = record.code;
	$('#issue_sku2').textbox('setValue', newValue);
	getCategoriesByCache(function(data, el) {
		$('#issue_category3').combobox('loadData', data);
		if (data.length == 0) {
			$('#issue_category3').combobox('setValue', '');
		} else {
			if (updateProduct == undefined) {
				$('#issue_category3').combobox('setValue', data[0].code);
				loadProductCategory3(data[0]);
			}
		}
	}, record.id);
}
function loadProductCategory3(record) {
	var newValue = record.code;
	$('#issue_sku3').textbox('setValue', newValue);
}
function loadProductSku(row, ary, index) {
	if (ary == undefined)
		ary = row.sku.split('-');
	if (index == undefined)
		$('#issue_category1').combobox('select', ary[0]);
	if ($('#issue_category2').combobox('getData').length == 0) {
		setTimeout(function(){loadProductSku(row, ary, 1)}, 500);
	} else {
		$('#issue_category2').combobox('select', ary[1]);
		if ($('#issue_category3').combobox('getData').length == 0) {
			setTimeout(function(){loadProductSku(row, ary, 2)}, 500);
		} else {		
			$('#issue_category3').combobox('select', ary[2]);
			$('#issue_sku5').textbox('setValue', ary[4]);
		}
	}
}
function loadProductImage(product) {
	if (product.images) {
		if ($('#frmIssueUpload')[0].contentWindow.loadUploadImage == undefined) {
			setTimeout(function(){loadProductImage(product)}, 500);
		} else {
			var fileIds = [], fileNames = [], realPaths = [], fileYPIds = [], fileYPNames = [], realYPPaths = [];
			var rows = product.images.split(';');
			for (var i = 0; i < rows.length; i++) {
				var vals = rows[i].split(',');
				if (vals[2] == _fileType.product) {
					fileIds.push(vals[0]);
					fileNames.push(vals[1]);
					realPaths.push(vals[3]);
				} else {
					fileYPIds.push(vals[0]);
					fileYPNames.push(vals[1]);
					realYPPaths.push(vals[3]);
				}
			}
			$('#frmIssueUpload')[0].contentWindow.loadUploadImage(fileIds, fileNames, realPaths);
		}
	}
}
function loadProductModel(product) {
	if (product.models) {
		var rows = product.models.split(';');
		for (var i = 0; i < rows.length; i++) {
			var element = '<tr><td width="30px" style="padding-left:60px;">名稱</td><td><input class="easyui-textbox" style="width:160px" data-options="required:true,prompt:\'必填项\',validType:[\'remodel\']"></td>';
			element = element +	'<td width="30px" style="padding-left:20px;">價格</td><td><input class="easyui-textbox" style="width:60px" data-options="required:true,prompt:\'必填项\',onChange:changeIssueModelPrice"></td>';
			element = element +	'<td width="30px" style="padding-left:20px;">庫存</td><td><input class="easyui-textbox" style="width:60px" data-options="required:true,prompt:\'必填项\',onChange:changeIssueModelStock"></td>';		
			element = element +	'<td><a class="easyui-linkbutton" data-options="iconCls:\'icon-remove\'" href="javascript:void(0)" onclick="delProductModel(event)">删除</a></td></tr>';
			$('#divProductModel').append($(element));
		}
		$.parser.parse('#divProductModel');
		var trs = $('#divProductModel').find('tr');
		for (var i = 0; i < rows.length; i++) {			
			var cols = $(trs[i]).find("input[class='easyui-textbox textbox-f']");
			var vals = rows[i].split(',');
			for (var j = 0; j < cols.length; j++)
				$(cols[j]).textbox('setValue', vals[j + 1]);
		}
	}
}
function changeIssueSkuName(newValue, oldValue) {
	$('#issue_registName').textbox('setValue', newValue);
}
function changeIssueModelPrice(newValue, oldValue) {
	resetIssuePrice();
}
function changeIssueModelStock(newValue, oldValue) {
	resetIssuePrice();
}
function resetIssuePrice() {
	var model = {price:[], stock:0};
	var rows = $('#divProductModel').find('tr');
	for (var i = 0; i < rows.length; i++) {
		var cols = $(rows[i]).find("input[type='hidden']");
		for (var j = 0; j < cols.length; j++) {
			if ($(cols[j]).val() && (model[modelNames[j]] || model[modelNames[j]] == 0)) {
				if (modelNames[j] == 'stock') {
					model[modelNames[j]] += parseInt($(cols[j]).val());
				} else {
					model[modelNames[j]].push($(cols[j]).val());
				}
			}
		}
	}
	if (model.price.length != 0) {
		var ary = model.price.sort(function(x, y) { 
			if (parseInt(x) > parseInt(y))
				return 1;
			return -1;
		});
		$('#issue_price').textbox('setValue', ary[0]);
		$('#issue_priceMax').val(ary[ary.length - 1]);
	}
	if (model.stock != 0)
		$('#issue_stock').textbox('setValue', model.stock);
}
function addProductModel() {
	if ($('#fomIssue').form('enableValidation').form('validate')) {
		var element = '<tr><td width="30px" style="padding-left:60px;">名稱</td><td><input class="easyui-textbox" style="width:160px" data-options="required:true,prompt:\'必填项\',validType:[\'remodel\']"></td>';
		element = element +	'<td width="30px" style="padding-left:20px;">價格</td><td><input class="easyui-textbox" style="width:60px" data-options="required:true,prompt:\'必填项\',onChange:changeIssueModelPrice"></td>';
		element = element +	'<td width="30px" style="padding-left:20px;">庫存</td><td><input class="easyui-textbox" style="width:60px" data-options="required:true,prompt:\'必填项\',onChange:changeIssueModelStock"></td>';		
		element = element +	'<td><a class="easyui-linkbutton" data-options="iconCls:\'icon-remove\'" href="javascript:void(0)" onclick="delProductModel(event)">删除</a></td></tr>';
		$('#divProductModel').append($(element));
		$.parser.parse('#divProductModel');
	}
}
function delProductModel(e) {
	$(e.target).closest('tr').remove();
}
function saveIssueProduct() {
	if ($('#frmIssueUpload')[0].contentWindow.queueImages.length == 0) {
		if ($('#fomIssue').form('enableValidation').form('validate'))
			doSaveIssueProduct(key);
	} else {
		if ($('#fomIssue').form('enableValidation').form('validate')) {
			if ($('#frmIssueUpload')[0].contentWindow.queueImages.length != 0) {
				$('#frmIssueUpload')[0].contentWindow.uploadImages({key:key,module:_fileType.product});
			} else {
				
			}
		}
	}
}
function doSaveIssueProduct(key, info) {
	var models = [], sumStock = 0;

	if ($('#issue_priceMax').val() == '')
		$('#issue_priceMax').val($('#issue_price').textbox('getValue'));
	issueEditor.sync();
	var row = $('#fomIssue').getValues();
	row.sku = $('#issue_sku1').textbox('getValue') + '-' + $('#issue_sku2').textbox('getValue') + '-' + $('#issue_sku3').textbox('getValue') + '-' + $('#issue_sku4').textbox('getValue') + '-' + $('#issue_sku5').textbox('getValue');
	row.productModels = models;
	row.key = key;
// 	row.fileIds = $('#frmIssueUpload')[0].contentWindow.delImageIds;
	var delImageIds = $('#frmIssueUpload')[0].contentWindow.delImageIds;
	var imageIds = new Array();
	if (delImageIds) {
		for(var i=0; i < delImageIds.length; i++){
			if (delImageIds[i] != '') {
				imageIds.push(delImageIds[i]);
			}
		}
	}
	row.fileIds = imageIds;
	$.ajax({
		url:'${pageContext.request.contextPath}/product/issue/save.json',
		data:$.param(row, true),
		info:'保存商品信息成功！' + (info == undefined ? '' : info),
		task:function(data, statusText, xhr) {
// 			if (row.id) {
// 				parent.closeCurrentTab();
// 			} 
// 			else {
				resetIssueForm();
				$("#saleGrid").datagrid("reload");
// 			}
		}
	});
}
function resetIssueForm() {
// 	var code = $('#issue_currency').textbox('getValue');
// 	var name = $('#issue_currency').textbox('getText');
	resetForm('fomIssue');
	$("input[type=hidden]").val("");
	issueEditor.html('');
	$('#fomIssue').form('disableValidation');
// 	$('#issue_currency').textbox('setValue', code);
// 	$('#issue_currency').textbox('setText', name);
	$('#issue_sku4').textbox('setValue', getCurUser().orgCode);
	$('#issue_buyLimit').textbox('setValue', 0);
	$('#frmIssueUpload')[0].contentWindow.closeImageUploader();
// 	$('#frmIssueYPUpload')[0].contentWindow.closeImageUploader();
	var rows = $('#divProductModel').find('tr');
	for (var i = 0; i < rows.length; i++)
		$(rows[i]).remove();
}

function showApplyImage(value, row, index) {
	if (value) {
		var rows = value.split(';');
		var path = getFileUrl(rows[0].split(',')[3]);
		return '<img src="' + path + '" style="width:60px;height:60px;">';
	}
	return "";
}
function showApplyModel(value, row, index) {
	if (value) {
		var data = '<table>';
		var rows = value.split(';');
		for (var i = 0; i < rows.length; i++) {
			var cols = rows[i].split(',');
			if (i != 0)
				data = data + '<tr>';
			for (var j = 1; j < cols.length; j++) {
				if (j == 1) {
					data = data + '<td style="border:0px;">' + cols[j] + '</td>';
				} else {
					data = data + '<td style="padding-left:20px;border:0px;align:right">' + cols[j] + '</td>';
				}
			}
			data = data + '</tr>';
		}
		return data + '</table>';
	}
	return value;
}
function showDescription(index) {
	openDialog('winDescription', '圖文描述');
	var row = $('#saleGrid').datagrid('getRows')[index];
	if (row)
		saleEditor.html(getValue(row.description));
}
function showApplyArea(value, row, index) {
	return value == 1 ? '是' : '否';
}

function addProduct() {
	key = new Date().getTime();
	resetIssueForm();
	openDialog('winProduct', '商品发布');
}

function updateProduct(row) {
	if (!row)
		row = $('#saleGrid').datagrid('getSelected');
	if (row) {
		key = new Date().getTime();
		openDialog('winProduct', '修改商品信息');
		loadProductValue(row);
		var currency, product = '${_product}', description = '${_description}';
	} else {
		infoMsg('请选择需要修改的商品信息！');
	}
}

function loadProductValue(row){
	var product = row;
	if (product) {
		$('#fomIssue').form('load', product);
		loadProductSku(product);
		loadProductImage(product);
		issueEditor.html(product.description);
	//	loadProductModel(product);
		currency = getCurrencyByCode(product.currency);
	} else {
		var currencyCode = getCurUser().currency;
		currencyCode = currencyCode ? currencyCode : 'CNY';
		currency = getCurrencyByCode(currencyCode);
	}
}


function loadProductImage(row) {
	showImage(row.images, '#frmIssueUpload');
	
}
function showImage(image, id) {
	var fileIds = [], fileNames = [], realPaths = [];
	var vals = image.split(';')[0].split(',');
	fileIds.push(vals[0]);
	fileNames.push(vals[1]);
	realPaths.push(vals[3]);
	$(id)[0].contentWindow.loadUploadImage(fileIds, fileNames, realPaths);
}

function deleProduct(){
	var rows = $('#saleGrid').datagrid('getSelections');
	if (rows.length == 0) {
		infoMsg('请选择需要删除的商品！');
	} else {
		confirmMsg('您确定要删除该商品吗?', doDeleProoduct, [ rows ]);
	}
}

function doDeleProoduct(rows){
	var ids = [];
	for (var i = 0; i < rows.length; i++)
		ids.push(rows[i].id);
	       $.ajax({
				  url : '${pageContext.request.contextPath}/product/issue/delete.json',
				  data : $.param({
					'ids' : ids
				  }, true),
				  info : '所选删除成功！',
				  task : function(data, statusText, xhr) {
					queryData('saleGrid', 'applyQueryForm');
				}
			});
}

function closeWinProduct() {
    $('#frmIssueUpload')[0].contentWindow.closeImageUploader();
    closeDialog('winProduct');
}

</script>
</head>
<body class="easyui-layout">

<div id="bannerOpt">
	<sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'商品发布'" onclick="addProduct()"></a></sec:authorize>
	<sec:authorize access="update,save"><a href="javascript:void(0)" class="icon-edit easyui-tooltip" data-options="content:'修改商品'" onclick="updateProduct()"></a></sec:authorize>
	<sec:authorize access="delete"><a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除'" onclick="deleProduct()"></a></sec:authorize>
</div>

<div data-options="region:'north',border:false"><table><tr><td>
	<form id="applyQueryForm">
	<table style="border:1px solid #ccc;"><tr>
		<td style="padding:5px 5px 5px 10px;">SKU</td><td style="padding:5px 0px 5px 0px;"><div id="q_apply_sku" name="sku" class="easyui-textbox" style="width:180px"></div></td>
		<td style="padding:5px 5px 5px 15px;">商品名称</td><td style="padding:5px 0px 5px 0px;"><div id="q_apply_skuName" name="skuName" class="easyui-textbox" style="width:150px"></div></td>
	</tr></table></form></td><sec:authorize access="find">
		<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-search'" href="javascript:void(0)" onclick="queryData('saleGrid','applyQueryForm')">查询</a></td>
		<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-no'" href="javascript:void(0)" onclick="resetForm('applyQueryForm')">重置</a></td>
	</sec:authorize></tr>
</table></div>
<div data-options="region:'center',border:false"><table id="saleGrid" data-options="nowrap:false,striped:true,fit:true,border:false,idField:'id',tools:'#bannerOpt'" title="商品列表">
	<thead>
		<tr>
			<th data-options="field:'ck',checkbox:true,width:20"></th>
			<th data-options="field:'images',width:100,align:'center',formatter:showApplyImage">商品图片</th>
			<th data-options="field:'sku',width:200">SKU</th>
			<th data-options="field:'skuName',width:250">商品名称</th>
			<th data-options="field:'brand',width:180">品牌</th>
		    <th data-options="field:'origin',width:150">原产地</th>
		    <th id="th_price_one" data-options="field:'priceOne',width:150">一级渠道价</th>
		    <th id="th_price_two" data-options="field:'priceTwo',width:150">二级渠道价</th>
		    <th data-options="field:'price',width:150">零售价</th>
		    <th data-options="field:'stock',width:150">商品库存</th>
		    <th data-options="field:'buyLimit',width:100">限购数量</th>
		</tr>
	</thead>
</table></div>

<div id="winProduct" class="easyui-dialog" data-options="closed:true, onClose:closeWinProduct" style="width:1000px;height:600px;padding:5px">
		<div class="easyui-layout" data-options="fit:true">
			<div data-options="region:'center',border:false" style="padding:10px 10px;background:#fff;border:1px solid #ccc;">
<form id="fomIssue" method="post">
<input type="hidden" id="issue_id" name="id">
<input type="hidden" id="issue_stockHold" name="stockHold">
<input type="hidden" id="issue_priceMax" name="priceMax">
<input type="hidden" id="issue_offTime" name="offTime">
<input type="hidden" id="issue_onTime" name="onTime">
<input type="hidden" id="issue_combo" name="combo">
<input type="hidden" id="issue_state" name="state">
<input type="hidden" id="issue_taxRate" name="taxRate">
<input type="hidden" id="issue_orgId" name="orgId">
<input type="hidden" id="issue_createUser" name="createUser">
<input type="hidden" id="issue_createTime" name="createTime">
<input type="hidden" id="issue_ingredient" name="ingredient">
<input type="hidden" id="issue_firm" name="firm">
<input type="hidden" id="issue_isUpload" name="isUpload">
<input type="hidden" id="issue_vStatus" name="vStatus">
<input type="hidden" id="issue_sales" name="sales">
<table style="width:870px;margin-top:10px;margin-right:auto;margin-bottom:10px;margin-left:auto;padding:5px;cellpadding:0;cellspacing:0;font-size:13px;border:#95B8E7 solid 1px;">
	<tr>
		<td style="padding-left:50px;" colspan="4"  style="margin:0px;padding:0px;padding-left:23px;"><table style="margin:0px;padding:0px;cellpadding:0;cellspacing:0;border:#d9ead3 solid 1px;"><tr>
		<td><iframe id="frmIssueUpload" width="280px" height="180px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" style="float:right"onload="initIssueUploader()"></iframe></td></tr></table></td>
	</tr>

	<tr>
		<td width="60px" style="padding-left:50px;">类目</td><td colspan="3"  style="margin:0px;padding:0px;"><table cellspacing="0" cellpadding="0"><tr>
		<td style="width:203px;"><input id="issue_category1" class="easyui-combobox" style="width:200px;" data-options="editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory1"></td>
		<td style="width:203px;"><input id="issue_category2" class="easyui-combobox" style="width:200px;" data-options="editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory2"></td>
		<td style="width:200px;"><input id="issue_category3" class="easyui-combobox" style="width:200px;" data-options="editable:false,valueField:'code',textField:'name',onSelect:loadProductCategory3"></td>
		<td></td></tr></table></td>
	</tr>

	<tr>
		<td width="60px" style="padding-left:50px;">SKU</td><td colspan="3"  style="margin:0px;padding:0px;"><table cellspacing="0" cellpadding="0"><tr>
		<td><input id="issue_sku1" class="easyui-textbox" class="easyui-textbox" style="width:40px;" data-options="required:true,prompt:'必填项'" readonly="readonly"></td><td>-</td>
		<td><input id="issue_sku2" class="easyui-textbox" class="easyui-textbox" style="width:40px;" data-options="required:true,prompt:'必填项'" readonly="readonly"></td><td>-</td>
		<td><input id="issue_sku3" class="easyui-textbox" class="easyui-textbox" style="width:40px;" data-options="required:true,prompt:'必填项'" readonly="readonly"></td><td>-</td>
		<td><input id="issue_sku4" class="easyui-textbox" class="easyui-textbox" style="width:60px;" data-options="required:true,prompt:'必填项'" readonly="readonly"></td><td>-</td>
		<td><input id="issue_sku5" class="easyui-textbox" class="easyui-textbox" style="width:70px;" data-options="required:true,prompt:'必填项',validType:['skuLength[4]']"></td>
       </tr></table></td>
	</tr>
	<tr>
		<td width="60px" style="padding-left:50px;">商品名称</td><td style="width:300px;"><input id="issue_skuName" name="skuName" class="easyui-textbox" style="width:300px" data-options="required:true,prompt:'必填项',onChange:changeIssueSkuName"></td>
	</tr>
	<tr>
	   <td id="issue_price_one" width="70px" style="padding-left:50px;">一级渠道价</td><td id="issue_price_one2"><input id="issue_priceOne" name="priceOne" class="easyui-numberbox" precision="2" max="99999.99" size="8" maxlength="8" style="width:300px"></td>
       <td id="issue_price_two" width="70px" style="padding-left:30px;">二级渠道价</td><td id="issue_price_two2" style="width:300px;"><input id="issue_price_two" name="priceTwo" class="easyui-numberbox" precision="2" max="99999.99" size="8" maxlength="8" style="width:300px"></td>
	</tr>
	<tr>
		<td width="60px" style="padding-left:50px;">零售价</td><td><input id="issue_price" name="price" class="easyui-numberbox" precision="2" max="99999.99" size="8" maxlength="8" precision="0" style="width:300px" data-options="required:true,prompt:'必填项'"></td>
		<td width="60px" style="padding-left:30px;">商品库存</td><td style="width:300px;"><input id="issue_stock" name="stock" class="easyui-numberbox" precision="0" style="width:300px" data-options="required:true,prompt:'必填项'"></td>
	</tr>
	<tr>
		<td width="60px" style="padding-left:50px;">净含量</td><td style="width:300px;"><input id="issue_weight" name="weight" class="easyui-numberbox" precision="2" style="width:300px" data-options="required:true,prompt:'必填项'"></td>
		<td width="60px" style="padding-left:30px;">品牌</td><td><input id="issue_brand" name="brand" class="easyui-textbox" style="width:300px"></td>
	</tr>	
	<tr>
		<td width="60px" style="padding-left:50px;">原产地</td><td style="width:300px;"><input id="issue_origin" name="origin" class="easyui-textbox" style="width:300px"></td>
		<td width="60px" style="padding-left:30px;">保质期</td><td><input id="issue_expiration" name="expiration" class="easyui-textbox" style="width:300px" data-options="prompt:'单位：月'"></td>
	</tr>	
	
 	<tr> 
 		<td width="60px" style="padding-left:50px;">商品编码</td><td style="width:300px;"><input id="issue_code" name="code" class="easyui-textbox" style="width:300px" data-options="required:true,prompt:'必填项'"></td>
 		<td width="60px" style="padding-left:30px;">限购数量</td><td style="width:300px;"><input name="buyLimit" class="easyui-textbox" style="width:300px"></td>
 	</tr>	 

	<tr>
		<td width="60px" style="padding-left:50px;">图文描述</td><td colspan="3"><textarea id="issue_description" name="description" style="width:697px;height:300px;"></textarea></td>
	</tr>
	<tr>
		<td colspan="4"  style="margin:0px;padding:0px;"><hr color="#95B8E7"></td>
	</tr>
	<tr>
		<td colspan="4"  style="margin:0px;padding:0px;"><table style="margin:0px;padding:0px;cellpadding:0;cellspacing:0;"><tr>
		<td width="810px" align="right">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveIssueProduct()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-no'" href="javascript:void(0)" onclick="resetIssueForm()">重置</a>
		</td></tr></table></td>
	</tr>
</table></form>
</div>
</div>
</div>
<div id="winDescription" class="easyui-dialog" data-options="closed:true" style="width:815px;height:635px;padding:5px">
	<div class="easyui-layout" data-options="fit:true">
	<div data-options="region:'center',border:false" style="padding:10px 10px;background:#fff;border:1px solid #ccc;">
		<textarea id="txtDescription" style="width:760px;height:560px;"></textarea>
	</div></div>
</div>
</body>
</html>