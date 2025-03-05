package com.yame.leeks.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yame.leeks.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 股票数据处理层
 * @author yangmeng
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {

   void saveOrUpdateBySymbol(Stock stock);

   void saveOrUpdateBatchBySymbol(List<Stock> stocks);
}
