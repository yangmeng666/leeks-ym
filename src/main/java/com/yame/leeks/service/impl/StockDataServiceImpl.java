package com.yame.leeks.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yame.leeks.entity.Stock;
import com.yame.leeks.entity.StockData;
import com.yame.leeks.mapper.StockDataMapper;
import com.yame.leeks.service.StockDataService;
import com.yame.leeks.service.StockService;
import com.yame.leeks.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基金接口实现
 *
 * @author yangmeng
 */
@Slf4j
@Service
public class StockDataServiceImpl extends ServiceImpl<StockDataMapper, StockData> implements StockDataService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StockService stockService;


    @Override
    public void insertRealDatas() {
        long start = System.currentTimeMillis();
        List<Stock> dbStocks = stockService.list();
        List<String> stockCodes = dbStocks.stream().map(Stock::getSymbol).collect(Collectors.toList());
        // 补充上证指数
        stockCodes.add("sh000001");
        List<List<String>> partition = ListUtil.partition(stockCodes, 600);
        CountDownLatch countDownLatch = new CountDownLatch(partition.size());
        for (List<String> strings : partition) {
            new Thread(() -> {
                String codes = String.join(",", strings);
                URI uri = URI.create("http://qt.gtimg.cn/q=" + codes);
                //log.info("strings.size() :{} ,uri :{}", strings.size(),uri);
                ResponseEntity<String> entity = restTemplate.getForEntity(uri, String.class);
                List<StockData> stockDatas = parseStockData(entity.getBody());
                this.saveBatchList(stockDatas);
                countDownLatch.countDown();
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("insertRealDatas cost :{} ms", System.currentTimeMillis() - start);
    }

    @Override
    public void insertOrUpdateDayDatas(List<StockData> stockDatas) {
        this.baseMapper.insertOrUpdateDayDatas(stockDatas);
    }


    @Override
    public void saveBatchList(List<StockData> stockDatas) {
        this.baseMapper.saveBatchList(stockDatas);
    }

    @Override
    public boolean haveStockDayDataByDate(String date) {
        return this.baseMapper.haveStockDayDataByDate(date);
    }

    @Override
    public boolean haveStockHourTableByDate(String date) {
        return this.baseMapper.haveStockHourTableByDate(date);
    }

    @Override
    public boolean haveStockHourDataByDate(String todayTable, String date) {
        return this.baseMapper.haveStockHourDataByDate(todayTable, date);
    }


    @Override
    public void handleStockDataDayData(String date) {
        if (StrUtil.isBlank(date)) {
            date = DateUtil.today();
        }
        String todayTable = DateUtil.parse(date).toString("yyyy_MM_dd");
        boolean haveStockDataDateTable = this.haveStockHourTableByDate(todayTable);
        if(!haveStockDataDateTable) {
            boolean haveStockDataTable = this.baseMapper.haveStockDataTable();
            boolean haveStockData = this.baseMapper.haveStockDataByDate(date);
            if (!CommonUtils.isStockTradingTime(true) && CommonUtils.isStockTradingDay()&&haveStockDataTable && haveStockData) {
                // 如果表不存在 且 stock_data_day 当天有数据，修改 stock_data 表明 为stock_data_当天日期
                this.baseMapper.renameStockDataDayTable(todayTable);
                // 创建新的 stock_data_day
                this.baseMapper.createStockDataDayTable();
                log.info("修改 stock_data_day 为当天表,当前时间{},tableName:stock_data_{}", DateUtil.now(),todayTable);
            }else{
                log.info("非交易日,无需处理stock_data_day当天小时数据");
            }
        }else{
            log.info("当天小时数据stock_data_{}已存在,当前时间{}",todayTable, DateUtil.now());
        }
    }

    @Override
    public void handleStockDataMonthData(String today) {
        if(StrUtil.isBlank(today)){
            today = DateUtil.today();
        }
        String monthTable = DateUtil.parse(today).toLocalDateTime().plusMonths(-1).format(DateTimeFormatter.ofPattern("yyyy_MM"));
        //判断是否有上月表
        boolean haveStockDataMonthTable =  this.baseMapper.haveStockDataMonthTable(monthTable);
        if(!haveStockDataMonthTable){
            //没有上月表 则修改当前月份表名为上月表
            this.baseMapper.renameStockDataMonthTable(monthTable);
            //新建 当月表
            this.baseMapper.createStockDataMonthTable();
        }
    }

    @Override
    public void updateBatchBySymbol(List<StockData> stockDataList) {
        this.baseMapper.updateBatchBySymbol(stockDataList);
    }


    public List<StockData> parseStockData(String data) {
        List<StockData> stockList = new ArrayList<>();
        String[] lines = data.split("\n");
        for (String line : lines) {
            String symbol = line.substring(line.indexOf("_") + 1, line.indexOf("="));
            String dataStr = line.substring(line.indexOf("=") + 2, line.length() - 2);
            // 按 ~ 分割字段
            String[] fields = dataStr.split("~");
            // 创建 Stock 对象
            StockData stockData = new StockData();
            // 解析字段
//            stockData.setSymbol(fields[2]);   // 股票代码
            stockData.setSymbol(symbol);   // 股票代码
            stockData.setTrade(new BigDecimal(fields[3]));      // 当前交易价格
            stockData.setPriceChange(new BigDecimal(fields[31])); // 涨跌
            stockData.setChangePercent(new BigDecimal(fields[32])); // 涨跌幅
            stockData.setOpen(new BigDecimal(fields[5]));       // 当日开盘价
            stockData.setHigh(new BigDecimal(fields[33]));      // 当日最高价
            stockData.setLow(new BigDecimal(fields[34]));       // 当日最低价
            stockData.setVolume(Long.parseLong(fields[36]));     // 当日成交量
            stockData.setAmount(new BigDecimal(fields[37]));    // 当日成交金额
            stockData.setInsideDish(Long.parseLong(fields[8]));
            stockData.setOuterDisc(Long.parseLong(fields[7]));
            // 解析时间戳
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            try {
                Date tickTime = dateFormat.parse(fields[30]);
                stockData.setTickTime(tickTime);
            } catch (ParseException e) {
                throw new RuntimeException("Failed to parse tick time", e);
            }
            // 换手率 %
            if (StrUtil.isNotBlank(fields[38])) {
                stockData.setTurnoverRatio(new BigDecimal(fields[38]));
            }
            // 设置数据更新时间
            stockData.setUpdateTime(new Date());
            stockList.add(stockData);
        }
        return stockList;
    }


}
