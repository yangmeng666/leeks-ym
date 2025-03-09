package com.yame.leeksym;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yame.leeks.LeeksYmApplication;
import com.yame.leeks.entity.Stock;
import com.yame.leeks.entity.StockData;
import com.yame.leeks.enums.ExchangeEnum;
import com.yame.leeks.enums.MarketTypeEnum;
import com.yame.leeks.service.StockDataService;
import com.yame.leeks.service.StockService;
import com.yame.leeks.task.StockTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description 股票测试类
 * @Date 2025/3/4
 * @Created by yangmeng
 */
@Slf4j
@SpringBootTest(classes = LeeksYmApplication.class)
public class StockTest extends AbstractTestNGSpringContextTests {


    @Autowired
    private StockService stockService;
    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private StockTask stockTask;


    /**
     * 新浪接口 有限流请求限制
     * 适合单次调用 获取全部股票数据
     */
    @Test
    public void testMainStockListInfo() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Stock> stockMap = new HashMap<>();
        for (int i = 1; i < 60; i++) {

            URI uri = URI.create("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php" +
                    "/Market_Center.getHQNodeData?page=" + i + "&num=100&sort=changepercent&asc=0&node=hs_a&" +
                    "symbol=&_s_r_a=setlen");
            ResponseEntity<List> entity = restTemplate.getForEntity(uri, List.class);
            if (ObjectUtil.isEmpty(entity.getBody())) {
                return;
            }
            log.info("main entity.getBody().size() :{}", entity.getBody().size());
            for (Object object : entity.getBody()) {
                LinkedHashMap obj = (LinkedHashMap) object;
                obj.put("ticktime", DateUtil.today() + " " + obj.get("ticktime"));
                Stock stock = JSONObject.parseObject(JSONObject.toJSONString(obj), Stock.class);
                stock.setExchange(ExchangeEnum.getStockExchange(stock.getCode()));
                MarketTypeEnum marketTypeByCode = MarketTypeEnum.getMarketTypeByCode(stock.getCode());
                if (marketTypeByCode != null) {
                    stock.setMarketType(marketTypeByCode.getChineseName());
                }
                stockService.saveOrUpdateBySymbol(stock);
                stockMap.put(stock.getSymbol(), stock);
            }
            log.info("main stockMap.size() :{}", stockMap.size());
            try {
                long sleepTime = RandomUtil.randomLong(100, 5000);
                Thread.sleep(sleepTime);
                log.info("zz sleepTime :{}", sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("main stockMap.size() :{}", stockMap.size());
        List<Stock> dbStocks = stockService.getBaseMapper().selectList(null).stream().filter(stock -> !stock.getCode().startsWith("1")).collect(Collectors.toList());
        log.info("main db Stocks.size() :{}", dbStocks.size());
        Map<String, Stock> dbStockMap = dbStocks.stream().collect(Collectors.toMap(Stock::getSymbol, Function.identity()));
        for (String key : stockMap.keySet()) {
            if (!dbStockMap.containsKey(key)) {
                log.info("main 不存在的股票 :{}", JSONObject.toJSONString(stockMap.get(key)));
                stockService.saveOrUpdateBySymbol(stockMap.get(key));
            }

        }
    }

    /**
     * 获取所有可转债股票数据
     */
    @Test
    public void testKzzStockListInfo() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Stock> stockMap = new HashMap<>();
        for (int i = 1; i < 6; i++) {

            URI uri = URI.create("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php" +
                    "/Market_Center.getHQNodeDataSimple?page=" + i + "&num=100&sort=changepercent&asc=0&node=hskzz_z&_s_r_a=sort");
            ResponseEntity<List> entity = restTemplate.getForEntity(uri, List.class);
            if (ObjectUtil.isEmpty(entity.getBody())) {
                return;
            }
            log.info("zz entity.getBody().size() :{}", entity.getBody().size());
            for (Object object : entity.getBody()) {
                LinkedHashMap obj = (LinkedHashMap) object;
                obj.put("ticktime", DateUtil.today() + " " + obj.get("ticktime"));
                Stock stock = JSONObject.parseObject(JSONObject.toJSONString(obj), Stock.class);
                stockService.saveOrUpdateBySymbol(stock);
                stockMap.put(stock.getSymbol(), stock);
            }
            log.info("stockMap.size() :{}", stockMap.size());
            try {
                long sleepTime = RandomUtil.randomLong(100, 5000);
                Thread.sleep(sleepTime);
                log.info("zz sleepTime :{}", sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("stockMap.size() :{}", stockMap.size());
        List<Stock> zzStocks = stockService.getBaseMapper().selectList(
                new QueryWrapper<Stock>().likeRight("code", "1")
        );
        log.info("zzStocks.size() :{}", zzStocks.size());
        Map<String, Stock> dbStockMap = zzStocks.stream().collect(Collectors.toMap(Stock::getSymbol, Function.identity()));
        for (String key : stockMap.keySet()) {
            if (!dbStockMap.containsKey(key)) {
                log.info("不存在的股票 :{}", JSONObject.toJSONString(stockMap.get(key)));
                stockService.saveOrUpdateBySymbol(stockMap.get(key));
            }
        }
    }

    @Test
    public void testStockRealData() {
        RestTemplate restTemplate = new RestTemplate();
        List<Stock> dbStocks = stockService.getBaseMapper().selectList(null);
        Map<String, Stock> dbStockMap = dbStocks.stream().collect(Collectors.toMap(stock -> stock.getSymbol(), Function.identity()));
        List<String> stockCodes = dbStocks.stream().map(Stock::getSymbol).collect(Collectors.toList());
        stockCodes.add("sh000001");
        List<List<String>> partition = ListUtil.partition(stockCodes, 600);
        CountDownLatch countDownLatch = new CountDownLatch(partition.size());
        for (List<String> strings : partition) {
            new Thread(() -> {
                String codes = String.join(",", strings);
                URI uri = URI.create("http://qt.gtimg.cn/q=" + codes);
                log.info("strings.size() :{} ,uri :{}", strings.size(), uri);
                ResponseEntity<String> entity = restTemplate.getForEntity(uri, String.class);
                List<Stock> stocks = parseStock(entity.getBody());
                for (Stock stock : stocks) {
                    if (dbStockMap.containsKey(stock.getSymbol())) {
                        stock.setId(dbStockMap.get(stock.getSymbol()).getId());
                    }
                }
                stockService.saveOrUpdateBatchBySymbol(dbStockMap.values().stream().collect(Collectors.toList()));
                countDownLatch.countDown();
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //URI uri = URI.create("http://qt.gtimg.cn/q=sh000001,sh600272,sz002640,sh600187,sh603278");
        //log.info("uri :{}", uri);
        //ResponseEntity<String> entity = restTemplate.getForEntity(uri, String.class);
        //List<Stock> stocks = parseStock(entity.getBody());
        //log.info("stocks.size() :{} stocks:{},", stocks.size(),stocks);


    }

    @Test
    public void testUpdateStock() {
        stockService.updateRealData();
    }

    @Test
    public void testHaveStockHourDataByDate() {
        boolean yyyyMmDd = stockDataService.haveStockHourDataByDate(DateUtil.date().toString("yyyy_MM_dd"), DateUtil.today());
        log.info("yyyyMmDd :{}", yyyyMmDd);
    }

    @Test
    public void testCreateHourTable() {

    }

    @Test
    public void testInsertStockDatas() {
        stockDataService.insertRealDatas();
    }

    @Test
    public void testStockTaskDayData() {
        stockTask.updateStockDayData();
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

    @Test
    public void testUpdateStockDatas() {
        List<Stock> stocks = stockService.list();
        stocks.sort(Comparator.comparing(Stock::getCode));
        List<List<Stock>> partition = ListUtil.partition(stocks, 1000);
        Map<String, String> codeSymbolMap = partition.get(0).stream()
                .filter(x -> !x.getCode().equals("000001"))
                .collect(Collectors.toMap(Stock::getCode, Stock::getSymbol));
        //CountDownLatch latch = new CountDownLatch(10);
        for (String code : codeSymbolMap.keySet()) {
            //new Thread(() -> {
                QueryWrapper<StockData> queryWrapper = new QueryWrapper();
                queryWrapper.eq("symbol", code);
                List<StockData> stockDataList = stockDataService.list(queryWrapper);
                if (ObjectUtil.isEmpty(stockDataList)) {
                    continue;
                }
                for (StockData stockData : stockDataList) {
                    stockData.setSymbol(codeSymbolMap.get(stockData.getSymbol()));
                }
                stockDataService.saveOrUpdateBatch(stockDataList);
                log.info("1-code:{},stockDataList.size:{}", code, stockDataList.size());
                //latch.countDown();
            //}).start();
        }
        //try {
        //    latch.await();
        //} catch (
        //        InterruptedException e) {
        //    throw new RuntimeException(e);
        //}

    }
    @Test
    public void testUpdateStockDatas2() {
        List<Stock> stocks = stockService.list();
        stocks.sort(Comparator.comparing(Stock::getCode));
        List<List<Stock>> partition = ListUtil.partition(stocks, 1000);
        Map<String, String> codeSymbolMap = partition.get(1).stream()
                .filter(x -> !x.getCode().equals("000001"))
                .collect(Collectors.toMap(Stock::getCode, Stock::getSymbol));
        //CountDownLatch latch = new CountDownLatch(10);
        for (String code : codeSymbolMap.keySet()) {
            //new Thread(() -> {
                QueryWrapper<StockData> queryWrapper = new QueryWrapper();
                queryWrapper.eq("symbol", code);
                List<StockData> stockDataList = stockDataService.list(queryWrapper);
                if (ObjectUtil.isEmpty(stockDataList)) {
                    continue;
                }
                for (StockData stockData : stockDataList) {
                    stockData.setSymbol(codeSymbolMap.get(stockData.getSymbol()));
                }
                stockDataService.saveOrUpdateBatch(stockDataList);
                log.info("2-code:{},stockDataList.size:{}", code, stockDataList.size());
                //latch.countDown();
            //}).start();
        }
        //try {
        //    latch.await();
        //} catch (
        //        InterruptedException e) {
        //    throw new RuntimeException(e);
        //}

    }
    @Test
    public void testUpdateStockDatas3() {
        List<Stock> stocks = stockService.list();
        stocks.sort(Comparator.comparing(Stock::getCode));
        List<List<Stock>> partition = ListUtil.partition(stocks, 1000);
        Map<String, String> codeSymbolMap = partition.get(2).stream()
                .filter(x -> !x.getCode().equals("000001"))
                .collect(Collectors.toMap(Stock::getCode, Stock::getSymbol));
        //CountDownLatch latch = new CountDownLatch(10);
        int count=0;
        for (String code : codeSymbolMap.keySet()) {
            //new Thread(() -> {
                QueryWrapper<StockData> queryWrapper = new QueryWrapper();
                queryWrapper.eq("symbol", code);
                List<StockData> stockDataList = stockDataService.list(queryWrapper);
                if (ObjectUtil.isEmpty(stockDataList)) {
                    continue;
                }
                for (StockData stockData : stockDataList) {
                    stockData.setSymbol(codeSymbolMap.get(stockData.getSymbol()));
                }
                stockDataService.updateBatchBySymbol(stockDataList);
                log.info("3-code:{},stockDataList.size:{},codeSymbolMap:{},count:{}", code, stockDataList.size(),codeSymbolMap.keySet().size(),count++);
                //latch.countDown();
            //}).start();
        }
        //try {
        //    latch.await();
        //} catch (
        //        InterruptedException e) {
        //    throw new RuntimeException(e);
        //}

    }
    @Test
    public void testUpdateStockDatas4() {
        List<Stock> stocks = stockService.list();
        stocks.sort(Comparator.comparing(Stock::getCode));
        List<List<Stock>> partition = ListUtil.partition(stocks, 1000);
        Map<String, String> codeSymbolMap = partition.get(3).stream()
                .filter(x -> !x.getCode().equals("000001"))
                .collect(Collectors.toMap(Stock::getCode, Stock::getSymbol));
        //CountDownLatch latch = new CountDownLatch(10);
        int count=0;
        for (String code : codeSymbolMap.keySet()) {
            //new Thread(() -> {
                QueryWrapper<StockData> queryWrapper = new QueryWrapper();
                queryWrapper.eq("symbol", code);
                List<StockData> stockDataList = stockDataService.list(queryWrapper);
                if (ObjectUtil.isEmpty(stockDataList)) {
                    continue;
                }
                for (StockData stockData : stockDataList) {
                    stockData.setSymbol(codeSymbolMap.get(stockData.getSymbol()));
                }
                stockDataService.updateBatchBySymbol(stockDataList);
            log.info("4-code:{},stockDataList.size:{},codeSymbolMap:{},count:{}", code, stockDataList.size(),codeSymbolMap.keySet().size(),count++);
                //latch.countDown();
            //}).start();
        }
      log.info("complete.......................");

    }
    @Test
    public void testUpdateStockDatas5() {
        List<Stock> stocks = stockService.list();
        stocks.sort(Comparator.comparing(Stock::getCode));
        List<List<Stock>> partition = ListUtil.partition(stocks, 1000);
        Map<String, String> codeSymbolMap = partition.get(4).stream()
                .filter(x -> !x.getCode().equals("000001"))
                .collect(Collectors.toMap(Stock::getCode, Stock::getSymbol));
        //CountDownLatch latch = new CountDownLatch(10);
        int count=0;
        for (String code : codeSymbolMap.keySet()) {
            //new Thread(() -> {
                QueryWrapper<StockData> queryWrapper = new QueryWrapper();
                queryWrapper.eq("symbol", code);
                List<StockData> stockDataList = stockDataService.list(queryWrapper);
                if (ObjectUtil.isEmpty(stockDataList)) {
                    continue;
                }
                for (StockData stockData : stockDataList) {
                    stockData.setSymbol(codeSymbolMap.get(stockData.getSymbol()));
                }
                stockDataService.updateBatchBySymbol(stockDataList);
            log.info("5-code:{},stockDataList.size:{},codeSymbolMap:{},count:{}", code, stockDataList.size(),codeSymbolMap.keySet().size(),count++);
                //latch.countDown();
            //}).start();
        }
        log.info("complete.......................");

    }
    @Test
    public void testUpdateStockDatas6() {
        List<Stock> stocks = stockService.list();
        stocks.sort(Comparator.comparing(Stock::getCode));
        List<List<Stock>> partition = ListUtil.partition(stocks, 1000);
        Map<String, String> codeSymbolMap = partition.get(5).stream()
                .filter(x -> !x.getCode().equals("000001"))
                .collect(Collectors.toMap(Stock::getCode, Stock::getSymbol));
        //CountDownLatch latch = new CountDownLatch(10);
        int count=0;
        for (String code : codeSymbolMap.keySet()) {
            //new Thread(() -> {
                QueryWrapper<StockData> queryWrapper = new QueryWrapper();
                queryWrapper.eq("symbol", code);
                List<StockData> stockDataList = stockDataService.list(queryWrapper);
                if (ObjectUtil.isEmpty(stockDataList)) {
                    continue;
                }
                for (StockData stockData : stockDataList) {
                    stockData.setSymbol(codeSymbolMap.get(stockData.getSymbol()));
                }
                stockDataService.updateBatchBySymbol(stockDataList);
            log.info("6-code:{},stockDataList.size:{},codeSymbolMap:{},count:{}", code, stockDataList.size(),codeSymbolMap.keySet().size(),count++);
                //latch.countDown();
            //}).start();
        }
        log.info("complete.......................");

    }


    @Test
    public void testDeleteStockData() {
        List<Stock> stocks = stockService.list();
        stocks.sort(Comparator.comparing(Stock::getCode));
        Map<String, String> codeSymbolMap = stocks.stream()
                .filter(x -> !x.getCode().equals("000001"))
                .collect(Collectors.toMap(Stock::getCode, Stock::getSymbol));
        int count = 0;
        codeSymbolMap.keySet().add("sh000001");
        codeSymbolMap.keySet().add("sz000001");
        for (String code : codeSymbolMap.keySet()) {
            count++;
            QueryWrapper<StockData> queryWrapper = new QueryWrapper();
            queryWrapper.likeRight("symbol", code);
            queryWrapper.orderByAsc("tick_time");
            List<StockData> stockDataList = stockDataService.list(queryWrapper);
            if (ObjectUtil.isEmpty(stockDataList)) {
                continue;
            }
            HashSet times = new HashSet();
            List<Long> deletedIds = new ArrayList<>();
            for (StockData stockData : stockDataList) {
                String time = DateUtil.format(stockData.getTickTime(), "yyyy-MM-dd HH:mm");
                if(!times.add(time)){
                    deletedIds.add(stockData.getId());
                }
            }
            this.stockDataService.removeBatchByIds(deletedIds);
                log.info("count():{},symbol:{},stockDataList.size():{},deleteIds.size():{}",count,code,stockDataList.size(),deletedIds.size());
        }
    }

}
