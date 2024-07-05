package com.yame.leeks.thread;

import cn.hutool.extra.spring.SpringUtil;
import com.yame.leeks.service.FundDataService;
import lombok.Data;

/**
 * @Description TODO
 * @Date 2023/8/11
 * @Created by yangmeng
 */
@Data
public class LsjzThread extends Thread {

    private String fundCode;

    public LsjzThread(String fundCode){
        currentThread().setName("LsjzThread-"+fundCode);
        this.fundCode = fundCode;
    }

    @Override
    public void run() {
        FundDataService fundService = SpringUtil.getBean(FundDataService.class);
        fundService.updateLsjz(fundCode);
    }
}
