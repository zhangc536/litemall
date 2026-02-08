package org.linlinjava.litemall.wx.config;

import org.linlinjava.litemall.db.domain.LitemallUser;
import org.linlinjava.litemall.db.service.LitemallUserService;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.linlinjava.litemall.wx.annotation.support.LoginUserHandlerMethodArgumentResolver;
import org.linlinjava.litemall.wx.service.UserTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
public class WxWebMvcConfiguration implements WebMvcConfigurer {
    @Autowired
    private LitemallUserService userService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new LoginUserHandlerMethodArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InviteCodeInterceptor(userService)).addPathPatterns("/wx/**");
    }

    private static class InviteCodeInterceptor implements HandlerInterceptor {
        private final LitemallUserService userService;

        private InviteCodeInterceptor(LitemallUserService userService) {
            this.userService = userService;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            MethodParameter[] parameters = handlerMethod.getMethodParameters();
            boolean requiresLogin = false;
            for (MethodParameter parameter : parameters) {
                if (parameter.hasParameterAnnotation(LoginUser.class)) {
                    requiresLogin = true;
                    break;
                }
            }
            if (!requiresLogin) {
                return true;
            }
            String uri = request.getRequestURI();
            if (uri != null && uri.startsWith("/wx/auth/")) {
                return true;
            }
            String token = request.getHeader(LoginUserHandlerMethodArgumentResolver.LOGIN_TOKEN_KEY);
            if (token == null || token.isEmpty()) {
                return true;
            }
            Integer userId = UserTokenManager.getUserId(token);
            if (userId == null) {
                return true;
            }
            LitemallUser user = userService.findById(userId);
            if (user == null) {
                return true;
            }
            return true;
        }
    }
}
