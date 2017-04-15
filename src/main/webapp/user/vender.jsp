<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>城市合伙人</title>
<%@ include file="/common.jsp"%>
<style>
#winBindDevice .panel-body-noborder, #winUnBindDevice .panel-body-noborder {
	height: 310px !important;
}
.btn-df {
	line-height: 18px;
    font-size: 12px;
    width: 60px;
    border: none;
}
.prov, .city, .dist {
	width: 30%;
	height: 30px;
}
</style>
<script type="text/javascript">
    var deviceTypes = [];
	$(function() {
		var now = new Date();

		//初始化城市合伙人列表
		$('#cityPartnerGrid').datagrid({
			fitColumns:true,
			singleSelect: true,
			url:'${pageContext.request.contextPath}/user/vender/find.json'
		}); 
		
		var cache = getAllSysTypes();
        for (var i = 0; i < cache.length; i++) {
            if (cache[i].type == 'DEVICE_TYPE2') {
            	deviceTypes.push(cache[i]);
            }
        }
	});

	function addCityPartner() {
		openDialog('winCityPartner', '新增城市合伙人');
	}
	
	function saveCityPartner() {
		var row = $('#fomCityPartner').getValues();
		postForm({
			form : 'fomCityPartner',
			url : '${pageContext.request.contextPath}/user/vender/save.json',
			info : '保存城市合伙人信息成功！',
			task : function(data, statusText, xhr) {
				queryData('cityPartnerGrid', 'cityPartnerQueryForm');
			}
		});
	}

	function delCityPartner() {
		var rows = $('#cityPartnerGrid').datagrid('getSelections');
		if (rows.length == 0) {
			infoMsg('请选择需要删除的城市合伙人！');
		} else {
			confirmMsg('您确定要删除该所选城市合伙人吗？', doDelCityPartners, [ rows ]);
		}
	}

	function doDelCityPartners(rows) {
		var ids = [];
		for (var i = 0; i < rows.length; i++)
			ids.push(rows[i].id);
		$.ajax({
			url : '${pageContext.request.contextPath}/user/vender/delete.json',
			data : $.param({
				'ids' : ids
			}, true),
			info : '所选城市合伙人删除成功！',
			task : function(data, statusText, xhr) {
				queryData('cityPartnerGrid', 'cityPartnerQueryForm');
			}
		});
	}

	function updateCityPartner(row) {
		if (!row)
			row = $('#cityPartnerGrid').datagrid('getSelected');
		if (row) {
			openDialog('winCityPartner', '修改城市合伙人信息');

			$('#fomCityPartner').form('load', row);
		} else {
			infoMsg('请选择需要修改的城市合伙人！');
		}
	}
	
	// 绑定设备
	function bindDevices() {
		openDialog('winBindDevice', '绑定设备');
		$('#deviceGrid').datagrid({
			pageNumber: 1,
            url:'${pageContext.request.contextPath}/user/vender/findVenderPartnerDevices.json'
        });
	}
	
	// 绑定设备Dialog中的【保存】按钮
	function saveDevice() {
		var rows = $('#deviceGrid').datagrid('getSelections');
        if (rows.length == 0) {
            infoMsg('请选择需要绑定的设备！');
            return;
        }
        saveBindDevices();
	}

	//调用绑定接口绑定设备
	function saveBindDevices() {
		var orgId = $('#cityPartnerGrid').datagrid('getSelected').id;
		var deviceSelections = $('#deviceGrid').datagrid('getSelections');
		var deviceNos = [];
		if (deviceSelections) {
			for (var i = 0; i < deviceSelections.length; i++) {
				deviceNos.push(deviceSelections[i].devNo);
			}
		} else {
			infoMsg('请选择需要绑定的设备！');
			return;
		}

		$.ajax({
			type: 'POST',
			url: '${pageContext.request.contextPath}/user/vender/saveBindDevices.json',
			data: $.param({bindingFlag:1, orgId:orgId, devNos:deviceNos},true),
			task: function() {
				resetForm('deviceQueryForm')
                $('#deviceGrid').datagrid('clearSelections');
                closeDialog('winBindDevice');
			}
		})
	}
	
	// 解绑设备
	function unbindDevices(orgId) {
		$('#deviceUnBindQueryForm').form('clear');
    	$('#q_device_unbind_orgId').val(orgId);
		openDialog('winUnBindDevice', '解绑设备');
		// 解绑设备列表查询
		setTimeout(queryUnBindDevices, 0);
	}
	
	// 解绑设备列表查询
	function queryUnBindDevices() {
        $('#deviceUnBindGrid').datagrid({
            pageNumber: 1,
            url:'${pageContext.request.contextPath}/user/vender/findUnBindingVenderPartnerDevices.json',
            queryParams: { 
               'orgId': $('#q_device_unbind_orgId').val()
           }
        });
	}
	
	// 解绑设备Dialog中的【保存】按钮
    function saveUnBindDevice() {
        var rows = $('#deviceUnBindGrid').datagrid('getSelections');
        if (rows.length == 0) {
            infoMsg('请选择需要解绑的设备！');
            return;
        }
        
        var devNos = [];
        for (var i = 0; i < rows.length; i++) {
            devNos.push(rows[i].devNo);
        }

        confirmMsg('该操作无法撤销，您确定要解绑所选设备吗？', function() {
            $.ajax({
                url:'${pageContext.request.contextPath}/user/vender/saveUnBindDevices.json',
                data:$.param({'devNos':devNos}, true),
                info:'所选设备解绑成功！',
                task:function(data, statusText, xhr) {
			        resetForm('deviceUnBindQueryForm')
			        $('#deviceUnBindGrid').datagrid('clearSelections');
			        closeDialog('winUnBindDevice');
                }
            });
        });  
    }

    function deviceType(value) {
		for (var i = 0; i < deviceTypes.length; i++)
            if (deviceTypes[i].code == value)
                return deviceTypes[i].name;
        return value;
    }

    function showOrgState(value, row, index) {
		if (!value) {
			return '<span style="cursor:pointer;color:green;" title="点击禁用" onclick="changeOrgState('+row.id+','+value+')">启用</span>'
		} else {
			return '<span style="cursor:pointer;color:red;" title="点击启用" onclick="changeOrgState('+row.id+','+value+')">禁用</span>'
		}
	}
	function changeOrgState(orgId, state) {
		if (state) {
			state = 0;
		} else {
			state = 1;
		}
		$.ajax({
			data: {
				orgId: orgId,
				state: state
			},
			url: '${pageContext.request.contextPath}/user/vender/saveAccountOrgState.json',
			success: function() {
				$('#cityPartnerGrid').datagrid('reload');
			}
		})
	}

	function formatOperate(value, row) {
		return '<span class="btn-df" onclick="bindDevices()">绑定设备</span> <span class="btn-df" onclick="unbindDevices(\'' + row.id + '\')">解绑设备</span>';
	}
