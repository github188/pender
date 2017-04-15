<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>设备管理</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/base64.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/webuploader/webuploader.css" />
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/webuploader/webuploader.min.js"></script>
<script type="text/javascript">
	var deviceTypes = [];
	
	$(function() {
		$('#deviceGrid').datagrid({
			url:'${pageContext.request.contextPath}/platform/device/find.json'
		});
		var deviceTypesQuery = [{'code':'', 'name':'所有'}];
		var cache = getAllSysTypes();
		for (var i = 0; i < cache.length; i++) {
			if (cache[i].type == 'DEVICE_TYPE2') {
				deviceTypes.push(cache[i]);
				deviceTypesQuery.push({'code':cache[i].code, 'name':cache[i].name});
			}
		}
		$('#q_device_type').combobox({data:deviceTypesQuery}); 
		
		$('#frmIssueUpload').attr('src', '${pageContext.request.contextPath}/free/uploadExcel.html');
	});

	//formatter
	function deviceTypeOrigin(value) {
		for (var i = 0; i < deviceTypes.length; i++)
            if (deviceTypes[i].code == value)
                return deviceTypes[i].name;
        return value;
	}
	//formatter
	function bindState(value) {
		return value === 1 ? "绑定" : "未绑定";
	}
	
	function delDevices() {
		var rows = $('#deviceGrid').datagrid('getSelections');
		if (rows.length == 0) {
			infoMsg('请选择需要删除的设备信息！');
		} else {
			confirmMsg('您确定要删除该所选设备信息吗？', doDelDevices, [ rows ]);
		}
	}
	
	function doDelDevices(rows) {
		var ids = [];
		for (var i = 0; i < rows.length; i++) {
			ids.push(rows[i].id);
		}
		$.ajax({
			url : '${pageContext.request.contextPath}/platform/device/delete.json',
			data : $.param({
				'ids' : ids
			}, true),
			info : '所选设备信息删除成功！',
			task : function(data, statusText, xhr) {
				queryData('deviceGrid', 'deviceQueryForm');
			}
		});
	}
	
	// 导入设备点击事件
	function importDevice() {
		openDialog('winImportDevice');
	}
	
	// 导入设备excel
	function saveImportDevice() {
		var key = new Date().getTime();
	    if ($('#frmIssueUpload')[0].contentWindow.queueExcels.length == 0) {
	    	infoMsg('请选择需要导入的excel文件！');
	    	return;
	    }
        if ($('#fomImportDevice').form('enableValidation').form('validate'))
            $('#frmIssueUpload')[0].contentWindow.uploadExcels({key:key,module:_fileType.excel});
	}
	
	function initIssueUploader() {
	    if ($('#frmIssueUpload').attr('src')) {
	        $('#frmIssueUpload')[0].contentWindow.initExcelUploader(function(key, info) {
	        	doSaveIssueExcel(key, '</br>' + info);
	        }, _token, 1);
	    }
	}
	
	function doSaveIssueExcel(key, info) {
	    var row = $('#fomImportDevice').getValues();
	    row.key = key;
	    var delExcelIds = $('#frmIssueUpload')[0].contentWindow.delExcelIds;
	    var excelIds = new Array();
	    if (delExcelIds) {
	        for(var i=0; i < delExcelIds.length; i++){
	            if (delExcelIds[i] != '') {
	                excelIds.push(delExcelIds[i]);
	            }
	        }
	    }
	    row.fileIds = excelIds;
	    $.ajax({
	        url:'${pageContext.request.contextPath}/platform/device/importDevices.json',
	        data:$.param(row, true),
	        info:'设备信息导入成功！' + (info == undefined ? '' : info),
	        task:function(data, statusText, xhr) {
	        	closeDialog('winImportDevice');
	        	$('#deviceGrid').datagrid('reload');
	        }
	    });
	}
	
	function closeWinImportDevice() {
		$('#frmIssueUpload')[0].contentWindow.closeExcelUploader();
	}
</script>
</head>
<body class="easyui-layout" >
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="deviceQueryForm" class="search-form">
			<div class="form-item">
				<div class="text">设备组号</div>
				<div class="input">
					<input id="q_device_factoryDevNo"  name="factoryDevNo" class="easyui-textbox">
				</div>
			</div>
			<div class="form-item">
				<div class="text">设备类型</div>
				<div class="input">
					<input id="q_device_type" name="type" class="easyui-combobox" data-options="valueField:'code',textField:'name',editable:false,panelHeight:'auto'">
				</div>
			</div>
			<div class="form-item">
				<div class="text">设备状态</div>
	            <div class="input">
	                <select class="easyui-combobox" data-options="panelHeight:'auto', editable:false" name="bindState">
	                    <option value="" selected="selected">请选择</option>
	                    <option value="1">绑定</option>
	                    <option value="0">未绑定</option>
	                </select>
	            </div>
            </div>
		</form>
		<sec:authorize access="find">
			<div class="search-btn" onclick="queryData('deviceGrid','deviceQueryForm')">查询</div>
			<div class="search-btn" onclick="resetForm('deviceQueryForm')">重置</div>
		</sec:authorize>
		<sec:authorize access="import">
			<div class="search-btn" onclick="importDevice()" style="width:100px;">导入设备</div>
		</sec:authorize>
	</div>
	<div data-options="region:'center',border:false,split:true,headerCls:'list-head',tools:'#deviceOpt'" title="设备信息">
		<div id="deviceOpt">
			<sec:authorize access="delete"><a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除设备信息'" onclick="delDevices()"></a></sec:authorize>
		</div>
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<table id="deviceGrid" data-options="nowrap:false,striped:true,fit:true,fitColumns:true,idField:'id'">
				<thead>
					<tr>
						<th data-options="checkbox:true,field:'',width:20,align:'center'"></th>
						<th data-options="field:'factoryDevNo',width:120,align:'center'">设备组号</th>
						<th data-options="field:'type',width:230,formatter:deviceTypeOrigin,align:'center'">设备类型</th>
						<th data-options="field:'factoryNo',width:120,align:'center'">出厂编号</th>
						<th data-options="field:'manufacturer',width:80,align:'center'">生产厂商</th>
						<th data-options="field:'orgName',width:150,align:'center'">所属人</th>
						<th data-options="field:'aisleCount',width:50,align:'center'">货道数量</th>
						<th data-options="field:'address',width:230,align:'center'">设备地址</th>
						<th data-options="field:'bindState',width:230,formatter:bindState,align:'center'">设备状态</th>
						<th data-options="field:'version',width:50,align:'center'">版本号</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="winImportDevice" class="easyui-dialog" data-options="closed:true,title:'导入设备', onClose:closeWinImportDevice" style="width:600px;height:260px;padding:5px">
		<div class="easyui-layout" data-options="fit:true">
			<div data-options="region:'center',border:false" style="padding:10px 10px;background:#fff;border:1px solid #ccc;">
				<form id="fomImportDevice" method="post">
                    <table>
                        <tr>
                            <td style="padding-left:50px;" colspan="4"  style="margin:0px;padding:0px;padding-left:23px;"><table style="margin:0px;padding:0px;cellpadding:0;cellspacing:0;border:#d9ead3 solid 1px;"><tr>
                            <td><iframe id="frmIssueUpload" width="380px" height="140px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" style="float:right"onload="initIssueUploader()"></iframe></td></tr></table></td>
                        </tr>
                    </table>
				</form>
			</div>
			<div data-options="region:'south',border:false" style="height:38px;text-align:right;padding:5px 0;">
				<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveImportDevice();">导入</a>
				<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winImportDevice');">取消</a>
			</div>
		</div>
	</div>
</body>
</html>