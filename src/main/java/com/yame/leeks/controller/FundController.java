package com.yame.leeks.controller;

import com.yame.leeks.common.vo.Result;
import com.yame.leeks.service.FundDataService;
import com.yame.leeks.service.FundService;
import com.yame.leeks.utils.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author yangmeng
 */
@Slf4j
@RestController
@Api(tags = "基金管理接口")
@RequestMapping("/leeks/fund")
public class FundController {

    @Autowired
    private FundService fundService;

    @Autowired
    private FundDataService fundDataService;

    @RequestMapping(value = "/getByPage", method = RequestMethod.GET)
    @ApiOperation(value = "分页查询 为实现返回空")
    public Result getByPage() {
        return null;
    }


    @RequestMapping(value = "/updateLastGsjz", method = RequestMethod.POST)
    @ApiOperation(value = "更新最新估值")
    public Result updateLastGsjz(@RequestParam(required = false) boolean isAll) {
         fundService.updateLastGsjz(isAll);
         return ResultUtil.success();
    }

    @RequestMapping(value = "/updateAllLsjz", method = RequestMethod.POST)
    @ApiOperation(value = "更新上一日历史净值")
    public Result updateAllLsjz(@RequestParam(required = false) boolean isAll) {
        fundDataService.updateAllLsjz(isAll);
        return ResultUtil.success();
    }
    @RequestMapping(value = "/updateLsjz", method = RequestMethod.POST)
    @ApiOperation(value = "根据基金编号更新单个历史净值")
    public Result updateLsjz(@RequestParam String fundCode) {
        fundDataService.updateLsjz(fundCode);
        return ResultUtil.success();
    }

}
