package com.yame.leeks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;


/**
 * @author yangmeng
 */
@Data
@TableName("fund_category")
@ApiModel(value = "基金分类")
public class FundCategory {

    @Id
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("父id")
    private Long parentId;

    @ApiModelProperty("备注")
    private String remark;

}
