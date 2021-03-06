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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainPermissions {
   // constants for the important system permission names with pre-defined semantics
   private static final SysPermission SYSPERMISSION_SUPER_USER          = new SysPermission(-301, "*SUPER-USER");
   public static final  String        SUPER_USER                        = SYSPERMISSION_SUPER_USER.getPermissionName();
   private static final SysPermission SYSPERMISSION_CREATE_CHILD_DOMAIN = new SysPermission(-302, "*CREATE-CHILD-DOMAIN");
   public static final  String        CREATE_CHILD_DOMAIN               = SYSPERMISSION_CREATE_CHILD_DOMAIN.getPermissionName();
   private static final SysPermission SYSPERMISSION_DELETE              = new SysPermission(-303, "*DELETE");
   public static final  String        DELETE                            = SYSPERMISSION_DELETE.getPermissionName();

   private static final Map<String, SysPermission> sysPermissionsByName;
   private static final Map<Long, String>          sysPermissionNamesById;
   private static final List<String>               sysPermissionNames;
   static {
      sysPermissionsByName = new HashMap<>();
      sysPermissionsByName.put(SUPER_USER, SYSPERMISSION_SUPER_USER);
      sysPermissionsByName.put(CREATE_CHILD_DOMAIN, SYSPERMISSION_CREATE_CHILD_DOMAIN);
      sysPermissionsByName.put(DELETE, SYSPERMISSION_DELETE);

      sysPermissionNamesById = new HashMap<>(sysPermissionsByName.size());
      for (SysPermission sysPermission : sysPermissionsByName.values()) {
         sysPermissionNamesById.put(sysPermission.getSystemPermissionId(), sysPermission.getPermissionName());
      }

      sysPermissionNames = Collections.unmodifiableList(new ArrayList<>(sysPermissionNamesById.values()));
   }

   public static List<String> getSysPermissionNames() {
      return sysPermissionNames;
   }

   public static String getSysPermissionName(long systemPermissionId) {
      final String sysPermissionName = sysPermissionNamesById.get(systemPermissionId);

      if (sysPermissionName == null) {
         throw new IllegalArgumentException("Invalid system permission ID: " + systemPermissionId);
      }

      return sysPermissionName;
   }

   public static DomainPermission getInstance(String sysPermissionName) {
      return new DomainPermissionImpl(sysPermissionName, false);
   }

   public static DomainPermission getInstanceWithGrantOption(String sysPermissionName) {
      return new DomainPermissionImpl(sysPermissionName, true);
   }

   /**
    * @deprecated as of v2.0.0-rc.5; use {@link #getInstanceWithGrantOption(String)} or {@link #getInstance(String)} instead.
    */
   @Deprecated
   public static DomainPermission getInstance(String sysPermissionName, boolean withGrant) {
      return new DomainPermissionImpl(sysPermissionName, withGrant);
   }

   public static DomainPermission getInstance(DomainPermission domainPermission) {
      if (domainPermission instanceof DomainPermissions.DomainPermissionImpl) {
         return domainPermission;
      }

      final DomainPermission verifiedPermission;

      if(domainPermission.isWithGrantOption()) {
         verifiedPermission = getInstanceWithGrantOption(domainPermission.getPermissionName());
      }
      else {
         verifiedPermission = getInstance(domainPermission.getPermissionName());
      }

      // validate system permission name and id matched
      if (verifiedPermission.getSystemPermissionId() != domainPermission.getSystemPermissionId()) {
         throw new IllegalArgumentException("Invalid system permission id for domain permission: " + domainPermission);
      }

      return verifiedPermission;
   }

   private static class DomainPermissionImpl implements DomainPermission, Serializable {
      // permission data
      private final long    systemPermissionId;
      private final String  permissionName;
      private final boolean withGrantOption;

      private DomainPermissionImpl(String sysPermissionName, boolean withGrantOption) {
         SysPermission sysPermission = getSysPermission(sysPermissionName);

         this.systemPermissionId = sysPermission.getSystemPermissionId();
         this.permissionName = sysPermission.getPermissionName();
         this.withGrantOption = withGrantOption;
      }

      @Override
      public boolean isSystemPermission() {
         return systemPermissionId != 0;
      }

      @Override
      public String getPermissionName() {
         return permissionName;
      }

      @Override
      public long getSystemPermissionId() {
         if (!isSystemPermission()) {
            throw new IllegalArgumentException("No system permission ID may be retrieved for user permission: " + permissionName + ", please check your code");
         }

         return systemPermissionId;
      }

      @Override
      public boolean isWithGrantOption() {
         return withGrantOption;
      }

      @Override
      @Deprecated
      public boolean isWithGrant() {
         return isWithGrantOption();
      }

      @Override
      public boolean isGrantableFrom(DomainPermission other) {
         if (other == null) {
            return false;
         }

         if (!other.isWithGrantOption()) {
            return false;
         }

         return permissionName.equals(other.getPermissionName());
      }

      // equals() and hashCode()

      @Override
      public boolean equals(Object other) {
         if (this == other) {
            return true;
         }
         if (other == null || getClass() != other.getClass()) {
            return false;
         }

         DomainPermissionImpl otherDomainPermission = (DomainPermissionImpl) other;

         if (withGrantOption != otherDomainPermission.withGrantOption) {
            return false;
         }
         if (!permissionName.equals(otherDomainPermission.permissionName)) {
            return false;
         }

         return true;
      }

      @Override
      public boolean equalsIgnoreGrantOption(Object other) {
         if (this == other) {
            return true;
         }
         if (other == null || getClass() != other.getClass()) {
            return false;
         }

         DomainPermissionImpl otherDomainPermission = (DomainPermissionImpl) other;

         if (!permissionName.equals(otherDomainPermission.permissionName)) {
            return false;
         }

         return true;
      }

      @Override
      @Deprecated
      public boolean equalsIgnoreGrant(Object other) {
         return equalsIgnoreGrantOption(other);
      }

      @Override
      public int hashCode() {
         int result = permissionName.hashCode();
         result = 31 * result + (withGrantOption ? 1 : 0);
         return result;
      }

      @Override
      public String toString() {
         if (isSystemPermission()) {
            return "DOMAIN:SYS:" + permissionName
                  + (withGrantOption ? " /G" : "");
         }
         else {
            // USR: for user-defined
            return "DOMAIN:" + permissionName
                  + (withGrantOption ? " /G" : "");
         }
      }

      // private static helper method

      private static SysPermission getSysPermission(String permissionName) {
         if (permissionName == null) {
            throw new IllegalArgumentException("A system permission name is required");
         }

         final String trimmedPermissionName = permissionName.trim();

         if (trimmedPermissionName.isEmpty()) {
            throw new IllegalArgumentException("A system permission name is required");
         }

         final SysPermission sysPermission = sysPermissionsByName.get(trimmedPermissionName);

         if (sysPermission == null) {
            throw new IllegalArgumentException("Invalid system permission name: " + trimmedPermissionName);
         }

         return sysPermission;
      }
   }
}
