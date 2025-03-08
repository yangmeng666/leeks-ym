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

    boolean haveStockHourTableByDate(String date);
    boolean haveStockHourDataByDate(String todayTable,String date);
    void createHourTable(String todayTable);

    int insertHourData(String todayTable,String date);
}
