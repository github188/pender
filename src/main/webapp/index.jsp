<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<title>首页</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo_12.png"/>
<%@ include file="/common.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/template.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/echarts.common.min.js"></script>
<style>
    html, body {height: 100%;}
    body {background-color: #f5f5f5; font-size: 14px; margin: 0;}
    .red {color: #f00;}
    .top-wrap {padding: 10px 8px 10px 0;}

    .notify-ctn {float: right; width: 30%; border-radius: 4px; box-shadow: 0 0 8px #ddd; background-color: #fff;}
    .notify-head {line-height: 38px; text-indent: 7px; border-bottom: 1px solid #e5e5e5;}
    .notify-more {float: right; color: #009cd3; margin-right: 7px; cursor: pointer;}
    .notify-more:hover {color: #06759c;}
    .notify-nav {margin-right: 20px; border: 1px solid #009cd3; border-radius: 3px; margin-top: 8px; color: #009cd3; line-height: 20px; text-indent: 0; cursor: pointer;}
    .notify-nav span {display: inline-block; padding: 0 7px;}
    .notify-nav span.active {color: #fff; background-color: #009cd3;}
    .notify-body {box-sizing: border-box; padding: 10px 7px;}
    .notify-list {height: 100%; overflow: hidden;}
    .notify-item {border: 1px solid #e9e9e9; border-left: 5px solid #e9545d; padding: 5px; margin-bottom: 10px; font-size: 13px;}
    .notify-item:last-child {margin-bottom: 0;}
    .notify-item .info .row {margin-bottom: 12px;}
    .notify-item .info .row .l {float: left; margin-right: 8px;}
    .notify-item .info .row .r {float: right; margin-left: 8px;}
    .notify-item .info .row .c {margin: 0 34px 0 60px; display: block; word-break: break-all;}
    .notify-item .time {line-height: 30px; border-top: 1px solid #e9e9e9;}

    .panel-ctn {margin-right: 30%;}
    .panel-operate-ctn, .panel-data-ctn {float: left; width: 100%; box-sizing: border-box; padding-right: 20px;}
    .panel-operate-ctn .panel-item {float: left; width: 33.33%;}
    .panel-data-ctn .panel-item {float: left; width: 25%;}
    .panel-general {box-shadow: 0 0 8px #ddd; border-radius: 4px;}
    .panel-general .head {line-height: 35px; border-bottom: 1px solid; text-indent: 10px;}
    .panel-general .body {padding: 10px;}
    .panel-operate {margin: 0 10px 20px; background-color: #69bc82; color: #fff;}
    .panel-operate .head {border-color: #fff;}
    .panel-operate .body {height: 65px;}
    .panel-operate .body .btn {display: block; width: 60px; margin: 0 auto; margin-top: 10px; padding: 5px 25px; border: 1px solid #fff; border-radius: 2px; cursor: pointer;}
    .panel-operate .body .btn:hover {background-color: #8fcaa1;}
    .panel-data {margin: 0 10px 20px; background-color: #fff;}
    .panel-data .head {border-color: #e5e5e5;}
    .panel-data .head .sign {float: right; display: block; background-color: #009cd3; color: #fff; line-height: 22px; text-align: center; text-indent: 0; padding: 0 10px; margin: 7px; border-radius: 7px;}
    .panel-data .body .data {height: 40px; font-size: 30px; color: #333;}
    .panel-data .body .yesterday {color: #999; margin-bottom: 5px;}
    .panel-data .body .device-detail {float: right; padding: 2px 10px; border: 1px solid #009cd3; border-radius: 4px; color: #009cd3; margin-top: 10px; cursor: pointer;}
    .panel-data .body .device-detail:hover {background-color: #f5f5f5;}

    .chart-ctn {margin: 0 8px 10px 10px; background-color: #fff; box-shadow: 0 0 8px #ddd; border-radius: 4px;}
    .chart-ctn .head {line-height: 38px; text-indent: 10px; border-bottom: 1px solid #e5e5e5;}
    .time-btn-ctn {font-size: 0; margin-right: 12px; height: 36px;}
    .time-btn {font-size: 12px; padding: 4px 12px; border: 1px solid #e5e5e5; margin-left: -1px; cursor: pointer; -webkit-user-select:none;-moz-user-select:none;-o-user-select:none;user-select:none;}
    .time-btn.active {background-color: rgba(0, 156, 211, 1); border-color: rgba(0, 156, 211, 1); color: #fff;}

    .rank-ctn {margin: 0 8px 10px 10px;}
    .rank-item {float: left; width: 32%; margin-right: 2%; background-color: #fff; border-radius: 4px; box-shadow: 0 0 8px #ddd;}
    .rank-item:last-child {margin-right: 0;}
    .rank-item .head {line-height: 38px; text-indent: 10px; border-bottom: 1px solid #e5e5e5;}
    .rank-item .head .sign {background-color: #009cd3; color: #fff; line-height: 22px; text-align: center; text-indent: 0; padding: 0 10px; border-radius: 7px;}
    .rank-item .head .all {float: right; margin-right: 12px; color: #009cd3; cursor: pointer;}
    .rank-item .head .all:hover {color: #35c0f2;}
    .rank-item .body {padding: 10px; height: 325px;}
    .rank-item .body .item {margin-bottom: 15px;}
    .rank-item .body .item:last-child {margin-bottom: 0;}
    .rank-item .body .item .l {float: left;}
    .rank-item .body .item .r {float: right; width: 80px; background-color: #a54a96; border-radius: 5px; color: #fff; text-align: center;}
    .rank-item .body .item .c {margin: 0 85px 0 30px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;}
</style>
</head>
<body>
<div style="height:100%;overflow:auto">
	<div class="top-wrap">
        <div id="notify-ctn" class="notify-ctn">
            <div class="notify-head">
                <span>消息通知</span>
                <span class="notify-more" id="moreMes">查看更多</span>
                <div id="notify-nav" class="pull-right notify-nav">
                    <span class="active">设备</span><span>库存</span>
                </div>
            </div>
            <div class="notify-body">
                <div id="notify-list" class="notify-list"></div>
            </div>
        </div>
        <div id="panel-ctn" class="panel-ctn clearfix">
            <div class="panel-operate-ctn clearfix">
                <div style="margin-right:-10px;">
                    <div class="panel-item">
                        <div class="panel-general panel-operate">
                            <div class="head">我要开业</div>
                            <div class="body">
                                <span class="btn" onclick="parent.addOrSelectTab(this)" data-title="店铺总览" data-href="store/overview/forward.do">马上开始</span>
                            </div>
                        </div>
                    </div>
                    <div class="panel-item">
                        <div class="panel-general panel-operate">
                            <div class="head">发布商品</div>
                            <div class="body">
                                <span id="linkProductStock" class="btn" onclick="parent.addOrSelectTab(this)" data-title="商品库存管理" data-href="product/productStock/forward.do">马上开始</span>
                            </div>
                        </div>
                    </div>
                    <div class="panel-item">
                        <div class="panel-general panel-operate">
                            <div class="head">平台供货</div>
                            <div class="body">
                                <span class="btn" onclick="parent.addOrSelectTab(this)" data-title="平台供货" data-href="product/platformSupply/forward.do">马上开始</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="panel-data-ctn clearfix">
                <div style="margin-right:-10px;">
                    <div class="panel-item">
                        <div class="panel-general panel-data">
                            <div class="head">销售额<span class="sign">今日</span></div>
                            <div class="body">
                                <span class="data" id="saleAmount-t">0</span>
                                <div class="yesterday">
                                    <span>昨日销售额</span>
                                    <span style="float:right;" id="saleAmount-y">0</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="panel-item">
                        <div class="panel-general panel-data">
                            <div class="head">销售量<span class="sign">今日</span></div>
                            <div class="body">
                                <span class="data" id="saleVolume-t">0</span>
                                <div class="yesterday">
                                    <span>昨日销售量</span>
                                    <span style="float:right;" id="saleVolume-y">0</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="panel-item">
                        <div class="panel-general panel-data">
                            <div class="head">平均单价<span class="sign">今日</span></div>
                            <div class="body">
                                <span class="data" id="average-t">0</span>
                                <div class="yesterday">
                                    <span>昨日平均单价</span>
                                    <span style="float:right;" id="average-y">0</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="panel-item">
                        <div class="panel-general panel-data">
                            <div class="head">退款金额<span class="sign">今日</span></div>
                            <div class="body">
                                <span class="data" id="refund-t">0</span>
                                <div class="yesterday">
                                    <span>昨日退款金额</span>
                                    <span style="float:right;" id="refund-y">0</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="panel-item">
                        <div class="panel-general panel-data">
                            <div class="head red">货道售空提醒</div>
                            <div class="body">
                                <span class="data" style="color:#f00;" id="soldOut">0</span>台
                                <span id="soldOutDetail" class="device-detail">查看详情</span>
                                <div class="yesterday" style="height:19px;"></div>
                            </div>
                        </div>
                    </div>
                    <div class="panel-item">
                        <div class="panel-general panel-data">
                            <div class="head red">网络异常设备<span class="sign">今日</span></div>
                            <div class="body">
                                <span class="data" style="color:#f00;" id="offlineDevice">0</span>台
                                <div class="yesterday" style="height:19px;"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="chart-ctn">
        <div class="head">
            <span>销售统计图</span>
            <div id="charts-nav" class="pull-right time-btn-ctn">
                <span class="time-btn" data-near="1">今日</span>
                <span class="time-btn" data-near="1" data-pull="1">昨日</span>
                <span class="time-btn" data-near="7">最近7天</span>
                <span class="time-btn" data-near="30">最近30天</span>
            </div>
        </div>
        <div id="charts" style="padding:10px; height:400px;"></div>
    </div>
    <div id="rank-ctn" class="rank-ctn clearfix">
        <div class="rank-item">
            <div class="head">
                <span>店铺销售排行榜</span>
                <span class="sign">今日</span>
                <span class="all">查看全部</span>
            </div>
            <div id="store-rank" class="body"></div>
        </div>
        <div class="rank-item">
            <div class="head">
                <span>商品销售额排行榜</span>
                <span class="sign">今日</span>
                <span class="all">查看全部</span>
            </div>
            <div id="pro-amount-rank" class="body"></div>
        </div>
        <div class="rank-item">
            <div class="head">
                <span>商品销售量排行榜</span>
                <span class="sign">今日</span>
                <span class="all">查看全部</span>
            </div>
            <div id="pro-volume-rank" class="body"></div>
        </div>
    </div>
</div>
<div id="winDevice" class="easyui-dialog" data-options="closed:true" style="width:750px;">
    <table id="deviceGrid" class="easyui-datagrid" data-options="fitColumns:true,striped:true,border:false" style="height:500px;">
        <thead>
            <tr>
                <th data-options="field:'factoryDevNo',width:150,align:'center'">设备组号</th>
                <th data-options="field:'pointAddress',width:300,align:'center'">设备地址</th>
                <th data-options="field:'deviceStatus',width:100,align:'center',formatter:formatterDeviceStatus">状态</th>
            </tr>
        </thead>
    </table>
</div>
<div id="winSoldOut" class="easyui-dialog" data-options="closed:true" style="width:900px;">
    <div style="padding:10px;border-bottom:1px solid #ddd;text-align:right;">
        <span class="u-btn" onclick="parent.addOrSelectTab(this)" data-title="店铺库存" data-href="operation/storeStock/forward.do">查看店铺库存</span>
    </div>
    <table id="soldOutGrid" class="easyui-datagrid" data-options="fitColumns:true,striped:true,border:false" style="height:500px;">
        <thead>
            <tr>
                <th data-options="field:'factoryDevNo',width:150,align:'center'">设备组号</th>
                <th data-options="field:'pointName',width:300,align:'center'">所属店铺名称</th>
                <th data-options="field:'pointAddress',width:300,align:'center'">所属店铺地址</th>
                <th data-options="field:'aisleCounts',width:100,align:'center'">货道数量</th>
                <th data-options="field:'saleEmptyQty',width:100,align:'center'">货道售空数量</th>
            </tr>
        </thead>
    </table>
</div>
<div id="winStoreRankAll" class="easyui-dialog" data-options="closed:true" style="width:900px;">
    <table id="storeRankAllGrid" class="easyui-datagrid" data-options="fitColumns:true,striped:true,border:false" style="height:500px;">
        <thead>
            <tr>
                <th data-options="field:'pointName',width:300,align:'center'">店铺名称</th>
                <th data-options="field:'pointAddress',width:300,align:'center'">店铺地址</th>
                <th data-options="field:'orgName',width:300,align:'center'">所属组织</th>
                <th data-options="field:'salesAmount',width:80,align:'center'">销售额</th>
                <th data-options="field:'salesVolume',width:80,align:'center'">销售量</th>
            </tr>
        </thead>
    </table>
</div>
<div id="winProAmountRankAll" class="easyui-dialog" data-options="closed:true" style="width:600px;">
    <table id="proAmountRankAllGrid" class="easyui-datagrid" data-options="fitColumns:true,striped:true,border:false" style="height:500px;">
        <thead>
            <tr>
                <th data-options="field:'productName',width:300,align:'center'">商品名称</th>
                <th data-options="field:'salesAmount',width:80,align:'center'">销售额</th>
                <th data-options="field:'salesRate',width:80,align:'center',formatter:formatterRate">销售额占比</th>
            </tr>
        </thead>
    </table>
</div>
<div id="winProVolumeRankAll" class="easyui-dialog" data-options="closed:true" style="width:600px;">
    <table id="proVolumeRankAllGrid" class="easyui-datagrid" data-options="fitColumns:true,striped:true,border:false" style="height:500px;">
        <thead>
            <tr>
                <th data-options="field:'productName',width:300,align:'center'">商品名称</th>
                <th data-options="field:'salesVolume',width:80,align:'center'">销售量</th>
                <th data-options="field:'salesVolumeRate',width:80,align:'center',formatter:formatterRate">销售量占比</th>
            </tr>
        </thead>
    </table>
</div>
<script id="deviceMesItem" type="text/html">
{{each rows as item i}}
    <div class="notify-item">
        <div class="info">
            <div class="row">
                <span class="l">设备组号</span>
                <span class="r red">{{item.deviceStatus == 2 ? '网络异常' : '待补货'}}</span>
                <span class="c">{{item.factoryDevNo}}</span>
            </div>
            <div class="row">
                <span class="l">设备地址</span>
                <span class="c">{{!item.pointAddress ? '无' : item.pointAddress}}</span>
            </div>
        </div>
        <div class="time red">
            <span>{{item.createTime}}</span>
        </div>
    </div>
{{/each}}
</script>
<script id="prodMesItem" type="text/html">
{{each rows as item i}}
    <div class="notify-item">
        <div class="info">
            <div class="row">
                <span class="l">商品名称</span>
                <span class="r red">{{item.productStatus == 1 ? '库存不足' : item.productStatus}}</span>
                <span class="c">{{item.productName}}</span>
            </div>
        </div>
        <div class="time red">
            <span>{{item.createTime}}</span>
        </div>
    </div>
{{/each}}
</script>
<script id="rankItem" type="text/html">
{{each rankList as item i}}
    <div class="item">
        <div class="l">{{i+1}}</div>
        <div class="r">￥{{item.amount.toFixed(2)}}</div>
        <div class="c">{{item.name}}</div>
    </div>
{{/each}}
</script>
<script type="text/javascript">
    function formatterDeviceStatus(value) {
        if (value == 2) {
            return '网络异常';
        } else if (value == 3) {
            return '待补货';
        } else {
            return value;
        }
    }

    function formatterRate(value) {
        return value + '%';
    }

    //格式化日期
    function getDateFormatter(d) {
        var str = d.getFullYear() + '-' + (d.getMonth()+1) + '-' + d.getDate();
        return formatTime(str);
    }

    //返回开始结束日期，near：开始结束日期相隔天数，pull：开始时间前推天数
    function getDateRange(near, pull) {
        var today = new Date;
        var end = pull ? new Date(today - pull*24*60*60*1000) : today;
        var start = new Date(end - (near-1)*24*60*60*1000);
        return {
            startDate: getDateFormatter(start),
            endDate: getDateFormatter(end)
        }
    }

    $(function() {
        //设置信息通知块高度
        $('#notify-ctn').css('height', $('#panel-ctn .panel-operate-ctn').height() + $('#panel-ctn .panel-data-ctn').height()); 
        $('#notify-ctn .notify-body').css('height', $('#panel-ctn .panel-operate-ctn').height() + $('#panel-ctn .panel-data-ctn').height() - $('#notify-ctn .notify-head').outerHeight()); 

        //信息通知块-滚轮模拟
        $('#notify-ctn .notify-list').on('mousewheel DOMMouseScroll', function(e){
            e.preventDefault();
            var deltaY = (e.originalEvent.wheelDelta && (e.originalEvent.wheelDelta > 0 ? 1 : -1)) ||  // chrome & ie
                (e.originalEvent.detail && (e.originalEvent.detail > 0 ? -1 : 1));
            if (deltaY>0) {
                var scrollTop = $(this).scrollTop()-50;
                $(this).scrollTop(scrollTop);
            } else {
                var scrollTop = $(this).scrollTop()+50;
                $(this).scrollTop(scrollTop);
            }
        })

        //消息通知请求
        $('#notify-nav').on('click', 'span', function() {
            var index = $(this).index();
            $(this).addClass('active').siblings().removeClass('active');
            if (index == 0) {//设备异常
                $.ajax({
                    url: '${pageContext.request.contextPath}/findDeviceLogs.json',
                    data: {page:1, rows:10},
                    task: function(data) {
                        if (data.rows.length == 0) {
                            $('#notify-list').html('<div style="line-height:200px;text-align:center;color:red;">无设备异常消息</div>');
                            return;
                        }
                        var html = template('deviceMesItem', data);
                        $('#notify-list').html(html);
                    }
                })
            } else {//商品异常
                $.ajax({
                    url: '${pageContext.request.contextPath}/findProductLogs.json',
                    data: {page:1, rows:10},
                    task: function(data) {
                        if (data.rows.length == 0) {
                            $('#notify-list').html('<div style="line-height:200px;text-align:center;color:red;">无商品异常消息</div>');
                            return;
                        }
                        var html = template('prodMesItem', data);
                        $('#notify-list').html(html);
                    }
                })
            }
        })

        //查看更多
        $('#moreMes').click(function(){
            var index = $('#notify-nav span.active').index();
            if (index == 0) {
                openDialog('winDevice', '设备异常信息');
                $('#deviceGrid').datagrid({
                    url: '${pageContext.request.contextPath}/findDeviceLogs.json'
                })
            } else {
                $('#linkProductStock').trigger('click');
            }
        })

        $('#notify-nav span').eq(0).trigger('click');


        //系统数据请求
        $.ajax({
            url: '${pageContext.request.contextPath}/findSysData.json',
            task: function(data) {
                $('#saleAmount-t').html(data.salesTodayAmount);
                $('#saleAmount-y').html(data.salesYesterdayAmount);
                $('#saleVolume-t').html(data.salesTodayQty);
                $('#saleVolume-y').html(data.salesYesterdayQty);
                $('#average-t').html(data.todayAveragePrice);
                $('#average-y').html(data.ystdAveragePrice);
                $('#refund-t').html(data.todayRefundAmount);
                $('#refund-y').html(data.ystdRefundAmount);
                $('#offlineDevice').html(data.offlineDevQty);
                $('#soldOut').html(data.saleEmptyQty);
            }
        })

        //货道售空提醒查看详情
        $('#soldOutDetail').on('click', function() {
            openWin('winSoldOut', '活动售空提醒');
            if ($('#soldOutGrid').datagrid('options').url == null) {
                $('#soldOutGrid').datagrid({
                    url: '${pageContext.request.contextPath}/findSaleEmptyDevices.json'
                })
            }
        })


        //销售图表
        var charts = echarts.init($('#charts')[0]);

        var chartOpt = {
            title: {
                show: false,
                text: '销售统计图'
            },
            tooltip: {
                trigger: 'axis'
            },
            toolbox: {
                show: true,
                bottom: 0,
                right: 0,
                feature: {
                    dataView: { //数据视图
                        show: true
                    },
                    magicType: {//转换视图
                        show: true, 
                        type: ['line', 'bar']
                    },
                    restore: {//还原
                        show: true
                    },
                    saveAsImage: {//保存图片
                        show: true
                    }
                }
            },
            grid: {
                left: 80,
                right: 80,
                top: 40,
                bottom: 65
            },
            legend: {
                data:['销售额','销售量','平均单价'],
                bottom: 10
            },
            xAxis: [
                {
                    type: 'category',
                    axisTick: {
                        alignWithLabel: true
                    },
                    data: []
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: '销售额/销售量'
                },
                {
                    type: 'value',
                    name: '平均单价',
                    axisLabel: {
                        formatter: '{value}元'
                    }
                }
            ],
            series: [
                {
                    name:'销售额',
                    type:'bar',
                    barMaxWidth: 30,
                    smooth: true,
                    itemStyle: {
                        normal: {
                            color: '#aacc03'
                        }
                    },
                    data:[]
                },
                {
                    name:'销售量',
                    type:'bar',
                    barMaxWidth: 30,
                    smooth: true,
                    barGap: 0,
                    itemStyle: {
                        normal: {
                            color: '#f9bf11'
                        }
                    },
                    data:[]
                },
                {
                    name:'平均单价',
                    type:'line',
                    barMaxWidth: 30,
                    yAxisIndex: 1,
                    smooth: true,
                    lineStyle: {
                        normal: {
                            color: '#009cd3'
                        }
                    },
                    itemStyle: {
                        normal: {
                            color: '#009cd3'
                        }
                    },
                    data:[]
                }
            ]
        };

        charts.setOption(chartOpt);

        $('#charts-nav').on('click', 'span', function(){
            var $this = $(this);
            if ($this.hasClass('active')) return;
            $this.addClass('active').siblings().removeClass('active');
            var near = $this.data('near'),
                pull = $this.data('pull');
            var range = getDateRange(near, pull);
            $.ajax({
                url: '${pageContext.request.contextPath}/findSalesAndAmount.json',
                data: {startTime:range.startDate, endTime:range.endDate},
                task: function(r) {
                    var orderList = r.orderList;
                    var dateArr = [], amountArr = [], volumeArr = [], averageArr = [];
                    for (var i = 0; i < orderList.length; i++) {
                        if (near == 1) {// 若是请求当日数据
                            dateArr.push(orderList[i].date + '点');
                        } else {
                            dateArr.push(orderList[i].date.slice(5).split('-').join('月') + '日'); 
                        }
                        amountArr.push(orderList[i].salesAmount);
                        volumeArr.push(orderList[i].salesVolume);
                        averageArr.push(orderList[i].averagePrice);
                    }
                    chartOpt.xAxis[0].data = dateArr;
                    chartOpt.series[0].data = amountArr;
                    chartOpt.series[1].data = volumeArr;
                    chartOpt.series[2].data = averageArr;
                    charts.setOption(chartOpt);
                }
            })
        })

        $('#charts-nav span').eq(0).trigger('click');


        //销售排行榜
        $.ajax({
            url: '${pageContext.request.contextPath}/findBestSellerLists.json',
            task: function(r) {
                r.rankList = r.storeSales;
                $('#store-rank').html(template('rankItem', r));

                r.rankList = r.productSales;
                $('#pro-amount-rank').html(template('rankItem', r));

                r.rankList = r.productSalesQtyOrders;
                $('#pro-volume-rank').html(template('rankItem', r));
            }
        })

        //查看更多
        $('#rank-ctn').on('click', '.all', function() {
            var index = $('#rank-ctn .all').index(this);
            var rankData = [
                {win:'winStoreRankAll', grid:'storeRankAllGrid', title:'店铺销售排行榜', api:'findStoreSalesList'},
                {win:'winProAmountRankAll', grid:'proAmountRankAllGrid', title:'商品销售额排行榜', api:'findStoreSalesAmountList'},
                {win:'winProVolumeRankAll', grid:'proVolumeRankAllGrid', title:'商品销售量排行榜', api:'findStoreSalesVolumeList'}
            ];

            var curRank = rankData[index];

            openWin(curRank.win, curRank.title);
            if ($('#'+curRank.grid).datagrid('options').url == null) {
                $('#'+curRank.grid).datagrid({
                    url: '${pageContext.request.contextPath}/'+curRank.api+'.json'
                })
            }
        })

    })
</script>
</body>
</html>