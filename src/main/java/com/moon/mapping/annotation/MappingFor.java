package com.moon.mapping.annotation;

import com.moon.mapping.MappingUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 映射注册
 *
 * 将{@code MappingFor}注册在普通 PO、BO、VO、DO、Entity、Model 上, 并指定映射目标
 * 映射目标同样是一个 PO、DO 或其他数据模型
 *
 * 运行是可以通过{@link MappingUtil}获取相应映射。如下:
 *
 * <pre>
 * public class CarEntity {
 *     private String id;
 *     private String name;
 *     private Integer kilometers;
 *     // ...
 * }
 *
 * &#64;MappingFor({CarEntity.class})
 * public class CarVO {
 *    private String id;
 *    private String name;
 *    // 自动类型转换
 *    private String kilometers;
 * }
 *
 * public class DemoApplication {
 *
 *     public static void main(String[] args) {
 *         // 获取映射器 {@link MappingUtil#get(Class, Class)}
 *         BeanMapping&lt;CarVO, CarEntity&gt; mapping = MappingUtil.get(CarVO.class, CarEntity.class);
 *     }
 * }
 * </pre>
 *
 * @author moonsky
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingFor {

    Class<?>[] value();
}
