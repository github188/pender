<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<title>自游邦运营平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo_12.png"/>
<%@ include file="/common.jsp"%>
<style type="text/css">
.mask-msg {
  position:absolute;
  left:50%;
  top:50%;
  height:16px;
  margin-left:-85.5px;
  margin-top:-20px;
  line-height:16px;
  padding:10px 5px 10px 30px;
  border-width:2px;
  border-style:solid;
  border-color:#95b8e7;
  font-size:12px;
  background: url('${pageContext.request.contextPath}/scripts/easyui/themes/default/images/loading.gif') no-repeat scroll 5px center #ffffff;
}
ul {
	list-style: none;
	padding-left: 0;
	margin: 0;
}
.first-menu>li {
	border-bottom: 1px solid rgba(107, 108, 109, 0.19);
}
.first-menu>li>a {
	display: block;
	font-size: 14px;
	line-height: 50px;
	color: #fff;
	padding-left: 20px;
	cursor: pointer;
}
.first-menu>li>a:after {
	content: '';
	float: right;
	display: inline-block;
	width: 7px;
	height: 7px;
	border-top: 1px solid #fff;
	border-right: 1px solid #fff;
	margin: 22px 20px 0 0;
	transform: rotate(45deg);
	transition: all 0.35s ease-in-out;
}
.first-menu>li>a.alone:after {
	display: none;
}
.first-menu>li.active>a {
	color: #1cc09f;
}
.first-menu>li.active>a:after {
	border-color: #1cc09f;
	transform: rotate(135deg);
}
.second-menu {
	display: none;
	background-color: #17191B;
}
.second-menu>li>a {
	display: block;
	font-size: 13px;
	line-height: 45px;
	color: #fff;
	padding-left: 35px;
	cursor: pointer;
}
.second-menu>li.active>a {
	color: #1cc09f;
}
.sys-btn {
	color: #fff!important;
	outline: none!important;
}
.sys-btn:hover, .sys-btn:active {
	background-color: rgb(40, 54, 67)!important;
	border-color: rgb(40, 54, 67)!important;
}
.tab-ctn {
	box-sizing: border-box;
	height: 100%;
	border-top: 5px solid #f5f5f5;
	overflow: hidden;
}
.tabs-title {
	font-size: 14px;
}
.tabs li a.tabs-inner {
	border-radius: 4px 4px 0 0;
	padding: 0 15px;
}
.tabs li.tabs-selected a.tabs-inner {
	border-bottom: 1px solid #f5f5f5;
	background-color: #f5f5f5;
	font-weight: normal;
}
</style>
<script type="text/javascript">
var debug = true;
var cookieId = '_system_tabs' + '<sec:authentication property="principal.id"/>';
//遮罩，防止样式加载速度慢导致页面错乱
var delayTime, removeMask = false;
$.parser.onComplete = function(){
	if (delayTime)
		clearTimeout(delayTime);
	delayTime = setTimeout(function() {
		if (!removeMask) {
			removeMask = true;
		}
	}, 500);
}
$(document).ready(function () {
	_contextPath = '${pageContext.request.contextPath}';
	_curUser = ${loginUser};
	_token = '${_csrf.token}';
	_fileServer = '${fileServer}';
  	initUserCache(_curUser.companyCode, _curUser.username); 
    addCodeHistoryToCache(_curUser.username);
	initSyncDataByCache(refreshPage);
});
function refreshPage() {
	if (removeMask) {
		$('#divInitMask').remove();
	} else {
		removeMask = true;
	}
}
function logout() {
	sessionStorage.setItem(cookieId, JSON.stringify({sel:-2}));
	fomLogout.submit();
}
function addTab(title, href) {
	var activeTab = $('#sys-tabs').tabs('getSelected');
	var activeIndex = $('#sys-tabs').tabs('getTabIndex', activeTab);
	$('#sys-tabs').tabs('add', {
		title: title,
		content: '<div class="tab-ctn"><iframe src="'+href+'" width="100%" height="100%" frameborder="0" scrolling="no" style="background-color:#fff;"></iframe></div>',
		closable: true,
		index: activeIndex+1
	})
}
function addOrSelectTab(dom) {
	var title = $(dom).data('title');
	var href = $(dom).data('href');
	var isTabExists = $('#sys-tabs').tabs('exists', title);
	if (!isTabExists) {
		addTab(title, href);
		openRequestMask();
		setTimeout(closeRequestMask, 5000);//防止特殊情况导致遮罩层没关闭
	} else {
		$('#sys-tabs').tabs('select', title);
	}
}
function updateTab(title, href) {
	var activeTab = $('#sys-tabs').tabs('getSelected');
	$('#sys-tabs').tabs('update', {
		tab: activeTab,
		options: {
			title: title,
			content: '<div class="tab-ctn"><iframe src="'+href+'" width="100%" height="100%" frameborder="0" scrolling="no" style="background-color:#fff;"></iframe></div>'
		}
	})
}
$(function(){
	$('#sys-menu').on('click', '>.first-menu>li>a', function(){
		var $this = $(this);
		$this.siblings().slideToggle().end().parent().toggleClass('active').siblings().removeClass('active').children('ul').slideUp();
		if ($this.parent().index() == 0) {
			$this.parent().addClass('active');
			$('#sys-menu').find('.second-menu>li').removeClass('active');
			addOrSelectTab(this);
		}
	})

	$('#sys-menu').on('click', '.second-menu>li>a', function(){
		var $this = $(this);
		$this.parents('#sys-menu').find('.second-menu>li').removeClass('active');
		$this.parent().addClass('active');
		addOrSelectTab(this);
	})
})
</script>
</head>
<body id="frameBody" class="easyui-layout">
	<div id="divInitMask">
		<div class="window-mask" style="z-index:9010;opacity:0.6"></div>
		<div class="mask-msg" style="z-index:9011;">正在同步基础数据，请稍候。。。</div>
	</div>
	<div id="menuBar" data-options="region:'north',border:false" style="height:40px;line-height:40px;padding:0px">
		<div style="background:#32414e;color:#fff;padding:0px;width:100%;height:100%;">
			<div style="float:left;padding-left:15px;">欢迎您！&nbsp;<sec:authentication property="principal.nickname"/></div>
      		<div style="float:right;">
      			<form id="fomLogout" action="${pageContext.request.contextPath}/logout" method="post">
      				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        			<a href="javascript:void(0)" class="easyui-linkbutton sys-btn" data-options="plain:true,iconCls:'icon-man-w'" data-title="个人信息" data-href="${pageContext.request.contextPath}/orgnization/userInfo.jsp" onclick="addOrSelectTab(this)">个人信息</a>
        			<a href="javascript:void(0)" class="easyui-linkbutton sys-btn" data-options="plain:true,iconCls:'icon-cancel'" onclick="logout()" style="margin-right:15px;">退出系统</a>
      			</form>
      		</div>
		</div>
	</div>
	<div id="sys-menu" data-options="region:'west',border:false" style="width:200px;background-color:rgb(40, 54, 67);">
		<ul class="first-menu">
			<li class="active"><a class="alone" data-title="首页" data-href="${pageContext.request.contextPath}/index/forward.do">首页</a></li>
			<c:forEach items="${menus}" var="menu">
     		<li>
 				<a>${menu.name}</a>
 				<ul class="second-menu">
				<c:forEach items="${menu.menus}" var="submenu">
					<li><a data-title="${submenu.name}" data-href="${submenu.url}/forward.do">${submenu.name}</a></li>
				</c:forEach>
 				</ul>
     		</li>
     		</c:forEach>
		</ul>
	</div>
	<div data-options="region:'center',border:false" style="position:reatlive;background-color:#f5f5f5;overflow:hidden;">
		<div id="sys-tabs" class="easyui-tabs" data-options="tabHeight:35,border:false" style="height:100%;">
			<div title="首页" data-options="closable:true">
				<div class="tab-ctn">
					<iframe src="${pageContext.request.contextPath}/index/forward.do" width="100%" height="100%" frameborder="0" scrolling="no" style="background-color:#fff;"></iframe>
				</div>
			</div>
		</div>
	</div>
</body>
</html>