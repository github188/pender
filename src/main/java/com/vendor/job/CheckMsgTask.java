package com.vendor.job;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.vendor.util.msg.PropertUtil;
import com.vendor.util.msg.SendPhoneMsg;
import com.vendor.util.msg.VerificationInfo;

/**
 * 验证码check
 * **/
@Component
public class CheckMsgTask implements Runnable{
    private final Logger logger=LoggerFactory.getLogger(CheckMsgTask.class);
    private String timeOut=PropertUtil.getInstall().prop.getProperty("msg.timeOut");
    private final Long DEFAULT_TIME_OUT=60000L;
    private String cronExpression="0/5 * * * * ?";//2s
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private int shutdownTimeout=20;//设置超时

    @PostConstruct
    public void start(){
        threadPoolTaskScheduler=new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix("CheckMsgTask");
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.schedule(this,new CronTrigger(cronExpression));
        logger.info("CheckMsgTask start ****************************** "+new Date());
    }
    
    @Override public void run(){
        try{
            checkMsgCode();
        }catch(Exception e){
            logger.error(e.getMessage(),e);
        }
    }
    
    @PreDestroy
    public void stop(){
        ScheduledExecutorService scheduledExecutorService=threadPoolTaskScheduler.getScheduledExecutor();
        normalShutdown(scheduledExecutorService,shutdownTimeout,TimeUnit.SECONDS);
    }
    
    private void normalShutdown(ExecutorService pool,int timeout,TimeUnit timeUnit){
        try{
            pool.shutdownNow();
            if(!pool.awaitTermination(timeout,timeUnit)){
                System.err.println("Pool did not terminated");
            }
        }catch(InterruptedException ie){
            Thread.currentThread().interrupt();
        }
    }
    
    private void checkMsgCode(){
        clearMsgCode(SendPhoneMsg.getInstall().getRegisterCache());
        clearMsgCode(SendPhoneMsg.getInstall().getOtherCache());
    }
    
    private void clearMsgCode(ConcurrentHashMap<String,VerificationInfo>concurrentHashMap){
        Long currentTime=System.currentTimeMillis();
        if(concurrentHashMap!=null&&concurrentHashMap.size()>0){
            for(Map.Entry<String,VerificationInfo> entry : concurrentHashMap.entrySet()){
                VerificationInfo info=entry.getValue();
                if(currentTime-info.getTime()>(timeOut==null?DEFAULT_TIME_OUT:Long.parseLong(timeOut))){
                    info.setCode(null);
                    info.setTime(-1);
                    info=null;
                    concurrentHashMap.remove(entry.getKey());
                }
            }
        }
    }

}
