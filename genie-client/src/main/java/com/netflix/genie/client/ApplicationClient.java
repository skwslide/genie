/*
 *
 *  Copyright 2016 Netflix, Inc.
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
package com.netflix.genie.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.netflix.genie.client.apis.ApplicationService;
import com.netflix.genie.client.exceptions.GenieClientException;
import com.netflix.genie.client.security.SecurityInterceptor;
import com.netflix.genie.common.dto.Application;
import com.netflix.genie.common.dto.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Client library for the Application Service.
 *
 * @author amsharma
 * @since 3.0.0
 */
@Slf4j
public class ApplicationClient extends BaseGenieClient {

    private ApplicationService applicationService;

    /**
     * Constructor.
     *
     * @param url The url of the Genie Service.
     * @param securityInterceptor An implementation of the Security Interceptor.
     *
     * @throws GenieClientException If there is any problem.
     */
    public ApplicationClient(
        final String url,
        final SecurityInterceptor securityInterceptor
    ) throws GenieClientException {
        super(url, securityInterceptor);
        applicationService = retrofit.create(ApplicationService.class);
    }

    /**
     * Constructor that takes only the URL.
     *
     * @param url The url of the Genie Service.
     * @throws GenieClientException If there is any problem.
     */
    // TODO Can we get rid of one constructor in either BaseGenieClient or JobClient.
    public ApplicationClient(
        final String url
    ) throws GenieClientException {
        super(url, null);
        applicationService = retrofit.create(ApplicationService.class);
    }

    /******************* CRUD Methods   ***************************/

    /**
     * Create a application ing genie.
     *
     * @param application A application object.
     *
     * @return The id of the application created.
     *
     * @throws GenieClientException For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public String createApplication(
        final Application application
    ) throws IOException, GenieClientException {
        if (application == null) {
            throw new GenieClientException("Application cannot be null.");
        }
        return getIdFromLocation(applicationService.createApplication(application).execute().headers().get("location"));
    }

    /**
     * Method to get a list of all the applications.
     *
     * @return A list of applications.
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public List<Application> getApplications() throws IOException, GenieClientException {
        return this.getApplications(null, null, null, null, null);
    }

    /**
     * Method to get a list of all the applications from Genie for the query parameters specified.
     *
     * @param name The name of the commands.
     * @param user The user who created the command.
     * @param statusList The list of Command statuses.
     * @param tagList The list of tags.
     * @param type The type of the application.
     *
     * @return A list of applications.
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public List<Application> getApplications(
        final String name,
        final String user,
        final List<String> statusList,
        final List<String> tagList,
        final String type
    ) throws IOException, GenieClientException {

        final List<Application> applicationList = new ArrayList<>();
        final JsonNode jnode =  applicationService.getApplications(
            name,
            user,
            statusList,
            tagList,
            type
        ).execute().body()
            .get("_embedded");
        if (jnode != null) {
            for (final JsonNode objNode : jnode.get("applicationList")) {
                final Application application  = mapper.treeToValue(objNode, Application.class);
                applicationList.add(application);
            }
        }
        return applicationList;
    }

    /**
     * Method to get a Application from Genie.
     *
     * @param applicationId The id of the application to get.
     * @return The application details.
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public Application getApplication(
        final String applicationId
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }
        return applicationService.getApplication(applicationId).execute().body();
    }

    /**
     * Method to delete a application from Genie.
     *
     * @param applicationId The id of the application.
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void deleteApplication(final String applicationId) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }
        applicationService.deleteApplication(applicationId).execute();
    }

    /**
     * Method to delete all applications from Genie.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void deleteAllApplications() throws IOException, GenieClientException {
        applicationService.deleteAllApplications().execute();
    }

    /**
     * Method to patch a application using json patch instructions.
     *
     * @param applicationId The id of the application.
     * @param patch The patch object specifying all the instructions.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void patchApplication(final String applicationId, final JsonPatch patch)
        throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (patch == null) {
            throw new GenieClientException("Patch cannot be null");
        }

        applicationService.patchApplication(applicationId, patch).execute();
    }

    /**
     * Method to updated a application.
     *
     * @param applicationId The id of the application.
     * @param application The updated application object to use.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void updateApplication(final String applicationId, final Application application)
        throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (application == null) {
            throw new GenieClientException("Patch cannot be null");
        }

        applicationService.updateApplication(applicationId, application).execute();
    }

    /**
     * Method to get all the commands for an application.
     *
     * @param applicationId The id of the application.
     *
     * @return The set of commands for the applications.
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public List<Command> getCommandsForApplication(final String applicationId)
        throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        return applicationService.getCommandsForApplication(applicationId).execute().body();
    }

    /****************** Methods to manipulate configs for a application   *********************/

