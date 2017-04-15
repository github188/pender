var saleOpt = {
	tooltip : {
        trigger: 'axis'
    },
    grid: {
        left: 0,
        right: 0,
        top: 0,
        bottom: 0
    },
    xAxis : [
        {
            type : 'category',
            axisTick: {
                alignWithLabel: true
            },
            axisLine: {
                show: false
            },
            axisTick: {
                show: false
            },
            axisLabel: {
                show: false
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
            boundaryGap: [0, '100%'],
            //name: '销售额',
            axisLine: {
                show: false
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
        },
        {
            type: 'value',
            boundaryGap: [0, '100%'],
            //name: '销售量',
            axisLine: {
                show: false
            },
            axisTick: {
                show: false
            },
            axisLabel: {
                show: false
            },
            splitLine: {
                show: false
            }
        },
        {
            type: 'value',
            boundaryGap: [0, '100%'],
            //name: '销售量',
            axisLine: {
                show: false
            },
            axisTick: {
                show: false
            },
            axisLabel: {
                show: false
            },
            splitLine: {
                show: false
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
                    color: '#4466AA'
                }
            },
            itemStyle: {
                normal: {
                    color: '#4466AA'
                }
            },
            data: []
        },
        {
            name:'销售量',
            type:'line',
            yAxisIndex: 1,
            smooth: true,
            lineStyle: {
                normal: {
                    color: '#F43853'
                }
            },
            itemStyle: {
                normal: {
                    color: '#F43853'
                }
            },
            data: []
        },
        {
            name:'平均单价',
            type:'line',
            yAxisIndex: 2,
            smooth: true,
            lineStyle: {
                normal: {
                    color: '#A8CD04'
                }
            },
            itemStyle: {
                normal: {
                    color: '#A8CD04'
                }
            },
            data: []
        }
    ]
}

var userOpt = {
    tooltip: {
        trigger: 'item',
        formatter: "{a} <br/>{b}: {c} ({d}%)"
    },
    series: [
        {
            name: '用户数量',
            type:'pie',
            radius: ['50%', '70%'],
            data:[
                
            ]
        }
    ]
}