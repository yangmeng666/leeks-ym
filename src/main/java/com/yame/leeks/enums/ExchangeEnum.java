package com.yame.leeks.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @Description 证券交易所枚举类
 * @Date 2025/3/5
 * @Created by yangmeng
 */
@Getter
@AllArgsConstructor
public enum ExchangeEnum {

    SH("sh", "上海证券交易所"),
    SZ("sz", "深圳证券交易所"),
    BJ("bj", "北京证券交易所"),
    OTHER("other", "未知交易所");
    private final String code;
    private final String name;

    public static String getStockExchange(String stockCode) {
        if (stockCode == null || stockCode.isEmpty()) {
            return OTHER.code;
        }
        // 判断可转债
        if (stockCode.startsWith("11")) {
            return SH.code;
        } else if (stockCode.startsWith("12")) {
            return SZ.code;
        }

        // 判断上证
        if (stockCode.startsWith("6")) {
            return SH.code;
        }
        // 判断深圳
        if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
            return SZ.code;
        }
        // 判断北交所
        if (stockCode.startsWith("8") || stockCode.startsWith("4") || stockCode.startsWith("9")) {
            return BJ.code;
        }
        return OTHER.code;
    }

}
