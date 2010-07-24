/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.rtsystem;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.ErrorAttributes;
import org.eclipse.ptp.core.elements.attributes.FilterAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MessageAttributes.Level;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeConnectedStateEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeErrorStateEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeJobChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeMachineChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeMessageEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewJobEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewMachineEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewNodeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewProcessEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewQueueEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNodeChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeProcessChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeQueueChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRMChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveAllEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveJobEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveMachineEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveNodeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveProcessEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveQueueEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRunningStateEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeShutdownStateEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeStartupErrorEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeTerminateJobErrorEvent;
import org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient;
import org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeEventListener;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeErrorStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeMachineChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeMessageEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewJobEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewMachineEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewNodeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewProcessEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewQueueEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeProcessChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeQueueChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRMChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveAllEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveJobEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveMachineEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveNodeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveProcessEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveQueueEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRunningStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeShutdownStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeStartupErrorEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeTerminateJobErrorEvent;
import org.eclipse.ptp.utils.core.RangeSet;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.ULocale;

/*
 * ProxyAttributeDefEvents are formatted as follows:
 * 
 *	EVENT_HEADER NUM_DEFS ATTR_DEF ... ATTR_DEF
 * 
 *	where:
 * 
 *	EVENT_HEADER is the event message header
 *	NUM_DEFS is the number of attribute definitions to follow
 *	ATTR_DEF is an attribute definition of the form:
 * 
 *	NUM_ARGS ID TYPE NAME DESCRIPTION DISPLAY DEFAULT [ADDITIONAL_PARAMS]
 * 
 *	where:
 * 
 *	NUM_ARGS is the number of arguments in the attribute definition
 *	ID is a unique definition ID
 *	TYPE is the type of the attribute. Legal types are:
 *		'BOOLEAN', 'DATE', 'DOUBLE', 'ENUMERATED', 'INTEGER', 'STRING', 'ARRAY'
 *	NAME is the short name of the attribute
 *	DESCRIPTION is the long name of the attribute
 *  DISPLAY is true if the attribute should be displayed in a UI
 *	DEFAULT is the default value of the attribute
 *	ADDITIONAL_PARAMS are optional parameters depending on the attribute type:
 *		BOOLEAN - none
 *		DATE - DATE_STYLE TIME_STYLE LOCALE [MIN MAX]
 *		DOUBLE - [MIN MAX]
 *		ENUMERATED - VAL ... VAL
 *		INTEGER - [MIN MAX]
 *		STRING - none
 *		ARRAY - none
 *	MIN is the minimum allowable value for the attribute
 *	MAX is the maximum allowable value for the attribute
 *	DATE_STYLE is the date format: SHORT, MEDIUM, LONG, or FULL
 *	TIME_STYLE is the time format: SHORT, MEDIUM, LONG, or FULL
 *	LOCALE is the country (see java.lang.Local)
 *	NUM_VALS is the number of enumerated values
 *	VAL is the enumerated value
 * 
 * ProxyNew*Events are formatted as follows:
 * 
 *	EVENT_HEADER PARENT_ID NUM_RANGES ID_RANGE NUM_ATTRS KEY=VALUE ... KEY=VALUE ...
 * 
 *	where:
 * 
 *	EVENT_HEADER is the event message header
 *	PARENT_ID is the model element ID of the parent element
 *	NUM_RANGES is the number of ID_RANGEs to follow
 *	ID_RANGE is a range of model element ID's in RangeSet notation
 *	NUM_ATTRS is the number of attributes to follow
 *	KEY=VALUE are key/value pairs, where KEY is the attribute ID and VALUE is the attribute value
 * 
 * Proxy*ChangeEvents are formatted as follows:
 * 
 *	EVENT_HEADER NUM_RANGES ID_RANGE NUM_ATTRS KEY=VALUE ... KEY=VALUE
 * 
 *	where:
 * 
 *	EVENT_HEADER is the event message header
 *	NUM_RANGES is the number of ID_RANGEs to follow
 *	ID_RANGE is a range of model element ID's in RangeSet notation
 *	NUM_ATTRS is the number of attributes to follow
 *	KEY=VALUE are key/value pairs, where KEY is the attribute ID and VALUE is the new attribute value
 *
 * ProxyRemove*Events (apart from ProxyRemoveAllEvent) are formatted as follows:
 * 
 *	EVENT_HEADER ID_RANGE
 * 
 *	where:
 * 
 *	EVENT_HEADER is the event message header
 *	ID_RANGE is a range of model element ID's in RangeSet notation.
 *
 * The ProxyRemoveAllEvent is formatted as follows:
 * 
 *  EVENT_HEADER
 *  
 *  where:
 * 
 *	EVENT_HEADER is the event message header
 */

