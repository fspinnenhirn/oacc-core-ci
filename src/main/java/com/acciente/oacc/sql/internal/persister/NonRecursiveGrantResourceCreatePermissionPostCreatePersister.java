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
package com.acciente.oacc.sql.internal.persister;

import com.acciente.oacc.Resource;
import com.acciente.oacc.ResourceCreatePermission;
import com.acciente.oacc.sql.SQLProfile;
import com.acciente.oacc.sql.internal.persister.id.DomainId;
import com.acciente.oacc.sql.internal.persister.id.Id;
import com.acciente.oacc.sql.internal.persister.id.ResourceClassId;
import com.acciente.oacc.sql.internal.persister.id.ResourceId;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NonRecursiveGrantResourceCreatePermissionPostCreatePersister extends CommonGrantResourceCreatePermissionPostCreatePersister {
   public NonRecursiveGrantResourceCreatePermissionPostCreatePersister(SQLProfile sqlProfile,
                                                                       SQLStrings sqlStrings) {
      super(sqlProfile, sqlStrings);
   }

   @Override
   public Set<ResourceCreatePermission> getResourceCreatePostCreatePermissionsIncludeInherited(SQLConnection connection,
                                                                                               Resource accessorResource,
                                                                                               Id<ResourceClassId> resourceClassId,
                                                                                               Id<DomainId> resourceDomainId) {
      SQLStatement statement = null;

      try {
         // first get all the resources from which the accessor inherits any permissions
         final Set<Id<ResourceId>> accessorResourceIds
               = NonRecursivePersisterHelper.getInheritedAccessorResourceIds(sqlStrings, connection, accessorResource);

         // get the ancestors of the specified domain, to which the accessors could also have permissions
         final Set<Id<DomainId>> ancestorDomainIds
               = NonRecursivePersisterHelper.getAncestorDomainIds(sqlStrings, connection, resourceDomainId);

         // now collect the sys-permissions any accessor resource has to the specified domain or its ancestors
         SQLResult resultSet;
         Set<ResourceCreatePermission> resourceCreatePermissions = new HashSet<>();
         statement = connection.prepareStatement(sqlStrings.SQL_findInGrantResourceCreatePermissionPostCreate_withoutInheritance_PostCreatePermissionName_PostCreateIsWithGrant_IsWithGrant_BY_AccessorID_AccessedDomainID_ResourceClassID);

         for (Id<ResourceId> accessorResourceId : accessorResourceIds) {
            for (Id<DomainId> domainId : ancestorDomainIds) {
               statement.setResourceId(1, accessorResourceId);
               statement.setResourceDomainId(2, domainId);
               statement.setResourceClassId(3, resourceClassId);
               resultSet = statement.executeQuery();

               while (resultSet.next()) {
                  resourceCreatePermissions.add(getResourceCreatePostCreatePermission(resultSet));
               }
               resultSet.close();
            }
         }

         return resourceCreatePermissions;
      }
      catch (SQLException e) {
         throw new RuntimeException(e);
      }
      finally {
         closeStatement(statement);
      }

   }

   @Override
   public Map<String, Map<String, Set<ResourceCreatePermission>>> getResourceCreatePostCreatePermissionsIncludeInherited(SQLConnection connection,
                                                                                                                         Resource accessorResource) {
      SQLStatement statement = null;

      try {
         // first get all the resources from which the accessor inherits any permissions
         final Set<Id<ResourceId>> accessorResourceIds
               = NonRecursivePersisterHelper.getInheritedAccessorResourceIds(sqlStrings, connection, accessorResource);

         // second, get all the resource create permissions the accessors directly have access to
         SQLResult resultSet;
         Map<String, Map<String, Set<ResourceCreatePermission>>> createPermissionsMap = new HashMap<>();

         statement = connection.prepareStatement(sqlStrings.SQL_findInGrantResourceCreatePermissionPostCreate_withoutInheritance_ResourceDomainName_ResourceClassName_PostCreatePermissionName_PostCreateIsWithGrant_IsWithGrant_BY_AccessorID);

         for (Id<ResourceId> accessorResourceId : accessorResourceIds) {
            statement.setResourceId(1, accessorResourceId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
               final String resourceDomainName = resultSet.getString("DomainName");
               final String resourceClassName = resultSet.getString("ResourceClassName");

               Map<String, Set<ResourceCreatePermission>> permissionsForResourceDomain
                     = createPermissionsMap.get(resourceDomainName);
               if (permissionsForResourceDomain == null) {
                  permissionsForResourceDomain = new HashMap<>();
                  createPermissionsMap.put(resourceDomainName, permissionsForResourceDomain);
               }

               Set<ResourceCreatePermission> permissionsForResourceClass
                     = permissionsForResourceDomain.get(resourceClassName);
               if (permissionsForResourceClass == null) {
                  permissionsForResourceClass = new HashSet<>();
                  permissionsForResourceDomain.put(resourceClassName, permissionsForResourceClass);
               }

               permissionsForResourceClass.add(getResourceCreatePostCreatePermission(resultSet));
            }
            resultSet.close();
         }
         closeStatement(statement);
         statement = null;

         // then apply each domain's direct permissions to all its descendants
         // !! DON'T UPDATE THE PERMISSION-MAP WHILE ITERATING OVER ITS KEY-SET !! (get a copy of the key-set instead)
         Set<String> directDomainNames = new HashSet<>(createPermissionsMap.keySet());
         for (String directDomainName : directDomainNames) {
            Set<String> descendentDomains = NonRecursivePersisterHelper.getDescendantDomainNames(sqlStrings,
                                                                                                 connection,
                                                                                                 directDomainName);

            for (String descendentDomain : descendentDomains) {
               Map<String, Set<ResourceCreatePermission>> permissionsForResourceDomain
                     = createPermissionsMap.get(descendentDomain);
               if (permissionsForResourceDomain == null) {
                  permissionsForResourceDomain = new HashMap<>();
                  createPermissionsMap.put(descendentDomain, permissionsForResourceDomain);
               }

               if (!descendentDomain.equals(directDomainName)) {
                  final Map<String, Set<ResourceCreatePermission>> sourceResourceClassPermissionsMap
                        = createPermissionsMap.get(directDomainName);

                  for (String resourceClassName : sourceResourceClassPermissionsMap.keySet()) {
                     Set<ResourceCreatePermission> permissionsForResourceClass
                           = permissionsForResourceDomain.get(resourceClassName);
                     if (permissionsForResourceClass == null) {
                        permissionsForResourceClass = new HashSet<>();
                        permissionsForResourceDomain.put(resourceClassName, permissionsForResourceClass);
                     }

                     permissionsForResourceClass.addAll(sourceResourceClassPermissionsMap.get(resourceClassName));
                  }
               }
            }
         }

         return createPermissionsMap;
      }
      catch (SQLException e) {
         throw new RuntimeException(e);
      }
      finally {
         closeStatement(statement);
      }
   }

   @Override
   public void removeAllResourceCreatePostCreatePermissions(SQLConnection connection,
                                                            Id<DomainId> accessedDomainId) {
      SQLStatement statement = null;
      try {
         // get descendant domain Ids
         List<Id<DomainId>> descendantDomainIds
               = new ArrayList<>(NonRecursivePersisterHelper.getDescendantDomainIdsOrderedByAscendingLevel(sqlStrings,
                                                                                                           connection,
                                                                                                           accessedDomainId));

         // delete domains' accessors (in reverse order of domainLevel, to preserve FK constraints)
         statement = connection.prepareStatement(sqlStrings.SQL_removeInGrantResourceCreatePermissionPostCreate_BY_AccessedDomainId);

         for (int i=descendantDomainIds.size()-1; i >= 0; i--) {
            statement.setResourceDomainId(1, descendantDomainIds.get(i));
            statement.executeUpdate();
         }
      }
      catch (SQLException e) {
         throw new RuntimeException(e);
      }
      finally {
         closeStatement(statement);
      }
   }
}
