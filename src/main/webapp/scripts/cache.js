var _sysDB = 'ECARRY', _synTime = 'syncEcarryTime', _sysLogin = 'ecarryLoginHistory', _synFlag = {};
var _synDone = [false, false, false, false, false];
var _tables = ['SysType', 'Orgnization', 'Country', 'Currency', 'Category'];
function syncData() {
	$.indexedDB(_sysDB, {
    	schema: {
    		1: function(versionTransaction) {
    			var sysType = versionTransaction.createObjectStore(_tables[0], {keyPath:'id'});
    			sysType.createIndex('code');
    			sysType.createIndex('type');
    			var org = versionTransaction.createObjectStore(_tables[1], {keyPath:'id'});
    			org.createIndex('code');
    			org.createIndex('parentId');
    			org.createIndex('companyId');
    			versionTransaction.createObjectStore(_tables[2], {keyPath:'id'});
    			versionTransaction.createObjectStore(_tables[3], {keyPath:'id'});
    			var category = versionTransaction.createObjectStore(_tables[4], {keyPath:'id'});
    			category.createIndex('parentId');
    		}
    	}
    }).done(function() {
    	window.setTimeout(function() {
    		var curTime = localStorage.getItem(_synTime);
    		if (curTime)
    			syncModule(curTime);
    		syncCacheData(curTime)
    	}, 200);
    });
}
function syncModule(curTime) {
	$.ajax({
		url:'session/data/findSyncModules.json',
		data:{curTime:curTime},
		async:false,
		error:function(data, status, xhr) {},
		task:function(data, statusText, xhr) {
			if (data) {
				var ary = data.split(',');
				for (var i = 0; i < ary.length; i++)
					_synFlag[ary[i]] = 1;
			}
		}
	});
}
function syncCacheData(curTime) {
	$.indexedDB(_sysDB).transaction(_tables).progress(function(transaction) {
		var localTime;
		for (var i = 0; i < _tables.length; i++) {
			var time;
			if (_synFlag[_tables[i]]) {
				transaction.objectStore(_tables[i]).clear();
			} else {
				time = curTime;
			}
			$.ajax({
				url:'session/data/find' +_tables[i] + 's.json',
				data:{curTime:time},
				async:false,
				error:function(data, status, xhr) {},
				task:function(data, statusText, xhr) {
					if (localTime == undefined)
						localTime = Date.parse(xhr.getResponseHeader('Date'));
	    			data = data ? eval('(' + data + ')') : [];
	    			var store = transaction.objectStore(_tables[i]);
	    			$.each(data, function(j) {
	    				if (data[j].del == true) {
	    					store['delete'](data[j].id);
	    				} else {
	    					store.get(data[j].id).then(function(item) {
		    					if (item) {
		    						store.put(data[j]);
		    					} else {
		    						store.add(data[j]);
		    					}
		    				});
	    				}
	    			})
	    			_synDone[i] = true;
				}
			});
		}
		localStorage.setItem(_synTime, localTime);
	});
}
function initSyncDataByCache(callback) {
	if (isSynDone() == true) {
		_allSysTypes = [], _allOrgs = [], _allCountries = [], _allCurrencies = [], _allCategories = [];
		var vals = [_allSysTypes, _allOrgs, _allCountries, _allCurrencies, _allCategories];
		for (var i = 0; i < vals.length; i++) {
			_synDone[i] = false;
		}
		var request = window.indexedDB.open(_sysDB);
		request.onsuccess = function(event) {
			var idx = {};
			for (var i = 0; i < _tables.length; i++)
				idx[_tables[i]] = i;
			var transaction = request.result.transaction(_tables, 'readonly');
			for (var i = 0; i < _tables.length; i++) {
				transaction.objectStore(_tables[i]).openCursor().onsuccess = function(e) {
					var cursor = e.target.result;
					var name = e.target.source.name;
					if (cursor) {
						vals[idx[name]].push(cursor.value);
						cursor['continue']();
					} else {
						_synDone[idx[name]] = true;
						if (callback && isSynDone(idx[name]) == true)
							callback();
					}
				}
			}
		};
	} else {
		setTimeout(function(){initSyncDataByCache(callback)}, 500);
	}
}
function isSynDone(index) {
	var done = true;
	for (var i = 0; i < _synDone.length; i++) {
		if (i != index && _synDone[i] == false) {
			done = false;
			break;
		}
	}
	return done;
}
function getSysTypesForSelectByCache(callback, type, el) {
	var systypes = [];
	var request = window.indexedDB.open(getDBName());
	request.onsuccess = function(event) {
		var transaction = request.result.transaction(_tables[0], 'readonly');
		var systype;
		if (type) {
			systype = transaction.objectStore(_tables[0]).index('type').openCursor(IDBKeyRange.only(type));
		} else {
			systype = transaction.objectStore(_tables[0]).openCursor();
		}
		systype.onsuccess = function() {
			var cursor = systype.result;
			if (cursor) {
				systypes.push(cursor.value);
				cursor['continue']();
			} else {
				if (callback)
					callback(systypes, el);
			}
		};
	}
}
function getOrgsForSelectByCache(callback, el, limitCompany, onlyCompany) {
	var orgs = [];
	var request = window.indexedDB.open(getDBName());
	request.onsuccess = function(event) {
		var transaction = request.result.transaction(_tables[1], 'readonly');
		var user = getCurUser();
		var org;
		if (isSysUser() && limitCompany == false) {
			org = transaction.objectStore(_tables[1]).openCursor();
		} else {
			org = transaction.objectStore(_tables[1]).index('companyId').openCursor(IDBKeyRange.only(user.companyId));
			if (onlyCompany == true)
				orgs.push({id:1,code:'999',name:'系统平台'});
		}
		org.onsuccess = function() {
			var cursor = org.result;
			if (cursor) {
				if (onlyCompany == true) {
					if (cursor.value.parentId == 2 || cursor.value.parentId == 3 || cursor.value.id == 1)
						orgs.push(cursor.value);
				} else {
					if (cursor.value.parentId || isSysUser())
						orgs.push(cursor.value);
				}
				cursor['continue']();
			} else {
				if (callback)
					callback(orgs, el);
			}
		};
	}
}
function getCategoriesByCache(callback, parentId, el) {
	var categories = [];
	var request = window.indexedDB.open(getDBName());
	request.onsuccess = function(event) {
		var transaction = request.result.transaction(_tables[4], 'readonly');
		var category = transaction.objectStore(_tables[4]).index('parentId').openCursor(IDBKeyRange.only(parentId));
		category.onsuccess = function() {
			var cursor = category.result;
			if (cursor) {
				categories.push(cursor.value);
				cursor['continue']();
			} else {
				if (callback)
					callback(categories, el);
			}
		};
	}
}
function initUserCache(orgCode, user) {
	var name = orgCode + '_' + user;
	addLoginHistoryToCache(name)
	syncData();
}
function addCodeHistoryToCache(orgCode) {
	var codes = localStorage.getItem('ecarryHistory');
	if (codes) {
		var ary = codes.split(',');
		for (var i = 0; i < ary.length; i++) {
			if (ary[i] == orgCode) {
				ary.splice(i, 1);
				break;
			}
		}
		if (ary.length == 10)
			ary.pop();
		localStorage.setItem('ecarryHistory', orgCode + (ary.length == 0 ? '' : (',' + ary.toString())));
	} else {
		localStorage.setItem('ecarryHistory', orgCode);
	}
}
function addLoginHistoryToCache(name) {
	var users = localStorage.getItem(_sysLogin);
	if (users) {
		var ary = users.split(',');
		for (var i = 0; i < ary.length; i++) {
			if (ary[i] == name) {
				ary.splice(i, 1);
				break;
			}
		}
		if (ary.length == 3) {
			var val = ary.pop();
			localStorage.removeItem(_synTime);
			$.indexedDB(_sysDB).deleteDatabase();
		}
		localStorage.setItem(_sysLogin, name + (ary.length == 0 ? '' : (',' + ary.toString())));
	} else {
		localStorage.setItem(_sysLogin, name);
	}
}
function getLoginHistoryByCache() {
	var users = localStorage.getItem(_sysLogin);
	return users ? users.split(',') : [];
}
function getCodeHistoryByCache() {
	var codes = localStorage.getItem('ecarryHistory');
	return codes ? codes.split(',') : [];
}
function clearCacheDataByCache() {
	var ary = getLoginHistoryByCache();
	localStorage.removeItem(_synTime);
	$.indexedDB(_sysDB).deleteDatabase();
	localStorage.removeItem(_sysLogin);
}
function getDBName() {
	return _sysDB == 'ECARRY' ? parent._sysDB : _sysDB;
}
function getUnionId(id1, id2, id3, id4) {
	var id = '';
	if (id1)
		id = id1;
	if (id2)
		id = id + (id == '' ? '' : ':') + id2;
	if (id3)
		id = id + (id == '' ? '' : ':') + id3;
	if (id4)
		id = id + (id == '' ? '' : ':') + id4;
	return id;
}