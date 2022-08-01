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
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(columnDefinition = "char", length = 32)
    private String id;
    private String name;
    private Integer age;
    private String email;
}
