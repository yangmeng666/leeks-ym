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
 * @author yangmeng
 */
@Data
@TableName("fund")
@ApiModel(value = "基金")
public class Fund {

    @Id
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("编码")
    private String fundCode;//编号

    @ApiModelProperty("名称")
    private String name;//名称

    @ApiModelProperty("净值估算")
    private Float gszzl;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty("估值时间")
    private Date gztime;

    @ApiModelProperty("自定义标签分类")
    private String tag;

    @ApiModelProperty("分类")
    private String category;

    @ApiModelProperty("买入规则")
    private String buyRule;

    @ApiModelProperty("卖出规则")
    private String saleRule;

    @ApiModelProperty("交易类型:T+1 T+2")
    private String transactionType;

    @ApiModelProperty("重仓股票")
    private String stocks;

    //private String jzrq;//净值日期

    //private String dwjz;//当日净值

    //private String gsz; //估算净值

}
