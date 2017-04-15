<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>员工账号管理</title>
	<%@ include file="/common.jsp"%>
	<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
	<style type="text/css">
		html, body {height: 100%;}
		.form-row {margin: 15px 0;}
		.form-cloumn {width: 100%;}
		.form-cloumn .text {display: inline-block; margin-right: 8px;}
		.role-selects {margin-left: 64px; margin-top: -1px; font-size: 0;}
		.role-selects label {display: inline-block; width: 33.33%; margin-bottom: 5px; font-size: 14px; cursor: pointer;}
		.role-selects input {position: relative; top: 2px;}
		.role-list {margin: 10px;}
		.role-wrapper {float: left;}
		.role-item {margin: 20px; padding: 10px; border: 1px solid #d9d9d9; border-radius: 2px; color: #999; font-size: 14px; background-color: #fcfcfc;}
		.role-item .setting {float: right; line-height: 1; padding: 5px; cursor: pointer;}
		.role-item .setting:hover {color: #666;}
		.role-item .icon-setting {float: right; height: 14px; width: 14px; margin-left: 5px; background: url(${pageContext.request.contextPath}/images/icon-setting.png) no-repeat center/100%;}
		.role-item .body {clear: both; padding-top: 2px;}
		.role-item .img {display: block; margin: 10px auto;}
		.role-item .name {color: #333; font-size: 16px; text-align: center; margin-bottom: 10px;}
		.role-item .dsec {margin-bottom: 10px; height: 57px;}
		.role-item .num {height: 19px;}
		.role-item .num span {color: red;}
		.role-item .bottom {height: 31px; line-height: 31px; text-align: center; margin: 20px 0 5px;}
		.role-item .bottom .u-btn:first-child {margin-right: 10px;}
		.role-item .bottom .u-btn.add {margin-right:0; background-color:#009cd3; color:#fff;}
		.role-item .bottom .u-btn.add:hover {background-color: #08b2ee; border-color: #08b2ee;}
		.role-del {float: right; color: #009cd3; cursor: pointer;}
		.role-del:hover {color: #2ec1f5;}
		.page-tip {font-size: 14px; padding: 30px 0 0 30px;}
		.page-tip img {vertical-align: bottom;}
		@media screen and (min-width: 1700px) {
			.role-wrapper {width: 16.666%;}
		}
		@media screen and (min-width: 1420px) and (max-width: 1699px) {
			.role-wrapper {width: 20%;}
		}
		@media screen and (min-width: 1140px) and (max-width: 1419px) {
			.role-wrapper {width: 25%;}
		}
		@media screen and (max-width: 1139px) {
			.role-wrapper {width: 33.333%;}
		}
	</style>
</head>
<body style="margin:0;">
	<div style="height:100%;overflow:auto;">
		<div class="page-tip">
			<img src="${pageContext.request.contextPath}/images/role-tip.png">
			根据员工的职能选择角色，然后新增账号。可以自定义角色，进行权限配置。
		</div>
		<div id="role-list" class="role-list clearfix">
			<div class="role-wrapper">
				<div class="role-item">
					<div style="height:24px;"></div>
					<div class="body">
						<img class="img" src="${pageContext.request.contextPath}/images/role-add.png">
						<div class="name">自定义</div>
						<div class="dsec">配置自定义角色，并在该角色下配置员工账号，灵活管理商户平台权限。</div>
						<div class="num"></div>
					</div>
					<div class="bottom">
						<span id="add-role" class="u-btn u-btn-lg add">新增角色</span>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div id="winAccount" class="easyui-dialog" data-options="closed:true,buttons:'#accountBtns'" style="width:400px;height:370px;padding:15px 25px 25px;">
		<form id="formAccount">
			<input type="hidden" name="id">
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">登录帐号</div>
					<input class="easyui-textbox" name="username" data-options="prompt:'请输入登录帐号',required:true,validType:['account','length[6,12]']">
					<div style="color:#999;font-size:12px;margin:2px 0 0 67px;">6-12位字符，允许数字及大、小写字母</div>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">员工姓名</div>
					<input class="easyui-textbox" name="realName" data-options="prompt:'请输入员工姓名',required:true">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">联系手机</div>
					<input class="easyui-textbox" name="mobile" data-options="prompt:'请输入联系手机',required:true,validType:['length[7,12]','allNum']">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text" style="float:left;">角色权限</div>
					<div id="role-selects" class="role-selects"></div>
				</div>
			</div>
		</form>
		<div id="accountBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveAccount()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeWin('winAccount');">取消</a>
		</div>
	</div>
	<div id="winAccountList" class="easyui-dialog" data-options="closed:true" style="width:900px;padding:10px;">
		<span class="u-btn u-btn-lg" style="float:right;margin-right:0;" onclick="delAccount()">批量删除账号</span>
		<div style="margin-bottom:10px;">   
            <form id="accountQueryForm" class="search-form">
                <div class="form-item">
					<input class="easyui-textbox" name="realName" data-options="prompt:'员工姓名'">
                </div>
            </form>
            <sec:authorize access="find">
                <div class="search-btn" onclick="queryData('accountList','accountQueryForm')">查询</div>
            </sec:authorize>
        </div>
        <table id="accountList" class="easyui-datagrid" data-options="striped:true,fitColumns:true,idField:'id'" style="width:100%;height:500px;">
			<thead>
				<tr>
					<th data-options="checkbox:true,field:'',width:20,align:'center'"></th>
					<th data-options="field:'realName',width:120,align:'center'">员工姓名</th>
					<th data-options="field:'username',width:120,align:'center'">登录账号</th>
					<th data-options="field:'bound',width:300,align:'center',formatter:formatterBound">角色权限</th>
					<th data-options="field:'createTime',width:170,align:'center'">创建时间</th>
					<th data-options="field:'enabled',width:50,align:'center',formatter:formatterState">状态</th>
					<th data-options="field:'operate',width:300,align:'center',formatter:formatterOperate">操作</th>
				</tr>
			</thead>
		</table>
	</div>
	<div id="winRole" class="easyui-dialog" data-options="closed:true,onClose:uncheckAccess,buttons:'#roleBtns'" style="width:600px;height:600px;padding:10px;">
		<form id="formRole">
			<input type="hidden" name="id">
			<div class="form-row" style="margin-top:0;">
				<div class="form-cloumn form-item">
					<div class="text">角色名称</div>
					<input id="role-name" class="easyui-textbox" name="name" data-options="prompt:'请输入角色名称',required:true">
					<span id="delRole" class="role-del">删除该角色</span>
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text">角色备注</div>
					<input class="easyui-textbox" name="remark" data-options="prompt:'32个字以内',validType:'length[0,32]'" style="width:400px;">
				</div>
			</div>
			<div class="form-row">
				<div class="form-cloumn form-item">
					<div class="text" style="float:left;">选择权限</div>
					<ul id="accessTree" class="easyui-tree" data-options="checkbox:true,animate:true,textField:'name'" style="width:495px;margin-left:64px;"></ul>
				</div>
			</div>
		</form>
		<div id="roleBtns">
			<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveRole()">保存</a>
			<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeWin('winRole');">取消</a>
		</div>
	</div>
	<script id="role-tpl" type="text/html">
		{{each rows as role i}}
		<div class="role-wrapper">
			<div class="role-item">
				<div class="setting">
					配置权限<i class="icon-setting"></i>
				</div>
				<div class="body">
					<img class="img" src="{{role.logo}}">
					<div class="name">{{role.name}}</div>
					<div class="dsec">{{role.remark}}</div>
					<div class="num">该角色目前已配置 <span>{{role.userCount}}</span> 个账号。</div>
				</div>
				<div class="bottom">
					<span class="u-btn u-btn-lg add-account">新增账号</span>
					<span class="u-btn u-btn-lg manage-account">管理账号</span>
				</div>
			</div>
		</div>
		{{/each}}
	</script>
	<script type="text/javascript">
		var rolesObj = {}; 

		$.extend($.fn.validatebox.defaults.rules,{
		    allNum: {
		    	validator: function(value) {
		    		return +value === +value;
		    	},
		    	message: '必须全为数字'
		    },
		    account: {
		    	validator: function(value) {
		    		var regx = /^[0-9a-zA-Z]*$/;
		    		return regx.test(value);
		    	},
		    	message: '只能包含数字以及大小写字母'
		    }
		});

		//formatter
		function formatterState(val) {
			if (val) {
				return '启用';
			} else {
				return '禁用';
			}
		}

		function formatterBound(arr) {
			var names = [];
			$.each(arr, function(i, n) {
				names[i] = rolesObj[n];
			})
			return names.join('、');
		}

		function formatterOperate(val, row) {
			var resetPassword = '<span class="u-btn" onclick="resetAccountPassword('+row.id+')">重置密码</span>',
				edit = '<span class="u-btn" onclick="updateAccount('+row.id+')">编辑</span>',
				enable = '<span class="u-btn" onclick="changeAccountState('+row.id+', 1)">启用</span>',
				disable = '<span class="u-btn" onclick="changeAccountState('+row.id+', 0)">禁用</span>',
				del = '<span class="u-btn" onclick="delAccount('+row.id+')">删除</span>';

			if (row.enabled) {
				return resetPassword + edit + disable + del;
			} else {
				return resetPassword + edit + enable + del;
			}
		}

		//获取角色列表
		function getRoles() {
			$.ajax({
				url: '${pageContext.request.contextPath}/orgnization/account/findRoles.json',
				task: function(r) {
					var rows = r.rows;
					var customRoles = rows.filter(function(n) {
						return n.type == 0;
					})
					var defaultRoles = rows.filter(function(n) {
						return n.type != 0 && n.type != 3;//不显示超级管理员角色
					}).sort(function(a, b) {
						return a.type > b.type ? 1 : -1;
					})
					r.rows = rows = defaultRoles.concat(customRoles);

					var roleImgs = {4:'role-manager', 5:'role-operate', 6:'role-finance', 7:'role-service', 8:'role-replenishment'};
					for (var i = 0; i < rows.length; i++) {
						rolesObj[rows[i].id] = rows[i].name;
						var logo = roleImgs[rows[i].type] || 'role-added';
						rows[i].logo = '${pageContext.request.contextPath}/images/' + logo + '.png';
					}
					$('#role-list .role-wrapper').not(':last-child').remove();
					$('#role-list').prepend(template('role-tpl', r));

					$('#role-selects').empty();
					$('#role-list .role-wrapper').not(':last-child').each(function(i) {
						//缓存角色数据
						$(this).data('role', rows[i]);
						//生成用户权限选择列表
						$('#role-selects').append('<label><input type="checkbox" value="'+rows[i].id+'">'+rows[i].name+'</label>');
					})
				}
			})
		}

		//递归生成tree数据
		function loadAccess(data) {
			var nodes = [];
			for (var i = 0; i < data.length; i++) {
				var node = {};
				node.id = data[i].id;
				node.text = data[i].name;
				node.attributes = data[i];					
				if (data[i].url) {
					node.iconCls = 'icon-tip';
					var roles = [];
					for (var j = 0; j < _rightsData.length; j++)
						if (checkAccess(data[i].rights, _rightsData[j][0]))
							roles.push({id:node.id+'_'+_rightsData[j][0], text:_rightsData[j][1], checked:checkAccess(data[i].grants, _rightsData[j][0]), iconCls:'icon-sum', style:{display:'inline'}});
					node.children = roles;
					node.state = 'open';
				} else {
					node.state = 'closed';
					if (data[i].menus) {
						node.children = loadAccess(data[i].menus);
					}
				}
				nodes.push(node);
			}
			return nodes;
		}

		//获取所选角色权限数据
		function calcRights() {
			var allRights = [];
			var rootNode = $('#accessTree').tree('getRoots');
			var children = $('#accessTree').tree('getChildren', rootNode.target);
			for(var i = 0; i < children.length; i++) {
				if (children[i].attributes && children[i].attributes.url) {
					var curRights = {}, ary = [];
					var optNodes = $('#accessTree').tree('getChildren', children[i].target);
					for (var j = 0; j < optNodes.length; j++)
						if (optNodes[j].checked)
							ary.push(parseInt(optNodes[j].id.split('_')[1]));
					curRights.menuId = children[i].attributes.id;
					curRights.value = grantRights(ary);
					if (curRights.value > 0)
						allRights.push(curRights);//JSON.stringify 需IE8以上版本
				}
			}
			return allRights;
		}

		//编辑用户
		function updateAccount(id) {
			var row = $('#accountList').datagrid('getRows')[$('#accountList').datagrid('getRowIndex', id)];
			openWin('winAccount', '编辑账号');
			$('#formAccount').form('load', row);
			$.each(row.bound, function(i, n) {
				$('#role-selects input[value="'+n+'"]').prop('checked', true);
			})
		}

		//保存用户
		function saveAccount() {
			if (!validateForm('formAccount')) return;
			var account = $('#formAccount').getValues();
			account.roleIds = [];
			$('#role-selects input:checked').each(function() {
				account.roleIds.push($(this).val());
			})
			if (account.roleIds.length == 0) {
				infoMsg('请选择角色权限');
				return;
			}

			if (account.id) {
				var url = '${pageContext.request.contextPath}/orgnization/account/updateAccountAndRoles.json';
			} else {
				var url = '${pageContext.request.contextPath}/orgnization/account/addAccountAndSetRoles.json';
			}
			$.ajax({
				url: url,
				data: $.param(account, true),
				info: '保存成功！',
				task: function(r) {
					closeWin('winAccount');
					if (account.id) {
						$('#accountList').datagrid('reload');
					}
				}
			})
		}

		//删除用户
		function delAccount(id) {
			var ids = [];
			if (id) {
				ids.push(id);
			} else {
				var selections = $('#accountList').datagrid('getSelections');
				for (var i = 0; i < selections.length; i++) {
					ids.push(selections[i].id);
				}
			}
			if (ids.length == 0) {
				infoMsg('请选择要删除的账号！');
				return;
			}

			confirmMsg('确定删除所选账号吗？', function() {
				$.ajax({
					url: '${pageContext.request.contextPath}/orgnization/account/deleteAccounts.json',
					data: $.param({ids: ids}, true),
					task: function(r) {
						$('#accountList').datagrid('reload');
					}
				})
			})
		}

		//启用禁用用户
		function changeAccountState(id, type) {
			var url = '';
			if (type) {
				url = '${pageContext.request.contextPath}/orgnization/account/saveAccountsEnable.json'
			} else {
				url = '${pageContext.request.contextPath}/orgnization/account/saveAccountsDisable.json'
			}
			confirmMsg('确定'+(type ? '启用' : '禁用')+'该用户吗？', function() {
				$.ajax({
					url: url,
					data: $.param({ids: [id]}, true),
					task: function() {
						$('#accountList').datagrid('reload');
					}
				})
			})
		}

		//重置用户密码
		function resetAccountPassword(id) {
			confirmMsg('确定将该用户的密码重置为初始密码吗？', function() {
				$.ajax({
					url: '${pageContext.request.contextPath}/orgnization/account/saveResetAccountPassword.json',
					data: {userId: id},
					info: '重置密码成功！'
				})
			})
		}

		//保存角色
		function saveRole() {
			if (!validateForm('formRole')) return;
			var role = $('#formRole').getValues();
			var authorities = calcRights();

			if (authorities.length == 0) {
				infoMsg('请为角色配置权限！');
				return;
			}

			//与之前的权限对比
			var oldRights = $('#formRole').data('rights');
			if (oldRights.length == authorities.length) {
				var lowToHigh = function(a, b) {
					return a.menuId > b.menuId ? 1: -1;
				}
				oldRights.sort(lowToHigh);
				authorities.sort(lowToHigh);
				for (var i = 0; i < oldRights.length; i++) {
					if (oldRights[i].menuId != authorities[i].menuId || oldRights[i].value != authorities[i].value) {
						role.authorities = authorities;
						break;
					}
				}
			} else {
				role.authorities = authorities;
			}

			if (role.authorities) {
				$.each(role.authorities, function(i, n) {
					n.roleId = +role.id || 0;
					role.authorities[i] = JSON.stringify(n);
				})
			}

			$.ajax({
				url: '${pageContext.request.contextPath}/orgnization/account/saveRoleAndRights.json',
				data: $.param(role, true),
				info: '保存角色成功！',
				task: function(r) {
					closeWin('winRole');
					getRoles();
				}
			})
		}

		//关闭winRole后清空权限资源的勾选
		function uncheckAccess() {
			$('#accessTree').tree('uncheck', $('#accessTree').tree('getRoot').target);
		}

		$(function() {
			//新增角色
			$('#add-role').on('click', function() {
				openWin('winRole', '新增角色');
				$('#formRole input[name="id"]').val('');
				$('#formRole').data('rights', []);
				$('#delRole').hide();
			})

			//编辑角色
			$('#role-list').on('click', '.setting', function() {
				var role = $(this).parents('.role-wrapper').data('role');
				openWin('winRole', '编辑角色');
				$('#delRole').show();
				$('#formRole').form('load', role);
				$.ajax({
					sync: true,
					url: '${pageContext.request.contextPath}/orgnization/account/findRights.json',
					data: {roleId: role.id},
					task: function(data) {
						//缓存权限数据
						$('#formRole').data('rights', $.extend(true, {}, data).rows);

						var rootNode = $('#accessTree').tree('getRoot');
						var children = $('#accessTree').tree('getChildren', rootNode.target);
						for(var i = 0; i < children.length; i++) {
							if (children[i].attributes && children[i].attributes.url) {
								var curRights = undefined;
								for (var j = 0; j < data.rows.length; j++) {
									if (children[i].attributes.id == data.rows[j].menuId) {
										curRights = data.rows[j].value;
										data.rows.splice(j, 1);
										break;
									}
								}
								var optNodes = $('#accessTree').tree('getChildren', children[i].target);
								for (var j = 0; j < optNodes.length; j++) {
									var check = checkAccess(curRights, parseInt(optNodes[j].id.split('_')[1]));
									if (check) {
										$('#accessTree').tree('check', optNodes[j].target);
									} else {
										$('#accessTree').tree('uncheck', optNodes[j].target);
									}
								}
							}
						}
					}
				})
			})

			//删除角色
			$('#delRole').on('click', function() {
				var name = $('#role-name').textbox('getValue');
				confirmMsg('确定将角色 <span style="color:red;">'+name+'</span> 删除吗？', function() {
					var id = $('#formRole input[name="id"]').val();
					$.ajax({
						url: '${pageContext.request.contextPath}/orgnization/account/deleteRoles.json',
						data: $.param({ids:[id]}, true),
						task: function() {
							closeWin('winRole');
							getRoles();
						}
					})
				})
			})

			//新增用户
			$('#role-list').on('click', '.add-account', function() {
				var role = $(this).parents('.role-wrapper').data('role');
				openWin('winAccount', '新增<span style="color:#00C19F;">'+role.name+'</span>账号');
				$('#formAccount input[name="id"]').val('');
				$('#role-selects input[value="'+role.id+'"]').prop('checked', true);
			})

			//查看账户列表
			$('#role-list').on('click', '.manage-account', function() {
				var role = $(this).parents('.role-wrapper').data('role');
				openWin('winAccountList', '<span style="color:#00C19F;">'+role.name+'</span>员工列表');
				$('#accountList').datagrid({
					url: '${pageContext.request.contextPath}/orgnization/account/findAccountsByRoleAndRealName.json',
					queryParams: {roleId:role.id},
					loadFilter: function(data) {
						data.rows = data.userList;
						return data;
					}
				})
			})

			$('#accessTree').tree({
				url: '${pageContext.request.contextPath}/system/access/findMenusForAccess.json',
				loadFilter: function(data, parentNode) {
					if (data.rows) {
						var nodes = loadAccess(data.rows);
						if (!parentNode) {
							var rootNode = {};
							rootNode.id = 'root';
							rootNode.text = '权限资源';					
							rootNode.children = nodes;
							return [rootNode];
						}
						return nodes;
					}
					return [data];
				},
				onLoadSuccess:function(node, data) {
					if (node == null && data[0].id == 'root') {
						var rootNode = $('#accessTree').tree('getRoot');
						$('#accessTree').tree('expand', rootNode.target);
						var nodes = $('#accessTree').tree('getChildren', rootNode.target);
						var del = false;
						for (var i = 0; i < nodes.length; i++) {
							if ($('#accessTree').tree('isLeaf', nodes[i].target)) {
								var el = nodes[i].target;
								if (!$(el).parent().attr('style')) {
									$(el).parent().attr('style', 'display:inline');
									$(el).attr('style', 'display:inline');
									if (del) {
										var els = $(el).children();
										for (var j = 0; j < els.length; j++) {
											var cls = $(els[j]).attr('class');
											if (cls == 'tree-indent') {
												$(els[j]).remove();
											} else if (cls.indexOf('tree-line') != -1) {
												$(els[j]).attr('class', cls.replace(' tree-line', ''));
											}
										}
									}
								}
								del = true;
							} else {
								del = false;
							}
						}
					}
				}
			})

			$('#accountList').datagrid({
				onClickCell: function(index, field) {
					if (field === 'operate') {
						var self = this;
						setTimeout(function(){
							$(self).datagrid('unselectRow', index);
						},0)	
					}
				}
			})

			getRoles();
		})
	</script>
</body>
</html>