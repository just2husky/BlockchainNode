package demo.dubbo;

import demo.spring.data.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chao on 2017/12/12.
 */
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }

    @Override
    public List getUsers() {
        List<User> list = new ArrayList<User>();
        User u1 = new User("1", "chao", "123");
        User u2 = new User("2", "ting", "123");
        list.add(u1);
        list.add(u2);
        return list;
    }
}
