package com.yame.leeks.service;

/**
 * 基金接口
 * @author yangmeng
 */
public interface FundDataService {

    /**
     * 插入更新昨天的净值
     * @param fundCode
     */
    void updateLsjz(String fundCode);

    void updateAllLsjz(boolean isAll);
}
