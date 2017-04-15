#!/usr/bin/env bash
export PATH=${CATALINA_HOME}/bin:$PATH
export PROJECT_HOME=`pwd`/..

#更新代码并编译打包
cd ${PROJECT_HOME}
svn update
mvn clean package -Ptest

#停止Tomcat
shutdown.sh

#按日期时间备份web
cd ${CATALINA_HOME}/webapps
tar zcvf ROOT-$(date +%y%m%d%H%M%S).gz ROOT

#删除旧的web
rm -rf ROOT
rm -f ROOT.war

#部署新的web
cp ${PROJECT_HOME}/target/ROOT.war ${CATALINA_HOME}/webapps

#删除Tomcat缓存文件
rm -rf ${CATALINA_HOME}/work/*

#启动Tomcat
startup.sh