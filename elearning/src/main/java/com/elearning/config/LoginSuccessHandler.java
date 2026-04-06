package com.elearning.config;

import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.util.RedirectUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String SESSION_LOGIN_REDIRECT = "LOGIN_REDIRECT";

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String redirect = request.getParameter("redirect");
        if (redirect == null || !RedirectUrls.isSafeRelativePath(redirect)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object s = session.getAttribute(SESSION_LOGIN_REDIRECT);
                if (s instanceof String && RedirectUrls.isSafeRelativePath((String) s)) {
                    redirect = (String) s;
                }
            }
        }
        if (redirect != null && RedirectUrls.isSafeRelativePath(redirect)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(SESSION_LOGIN_REDIRECT);
            }
            getRedirectStrategy().sendRedirect(request, response, request.getContextPath() + redirect);
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            getRedirectStrategy().sendRedirect(request, response, request.getContextPath() + "/");
            return;
        }
        String email = ((UserDetails) principal).getUsername();
        User user = userRepository.findByEmail(email).orElse(null);
        String target = "/dashboard";
        if (user != null) {
            switch (user.getRole()) {
                case STUDENT:
                    target = "/student/dashboard";
                    break;
                case TEACHER:
                    target = "/teacher/dashboard";
                    break;
                case ADMIN:
                    target = "/admin/dashboard";
                    break;
                default:
                    break;
            }
        }
        getRedirectStrategy().sendRedirect(request, response, request.getContextPath() + target);
    }
}