</script>
</head>
<body class="easyui-layout">
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<form id="cityPartnerQueryForm" class="search-form">
			<input type="hidden" id="q_user_orgId" name="orgId" value="">
			<div class="form-item">
				<div class="text">合伙人名称</div>
				<div class="input">
					<input id="q_vender_name"  name="name" class="easyui-textbox" data-options="prompt:'合伙人名称'">
				</div>
			</div>
			<div class="form-item" style="width:377px;">
				<div class="text">入驻时间</div>
				<div class="input">
					<input id="q_vender_startDate" class="easyui-datebox" name="startDate">&nbsp;-&nbsp;<input id="q_vender_endDate" class="easyui-datebox" name="endDate">
				</div>
			</div>
		</form>
		<sec:authorize access="find">
		<div class="search-btn" onclick="queryData('cityPartnerGrid','cityPartnerQueryForm')">查询</div>
		<div class="search-btn" onclick="resetForm('cityPartnerQueryForm')">重置</div>
		</sec:authorize>
	</div>
	<div id="cityPartnerOpt">
		<sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增城市合伙人'" onclick="addCityPartner();"></a></sec:authorize>
		<sec:authorize access="update,save"><a href="javascript:void(0)" class="icon-edit easyui-tooltip" data-options="content:'修改城市合伙人'" onclick="updateCityPartner();"></a></sec:authorize>
		<sec:authorize access="delete"><a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除城市合伙人'" onclick="delCityPartner();"></a></sec:authorize>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#cityPartnerOpt'" title="城市合伙人信息列表">
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<table id="cityPartnerGrid" data-options="nowrap:false,striped:true,fit:true,idField:'id'">
				<thead>
					<tr>
						<th data-options="checkbox:true,field:'',width:20,align:'center'"></th>
						<th data-options="field:'name',width:150,align:'center'">城市合伙人名称</th>
						<th data-options="field:'manager',width:100,align:'center'">负责人</th>
						<th data-options="field:'phone',width:150,align:'center'">联系电话</th>
						<th data-options="field:'office',width:200,align:'center'">公司地址</th>
						<th data-options="field:'genusArea',width:200,align:'center'">负责区域</th>
						<th data-options="field:'settledTime',width:150,align:'center'">入驻时间</th>
