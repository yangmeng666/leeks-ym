package com.yame.leeks.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.yame.leeks.service.FundDataService;
import com.yame.leeks.service.FundService;
import com.yame.leeks.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * @Description TODO
 * @Date 2024/1/25
 * @Created by yangmeng
 */
@Component
@Slf4j
public class StockTask {

    @Autowired
    private StockService stockService;


    @Scheduled(cron = "0/3 * 9,10,11,13,14,15 * * ? ")
    public void updateStock() {
        long execTime = DateUtil.date().getTime();
        long amStartTime = DateUtil.parse(DateUtil.today() + " 09:15:00").getTime();
        long amEndTime= DateUtil.parse(DateUtil.today() + " 11:30:00").getTime();
        long pmStartTime = DateUtil.parse(DateUtil.today() + " 13:00:00").getTime();
        long pmEndTime= DateUtil.parse(DateUtil.today() + " 15:00:00").getTime();
        if ((execTime >= amStartTime && execTime <= amEndTime)||(execTime >= pmStartTime && execTime <= pmEndTime)) {
            log.info("刷新股票数据，当前时间：{}", DateUtil.now() );
            stockService.updateRealData();
        }
    }

    //@Scheduled(cron = "0/3 * 0,9,10,11,13,14,15 * * ? ")
    //public void updateStockTest() {
    //    log.info("开始更新股票数据，当前时间1：{}", DateUtil.now() );
    //    stockService.updateRealData();
    //}


}
