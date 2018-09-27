/*
 * Copyright 2015 the original author or authors.
 *
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
 */

package org.gradle.tooling.internal.provider.runner

import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.TaskOutputsInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.TaskContainerInternal
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestFilter
import org.gradle.execution.BuildExecutionContext
import org.gradle.execution.taskgraph.TaskExecutionGraphInternal
import org.gradle.tooling.internal.protocol.test.InternalJvmTestRequest
import org.gradle.tooling.internal.provider.TestExecutionRequestAction
import org.gradle.tooling.internal.provider.events.DefaultTestDescriptor
import spock.lang.Specification
import spock.lang.Unroll

class TestExecutionBuildConfigurationActionTest extends Specification {

    public static final String TEST_CLASS_NAME = "TestClass"
    public static final String TEST_METHOD_NAME = "testMethod"
    public static final String TEST_TASK_NAME = ":test"

    ProjectInternal projectInternal
    Test testTask
    TaskContainerInternal tasksContainerInternal
    TestFilter testFilter
    TaskOutputsInternal outputsInternal
    GradleInternal gradleInternal
    BuildExecutionContext buildContext
    TaskExecutionGraphInternal taskGraph
    TestExecutionRequestAction testExecutionRequest

    def setup() {
        outputsInternal = Mock()
        projectInternal = Mock()
        gradleInternal = Mock()
        buildContext = Mock()
        tasksContainerInternal = Mock()
        taskGraph = Mock()
        testExecutionRequest = Mock()
        testTask = Mock()
        testFilter = Mock()

        setupProject()
        setupTestTask()
    }

    private void setupProject() {
        1 * gradleInternal.getTaskGraph() >> taskGraph
        1 * buildContext.getGradle() >> gradleInternal
        _ * gradleInternal.getRootProject() >> projectInternal
    }

    def "empty test execution request configures no tasks"() {
        1 * testExecutionRequest.getTestExecutionDescriptors() >> []
        1 * testExecutionRequest.getInternalJvmTestRequests() >> []

        setup:
        def buildConfigurationAction = new TestExecutionBuildConfigurationAction(testExecutionRequest, gradleInternal);
        when:
        buildConfigurationAction.configure(buildContext)
        then:
        0 * projectInternal.getAllprojects() >> [projectInternal]
        _ * taskGraph.addEntryTasks({ args -> assert args.size() == 0 })
    }

    @Unroll
    def "sets test filter with information from #requestType"() {
        setup:
        _ * projectInternal.getAllprojects() >> [projectInternal]

        1 * testExecutionRequest.getTestExecutionDescriptors() >> descriptors
        1 * testExecutionRequest.getInternalJvmTestRequests() >> internalJvmRequests

        def buildConfigurationAction = new TestExecutionBuildConfigurationAction(testExecutionRequest, gradleInternal);
        when:
        buildConfigurationAction.configure(buildContext)
        then:
        1 * testFilter.includeTest(expectedClassFilter, expectedMethodFilter)

        1 * testTask.setIgnoreFailures(true)
        1 * testFilter.setFailOnNoMatchingTests(false)
        1 * outputsInternal.upToDateWhen(Specs.SATISFIES_NONE)
        where:
        requestType        | descriptors        | internalJvmRequests                                 | expectedClassFilter | expectedMethodFilter
        "test descriptors" | [testDescriptor()] | []                                                  | TEST_CLASS_NAME     | TEST_METHOD_NAME
        "test classes"     | []                 | [jvmTestRequest(TEST_CLASS_NAME, null)]             | TEST_CLASS_NAME     | null
        "test methods"     | []                 | [jvmTestRequest(TEST_CLASS_NAME, TEST_METHOD_NAME)] | TEST_CLASS_NAME     | TEST_METHOD_NAME
    }

    InternalJvmTestRequest jvmTestRequest(String className, String methodName) {
        InternalJvmTestRequest jvmTestRequest = Mock()
        _ * jvmTestRequest.getClassName() >> className
        _ * jvmTestRequest.getMethodName() >> methodName
        jvmTestRequest
    }

    private void setupTestTask() {
        _ * projectInternal.getTasks() >> tasksContainerInternal
        _ * testTask.getFilter() >> testFilter
        _ * tasksContainerInternal.findByPath(TEST_TASK_NAME) >> testTask
        TaskCollection<Test> testTaskCollection = Mock()
        _ * testTaskCollection.iterator() >> [testTask].iterator()
        _ * testTaskCollection.toArray() >> [testTask].toArray()
        _ * tasksContainerInternal.withType(Test) >> testTaskCollection
        _ * testTask.getOutputs() >> outputsInternal
    }

    private DefaultTestDescriptor testDescriptor() {
        new DefaultTestDescriptor(1, "test1", "test 1", "ATOMIC", "test suite", TEST_CLASS_NAME, TEST_METHOD_NAME, 0, TEST_TASK_NAME)
    }

}
