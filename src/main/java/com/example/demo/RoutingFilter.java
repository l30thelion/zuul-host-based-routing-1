package com.example.demo;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORWARD_TO_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

@Component
public class RoutingFilter extends ZuulFilter {
    
    private final String consumerDomain;
    private final String consumerAdminDomain;
    
    public RoutingFilter(@Value("${domains.consumer-domain:local-dev-consumer-web.com}") String consumerDomain,
                         @Value("${domains.consumer-admin-domain:admin.local-dev-consumer-web.com}") String consumerAdminDomain) {
        this.consumerDomain = consumerDomain;
        this.consumerAdminDomain = consumerAdminDomain;
    }
    
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }
    
    @Override
    public int filterOrder() {
        return 0;
    }
    
    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        return !requestContext.containsKey(FORWARD_TO_KEY) && !requestContext.containsKey(SERVICE_ID_KEY);
    }
    
    @Override
    public Object run() {
        
        RequestContext requestContext = RequestContext.getCurrentContext();
        
        String host = requestContext.getRequest().getServerName();
        
        if (StringUtils.isBlank(host)) {
            
            return null;
            
        }
        
        if (consumerDomain.equalsIgnoreCase(host)) {
            
            setConsumerWebServiceId(requestContext);
            
        } else if (consumerAdminDomain.equalsIgnoreCase(host)) {
            
            setConsumerWebAdminServiceId(requestContext);
            
        }
        
        return null;
    }
    
    private void setConsumerWebServiceId(RequestContext requestContext) {
        
        String requestURI = getRequestURI(requestContext);
        
        if (isContextPathMatch("/consumer-web", requestURI)) {
    
            requestContext.put(SERVICE_ID_KEY, "consumer-web");
            
        } else {
    
            requestContext.put(SERVICE_ID_KEY, "consumer-web-static");
        }
    }
    
    private void setConsumerWebAdminServiceId(RequestContext requestContext) {
        
        String requestURI = getRequestURI(requestContext);
        
        if (isContextPathMatch("/consumer-web-admin", requestURI)) {
            
            requestContext.put(SERVICE_ID_KEY, "consumer-web-admin");
            
        } else if (isContextPathMatch("/consumer-web-admin-other", requestURI)) {
            
            requestContext.put(SERVICE_ID_KEY, "consumer-web-admin-other");
            
        } else {
            
            requestContext.put(SERVICE_ID_KEY, "consumer-web-admin-static");
        }
    }
    
    private String getRequestURI(RequestContext requestContext) {
        
        return requestContext.getRequest().getRequestURI();
        
    }
    
    private boolean isContextPathMatch(String uriToMatch, String requestURI) {
        
        return requestURI.startsWith(uriToMatch + "/") || requestURI.equalsIgnoreCase(uriToMatch);
        
    }
}