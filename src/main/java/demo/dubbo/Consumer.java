package demo.dubbo;

import demo.spring.data.User;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Created by chao on 2017/12/12.
 */
public class Consumer {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "dubbo-demo-consumer.xml" });
        context.start();

        DemoService demoService = (DemoService) context.getBean("demoService");
        String hello = demoService.sayHello("hejingyuan");
        System.out.println(hello);

        List<User> list = demoService.getUsers();
        if (list != null && list.size() > 0) {
            for (User user : list) {
                System.out.println(user);
            }
        }
        System.in.read();
    }
}
