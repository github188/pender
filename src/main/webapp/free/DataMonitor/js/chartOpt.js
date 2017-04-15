var dayOpt = {
	title: {
		text: '当日实时销售状况',
		left: 5,
		top: 5,
		textStyle: {
			color: '#9d2e3c'
		}
	},
	legend: {
		data:['销售额','销售量'],
		right: 10,
		top: 30,
		textStyle: {
			color: '#f2f2f2'
		}
	},
	tooltip : {
        trigger: 'axis'
    },
    grid: {
    	top: 60,
        bottom: 30,
        left: 10,
        right: 10
    },
    xAxis : [
        {
            type : 'category',
            axisLine: {
                lineStyle: {
                	color: '#ccc'
                }
            },
            axisTick: {
                show: true,
                alignWithLabel: true
            },
            splitLine: {
                show: false
            },
            data : []
        }
    ],
    yAxis : [
        {
            type: 'value',
            axisLine: {
                lineStyle: {
                	color: '#ccc'
                }
            },
            axisTick: {
                show: false
            },
            axisLabel: {
                show: false
            },
            splitLine: {
                show: true
            }
        }
    ],
    series : [
        {
            name:'销售额',
            type:'line',
            smooth: true,
            lineStyle: {
                normal: {
                    color: '#fd3e44'
                }
            },
            itemStyle: {
                normal: {
                    color: '#fd3e44'
                }
            },
            data: []
        },
        {
            name:'销售量',
            type:'line',
            smooth: true,
            lineStyle: {
                normal: {
                    color: '#0c94f1'
                }
            },
            itemStyle: {
                normal: {
                    color: '#0c94f1'
                }
            },
            data: []
        }
    ]
}

var monthOpt = {
	title: {
		text: '当月销售状况',
		left: 5,
		top: 5,
		textStyle: {
			color: '#ffcc00'
		}
	},
	legend: {
		data:['销售额','销售量'],
		right: 10,
		top: 30,
		textStyle: {
			color: '#f2f2f2'
		}
	},
	tooltip : {
        trigger: 'axis'
    },
    grid: {
    	top: 60,
        bottom: 30,
        left: 10,
        right: 10
    },
    xAxis : [
        {
            type : 'category',
            axisLine: {
                lineStyle: {
                	color: '#ccc'
                }
            },
            axisTick: {
                show: true,
                alignWithLabel: true
            },
            splitLine: {
                show: false
            },
            data : []
        }
    ],
    yAxis : [
        {
            type: 'value',
            axisLine: {
                lineStyle: {
                	color: '#ccc'
                }
            },
            axisTick: {
                show: false
            },
            axisLabel: {
                show: false
            },
            splitLine: {
                show: true
            }
        }
    ],
    series : [
        {
            name:'销售额',
            type:'line',
            smooth: true,
            lineStyle: {
                normal: {
                    color: '#ffcc00'
                }
            },
            itemStyle: {
                normal: {
                    color: '#ffcc00'
                }
            },
            data: [],
            markPoint: {
            	data: []
            }
        },
        {
            name:'销售量',
            type:'line',
            smooth: true,
            lineStyle: {
                normal: {
                    color: '#e54bcd'
                }
            },
            itemStyle: {
                normal: {
                    color: '#e54bcd'
                }
            },
            data: []
        }
    ]
}

// var monthOpt = $.extend(dayOpt, {
// 	title: {
// 		text: '当月销售状况',
// 		textStyle: {
// 			color: '#ffcc00'
// 		}
// 	},
// 	series:[
// 		{
// 			lineStyle: {
//                 normal: {
//                     color: '#ffcc00'
//                 }
//             },
//             itemStyle: {
//                 normal: {
//                     color: '#ffcc00'
//                 }
//             }
// 		},
// 		{
// 			lineStyle: {
//                 normal: {
//                     color: '#e54bcd'
//                 }
//             },
//             itemStyle: {
//                 normal: {
//                     color: '#e54bcd'
//                 }
//             }
// 		}
// 	]
// })