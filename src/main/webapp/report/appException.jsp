<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>APP异常信息</title>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/base64.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/easyui/datagrid-detailview.min.js"></script>
<script type="text/javascript">
	$(function() {
        showDateCombo();
		$('#exceptionGrid').datagrid({
			url:'${pageContext.request.contextPath}/report/appException/find.json',
			view: detailview,
			detailFormatter:function(index,row){
				var content = '<table style="margin:0px;padding:20px;cellpadding:0;cellspacing:0;font-size:13px;border:0px;background:#FFFFE0;">';
	            content = content + '<tr><td style="font-weight:bold;border:0px;width:70px;">异常信息:</td><td style="border:0px;width:200px;">' + getValue(row.exceptions) + '</td></tr>';
	            content = content + '</table>';
	            return content;
			},
			onExpandRow: function(index,row){
				$('#exceptionGrid').datagrid('fixDetailRowHeight',index);
			}
		});
	});
	
	function formatExceptions(value, row, index) {
		return value ? value.substring(0, 200) + " ......" : "";
	}
	
</script>
</head>
<body class="easyui-layout" >
	<div data-options="region:'north',border:false">
		<table>
			<tr>
				<td>
					<form id="exceptionQueryForm">
						<table style="border:1px solid #ccc;">
							<tr>
								<td style="padding:5px 5px 5px 10px;">设备编号</td><td style="padding:5px 0px 5px 0px;"><input name="deviceNo" class="easyui-textbox" style="width:150px"></td>
							    <td style="padding:5px 5px 5px 10px;">异常时间</td>
							    <td colspan="3" style="padding:3px 0px 6px 0px;">
                                    <table cellspacing="0" cellpadding="0">
                                        <tr>
                                            <td style="padding:0px 0px 0px 0px;"><input id="q_ex_startDate" class="easyui-datebox" style="width:150px;" name="startDate" data-options="editable:false">
                                            <td style="padding:0px 3px 0px 3px;"><div style="width:5px;">-</div></td>
                                            <td style="padding:0px 0px 0px 0px;"><input id="q_ex_endDate" class="easyui-datebox" style="width:150px;" name="endDate" data-options="editable:false"></td>
                                            <td style="padding:0px 10px 0px 2px;"><div class="date-combo" data-options="start:'q_ex_startDate',end:'q_ex_endDate'" style="width:80px;"></div></td>
                                        </tr>
                                    </table>
                                </td>
							</tr>
						</table>
					</form>
				</td>
				<sec:authorize access="find">
					<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-search'" href="javascript:void(0)" onclick="queryData('exceptionGrid','exceptionQueryForm')">查询</a></td>
					<td valign="bottom"><a class="easyui-linkbutton" data-options="iconCls:'icon-no'" href="javascript:void(0)" onclick="resetForm('exceptionQueryForm')">重置</a></td>
				</sec:authorize>
			</tr>
		</table>
	</div>
	<div data-options="region:'center',border:false">
		<table id="exceptionGrid" data-options="nowrap:false,striped:true,fit:true,border:false,idField:'id'" title="APP异常信息列表">
			<thead>
				<tr>
					<th data-options="field:'deviceNo',width:80">设备编号</th>
					<th data-options="field:'version',width:150">版本号</th>
					<th data-options="field:'exceptions',width:450,formatter:formatExceptions">异常信息</th>
					<th data-options="field:'createTime',width:150">异常时间</th>
				</tr>
			</thead>
		</table>
	</div>
</body>
</html>