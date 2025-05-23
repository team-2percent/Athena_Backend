package goorm.athena.global.jwt.util;

import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.token.JwtAuthenticationToken;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CheckLoginArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter){
        return parameter.getParameterAnnotation(CheckLogin.class) != null
                && parameter.getParameterType() == LoginUserRequest.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mvContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new CustomException(ErrorCode.ACCESSTOKEN_EXPIRED);
        }

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;

        Object principal = jwtAuthenticationToken.getPrincipal();
        if(principal == null){
            return null;
        }

        if (!(principal instanceof LoginInfoDto)) {
            return null;
        }
        LoginInfoDto loginInfoDto = (LoginInfoDto) principal;
        LoginUserRequest loginUserRequest = LoginMapper.toRequest(loginInfoDto.nickname(), loginInfoDto.userId(), loginInfoDto.role());

        return loginUserRequest;
    }

}
