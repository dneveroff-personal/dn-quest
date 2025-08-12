package dn.quizengine.model.repository;

import dn.quizengine.model.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findUserByEmail(String username);
}
