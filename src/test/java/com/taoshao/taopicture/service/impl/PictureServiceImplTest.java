package com.taoshao.taopicture.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.taoshao.taopicture.common.ErrorCode;
import com.taoshao.taopicture.exception.BusinessException;
import com.taoshao.taopicture.model.dto.picture.PictureUploadRequest;
import com.taoshao.taopicture.model.entity.User;
import com.taoshao.taopicture.model.vo.PictureVO;
import com.taoshao.taopicture.service.PictureService;
import com.taoshao.taopicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * 使用 CompletableFuture 实现批量上传和入库
 * @author taoshao
 * @date 2025/3/1
 */
@Slf4j
@SpringBootTest
class PictureServiceImplTest {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    // 自定义线程池
    private final ExecutorService executorService = new ThreadPoolExecutor(
            24,
            32,
            5,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(20000), // 阻塞队列使用的是有界阻塞队列，容量为 20000
            Executors.defaultThreadFactory(), // 使用默认的线程工厂
            new ThreadPoolExecutor.AbortPolicy() // 任务的拒绝策略，默认的任务处理策略
    );

    @Test
    void uploadPictureByBatch() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 校验参数
        String searchText = "小黑子";
        int count = 10;
        String category = "搞笑";
        String tag = "表情包";
        String namePrefix = searchText;

        // 从第几个开始获取
        int first = RandomUtil.randomInt(1, 301);
        // 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&first=%s&mmasync=1", searchText, first);
        Document document = null;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 修改选择器，获取包含完整数据的元素
        Elements imgElementList = div.select("a.iusc");

        // 使用 CompletableFuture 并发处理图片上传和入库
        CompletableFuture<?>[] futures = imgElementList.stream()
                .limit(count)
                .map(imgElement -> CompletableFuture.runAsync(
                        () -> {
                            String fileUrl;
                            String introduction;
                            String dataM = imgElement.attr("m");
                            try {
                                // 解析JSON字符串
                                JSONObject jsonObject = JSONUtil.parseObj(dataM);
                                // 获取murl字段（原始图片URL）
                                fileUrl = jsonObject.getStr("murl");
                                // 获取图片标题
                                introduction = jsonObject.getStr("t");
                            } catch (Exception e) {
                                log.error("解析图片数据失败", e);
                                return;
                            }
                            if (StrUtil.isBlank(fileUrl)) {
                                log.info("当前链接为空，已跳过：{}", fileUrl);
                                return;
                            }
                            // 处理图片的地址，防止转义或者和对象存储冲突的问题
                            int questionMarkIndex = fileUrl.indexOf("?");
                            // 有，截断
                            if (questionMarkIndex > -1) {
                                fileUrl = fileUrl.substring(0, questionMarkIndex);
                            }
                            // 上传图片
                            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                            pictureUploadRequest.setFileUrl(fileUrl);
                            pictureUploadRequest.setPicName(namePrefix + (imgElementList.indexOf(imgElement) + 1));
                            pictureUploadRequest.setIntroduction(introduction);
                            pictureUploadRequest.setCategory(category);
                            pictureUploadRequest.setTags(JSONUtil.toJsonStr(tag));

                            User loginUser = userService.getById(1875357145545859073L);
                            try {
                                PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                                log.info("图片上传成功，id={}", pictureVO.getId());
                            } catch (Exception e) {
                                log.error("图片上传失败，", e);
                            }

                        }, executorService))
                .toArray(CompletableFuture[]::new);
        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}