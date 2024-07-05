package com.yame.leeksym;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yame.leeks.LeeksYmApplication;
import com.yame.leeks.entity.Fund;
import com.yame.leeks.entity.FundData;
import com.yame.leeks.mapper.FundCategoryMapper;
import com.yame.leeks.mapper.FundDataMapper;
import com.yame.leeks.mapper.FundMapper;
import com.yame.leeks.service.FundDataService;
import com.yame.leeks.service.FundService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest(classes = LeeksYmApplication.class)
class LeeksYmApplicationTests extends AbstractTestNGSpringContextTests {

    @Value("${ttjj.url.get_funds}")
    private String getFunds;
    @Value("${ttjj.url.lsjz}")
    private String lsjz;
    @Value("${ttjj.url.https_lsjz}")
    private String httpsLsjz;
    @Value("${ttjj.url.last_gsjz}")
    private String lastGsjz;
    @Value("${ttjj.url.fund_info}")
    private String fundInfo;
    @Value("${ttjj.init}")
    private boolean init;

    @Autowired
    private FundService fundService;
    @Autowired
    private FundDataService fundDataService;

    @Autowired
    private FundMapper fundMapper;

    @Autowired
    private FundDataMapper fundDataMapper;

    @Autowired
    private FundCategoryMapper fundCategoryMapper;

    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void testFlush() {
        fundService.updateLastGsjz(false);
    }

    @Test
    public void test() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add("Content-Type", "application/json");
        Map params = new HashMap();
        HttpEntity httpEntity = new HttpEntity(params, httpHeaders);
        //URI uri = URI.create("http://fundgz.1234567.com.cn/js/" + founds[0] + ".js?rt="+System.currentTimeMillis());
        URI uri = URI.create("http://fund.eastmoney.com/js/fundcode_search.js");
        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(uri, byte[].class);
        //获取响应体中的内容
        byte[] responseEntityBody = responseEntity.getBody();
        //将byte[]转为JSON格式的字符串
        String json = new String(responseEntityBody);
        String result = json.replace("var r = ", "").replace(";", "");
        JSONArray fundsArray = JSONObject.parseArray(result);
        List<Fund> funds = new ArrayList<>();
        for (Object fundStr : fundsArray) {
            Fund fund = new Fund();
            JSONArray fundArray = JSONObject.parseArray(fundStr.toString());
            fund.setFundCode(fundArray.get(0).toString());
            fund.setName(fundArray.get(2).toString());
            fund.setCategory(fundArray.get(3).toString());
            funds.add(fund);
        }
        System.out.println(funds.subList(0, 5));
    }

    @Test
    public void testRealData() {
        QueryWrapper<Fund> queryWrapper = new QueryWrapper();
        queryWrapper.isNotNull("tag");
        List<Fund> funds = fundMapper.selectList(queryWrapper);
        for (Fund fund : funds) {
            URI uri = URI.create("http://fundgz.1234567.com.cn/js/" + fund.getFundCode() + ".js");

            ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(uri, byte[].class);
            //获取响应体中的内容
            byte[] responseEntityBody = responseEntity.getBody();
            //将byte[]转为JSON格式的字符串
            String json = new String(responseEntityBody);
            String result = json.replace("jsonpgz(", "").replace(");", "");
            JSONObject jsonObject = JSONObject.parseObject(result);
            fund.setFundCode(fund.getFundCode());
            fund.setGztime(jsonObject.getDate("gztime"));
            fund.setGszzl(jsonObject.getFloat("gszzl"));
            fund.setName(jsonObject.getString("name"));
            System.out.println(fund);
        }

    }

    @Test
    public void testLsjz() {
        QueryWrapper<Fund> queryWrapper = new QueryWrapper();
        List<Fund> funds = fundMapper.selectList(queryWrapper);
        for (Fund fund : funds) {
            System.out.println(fund);
            //http://fund.eastmoney.com/f10/F10DataApi.aspx?type=lsjz&code=012414&sdate=2024-01-10&edate=2024-01-26
            URI uri = URI.create(lsjz + fund.getFundCode() + "&sdate=" + DateUtil.yesterday().toString("yyyy-MM-dd") + "&edate=" + DateUtil.today());
            ResponseEntity<byte[]> responseEntity;
            try {
                responseEntity = restTemplate.getForEntity(uri, byte[].class);
            }catch (Exception e){
                continue;
            }
            //获取响应体中的内容
            byte[] responseEntityBody = responseEntity.getBody();
            //将byte[]转为JSON格式的字符串
            String json = new String(responseEntityBody);
            String result = json.replace("var apidata={ content:\"", "");
            String html = result.substring(0, json.indexOf("</table>") - 15);
            //System.out.println(html);
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
                            String jzrq = td.get(0).text().replace("*","");
                            jzrqDateTime = DateUtil.parse(jzrq);
                        } catch (NumberFormatException e) {

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        FundData fundData = new FundData();
                        fundData.setFundCode(fund.getFundCode());
                        fundData.setValueType(2);
                        fundData.setTime(jzrqDateTime);
                        fundData.setValue(jz);
                        System.out.println(fundData);
                        fundDataMapper.insert(fundData);

                    }
                }
            }
        }

    }
    @Test
    public void testLsjzAll(){
        fundDataService.updateAllLsjz(true);
    }

}
