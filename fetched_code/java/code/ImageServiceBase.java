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

package org.kie.server.services.jbpm.ui;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.svg.SVGImageProcessor;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.server.services.jbpm.ui.img.ImageReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(ImageServiceBase.class);

    private RuntimeDataService dataService;
    private Map<String, ImageReference> imageReferenceMap;

    public ImageServiceBase(RuntimeDataService dataService, Map<String, ImageReference> imageReferenceMap) {
        this.dataService = dataService;
        this.imageReferenceMap = imageReferenceMap;
    }

    private byte[] getProcessImageAsBytes(String containerId, String processId) {

        ProcessDefinition procDef = dataService.getProcessesByDeploymentIdProcessId(containerId, processId);
        if( procDef == null ) {
            throw new IllegalArgumentException("No process found for " + processId + " within container " + containerId);
        }

        // get SVG String
        byte[] imageSVG = imageReferenceMap.get(containerId).getImageContent(processId);
        if( imageSVG == null ) {
            logger.warn("Could not find SVG image file for process '" + processId + "' within container " + containerId);
            return null;
        }

        return imageSVG;
    }

    public String getProcessImage(String containerId, String processId) {
        String imageSVGString = null;
        byte[] imageSVG = getProcessImageAsBytes(containerId, processId);
        if (imageSVG != null) {
            try {
                imageSVGString = new String(imageSVG, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.debug("UnsupportedEncodingException while building process image due to {}", e.getMessage());
            }
        }

        return imageSVGString;
    }

    public String getActiveProcessImage(String containerId, long procInstId) {
        ProcessInstanceDesc instance = dataService.getProcessInstanceById(procInstId);
        if (instance == null) {
            throw new ProcessInstanceNotFoundException("No instance found for process instance id " + procInstId);
        }
        String imageSVGString = null;
        // get SVG String
        byte[] imageSVG = getProcessImageAsBytes(containerId, instance.getProcessId());
        if (imageSVG != null) {
            // find active nodes and modify image
            Collection<NodeInstanceDesc> logs = dataService.getProcessInstanceFullHistory(procInstId, null);
            List<String> active = new ArrayList<String>(2);
            List<String> completed = new ArrayList<String>(logs.size() / 2);
            for (NodeInstanceDesc nodeLog : logs) {
                String nodeId = nodeLog.getNodeId();
                if (NodeInstanceLog.TYPE_ENTER == ((org.jbpm.kie.services.impl.model.NodeInstanceDesc) nodeLog).getType()) {
                    active.add(nodeId);
                } else {
                    completed.add(nodeId);
                }
            }
            ByteArrayInputStream svgStream = new ByteArrayInputStream(imageSVG);

            imageSVGString = SVGImageProcessor.transform(svgStream, completed, active);

            return imageSVGString;
        }
        throw new IllegalArgumentException("No process found for " + instance.getProcessId() + " within container " + containerId);
    }
}
