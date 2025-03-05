package com.yame.leeks.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yame.leeks.entity.Stock;

import java.util.List;

/**
 * 股票接口
 * @author yangmeng
 */
public interface StockService extends IService<Stock> {

    void saveOrUpdateBatchBySymbol(List<Stock> stocks);
    void saveOrUpdateBySymbol(Stock stock);

    void updateRealData();
}
