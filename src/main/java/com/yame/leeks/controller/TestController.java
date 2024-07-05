package com.yame.leeks.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Date 2024/2/22
 * @Created by yangmeng
 */
@Slf4j
@RestController
@Api(tags = "测试接口")
@RequestMapping("/test")
public class TestController {

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ApiOperation(value = "测试导出")
    public void testExport(HttpServletResponse response){
        try {
            response.setContentType("application/octet-stream");
            String fileName = "测试.xlsx";
            // 设置文件名称
            response.addHeader("Content-Disposition", String.format("attachment;filename=\"%s\"", URLEncoder.encode(fileName, "utf-8")));
            //创建excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            // 创建工作簿
            XSSFSheet sheet = workbook.createSheet();

            //边框居中样式
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            //标题样式 边框 居中 字体加黑
            CellStyle titleCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            titleCellStyle.setFont(font);
            titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleCellStyle.setBorderTop(BorderStyle.THIN);
            titleCellStyle.setBorderBottom(BorderStyle.THIN);
            titleCellStyle.setBorderLeft(BorderStyle.THIN);
            titleCellStyle.setBorderRight(BorderStyle.THIN);

            // 创建大标题
            XSSFRow firstTitleRow = sheet.createRow(0);
            for (int i = 0; i < 4; i++) {
                firstTitleRow.createCell(i+5*i).setCellValue("大标题"+(i+1));
                //合并单元格
                sheet.addMergedRegion(new CellRangeAddress(0, 0, i+5*i, i+5*i+5));
                firstTitleRow.getCell(i+5*i).setCellStyle(titleCellStyle);
            }
            //创建二级标题
            XSSFRow secondTitleRow = sheet.createRow(1);
            for (int i = 0; i < 24; i++) {
                secondTitleRow.createCell(i).setCellValue("小标题"+(i+1));
                secondTitleRow.getCell(i).setCellStyle(titleCellStyle);
            }
            List list = new ArrayList();
            for (int i = 0; i < 24; i++) {
                XSSFRow row = sheet.createRow(2+i);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(i+1);
            }
            workbook.write(response.getOutputStream());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
