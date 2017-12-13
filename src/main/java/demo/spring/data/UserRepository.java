package demo.spring.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Created by chao on 2017/12/12.
 * 继承MongoRepository接口，其提供了常用的CRUD操作
 * 只需要定义接口，不需要实现该接口，Spring Data Mongo会自动生成实现该接口的代理类
 * 并且根据接口的方法名进行相应的操作
 *
 * MongoRepository泛型接口有两个参数，分别是实体类和主键类型
 */
public interface UserRepository extends MongoRepository<User, String> {
    /**
     * 根据name查找
     * @param name name
     * @return user list
     */
    List<User> findByName(String name);

    /**
     * 同时根据name和password查找
     * @param name name
     * @param password password
     * @return user list
     */
    List<User> findByNameAndPassword(String name, String password);

    /**
     * 分页查找
     * @param name name
     * @param pageable 分页请求
     * @return 查找出的当前页的结果
     */
    Page<User> findByName(String name, Pageable pageable);

    /**
     * 自定义查找
     * 查找姓名为name1 或者 name2 的user
     * @param name1 name1
     * @param name2 name2
     * @return user list
     */
    @Query("{'$or':[{'name': ?0}, {'name': ?1}]}")
    List<User> customFind(String name1, String name2);
}
