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
package com.netflix.genie.core.jpa.entities;

import com.google.common.collect.Sets;
import com.netflix.genie.common.dto.Command;
import com.netflix.genie.common.dto.CommandStatus;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.exceptions.GeniePreconditionException;
import com.netflix.genie.test.categories.UnitTest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Test the command class.
 *
 * @author tgianos
 */
@Category(UnitTest.class)
public class CommandEntityUnitTests extends EntityTestsBase {
    private static final String NAME = "pig13";
    private static final String USER = "tgianos";
    private static final String EXECUTABLE = "/bin/pig13";
    private static final String VERSION = "1.0";

    private CommandEntity c;

    /**
     * Setup the tests.
     */
    @Before
    public void setup() {
        this.c = new CommandEntity();
    }

    /**
     * Test the default Constructor.
     */
    @Test
    public void testDefaultConstructor() {
        Assert.assertNull(this.c.getSetupFile());
        Assert.assertNull(this.c.getExecutable());
        Assert.assertNull(this.c.getName());
        Assert.assertNull(this.c.getStatus());
        Assert.assertNull(this.c.getUser());
        Assert.assertNull(this.c.getVersion());
        Assert.assertNotNull(this.c.getConfigs());
        Assert.assertTrue(this.c.getConfigs().isEmpty());
        Assert.assertNotNull(this.c.getTags());
        Assert.assertTrue(this.c.getTags().isEmpty());
        Assert.assertNotNull(this.c.getClusters());
        Assert.assertTrue(this.c.getClusters().isEmpty());
        Assert.assertNotNull(this.c.getApplications());
        Assert.assertTrue(this.c.getApplications().isEmpty());
    }

    /**
     * Test the argument Constructor.
     *
     * @throws GenieException If any precondition isn't met.
     */
    @Test
    public void testConstructor() throws GenieException {
        c = new CommandEntity(NAME, USER, VERSION, CommandStatus.ACTIVE, EXECUTABLE);
        Assert.assertNull(this.c.getSetupFile());
        Assert.assertEquals(EXECUTABLE, this.c.getExecutable());
        Assert.assertEquals(NAME, this.c.getName());
        Assert.assertEquals(CommandStatus.ACTIVE, this.c.getStatus());
        Assert.assertEquals(USER, this.c.getUser());
        Assert.assertEquals(VERSION, this.c.getVersion());
        Assert.assertNotNull(this.c.getConfigs());
        Assert.assertTrue(this.c.getConfigs().isEmpty());
        Assert.assertNotNull(this.c.getTags());
        Assert.assertTrue(this.c.getTags().isEmpty());
        Assert.assertNotNull(this.c.getClusters());
        Assert.assertTrue(this.c.getClusters().isEmpty());
        Assert.assertNotNull(this.c.getApplications());
        Assert.assertTrue(this.c.getApplications().isEmpty());
    }

    /**
     * Test to make sure validation works.
     *
     * @throws GenieException If any precondition isn't met.
     */
    @Test
    public void testOnCreateOrUpdateCommand() throws GenieException {
        this.c = new CommandEntity(NAME, USER, VERSION, CommandStatus.ACTIVE, EXECUTABLE);
        Assert.assertNotNull(this.c.getTags());
        Assert.assertTrue(this.c.getTags().isEmpty());
        this.c.onCreateOrUpdateCommand();
        Assert.assertEquals(2, this.c.getTags().size());
    }

