package CW.chatbot.services;

import CW.chatbot.commons.constants.Role;
import CW.chatbot.controllers.dtos.JwtToken;
import CW.chatbot.controllers.dtos.MemberSignupDto;
import CW.chatbot.controllers.dtos.SignUpReqDto;
import CW.chatbot.entities.USERS;
import CW.chatbot.provider.JwtTokenProvider;
import CW.chatbot.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService { // 서비스 클래스 - 로그인 메서드 구현
    private final UsersRepository usersRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional // 메서드가 포함하고 있는 작업 중에 하나라도 실패할 경우 전체 작업을 취소
    public JwtToken login(String id, String password) {
        // Login ID/PW 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(id, password);

        // 실제 검증 부분 - 사용자 비밀번호 체크
        // authenticate() 메서드를 통해 요청된 Member에 대한 검증이 진행
        // authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증 정보를 기반으로 JWT 토큰 생성 -> 검증 정상 통과
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        return jwtToken;
    }

    @Transactional
    public MemberSignupDto signUp(SignUpReqDto signUpReqDto) {
        log.info("회원가입 시도 ID: {}", signUpReqDto.getUserid());

        if (usersRepository.existsById(signUpReqDto.getUserid())) {
            log.warn("회원가입 실패 : ID 중복 : ", signUpReqDto.getUserid());
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다.");
        }
        // Password 암호화
        String encodedPassword = passwordEncoder.encode(signUpReqDto.getPassword());

        // 회원가입 시, USER 역할 부여
        USERS USERS = signUpReqDto.toEntity(encodedPassword, Role.USER);
        usersRepository.save(USERS);

        log.info("회원가입 성공 ID: {}", signUpReqDto.getUserid());
        
        return MemberSignupDto.toDto(USERS);
    }
}
