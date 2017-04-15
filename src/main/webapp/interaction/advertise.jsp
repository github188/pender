<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>广告管理</title>
<%@ include file="/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/webuploader/webuploader.css">
<!-- <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/uploadCreater/uploadCreater.css"> -->
<style>
    .adv-detail {float: left; width: 54%;}
    .adv-img {float: left; width: 46%; padding: 10px 0; box-sizing: border-box; height: 544px;}
    .screen-model {height: 100%; border: 1px solid #e5e5e5;}
    .screen-model .title {line-height: 35px; text-indent: 15px;}
    .screen-model .title+div {border-top: 1px solid #e5e5e5;}
    .adv-top {position: relative; height: 30%; box-sizing: border-box; border-bottom: 1px solid #e5e5e5; opacity: 0;}
    .adv-top .file-item {width: 33.33%; float: left;}
    .adv-top .file-item .file-inner {position: relative; width: 90px; height: 90px; margin: 0 auto; line-height: 120px; text-align: center;}
    .adv-top .file-item .file-inner .del {position: absolute; right: 0; top: 0; width: 16px; line-height: 16px; text-align: center; background-color: rgba(0,0,0,0.5); color: #fff; font-size: 16px; cursor: pointer;}
    .adv-bg {position: absolute; left: 0; top: 0; height: 100%; width: 100%; opacity: 0;}
    .adv-bottom {position: relative; height: 40%; margin-top: 146px; box-sizing: border-box; border-top: 1px solid #e5e5e5; opacity: 0;}
    .adv-bottom .queueList, .adv-bg .queueList {position: absolute; top: 0; left: 0; width: 100%; height: 100%; overflow: hidden; line-height: 226px; text-align: center;}
    .progress-item {display: inline-block; width: 150px; height: 8px; border: 1px solid #0075bf; vertical-align: middle; position: relative; top: -1px; margin-left: 10px;}
    .progress-item i {float: left; width: 0%; background-color: #0075bf; height: 8px;}
    /*修复datebox的样式错乱*/
    .easyui-datebox+span .textbox-icon {width: 18px!important; height: 26px!important;}
    .easyui-datebox+span .textbox-text {width: 146px!important;}
</style>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/webuploader/webuploader.min.js"></script>
<!-- <script type="text/javascript" src="${pageContext.request.contextPath}/scripts/uploadCreater/uploadCreater.js"></script> -->
<script type="text/javascript">
	var key;
    var storeList;//记录所有店铺信息
    var topUploader, bgUploader, btUploader;
    var delFileIds = [];//记录要删除的文件的id
    var topUploaderLimit = 3;//锁屏上部可上传文件数
    var initedFiles;//记录需要上传的文件
	
	$(function() {
        //广告列表
		$('#advsGrid').datagrid({
            singleSelect: true,
			url:'${pageContext.request.contextPath}/interaction/advertise/find.json'
		});

        //获取店铺信息
        $.ajax({
            url: '${pageContext.request.contextPath}/interaction/advertise/findPointPlaces.json',
            success: function(r) {
                storeList = r.pointPlaceList;
                var storeData = [{
                    //id: 'all',
                    text: '所有店铺',
                    children: []
                }];
                for (var i = 0; i < storeList.length; i++) {
                    storeData[0].children.push({id:storeList[i].id, text:storeList[i].pointName});
                }
                $('#advStore').combotree('loadData', storeData);
            }
        })
    });
	
	// 保存屏保广告信息
	function doSaveScreenAdv() {
        closeDialog('winProgress');
        closeRequestMask();
	    var row = $('#fomScreenAdv').getValues();
	    row.key = key;
        if (delFileIds.length != 0) {
	       row.fileIds = delFileIds;
        }
        var stores = $('#advStore').combotree('getValues'); 
        for (var i = 0; i < stores.length; i++) {
            if (stores[i] == 'undefined' || stores[i] == '') {
                stores.splice(i, 1);//去除‘所有店铺’的id
            }
        }
        row.pointIds = stores;
	    $.ajax({
	        url:'${pageContext.request.contextPath}/interaction/advertise/save.json',
	        data:$.param(row, true),
	        async: false,
	        info:'保存屏保广告信息成功！',
	        task:function(data, statusText, xhr) {
	        	$('#advsGrid').datagrid('reload');
	        	closeWinScreenAdv();
                closeDialog('winScreenAdv');
	        }
	    });
	}
	
	function saveAdvs() {
        if ($('#fomScreenAdv').form('enableValidation').form('validate')) {
            var row = $('#fomScreenAdv').getValues();
            //投放时间、播放时间比较
            if (row.type == '2') {
                if (row.startDate > row.endDate) {
                    infoMsg('广告投放起始日期不可大于结束日期');
                    return;
                }
                if (row.beginTime > row.endTime) {
                    infoMsg('广告播放起始时间不可大于结束时间');
                    return;
                }
                var td = new Date;
                var tdstr = td.getFullYear() + '-' + ((td.getMonth()+1)<10?('0'+(td.getMonth()+1)):(td.getMonth()+1)) + '-' + (td.getDate()<10?('0'+td.getDate()):td.getDate());
                if (row.endDate < tdstr) {
                    infoMsg('广告投放结束日期不可小于当前日期');
                    return;
                }
            }

            //doSaveScreenAdv();

            var position = $('#advPosition').combobox('getValue');
            if (position == 1) {
                initedFiles = topUploader.getFiles('inited');
                if (getFileNum() + topUploader.getFiles('complete').length != 3) {//校验图片数量
                    infoMsg('请上传三张广告图片');
                    return;
                }
                if (initedFiles.length == 0) {//如果没有新添加的图片
                    doSaveScreenAdv();
                    return;
                }
                topUploader.option('formData', {key:key, module:_fileType.advertise_top});
                topUploader.upload();
            } else if (position == 2) {
                initedFiles = btUploader.getFiles('inited');
                if (getFileNum() + btUploader.getFiles('complete').length != 1) {
                    infoMsg('请上传广告图片或视频');
                    return;
                }
                if (initedFiles.length == 0) {//如果没有新添加的图片/视频
                    doSaveScreenAdv();
                    return;
                }
                btUploader.option('formData', {key:key, module:_fileType.advertise_bottom});
                btUploader.upload();
            } else if (position == 3) {
                initedFiles = bgUploader.getFiles('inited');
                if (getFileNum() + bgUploader.getFiles('complete').length != 1) {
                    infoMsg('请上传广告图片');
                    return;
                }
                if (initedFiles.length == 0) {//如果没有新添加的图片
                    doSaveScreenAdv();
                    return;
                }
                bgUploader.option('formData', {key:key, module:_fileType.advertise_bg});
                bgUploader.upload();
            }
            openDialog('winProgress', '文件上传中，请稍候……');
            $('#uploadingNum').html(initedFiles.length);
            $('#progressList').empty();
            for (var i = 0; i < initedFiles.length; i++) {
                $('#progressList').append('<li style="line-height:30px;">第'+(i+1)+'个文件：<span id="progress-item-'+i+'" class="progress-item"><i></i></span></li>')
            }
        }
	}

    //判断文件数量是否符合要求，锁屏上部广告文件数量：3，下部：1，背景：1
    function getFileNum() {
        if ($('#screenId').val() != '') {//广告编辑
            var row = $('#advsGrid').datagrid('getSelected');
            var fileArr = row.images.split(';')
            return fileArr.length + initedFiles.length - delFileIds.length;
        } else {//新增广告
            return initedFiles.length;
        }
    }

    function uploadProgress(file, percentage) {
        var index = initedFiles.indexOf(file);
        $('#progress-item-'+index).find('i').css('width', percentage*100+'%');
        if (index == initedFiles.length-1 && percentage == 1) {
            openRequestMask('正在等待服务器响应');
        }
    }

    function uploadFinished() {
        doSaveScreenAdv();
    }
    
    //判断pick-btn中内容是否为空，若为空则初始化上传组件
    function initUploader() {
        if ($('#winScreenAdv .pick-btn').html() == '') {
            topUploader = WebUploader.create({
                swf: '${pageContext.request.contextPath}/scripts/webuploader/Uploader.swf',
                server: '${pageContext.request.contextPath}/session/data/saveUploadFile.json?_csrf='+_token,
                pick: {id: '#topPicker', label: '添加图片'},
                accept: {title: 'Images', extensions: 'gif,jpg,jpeg,bmp,png', mimeTypes: 'image/gif,image/jpg,image/jpeg,image/bmp,image/png'}
                //fileNumLimit: topUploaderLimit
            });
            topUploader.onFileQueued = function(file) {
                if ($('#adv-position .adv-top .file-item').length == 3) {//如果已有三张图片
                    infoMsg('只允许上传3张图片');
                    topUploader.removeFile(file, true);
                    return;
                }
                $item = $('<div id="tu_'+file.id+'" class="file-item"></div>');
                $inner = $('<div class="file-inner"></div>');
                $delIcon = $('<span class="del">×</span>');
                $delIcon.on('click', function(){
                    $('#tu_'+file.id).remove();
                    if (file.id.indexOf('WU_FILE') == -1) {
                        delFileIds.push(file.id);
                        topUploader.option('fileNumLimit', topUploaderLimit+1)
                    } else {
                        topUploader.removeFile(file, true);
                    }
                });
                $delIcon.appendTo($inner);
                $inner.appendTo($item);
                $item.appendTo('#topUploader .queueList');

                if (file.src) {//如果是载入已有图片
                    var img = $('<img src="'+file.src+'" style="width:100%;height:100%;">');
                    $('#tu_'+file.id+' .file-inner').append(img);
                    return;
                }

                topUploader.makeThumb( file, function( error, src ) {
                    if ( error ) {
                        $('#tu_'+file.id+' .file-inner').append(file.name);
                        return;
                    }

                    var img = $('<img src="'+src+'" style="width:100%;height:100%;">');
                    $('#tu_'+file.id+' .file-inner').append(img);
                }, 120, 120);
            }
            topUploader.onUploadFinished = uploadFinished;
            topUploader.onUploadProgress = uploadProgress;

            btUploader = WebUploader.create({
                swf: '${pageContext.request.contextPath}/scripts/webuploader/Uploader.swf',
                server: '${pageContext.request.contextPath}/session/data/saveUploadFile.json?_csrf='+_token,
                timeout: 0,
                pick: {id: '#btPicker', label: '选择图片/视频'},
                accept: {title: 'Images', extensions: 'gif,jpg,jpeg,bmp,png,mp4', mimeTypes: 'image/video/*'},
                fileNumLimit: 2//实际文件限制数量为1，设置为2可直接切换图片而不必先删除
            });
            btUploader.onFileQueued = function(file) {
                //$('#btPicker').hide();
                $('#btUploader').hover(function(){
                    if ($('#adv-position .adv-bottom').css('opacity') == 0) return;
                    $('#btPicker').stop().fadeIn();
                },function(){
                    if ($('#adv-position .adv-bottom').css('opacity') == 0) return;
                    $('#btPicker').stop().fadeOut();
                });

                if (file.src) {//如果是载入已有图片
                    if (file.src.indexOf('mp4') != -1) {//如果是视频文件
                        var img = $('<video src="'+file.src+'" controls="controls" style="width:100%;height:100%;"></video>')
                    } else {
                        var img = $('<img src="'+file.src+'" style="width:100%;height:100%;">');
                    }
                    $('#btUploader .queueList').html(img);
                    return;
                }

                btUploader.makeThumb( file, function( error, src ) {
                    if ( error ) {
                        $('#btUploader .queueList').html(file.name);
                        return;
                    }

                    var img = $('<img src="'+src+'" style="width:100%;">');
                    $('#btUploader .queueList').html(img);
                }, 350, 194);
            }
            btUploader.onBeforeFileQueued = function() {
                if ($('#screenId').val() != '') {//如果是广告编辑，把旧的文件的id推送进delFileIds进行删除
                    var row = $('#advsGrid').datagrid('getSelected');
                    var id = row.images.split(',')[0];
                    if (id != '') delFileIds.push(id);
                }
                btUploader.reset();//清空队列
            }
            btUploader.onUploadFinished = uploadFinished;
            btUploader.onUploadProgress = uploadProgress;

            bgUploader = WebUploader.create({
                swf: '${pageContext.request.contextPath}/scripts/webuploader/Uploader.swf',
                server: '${pageContext.request.contextPath}/session/data/saveUploadFile.json?_csrf='+_token,
                pick: {id: '#bgPicker', label: '选择图片'},
                accept: {title: 'Images', extensions: 'gif,jpg,jpeg,bmp,png', mimeTypes: 'image/*'},
                fileNumLimit: 2,//实际文件限制数量为1，设置为2可直接切换图片而不必先删除
                fileSingleSizeLimit:100 * 1024 * 1024   //100M
            });
            bgUploader.onFileQueued = function(file) {
                //$('#bgPicker').hide();
                $('#bgUploader').hover(function(){
                    if ($('#adv-position .adv-bg').css('opacity') == 0) return;
                    $('#bgPicker').stop().fadeIn();
                },function(){
                    if ($('#adv-position .adv-bg').css('opacity') == 0) return;
                    $('#bgPicker').stop().fadeOut();
                });

                if (file.src) {//如果是载入已有图片
                    var img = $('<img src="'+file.src+'" style="width:100%;height:100%;">');
                    $('#bgUploader .queueList').html(img);
                    return;
                }

                bgUploader.makeThumb( file, function( error, src ) {
                    if ( error ) {
                        $('#bgUploader .queueList').html(file.name);
                        return;
                    }

                    var img = $('<img src="'+src+'" style="width:100%;">');
                    $('#bgUploader .queueList').html(img);
                }, 350, 488);
            }
            bgUploader.onBeforeFileQueued = function() {
                if ($('#screenId').val() != '') {//如果是广告编辑，把旧的文件的id推送进delFileIds进行删除
                    var row = $('#advsGrid').datagrid('getSelected');
                    var id = row.images.split(',')[0];
                    if (id != '') delFileIds.push(id);
                }
                bgUploader.reset();//清空队列
            }
            bgUploader.onUploadFinished = uploadFinished;
            bgUploader.onUploadProgress = uploadProgress;
            bgUploader.onError = function(error) {
                if (error == 'Q_EXCEED_SIZE_LIMIT') {
                    parent.errorMsg('超出最大上传文件大小' + videoUploader.options.fileSingleSizeLimit) + '！';
                } else if (error == 'Q_TYPE_DENIED') {
                    parent.errorMsg('不支持上传此文件类型！');
                }
                parent.closeRequestMask();
            };
        }
    }

    function addScreenAdv() {
        key = new Date().getTime();
        openDialog('winScreenAdv', '新增屏幕广告信息');
        var t = $('#advStore').combotree('tree');
        t.tree('check', t.tree('getRoot').target);
        initUploader();
    }
    
    function updateScreenAdv() {
        row = $('#advsGrid').datagrid('getSelected');
        if (row) {
            if (row.status === 2) {
                infoMsg('该广告已结束，不可再编辑！');
                return;
            }
        	key = new Date().getTime();
            openDialog('winScreenAdv', '修改屏幕广告信息');
            showUploader({value: row.advPosition});
            selectAdvType({value: row.type});
            $('#fomScreenAdv').form('load', row);
            // 初始化店铺绑定信息
            var pointPlaces = row.pointPlaces;
            var pointIds = [];
            for (var i = 0; i < pointPlaces.length; i++) {
                pointIds.push(pointPlaces[i].id);
            }
            $('#advStore').combotree('setValues', pointIds);
            if (pointIds.length == storeList.length) {
               $('#advStore').combotree('setText', '所有店铺'); 
            }
            //禁止编辑广告位置
            $('#advPosition').combobox({readonly:true});
            $('#advPosition').combobox('setValue', row.advPosition);
            //载入图片、视频
            initUploader();
            var files = getLoadedFiles(row);
            if (row.advPosition == 1) {
                for (var i = 0; i < files.length; i++) {
                    topUploader.trigger('fileQueued', files[i]);
                }
            } else if (row.advPosition == 2) {
                for (var i = 0; i < files.length; i++) {
                    btUploader.trigger('fileQueued', files[i]);
                }
            } else if (row.advPosition == 3) {
                for (var i = 0; i < files.length; i++) {
                    bgUploader.trigger('fileQueued', files[i]);
                }
            }
        } else {
            infoMsg('请选择需要修改的屏幕广告信息！');
        }
    }

    function getLoadedFiles(row) {
        var files = [];
        var filesStr = row.images;
        var filesStrArr = filesStr.split(';');
        for (var i = 0; i < filesStrArr.length; i++) {
            var fileArr = filesStrArr[i].split(',');
            files.push({id:fileArr[0], name:fileArr[1], src:getFileUrl(fileArr[3])});
        }
        return files;
    }
    
    function delScreenAdv(){
        var rows = $('#advsGrid').datagrid('getSelections');
        if (rows.length == 0) {
            infoMsg('请选择需要删除的屏幕广告！');
        } else {
            confirmMsg('您确定要删除该屏幕广告吗?', doDeleteScreenAdvs, [ rows ]);
        }
    }
    
    function doDeleteScreenAdvs(rows){
        var ids = [];
        for (var i = 0; i < rows.length; i++) {
            ids.push(rows[i].id);
            $.ajax({
               url : '${pageContext.request.contextPath}/interaction/advertise/delete.json',
               data : $.param({
                    'ids' : ids
               }, true),
               info : '所选删除成功！',
               task : function(data, statusText, xhr) {
                    queryData('advsGrid', 'advQueryForm');
                 
                    $('#advsGrid').datagrid('reload');
                }
            });
        }
    }

    function closeWinScreenAdv() {
        topUploader.reset();
        // topUploaderLimit = 3;
        // topUploader.option('fileNumLimit', topUploaderLimit);
        btUploader.reset();
        bgUploader.reset();
        delFileIds = [];
        $('#advPosition').combobox({readonly:false});
        $('#fomScreenAdv .easyui-datebox').datebox({disabled:false, required:true});
        $('#fomScreenAdv .easyui-timespinner').timespinner({disabled:false, required:true});
        $('#topUploader .queueList, #btUploader .queueList, #bgUploader .queueList').empty();
    }

    function formatPreview(value) {
        if (!!value) {
            var path = value.split(';')[0].split(',')[3];
            if (path.indexOf('mp4') != -1) {//如果是视频文件
                return value.split(';')[0].split(',')[1];
            }
            return '<img src="'+getFileUrl(path)+'" style="width:70px;height:70px;margin:10px 0;" />';
        } else {
            return '<img style="width:70px;height:70px;margin:10px 0;" />';
        }
    }

    function formatType(value) {
        switch(value) {
            case 1:
                return '默认';
            case 2:
                return '普通';
            default:
                return value;
        }
    }

    function formatStatus(value) {
        switch(value) {
          case 0:
              return "未开始";
          case 1:
              return "进行中";
          case 2:
              return "结束";
          default:
              return value;
        }
    }

    function formatPosition(value) {
        switch(value) {
            case 1:
                return "屏幕上部";
            case 2:
                return "屏幕下部";
            case 3:
                return "屏幕背景";
          default:
                return value;
        }
    }

    function formatPointPlaces(value) {
        if (value instanceof Array) {
            if (value.length == storeList.length) {
                return '所有店铺';
            }
            var str = '';
            for (var i = 0; i < value.length; i++) {
                str += value[i].pointName + '、';
            }
            return str.slice(0, str.length-1);
        } else {
            return value;
        }
    }

    function isAllStore(node, checked) {
        if (checked) {
            var stores = $('#advStore').combotree('tree');
            var root = stores.tree('getRoot');
            if (!root.checked) return;
            setTimeout(function() {
                $('#advStore').combotree('setText', '所有店铺');
            }, 0)
        }
    }

    function selectAdvType(record) {
        var $dateboxs = $('#fomScreenAdv .easyui-datebox');
        var $timespinners = $('#fomScreenAdv .easyui-timespinner');
        if (record.value == '1') {//默认广告
            $dateboxs.datebox({disabled:true, required:false});
            $dateboxs.datebox('setValue', '');
            $timespinners.timespinner({disabled:true, required:false});
            $timespinners.timespinner('setValue', '');
        } else {
            $dateboxs.datebox({disabled:false, required:true});
            $timespinners.timespinner({disabled:false, required:true});
        }
    }

    function showUploader(record) {
        var $cur = $('#adv-position>div').eq(+record.value-1);
        $cur.css({opacity:1, zIndex:1}).siblings().css({opacity:0, zIndex:0});
        // $cur.find('.webuploader-pick').siblings().css({
        //     width: $cur.find('.webuploader-pick').outerWidth(),
        //     height: $cur.find('.webuploader-pick').outerHeight()
        // })
    }
	
</script>
</head>
<body class="easyui-layout" >
    <div id="winProgress" class="easyui-dialog" style="width:315px;height:195px;padding:10px;font-size:14px;" data-options="closed:true">
        <div style="font-size:16px;">此次共需上传<span id="uploadingNum" style="color:#32a000; margin:0 5px;"></span>个文件</div>
        <ul id="progressList" style="padding-left:20px; list-style:none;"></ul>
    </div>
	<div data-options="region:'north',border:false,split:true" style="padding:15px; height:84px;">
        <form id="advQueryForm" class="search-form">
            <div class="form-item">
                <div class="text">广告名称</div>
                <div class="input">
                    <input class="easyui-textbox" name="advName" data-options="prompt:'广告名称'">
                </div>
            </div>
            <div class="form-item" style="width:377px;">
                <div class="text">投放日期</div>
                <div class="input">
                    <input class="easyui-datebox" name="startDate"> - <input class="easyui-datebox" name="endDate">
                </div>
            </div>
            <div class="form-item">
                <div class="text">广告类型</div>
                <div class="input">
                    <select class="easyui-combobox" name="type" data-options="panelHeight:'auto',editable:false">
                        <option value="">请选择</option>  
                        <option value="2">普通</option>
                        <option value="1">默认</option>
                    </select>
                </div>
            </div>
            <div class="form-item">
                <div class="text">状态</div>
                <div class="input">
                    <select class="easyui-combobox" name="status" data-options="panelHeight:'auto',editable:false">
                        <option value="">请选择</option>
                        <option value="0">未上线</option>
                        <option value="1">进行中</option>
                        <option value="2">已下线</option>
                    </select>
                </div>
            </div>
        </form>
        <sec:authorize access="find">
        <div class="search-btn" onclick="queryData('advsGrid','advQueryForm')">查询</div>
        <div class="search-btn" onclick="resetForm('advQueryForm')">重置</div>
        </sec:authorize>
	</div>
	<div id="screenOpt">
        <sec:authorize access="add,save"><a href="javascript:void(0)" class="icon-add easyui-tooltip" data-options="content:'新增屏保广告信息'" onclick="addScreenAdv()"></a></sec:authorize>
        <sec:authorize access="update,save"><a href="javascript:void(0)" class="icon-edit easyui-tooltip" data-options="content:'修改屏保广告信息'" onclick="updateScreenAdv()"></a></sec:authorize>
        <sec:authorize access="delete"><a href="javascript:void(0)" class="icon-remove easyui-tooltip" data-options="content:'删除屏保广告信息'" onclick="delScreenAdv()"></a></sec:authorize>
    </div>
	<div data-options="region:'center',border:false,headerCls:'list-head',tools:'#screenOpt'" title="广告列表">
        <div style="box-sizing:border-box; height:100%; padding:10px;">
    		<table id="advsGrid" data-options="nowrap:false,striped:true,fit:true,fitColumns:true,idField:'id'">
    			<thead>
    				<tr>
    					<th data-options="checkbox:true,field:'',width:20"></th>
    					<th data-options="field:'images',width:100,align:'center',formatter:formatPreview">广告图片/视频</th>
    					<th data-options="field:'advName',width:150,align:'center'">广告名称</th>
                        <th data-options="field:'type',width:50,align:'center',formatter:formatType">广告类型</th>
                        <th data-options="field:'startDate',width:150,align:'center'">投放起始日期</th>
                        <th data-options="field:'endDate',width:150,align:'center'">投放结束日期</th>
                        <th data-options="field:'advPosition',width:150,align:'center',formatter:formatPosition">广告位置</th>
                        <th data-options="field:'pointPlaces',width:150,align:'center',formatter:formatPointPlaces">投放店铺</th>
    					<th data-options="field:'status',width:80,align:'center',formatter:formatStatus">状态</th>
                        <th data-options="field:'index',width:80,align:'center'">轮播排序</th>
    				</tr>
    			</thead>
    		</table>
        </div>
	</div>
	
	<!-- 屏保广告Dialog-->
	<div id="winScreenAdv" class="easyui-dialog" data-options="closed:true, onClose:closeWinScreenAdv, buttons:'#screenAdvBtns'" style="width:800px;padding:0 10px">
			<form id="fomScreenAdv" method="post">
				<input type="hidden" id="screenId" name="id">
				<input type="hidden" name="orgId">
				<input type="hidden" name="creatime">
                <div class="adv-detail">
                    <div class="form-row">
                        <div class="form-cloumn form-item">
                            <div class="text">广告名称</div>
                            <input name="advName" class="easyui-textbox" data-options="required:true,prompt:'必填项',validType:['length[1,120]']">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-cloumn form-item">
                            <div class="text">广告类型</div>
                            <select class="easyui-combobox" name="type" data-options="required:true,prompt:'必选项',panelHeight:'auto',editable:false,onSelect:selectAdvType">  
                                <option value="2">普通</option>
                                <option value="1">默认</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-cloumn form-item">
                            <div class="text">轮播排序</div>
                            <input class="easyui-numberbox" name="index" data-options="required:true,prompt:'必选项'">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-cloumn form-item" style="width:100%;">
                            <div class="text">广告位置</div>
                            <select id="advPosition" class="easyui-combobox" name="advPosition" data-options="required:true,prompt:'必选项',panelHeight:'auto',editable:false,onSelect:showUploader">
                                <option value="1">锁屏上部</option>
                                <option value="2">锁屏下部</option>
                                <option value="3">锁屏背景</option>
                            </select>
                            <div>所支持图片格式：gif, jpg, jpeg, bmp, png</div>
                            <div>所支持视频格式：mp4(不大于100m)</div>
                        </div>
                    </div>
                    <div class="form-row" id="advUseTime">
                        <div class="form-cloumn form-item" style="width:100%;">
                            <div class="text">投放日期</div>
                            <input name="startDate" class="easyui-datebox" data-options="required:true,editable:false,prompt:'投放起始日期'"> - <input name="endDate" class="easyui-datebox" data-options="required:true,editable:false,prompt:'投放结束日期'">
                        </div>
                    </div>
                    <div class="form-row" id="advPlayTime">
                        <div class="form-cloumn form-item" style="width:100%;">
                            <div class="text">播放时间</div>
                            <input name="beginTime" class="easyui-timespinner" data-options="required:true,editable:false,prompt:'播放起始时间'"> - <input name="endTime" class="easyui-timespinner" data-options="required:true,editable:false,prompt:'播放结束时间'">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-cloumn form-item">
                            <div class="text">请选择投放店铺</div>
                            <input id="advStore" name="advStore" class="easyui-combotree" data-options="multiple:true,onCheck:isAllStore">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-cloumn form-item">
                            <div class="text">请选择投放屏幕类型</div>
                            <select class="easyui-combobox" name="screenType" data-options="required:true,prompt:'必选项',panelHeight:'auto',editable:false">  
                                <option value="2">竖屏</option>
                                <option value="1">横屏</option>
                            </select>
                        </div>
                    </div>
                </div>
            </form>
            <div class="adv-img">
                <div class="screen-model">
                    <div class="title">模板布局</div>
                    <div id="adv-position" style="position:relative;height:488px;">
                        <div class="adv-top">
                            <div id="topUploader" class="uploader">
                                <div class="queueList" style="height:96px;padding:5px 10px;"></div>
                                <div id="topPicker" class="pick-btn" style="text-align:center;"></div>
                            </div>
                        </div>
                        <div class="adv-bottom">
                            <div id="btUploader" class="uploader">
                                <div class="queueList"></div>
                                <div id="btPicker" class="pick-btn" style="text-align:center;top:94px;"></div>
                            </div>
                        </div>
                        <div class="adv-bg">
                            <div id="bgUploader" class="uploader">
                                <div class="queueList"></div>
                                <div id="bgPicker" class="pick-btn" style="text-align:center;top:245px;"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
			<div id="screenAdvBtns">
				<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" href="javascript:void(0)" onclick="saveAdvs();">保存</a>
				<a class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" href="javascript:void(0)" onclick="closeDialog('winScreenAdv');">取消</a>
			</div>
		</div>
	</div>
    <!-- 屏保广告Dialog -->	
</body>
</html>