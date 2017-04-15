var _contextPath, _curUser, _fileServer, _pageSize = 50, _token, _allSysTypes, _allOrgs, _allCountries, _allCurrencies, _allCategories;
var _rights={'find':1,'add':2,'update':3,'save':4,'copy':5,'del':6,'audit':7,'unaudit':8,'imp':9,'exp':10,'upload':11,'download':12,'execute':13};
var _rightsName={'find':'查询','add':'新增','update':'修改','save':'编辑','copy':'复制','del':'删除','audit':'审核','unaudit':'弃审','imp':'导入','exp':'导出','upload':'上传','download':'下载','execute':'执行'};
var _rightsData=[[1,'查询'],[2,'新增'],[3,'修改'],[4,'编辑'],[5,'复制'],[6,'删除'],[7,'审核'],[8,'弃审'],[9,'导入'],[10,'导出'],[11,'上传'],[12,'下载'],[13,'执行']];
var _fileType={vender:1,category:2,product:3,youpin:4,template:5,discover:6,banner:8,discover_logo:9,spread:10,promote:11,articel:12,qrcode:13,product_qrcode:15,productDesc:21,templateDesc:22,discoverDesc:23,articelDesc:24,info:25,vender_bg:14,excel:26, advertise:27, advertise_top: 28, advertise_bg:29, advertise_bottom:30, product_detail:31, lottery_pro_m:32, lottery_pro_d:33, lottery_pro_s:34};
$.extend($.parser,{
	onComplete:function(context){
		closeRequestMask();
	}
})
$.extend($.fn.datagrid.defaults,{
	pageSize:_pageSize,
	pagination:true,
	rownumbers:true,
	//rowStyler:handRowStyle,
	loadMsg:'',
	onLoadError:function(data, status, error) {
		if (status == 'timeout' || !error) {
			errorTip('请求超时，请检查网络是否正常');
		} else if (status == 'parsererror') {
			errorTip('登陆失效，请重新登陆');
		} else {
			var rs = eval('('+data.responseText+')');
			var title = $(this).attr('title');
			if (rs.exception.message.indexOf('操作权限') == -1) {
				errorMsg(rs.exception.message);
			} else {
				$(this).datagrid("getPanel").panel('setTitle', title + '&nbsp;&nbsp;<font color="red">' + rs.exception.message + '</font>');
			}
		}
	},
	onLoadSuccess:function(data) {
		var title = $(this).attr('title');
		if (data.rows && title) {
			var pos = title.indexOf('&nbsp;');
			if (pos != -1)
				title = title.substr(0, pos);
			if (data.rows.length == 0) {
				$(this).datagrid("getPanel").panel('setTitle', title + '&nbsp;&nbsp;<font color="green">没有查询到数据！</font>');
			} else {
				$(this).datagrid("getPanel").panel('setTitle', title + ($(this).datagrid("options").pagination == true ? '' : '&nbsp;&nbsp;<font color="green">共' + data.rows.length + '记录</font>'));
			}
		}
		var opt = $(this).datagrid("options");
		if (opt.task)
			opt.task.call(opt, data);
	}
});
$.extend($.fn.treegrid.defaults,{
	pageSize:_pageSize,
	pagination:true,
	rownumbers:true,
	rowStyler:handRowStyle,
	loadMsg:'',
	onBeforeLoad:function(node, param) {
		param.queryParams = $.extend(param.queryParams, {_csrf:_token});
		if (param.beforeTask)
			param.beforeTask.call(node, param);
	},
	onLoadError:function(data, status, error) {
		if (status == 'timeout' || !error) {
			errorTip('请求超时，请检查网络是否正常');
		} else if (status == 'parsererror') {
			errorTip('登陆失效，请重新登陆');
		} else {
			var rs = eval('('+data.responseText+')');
			var title = $(this).attr('title');
			if (rs.exception.message.indexOf('操作权限') == -1) {
				errorMsg(rs.exception.message);
			} else {
				$(this).datagrid("getPanel").panel('setTitle', title + '&nbsp;&nbsp;<font color="red">' + rs.exception.message + '</font>');
			}
		}
	},
	onLoadSuccess:function(row, data) {
		var title = $(this).attr('title');
		if (data && title) {
			var pos = title.indexOf('&nbsp;');
			if (pos != -1)
				title = title.substr(0, pos);
			if (data.length == 0) {
				$(this).datagrid("getPanel").panel('setTitle', title + '&nbsp;&nbsp;<font color="green">没有查询到数据！</font>');
			} else {
				$(this).datagrid("getPanel").panel('setTitle', title);
			}
		}
		var opt = $(this).datagrid("options");
		if (opt.task)
			opt.task.call(opt, row, data);
	}
});
$.extend($.fn.tree.defaults,{
	onBeforeLoad:function(node, param) {
		param.queryParams = $.extend(param.queryParams, {_csrf:_token});
		if (param.beforeTask)
			param.beforeTask.call(node, param);
	},
	onLoadError:function(data, status, error) {
		if (status == 'timeout' || !error) {
			errorTip('请求超时，请检查网络是否正常');
		} else if (status == 'parsererror') {
			errorTip('登陆失效，请重新登陆');
		} else {
			var rs = eval('('+data.responseText+')');
			if (!$(this).tree('getRoot')) {
				if (rs.exception.message.indexOf('操作权限') == -1) {
					errorMsg(rs.exception.message);
				} else {
					var node = {};
					node.id = 'root';
					node.iconCls = 'tree-dnd-no';
					node.text = '<font color="red"><strong>' + rs.exception.message + '</strong></font>';
					node.children = [];
					$(this).tree('options').onContextMenu = undefined;
					$(this).tree('options').onClick = undefined;
					$(this).tree('options').onDblClick = undefined;
					$(this).tree('options').onCheck = undefined;
					$(this).tree('loadData', node);
					var els = $($(this).tree('getRoot').target).find('.tree-checkbox');
					if (els && els.length >= 1)
						for (var i = 0; i < els.length; i++)
							$(els[i]).remove();
				}
			}
		}
	}
});
// $.extend($.fn.form.defaults,{
// 	onSubmit:function(param) {
// 		var valid = $(this).form('enableValidation').form('validate');
// 		return valid;
// 	},
// 	success:function(data) {
// 		var formId = getRequestMaskValue();
// 		closeRequestMask();
// 		try {
// 			var json = eval('(' + data + ')');
// 			if (json.exception) {
// 				errorMsg(json.exception.message);
// 			} else {
// 				try {
// 					if (this.complete) {
// 						this.complete.call(this, json);
// 					} else {
// 						if (formId && formId.length > 3) {
// 							var winId = formId.substr(3, formId.length - 3);
// 							if (winId)
// 								closeDialog('win' + winId);
// 						}
// 						if (this.task)
// 							this.task.call(this, json);
// 					}
// 				} catch(e) {
// 					errorMsg(e.message);
// 				}
// 			}
// 		} catch(e) {
// 			if ($.isXMLDoc(data))
// 				errorTip('服务器请求地址无响应！');
// 		}
// 	}
// });
$.ajaxSetup({
	type:'POST',
	beforeSend:function(xhr, settings) {
		xhr.setRequestHeader('X-CSRF-TOKEN', _token);
	},
	success:function(data, statusText, xhr) {
		if (this.info)
			infoTip(this.info);
		if (this.task)
			this.task.call(this, data, statusText, xhr);
	},
	error:function(data, status, xhr) {
		if (status == 'timeout') {
			errorTip('请求超时，请检查网络是否正常');
		} else if (status == 'parsererror') {
			errorTip('登陆失效，请重新登陆');
		} else {
			var rs = eval('(' + data.responseText + ')');
			errorMsg(rs.exception.message);
		}
	}
});
//获取url中的参数
function getUrlArg (argName) {
	var argStr = location.search.slice(1);
	if (!argStr) return;
	var argArr = argStr.split('&');
	if (!!argName) {
		for (var i = 0; i < argArr.length; i++) {
			if (argArr[i].indexOf(argName) !== -1) {
				return argArr[i].slice(argName.length+1);
			}
		}
		return;
	} else {
		var argObj = {};
		for (var i = 0; i < argArr.length; i++) {
			var argItemArr = argArr[i].split('=');
			argObj[argItemArr[0]] = argItemArr[1];
		}
		return argObj;
	}
};
function upperTextBox(id, index) {
	var idx = index ? index : 0;
	var textbox = $('#' + id).parent().find('.textbox-text');
	$(textbox[idx]).css('text-transform','uppercase');
	$(textbox[idx]).bind("keyup", function (e) {
		$(textbox[idx]).val($(textbox[idx]).val().toUpperCase());
    });
}
function numberTextBox(id, index) {
	var idx = index ? index : 0;
	var textbox = $('#' + id).parent().find('.textbox-text');
	$(textbox[idx]).css('ime-mode','disabled');
	$(textbox[idx]).css('ondragenter','expression(ondragenter=function(){return false;})');
	$(textbox[idx]).css('onpaste','expression(onpaste=function(){return false;})');
	$(textbox[idx]).bind("keypress", function (e) {
        var code = e.keyCode ? e.keyCode : e.which;
        return code >= 48 && code <= 57 || code == 8;
    });
}
function checkTextBox(value){
	var Regx=/^[A-Z0-9]*$/;
	if(Regx.test(value)){
		return true;
	}else{
		return false;
	}
}
function showDateCombo(index){
	var els = $('.date-combo');
	for (var i = 0; i < els.length; i++) {
		var options = $(els[i]).attr('data-options');
		if (options)
			options = eval('({' + options + '})');
		$(els[i]).combobox({
			editable:false,
			data:[{text:'今天',value:1},{text:'昨天',value:2},{text:'过去一周',value:3},{text:'过去一月',value:4}],
			onSelect:function(record){
				if (options && options.start && options.end && $('#' + options.start) && $('#' + options.end)) {
					var curDate = new Date();
					if (record.value == 1) {
						$('#' + options.start).datebox('setValue', currentDateString(curDate));
						$('#' + options.end).datebox('setValue', currentDateString(curDate));
					} else if (record.value == 2) {
						var date = addDays(curDate, -1);
						$('#' + options.start).datebox('setValue', currentDateString(date));
						$('#' + options.end).datebox('setValue', currentDateString(date));
					} else if (record.value == 3) {
						var date = addDays(curDate, -7);
						var dayOfWeek = date.getDay() == 0 ? 7 : date.getDay() - 1;
						date = addDays(date, -dayOfWeek);
						$('#' + options.start).datebox('setValue', currentDateString(date));
						$('#' + options.end).datebox('setValue', currentDateString(addDays(date, 6)));
					} else if (record.value == 4) {
						var date = addMonths(curDate, -1);
						date = new Date(date.getFullYear(), date.getMonth(), 1);
						$('#' + options.start).datebox('setValue', currentDateString(date));
						$('#' + options.end).datebox('setValue', currentDateString(addDays(addMonths(date, 1), -1)));
					}
				}
			}
		});
		if (index)
			$(els[i]).combobox('select', index);
	}
}
function showOrgCombo(limitCompany, onlyCompany) {
	var els = $('.org-combo');
	for (var i = 0; i < els.length; i++) {
		var options = $(els[i]).attr('data-options');
		options = options ? eval('({' + options + '})') : {};
		options.columns = [[{field:'code',title:'机构编号',width:80},{field:'name',title:'机构名称',width:200,formatter:function(value, row, index){
			if (isSysUser() && row.id != row.companyId) {
				row.name = value + '-' + getOrgNameByOrgId(row.companyId);
				return row.name;
			}
			return value;
		}}]];
		options.mode = 'local';
		options.fitColumns = true;
		options.pageNumber = 1;
		if (!options.panelWidth)
			options.panelWidth = 280;
		if (!options.idField)
			options.idField = 'id';
		if (!options.textField)
			options.textField = 'name';
		if (!options.pagination)
			options.pagination = true;
		if (!options.pageSize)
			options.pageSize = 50;
		if (!options.filter) {
			options.filter = function(q, row) {
				return row.code.indexOf(q) == 0 || row.name.indexOf(q) != -1;
			};
		}
		if (!options.loadFilter) {
			options.loadFilter = function(data) {
	        	if (typeof data.length == 'number' && typeof data.splice == 'function') {
	                data = {
	                    total: data.length,
	                    rows: data
	                }
	            }
	            var dg = $(this);
	            var pager = dg.datagrid('getPager');
	            pager.pagination({
	                onSelectPage:function(pageNum, pageSize){
	                	options.pageNumber = pageNum;
	                    pager.pagination('refresh',{
	                        pageNumber:pageNum,
	                        pageSize:pageSize
	                    });
	                    dg.datagrid('loadData',data);
	                }
	            });
	            if (!data.originalRows)
	                data.originalRows = data.rows;
	            var start = (options.pageNumber - 1) * parseInt(options.pageSize);
	            var end = start + parseInt(options.pageSize);
	            data.rows = data.originalRows.slice(start, end);
	            return data;
	        }
		};
		getOrgsForSelectByCache(function(rows, ary) {
			var combo = ary[0];
			var options = ary[1];
			var gridData = rows;
			options.data = rows;
			combo.combogrid(options);
			var grid = combo.combogrid('grid');
			grid.datagrid('getPager').pagination({pageSize:options.pageSize,pageNumber:1,layout:['first','prev','next','last']});
			var textbox = combo.combogrid('textbox').parent().find('.textbox-text');
			$(textbox[0]).css('text-transform','uppercase');
			$(textbox[0]).keyup(function (e) {
				if (gridData) {
					grid.datagrid('options').pageNumber = 1;
					var text = $(this).val().toUpperCase();
					if (text) {
						var rowData = [];
						for (var i = 0; i < gridData.length; i++)
							if (gridData[i].code.indexOf(text) == 0 || gridData[i].name.indexOf(text) != -1)
								rowData.push(gridData[i]);
						grid.datagrid('loadData', rowData);
					} else {
						grid.datagrid('loadData', gridData);
					}
				};
				$(this).val(text);
		    });
			$(textbox[0]).blur(function (e) {
				var exist = false;
				var text = $(this).val().toUpperCase();
				var data = grid.datagrid('getData').originalRows;
				for (var i = 0; i < data.length; i++) {
					if (data[i].code == text || data[i].name == text) {
						combo.combogrid('setValue', data[i].id);
						exist = true;
						break;
					}
				}
				if (exist == false)
					combo.combogrid('setValue', '');
			});
		}, [$(els[i]), options], limitCompany, onlyCompany);
	}
}
function showSysTypeCombo() {
	var els = $('.systype-combo');
	for (var i = 0; i < els.length; i++) {
		var options = $(els[i]).attr('data-options');
		options = options ? eval('({' + options + '})') : {};
		options.columns = [[{field:'type',title:'类型分类',width:100},{field:'code',title:'类型编码',width:100},{field:'name',title:'类型名称',width:60}]];
		options.mode = 'local';
		options.fitColumns = true;
		options.pageNumber = 1;
		if (!options.panelWidth)
			options.panelWidth = 280;
		if (!options.idField)
			options.idField = 'id';
		if (!options.textField)
			options.textField = 'name';
		if (!options.pagination)
			options.pagination = true;
		if (!options.pageSize)
			options.pageSize = 50;
		if (!options.filter) {
			options.filter = function(q, row) {
				return row.code.indexOf(q) == 0 || row.name.indexOf(q) != -1;
			};
		}
		if (!options.loadFilter) {
			options.loadFilter = function(data) {
	        	if (typeof data.length == 'number' && typeof data.splice == 'function') {
	                data = {
	                    total: data.length,
	                    rows: data
	                }
	            }
	            var dg = $(this);
	            var pager = dg.datagrid('getPager');
	            pager.pagination({
	                onSelectPage:function(pageNum, pageSize){
	                	options.pageNumber = pageNum;
	                    pager.pagination('refresh',{
	                        pageNumber:pageNum,
	                        pageSize:pageSize
	                    });
	                    dg.datagrid('loadData',data);
	                }
	            });
	            if (!data.originalRows)
	                data.originalRows = data.rows;
	            var start = (options.pageNumber - 1) * parseInt(options.pageSize);
	            var end = start + parseInt(options.pageSize);
	            data.rows = data.originalRows.slice(start, end);
	            return data;
	        }
		};
		getSysTypesForSelectByCache(function(rows, ary) {
			var combo = ary[0];
			var options = ary[1];
			var gridData = rows;
			options.data = rows;
			combo.combogrid(options);
			var grid = combo.combogrid('grid');
			grid.datagrid('getPager').pagination({pageSize:options.pageSize,pageNumber:1,layout:['first','prev','next','last']});
			var textbox = combo.combogrid('textbox').parent().find('.textbox-text');
			$(textbox[0]).css('text-transform','uppercase');
			$(textbox[0]).keyup(function (e) {
				if (gridData) {
					grid.datagrid('options').pageNumber = 1;
					var text = $(this).val().toUpperCase();
					if (text) {
						var rowData = [];
						for (var i = 0; i < gridData.length; i++)
							if (gridData[i].code.indexOf(text) == 0 || gridData[i].name.indexOf(text) != -1)
								rowData.push(gridData[i]);
						grid.datagrid('loadData', rowData);
					} else {
						grid.datagrid('loadData', gridData);
					}
				};
				$(this).val(text);
		    });
			$(textbox[0]).blur(function (e) {
				var exist = false;
				var text = $(this).val().toUpperCase();
				var data = grid.datagrid('getData').originalRows;
				for (var i = 0; i < data.length; i++) {
					if (data[i].code == text || data[i].name == text) {
						combo.combogrid('setValue', data[i].id);
						exist = true;
						break;
					}
				}
				if (exist == false)
					combo.combogrid('setValue', '');
			});
		}, undefined, [$(els[i]), options]);
	}
}
function addEnterKeyForm(formId) {
	var textbox = $('#' + formId).find('.textbox');
	for (var i = 0; i < textbox.length; i++) {
		var text = textbox[i].childNodes, el;
		if (text.length == 2) {
			el = $(text[0]);
		} else {
			el = $(text[1]);
		}
		el.data('index', i);
		el.keyup(function (e) {
	        var code = e.keyCode ? e.keyCode : e.which;
	        if (code == 13) {
	        	var index = $(this).data('index');
	        	if (index == textbox.length - 1) {
	        		var links = $('#' + formId).find('.easyui-linkbutton');
	        		for (var j = 0; j < links.length; j++) {
	        			if (links[j].onclick) {
	        				$(links[j]).click();
	        				break;
	        			}
	        		}
	        	} else {
	        		do {
		        		var next = $(textbox[index + 1]).find('input[type="text"]');
		        		if (next.length == 0)
		        			next = $(textbox[index + 1]).find('textarea');
		        		var readonly = next.attr('readonly');
		        		if (next.parent().attr('class').indexOf('combo') == -1 && (readonly == 'readonly' || readonly == true)) {
		        			index++;
		        		} else {
		        			next.focus();
		        			break;
		        		}
	        		} while(index < textbox.length - 1);
	        	}
	        }
	    });
	}
}
function checkAccess(role, access) {
	if (role) {
		var value = Math.pow(2, access);
		return parseInt(role & value) == value;
	}
	return false;
}
function grantRights(ary) {
	var rs = 0;
	for(var i = 0; i < ary.length; i++)
		rs = rs | parseInt(Math.pow(2, ary[i]));
	return rs;
}
function postForm(param) {
	if (param.url && param.form) {
		var elForm = $('#' + param.form);
		if (elForm.form('enableValidation').form('validate')) {
			if (param.before && param.before.call(this, param) == false)
				return;
//			openRequestMask();
			elForm.ajaxSubmit({
				url:param.url,
				success:function(data, statusText, xhr) {
					closeRequestMask();
					if (param.success) {
						param.success.call(this, data, statusText, xhr);
					} else {
						infoTip(param.info ? param.info : '操作成功！');
						var formId = elForm.attr('id');
						if (formId && formId.length > 3) {
							var winId = formId.substr(3, formId.length - 3);
							if (winId)
								closeDialog('win' + winId);
						}
						if (param.task)
							param.task.call(this, data, statusText, xhr);
					}
				},
				error:function(data, statusText, xhr) {
					closeRequestMask();
					var rs = eval('('+data.responseText+')');
					errorMsg(rs.exception.message);
				}
			});
		}
	}
}
function openRequestMask(content) {
	var mask = '<div id="pageLoadMask"><div class="window-mask" style="z-index:999;opacity:0.7"></div><div class="mask-msg" style="z-index:1000;" ondblclick="closeMask(this)">'+(content?content:'正在加载中，请稍候。。。')+'</div></div>';
	$(mask).appendTo($(parent.frameBody));
}
function closeMask(target) {
	$(target).parent().remove();
}
function closeRequestMask() {
	$('#pageLoadMask', parent.document).remove();
}
// function getRequestMaskValue() {
// 	if ($(parent.frameBody)) {
// 		var maskIds = $(parent.frameBody).data('loadMaskId');
// 		if (maskIds)
// 			return $('#' + maskIds[maskIds.length - 1], parent.document).attr('formId');
// 	}
// 	return undefined;
// }
function openDialog(id, title) {
	$('#'+id).dialog({
		title:title,
	    iconCls:'icon-save',
	    cache:false,
	    modal:true
	}); 
	var formId = id.substr(3, id.length - 3);
	if ($('#fom' + formId).length != 0) {
		$('#fom' + formId).form('clear');
		$('#fom' + formId).form('disableValidation');
	}
	$('#'+id).dialog('open');
}
function closeDialog(id) {
	$('#'+id).dialog('close');
}

