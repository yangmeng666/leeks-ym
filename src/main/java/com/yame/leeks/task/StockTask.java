package com.yame.leeks.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.yame.leeks.entity.Stock;
import com.yame.leeks.entity.StockData;
import com.yame.leeks.service.StockDataService;
import com.yame.leeks.service.StockService;
import com.yame.leeks.utils.CommonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.DataUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


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
    @Autowired
    private StockDataService stockDataService;


    /**
     * 更新股票数据
     * 每天9:15-15:00之间 每5分钟执行一次执行一次
     */
    @Scheduled(cron = "0/10 * 9,10,11,13,14,15 * * ? ")
    public void updateStock() {
        if (CommonUtils.isStockTradingTime(true)) {
            log.info("刷新股票基础数据，当前时间：{}", DateUtil.now());
            stockService.updateRealData();
        }
    }
    @Scheduled(cron = "0 0/10 15,16,17,18,19,20,21,22,23 * * ?")
    public void updateStockDayData() {
        boolean isFlusDayData = (!CommonUtils.isStockTradingTime(true))
                &&(!stockDataService.haveStockDayDataByDate(DateUtil.today()));
        log.info("刷新股票日线数据，当前时间：{},是否刷新：{}", DateUtil.now(),isFlusDayData);
        if (isFlusDayData) {
            List<Stock> stocks = stockService.list();
            List<StockData> stockDatas = BeanUtil.copyToList(stocks, StockData.class);
            stockDataService.insertOrUpdateDayDatas(stockDatas);
            log.info("刷新股票日线数据insertOrUpdateDayDatas完成，当前时间：{}", DateUtil.now());
        }
    }

    @Scheduled(cron = "0/10 * 9,10,11,13,14,15 * * ? ")
    public void updateStockData() {
        if (CommonUtils.isStockTradingTime(false)) {
            log.info("获取股票数据，当前时间：{}", DateUtil.now());
            stockDataService.insertRealDatas();
            log.info("获取股票数据insertRealDatas完成，当前时间：{}", DateUtil.now());
        }
    }
  /*  long execTime = DateUtil.date().getTime();
    long amStartTime = DateUtil.parse(DateUtil.today() + " 09:15:00").getTime();
    long amEndTime = DateUtil.parse(DateUtil.today() + " 11:30:00").getTime();
    long pmStartTime = DateUtil.parse(DateUtil.today() + " 13:00:00").getTime();
    long pmEndTime = DateUtil.parse(DateUtil.today() + " 15:00:00").getTime();
        if ((execTime >= amStartTime && execTime <= amEndTime) || (execTime >= pmStartTime && execTime <= pmEndTime)) {
        log.info("刷新股票数据，当前时间：{}", DateUtil.now());
        stockService.updateRealData()*/;
}
