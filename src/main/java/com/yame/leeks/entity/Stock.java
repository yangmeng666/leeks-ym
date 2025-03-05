package com.yame.leeks.entity;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yame.leeks.enums.MarketTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 股票 实体类
 * @Date 2025/3/4
 * @Created by yangmeng
 */
@NoArgsConstructor
@Data
@TableName("stock")
@ApiModel(value = "股票")
public class Stock {

    @Id
    @TableId(value = "id",type = IdType.INPUT)
    private Long id = IdUtil.createSnowflake(1, 1).nextId();
    // 完整股票代码
    private String symbol;
    // 股票代码
    private String code;
    // 股票名称
    private String name;
    //证券交易所简码
    private String exchange;
    /**
     * {@link MarketTypeEnum}
     */
    private String marketType;
    // 前一个交易日收盘价
    private BigDecimal settlement;
    // 当前交易价格
    private BigDecimal trade;
    //涨跌额（当前价 - 昨日收盘价）
    private BigDecimal priceChange;
    //涨跌幅（百分比）
    private BigDecimal changePercent;
    // 当日开盘价
    private BigDecimal open;
    // 当日最高价
    private BigDecimal high;
    // 当日最低价
    private BigDecimal low;
    //成交量（单位：手，1手=100股）
    private long volume;
    //成交额（单位：元）
    private BigDecimal amount;
    //内盘（主动卖出的成交量）
    private Long insideDish;
    //外盘（主动买入的成交量）
    private Long outerDisc;
    // 数据更新时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date tickTime;
    // 换手率（百分比）
    private BigDecimal turnoverRatio;
    // 股票板块
    private String sectors;
    // 更新时间
    @TableField(value = "update_time", update = "now()")
    private Date updateTime;

}
//private String symbol;          // 股票代码
//private String code;            // 股票代码
//private BigDecimal per;         // 市盈率
//private BigDecimal pb;          // 市净率
//private BigDecimal mktCap;      // 市值
//private BigDecimal nmc;         // 流通市值
//private BigDecimal buy;         // 当前买入价
//private BigDecimal sell;        // 当前卖出价
   /* {
        "symbol": "sh600272",
            "code": "600272",
            "name": "开开实业",
            "trade": "15.070",
            "pricechange": -0.27,
            "changepercent": -1.76,
            "buy": "15.070",
            "sell": "15.080",
            "settlement": "15.340",
            "open": "15.360",
            "high": "15.370",
            "low": "15.020",
            "volume": 4937271,
            "amount": 74666637,
            "ticktime": "11:30:00",
            "per": 94.188,
            "pb": 5.921,
            "mktcap": 366201,
            "nmc": 241120,
            "turnoverratio": 3.08579
    }*/