<!-- 						<th data-options="field:'state',width:50,formatter:showOrgState,align:'center'">状态</th> -->
						<th data-options="field:'operate',width:130,align:'center',formatter:formatOperate">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="winCityPartner" class="easyui-dialog" data-options="closed:true,buttons:'#cityParterBtns'" style="width:429px;height:290px;padding:10px 20px;">
		<form id="fomCityPartner" method="post">
			<input type="hidden" id="vender_id" name="id">
			<input type="hidden" id="vender_type" name="type">
			<input type="hidden" id="vender_sort" name="sort">
			<input type="hidden" id="vender_companyId" name="companyId">
			<input type="hidden" id="vender_createTime" name="createTime">
			<input type="hidden" id="vender_createUser" name="createUser">
			<input type="hidden" id="vender_state" name="state" value="0">
			<input type="hidden" id="vender_settledTime" name="settledTime">
			<input type="hidden" name="prov">
			<input type="hidden" name="city">
			<input type="hidden" name="dist">
			
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">城市合伙人名称</div>
					<input id="name" name="name" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['length[1,64]']">
				</div>
				<div class="form-cloumn form-item">
					<div class="text">负责人</div>
					<input id="manager" name="manager" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['length[1,32]']">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">联系电话</div>
					<input id="phone" name="phone" class="easyui-numberbox" data-options="required:true,prompt:'必填项',validType:['length[1,16]']">
				</div>
				<div class="form-cloumn form-item">
					<div class="text">公司地址</div>
					<input id="office" name="office" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['length[1,256]']">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item" style="width:100%;">
					<div class="text">负责区域</div>
					<input id="genusArea" name="genusArea" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['length[1,256]']" style="width:378px">
				</div>
			</div>
		</form>
		<div id="cityParterBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveCityPartner()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winCityPartner')">取消</a>
		</div>
	</div>
	
	<!-- 绑定设备 -->
    <div id="winBindDevice" class="easyui-dialog" data-options="closed:true,buttons:'#bindBtns'" style="width:750px;height:500px;">
        <div style="padding:10px;">   
            <form id="deviceQueryForm" class="search-form">
                <input type="hidden" id="q_device_orgId" name="orgId" value="">       
                <span>设备组号：</span>
                <div class="form-item" style="width:380px;">
					<input class="easyui-textbox" name="startFactoryDevNo">
					至
					<input class="easyui-textbox" name="endFactoryDevNo">
                </div>
            </form>
            <sec:authorize access="find">
                <div class="search-btn" onclick="queryData('deviceGrid','deviceQueryForm')">查询</div>
                <div class="search-btn" onclick="resetForm('deviceQueryForm')">重置</div>
            </sec:authorize>
        </div>
        <div style="padding:0 10px 10px 10px;height:368px;">
	        <table id="deviceGrid" style="height:400px;" data-options="nowrap:false,striped:true,fit:true,fitColumns:true,idField:'id'" title="设备列表">
	            <thead>
	                <tr>
	                    <th data-options="checkbox:true,field:'',width:20"></th>
	                    <th data-options="field:'factoryDevNo',width:100">设备组号</th>
	                    <th data-options="field:'type',width:300,formatter:deviceType">设备类型</th>
	                </tr>
	            </thead>
	        </table>
	    </div>
	    <div id="bindBtns">
	        <a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveDevice()">保存</a>
	        <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winBindDevice')">取消</a>
	    </div>
    </div>
    <!-- 解绑设备 -->
    <div id="winUnBindDevice" class="easyui-dialog" data-options="closed:true,buttons:'#unbindBtns'" style="width:750px;height:500px;">
        <div style="padding:10px;">
            <form id="deviceUnBindQueryForm" class="search-form">
                <input type="hidden" id="q_device_unbind_orgId" name="orgId" value=""> 
                <span>设备组号：</span>
                <div class="form-item" style="width:380px;">
					<input class="easyui-textbox" id="startFactoryDevNo" name="startFactoryDevNo">
					至
					<input class="easyui-textbox" id="endFactoryDevNo" name="endFactoryDevNo">
                </div>                
            </form>
            <sec:authorize access="find">
                <div class="search-btn" onclick="queryData('deviceUnBindGrid','deviceUnBindQueryForm')">查询</div>
                <div class="search-btn" onclick="resetForm('deviceUnBindQueryForm')">重置</div>
            </sec:authorize>
        </div>
        <div style="padding:0 10px 10px 10px;height:368px;">
            <table id="deviceUnBindGrid" data-options="nowrap:false,striped:true,fit:true,fitColumns:true,idField:'id'" title="设备列表">
                <thead>
                    <tr>
                        <th data-options="checkbox:true,field:'',width:20"></th>
                        <th data-options="field:'factoryDevNo',width:100">设备组号</th>
                        <th data-options="field:'type',width:300,formatter:deviceType">设备类型</th>
                    </tr>
                </thead>
            </table>
        </div>
        <div id="unbindBtns">
            <a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveUnBindDevice()">保存</a>
            <a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winUnBindDevice')">取消</a>
        </div>
    </div>
</body>
</html>