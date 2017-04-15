var WE = {
	showTip: function(content) {
		var $tip = $('<div class="cm-tip"><span>'+content+'</span></div>');
		$tip.appendTo('body');
		setTimeout(function() {
			$('.cm-tip').fadeOut(1000);
		}, 1000)
		setTimeout(function() {
			$tip.remove();
		}, 2500)
	},
	yearFormatter: function(d){
		return d.getFullYear().toString();
	},
	monthFormatter: function(d) {
		var month = d.getMonth()+1 < 10 ? '0'+(d.getMonth()+1) : d.getMonth()+1;
		return d.getFullYear() + '-' + month;
	},
	dateFormatter: function(d) {
		var month = d.getMonth()+1 < 10 ? '0'+(d.getMonth()+1) : d.getMonth()+1;
		var day = d.getDate() < 10 ? '0'+d.getDate() : d.getDate();
		return d.getFullYear() + '-' + month + '-' + day;
	},
	getDateRange: function(dateStr) {
		return {
			startTime: dateStr + ' 00:00:00',
			endTime: dateStr + ' 23:59:59'
		}
	},
	getMonthRange: function(monthStr) {
		var arr = monthStr.split('-');
		var d = new Date(arr[0],arr[1],0);
		return {
			startTime: monthStr + '-01 00:00:00',
			endTime: monthStr + '-' + d.getDate() + ' 23:59:59'
		}
	},
	getYearRange: function(yearStr) {
		return {
			startTime: yearStr + '-01-01 00:00:00',
			endTime: yearStr + '-12-31 23:59:59'
		}
	},
	getBaseURL: function() {
		var protocol = window.location.protocol;
		var hostname = window.location.hostname;
		var port = (window.location.port && ":") + location.port;
		var contextPath = window.location.pathname.split('/')[1];
		return protocol + "//" + hostname + port + (location.port ? ("/"+contextPath) : "");//+ "/" + contextPath;
	}
}