    /**
     * Make sure validation works on valid apps.
     */
    @Test
    public void testValidate() {
        this.c = new CommandEntity(NAME, USER, VERSION, CommandStatus.ACTIVE, EXECUTABLE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from super class.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoName() {
        this.c = new CommandEntity(null, USER, VERSION, CommandStatus.ACTIVE, EXECUTABLE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from super class.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoUser() {
        this.c = new CommandEntity(NAME, "   ", VERSION, CommandStatus.ACTIVE, EXECUTABLE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from super class.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoVersion() {
        this.c = new CommandEntity(NAME, USER, "", CommandStatus.ACTIVE, EXECUTABLE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from command.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoStatus() {
        this.c = new CommandEntity(NAME, USER, VERSION, null, EXECUTABLE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from command.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoExecutable() {
        this.c = new CommandEntity(NAME, USER, VERSION, CommandStatus.ACTIVE, "    ");
        this.validate(this.c);
    }

    /**
     * Test setting the status.
     */
    @Test
    public void testSetStatus() {
        Assert.assertNull(this.c.getStatus());
        this.c.setStatus(CommandStatus.ACTIVE);
        Assert.assertEquals(CommandStatus.ACTIVE, this.c.getStatus());
    }

    /**
     * Test setting the property file.
     */
    @Test
    public void testSetEnvPropFile() {
        Assert.assertNull(this.c.getSetupFile());
        final String propFile = "s3://netflix.propFile";
        this.c.setSetupFile(propFile);
        Assert.assertEquals(propFile, this.c.getSetupFile());
    }

    /**
     * Test setting the executable.
     */
    @Test
    public void testSetExecutable() {
        Assert.assertNull(this.c.getExecutable());
        this.c.setExecutable(EXECUTABLE);
        Assert.assertEquals(EXECUTABLE, this.c.getExecutable());
    }

    /**
     * Test setting the configs.
     */
    @Test
    public void testSetConfigs() {
        Assert.assertNotNull(this.c.getConfigs());
        Assert.assertTrue(this.c.getConfigs().isEmpty());
        final Set<String> configs = new HashSet<>();
        configs.add("s3://netflix.configFile");
        this.c.setConfigs(configs);
        Assert.assertEquals(configs, this.c.getConfigs());

        this.c.setConfigs(null);
        Assert.assertThat(this.c.getConfigs(), Matchers.empty());
    }

    /**
     * Test setting the tags.
     */
    @Test
    public void testSetTags() {
        Assert.assertNotNull(this.c.getTags());
        Assert.assertTrue(this.c.getTags().isEmpty());
        final Set<String> tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");
        this.c.setTags(tags);
        Assert.assertEquals(tags, this.c.getTags());

        this.c.setTags(null);
        Assert.assertThat(this.c.getTags(), Matchers.empty());
    }

    /**
     * Test to make sure we can set the command tags.
     */
    @Test
    public void canSetCommandTags() {
        final Set<String> tags = Sets.newHashSet("Third", "first", "second");
        this.c.setCommandTags(tags);
        Assert.assertThat(this.c.getCommandTags(), Matchers.is(tags));
        Assert.assertThat(this.c.getTags(), Matchers.is(tags));

        this.c.setCommandTags(null);
        Assert.assertThat(this.c.getCommandTags(), Matchers.empty());
        Assert.assertThat(this.c.getTags(), Matchers.empty());
    }

    /**
     * Test setting applications.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test
    public void testSetApplications() throws GeniePreconditionException {
        Assert.assertNotNull(this.c.getApplications());
        Assert.assertTrue(this.c.getApplications().isEmpty());
        final Set<ApplicationEntity> applicationEntities = new HashSet<>();
        final ApplicationEntity one = new ApplicationEntity();
        one.setId("one");
        final ApplicationEntity two = new ApplicationEntity();
        two.setId("two");
        applicationEntities.add(one);
        applicationEntities.add(two);
        this.c.setApplications(applicationEntities);
        Assert.assertEquals(2, this.c.getApplications().size());
        Assert.assertTrue(this.c.getApplications().contains(one));
        Assert.assertTrue(this.c.getApplications().contains(two));
        Assert.assertTrue(one.getCommands().contains(this.c));
        Assert.assertTrue(two.getCommands().contains(this.c));

        applicationEntities.clear();
        applicationEntities.add(two);
        this.c.setApplications(applicationEntities);
        Assert.assertEquals(1, this.c.getApplications().size());
        Assert.assertTrue(this.c.getApplications().contains(two));
        Assert.assertFalse(one.getCommands().contains(this.c));
        Assert.assertTrue(two.getCommands().contains(this.c));
        this.c.setApplications(null);
        Assert.assertTrue(this.c.getApplications().isEmpty());
        Assert.assertTrue(one.getCommands().isEmpty());
        Assert.assertTrue(two.getCommands().isEmpty());
    }

    /**
     * Test setting the clusters.
     */
    @Test
    public void testSetClusters() {
        Assert.assertNotNull(this.c.getClusters());
        Assert.assertTrue(this.c.getClusters().isEmpty());
        final Set<ClusterEntity> clusterEntities = new HashSet<>();
        clusterEntities.add(new ClusterEntity());
        this.c.setClusters(clusterEntities);
        Assert.assertEquals(clusterEntities, this.c.getClusters());

        this.c.setClusters(null);
        Assert.assertThat(this.c.getClusters(), Matchers.empty());
    }

    /**
     * Test to make sure we can set the jobs for the cluster.
     */
    @Test
    public void canSetJobs() {
        final CommandEntity entity = new CommandEntity();
        final Set<JobEntity> jobEntities = Sets.newHashSet(new JobEntity(), new JobEntity());
        entity.setJobs(jobEntities);
        Assert.assertThat(entity.getJobs(), Matchers.is(jobEntities));

        entity.setJobs(null);
        Assert.assertThat(entity.getJobs(), Matchers.empty());
    }

    /**
     * Test to make sure we can add a job to the set of jobs for the cluster.
     *
     * @throws GeniePreconditionException for any error
     */
    @Test
    public void canAddJob() throws GeniePreconditionException {
        final CommandEntity entity = new CommandEntity();
        final JobEntity job = new JobEntity();
        job.setId(UUID.randomUUID().toString());
        entity.addJob(job);
        Assert.assertThat(entity.getJobs(), Matchers.contains(job));

        Assert.assertThat(entity.getJobs().size(), Matchers.is(1));
        entity.addJob(null);
        Assert.assertThat(entity.getJobs().size(), Matchers.is(1));
    }

    /**
     * Make sure we can get a valid Command DTO.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetDTO() throws GenieException {
        final CommandEntity entity = new CommandEntity();
        final String id = UUID.randomUUID().toString();
        entity.setId(id);
        final String name = UUID.randomUUID().toString();
        entity.setName(name);
        final String user = UUID.randomUUID().toString();
        entity.setUser(user);
        final String version = UUID.randomUUID().toString();
        entity.setVersion(version);
        final String description = UUID.randomUUID().toString();
        entity.setDescription(description);
        final Date created = entity.getCreated();
        final Date updated = entity.getUpdated();
        entity.setStatus(CommandStatus.DEPRECATED);
        final Set<String> tags = Sets.newHashSet(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        entity.setCommandTags(tags);
        final Set<String> configs = Sets.newHashSet(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        entity.setConfigs(configs);
        final String setupFile = UUID.randomUUID().toString();
        entity.setSetupFile(setupFile);
        final String executable = UUID.randomUUID().toString();
        entity.setExecutable(executable);

        final Command command = entity.getDTO();
        Assert.assertThat(command.getId(), Matchers.is(id));
        Assert.assertThat(command.getName(), Matchers.is(name));
        Assert.assertThat(command.getUser(), Matchers.is(user));
        Assert.assertThat(command.getVersion(), Matchers.is(version));
        Assert.assertThat(command.getStatus(), Matchers.is(CommandStatus.DEPRECATED));
        Assert.assertThat(command.getDescription(), Matchers.is(description));
        Assert.assertThat(command.getCreated(), Matchers.is(created));
        Assert.assertThat(command.getUpdated(), Matchers.is(updated));
        Assert.assertThat(command.getExecutable(), Matchers.is(executable));
        Assert.assertThat(command.getTags(), Matchers.is(tags));
        Assert.assertThat(command.getSetupFile(), Matchers.is(setupFile));
        Assert.assertThat(command.getConfigs(), Matchers.is(configs));
    }
}