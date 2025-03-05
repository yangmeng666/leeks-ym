package com.yame.leeks.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yame.leeks.entity.Fund;
import com.yame.leeks.entity.FundData;
import com.yame.leeks.mapper.FundDataMapper;
import com.yame.leeks.mapper.FundMapper;
import com.yame.leeks.service.FundDataService;
import com.yame.leeks.thread.LsjzThread;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基金数据接口实现
 *
 * @author yangmeng
 */
@Slf4j
@Service
public class FundDataServiceImpl extends ServiceImpl<FundDataMapper, FundData> implements FundDataService {

    @Value("${ttjj.url.lsjz}")
    private String lsjz;

    @Autowired
    private FundDataMapper fundDataMapper;
    @Autowired
    private FundMapper fundMapper;

    @Autowired
    @Qualifier("lzjzExecutor")
    private ThreadPoolTaskExecutor lzjzExecutor;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 更所有昨天历史净值
     */
    public void updateAllLsjz(boolean isAll) {
        QueryWrapper<Fund> queryWrapper = new QueryWrapper();
        if (!isAll) {
            queryWrapper.isNotNull("tag");
        }
        queryWrapper.orderByAsc("fund_code");
        List<Fund> funds = fundMapper.selectList(queryWrapper);
        List<String> fundCodes = funds.stream().map(Fund::getFundCode).collect(Collectors.toList());
        for (String fundCode : fundCodes) {
            lzjzExecutor.submit(new LsjzThread(fundCode));
        }
    }

    @Override
    public void updateLsjz(String fundCode) {
        String sdate = DateUtil.yesterday().toString("yyyy-MM-dd");
        String edate = DateUtil.today();
        int week = DateUtil.date().dayOfWeek();
        if (week == 1) {
            sdate = DateUtil.offsetDay(DateUtil.yesterday(), -1).toString("yyyy-MM-dd");
        } else if (week == 2) {
            sdate = DateUtil.offsetDay(DateUtil.yesterday(), -2).toString("yyyy-MM-dd");
        }

        QueryWrapper<FundData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fund_code", fundCode);
        queryWrapper.eq("value_type", 2);
        Map<String, FundData> fundDataMap = fundDataMapper.selectList(queryWrapper)
                .stream().collect(Collectors.toMap(x -> x.getFundCode() + DateUtil.date(x.getTime()).toString("yyyy-MM-dd HH:mm:ss"), x -> x));
        if(fundDataMap.get(fundCode+sdate+" HH:mm:ss")!=null){
            return;
        }
        URI uri = URI.create(lsjz + fundCode + "&sdate=" + sdate + "&edate=" + edate);
        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(uri, byte[].class);
            //获取响应体中的内容
            byte[] responseEntityBody = responseEntity.getBody();
            //将byte[]转为JSON格式的字符串
            String json = new String(responseEntityBody);
            String result = json.replace("var apidata={ content:\"", "");
            String html = result.substring(0, json.indexOf("</table>") - 15);
            Document doc = Jsoup.parse(html);
            Elements tables = doc.select("table");
            for (Element table : tables) {
                Elements tbody = table.select("tbody");
                Elements rows = tbody.select("tr");
                for (Element tr : rows) {
                    Elements td = tr.select("td");

                    if (td.size() > 1) {
                        DateTime jzrqDateTime = null;
                        Float jz = null;
                        try {
                            jz = Float.valueOf(td.get(3).text().replace("%", ""));
                            String jzrq = td.get(0).text().replace("*", "");
                            jzrqDateTime = DateUtil.parse(jzrq);
                        } catch (Exception e) {
                            return;
                        }
                        String key = fundCode + jzrqDateTime;
                        //log.info("key====={}",key);
                        FundData fundData = fundDataMap.get(fundCode + jzrqDateTime);
                        if (fundData != null) {
                            continue;
                        }
                        fundData = new FundData();
                        fundData.setFundCode(fundCode);
                        fundData.setValueType(2);
                        fundData.setTime(jzrqDateTime);
                        fundData.setValue(jz);
                        log.info("fundata:{}", fundData);
                        this.save(fundData);
                    }
                }
            }
        } catch (Exception e) {
                log.info("请求失败的历史净值 uri:{}", uri);
        }
        //JSONObject lzjz = new JSONObject();
        //lzjz.set("name", "lzjzExecutor");
        //lzjz.set("activeCount", lzjzExecutor.getActiveCount());
        //lzjz.set("completedTaskCount", lzjzExecutor.getThreadPoolExecutor().getCompletedTaskCount());
        //lzjz.set("queueSize", lzjzExecutor.getThreadPoolExecutor().getQueue().size());
        //log.info("lzjz线程池状态：{}", lzjz);
    }
}
