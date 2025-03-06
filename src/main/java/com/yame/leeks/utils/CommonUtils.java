package com.yame.leeks.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @Description 公共工具类
 * @Date 2025/3/6
 * @Created by yangmeng
 */
public class CommonUtils {

    public static boolean isStockTradingTime(boolean isBiddingTime) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime openTime = LocalTime.of(9, 30);
        if (isBiddingTime) {
            openTime = LocalTime.of(9, 15);
        }
        LocalTime closeTime = LocalTime.of(15, 00,11);
        LocalTime lunchBreakStart = LocalTime.of(11, 30);
        LocalTime lunchBreakEnd = LocalTime.of(13, 0);

        // Check if today is a weekday
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime currentTime = now.toLocalTime();

        // Check if current time is within trading hours excluding lunch break
        if (currentTime.isAfter(openTime) && currentTime.isBefore(closeTime)) {
            if (currentTime.isBefore(lunchBreakStart) || currentTime.isAfter(lunchBreakEnd)) {
                return true;
            }
        }

        return false;
    }
}
