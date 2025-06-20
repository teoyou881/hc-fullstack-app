package teo.springjwt.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import teo.springjwt.user.dto.RequestRegisterDTO;
import teo.springjwt.user.entity.UserEntity;
import teo.springjwt.user.enumerated.UserRole;
import teo.springjwt.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder encoder;

  public void signUpProcess(RequestRegisterDTO dto) {
    Boolean isExist = userRepository.existsByEmail(dto.getEmail());

    if (isExist) {
      return;
    }

    UserEntity user = new UserEntity(dto.getEmail(), encoder.encode(dto.getPassword()),  UserRole.ROLE_USER, dto.getUsername(),dto.getPhoneNumber());
    userRepository.save(user);
  }
}
