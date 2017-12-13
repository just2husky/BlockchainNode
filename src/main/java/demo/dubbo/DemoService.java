package demo.dubbo;

import demo.spring.data.User;

import java.util.List;

/**
 * Created by chao on 2017/12/12.
 */
public interface DemoService {
    String sayHello(String name);

    public List<User> getUsers();
}