public abstract class AbstractProxyRuntimeSystem extends AbstractRuntimeSystem implements IProxyRuntimeEventListener {

	private final static int ATTR_MIN_LEN = 5;
	protected IProxyRuntimeClient proxy = null;
	private final AttributeDefinitionManager attrDefManager;
	private final Map<String, AttributeManager> jobSubs = Collections.synchronizedMap(new HashMap<String, AttributeManager>());

	public AbstractProxyRuntimeSystem(IProxyRuntimeClient proxy, AttributeDefinitionManager manager) {
		this.proxy = proxy;
		this.attrDefManager = manager;
		proxy.addProxyRuntimeEventListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IMonitoringSystem#filterEvents(org.eclipse.ptp.core.elements.IPElement, boolean, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void filterEvents(IPElement element, boolean filterChildren, AttributeManager filterAttributes) throws CoreException {
		try {
			filterAttributes.addAttribute(ElementAttributes.getIdAttributeDefinition().create(element.getID()));
			filterAttributes.addAttribute(FilterAttributes.getFilterChildrenAttributeDefinition().create(filterChildren));
			proxy.filterEvents(filterAttributes.toStringArray());
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
		}		
	}

	/**
	 * Create an attribute manager given an array of key/value strings. Each
	 * key/value string must be in the form "key=value", where "key" is a string
	 * containing at least one character, and "value" is a string containing
	 * zero or more characters. The "key" string cannot contain an '='
	 * character. There is no white space allowed between the end of the "key"
	 * string, the '=', and the start of the "value" string, unless that white
	 * space is part of those strings.
	 * 
	 * @param kvs
	 * @param start
	 * @param end
	 * @return
	 */
	private AttributeManager getAttributeManager(String[] kvs, int start, int end) {
		AttributeManager mgr = new AttributeManager();
		
		for (int i = start; i <= end; i++) {
			String kv = kvs[i];
			int sep = kv.indexOf('=');
			if (sep > 0) {
				try {
					String id = kv.substring(0, sep);
					IAttributeDefinition<?,?,?> attrDef = attrDefManager.getAttributeDefinition(id);
					if (attrDef == null) {
						/*
						 * Treat this as a string attribute. This allows the
						 * proxy to send unsolicited attributes when their type
						 * is not important.
						 */
						attrDef = attrDefManager.createStringAttributeDefinition(id, id, id, true, ""); //$NON-NLS-1$
					}
					String value = ""; //$NON-NLS-1$
					if (sep < kv.length() - 1) {
						value = kv.substring(sep+1);
					}
					IAttribute<?,?,?> attr = attrDef.create(value);
					mgr.addAttribute(attr);
				} catch (IllegalValueException e1) {
					PTPCorePlugin.log(Messages.AbstractProxyRuntimeSystem_14+": "+e1.getMessage()); //$NON-NLS-1$
				}
			}
		}
		
		return mgr;
	}

	/**
	 * @param attrs
	 * @param pos
	 * @return
	 */
	private ElementAttributeManager getElementAttributeManager(String[] attrs, int pos) {
		ElementAttributeManager eMgr = new ElementAttributeManager();
		
		try {
			int numRanges = Integer.parseInt(attrs[pos++]);
			
			for (int i = 0; i < numRanges; i++) {
				if (pos >= attrs.length) {
					return null;					
				}
				
				RangeSet ids = new RangeSet(attrs[pos++]);
				int numAttrs = Integer.parseInt(attrs[pos++]);
				
				int start = pos;
				int end = pos + numAttrs - 1;
				
				if (end >= attrs.length) {
					return null;					
				}
				
				eMgr.setAttributeManager(ids, getAttributeManager(attrs, start, end));
				
				pos = end + 1;
			}
		} catch (NumberFormatException e1) {
			return null;
		}
		
		return eMgr;
	}

