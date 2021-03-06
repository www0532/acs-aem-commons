/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.commons.auth.saml.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.osgi.service.component.annotations.ConfigurationPolicy;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * When using the SAML 2.0 Template from Okta, traditional Single Log Out (SLO) support
 * is not available. Logout is instead supported through a special URL.
 *
 * Details are here: https://community.okta.com/community/okta/blog/2014/01/31/logout-and-redirect-to-an-url
 */


/**
 *
 * TODO:
 * - improve the descriptions
 * - add a configuration policy to only work with a provided configuration
 *
 */
@Component(service=AuthenticationHandler.class,
configurationPolicy=ConfigurationPolicy.REQUIRE,
        property= {
      Constants.SERVICE_DESCRIPTION + "=" + "ACS AEM Commons Okta Logout Handler",
})
@Designate(ocd=OktaLogoutHandler.Config.class)
public class OktaLogoutHandler implements AuthenticationHandler {

   @ObjectClassDefinition(name = "ACS AEM Commons - Okta Logout Handler",
        description = "Specific Authentication Handler to handle logout to Okta SSO Provider which, in some configurations, does not support traditional Single Logout")
   public @interface Config {

       int DEFAULT_SERVICE_RANKING = 5003;
       String DEFAULT_PATH = "/";

       @AttributeDefinition(defaultValue = ""+ DEFAULT_SERVICE_RANKING)
       int service_ranking() default DEFAULT_SERVICE_RANKING;

       @AttributeDefinition(defaultValue = DEFAULT_PATH)
       String path() default DEFAULT_PATH;

       @AttributeDefinition
       String okta_host_name();

       @AttributeDefinition
       String from_uri();
    }

    private String redirectLocation;

    @Activate
    protected void activate(OktaLogoutHandler.Config config) {
        String oktaHostName = config.okta_host_name();
        if (oktaHostName == null) {
            throw new IllegalArgumentException("Okta Host Name must be provided");
        }

        StringBuilder builder = new StringBuilder("https://");
        builder.append(config.okta_host_name());
        builder.append("/login/signout");
        if (config.from_uri() != null) {
            builder.append("?fromURI=").append(config.from_uri());
        }
        this.redirectLocation = builder.toString();
    }

    @Override
    public AuthenticationInfo extractCredentials(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public boolean requestCredentials(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return false;
    }

    @Override
    public void dropCredentials(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(redirectLocation);
    }

}
