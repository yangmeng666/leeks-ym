package com.yame.leeks.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
     * 每天9:15-15:00之间 每3秒执行一次
     */
    @Scheduled(cron = "0/3 * 9,10,11,13,14,15 * * ? ")
    public void updateStock() {
        if (CommonUtils.isStockTradingTime(true)) {
            log.info("刷新股票基础数据，当前时间：{}", DateUtil.now());
            stockService.updateRealData();
        }
    }

    /**
     * 定时任务，每天交易时间内 每分钟执行一次
     * 插入每分钟的股票数据
     */
    @Scheduled(cron = "0 * 9,10,11,13,14,15 * * ? ")
    public void updateStockData() {
        if (CommonUtils.isStockTradingTime(true)) {
            log.info("获取股票数据，当前时间：{}", DateUtil.now());
            stockDataService.insertRealDatas();
            log.info("获取股票数据insertRealDatas完成，当前时间：{}", DateUtil.now());
        }
    }

    /**
     * 定时任务，非交易时间内 每10分钟执行一次
     * 插入每日的股票日线数据
     * 有数据则不插入
     * 没有数据则插入
     */
    @Scheduled(cron = "0 0/10 15-20 * * ?")
    public void updateStockDayData() {
        // 非交易时间，且没有日线数据
        boolean isFlusDayData = (!CommonUtils.isStockTradingTime(true))
                &&(!stockDataService.haveStockDayDataByDate(DateUtil.today()));
        log.info("导入股票日线数据，当前时间：{},是否导入：{}", DateUtil.now(),isFlusDayData);
        if (isFlusDayData) {
            List<Stock> stocks = stockService.list();
            List<StockData> stockDatas = BeanUtil.copyToList(stocks, StockData.class, CopyOptions.create().setIgnoreNullValue(true).setIgnoreProperties("id"));
            stockDataService.insertOrUpdateDayDatas(stockDatas);
            log.info("导入股票日线数据完成，当前时间：{}", DateUtil.now());
        }
    }

    @Scheduled(cron = "0 0/10 15-20 * * ?")
    public void insertStockHourData() {
        stockDataService.insertStockHourData(DateUtil.today());
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
