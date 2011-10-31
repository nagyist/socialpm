/*
 * Copyright 2011 <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ocpsoft.socialpm.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;

import org.jboss.seam.security.Identity;
import org.jboss.seam.security.events.DeferredAuthenticationEvent;
import org.jboss.seam.security.external.api.ResponseHolder;
import org.jboss.seam.security.external.openid.OpenIdAuthenticator;
import org.jboss.seam.security.external.openid.api.OpenIdPrincipal;
import org.jboss.seam.security.external.spi.OpenIdRelyingPartySpi;
import org.jboss.seam.security.management.picketlink.IdentitySessionProducer;
import org.jboss.seam.transaction.Transactional;

import com.ocpsoft.socialpm.domain.user.Profile;
import com.ocpsoft.socialpm.model.ProfileService;

@TransactionAttribute
public class OpenIdAuthHandler implements OpenIdRelyingPartySpi
{
   @Inject
   private EntityManager em;

   @Inject
   private Identity identity;

   @Inject
   private ProfileService profileService;

   @Inject
   private ServletContext servletContext;

   @Inject
   private OpenIdAuthenticator openIdAuthenticator;

   @Inject
   private Event<DeferredAuthenticationEvent> deferredAuthentication;

   @Inject
   private HttpServletRequest request;

   @Override
   @Transactional
   public void loginSucceeded(final OpenIdPrincipal principal, final ResponseHolder responseHolder)
   {
      try {
         openIdAuthenticator.success(principal);
         deferredAuthentication.fire(new DeferredAuthenticationEvent(true));

         /*
          * If this is a new registration, we need to create a profile for this OpenId.
          */
         if (attachProfile(principal, responseHolder))
            responseHolder.getResponse().sendRedirect(servletContext.getContextPath() + "/");
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @PostConstruct
   public void init()
   {
      profileService.setEntityManager(em);
   }

   @Inject
   private UserTransaction tx;

   private boolean attachProfile(OpenIdPrincipal principal, ResponseHolder responseHolder)
   {
      try
      {
         Map<String, Object> sessionOptions = new HashMap<String, Object>();
         sessionOptions.put(IdentitySessionProducer.SESSION_OPTION_ENTITY_MANAGER, em);

         String key = identity.getUser().getKey();
         String email = principal.getAttribute("email");

         try {
            int status = tx.getStatus();
            profileService.getProfileByEmail(email);
            identity.logout();

            responseHolder.getResponse().sendRedirect(
                     servletContext.getContextPath()
                              + "/signup?error=The email address used by your OpenID account is already " +
                              "registered with a username on this website. " +
                              "Please log in with that account and visit the " +
                              "Accounts page to merge your profiles.");
         }
         catch (NoResultException e) {
            int status = tx.getStatus();
            if (!profileService.hasProfileByIdentityKey(key))
            {
               Profile p = new Profile();
               p.getKeys().add(key);
               p.setEmail(email);
               p.setFullName(principal.getAttribute("firstName") + " " + principal.getAttribute("lastName"));
               p.setUsername(profileService.getRandomUsername(p.getFullName()));
               profileService.create(p);
               return true;
            }
            else
            {
               // what do we do if they have a profile already but the email address does not match?
            }

         }
      }
      catch (Exception e) {
         try {
            identity.logout();
         }
         catch (Exception e2) {}

         throw new RuntimeException(e);
      }

      return false;
   }

   @Override
   public void loginFailed(final String message, final ResponseHolder responseHolder)
   {
      try {
         responseHolder.getResponse().sendRedirect(servletContext.getContextPath() + "/signup");
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}