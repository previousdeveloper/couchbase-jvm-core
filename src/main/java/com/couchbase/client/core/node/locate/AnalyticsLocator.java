/*
 * Copyright (c) 2017 Couchbase, Inc.
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
package com.couchbase.client.core.node.locate;

import com.couchbase.client.core.node.Node;
import com.couchbase.client.core.service.ServiceType;

/**
 * Round robin node locator for Analytics.
 *
 * @author Michael Nitschinger
 * @since 1.4.3
 */
public class AnalyticsLocator extends QueryLocator {

    public AnalyticsLocator() {
        super();
    }

    AnalyticsLocator(long initialValue) {
        super(initialValue);
    }

    @Override
    protected boolean checkNode(Node node) {
        return node.serviceEnabled(ServiceType.ANALYTICS);
    }

}