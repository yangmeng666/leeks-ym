package com.yame.leeks.thread;

import cn.hutool.extra.spring.SpringUtil;
import com.yame.leeks.entity.Fund;
import com.yame.leeks.service.FundService;
import lombok.Data;

/**
 * @Description TODO
 * @Date 2023/8/11
 * @Created by yangmeng
 */
@Data
public class RealDataFlushThread extends Thread {

    private Fund fund;

    public RealDataFlushThread(Fund fund){
        currentThread().setName("RealDataFlushThread-"+fund.getId());
        this.fund = fund;
    }

    @Override
    public void run() {
        FundService fundService = SpringUtil.getBean(FundService.class);
        fundService.realDataFlushGsjz(fund);
    }
}
