/*
 * Copyright 2016 the original author or authors.
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
package com.dopplertask.doppler;

import com.dopplertask.doppler.dao.TaskDao;
import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.dao.TaskExecutionLogDao;
import com.dopplertask.doppler.domain.Task;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.TaskExecutionLog;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private TaskExecutionDao taskExecutionDao;

    @Autowired
    private TaskExecutionLogDao taskExecutionLogDao;

    @Test
    public void noParamGreetingShouldReturnDefaultMessage() throws Exception {
        this.mockMvc.perform(post("/schedule/task")).andDo(print()).andExpect(status().isBadRequest());
    }


    @Test
    public void testBuildAndRunShouldReturnOK() throws Exception {
        String createJson = "{\n" +
                "    \"name\":\"accept-alert\",\n" +
                "    \"actions\":[\n" +
                "        {\n" +
                "            \"@type\":\"PrintAction\",\n" +
                "            \"message\":\"Testing text\"\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";
        MvcResult result = this.mockMvc.perform(post("/task").content(createJson)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.checksum").isString()).andReturn();


        String requestTaskRunStr = "{\n" +
                "  \"taskName\": \"accept-alert\",\n" +
                "  \"parameters\": {\n" +
                "  }\n" +
                "}";
        this.mockMvc.perform(post("/schedule/directtask").contentType(MediaType.APPLICATION_JSON).content(requestTaskRunStr)).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testBuildAndRunWithParametersShouldReturnOK() throws Exception {
        String createJson = "{\n" +
                "    \"name\":\"example-task-2\",\n" +
                "    \"actions\":[\n" +
                "        {\n" +
                "            \"@type\":\"PrintAction\",\n" +
                "            \"message\":\"$parameters.get('message')\"\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";
        MvcResult result = this.mockMvc.perform(post("/task").content(createJson)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.checksum").isString()).andReturn();


        String requestTaskRunStr = "{\n" +
                "  \"taskName\": \"example-task-2\",\n" +
                "  \"parameters\": {\n" +
                " \"message\":\"Hello my fellow automators\"\n" +
                "  }\n" +
                "}";
        this.mockMvc.perform(post("/schedule/directtask").contentType(MediaType.APPLICATION_JSON).content(requestTaskRunStr)).andDo(print()).andExpect(status().isOk()).andExpect(content().json("{\"output\":[\"Task execution started [taskId=1, executionId=1]\",\"Hello my fellow automators\",\"Task execution completed [taskId=1, executionId=1, success=true]\"]}"));

        Optional<Task> task = taskDao.findFirstByNameOrderByCreatedDesc("example-task-2");

        Assert.assertEquals(true, task.isPresent());
        Assert.assertEquals("example-task-2", task.get().getName());


        /*List<TaskExecution> execution = taskExecutionDao.findAllByTask(task.get());
        Assert.assertNotNull(execution.get(1).getTask());

        List<TaskExecutionLog> logs = taskExecutionLogDao.findByTaskExecution(execution.get(1));

        Assert.assertEquals("Hello my fellow automators", logs.get(1).getOutput());*/
    }


    @Test
    @Ignore
    public void testBuildAndRunBrowseWebActionWithParametersShouldReturnOK() throws Exception {
        // Start server
        FakeWebserver webserver = new FakeWebserver(7894);

        String createJson = "{\n" +
                "    \"name\":\"example-task-3\",\n" +
                "    \"actions\":[\n" +
                "        {\n" +
                "            \"@type\":\"BrowseWebAction\",\n" +
                "            \"url\":\"http://localhost:7894\",\n" +
                "            \"actionList\": [\n" +
                "                {\"fieldName\": \"exampleInput\", \"findByType\": \"ID\", \"action\": \"WRITE\", \"value\": \"${message}\"}\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";
        MvcResult result = this.mockMvc.perform(post("/task").content(createJson)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.id").isString()).andReturn();


        String requestTaskRunStr = "{\n" +
                "  \"taskName\": \"example-task-3\",\n" +
                "  \"parameters\": {\n" +
                " \"message\":\"Hello my fellow automators\"\n" +
                "  }\n" +
                "}";
        this.mockMvc.perform(post("/schedule/directtask").contentType(MediaType.APPLICATION_JSON).content(requestTaskRunStr)).andDo(print()).andExpect(status().isOk());

        webserver.close();

        Optional<Task> task = taskDao.findFirstByNameOrderByCreatedDesc("example-task-3");
        Assert.assertEquals(true, task.isPresent());
        Assert.assertEquals("example-task-3", task.get().getName());
        List<TaskExecution> execution = taskExecutionDao.findAllByTask(task.get());
        Assert.assertNotNull(execution.get(1).getTask());
        List<TaskExecutionLog> logs = taskExecutionLogDao.findByTaskExecution(execution.get(1));

        System.out.println("Log size: " + logs.size() + " " + logs.get(1).getOutput());

        Assert.assertEquals(true, logs.get(1).getOutput().contains("text=Hello my fellow automators"));
    }


}
