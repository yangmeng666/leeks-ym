package com.yame.leeks.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yame.leeks.entity.StockData;

import java.util.List;

/**
 * 股票接口
 * @author yangmeng
 */
public interface StockDataService extends IService<StockData> {

    /**
     * 插入实时数据
     */
    void insertRealDatas();

    /**
     * 插入更新当天数据
     */
    void insertOrUpdateDayDatas(List<StockData> stockDatas);


    void saveBatchList(List<StockData> stockDatas);

    /**
     * 查询股票天粒度表中是否有当天数据
     * @param date yyyy-MM-dd
     * @return
     */
    boolean haveStockDayDataByDate(String date);

    /**
     * 实际判断表是否存在
     * @param date yyyy_MM_dd
     * @return
     */
    boolean haveStockHourTableByDate(String date);

    /**
     * 股票小时粒度表中是否有当天数据
     * @param todayTable
     * @param date
     * @return
     */
    boolean haveStockHourDataByDate(String todayTable,String date);

    void createHourTable(String todayTable);

    int insertHourData(String tableName, String date);

    void insertStockHourData(String today);
}
