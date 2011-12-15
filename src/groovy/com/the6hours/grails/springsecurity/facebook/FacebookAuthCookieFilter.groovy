package com.the6hours.grails.springsecurity.facebook

import org.springframework.web.filter.GenericFilterBean
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.context.ApplicationEventPublisher
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Cookie
import org.springframework.security.core.Authentication
import org.springframework.security.authentication.AuthenticationManager
import javax.servlet.http.HttpServletResponse

/**
 * TODO
 *
 * @since 14.10.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class FacebookAuthCookieFilter extends GenericFilterBean implements ApplicationEventPublisherAware {

    ApplicationEventPublisher applicationEventPublisher
    FacebookAuthUtils facebookAuthUtils
    AuthenticationManager authenticationManager
    String logoutUrl = '/j_spring_security_logout'

    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, javax.servlet.FilterChain chain) {
        HttpServletRequest request = servletRequest
        HttpServletResponse response = servletResponse
        Cookie cookie = facebookAuthUtils.getAuthCookie(request)
        String url = request.requestURI.substring(request.contextPath.length())
        logger.debug("Processing url: $url")
        if (url != logoutUrl && SecurityContextHolder.context.authentication == null) {
            logger.debug("Applying facebook auth filter")

            if (cookie != null) {
                FacebookAuthToken token = facebookAuthUtils.build(cookie.value)
                if (token != null) {
                    Authentication authentication = authenticationManager.authenticate(token);
                    // Store to SecurityContextHolder
                    SecurityContextHolder.context.authentication = authentication;

                    if (logger.isDebugEnabled()) {
                        logger.debug("SecurityContextHolder populated with FacebookAuthToken: '"
                            + SecurityContextHolder.context.authentication + "'");
                    }
                    try {
                        chain.doFilter(request, response);
                    } finally {
                        SecurityContextHolder.context.authentication = null;
                    }
                    return
                }
            } else {
                logger.debug("No auth cookie")
            }
        } else {
            logger.debug("SecurityContextHolder not populated with FacebookAuthToken token, as it already contained: $SecurityContextHolder.context.authentication");
        }
        chain.doFilter(request, response);
    }


}
