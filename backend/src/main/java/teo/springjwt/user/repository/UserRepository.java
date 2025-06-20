package teo.springjwt.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teo.springjwt.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryCustom {

  Boolean existsByEmail(String email);
  UserEntity findByEmail(String email);
}
