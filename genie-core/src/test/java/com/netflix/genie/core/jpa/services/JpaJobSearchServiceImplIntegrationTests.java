/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.core.jpa.services;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.Sets;
import com.netflix.genie.common.dto.Application;
import com.netflix.genie.common.dto.JobExecution;
import com.netflix.genie.common.dto.JobStatus;
import com.netflix.genie.common.dto.search.JobSearchResult;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.exceptions.GenieNotFoundException;
import com.netflix.genie.core.services.JobSearchService;
import com.netflix.genie.test.categories.IntegrationTest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Integration tests for the Job Search Service using JPA.
 *
 * @author tgianos
 * @since 3.0.0
 */
@Category(IntegrationTest.class)
@DatabaseSetup("JpaJobSearchServiceImplIntegrationTests/init.xml")
@DatabaseTearDown("cleanup.xml")
public class JpaJobSearchServiceImplIntegrationTests extends DBUnitTestBase {

    private static final String JOB_1_ID = "job1";
    private static final String JOB_2_ID = "job2";
    private static final String JOB_3_ID = "job3";

    // This needs to be injected as a Spring Bean otherwise transactions don't work as there is no proxy
    @Autowired
    private JobSearchService service;

    /**
     * Make sure we can search jobs successfully.
     */
    @Test
    public void canFindJobs() {
        //TODO: add more cases
        final Pageable page = new PageRequest(0, 10, Sort.Direction.DESC, "updated");
        Page<JobSearchResult> jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(3L));

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                Sets.newHashSet(JobStatus.RUNNING),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(2L));
        Assert.assertThat(
            jobs
                .getContent()
                .stream()
                .filter(job -> job.getId().equals(JOB_2_ID) || job.getId().equals(JOB_3_ID))
                .count(),
            Matchers.is(2L)
        );

        jobs = this.service
            .findJobs(
                JOB_1_ID,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(1L));
        Assert.assertThat(
            jobs
                .getContent()
                .stream()
                .filter(job -> job.getId().equals(JOB_1_ID))
                .count(),
            Matchers.is(1L)
        );
    }

    /**
     * Make sure we can get the correct number of job executions which are running on a given host.
     *
     * @throws GenieException on error
     */
    @Test
    public void canFindRunningJobsByHostName() throws GenieException {
        final String hostA = "a.netflix.com";
        final String hostB = "b.netflix.com";
        final String hostC = "c.netflix.com";

        Set<JobExecution> executions = this.service.getAllRunningJobExecutionsOnHost(hostA);
        Assert.assertThat(executions.size(), Matchers.is(1));
        Assert.assertThat(
            executions.stream().filter(jobExecution -> JOB_2_ID.equals(jobExecution.getId())).count(),
            Matchers.is(1L)
        );

        executions = this.service.getAllRunningJobExecutionsOnHost(hostB);
        Assert.assertThat(executions.size(), Matchers.is(1));
        Assert.assertThat(
            executions.stream().filter(jobExecution -> JOB_3_ID.equals(jobExecution.getId())).count(),
            Matchers.is(1L)
        );

        executions = this.service.getAllRunningJobExecutionsOnHost(hostC);
        Assert.assertTrue(executions.isEmpty());
    }

    /**
     * Make sure we can get the host names of nodes currently running jobs.
     */
    @Test
    public void canFindHostnamesOfRunningJobs() {
        final String hostA = "a.netflix.com";
        final String hostB = "b.netflix.com";

        final List<String> hostNames = this.service.getAllHostsRunningJobs();
        Assert.assertThat(hostNames.size(), Matchers.is(2));
        Assert.assertThat(hostNames, Matchers.hasItem(hostA));
        Assert.assertThat(hostNames, Matchers.hasItem(hostB));
    }

    /**
     * Make sure the getting job method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJob() throws GenieException {
        Assert.assertThat(this.service.getJob(JOB_1_ID).getName(), Matchers.is("testSparkJob"));
        Assert.assertThat(this.service.getJob(JOB_2_ID).getName(), Matchers.is("testSparkJob1"));
        Assert.assertThat(this.service.getJob(JOB_3_ID).getName(), Matchers.is("testSparkJob2"));

        try {
            this.service.getJobStatus(UUID.randomUUID().toString());
            Assert.fail();
        } catch (final GenieException ge) {
            Assert.assertTrue(ge instanceof GenieNotFoundException);
        }
    }

    /**
     * Make sure the job status method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobStatus() throws GenieException {
        Assert.assertThat(this.service.getJobStatus(JOB_1_ID), Matchers.is(JobStatus.SUCCEEDED));
        Assert.assertThat(this.service.getJobStatus(JOB_2_ID), Matchers.is(JobStatus.RUNNING));
        Assert.assertThat(this.service.getJobStatus(JOB_3_ID), Matchers.is(JobStatus.RUNNING));

        try {
            this.service.getJobStatus(UUID.randomUUID().toString());
            Assert.fail();
        } catch (final GenieException ge) {
            Assert.assertTrue(ge instanceof GenieNotFoundException);
        }
    }

    /**
     * Make sure the getting job request method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobRequest() throws GenieException {
        Assert.assertThat(this.service.getJobRequest(JOB_1_ID).getCommandArgs(), Matchers.is("-f query.q"));
        Assert.assertThat(this.service.getJobRequest(JOB_2_ID).getCommandArgs(), Matchers.is("-f spark.jar"));
        Assert.assertThat(this.service.getJobRequest(JOB_3_ID).getCommandArgs(), Matchers.is("-f spark.jar"));

        try {
            this.service.getJobRequest(UUID.randomUUID().toString());
            Assert.fail();
        } catch (final GenieException ge) {
            Assert.assertTrue(ge instanceof GenieNotFoundException);
        }
    }

    /**
     * Make sure the getting job execution method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobExecution() throws GenieException {
        Assert.assertThat(this.service.getJobExecution(JOB_1_ID).getProcessId(), Matchers.is(317));
        Assert.assertThat(this.service.getJobExecution(JOB_2_ID).getProcessId(), Matchers.is(318));
        Assert.assertThat(this.service.getJobExecution(JOB_3_ID).getProcessId(), Matchers.is(319));

        try {
            this.service.getJobExecution(UUID.randomUUID().toString());
            Assert.fail();
        } catch (final GenieException ge) {
            Assert.assertTrue(ge instanceof GenieNotFoundException);
        }
    }

    /**
     * Make sure getting the job cluster method returns a valid cluster.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobCluster() throws GenieException {
        Assert.assertThat(this.service.getJobCluster(JOB_1_ID).getId(), Matchers.is("cluster1"));
    }

    /**
     * Make sure getting the job command method returns a valid command.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobCommand() throws GenieException {
        Assert.assertThat(this.service.getJobCommand(JOB_1_ID).getId(), Matchers.is("command1"));
    }

    /**
     * Make sure getting the job applications method returns a valid list of applications.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobApplications() throws GenieException {
        List<Application> applications;
        applications = this.service.getJobApplications(JOB_1_ID);
        Assert.assertThat(applications.size(), Matchers.is(2));
        Assert.assertThat(applications.get(0).getId(), Matchers.is("app1"));
        Assert.assertThat(applications.get(1).getId(), Matchers.is("app3"));
        applications = this.service.getJobApplications(JOB_2_ID);
        Assert.assertThat(applications.size(), Matchers.is(2));
        Assert.assertThat(applications.get(0).getId(), Matchers.is("app1"));
        Assert.assertThat(applications.get(1).getId(), Matchers.is("app2"));
    }
}
