package ink.organics.test.spring.model.repository;

import ink.organics.test.spring.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, String> {
}