	/**
	 * @param attrs
	 * @param pos
	 * @return
	 */
	private String getJobId(String[] attrs, int pos) {
		String jobId = attrs[pos++];
		return jobId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeAttributeDefEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeAttributeDefEvent)
	 */
	public void handleEvent(IProxyRuntimeAttributeDefEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length >= ATTR_MIN_LEN + 2) {
			try {
				int numDefs = Integer.parseInt(attrs[0]);
				
				ArrayList<IAttributeDefinition<?,?,?>> attrDefs = 
					new ArrayList<IAttributeDefinition<?,?,?>>(numDefs);

				int pos = 1;
				
				for (int i = 0; i < numDefs; i++) {
					int numArgs = Integer.parseInt(attrs[pos]);
					
					if (numArgs >= ATTR_MIN_LEN && pos + numArgs < attrs.length) {
						IAttributeDefinition<?,?,?> attrDef = 
							parseAttributeDefinition(attrs, pos + 1, pos + numArgs);	
						if (attrDef != null) {
							attrDefs.add(attrDef);
						}
						pos += numArgs + 1;
					} else {
						fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_0));
						return;
					}
				}
				
				fireRuntimeAttributeDefinitionEvent(new RuntimeAttributeDefinitionEvent(attrDefs.toArray(new IAttributeDefinition[attrDefs.size()])));
			} catch (NumberFormatException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_1));
			}
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_2));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeConnectedStateEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeConnectedStateEvent)
	 */
	public void handleEvent(IProxyRuntimeConnectedStateEvent e) {
		fireRuntimeConnectedStateEvent(new RuntimeConnectedStateEvent());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.IProxyRuntimeEventListener#handleProxyRuntimeErrorStateEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeErrorStateEvent)
	 */
	public void handleEvent(IProxyRuntimeErrorStateEvent e) {
		fireRuntimeErrorStateEvent(new RuntimeErrorStateEvent());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeJobChangeEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeJobChangeEvent)
	 */
	public void handleEvent(IProxyRuntimeJobChangeEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 0);
		if (eMgr != null) {			
			fireRuntimeJobChangeEvent(new RuntimeJobChangeEvent(eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeMachineChangeEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeMachineChangeEvent)
	 */
	public void handleEvent(IProxyRuntimeMachineChangeEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 0);
		if (eMgr != null) {			
			fireRuntimeMachineChangeEvent(new RuntimeMachineChangeEvent(eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeErrorEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeErrorEvent)
	 */
	public void handleEvent(IProxyRuntimeMessageEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length > 0) {
			AttributeManager mgr = getAttributeManager(attrs, 0, attrs.length - 1);
			fireRuntimeMessageEvent(new RuntimeMessageEvent(mgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeNewJobEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewJobEvent)
	 */
	public void handleEvent(IProxyRuntimeNewJobEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 2) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 1);
		
		if (eMgr != null) {			
			/*
			 * Find any job submission attributes and add to the jobs
			 */
			for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
				StringAttribute subIdAttr = entry.getValue().getAttribute(JobAttributes.getSubIdAttributeDefinition());
				if (subIdAttr != null) {
					String subId = subIdAttr.getValueAsString();
					AttributeManager mgr = jobSubs.get(subId);
					if (mgr != null) {
						entry.getValue().addAttributes(mgr.getAttributes());
					}
					jobSubs.remove(subId);
				}
			}
			
			fireRuntimeNewJobEvent(new RuntimeNewJobEvent(attrs[0], eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeNewMachineEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewMachineEvent)
	 */
	public void handleEvent(IProxyRuntimeNewMachineEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 2) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 1);
		if (eMgr != null) {			
			fireRuntimeNewMachineEvent(new RuntimeNewMachineEvent(attrs[0], eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeNewNodeEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewNodeEvent)
	 */
	public void handleEvent(IProxyRuntimeNewNodeEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 2) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 1);
		if (eMgr != null) {			
			fireRuntimeNewNodeEvent(new RuntimeNewNodeEvent(attrs[0], eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeNewProcessEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewProcessEvent)
	 */
	public void handleEvent(IProxyRuntimeNewProcessEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 2) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 1);
		if (eMgr != null) {			
			fireRuntimeNewProcessEvent(new RuntimeNewProcessEvent(attrs[0], eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeNewQueueEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewQueueEvent)
	 */
	public void handleEvent(IProxyRuntimeNewQueueEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 2) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 1);
		if (eMgr != null) {			
			fireRuntimeNewQueueEvent(new RuntimeNewQueueEvent(attrs[0], eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeNodeChangeEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNodeChangeEvent)
	 */
	public void handleEvent(IProxyRuntimeNodeChangeEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 0);
		if (eMgr != null) {			
			fireRuntimeNodeChangeEvent(new RuntimeNodeChangeEvent(eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeProcessChangeEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeProcessChangeEvent)
	 */
	public void handleEvent(IProxyRuntimeProcessChangeEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 1);
		if (eMgr != null) {			
			fireRuntimeProcessChangeEvent(new RuntimeProcessChangeEvent(getJobId(attrs, 0), eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeQueueChangeEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeQueueChangeEvent)
	 */
	public void handleEvent(IProxyRuntimeQueueChangeEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 0);
		if (eMgr != null) {			
			fireRuntimeQueueChangeEvent(new RuntimeQueueChangeEvent(eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.IProxyRuntimeEventListener#handleProxyRuntimeRemoveAllEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveAllEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveAllEvent e) {
		fireRuntimeRemoveAllEvent(new RuntimeRemoveAllEvent());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveJobEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveJobEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveJobEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		fireRuntimeRemoveJobEvent(new RuntimeRemoveJobEvent(new RangeSet(attrs[0])));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveMachineEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveMachineEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveMachineEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		fireRuntimeRemoveMachineEvent(new RuntimeRemoveMachineEvent(new RangeSet(attrs[0])));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveNodeEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveNodeEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveNodeEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		fireRuntimeRemoveNodeEvent(new RuntimeRemoveNodeEvent(new RangeSet(attrs[0])));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveProcessEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveProcessEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveProcessEvent e) {
			String[] attrs = e.getAttributes();
			
			if (attrs.length < 1) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
				return;
			}
			
			fireRuntimeRemoveProcessEvent(new RuntimeRemoveProcessEvent(
					getJobId(attrs, 0), new RangeSet(attrs[1])));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveQueueEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveQueueEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveQueueEvent e) {
			String[] attrs = e.getAttributes();
			
			if (attrs.length < 1) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
				return;
			}
			
			fireRuntimeRemoveQueueEvent(new RuntimeRemoveQueueEvent(new RangeSet(attrs[0])));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeEventListener#handleEvent(org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRMChangeEvent)
	 */
	public void handleEvent(IProxyRuntimeRMChangeEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length < 1) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_4));
			return;
		}
		
		ElementAttributeManager eMgr = getElementAttributeManager(attrs, 0);
		if (eMgr != null) {			
			fireRuntimeRMChangeEvent(new RuntimeRMChangeEvent(eMgr));
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_3));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRunningStateEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRunningStateEvent)
	 */
	public void handleEvent(IProxyRuntimeRunningStateEvent e) {
		fireRuntimeRunningStateEvent(new RuntimeRunningStateEvent());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeShutdownStateEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeShutdownStateEvent)
	 */
	public void handleEvent(IProxyRuntimeShutdownStateEvent e) {
		fireRuntimeShutdownStateEvent(new RuntimeShutdownStateEvent());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeStartupErrorEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeStartupErrorEvent)
	 */
	public void handleEvent(IProxyRuntimeStartupErrorEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length > 0) {
			AttributeManager mgr = getAttributeManager(attrs, 0, attrs.length - 1);
			IntegerAttribute codeAttr = mgr.getAttribute(ErrorAttributes.getCodeAttributeDefinition());
			StringAttribute msgAttr = mgr.getAttribute(ErrorAttributes.getMsgAttributeDefinition());
			if (codeAttr == null || msgAttr == null) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_5));				
			} else {
				fireRuntimeStartupErrorEvent(new RuntimeStartupErrorEvent(codeAttr.getValue(), msgAttr.getValue()));
			}
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_6));				
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeSubmitJobErrorEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeSubmitJobErrorEvent)
	 */
	public void handleEvent(IProxyRuntimeSubmitJobErrorEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length > 0) {
			AttributeManager mgr = getAttributeManager(attrs, 0, attrs.length - 1);
			IntegerAttribute codeAttr = mgr.getAttribute(ErrorAttributes.getCodeAttributeDefinition());
			StringAttribute msgAttr = mgr.getAttribute(ErrorAttributes.getMsgAttributeDefinition());
			StringAttribute jobSubIdAttr = mgr.getAttribute(JobAttributes.getSubIdAttributeDefinition());
			if (codeAttr == null || msgAttr == null || jobSubIdAttr == null) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_7));				
			} else {
				fireRuntimeSubmitJobErrorEvent(new RuntimeSubmitJobErrorEvent(codeAttr.getValue(), msgAttr.getValue(), jobSubIdAttr
						.getValue()));
			}
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_8));				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeTerminateJobErrorEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeTerminateJobErrorEvent)
	 */
	public void handleEvent(IProxyRuntimeTerminateJobErrorEvent e) {
		String[] attrs = e.getAttributes();
		
		if (attrs.length > 0) {
			AttributeManager mgr = getAttributeManager(attrs, 0, attrs.length - 1);
			IntegerAttribute codeAttr = mgr.getAttribute(ErrorAttributes.getCodeAttributeDefinition());
			StringAttribute msgAttr = mgr.getAttribute(ErrorAttributes.getMsgAttributeDefinition());
			StringAttribute jobIdAttr = mgr.getAttribute(JobAttributes.getJobIdAttributeDefinition());
			if (codeAttr == null || msgAttr == null || jobIdAttr == null) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_9));				
			} else {
				fireRuntimeTerminateJobErrorEvent(new RuntimeTerminateJobErrorEvent(codeAttr.getValue(), msgAttr.getValue(),
						jobIdAttr.getValue()));
			}
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_10));				
		}
	}

	/**
	 * Parse and extract an attribute definition.
	 * 
	 * On entry, we know that end < attrs.length and end - start >= ATTR_MIN_LEN
	 * 
	 */
	private IAttributeDefinition<?,?,?> parseAttributeDefinition(String[] attrs, int start, int end) {
		int pos = start;
		IAttributeDefinition<?,?,?> attrDef = null;
		
		String attrId = attrs[pos++];
		String attrType = attrs[pos++];
		String attrName = attrs[pos++];
		String attrDesc = attrs[pos++];
		boolean attrDisplay;
			
		try {
			attrDisplay = Boolean.parseBoolean(attrs[pos++]);
		} catch (NumberFormatException ex) {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_15));
			return null;
		}
		
		String attrDefault = attrs[pos++];
		
		if (attrType.equals("BOOLEAN")) { //$NON-NLS-1$
			try {
				Boolean defVal = Boolean.parseBoolean(attrDefault);
				attrDef = attrDefManager.createBooleanAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal);
			} catch (NumberFormatException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_16));
			}
		} else if (attrType.equals("DATE")) { //$NON-NLS-1$
			if (end - pos > 2) {
				try {
					int dateStyle = toDateStyle(attrs[pos++]);
					int timeStyle = toDateStyle(attrs[pos++]);
					ULocale locale = toLocale(attrs[pos++]);
					
					DateFormat fmt = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
					Date defVal = fmt.parse(attrDefault);
					
					if (end - pos > 1) {
						Date min = fmt.parse(attrs[pos++]);
						Date max = fmt.parse(attrs[pos++]);
						attrDef = attrDefManager.createDateAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal,
								fmt, min, max);
					} else {
						attrDef = attrDefManager
								.createDateAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal, fmt);
					}
				} catch (ParseException ex) {
					fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_17));
				} catch (IllegalValueException ex) {
					fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_18
							+ ex.getMessage()));					
				}
			} else {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_19));
			}
		} else if (attrType.equals("DOUBLE")) { //$NON-NLS-1$
			try {
				Double defVal = Double.parseDouble(attrDefault);
				if (end - pos > 0) {
						Double min = Double.parseDouble(attrs[pos++]);
						Double max = Double.parseDouble(attrs[pos++]);
					attrDef = attrDefManager.createDoubleAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal, min,
							max);
				} else {
					attrDef = attrDefManager.createDoubleAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal);
				}
			} catch (NumberFormatException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_20));
			} catch (IllegalValueException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_18
						+ ex.getMessage()));					
			}
		} else if (attrType.equals("ENUMERATED")) { //$NON-NLS-1$
			ArrayList<String> values = new ArrayList<String>();
			while (pos <= end) {
				values.add(attrs[pos++]);
			}
			try {
				attrDef = attrDefManager.createStringSetAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, attrDefault,
						values.toArray(new String[values.size()]));
			} catch (IllegalValueException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_18
						+ ex.getMessage()));					
			}
		} else if (attrType.equals("INTEGER")) { //$NON-NLS-1$
			try {
				Integer defVal = Integer.parseInt(attrDefault);
				if (end - pos > 0) {
						Integer min = Integer.parseInt(attrs[pos++]);
						Integer max = Integer.parseInt(attrs[pos++]);
					attrDef = attrDefManager.createIntegerAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal, min,
							max);
				} else {
					attrDef = attrDefManager.createIntegerAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal);
				}
			} catch (NumberFormatException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_21));
			} catch (IllegalValueException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_18
						+ ex.getMessage()));					
			}
		} else if (attrType.equals("BIGINTEGER")) { //$NON-NLS-1$
			try {
				BigInteger defVal = new BigInteger(attrDefault);
				if (end - pos > 0) {
						BigInteger min = new BigInteger(attrs[pos++]);
						BigInteger max = new BigInteger(attrs[pos++]);
					attrDef = attrDefManager.createBigIntegerAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal,
							min, max);
				} else {
					attrDef = attrDefManager.createBigIntegerAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, defVal);
				}
			} catch (NumberFormatException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_22));
			} catch (IllegalValueException ex) {
				fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_18
						+ ex.getMessage()));					
			}
		} else if (attrType.equals("STRING")) { //$NON-NLS-1$
			attrDef = attrDefManager.createStringAttributeDefinition(attrId, attrName, attrDesc, attrDisplay, attrDefault);
		} else {
			fireRuntimeMessageEvent(new RuntimeMessageEvent(Level.ERROR, Messages.AbstractProxyRuntimeSystem_23));
		}
		
		return attrDef;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeSystem#shutdown()
	 */
	public abstract void shutdown() throws CoreException;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IMonitoringSystem#startEvents()
	 */
	public void startEvents() throws CoreException {
		try {
			proxy.startEvents();
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeSystem#startup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract void startup(IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IMonitoringSystem#stopEvents()
	 */
	public void stopEvents() throws CoreException {
		try {
			proxy.stopEvents();
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IControlSystem#submitJob(java.lang.String, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void submitJob(String subId, AttributeManager attrMgr) throws CoreException {
		try {
			StringAttribute jobSubAttr = JobAttributes.getSubIdAttributeDefinition().create(subId);
			attrMgr.addAttribute(jobSubAttr);
			jobSubs.put(subId, attrMgr);
			proxy.submitJob(attrMgr.toStringArray());
		} catch(IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
				Messages.AbstractProxyRuntimeSystem_11, null));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IControlSystem#terminateJob(org.eclipse.ptp.core.elements.IPJob)
	 */
	public void terminateJob(IPJob job) throws CoreException {
		if(job == null) {
			PTPCorePlugin.log(Messages.AbstractProxyRuntimeSystem_12);
			return;
		}
		
		try {
			proxy.terminateJob(job.getID());
		} catch(IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
				Messages.AbstractProxyRuntimeSystem_11, null));
		}
	}

	/**
	 * @param val
	 * @return
	 */
	private int toDateStyle(String val) {
		if (val.equals("SHORT")) { //$NON-NLS-1$
			return DateFormat.SHORT;
		} else if (val.equals("MEDIUM")) { //$NON-NLS-1$
			return DateFormat.MEDIUM;
		} else if (val.equals("LONG")) { //$NON-NLS-1$
			return DateFormat.LONG;
		} else if (val.equals("FULL")) { //$NON-NLS-1$
			return DateFormat.FULL;
		} else {
			return DateFormat.DEFAULT;
		}
	}

	/**
	 * @param val
	 * @return
	 */
	private ULocale toLocale(String val) {
		if (val.equals("CANADA")) { //$NON-NLS-1$
			return ULocale.CANADA;
		} else if (val.equals("CHINA")) { //$NON-NLS-1$
			return ULocale.CHINA;
		} else if (val.equals("FRANCE")) { //$NON-NLS-1$
			return ULocale.FRANCE;
		} else if (val.equals("GERMANY")) { //$NON-NLS-1$
			return ULocale.GERMANY;
		} else if (val.equals("ITALY")) { //$NON-NLS-1$
			return ULocale.ITALY;
		} else if (val.equals("JAPAN")) { //$NON-NLS-1$
			return ULocale.JAPAN;
		} else if (val.equals("TAIWAN")) { //$NON-NLS-1$
			return ULocale.TAIWAN;
		} else if (val.equals("UK")) { //$NON-NLS-1$
			return ULocale.UK;
		} else if (val.equals("US")) { //$NON-NLS-1$
			return ULocale.US;
		} else {
			return ULocale.US;
		}
	}
}
