<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>我的店铺</title>
<%@ include file="/common.jsp"%>
<style>
#winBindDevice .panel-body-noborder, #winUnBindDevice .panel-body-noborder {height: 310px !important;}
#pointAddress {box-sizing: border-box; width: 100%; height: 50px; padding: 3px 7px; border: 1px solid #ddd; resize: none; outline: none;}
.del-timer {display: none; float:right; line-height:28px; font-size: 13px; cursor:pointer; padding: 0 5px; border: 1px solid #e5e5e5; border-radius: 4px;}
.del-timer:hover {background-color: #eee;}
.add-timer {color:#0f92f3; cursor:pointer; font-weight:normal;}
.add-timer:hover {color: #0572c3;} 
.area-select {height: 28px; border-color: #ddd; outline: none;}
</style>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/distpicker.data.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/jquery.distpicker/distpicker.min.js"></script>
<script type="text/javascript">
	var pointTypes = [], deviceTypes = [], pointNatures = [];
	var deviceList = [];//缓存当前店铺设备列表
	var notBindDeviceList = [];//缓存所有设备列表
	$(function() {
		var pointTypesQuery = [{'code':'', 'name':'请选择'}];
		var cache = getAllSysTypes();
		for (var i = 0; i < cache.length; i++) {
			if (cache[i].type == 'POINT_PLACE_TYPE') {
				pointTypes.push(cache[i]);
				pointTypesQuery.push(cache[i]);
			} else if (cache[i].type == 'DEVICE_TYPE2') {
                deviceTypes.push(cache[i]);
            } else if (cache[i].type == 'POINT_NATURE') {
            	pointNatures.push(cache[i]);
            }
		}
		$('#pointType').combobox({data:pointTypes});
		$('#q_point_type').combobox({data:pointTypesQuery});
		$('#pointNature').combobox({data:pointNatures});

		$('#pointGrid').datagrid({
			nowrap:false,
			striped:true,
			fit:true,
			fitColumns:true,
			singleSelect: true,
			idField:'id',
			url:'${pageContext.request.contextPath}/store/myStore/find.json',
			onLoadSuccess: function(data) {
				if (data.rows.length != 0) {
					$('#pointGrid').datagrid('selectRow', 0);
				}
			},
			onSelect: function(rowIndex, rowData) {
				$('#deviceGrid').datagrid({
					url : '${pageContext.request.contextPath}/store/myStore/findBindDevCombination.json',
					queryParams: {
						'ORG_ID': rowData.orgId,
						'pointplaceId' : rowData.id
					}
				})
			}
		});

		$('#deviceGrid').datagrid({
			fit: true,
			fitColumns: true,
			singleSelect: true,
			pagination:false,
			loadFilter: function(data) {
				deviceList = data.rows;
				var rows = [];
				for (var k=0; k<deviceList.length; k++) {//筛选主控设备
					if (deviceList[k].cabinetNo == 1) {
						rows.push(deviceList[k]);
					}
				}
				data.rows = rows;
				data.total = rows.length;
				return data;
			},
			view: detailview,
	        detailFormatter:function(index,row){
	          return '<div style="padding:5px;background-color:#f5f5f5;"><table id="subDevice-' + index + '"></table></div>';
	        },
	        onExpandRow: function(index,row){
	        	var subDevices = [];
	        	for (var i=0; i<deviceList.length; i++) {
	        		if (deviceList[i].identity == row.identity && deviceList[i].cabinetNo != 1) {
	        			subDevices.push(deviceList[i]);
	        		}
	        	}
	        	if (subDevices.length == 0) {
	        		$('#subDevice-'+index).html('无附属货柜');
	        		$('#deviceGrid').datagrid('fixDetailRowHeight',index);
	        		return;
	        	}
	          	$('#subDevice-'+index).datagrid({
		            fitColumns:true,
		            singleSelect:true,
		            pagination:false,
		            striped:true,
		            height:'auto',
		            columns:[[
		              	{field:'factoryDevNo', width:130, align:'center', title:'设备组号', formatter:bindState},
		              	{field:'model', width:220, align:'center', title:'设备类型', formatter:deviceType},
		              	{field:'aisleCount', width:50, align:'center', title:'货道数量'}
		            ]],
		            data: {
		            	total: subDevices.length,
		            	rows: subDevices
		            },
		            onResize:function(){
		              $('#deviceGrid').datagrid('fixDetailRowHeight',index);
		            },
		            onLoadSuccess:function(){
		              setTimeout(function(){
		                $('#deviceGrid').datagrid('fixDetailRowHeight',index);
		              },0);
		            }
	          	});
	          	$('#deviceGrid').datagrid('fixDetailRowHeight',index);
		    }
		})

		$('#replenishTimes').on('click', '.add-timer', addTimer);

		$('#replenishTimes').on('click', '.del-timer', function() {
			$(this).parent().remove();
			var $dels = $('#replenishTimes .del-timer');
			if ($dels.length === 1) {
				$dels.hide();
			}
		})
	});
	
	function initNotBindDeviceGrid() {
		$('#notBindDeviceGrid').datagrid({
            fit: true,
            fitColumns: true,
            border: false,
            singleSelect: true,
            pagination:false,
            url: '${pageContext.request.contextPath}/store/myStore/findDevCombination.json',
            queryParams: {orgId: $('#pointGrid').datagrid('getSelected').orgId},
            loadFilter: function(data) {
                notBindDeviceList = data.rows;
                var flag = true,
                	rows = [],
                	masterDevices = [];
                for (var k=0; k<notBindDeviceList.length; k++) {//筛选主控设备
                    if (notBindDeviceList[k].cabinetNo == 1) {
                        masterDevices.push(notBindDeviceList[k]);
                    }
                }
                for (var i = 0; i < masterDevices.length; i++) {//设备组去重
                	for (var j = 0; j < rows.length; j++) {
                		if (masterDevices[i].combinationNo == rows[j].combinationNo) {
                			rows[j].count += 1;
                			flag = false;
                			break;
                		}
                	}
                	if (flag) {
                		rows.push($.extend({count: 1}, masterDevices[i]));
                	} else {
                		flag = true;
                	}
                }
                data.rows = rows;
                data.total = rows.length;
                return data;
            },
            view: detailview,
            detailFormatter:function(index,row){
              return '<div style="padding:5px;background-color:#f5f5f5;"><table id="notBindSubDevice-' + index + '"></table></div>';
            },
            onExpandRow: function(index,row){
                var subDevices = [];
                for (var i=0; i<notBindDeviceList.length; i++) {
                    if (notBindDeviceList[i].devNo == row.devNo && notBindDeviceList[i].combinationNo == row.combinationNo && notBindDeviceList[i].cabinetNo != 1) {
                        subDevices.push(notBindDeviceList[i]);
                    }
                }
                if (subDevices.length == 0) {
                    $('#notBindSubDevice-'+index).html('无附属货柜');
                    $('#notBindDeviceGrid').datagrid('fixDetailRowHeight',index);
                    return;
                }
                $('#notBindSubDevice-'+index).datagrid({
                    fitColumns:true,
                    singleSelect:true,
                    pagination:false,
                    striped:true,
                    height:'auto',
                    columns:[[
                        {field:'model', width:220, align:'center', title:'设备类型', formatter:deviceType},
                        {field:'aisleCounts', width:50, align:'center', title:'货道数量'}
                    ]],
                    data: {
                        total: subDevices.length,
                        rows: subDevices
                    },
                    onResize:function(){
                      $('#notBindDeviceGrid').datagrid('fixDetailRowHeight',index);
                    },
                    onLoadSuccess:function(){
                      setTimeout(function(){
                        $('#notBindDeviceGrid').datagrid('fixDetailRowHeight',index);
                      },0);
                    }
                });
                $('#notBindDeviceGrid').datagrid('fixDetailRowHeight',index);
            }
        })
	}

	function formatPointType(value) {
		for (var i = 0; i < pointTypes.length; i++)
			if (pointTypes[i].code == value)
				return pointTypes[i].name;
		return value;
	}

	function addTimer(times) {
		var $timer = $('<div class="timer" style="margin-bottom:5px;"><input class="easyui-timespinner" data-options="required:true,prompt:\'开始时间\'" style="width:160px"> - <input class="easyui-timespinner" data-options="required:true,prompt:\'结束时间\'" style="width:160px"><span class="del-timer">删除<span></div>');
		$('#replenishTimes').append($timer);
		$inputs = $timer.find('input');
		$inputs.timespinner();
		if (times) {
			$inputs.eq(0).timespinner('setValue', times.startTime);
			$inputs.eq(1).timespinner('setValue', times.endTime);
		}
		if ($('#replenishTimes .del-timer').length > 1) {
			$('#replenishTimes .del-timer').show();
		}
	}

	function resetPointForm() {
		$('#fomPoint').form('reset');
		$('#replenishTimes .timer').remove();
	}

	function updatePoint(id) {
		var rows = $('#pointGrid').datagrid('getRows');
		var index = $('#pointGrid').datagrid('getRowIndex', id);
		var row = rows[index];
		openDialog('winPoint', '修改店铺');
		$('#fomPoint').form('load', row);
		$('#province').val(row.prov).trigger('change');
		$('#city').val(row.city).trigger('change');
		$('#district').val(row.dist);
		for (var i = 0; i < row.pointReplenishTimes.length; i++) {
			addTimer(row.pointReplenishTimes[i]);
		}
	}
	
	function savePoint() {
		var $form = $('#fomPoint');

		if (!$form.form('enableValidation').form('validate')) return;

		var row = $form.getValues();
		
		row.pointReplenishTimes = [];
	    var $timers = $form.find('.timer');
	    for (var i = 0; i < $timers.length; i++) {
	        var $inputs = $timers.eq(i).find('.textbox-value');
	        var timerItem = {startTime: $inputs.eq(0).val(), endTime: $inputs.eq(1).val()};
	        row.pointReplenishTimes.push(timerItem);
	    }
	     
		$.ajax({
			url: '${pageContext.request.contextPath}/store/myStore/save.json',
			dataType:"json",
            contentType : 'application/json;charset=utf-8', //设置请求头信息  
			data: JSON.stringify(row),
			info: '保存店铺成功',
			task: function() {
				closeDialog('winPoint');
				queryData('pointGrid', 'pointQueryForm');
			}
		})
	}

	//绑定设备
	function bindDevices() {
		openDialog('winBindDevices', '新增设备组');
		initNotBindDeviceGrid();
	}

	function saveDevice() {
		var pointPlace = $('#pointGrid').datagrid('getSelected');
		var devCombination = $('#notBindDeviceGrid').datagrid('getSelected');
		$.ajax({
			data: {
				pointplaceID: pointPlace.id,
				combinationNo: devCombination.combinationNo
			},
			url: '${pageContext.request.contextPath}/store/myStore/addDevCombination.json',
			success: function() {
				closeDialog('winBindDevices');
				$('#deviceGrid').datagrid('reload');
			}
		})
	}

	//删除设备
	function unbindDevices(identity) {
		confirmMsg('解绑设备后，设备上的商品将会自动下架<br>是否确认解绑该设备？', doUnbindDevices, [ identity ]);
	}
	function doUnbindDevices(identity) {
		$.ajax({
			data: {
				identity: identity
			},
			url: '${pageContext.request.contextPath}/store/myStore/deleteDevCombination.json',
			success: function() {
				$('#deviceGrid').datagrid('reload');
			}
		})
	}
     
    function deviceType(value) {
    	for (var i = 0; i < deviceTypes.length; i++)
            if (deviceTypes[i].code == value)
                return deviceTypes[i].name;
        return value;
    }

    function bindState(value, row) {
    	return row.bindState == 0 ? '' : value;
    }

    //操作按钮
    function showPointOperate(value, row) {
		var operate = '<a href="javascript:void(0)" class="icon-edit easyui-tooltip" data-options="content:\'编辑\'" onclick="updatePoint('+row.id+')" style="display:inline-block;width:16px;height:16px;margin-right:5px;"></a>';
		return operate;
    }
    function showDeviceOperate(value, row) {
		return '<a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:\'删除\'" onclick="unbindDevices(\''+row.identity+'\')" style="display:inline-block;width:16px;height:16px;"></a>';
    }
</script>
</head>
<body class="easyui-layout" >
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
		<!-- 查询 -->
		<form id="pointQueryForm" class="search-form">
			<div class="form-item">
				<div class="text">店铺名称</div>
				<div class="input">
					<input name="pointName" class="easyui-textbox" data-options="prompt:'店铺名称'" />
				</div>
			</div>
			<div class="form-item">
				<div class="text">店铺地址</div>
				<div class="input">
					<input name="pointAddress" class="easyui-textbox" data-options="prompt:'店铺地址'" />
				</div>
			</div>
			<div class="form-item">
				<div class="text">店铺类型</div>
				<div class="input">
					<input id="q_point_type" name="pointType" class="easyui-combobox" data-options="valueField:'code',textField:'name',editable:false,panelHeight:'auto'" />
				</div>
			</div>
		</form>
		<sec:authorize access="find">
		<div class="search-btn" onclick="queryData('pointGrid','pointQueryForm')">查询</div>
		<div class="search-btn" onclick="resetForm('pointQueryForm')">重置</div>
		</sec:authorize>
	</div>
	<div data-options="region:'center',border:false,headerCls:'list-head'" title="我的店铺" style="width:60%;">
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<table id="pointGrid">
				<thead>
					<tr>
						<th data-options="checkbox:true,field:'',width:20" align="center"></th>
						<th data-options="field:'pointNo',width:80" align="center">店铺编号</th>
						<th data-options="field:'pointName',width:150" align="center">店铺名称</th>
						<th data-options="field:'pointAddress',width:150" align="center">店铺地址</th>
						<th data-options="field:'pointType',width:100,formatter:formatPointType" align="center">店铺类型</th>
						<sec:authorize access="update,save,delete"><th data-options="field:'operate',width:50,formatter:showPointOperate" align="center">操作</th></sec:authorize>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div data-options="region:'east',border:false,split:true,headerCls:'list-head',tools:'#deviceOpt'" title="设备信息" style="width:40%;">
		<div id="deviceOpt">
			<sec:authorize access="add"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增设备'" onclick="bindDevices()"></a></sec:authorize>	
		</div>
		<div style="box-sizing:border-box; height:100%; padding:10px;">
			<table id="deviceGrid">
				<thead>
					<tr>					
						<th data-options="field:'factoryDevNo',width:120,formatter:bindState" align="center">设备组号</th>						
						<th data-options="field:'model',width:200,formatter:deviceType" align="center">设备类型</th>
						<th data-options="field:'aisleCount',width:50" align="center">货道数量</th>
						<sec:authorize access="delete"><th data-options="field:'operate',width:50,formatter:showDeviceOperate" align="center">操作</th></sec:authorize>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="winPoint" class="easyui-dialog" data-options="closed:true, buttons:'#pointBtns', onClose:resetPointForm" style="width:429px;padding:10px 20px;">
		<form id="fomPoint" method="post">
			<input type="hidden" name="id">
			<input type="hidden" name="orgId">
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">店铺名称</div>
					<input id="pointName" name="pointName" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['length[1,64]']">
				</div>
				<div class="form-cloumn form-item">
					<div class="text">店铺类型</div>
					<input id="pointType" name="pointType" class="easyui-combobox" data-options="valueField:'code',textField:'name',editable:false,required:true,prompt:'必填项'">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item" style="width:100%;">
					<div class="text">所在地区</div>
					<div id="distpicker" data-toggle="distpicker">
				        <select class="area-select easyui-validatebox" data-options="required:true" data-province="广东省" id="province" name="prov"></select> 
				        <select class="area-select easyui-validatebox" data-options="required:true" data-city="深圳市" id="city" name="city"></select>
				        <select class="area-select easyui-validatebox" data-options="required:true" data-district="南山区" id="district" name="dist"></select>
				    </div>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item" style="width:100%;">
					<div class="text">详细地址</div>
					<textarea id="pointAddress" class="easyui-validatebox" name="pointAddress" placeholder="请填写详细地址，街道/门牌/楼层/房间号等" data-options="required:true"></textarea>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">经纬度</div>
					<input name="latitudeLongitude" class="easyui-textbox" data-options="prompt:'选填项'">
				</div>
				<div class="form-cloumn form-item">
					<div class="text">场所性质</div>
					<select id="pointNature" name="nature" class="easyui-combobox" data-options="required:true,prompt:'必填项',valueField:'code',textField:'name',editable:false,panelHeight:'auto'"></select>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">人流量<span style="font-weight:normal;">(人次/天)</span></div>
					<input name="humanTraffic" class="easyui-numberbox" data-options="prompt:'选填项',validType:['length[1,15]']">
				</div>
			</div>
			<div class="form-row">
				<div id="replenishTimes" class="form-cloumn form-item" style="width:100%;">
					<div class="text">建议补货时间 (<span class="add-timer">添加</span>)</div>
				</div>
			</div>
		</form>
		<div id="pointBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="savePoint();">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winPoint');">取消</a>
		</div>
	</div>
	<!-- 新增设备组信息 -->
	<div id="winBindDevices" class="easyui-dialog" data-options="closed:true,buttons:'#bindDevicesBtns'" style="width:750px;height:500px;">
		<div class="easyui-layout" style="height:100%;">
			<div data-options="region:'center',border:false">
				<div id="bindTableCtn" style="height:100%;">
					<table id="notBindDeviceGrid">
						<thead>
							<tr>
								<th data-options="checkbox:true,field:'',width:20" align="center"></th>
								<th data-options="field:'model',width:200,formatter:deviceType" align="center">设备类型</th>
								<th data-options="field:'aisleCounts',width:50" align="center">货道数量</th>
								<th data-options="field:'count',width:50" align="center">设备组数量</th>
							</tr>
						</thead>
					</table>
				</div>
			</div>
		</div>
		<div id="bindDevicesBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveDevice();">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winBindDevices');" style="margin-left:10px;">取消</a>
		</div>
	</div>
</body>
</html>