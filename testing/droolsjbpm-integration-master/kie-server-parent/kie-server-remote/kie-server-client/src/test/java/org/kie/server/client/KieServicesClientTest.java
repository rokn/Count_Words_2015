/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.client;

import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.impl.KieServicesClientImpl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;

public class KieServicesClientTest extends BaseKieServicesClientTest {

    @Test
    public void testGetServerInfo() {
        stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                        "  <kie-server-info>\n" +
                                        "    <version>1.2.3</version>\n" +
                                        "  </kie-server-info>\n" +
                                        "</response>")));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1.2.3", response.getResult().getVersion());
    }

    @Test
    public void testListContainers() {
        stubFor(get(urlEqualTo("/containers"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody("<response type=\"SUCCESS\" msg=\"List of created containers\">\n" +
                                        "  <kie-containers>\n" +
                                        "    <kie-container container-id=\"kjar1\" status=\"FAILED\"/>\n" +
                                        "    <kie-container container-id=\"kjar2\" status=\"FAILED\"/>" +
                                        "  </kie-containers>" +
                                        "</response>")));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        assertSuccess(response);
        assertEquals("Number of listed containers", 2, response.getResult().getContainers().size());
    }

    @Test
    public void testCreateContainer() {
        stubFor(put(urlEqualTo("/containers/kie1"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Container successfully deployed\">\n" +
                                "  <kie-container container-id=\"kie1\" status=\"STARTED\">\n" +
                                "    <release-id>\n" +
                                "      <group-id>org.kie.server.testing</group-id>\n" +
                                "      <artifact-id>kjar2</artifact-id>\n" +
                                "      <version>1.0-SNAPSHOT</version>\n" +
                                "    </release-id>\n" +
                                "    <resolved-release-id>\n" +
                                "      <group-id>org.kie.server.testing</group-id>\n" +
                                "      <artifact-id>kjar2</artifact-id>\n" +
                                "      <version>1.0-SNAPSHOT</version>\n" +
                                "    </resolved-release-id>\n" +
                                "  </kie-container>\n" +
                                "</response>")));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "kjar2", "1.0-SNAPSHOT");
        KieContainerResource resource = new KieContainerResource("kie1", releaseId);
        ServiceResponse<KieContainerResource> response = client.createContainer("kie1", resource);
        assertSuccess(response);
        KieContainerResource container = response.getResult();
        assertEquals("Container id", "kie1", container.getContainerId());
        assertEquals("Release id", releaseId, container.getReleaseId());
        assertEquals("Resolved release Id", releaseId, container.getResolvedReleaseId());
    }

    // TODO create more tests for other operations

    private void assertSuccess(ServiceResponse<?> response) {
        assertEquals("Response type", ServiceResponse.ResponseType.SUCCESS, response.getType());
    }
}
