package com.taoshao.taopicture.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.taoshao.taopicture.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel 测试
 *
 * @author taoshao
 */
//@SpringBootTest
public class EasyExcelTest {

    @Test
    public void doImport() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        List<Map<Integer, String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
        System.out.println(list);
    }
    @Resource
    private UserService userService;

    @Test
    public void doWrite() {
        // 定义 Excel 文件的路径

        String fileName = "demoData.xlsx";

        // 准备数据
        List<DemoData> data = new ArrayList<>();
        data.add(new DemoData("Hello", new Date(), 3.14));
        data.add(new DemoData("EasyExcel", new Date(), 1.59));

        // 写入数据到 Excel 文件
        EasyExcel.write(fileName, DemoData.class)
                .sheet("Sheet1")
                .doWrite(data);
    }
}
