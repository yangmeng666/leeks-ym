package com.yame.leeks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description TODO
 * @Date 2024/1/25
 * @Created by yangmeng
 */
@Data
@TableName("fund_data")
@ApiModel(value = "基金净值数据")
public class FundData {

    @Id
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("编码")
    private String fundCode;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty("净值时间")
    private Date time;

    @ApiModelProperty("净值估算gszzl 实际净值sjjz")
    private Float value;

    @ApiModelProperty("值类型;1-估算净值 2-实际净值")
    private int valueType;

}
