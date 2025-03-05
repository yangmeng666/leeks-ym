package com.yame.leeks.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yame.leeks.entity.Stock;
import com.yame.leeks.enums.ExchangeEnum;
import com.yame.leeks.enums.MarketTypeEnum;
import com.yame.leeks.mapper.StockMapper;
import com.yame.leeks.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    @Autowired
    private RestTemplate restTemplate;
    @Override
    public void saveOrUpdateBatchBySymbol(List<Stock> stocks) {
        this.baseMapper.saveOrUpdateBatchBySymbol(stocks);
    }

    @Override
    public void saveOrUpdateBySymbol(Stock stock) {
        this.baseMapper.saveOrUpdateBySymbol(stock);
    }

    @Override
    public void updateRealData() {
        long start = System.currentTimeMillis();
        List<Stock> dbStocks = this.baseMapper.selectList(null);
        Map<String, Stock> dbStockMap = dbStocks.stream().collect(Collectors.toMap(stock -> stock.getSymbol(), Function.identity()));
        List<String> stockCodes = dbStocks.stream().map(Stock::getSymbol).collect(Collectors.toList());
        // 补充上证指数
        stockCodes.add("sh000001");
        List<List<String>> partition = ListUtil.partition(stockCodes, 600);
        CountDownLatch countDownLatch = new CountDownLatch(partition.size());
        for (List<String> strings : partition) {
            new Thread(() -> {
                String codes = String.join(",", strings);
                URI uri = URI.create("http://qt.gtimg.cn/q=" + codes);
                log.info("strings.size() :{} ,uri :{}", strings.size(),uri);
                ResponseEntity<String> entity = restTemplate.getForEntity(uri, String.class);
                List<Stock> stocks = parseStock(entity.getBody());
                for (Stock stock : stocks) {
                    if (dbStockMap.containsKey(stock.getSymbol())) {
                        stock.setId(dbStockMap.get(stock.getSymbol()).getId());
                    }
                }
                saveOrUpdateBatchBySymbol(dbStockMap.values().stream().collect(Collectors.toList()));
                countDownLatch.countDown();
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("updateRealData cost :{} ms", System.currentTimeMillis() - start);
    }

    public List<Stock> parseStock(String data) {
        List<Stock> stockList = new ArrayList<>();
        String[] lines = data.split("\n");
        for (String line : lines) {
            String code = line.substring(line.indexOf("_") + 1, line.indexOf("="));
            String dataStr = line.substring(line.indexOf("=") + 2, line.length() - 2);
            // 按 ~ 分割字段
            String[] fields = dataStr.split("~");
            // 创建 Stock 对象
            Stock stock = new Stock();
            // 解析字段
            stock.setSymbol(code); // 完整股票代码
            stock.setCode(fields[2]);   // 股票代码
            stock.setName(fields[1]);   // 股票名称
            stock.setSettlement(new BigDecimal(fields[4])); // 前一个交易日收盘价
            stock.setTrade(new BigDecimal(fields[3]));      // 当前交易价格
            stock.setPriceChange(new BigDecimal(fields[31])); // 涨跌
            stock.setChangePercent(new BigDecimal(fields[32])); // 涨跌幅
            stock.setOpen(new BigDecimal(fields[5]));       // 当日开盘价
            stock.setHigh(new BigDecimal(fields[33]));      // 当日最高价
            stock.setLow(new BigDecimal(fields[34]));       // 当日最低价
            stock.setVolume(Long.parseLong(fields[36]));     // 当日成交量
            stock.setAmount(new BigDecimal(fields[37]));    // 当日成交金额
            stock.setInsideDish(Long.parseLong(fields[8]));
            stock.setOuterDisc(Long.parseLong(fields[7]));
            stock.setExchange(ExchangeEnum.getStockExchange(stock.getCode()));
            MarketTypeEnum marketTypeByCode = MarketTypeEnum.getMarketTypeByCode(stock.getCode());
            if (marketTypeByCode != null) {
                stock.setMarketType(marketTypeByCode.getChineseName());
            }
            // 解析时间戳
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            try {
                Date tickTime = dateFormat.parse(fields[30]);
                stock.setTickTime(tickTime);
            } catch (ParseException e) {
                throw new RuntimeException("Failed to parse tick time", e);
            }
            // 换手率 %
            if (StrUtil.isNotBlank(fields[38])) {
                stock.setTurnoverRatio(new BigDecimal(fields[38]));
            }
            // 设置数据更新时间
            stock.setUpdateTime(new Date());
            //stockService.saveOrUpdateBySymbol(stock);
            stockList.add(stock);
        }
        return stockList;
    }


}
