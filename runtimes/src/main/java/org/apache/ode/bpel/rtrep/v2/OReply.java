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
package org.apache.ode.bpel.rtrep.v2;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

/**
 * Compiled representation of the BPEL <code>&lt;reply&gt;</code> activity.
 */
public class OReply extends OActivity {
  
    static final long serialVersionUID = -1L  ;

    /** Is this a Fault reply? */
    public boolean isFaultReply;

    /** The type of the fault (if isFaultReply). */
    public QName fault;

    public OPartnerLink partnerLink;
    public Operation operation;
    public OScope.Variable variable;

    /** Correlation sets initialized. */
    public final List<OScope.CorrelationSet> initCorrelations = new ArrayList<OScope.CorrelationSet>();

    /** Correlation sets asserted. */
    public final List<OScope.CorrelationSet> assertCorrelations = new ArrayList<OScope.CorrelationSet>();

    /** OASIS modification - Message Exchange Id. */
    public String messageExchangeId = "";

    public OReply(OProcess owner, OActivity parent) {
        super(owner, parent);
    }
}