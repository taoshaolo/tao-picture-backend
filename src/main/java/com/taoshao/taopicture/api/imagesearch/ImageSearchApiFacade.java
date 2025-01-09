package com.taoshao.taopicture.api.imagesearch;

import com.taoshao.taopicture.api.imagesearch.model.ImageSearchResult;
import com.taoshao.taopicture.api.imagesearch.sub.GetImageFirstUrlApi;
import com.taoshao.taopicture.api.imagesearch.sub.GetImageListApi;
import com.taoshao.taopicture.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 门面模式
 * @Author taoshao
 * @Date 2025/1/9
 */
@Slf4j
public class ImageSearchApiFacade {

    public static List<ImageSearchResult> searchImage(String imageUrl){
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        String imageUrl = "http://www.codefather.cn/logo.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表："+ resultList);
    }
}
