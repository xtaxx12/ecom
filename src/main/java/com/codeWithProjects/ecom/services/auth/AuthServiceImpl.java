package com.codeWithProjects.ecom.services.auth;

import com.codeWithProjects.ecom.dto.SignupRequest;
import com.codeWithProjects.ecom.dto.UserDto;
import com.codeWithProjects.ecom.entity.Order;
import com.codeWithProjects.ecom.entity.PasswordResetToken;
import com.codeWithProjects.ecom.entity.User;
import com.codeWithProjects.ecom.enums.OrderStatus;
import com.codeWithProjects.ecom.enums.UserRole;
import com.codeWithProjects.ecom.repository.OrderRepository;
import com.codeWithProjects.ecom.repository.PasswordResetTokenRepository;
import com.codeWithProjects.ecom.repository.UserRepository;
import com.codeWithProjects.ecom.services.jwt.PasswordResetTokenService;
import com.codeWithProjects.ecom.utils.FileUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Optional;


@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final OrderRepository orderRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordResetTokenService passwordResetTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                           OrderRepository orderRepository, PasswordResetTokenRepository passwordResetTokenRepository,
                           JavaMailSender javaMailSender, PasswordResetTokenService passwordResetTokenService, PasswordResetTokenRepository passwordResetTokenRepository1) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.orderRepository = orderRepository;
        this.passwordResetTokenService = passwordResetTokenService;
        this.javaMailSender = javaMailSender;
        this.passwordResetTokenRepository = passwordResetTokenRepository1;
    }

    @Override
    public UserDto createUser(SignupRequest signupRequest) {

        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setName(signupRequest.getName());
        user.setPassword(bCryptPasswordEncoder.encode(signupRequest.getPassword()));
        user.setRole(UserRole.CUSTOMER);

        User createdUser = userRepository.save(user);

        Order order = new Order();
        order.setAmount(0L);
        order.setTotalAmount(0L);
        order.setDiscount(0L);
        order.setUser(createdUser);
        order.setOrderStatus(OrderStatus.Pending);
        orderRepository.save(order);


        UserDto userDto = new UserDto();
        userDto.setId(createdUser.getId());


        return userDto;
    }

    @Override
    public Boolean hasUserWithEmail(String email) {

        Optional<User> user = userRepository.findFirstByEmail(email);
        boolean exists = user.isPresent();

        return exists;
    }

    public void createAdminAccount() {

        User adminAccount = userRepository.findByRole(UserRole.ADMIN);
        if (adminAccount == null) {

            User user = new User();
            user.setEmail("joel@email.com");
            user.setName("joel");
            user.setRole(UserRole.ADMIN);
            user.setPassword(bCryptPasswordEncoder.encode("admin"));
            userRepository.save(user);

        } else {

        }
    }

    @Transactional
    public void sendRecoveryEmail(String email) throws MessagingException, IOException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        PasswordResetToken token = passwordResetTokenService.createToken(user);
        String resetUrl = "http://localhost:4200/reset-password?token=" + token.getToken();
        String subject = "Password Reset Request";

        // Leer el contenido del archivo HTML
        String content = FileUtil.readHtmlFile("templates/Contenido.html");
        // Reemplazar el marcador con el URL real
        content = content.replace("{{resetUrl}}", resetUrl);

        sendEmail(user.getEmail(), subject, content);
    }

    private void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        javaMailSender.send(message);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token or token has expired"));

        // Obtener el usuario del token
        User user = passwordResetToken.getUser();

        // Actualizar la contraseña del usuario
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

        // Eliminar el token después de su uso
        passwordResetTokenRepository.delete(passwordResetToken);
    }


    public Optional<User> findUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(token);
        return passwordResetToken != null && !passwordResetToken.isExpired();
    }

}


