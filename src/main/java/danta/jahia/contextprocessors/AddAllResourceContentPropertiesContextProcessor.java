/**
 * Danta Jahia Bundle
 * (danta.jahia)
 *
 * Copyright (C) 2017 Tikal Technologies, Inc. All rights reserved.
 *
 * Licensed under GNU Affero General Public License, Version v3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied;
 * without even the implied warranty of MERCHANTABILITY.
 * See the License for more details.
 */

package danta.jahia.contextprocessors;

import com.google.common.collect.Sets;
import danta.api.ExecutionContext;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import danta.jahia.templating.TemplateContentModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;
import static danta.core.Constants.XK_CONTENT_ID_CP;
import static danta.jahia.Constants.JAHIA_RESOURCE;
import static danta.jahia.Constants.JCR_NODE_UUID;
import static danta.jahia.util.PropertyUtils.propsToMap;

/**
 * The context processor for adding resource content properties to content model
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2017-08-08
 */
@Component
@Service
public class AddAllResourceContentPropertiesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModel> {

    private static Logger LOG = LoggerFactory.getLogger(AddAllResourceContentPropertiesContextProcessor.class);

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(CONTENT_CATEGORY);
    }

    @Override
    public int priority() {
        // This processor must be one of the first processors executed.
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModel contentModel)throws ProcessException {
        try {
            Map<String, Object> content = new HashMap<>();
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);

            if (resource != null) {
                Node node = resource.getNode();
                if (node != null) {
                    String componentContentId = DigestUtils.md5Hex(resource.getPath());
                    content.put(XK_CONTENT_ID_CP, componentContentId);
                    Map<String, Object> propsMap = propsToMap(node.getProperties());
                    for (String propertyName : propsMap.keySet()) {
                        if (!StringUtils.startsWithAny(propertyName, RESERVED_SYSTEM_NAME_PREFIXES)) {
                            content.put(propertyName, propsMap.get(propertyName));
                        }
                    }
                    content.put(ID, DigestUtils.md5Hex(resource.getPath()));
                    content.put(JCR_NODE_UUID,node.getIdentifier());
                } else {
                    // the resource doesn't exist so we clear the content
                    content.clear();
                }
            } else {
                content.put(XK_CONTENT_ID_CP, "_NONE");
            }

            content.put(PATH, resource.getPath());
            content.put(NAME, resource.getNode().getName());
            contentModel.set(RESOURCE_CONTENT_KEY, content);

        } catch (Exception e) {
            LOG.error("LAYERX Exception: "+e.getMessage(),e);
            throw new ProcessException(e);
        }
    }
}