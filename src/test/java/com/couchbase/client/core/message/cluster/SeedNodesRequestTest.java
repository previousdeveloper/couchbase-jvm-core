/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.core.message.cluster;

import com.couchbase.client.core.config.ConfigurationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifies the correct functionality of a {@link SeedNodesRequest}.
 *
 * @author Michael Nitschinger
 * @since 1.0
 */
public class SeedNodesRequestTest {

    @Test
    public void shouldConstructWithDefaultHostname() {
        SeedNodesRequest request = new SeedNodesRequest();
        assertEquals(1, request.nodes().size());
        assertTrue(request.nodes().contains("127.0.0.1"));
    }

    @Test
    public void shouldConstructWithCustomHostname() {
        SeedNodesRequest request = new SeedNodesRequest("127.0.0.1");
        assertEquals(1, request.nodes().size());
        assertTrue(request.nodes().contains("127.0.0.1"));
    }

    @Test(expected = ConfigurationException.class)
    public void shouldFailOnNullHostname() {
        List<String> nodes = null;
        new SeedNodesRequest(nodes);
    }

    @Test
    public void shouldDeduplicateHosts() {
        SeedNodesRequest request = new SeedNodesRequest("127.0.0.1", "127.0.0.1");
        assertEquals(1, request.nodes().size());
    }

    @Test(expected = ConfigurationException.class)
    public void shouldFailOnEmptyHostname() {
        new SeedNodesRequest(new ArrayList<String>());
    }

}