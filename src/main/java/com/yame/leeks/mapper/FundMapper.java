package com.yame.leeks.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yame.leeks.entity.Fund;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基金数据处理层
 * @author yangmeng
 */
@Mapper
public interface FundMapper extends BaseMapper<Fund> {

}
