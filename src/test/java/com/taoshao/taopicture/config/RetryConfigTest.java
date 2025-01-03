package com.taoshao.taopicture.config;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * @Author taoshao
 * @Date 2024/10/7
 */
@SpringBootTest
class RetryConfigTest {

        @Resource
        private Retryer<Boolean> retryer;

        private int i = 1;

        @Test
        public void test() {
            try {
                retryer.call(this::isGreaterThan10);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (RetryException e) {
                // 超过指定的重试次数、超过的最大等待时间或超过重试时间就会抛这个异常
                throw new RuntimeException(e);
            }
        }

        // 判断生成的随机数是否等于 10
        private boolean isGreaterThan10() {
            Random random = new Random();
            System.out.println("重试次数：" + i++);
            int num = random.nextInt();
            System.out.println(num);
            return num == 10;
        }
}