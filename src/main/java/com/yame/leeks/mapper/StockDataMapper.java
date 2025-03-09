package com.yame.leeks.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yame.leeks.entity.StockData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 股票数据处理层
 * @author yangmeng
 */
@Mapper
public interface StockDataMapper extends BaseMapper<StockData> {

    void saveBatchList(List<StockData> stockDatas);

    void insertOrUpdateDayDatas(List<StockData> stockDatas);

    boolean haveStockDayDataByDate(String today);

    /**
     * 判断 stock_data_day 是否有当天数据
     * @param today
     * @return
     */
    boolean haveStockDataByDate(String today);

    boolean haveStockHourTableByDate(String date);

    /**
     * 判断是否有stock_data 表是否存在
     * @return
     */
    boolean haveStockDataTable();

    boolean haveStockHourDataByDate(String todayTable,String date);

    void renameStockDataDayTable(String todayTable);

    void createStockDataDayTable();

    boolean haveStockDataMonthTable(String monthTable);

    void renameStockDataMonthTable(String monthTable);

    void createStockDataMonthTable();

    void updateBatchBySymbol(List<StockData> stockDataList);
}