    /**
     * Method to get all the configs for a application.
     *
     * @param applicationId The id of the application.
     *
     * @return The set of configs for the application.
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public Set<String> getConfigsForApplication(final String applicationId) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        return applicationService.getConfigsForApplication(applicationId).execute().body();
    }

    /**
     * Method to add configs to a application.
     *
     * @param applicationId The id of the application.
     * @param configs The set of configs to add.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void addConfigsToApplication(
        final String applicationId, final Set<String> configs
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (configs == null || configs.isEmpty()) {
            throw new GenieClientException("Configs cannot be null or empty");
        }

        applicationService.addConfigsToApplication(applicationId, configs).execute();
    }

    /**
     * Method to update configs for a application.
     *
     * @param applicationId The id of the application.
     * @param configs The set of configs to add.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void updateConfigsForApplication(
        final String applicationId, final Set<String> configs
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (configs == null || configs.isEmpty()) {
            throw new GenieClientException("Configs cannot be null or empty");
        }

        applicationService.updateConfigsForApplication(applicationId, configs).execute();
    }

    /**
     * Remove all configs for this application.
     *
     * @param applicationId The id of the application.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void removeAllConfigsForApplication(
        final String applicationId
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        applicationService.removeAllConfigsForApplication(applicationId).execute();
    }

    /****************** Methods to manipulate dependencies for a application   *********************/

    /**
     * Method to get all the dependency files for an application.
     *
     * @param applicationId The id of the application.
     *
     * @return The set of dependencies for the application.
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public Set<String> getDependenciesForApplication(final String applicationId) throws IOException,
        GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        return applicationService.getDependenciesForApplication(applicationId).execute().body();
    }

    /**
     * Method to add dependencies to a application.
     *
     * @param applicationId The id of the application.
     * @param dependencies The set of dependencies to add.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void addDependenciesToApplication(
        final String applicationId, final Set<String> dependencies
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (dependencies == null || dependencies.isEmpty()) {
            throw new GenieClientException("Dependencies cannot be null or empty");
        }

        applicationService.addDependenciesToApplication(applicationId, dependencies).execute();
    }

    /**
     * Method to update dependencies for a application.
     *
     * @param applicationId The id of the application.
     * @param dependencies The set of dependencies to add.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void updateDependenciesForApplication(
        final String applicationId, final Set<String> dependencies
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (dependencies == null || dependencies.isEmpty()) {
            throw new GenieClientException("Dependencies cannot be null or empty");
        }

        applicationService.updateDependenciesForApplication(applicationId, dependencies).execute();
    }

    /**
     * Remove all dependencies for this application.
     *
     * @param applicationId The id of the application.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void removeAllDependenciesForApplication(
        final String applicationId
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        applicationService.removeAllDependenciesForApplication(applicationId).execute();
    }

    /****************** Methods to manipulate tags for a application   *********************/

    /**
     * Method to get all the tags for a application.
     *
     * @param applicationId The id of the application.
     *
     * @return The set of configs for the application.
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public Set<String> getTagsForApplication(final String applicationId) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        return applicationService.getTagsForApplication(applicationId).execute().body();
    }

    /**
     * Method to add tags to a application.
     *
     * @param applicationId The id of the application.
     * @param tags The set of tags to add.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void addTagsToApplication(
        final String applicationId, final Set<String> tags
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (tags == null || tags.isEmpty()) {
            throw new GenieClientException("Tags cannot be null or empty");
        }

        applicationService.addTagsToApplication(applicationId, tags).execute();
    }

    /**
     * Method to update tags for a application.
     *
     * @param applicationId The id of the application.
     * @param tags The set of tags to add.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void updateTagsForApplication(
        final String applicationId, final Set<String> tags
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (tags == null || tags.isEmpty()) {
            throw new GenieClientException("Tags cannot be null or empty");
        }

        applicationService.updateTagsForApplication(applicationId, tags).execute();
    }

    /**
     * Remove a tag from a application.
     *
     * @param applicationId The id of the application.
     * @param tag The tag to remove.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void removeTagFromApplication(
        final String applicationId,
        final String tag
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        if (StringUtils.isEmpty(tag)) {
            throw new GenieClientException("Missing required parameter: tag.");
        }

        applicationService.removeTagForApplication(applicationId, tag).execute();
    }

    /**
     * Remove all tags for this application.
     *
     * @param applicationId The id of the application.
     *
     * @throws GenieClientException       For any other error.
     * @throws IOException If the response received is not 2xx.
     */
    public void removeAllTagsForApplication(
        final String applicationId
    ) throws IOException, GenieClientException {
        if (StringUtils.isEmpty(applicationId)) {
            throw new GenieClientException("Missing required parameter: applicationId.");
        }

        applicationService.removeAllTagsForApplication(applicationId).execute();
    }
}
