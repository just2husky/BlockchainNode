package demo;

import entity.Block;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.mapping.Document;
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
public interface BlockRepository extends MongoRepository<Block, String> {
    /**
     * 获取所有Block
     * @return
     */
    List<Block> findAllOrOrderByTimestamp();
}
