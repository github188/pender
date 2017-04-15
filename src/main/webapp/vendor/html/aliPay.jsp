<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <title>支付确认</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/vendor/css/pay.css">
    <script type="text/javascript" src="${pageContext.request.contextPath}/vendor/lib/zepto.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/vendor/lib/vue.min.js"></script>
</head>
<body>
<div class="pay-ctn">
    <div class="concern-tip clearfix">
        <img class="img" src="${pageContext.request.contextPath}/vendor/img/logo.png">
    </div>
    <ul id="good-list" class="good-list">
        <li class="good" v-for="good in goods">
            <div class="name">{{good.productName}}</div>
            <div class="num">x{{good.productCount}}</div>
        </li>
        <!-- <li style="line-height:3; text-align:center;" v-show="getGoodsFail">
            商品获取失败，请点<a v-on:click="getGoods" style="color:red;">此处</a>重新获取
        </li> -->
        <li class="pay-value clearfix">
            <div class="paid">总金额：<span>￥{{amount}}</span></div>
        </li>
    </ul>
    <div id="pay">
        <div class="pay-btn" v-on:click="pay">立即支付</div>
        <div class="offline-overlay" v-if="offlineTipShow">
            <div class="offline-tip">
                <div class="text">小主，网络同学打了一个盹，请稍后再试╮(╯▽╰)╭</div>
                <div class="btn" v-on:click="closeTip">确 定</div>
            </div>
        </div>
        <form id="payForm" action="/free/pay/alipay/launchPay.do">
            <input type="hidden" id="orderNo" name="orderNo"/>
            <input type="hidden" id="amount" name="amount"/>
        </form>
    </div>
</div>
<script type="text/javascript">

    function getContextUrl() {
        var href = window.location.href;
        var packageName = "/free/";
        var contentUrl = href.substring(0, href.indexOf(packageName));
        return contentUrl;
    }
    //获取url中的参数
    function getUrlArg(argName) {
        var argStr = location.search.slice(1);
        if (!argStr) return;
        var argArr = argStr.split('&');
        var self = this;
        if (!!argName) {
            for (var i = 0; i < argArr.length; i++) {
                if (argArr[i].indexOf(argName) !== -1) {
                    return argArr[i].slice(argName.length + 1);
                }
            }
            return null;
        } else {
            var argObj = {};
            for (var i = 0; i < argArr.length; i++) {
                var argItemArr = argArr[i].split('=');
                argObj[argItemArr[0]] = argItemArr[1];
            }
            return argObj;
        }
    }
    //        $.ajax({
    //			type: 'POST',
    //			url: getContextUrl()+'/free/findJsConfig.json',
    //			data: {url: location.href, openId: getUrlArg('openId')},
    //			dataType: 'json',
    //			success: function(r){
    //				if (!r.resultCode) {
    //					wx.config({
    //					    debug: false,
    //					    appId: r.appId, // 必填，公众号的唯一标识
    //					    timestamp: r.timestamp, // 必填，生成签名的时间戳
    //					    nonceStr: r.nonceStr, // 必填，生成签名的随机串
    //					    signature: r.signature,// 必填，签名，见附录1
    //					    jsApiList: ['chooseWXPay'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
    //					});
    //				}
    //			}
    //		});

    var goodlist = new Vue({
        el: '#good-list',
        data: {
            amount: 0,
            goods: []
        },
        methods: {
            getGoods: function () {
                //this.getGoodsFail = false;
                var self = this;
                var params = getUrlArg();
                $.ajax({
                    type: 'POST',
                    url: getContextUrl() + '/free/syncCartProductInfo.json',
                    data: params,
                    dataType: 'json',
                    success: function (r) {
                        if (r.resultCode == 0 && r.data.orderState == 1) {
                            var data = r.data;
                            self.goods = data.products;
                            self.amount = data.amount;
                        } else {
                            alert('二维码已失效');
                            if (!!WeixinJSBridge) {
                                WeixinJSBridge.call('closeWindow');
                            }
                        }
                    }
                })
            }
        }
    });

    var pay = new Vue({
        el: '#pay',
        data: {
            offlineTipShow: false
        },
        methods: {
            pay: function () {
                var self = this;
                var orderNo = getUrlArg("orderNo"),
                    facDevNo = getUrlArg("facDevNo");
                $.ajax({
                    type: 'POST',
                    url: getContextUrl() + '/device/checkDeviceOnlineByFacDevNo',
                    data: {facDevNo: facDevNo},
                    dataType: 'string',
                    success: function (r) {
                        if (r === "true") {
                            $("#orderNo")[0].value = orderNo;
                            $("#amount")[0].value = goodlist.amount;
                            $("#payForm").submit();
                        }
                        else {
                            self.offlineTipShow = true;
                        }
                    }
                });

            },
            closeTip: function () {
                this.offlineTipShow = false;
            }
        }
    });

    //请求商品信息
    goodlist.getGoods();

</script>
</body>
</html>