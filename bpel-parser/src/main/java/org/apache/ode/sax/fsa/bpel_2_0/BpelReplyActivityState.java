/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.ReplyActivity;
import org.apache.ode.bom.impl.nodes.ReplyActivityImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

import java.util.Iterator;

class BpelReplyActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new Factory();

  BpelReplyActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    ReplyActivityImpl rai = new ReplyActivityImpl();
    XmlAttributes atts = se.getAttributes();
    if (atts.hasAtt("portType")) {
      rai.setPortType(se.getNamespaceContext().derefQName(atts.getValue("portType")));
    }
    rai.setPartnerLink(atts.getValue("partnerLink"));
    rai.setOperation(atts.getValue("operation"));
    rai.setVariable(atts.getValue("variable"));
    rai.setMessageExchangeId(atts.getValue("messageExchange"));
    if (atts.hasAtt("faultName")) {
      rai.setFaultName(se.getNamespaceContext().derefQName(atts.getValue("faultName")));
    }
    return rai;
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL_CORRELATIONS) {
      for (Iterator<Correlation> it = ((BpelCorrelationsState)pn).getCorrelations();it.hasNext();) {
        Correlation c = it.next();
        if (c.getPattern() != Correlation.CORRPATTERN_OUT) {
          // force pattern OUT
          getParseContext().parseError(ParseError.WARNING,c,
              "",
              "The \"out\" correlation pattern is the only one that makes sense for <reply>.");
          // TODO: Get real error key.          
          c.setPattern(Correlation.CORRPATTERN_OUT);
        }
        ((ReplyActivity)getActivity()).addCorrelation(c);
      }
    } else {
      super.handleChildCompleted(pn);
    }
  }
  /**
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_REPLY;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelReplyActivityState(se,pc);
    }
  }
}
