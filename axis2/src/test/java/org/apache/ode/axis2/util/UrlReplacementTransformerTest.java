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

import junit.framework.TestCase;
import org.apache.ode.axis2.util.UrlReplacementTransformer;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class UrlReplacementTransformerTest extends TestCase {

    //<http:operation location="o1/A(part1)B(part2)/(part3)"/>
    private String baseUrl = "o1/A(part1)B(part2)/(part3)";

    public void testRegularCases() {
        String[][] a = new String[][]{
                new String[]{"with alphabetical chars", baseUrl, "o1/AtutuBtiti/toto", "part1", "tutu", "part2", "titi", "part3", "toto"}
                , new String[]{"parts are ends", "(part1)B(part2)/(part3)", "3B14/159", "part1", "3", "part2", "14", "part3", "159"}
                , new String[]{"a single part", "(part1)", "314159", "part1", "314159"}
                , new String[]{"parts surrounded with ()", "o1/A((part1))B((part2))/((part3))", "o1/A(3)B(14)/(159)", "part1", "3", "part2", "14", "part3", "159"}
                , new String[]{"with numeric chars", baseUrl, "o1/A3B14/159", "part1", "3", "part2", "14", "part3", "159"}
                , new String[]{"with empty values", baseUrl, "o1/AB/", "part1", "", "part2", "", "part3", ""}
                , new String[]{"with special chars", baseUrl, "o1/AWhatB$10,000/~!@#$%^&*()_+=-`[]{}|\\.", "part1", "What", "part2", "$10,000", "part3", "~!@#$%^&*()_+=-`[]{}|\\."}
                , new String[]{"with values containing key names", baseUrl, "o1/Avalue_of_part1_is_(part2)_and_should_not_be_replacedBsame_for_part2(part3)/foo", "part1", "value_of_part1_is_(part2)_and_should_not_be_replaced", "part2", "same_for_part2(part3)", "part3", "foo"}
        };

        Document doc = DOMUtils.newDocument();
        for (String[] data : a) {
            // convert into map
            Map<String, Element> parts = new HashMap<String, Element>();
            for (int k = 3; k < data.length; k = k + 2) {
                Element element = doc.createElement(data[k]);
                element.setTextContent(data[k + 1]);
                parts.put(data[k], element);
            }
            UrlReplacementTransformer encoder = new UrlReplacementTransformer(parts.keySet());
            assertEquals(data[0], data[2], encoder.transform(data[1], parts));
        }

    }

    public void testMissingPartPatterns() {
        try {
            Map<String, Element> map = new HashMap<String, Element>();
            map.put("part1", null);
            UrlReplacementTransformer encoder = new UrlReplacementTransformer(map.keySet());
            encoder.transform("", map);
            fail("Exception expected! Missing Part Patterns in URI");
        } catch (IllegalArgumentException e) {
            // expected behavior
        }
    }

    public void testComplexType() {
        Document doc = DOMUtils.newDocument();
        Element element = doc.createElement("part1");
        element.appendChild(doc.createElement("kid"));
        Map<String, Element> m = new HashMap<String, Element>();
        m.put("part1", element);

        UrlReplacementTransformer encoder = new UrlReplacementTransformer(m.keySet());
        try {
            encoder.transform("(part1)", m);
            fail("IllegalArgumentException expected because a complex type is passed.");
        } catch (IllegalArgumentException e) {
            // expected behavior
        }
    }

}
