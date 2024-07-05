package com.yame.leeks.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yame.leeks.entity.Fund;

/**
 * 基金接口
 * @author yangmeng
 */
public interface FundService extends IService<Fund> {

    void updateLastGsjz(boolean isAll);

    void realDataFlushGsjz(Fund fund);

}
