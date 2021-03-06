/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.physical.impl.common;

import mockit.NonStrictExpectations;

import org.apache.drill.common.config.DrillConfig;
import org.apache.drill.common.util.FileUtils;
import org.apache.drill.exec.ExecTest;
import org.apache.drill.exec.compile.CodeCompiler;
import org.apache.drill.exec.expr.fn.FunctionImplementationRegistry;
import org.apache.drill.exec.memory.TopLevelAllocator;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.physical.PhysicalPlan;
import org.apache.drill.exec.physical.base.FragmentRoot;
import org.apache.drill.exec.physical.impl.ImplCreator;
import org.apache.drill.exec.physical.impl.OperatorCreatorRegistry;
import org.apache.drill.exec.physical.impl.SimpleRootExec;
import org.apache.drill.exec.planner.PhysicalPlanReader;
import org.apache.drill.exec.proto.BitControl.PlanFragment;
import org.apache.drill.exec.proto.CoordinationProtos;
import org.apache.drill.exec.rpc.user.UserServer.UserClientConnection;
import org.apache.drill.exec.server.DrillbitContext;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class TestHashTable extends ExecTest {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestHashTable.class);
  DrillConfig c = DrillConfig.create();

  @SuppressWarnings("deprecation")
private SimpleRootExec doTest(final DrillbitContext bitContext, UserClientConnection connection, String plan_path) throws Exception{
    new NonStrictExpectations(){{
      bitContext.getMetrics(); result = new MetricRegistry();
      bitContext.getAllocator(); result = new TopLevelAllocator();
      bitContext.getOperatorCreatorRegistry(); result = new OperatorCreatorRegistry(c);
      bitContext.getConfig(); result = c;
      bitContext.getCompiler(); result = CodeCompiler.getTestCompiler(c);
    }};



    PhysicalPlanReader reader = new PhysicalPlanReader(c, c.getMapper(), CoordinationProtos.DrillbitEndpoint.getDefaultInstance());
    PhysicalPlan plan = reader.readPhysicalPlan(Files.toString(FileUtils.getResourceAsFile(plan_path), Charsets.UTF_8));
    FunctionImplementationRegistry registry = new FunctionImplementationRegistry(c);
    FragmentContext context = new FragmentContext(bitContext, PlanFragment.getDefaultInstance(), connection, registry);
    SimpleRootExec exec = new SimpleRootExec(ImplCreator.getExec(context, (FragmentRoot) plan.getSortedOperators(false).iterator().next()));
    return exec;
  }
/*
  @Test
  public void testHashTable1(@Injectable final DrillbitContext bitContext, @Injectable UserClientConnection connection) throws Throwable {
    String plan_path = "/common/test_hashtable1.json";
    SimpleRootExec exec = doTest(bitContext, connection, plan_path);

    SchemaPath regionkey = new SchemaPath("regionkey", ExpressionPosition.UNKNOWN);
    SchemaPath nationkey = new SchemaPath("nationkey", ExpressionPosition.UNKNOWN);

    NamedExpression[] keyExprs = new NamedExpression[1];
    keyExprs[0] = new NamedExpression(regionkey, new FieldReference(regionkey));

    HashTableConfig htConfig = new HashTableConfig(HashTable.DEFAULT_INITIAL_CAPACITY, HashTable.DEFAULT_LOAD_FACTOR, keyExprs);
    ChainedHashTable cht = new ChainedHashTable(htConfig, exec.getContext(), exec.getIncoming(), null);
    HashTable htable = cht.createAndSetupHashTable();
    IntHolder htIdxHolder = new IntHolder();

    for (int i = 0; i < exec.getRecordCount(); i++) {
      HashTable.PutStatus putStatus = htable.put(i, htIdxHolder);
      assertNotEquals(putStatus, HashTable.PutStatus.PUT_FAILED);
    }
    assertEquals(htable.size(), 5);
  }
  */
}
