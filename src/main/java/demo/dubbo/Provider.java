package demo.dubbo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by chao on 2017/12/12.
 */
public class Provider {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "dubbo-demo-provider.xml" });
        context.start();
        System.in.read(); // 为保证服务一直开着，利用输入流的阻塞来模拟
    }
}
