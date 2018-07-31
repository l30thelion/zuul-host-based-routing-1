package com.example.demo;

import com.netflix.zuul.context.RequestContext;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RoutingFilterTest {
    
    private static final String CONSUMER_DOMAIN = "local-dev-consumer-web.com";
    private static final String CONSUMER_REQUEST_URI = "/consumer-web/api/products/query";
    
    private static final String CONSUMER_ADMIN_DOMAIN = "admin.local-dev-consumer-web.com";
    private static final String CONSUMER_ADMIN_REQUEST_URI = "/consumer-web-admin/api/products/query";
    
    private final RoutingFilter routingFilter = new RoutingFilter(CONSUMER_DOMAIN, CONSUMER_ADMIN_DOMAIN);
    
    private void setupRequestContext(String hostName, String requestURI) {
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName(hostName);
        request.setRequestURI(requestURI);
        request.setQueryString("param1=value1&param");
        
        RequestContext requestContext = new RequestContext();
        requestContext.setRequest(request);
        requestContext.setResponse(new MockHttpServletResponse());
        
        RequestContext.testSetCurrentContext(requestContext);
    }
    
    @After
    public void reset() {
        RequestContext.testSetCurrentContext(null);
    }
    
    @Test
    public void testFilterType() {
        assertEquals("pre", routingFilter.filterType());
    }
    
    @Test
    public void testFilterOrder() {
        assertEquals(0, routingFilter.filterOrder());
    }
    
    @Test
    public void testShouldFilter() {
        assertTrue(routingFilter.shouldFilter());
    }
    
    @Test
    public void testHostNameIsNull() {
        setupRequestContext(null, CONSUMER_REQUEST_URI);
        assertNull(routingFilter.run());
        assertNull(RequestContext.getCurrentContext().get("serviceId"));
    }
    
    @Test
    public void testHostNameIsBlank() {
        setupRequestContext("  ", CONSUMER_REQUEST_URI);
        assertNull(routingFilter.run());
        assertNull(RequestContext.getCurrentContext().get("serviceId"));
    }
    
    @Test
    public void testHostNameNotMatch() {
        setupRequestContext("host name won't match, expect null", CONSUMER_REQUEST_URI);
        assertNull(routingFilter.run());
        assertNull(RequestContext.getCurrentContext().get("serviceId"));
        
    }
    
    @Test
    public void testHostNameMatchRequetURINotMatch() {
        setupRequestContext(CONSUMER_DOMAIN, "/no-match-for-consumer-web/api/products/query");
        assertNull(routingFilter.run());
        assertEquals("consumer-web-static", RequestContext.getCurrentContext().get("serviceId"));
    }
    
    @Test
    public void testHostNameAndRequetURIMatch() {
        setupRequestContext(CONSUMER_DOMAIN, CONSUMER_REQUEST_URI);
        assertNull(routingFilter.run());
        assertEquals("consumer-web", RequestContext.getCurrentContext().get("serviceId"));
    }
    
    @Test
    public void testHostNameAndRequetURIMatchNoTrailingSlash() {
        setupRequestContext(CONSUMER_DOMAIN, "/consumer-web");
        assertNull(routingFilter.run());
        assertEquals("consumer-web", RequestContext.getCurrentContext().get("serviceId"));
    }
    
    @Test
    public void testHostNameAndRequetURIMatchConsumerAdminDefault() {
        setupRequestContext(CONSUMER_ADMIN_DOMAIN, "/no-match-for-consumer-web/api/products/query");
        assertNull(routingFilter.run());
        assertEquals("consumer-web-admin-static", RequestContext.getCurrentContext().get("serviceId"));
    }
    
    @Test
    public void testHostNameAndRequetURIMatchConsumerAdmin() {
        setupRequestContext(CONSUMER_ADMIN_DOMAIN, CONSUMER_ADMIN_REQUEST_URI);
        assertNull(routingFilter.run());
        assertEquals("consumer-web-admin", RequestContext.getCurrentContext().get("serviceId"));
    }
    
    @Test
    public void testHostNameAndRequetURIMatchConsumerAdminOther() {
        setupRequestContext(CONSUMER_ADMIN_DOMAIN, "/consumer-web-admin-other");
        assertNull(routingFilter.run());
        assertEquals("consumer-web-admin-other", RequestContext.getCurrentContext().get("serviceId"));
    }
}