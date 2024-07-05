package com.yame.leeks.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.yame.leeks.service.FundDataService;
import com.yame.leeks.service.FundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * @Description TODO
 * @Date 2024/1/25
 * @Created by yangmeng
 */
@Component
@Slf4j
public class FundTask {

    @Autowired
    private FundService fundService;

    @Autowired
    private FundDataService fundDataService;

    @Value("${ttjj.lzjz_init}")
    private boolean lzjzInit;

    @Autowired
    @Qualifier("realDataFlushExecutor")
    private ThreadPoolTaskExecutor realDataFlushExecutor;
    @Autowired
    @Qualifier("lzjzExecutor")
    private ThreadPoolTaskExecutor lzjzExecutor;

    //@Scheduled(cron = "30 0/5 9,10,11,13,14,15 * * ? ")
    public void updateLastGsjz() {
        long execTime = DateUtil.date().getTime();
        long startTime = DateUtil.parse(DateUtil.today() + " 09:30:00").getTime();
        long endTime = DateUtil.parse(DateUtil.today() + " 15:10:00").getTime();
        if (execTime >= startTime && execTime <= endTime) {
            fundService.updateLastGsjz(true);
            log.info("{} 定时刷新估值-------", DateUtil.now());
        }

    }


    //@Scheduled(cron = "0 * 9,10,11,13,14,15 * * ? ")
    public void updateMyLastGsjz() {
        long execTime = DateUtil.date().getTime();
        long startTime = DateUtil.parse(DateUtil.today() + " 09:30:00").getTime();
        long endTime = DateUtil.parse(DateUtil.today() + " 15:10:00").getTime();
        if (execTime >= startTime && execTime <= endTime) {
            fundService.updateLastGsjz(false);
            log.info("{} 定时刷新自选估值-------", DateUtil.now());
        }
    }

    @Scheduled(cron = "0/30 * * * * ? ")
    //@Scheduled(cron = "0 * 9-15 * * ? ")
    public void executorStatus() {
        JSONObject item = new JSONObject();
        item.set("name", "realDataFlush");
        item.set("activeCount", realDataFlushExecutor.getActiveCount());
        item.set("completedTaskCount", realDataFlushExecutor.getThreadPoolExecutor().getCompletedTaskCount());
        item.set("queueSize", realDataFlushExecutor.getThreadPoolExecutor().getQueue().size());
        if(item.getInt("activeCount")>0) {
            log.info("定时刷新线程池状态：{}", item);
        }
        JSONObject lzjz = new JSONObject();
        lzjz.set("name", "lzjzExecutor");
        lzjz.set("activeCount", lzjzExecutor.getActiveCount());
        lzjz.set("completedTaskCount", lzjzExecutor.getThreadPoolExecutor().getCompletedTaskCount());
        lzjz.set("queueSize", lzjzExecutor.getThreadPoolExecutor().getQueue().size());
        if(lzjz.getInt("activeCount")>0) {
            log.info("定时lzjz线程池状态：{}", lzjz);
        }
    }

    @Scheduled(cron = "0 5 0 * * ?")
    public void updateAllLsjz() {
        fundDataService.updateAllLsjz(true);
    }


    @PostConstruct
    public void init() {
        log.info("上一日历史净值初始化:{}", lzjzInit);
        long startTime = DateUtil.parse(DateUtil.today() + " 10:00:00").getTime();

        if (DateUtil.date().getTime() < startTime && lzjzInit) {
            fundDataService.updateAllLsjz(true);
        }
    }

    public static void main(String[] args) {
        //int integer = Integer.parseInt("1.00");
        //System.out.println(integer);

        String equipmentNo = getNewEquipmentNo("SYXH", "00032");
        System.out.println("生成设备编号：" + equipmentNo);
    }

    public static String getNewEquipmentNo(String equipmentType, String equipmentNo){
        // 默认一个初始设备编号
        String newEquipmentNo = equipmentType + "00001";
        // 判断传入的设备类型与最新设备编号不为空
        if(equipmentNo != null && !equipmentNo.isEmpty()){
            // 字符串数字解析为整数
            int no = Integer.parseInt(equipmentNo);
            // 最新设备编号自增1
            int newEquipment = ++no;
            // 将整数格式化为5位数字
            newEquipmentNo = String.format(equipmentType + "%04d", newEquipment);
        }
        return newEquipmentNo;
    }

}
