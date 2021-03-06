/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.controller.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.Role;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.RequestStatusResponse;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceProvider;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.orm.InMemoryDefaultTestModule;
import org.apache.ambari.server.orm.dao.HostRoleCommandDAO;
import org.apache.ambari.server.orm.entities.HostRoleCommandEntity;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * TaskResourceProvider tests.
 */
public class TaskResourceProviderTest {
  @Test
  public void testCreateResources() throws Exception {
    Resource.Type type = Resource.Type.Task;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    // add the property map to a set for the request.  add more maps for multiple creates
    Set<Map<String, Object>> propertySet = new LinkedHashSet<Map<String, Object>>();

    Map<String, Object> properties = new LinkedHashMap<String, Object>();

    // add properties to the request map
    properties.put(TaskResourceProvider.TASK_REQUEST_ID_PROPERTY_ID, 100);
    properties.put(TaskResourceProvider.TASK_ID_PROPERTY_ID, 100);

    propertySet.add(properties);

    // create the request
    Request request = PropertyHelper.getCreateRequest(propertySet, null);

    try {
      provider.createResources(request);
      Assert.fail("Expected an UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }

    // verify
    verify(managementController, response);
  }

  @Test
  public void testGetResources() throws Exception {
    Resource.Type type = Resource.Type.Task;

    AmbariManagementController amc = createMock(AmbariManagementController.class);
    HostRoleCommandDAO hostRoleCommandDAO = createMock(HostRoleCommandDAO.class);

    Injector m_injector = Guice.createInjector(new InMemoryDefaultTestModule());
    TaskResourceProvider provider = (TaskResourceProvider) AbstractControllerResourceProvider.getResourceProvider(
        type, PropertyHelper.getPropertyIds(type), PropertyHelper.getKeyPropertyIds(type), amc);

    m_injector.injectMembers(provider);
    TaskResourceProvider.s_dao = hostRoleCommandDAO;

    List<HostRoleCommandEntity> entities = new ArrayList<>();
    HostRoleCommandEntity hostRoleCommandEntity = new HostRoleCommandEntity();
    hostRoleCommandEntity.setRequestId(100L);
    hostRoleCommandEntity.setTaskId(100L);
    hostRoleCommandEntity.setStageId(100L);
    hostRoleCommandEntity.setRole(Role.DATANODE);
    hostRoleCommandEntity.setCustomCommandName("customCommandName");
    hostRoleCommandEntity.setCommandDetail("commandDetail");
    entities.add(hostRoleCommandEntity);

    // set expectations
    expect(hostRoleCommandDAO.findAll(EasyMock.anyObject(Request.class),
        EasyMock.anyObject(Predicate.class))).andReturn(entities).once();

    // replay
    replay(hostRoleCommandDAO);

    Set<String> propertyIds = new HashSet<String>();

    propertyIds.add(TaskResourceProvider.TASK_ID_PROPERTY_ID);
    propertyIds.add(TaskResourceProvider.TASK_REQUEST_ID_PROPERTY_ID);
    propertyIds.add(TaskResourceProvider.TASK_COMMAND_DET_PROPERTY_ID);

    Predicate predicate = new PredicateBuilder().property(TaskResourceProvider.TASK_ID_PROPERTY_ID).equals("100").
                          and().property(TaskResourceProvider.TASK_REQUEST_ID_PROPERTY_ID).equals("100").toPredicate();
    Request request = PropertyHelper.getReadRequest(propertyIds);
    Set<Resource> resources = provider.getResources(request, predicate);

    Assert.assertEquals(1, resources.size());
    for (Resource resource : resources) {
      long taskId = (Long) resource.getPropertyValue(TaskResourceProvider.TASK_ID_PROPERTY_ID);
      Assert.assertEquals(100L, taskId);
      Assert.assertEquals(null, resource.getPropertyValue(TaskResourceProvider
          .TASK_CUST_CMD_NAME_PROPERTY_ID));
      Assert.assertEquals("commandDetail", resource.getPropertyValue(TaskResourceProvider
          .TASK_COMMAND_DET_PROPERTY_ID));
    }

    // verify
    verify(hostRoleCommandDAO);
  }

  @Test
  public void testUpdateResources() throws Exception {
    Resource.Type type = Resource.Type.Task;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    // add the property map to a set for the request.
    Map<String, Object> properties = new LinkedHashMap<String, Object>();

    // create the request
    Request request = PropertyHelper.getUpdateRequest(properties, null);

    Predicate predicate = new PredicateBuilder().property(TaskResourceProvider.TASK_ID_PROPERTY_ID).equals("Task100").
        toPredicate();

    try {
      provider.updateResources(request, predicate);
      Assert.fail("Expected an UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }

    // verify
    verify(managementController, response);
  }

  @Test
  public void testDeleteResources() throws Exception {
    Resource.Type type = Resource.Type.Task;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    // replay
    replay(managementController);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    Predicate predicate = new PredicateBuilder().property(TaskResourceProvider.TASK_ID_PROPERTY_ID).equals("Task100").
        toPredicate();
    try {
      provider.deleteResources(predicate);
      Assert.fail("Expected an UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }

    // verify
    verify(managementController);
  }

  @Test
  public void testParseStructuredOutput() {
    Resource.Type type = Resource.Type.Task;
    // Test general case
    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    TaskResourceProvider taskResourceProvider = new TaskResourceProvider(
            PropertyHelper.getPropertyIds(type),
            PropertyHelper.getKeyPropertyIds(type), managementController);

    replay(managementController);

    // Check parsing of nested JSON
    Map<?, ?> result = taskResourceProvider
        .parseStructuredOutput("{\"a\":\"b\", \"c\": {\"d\":\"e\",\"f\": [\"g\",\"h\"],\"i\": {\"k\":\"l\"}}}");
    assertEquals(result.size(), 2);
    Map<?, ?> submap = (Map<?, ?>) result.get("c");
    assertEquals(submap.size(), 3);
    List sublist = (List) submap.get("f");
    assertEquals(sublist.size(), 2);
    Map<?, ?> subsubmap = (Map<?, ?>) submap.get("i");
    assertEquals(subsubmap.size(), 1);
    assertEquals(subsubmap.get("k"), "l");

    // Check negative case - invalid JSON
    result = taskResourceProvider.parseStructuredOutput("{\"a\": invalid JSON}");
    assertNull(result);

    // ensure that integers come back as integers
    result = taskResourceProvider.parseStructuredOutput("{\"a\": 5}");
    assertEquals(result.get("a"), 5);

    verify(managementController);
  }

  @Test
  public void testParseStructuredOutputForHostCheck() {
    Resource.Type type = Resource.Type.Task;

    // Test general case
    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    TaskResourceProvider taskResourceProvider = new TaskResourceProvider(
            PropertyHelper.getPropertyIds(type),
            PropertyHelper.getKeyPropertyIds(type), managementController);

    replay(managementController);

    Map<?, ?> result = taskResourceProvider.parseStructuredOutput("{\"host_resolution_check\": {\"failures\": [{\"cause\": [-2, \"Name or service not known\"], \"host\": \"foobar\", \"type\": \"FORWARD_LOOKUP\"}], \"message\": \"There were 1 host(s) that could not resolve to an IP address.\", \"failed_count\": 1, \"success_count\": 3, \"exit_code\": 0}}");

    Assert.assertNotNull(result);
    Map<?,?> host_resolution_check = (Map<?,?>)result.get("host_resolution_check");

    assertEquals(host_resolution_check.get("success_count"), 3);
    assertEquals(host_resolution_check.get("failed_count"), 1);

    verify(managementController);
  }

  @Test
  public void testInvalidStructuredOutput() {
    Resource.Type type = Resource.Type.Task;

    // Test general case
    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    TaskResourceProvider taskResourceProvider = new TaskResourceProvider(
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type), managementController);

    replay(managementController);

    Map<?, ?> result = taskResourceProvider.parseStructuredOutput(null);
    Assert.assertNull(result);

    result = taskResourceProvider.parseStructuredOutput("This is some bad JSON");
    Assert.assertNull(result);

    verify(managementController);
  }

}
