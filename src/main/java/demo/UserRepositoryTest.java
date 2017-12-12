package demo;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by chao on 2017/12/12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring-mongo.xml")
public class UserRepositoryTest {
    @Resource
    private UserRepository userRepository;

    @Before
    public void before() {
        // 删除原有的数据
        userRepository.deleteAll();
        User user = new User("Jack", "YouJumpIJump");
        userRepository.save(user);

        User user1 = new User("Rose", "JackILoveU");
        userRepository.save(user1);

    }

//    @After
//    public void after() {
//        // 测试完毕，删除数据
//        userRepository.deleteAll();
//    }

    @Test
    public void testFindByName() {
        List<User> users = userRepository.findByName("Jack");
        System.out.println(users);
    }

    @Test
    public void testFindByNameAndPassword() {
        List<User> users = userRepository.findByNameAndPassword("Rose", "JackILoveU");
        System.out.println(users);
    }

    @Test
    public void testCustomFind() {
        List<User> users = userRepository.customFind("Jack", "Rose");
        System.out.println(users);
    }

    @Test
    public void testFindByPage() {
        // 查找第0页，共1条
        Page<User> page = userRepository.findByName("Jack", new PageRequest(0, 1));
        System.out.println(page.getContent());
    }

}