function openWin(id, title) {
	$('#'+id).dialog({
		title:title,
	    iconCls:'icon-save',
	    cache:false,
	    modal:true
	}).dialog('open');
	$('#form' + id.slice(3)).form('reset').form('disableValidation');
}
function closeWin(id) {
	$('#'+id).dialog('close');
}
function validateForm(id) {
	return $('#'+id).form('enableValidation').form('validate');
}

function resetForm(id) {
	$('#'+id).form('reset');
}
function infoMsg(msg) {
	$.messager.alert('信息提示', msg, 'info');
}
function errorMsg(msg) {
	$.messager.alert('错误提示', msg, 'error');
}
function infoTip(msg) {
	$.messager.show({title:'信息提示', msg:'<div class="messager-icon messager-info"></div><div>' + msg + '</div>', showType:'slide', style:{right:'',bottom:''}, top:document.body.scrollTop+document.documentElement.scrollTop, timeout:2000});
}
function errorTip(msg) {
	$.messager.show({title:'错误提示', msg:'<div class="messager-icon messager-error"></div><div>' + msg + '</div>', showType:'slide', style:{right:'',bottom:''}, top:document.body.scrollTop+document.documentElement.scrollTop, timeout:3000});
}
function confirmMsg(msg, fn, args) {
	$.messager.confirm('确认操作', msg, function(r) {
		if (r)
			fn.apply(this, args);
	});
}
function getComboboxText(id, value) {
	var options = $('#' + id).combobox('options');
	var ary = $('#' + id).combobox('getData');
	for (var i = 0; i < ary.length; i++)
		if (ary[i][options.valueField] == value)
			return ary[i][options.textField];
	return value;
}
function queryData(gridId, formId) {
	if (formId) {
		var param = $.extend($('#'+gridId).datagrid('options').queryParams, $('#'+formId).getValues());
		$('#'+gridId).datagrid('load', param);
	} else {
		$('#'+gridId).datagrid('load', $('#'+gridId).datagrid('options').queryParams);
	}
	$('#'+gridId).datagrid('clearSelections');
}
function handRowStyle(index, row) {
	return 'cursor:pointer';
}
function currentDateString(date, link) {
	if (!date)
		date = new Date();
	var y = date.getFullYear();
	var m = date.getMonth()+1;
	var d = date.getDate();
	if (link == undefined)
		link = '-';
	return y + link + (m < 10 ? ('0' + m) : m) + link + (d < 10 ? ('0' + d) : d);
}
function currentDateTimeString(date, dlink, dtlink, tlink) {
	if (!date)
		date = new Date();
	var y = date.getFullYear();
	var m = date.getMonth()+1;
	var d = date.getDate();
	var h = date.getHours();
	var mi = date.getMinutes();
	var s = date.getSeconds();
	if (dlink == undefined)
		dlink = '-';
	if (dtlink == undefined)
		dtlink = ' ';
	if (tlink == undefined)
		tlink = ':';
	return y + dlink + (m < 10 ? ('0' + m) : m) + dlink + (d < 10 ? ('0' + d) : d)  + dtlink + (h < 10 ? ('0' + h) : h)  + tlink + (mi < 10 ? ('0' + mi) : mi)  + tlink + (s < 10 ? ('0' + s) : s);
}
function addDays(d, n) {//复制并操作新对象，避免改动原对象
    var t = new Date(d);
    t.setDate(t.getDate() + n);
    return t;
}
function addMonths(d, n) {//日期+月。日对日，若目标月份不存在该日期，则置为最后一日
    var t = new Date(d);
    t.setMonth(t.getMonth() + n);
    if (t.getDate() != d.getDate()) { t.setDate(0); }
    return t;
}
function createPageBar(store,items) {
	if (items == undefined)
		items = [];
	return new Ext.toolbar.Paging({
		store:store,
		displayInfo:true,
		beforePageText:'Page',
		afterPageText:'of {0}',
		displayMsg:'Displaying topics {0} - {1} of {2}',
		emptyMsg:'No topics to display',
		items:items
	});
}
function addFloat(arg1, arg2) {
	var r1, r2, m; 
	try {r1=arg1.toString().split(".")[1].length;}catch(e){r1=0;} 
	try {r2=arg2.toString().split(".")[1].length;}catch(e){r2=0;} 
	m = Math.max(r1, r2);
	var v1, v2;
	if (r1 == 0) {
		v1 = arg1 * Math.pow(10, m);
	} else {
		v1 = arg1.toString().replace('.', '');
		if (r1 < m)
			v1 = parseInt(v1) * Math.pow(10, m - r1);
	}
	if (r2 == 0) {
		v2 = arg2 * Math.pow(10, m);
	} else {
		v2 = arg2.toString().replace('.', '');
		if (r2 < m)
			v2 = parseInt(v2) * Math.pow(10, m - r2);
	}
	r1 =  (parseInt(v1) + parseInt(v2)).toString();
	r2 = r1.length - m;
	if (r2 == 0 && m > 0)
		return '0.' + r1;
	if (r2 < 0 && m > 0) {
		r2 = Math.pow(10, Math.abs(r2));
		return '0.' + r2.toString().substr(1, r2.toString().length - 1) + r1;
	}
	return parseFloat(r1.substr(0, r2) + '.' + r1.substr(r2, m)); 
}
function subFloat(arg1, arg2) {
	var r1, r2, m; 
	try {r1=arg1.toString().split(".")[1].length;}catch(e){r1=0;} 
	try {r2=arg2.toString().split(".")[1].length;}catch(e){r2=0;} 
	m = Math.max(r1, r2);
	var v1, v2;
	if (r1 == 0) {
		v1 = arg1 * Math.pow(10, m);
	} else {
		v1 = arg1.toString().replace('.', '');
		if (r1 < m)
			v1 = parseInt(v1) * Math.pow(10, m - r1);
	}
	if (r2 == 0) {
		v2 = arg2 * Math.pow(10, m);
	} else {
		v2 = arg2.toString().replace('.', '');
		if (r2 < m)
			v2 = parseInt(v2) * Math.pow(10, m - r2);
	}
	return parseFloat((parseInt(v1) - parseInt(v2)) / Math.pow(10, m)); 
}
function mulFloat(arg1, arg2, precision) {
	var r1, r2, m; 
	try {r1=arg1.toString().split(".")[1].length;}catch(e){r1=0;} 
	try {r2=arg2.toString().split(".")[1].length;}catch(e){r2=0;} 
	var v1, v2;
	if (r1 == 0) {
		v1 = arg1;
	} else {
		v1 = arg1.toString().replace('.', '');
	}
	if (r2 == 0) {
		v2 = arg2;
	} else {
		v2 = arg2.toString().replace('.', '');
	}
	m = parseFloat(parseInt(v1) * parseInt(v2) / Math.pow(10, r1 + r2));
	if (isNaN(precision))
		return m;
	return roundFloat(m, precision);
}
function mulFloatMax(arg1, arg2, precision) {
	var r1, r2, m; 
	try {r1=arg1.toString().split(".")[1].length;}catch(e){r1=0;} 
	try {r2=arg2.toString().split(".")[1].length;}catch(e){r2=0;} 
	var v1, v2;
	if (r1 == 0) {
		v1 = arg1;
	} else {
		v1 = arg1.toString().replace('.', '');
	}
	if (r2 == 0) {
		v2 = arg2;
	} else {
		v2 = arg2.toString().replace('.', '');
	}
	m = parseFloat(parseInt(v1) * parseInt(v2) / Math.pow(10, r1 + r2));
	if (isNaN(precision))
		return m;
	return maxRoundFloat(m, precision);
}
function divFloat(arg1, arg2, precision) {
	var r1, r2, m; 
	try {r1=arg1.toString().split(".")[1].length;}catch(e){r1=0;} 
	try {r2=arg2.toString().split(".")[1].length;}catch(e){r2=0;} 
	m = Math.max(r1, r2);
	var v1, v2;
	if (r1 == 0) {
		v1 = arg1 * Math.pow(10, m);
	} else {
		v1 = arg1.toString().replace('.', '');
		if (r1 < m)
			v1 = parseInt(v1) * Math.pow(10, m - r1);
	}
	if (r2 == 0) {
		v2 = arg2 * Math.pow(10, m);
	} else {
		v2 = arg2.toString().replace('.', '');
		if (r2 < m)
			v2 = parseInt(v2) * Math.pow(10, m - r2);
	}
	m = parseFloat(parseInt(v1) / parseInt(v2));
	if (isNaN(precision))
		return m;
	return roundFloat(m, precision);
}
function divFloatMax(arg1, arg2, precision) {
	var r1, r2, m; 
	try {r1=arg1.toString().split(".")[1].length;}catch(e){r1=0;} 
	try {r2=arg2.toString().split(".")[1].length;}catch(e){r2=0;} 
	m = Math.max(r1, r2);
	var v1, v2;
	if (r1 == 0) {
		v1 = arg1 * Math.pow(10, m);
	} else {
		v1 = arg1.toString().replace('.', '');
		if (r1 < m)
			v1 = parseInt(v1) * Math.pow(10, m - r1);
	}
	if (r2 == 0) {
		v2 = arg2 * Math.pow(10, m);
	} else {
		v2 = arg2.toString().replace('.', '');
		if (r2 < m)
			v2 = parseInt(v2) * Math.pow(10, m - r2);
	}
	m = parseFloat(parseInt(v1) / parseInt(v2));
	if (isNaN(precision))
		return m;
	return maxRoundFloat(m, precision);
}
function roundFloat(value, precision) {
	if (!isNaN(precision)) {
		var r;
		try {r=value.toString().split(".")[1].length;}catch(e){r=0;}
		if (r != 0) {
			var v = value.toString().replace('.', '');
			v = parseInt(v) * Math.pow(10, precision);
			v = Math.round(parseInt(v) / Math.pow(10, r));
			return parseInt(v) / Math.pow(10, precision);
		}
	}
	return value;
}
function maxRoundFloat(value, precision) {
	if (!isNaN(precision)) {
		var r;
		try {r=value.toString().split(".")[1].length;}catch(e){r=0;}
		if (r != 0) {
			var v = value.toString().replace('.', '');
			v = parseInt(v) * Math.pow(10, precision);
			v = Math.ceil(parseInt(v) / Math.pow(10, r));
			return parseInt(v) / Math.pow(10, precision);
		}
	}
	return value;
}
function getValue(value) {
	return value ? value : '';
}
function formatTime(value) {
	if (value == null)
		return '';
	var val = new Date(value);
	var year = parseInt(val.getYear()) + 1900;
	var month = parseInt(val.getMonth()) + 1;
	month = month > 9 ? month : ('0' + month);
	var date = parseInt(val.getDate());
	date = date > 9 ? date : ('0' + date);
	var hours = parseInt(val.getHours());
	hours = hours > 9 ? hours : ('0' + hours);
	var minutes = parseInt(val.getMinutes());
	minutes = minutes > 9 ? minutes : ('0' + minutes);
	var seconds = parseInt(val.getSeconds());
	seconds = seconds > 9 ? seconds : ('0' + seconds);
	var time = year + '-' + month + '-' + date + ' ' + hours + ':' + minutes + ':' + seconds;
	return time;
}
function formatNumber(value, len) {
	if (value) {
		var val = '';
		for (var i = 0; i < len - value.toString().length; i++)
			val = val + '0';
		return val + value;
	} else {
		var val = '';
		for (var i = 0; i < len; i++)
			val = val + '0';
		return val + '1';
	}
}
function openTabPage(id, name, url) {
	if ($.isFunction(window.loadPage)) {
		loadPage(id, name, url, true);
	} else {
		parent.loadPage(id, name, url, true);
	}
}
function getContextPath() {
	return _contextPath == undefined ? parent._contextPath : _contextPath;
}
function getCurUser() {
	return _curUser == undefined ? parent._curUser : _curUser;
}
function getFileUrl(filePath) {
	if (_fileServer == undefined) {
		if (parent._fileServer == undefined) {
			_fileServer = parent.parent._fileServer;
		} else {
			_fileServer = parent._fileServer;
		}
	}
//	if (_fileServer.indexOf('/free/data/readImage.file') == -1)
//		return _fileServer + '/' + filePath;
//	return _fileServer + '?path=' + filePath;
	return _fileServer + filePath;
}
function isSysUser() {
	return getCurUser().companyId == 1;
}
function getAllSysTypes() {
	return _allSysTypes == undefined ? parent._allSysTypes : _allSysTypes;
}
function getAllOrgs() {
	return _allOrgs == undefined ? parent._allOrgs : _allOrgs;
}
function getAllCountries() {
	return _allCountries == undefined ? parent._allCountries : _allCountries;
}
function getAllCurrencies() {
	return _allCurrencies == undefined ? parent._allCurrencies : _allCurrencies;
}
function getAllProducts() {
	return _allProducts == undefined ? parent._allProducts : _allProducts;
}
function getAllCategories() {
	var categories = _allCategories == undefined ? parent._allCategories : _allCategories, vals = [];
	for (var i = 0; i < categories.length; i++) {
		if (categories[i].parentId == undefined) {
			vals.push(categories[i]);
		}
	}
	return vals;
}
function getOrgByOrgId(id) {
	var orgs = getAllOrgs();
	if (orgs)
		for (var i = 0; i < orgs.length; i++)
			if (orgs[i].id == id)
				return orgs[i];
	return undefined;
}
function getOrgNameByOrgId(id) {
	var org = getOrgByOrgId(id);
	if (org)
		return org.name;
	return undefined;
}
function getCurrencyByCode(code) {
	var currencies = getAllCurrencies();
	if (currencies)
		for (var i = 0; i < currencies.length; i++)
			if (currencies[i].code == code)
				return currencies[i];
	return undefined;
}
function getOrderStates() {
	var systypes = getAllSysTypes(), types = [];
	for (var i = 0; i < systypes.length; i++)
		if (systypes[i].type == 'ORDER_STATE')
			types.push(systypes[i]);
	return types;
}
function getOrgsForSelect() {
	var data = [], child = [];
	var orgs = getAllOrgs();
	var sysTypes = getAllOrgSysTypes();
	var childType = sysTypes[sysTypes.length - 1].typeCode;
	var siteType = sysTypes[sysTypes.length - 2].typeCode;
	for (var i = 0; i < orgs.length; i++) {
		if (orgs[i].orgType == childType) {
			if (getCurUser().orgType == siteType && getCurUser().orgId == orgs[i].parentOrg)
				child.push(orgs[i]);
		} else {
			data.push(orgs[i]);
		}
	}
	for (var i = 0; i < child.length; i++)
		data.push(child[i]);
	return data;
}
function skuRule(grid) {
	var skus = getAllSkus();
	$.extend($.fn.validatebox.defaults.rules, {  
	    sku: {  
	        validator: function(value, param) {
	        	for (var i = 0; i < skus.length; i++)
	        		if (skus[i] == value)
	        			return true;
	            return false;
	        },  
	        message: '输入的SKU不存在！'  
	    },
	    resku: {  
	        validator: function(value, param) {
	        	if (grid) {
	        		var rows = $('#' + grid).datagrid('getRows');
	            	for (var i = 0; i < rows.length; i++)
	            		if (i != param[i] && rows[i].sku == value)
	            			return false;
	        	}
	            return true;
	        },
	        message: '输入的SKU已存在！'  
	    }
	});
}

/**
 * 将form属性转化为JSON对象，支持复选框和select多选
 * @param {Object} $
 * @memberOf {TypeName} 
 * @return {TypeName} 
 */
(function($){
	$.fn.serializeJson = function(){
	var serializeObj = {};
	var array = this.serializeArray();
	$(array).each(function(){
		if(serializeObj[this.name]){
			if($.isArray(serializeObj[this.name])){
				serializeObj[this.name].push(this.value);
			}else{
				serializeObj[this.name]=[serializeObj[this.name],this.value];
			}
		}else{
			serializeObj[this.name]=this.value;
		}
	});
	return serializeObj;
};
})(jQuery);

function isNum(s) {
    var r = /^[1-9]\d*$/;
    return r.test(s);
}