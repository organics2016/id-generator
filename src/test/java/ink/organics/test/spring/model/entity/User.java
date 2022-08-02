package ink.organics.test.spring.model.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity(name = "user_t")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(generator = "my-generator")
    @GenericGenerator(name = "my-generator", strategy = "ink.organics.test.spring.model.generator.MyGenerator")
    private String id;
    private String name;
    private Integer age;
    private String email;
}
