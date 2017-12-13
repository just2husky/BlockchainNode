package demo.spring.data;

import entity.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by chao on 2017/12/12.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
@EnableAutoConfiguration
public class Application {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BlockRepository blockRepository;

    @RequestMapping("/")
    @ResponseBody
    public String greeting() {
        List<User> users = userRepository.findByName("Jack");
        System.out.println(users);
        return "Hello World!" + users;
    }

    @RequestMapping("/blockchain")
    @ResponseBody
    public List<Block> getBlockChain(){
        List<Block> blockList = blockRepository.findAll();
        return blockRepository.findAll();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
