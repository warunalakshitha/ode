/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.axis2.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.wsdl.Messages;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This encoder applies urlReplacement as defined by the <a href='http://www.w3.org/TR/wsdl#_http:urlReplacement'>WSDL specification</a>.
 * <br/>
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class UrlReplacementTransformer {

    private static final org.apache.ode.utils.wsdl.Messages msgs = Messages.getMessages(Messages.class);
    private static final org.apache.ode.axis2.httpbinding.Messages httpMsgs = org.apache.ode.axis2.httpbinding.Messages.getMessages(org.apache.ode.axis2.httpbinding.Messages.class);

    protected Set<String> partNames;

    public UrlReplacementTransformer() {
        partNames = new HashSet<String>();
    }

    public UrlReplacementTransformer(Set<String> partNames) {
        this.partNames = partNames;
    }

    /**
     * @param baseUri - the base uri template containing part names enclosed within single curly braces
     * @param values  - a map<String, Object>, the key is a part name (without curly braces), the value the replacement value for the part name.
     * @return the encoded uri
     * @throws java.lang.IllegalArgumentException
     *          if a part (key) is missing in the map, if a replacement value is null in the map, if a part pattern is not found in the uri template, if a part pattern is found more than once
     */
    public String transform(String baseUri, Map<String, Element> values) {
        // each part must be given a replacement value
        if (values.keySet().size() != partNames.size() || !values.keySet().containsAll(partNames)) {
            Collection<String> disjunction = CollectionUtils.disjunction(partNames, values.keySet());
            throw new IllegalArgumentException(msgs.msgMissingReplacementValuesFor(disjunction));
        }

        // the list containing the final split result
        List<String> result = new ArrayList<String>();

        // initial value
        result.add(baseUri);

        // replace each part exactly once
        for (Map.Entry<String, Element> e : values.entrySet()) {

            String partPattern;
            String replacementValue;
            {
                String partName = e.getKey();
                partPattern = "\\(" + partName + "\\)";
                Element value = e.getValue();
                replacementValue = DOMUtils.isEmptyElement(value) ? "" : DOMUtils.getTextContent(value);
                if (replacementValue == null) {
                    throw new IllegalArgumentException(httpMsgs.msgSimpleTypeExpected(partName));
                }
            }
            replace(result, partPattern, replacementValue);
        }

        // join all the array elements to form the final url
        StringBuilder sb = new StringBuilder(128);
        for (String aResult : result) sb.append(aResult);
        return sb.toString();
    }

    private void replace(List<String> result, String partPattern, String replacementValue) {
        // !!!  i=i+2      replacement values will be skipped,
        // so replaced values do not trigger additional matches
        for (int i = 0; i < result.size(); i = i + 2) {
            String segment = result.get(i);
            // use a negative limit, so empty strings are not discarded
            String[] matches = segment.split(partPattern, -1);
            if (matches.length == 1) {
                if (i == result.size() - 1) {
                    // last segment, no more string to test
                    throw new IllegalArgumentException(httpMsgs.msgInvalidURIPattern());
                } else {
                    continue;
                }
            } else if (matches.length > 2) {
                throw new IllegalArgumentException(httpMsgs.msgInvalidURIPattern());
            } else {
                // if exactly one match...

                // remove the matching segment
                result.remove(i);
                // replace it with the replacement value
                result.add(i, matches[0]);
                result.add(i + 1, replacementValue);
                result.add(i + 2, matches[1]);

                // pattern found and replaced, we're done for this pattern
                // move on to the next part
                break;
            }
        }
    }

}
