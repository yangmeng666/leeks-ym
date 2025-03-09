package com.yame.leeks.entity;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yame.leeks.enums.MarketTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 股票数据
 * @Date 2025/3/4
 * @Created by yangmeng
 */
@Data
@TableName("stock_data_2025_03_06")
@ApiModel(value = "股票数据")
public class StockData {
    private static final Snowflake idWorker = IdUtil.createSnowflake(1, 1);
    public StockData() {
        this.id = idWorker.nextId();
    }

    @Id
    @TableId(value = "id",type = IdType.INPUT)
    private Long id;
    // 完整股票代码
    private String symbol;
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
    // 更新时间
    @TableField(value = "update_time", update = "now()")
    private Date updateTime;

}

