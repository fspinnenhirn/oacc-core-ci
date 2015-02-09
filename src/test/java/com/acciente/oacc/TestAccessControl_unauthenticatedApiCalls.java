/*
 * Copyright 2009-2015, Acciente LLC
 *
 * Acciente LLC licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.acciente.oacc;

import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class TestAccessControl_unauthenticatedApiCalls extends TestAccessControlBase {
   @Test
   public void unimpersonate_shouldSucceed() throws AccessControlException {
      accessControlContext.unimpersonate();
   }

   @Test
   public void unauthenticated_noSetupReqd_shouldFail() throws AccessControlException {
      // verify authentication state
      try {
         accessControlContext.getAuthenticatedResource();
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getSessionResource();
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }

      // verify create methods
      try {
         accessControlContext.createResourceClass(null, false, false);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.createResourcePermission(null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.createDomain(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.createDomain(null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.createResource("any_resource_class_name");
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.createResource("any_resource_class_name", PasswordCredentials.newInstance(generateUniquePassword()));
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }

      // verify getters
      try {
         accessControlContext.getEffectiveResourceCreatePermissionsMap(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getAccessorResourcesByResourcePermission(null, null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveResourceCreatePermissionsMap(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveResourceCreatePermissionsMap(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveDomainCreatePermissions(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveDomainPermissionsMap(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveDomainPermissionsMap(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveGlobalResourcePermissionsMap(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveGlobalResourcePermissionsMap(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveGlobalResourcePermissionsMap(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getResourcePermissionNames(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getEffectiveResourcePermissions(null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getResourceClassInfoByResource(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getResourceClassNames();
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getDomainNameByResource(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getDomainDescendants(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getResourcesByResourcePermission(null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }

      // verify impersonate
      try {
         accessControlContext.impersonate(null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }

      // verify setters
      try {
         accessControlContext.setCredentials(null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }

      try {
         accessControlContext.setResourceCreatePermissions(null, null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.setResourceCreatePermissions(null, null, null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.setDomainCreatePermissions(null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.setDomainPermissions(null, null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.setGlobalResourcePermissions(null, null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.setGlobalResourcePermissions(null, null, null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.setResourcePermissions(null, null, null);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
   }

   @Test
   public void unauthenticated_withReqdSetup_shouldFail() throws AccessControlException, SQLException, InterruptedException {
      // verify authentication state
      try {
         accessControlContext.getAuthenticatedResource();
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
      try {
         accessControlContext.getSessionResource();
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }

      // setup basic scenario
      final String resourceClassName = generateResourceClass(false, false);
      final String authenticatableResourceClassName = generateResourceClass(true, false);
      final String domainName = generateDomain();

      // need valid resourceClass and domains to test the
      // following method calls from an unauthenticated context
      try {
         accessControlContext.createResource(resourceClassName, domainName);
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }

      try {
         accessControlContext.createResource(authenticatableResourceClassName,
                                             domainName,
                                             PasswordCredentials.newInstance("password".toCharArray()));
      }
      catch (AccessControlException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not authenticated"));
      }
   }
}
