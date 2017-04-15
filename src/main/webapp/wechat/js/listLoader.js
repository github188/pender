$(function($) {

	var Loader = function(option) {
		var DEFAULT = {
			url: '',
			params: {},
			curPage: 1,
			pageSize: 10,
			loading: false,
			loadFinish: false,
			container: '',
			template: '',
			loadingTip: '',
			field: {
				pageSize: 'pageSize',
				curPage: 'curPage',
				list: 'list'
			},
			berforeLoad: function() {},
			afterLoadSuccess: function(r){},
			afterLoadedAll: function(r){}
		}

		option = $.extend({}, DEFAULT, option);

		this.option = option;
	}

	Loader.prototype.loadData = function() {
		var option = this.option;

		if (option.loading || option.loadFinish) return;

		option.loading = true;

		option.berforeLoad();
		$(option.loadingTip).fadeIn();
		var pg = {};
		pg[option.field.curPage] = option.curPage;
		pg[option.field.pageSize] = option.pageSize;
		$.ajax({
			type: 'POST',
			url: option.url,
			data: $.extend({}, option.params, pg),
			success: function(r) {
				option.afterLoadSuccess(r);
				var html = template(option.template.substring(1), r);
				$(option.container).append(html);
				$(option.loadingTip).stop().hide();
				option.loading = false;
				option.curPage++;
				if (r[option.field.list].length < option.pageSize) {
					option.loadFinish = true;
    				option.afterLoadedAll(r);
    			}
			}
		})
	}

	Loader.prototype.setParams = function(params) {
		this.option.params = $.extend(this.option.params, params);
	}

	Loader.prototype.reset = function() {
		this.option.curPage = 1;
		this.option.loadFinish = false;
		$(this.option.container).html('');
	}

	window.Listloader = Loader;
	
}(jQuery))