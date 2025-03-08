package com.yame.leeks.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @Description 市场类型枚举
 * @Date 2025/3/5
 * @Created by yangmeng
 */
@Getter
@AllArgsConstructor
public enum MarketTypeEnum {

    MAIN_BOARD("主板", "Main Board", new String[]{"6", "000", "001", "002"}),
    GEM("创业板", "Growth Enterprise Market (GEM)", new String[]{"3"}),
    STAR_MARKET("科创板", "Sci-Tech Innovation Board (STAR Market)", new String[]{"688"}),
    SME_BOARD("中小板", "Small and Medium Enterprise Board", new String[]{"002"}),
    NEEQ("新三板", "National Equities Exchange and Quotations (NEEQ)", new String[]{"8", "4"}),
    BSE("北交所", "Beijing Stock Exchange", new String[]{"8","9"}),
    B_SHARES("B股", "B Shares", new String[]{"900", "200"}),
    ST("ST股", "Special Treatment Stocks", null),
    STAR_ST("*ST股", "Delisting Risk Warning Stocks", null),
    REITS("REITs", "Real Estate Investment Trusts", null),
    CONVERTIBLE_BOND("可转债", "Convertible bond", new String[]{"11","12"}),
    ;

    // 板块中文名称
    private final String chineseName;
    // 板块英文名称
    private final String englishName;
    // 股票代码前缀（用于判断板块）
    private final String[] codePrefixes;
    public static MarketTypeEnum getMarketTypeByCode(String stockCode) {
        if (stockCode == null || stockCode.isEmpty()) {
            return null;
        }

        for (MarketTypeEnum marketType : MarketTypeEnum.values()) {
            if (marketType.getCodePrefixes() != null) {
                for (String prefix : marketType.getCodePrefixes()) {
                    if (stockCode.startsWith(prefix)) {
                        return marketType;
                    }
                }
            }
        }

        return null;
    }

}
