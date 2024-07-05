package com.yame.leeks.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yame.leeks.entity.Fund;
import com.yame.leeks.entity.FundData;
import com.yame.leeks.mapper.FundCategoryMapper;
import com.yame.leeks.mapper.FundDataMapper;
import com.yame.leeks.mapper.FundMapper;
import com.yame.leeks.service.FundService;
import com.yame.leeks.thread.RealDataFlushThread;
import com.yame.leeks.thread.ZiXuanFlushThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基金接口实现
 *
 * @author yangmeng
 */
@Slf4j
@Service
public class FundServiceImpl extends ServiceImpl<FundMapper, Fund> implements FundService {

    @Value("${ttjj.url.get_funds}")
    private String getFunds;
    @Value("${ttjj.url.last_gsjz}")
    private String lastGsjz;
    @Value("${ttjj.url.fund_info}")
    private String fundInfo;
    @Value("${ttjj.init}")
    private boolean init;

    @Autowired
    private FundMapper fundMapper;

    @Autowired
    private FundDataMapper fundDataMapper;

    @Autowired
    private FundCategoryMapper fundCategoryMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("realDataFlushExecutor")
    private ThreadPoolTaskExecutor realDataFlushExecutor;
    @Autowired
    @Qualifier("ziXuanFlushexecutor")
    private ThreadPoolTaskExecutor ziXuanFlushexecutor;

    /**
     * 初始化基金基础数据
     */
    @PostConstruct
    public void initFundInfo() {
        if (!init) {
            log.info("初始化未开启...");
            return;
        }

        URI uri = URI.create(getFunds);
        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(uri, byte[].class);
        //获取响应体中的内容
        byte[] responseEntityBody = responseEntity.getBody();
        //将byte[]转为JSON格式的字符串
        String json = new String(responseEntityBody);
        String result = json.replace("var r = ", "").replace(";", "");
        JSONArray fundsArray = JSONObject.parseArray(result);
        List<Fund> oldfunds = fundMapper.selectList(null);
        Map<String, Fund> fundMap = oldfunds.stream().collect(Collectors.toMap(Fund::getFundCode, fund -> fund));
        List<Fund> funds = new ArrayList<>();
        /*List<FundCategory> categoryList = fundCategoryMapper.selectList(null);
        Map<Long, String> categoryMap = categoryList.stream().collect(Collectors.toMap(FundCategory::getId, FundCategory::getName));
        Map<String, String> zuheCategoryMap = new HashMap<>();
        for (FundCategory fundCategory : categoryList) {
            String key;
            if(fundCategory.getParentId()!=0l) {
                key = categoryMap.get(fundCategory.getParentId()) + "-" + fundCategory.getName();
            }else{
                key = categoryMap.get(fundCategory.getId());
            }
            zuheCategoryMap.put(key, fundCategory.getId().toString());
        }*/


        for (Object fundStr : fundsArray) {
            JSONArray fundArray = JSONObject.parseArray(fundStr.toString());

            String categoryName = fundArray.get(3) == null ? "未知" : fundArray.get(3).toString();
            /*if(categoryName.contains("-")){
                String[] categoryArr = categoryName.split("-");
            }else{

            }*/

            String fundCode = fundArray.get(0).toString();
            Fund fund;
            if (fundMap.containsKey(fundCode)) {
                fund = fundMap.get(fundCode);
            } else {
                fund = new Fund();
                fund.setFundCode(fundArray.get(0).toString());
            }
            fund.setName(fundArray.get(2).toString());
            //fund.setCategory(zuheCategoryMap.get(categoryName));
            fund.setCategory(categoryName);
            funds.add(fund);
        }
        this.saveOrUpdateBatch(funds);
    }

    /**
     * 更新刷新 估值
     */
    @Override
    public void updateLastGsjz(boolean isAll) {
        QueryWrapper<Fund> queryWrapper = new QueryWrapper();
        if (!isAll) {
            queryWrapper.isNotNull("tag");
        }
        queryWrapper.isNotNull("gszzl");
        queryWrapper.orderByAsc("fund_code");
        List<Fund> funds = fundMapper.selectList(queryWrapper);
        Map<String, Fund> fundMap = funds.stream().collect(Collectors.toMap(Fund::getFundCode, fund -> fund));
        List<String> fundCodes = funds.stream().map(Fund::getFundCode).collect(Collectors.toList());
        for (String fundCode : fundCodes) {
            Fund fund = fundMap.get(fundCode);
            if (isAll) {
                realDataFlushExecutor.submit(new RealDataFlushThread(fund));
            } else {
                ziXuanFlushexecutor.submit(new ZiXuanFlushThread(fund));
            }
        }
    }

    public void realDataFlushGsjz(Fund fund) {
        URI uri = URI.create(lastGsjz + fund.getFundCode() + ".js?rt=" + System.currentTimeMillis());
        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(uri, byte[].class);
            //获取响应体中的内容
            byte[] responseEntityBody = responseEntity.getBody();
            //将byte[]转为JSON格式的字符串
            String json = new String(responseEntityBody);
            String result = json.replace("jsonpgz(", "").replace(");", "");
            JSONObject jsonObject = JSONObject.parseObject(result);

            fund.setGszzl(jsonObject.getFloat("gszzl"));
            fund.setGztime(jsonObject.getDate("gztime"));
            fundMapper.updateById(fund);
            if (StrUtil.isNotBlank(fund.getTag())) {
                insertFundData(fund);
            }
        } catch (Exception e) {
            log.info("请求异常的url:{}", uri);
        }
    }

    private void insertFundData(Fund fund) {
        FundData fundData = new FundData();
        fundData.setValueType(1);
        fundData.setValue(fund.getGszzl());
        fundData.setFundCode(fund.getFundCode());
        fundData.setTime(fund.getGztime());
        QueryWrapper<FundData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fund_code", fundData.getFundCode());
        queryWrapper.eq("value_type", 1);
        queryWrapper.eq("time", fund.getGztime());
        FundData fd = fundDataMapper.selectOne(queryWrapper);
        if (fd == null) {
            fundDataMapper.insert(fundData);
        }
    }

}
