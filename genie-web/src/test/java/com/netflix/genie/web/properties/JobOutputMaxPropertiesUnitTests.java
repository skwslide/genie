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
package com.netflix.genie.web.properties;

import com.netflix.genie.test.categories.UnitTest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for JobOutputMaxProperties.
 *
 * @author tgianos
 * @since 3.0.0
 */
@Category(UnitTest.class)
public class JobOutputMaxPropertiesUnitTests {

    private JobOutputMaxProperties properties;

    /**
     * Setup for the tests.
     */
    @Before
    public void setup() {
        this.properties = new JobOutputMaxProperties();
    }

    /**
     * Make sure the default properties are set.
     */
    @Test
    public void canConstruct() {
        Assert.assertThat(this.properties.getStdOut(), Matchers.is(8589934592L));
        Assert.assertThat(this.properties.getStdErr(), Matchers.is(8589934592L));
    }

    /**
     * Make sure can set and get the std out variable.
     */
    @Test
    public void canSetStdOut() {
        final long newStdOut = 180234L;
        this.properties.setStdOut(newStdOut);
        Assert.assertThat(this.properties.getStdOut(), Matchers.is(newStdOut));
    }

    /**
     * Make sure can set and get the std err variable.
     */
    @Test
    public void canSetStdErr() {
        final long newStdErr = 180234L;
        this.properties.setStdErr(newStdErr);
        Assert.assertThat(this.properties.getStdErr(), Matchers.is(newStdErr));
    }
}
