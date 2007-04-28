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
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IIntegerAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.rtsystem.events.RuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeShutdownStateEvent;
import org.eclipse.ptp.rtsystem.proxy.IProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeShutdownStateEvent;

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
 *	NUM_ARGS ID TYPE NAME DESCRIPTION DEFAULT [ADDITIONAL_PARAMS]
 * 
 *	where:
 * 
 *	NUM_ARGS is the number of arguments in the attribute definition
 *	ID is a unique definition ID
 *	TYPE is the type of the attribute. Legal types are:
 *		'BOOLEAN', 'DATE', 'DOUBLE', 'ENUMERATED', 'INTEGER', 'STRING', 'ARRAY'
 *	NAME is the short name of the attribute
 *	DESCRIPTION is the long name of the attribute
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
 */

public abstract class AbstractProxyRuntimeSystem extends AbstractRuntimeSystem implements IProxyRuntimeEventListener {

	private final static int ATTR_MIN_LEN = 5;
	protected IProxyRuntimeClient proxy = null;
	private AttributeDefinitionManager attrDefManager;

	public AbstractProxyRuntimeSystem(IProxyRuntimeClient proxy, AttributeDefinitionManager manager) {
		this.proxy = proxy;
		this.attrDefManager = manager;
		proxy.addProxyRuntimeEventListener(this);
	}
	
	public void handleProxyRuntimeAttributeDefEvent(IProxyRuntimeAttributeDefEvent e) {
		String[] args = e.getArguments();
		
		if (args.length >= ATTR_MIN_LEN + 2) {
			try {
				int numDefs = Integer.parseInt(args[0]);
				
				ArrayList<IAttributeDefinition> attrDefs = new ArrayList<IAttributeDefinition>(numDefs);

				int pos = 1;
				
				for (int i = 0; i < numDefs; i++) {
					int numArgs = Integer.parseInt(args[pos]);
					
					if (numArgs >= ATTR_MIN_LEN && pos + numArgs < args.length) {
						IAttributeDefinition attrDef = parseAttributeDefinition(args, pos + 1, pos + numArgs);	
						if (attrDef != null) {
							attrDefs.add(attrDef);
						}
						pos += numArgs + 1;
					} else {
						fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: bad arg count"));
						return;
					}
				}
				
				fireRuntimeAttributeDefinitionEvent(new RuntimeAttributeDefinitionEvent(attrDefs.toArray(new IAttributeDefinition[attrDefs.size()])));
			} catch (NumberFormatException ex) {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not convert arg to integer"));
			}
		} else {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: not enough arguments"));
		}
	}

	public void handleProxyRuntimeErrorEvent(IProxyRuntimeErrorEvent e) {
		fireRuntimeErrorEvent(new RuntimeErrorEvent(e.getDescription()));
	}

	public void handleProxyRuntimeJobChangeEvent(IProxyRuntimeJobChangeEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			ElementAttributeManager eMgr = getElementAttributeManager(args, 0);
			if (eMgr != null) {			
				fireRuntimeJobChangeEvent(new RuntimeJobChangeEvent(eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}

	public void handleProxyRuntimeMachineChangeEvent(IProxyRuntimeMachineChangeEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			ElementAttributeManager eMgr = getElementAttributeManager(args, 0);
			if (eMgr != null) {			
				fireRuntimeMachineChangeEvent(new RuntimeMachineChangeEvent(eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}
	
	public void handleProxyRuntimeNewJobEvent(IProxyRuntimeNewJobEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 2) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			int parentId = Integer.parseInt(args[0]);
			
			ElementAttributeManager eMgr = getElementAttributeManager(args, 1);
			if (eMgr != null) {			
				fireRuntimeNewJobEvent(new RuntimeNewJobEvent(parentId, eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}
	
	public void handleProxyRuntimeNewMachineEvent(IProxyRuntimeNewMachineEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 2) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			int parentId = Integer.parseInt(args[0]);
			
			ElementAttributeManager eMgr = getElementAttributeManager(args, 1);
			if (eMgr != null) {			
				fireRuntimeNewMachineEvent(new RuntimeNewMachineEvent(parentId, eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}
	
	public void handleProxyRuntimeNewNodeEvent(IProxyRuntimeNewNodeEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 2) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			int parentId = Integer.parseInt(args[0]);
			
			ElementAttributeManager eMgr = getElementAttributeManager(args, 1);
			if (eMgr != null) {			
				fireRuntimeNewNodeEvent(new RuntimeNewNodeEvent(parentId, eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}
	
	public void handleProxyRuntimeNewProcessEvent(IProxyRuntimeNewProcessEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 2) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			int parentId = Integer.parseInt(args[0]);
			
			ElementAttributeManager eMgr = getElementAttributeManager(args, 1);
			if (eMgr != null) {			
				fireRuntimeNewProcessEvent(new RuntimeNewProcessEvent(parentId, eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}

	public void handleProxyRuntimeNewQueueEvent(IProxyRuntimeNewQueueEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 2) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			int parentId = Integer.parseInt(args[0]);
			
			ElementAttributeManager eMgr = getElementAttributeManager(args, 1);
			if (eMgr != null) {			
				fireRuntimeNewQueueEvent(new RuntimeNewQueueEvent(parentId, eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}
	
	public void handleProxyRuntimeNodeChangeEvent(IProxyRuntimeNodeChangeEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			ElementAttributeManager eMgr = getElementAttributeManager(args, 0);
			if (eMgr != null) {			
				fireRuntimeNodeChangeEvent(new RuntimeNodeChangeEvent(eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}

	public void handleProxyRuntimeProcessChangeEvent(IProxyRuntimeProcessChangeEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			ElementAttributeManager eMgr = getElementAttributeManager(args, 0);
			if (eMgr != null) {			
				fireRuntimeProcessChangeEvent(new RuntimeProcessChangeEvent(eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}
	
	public void handleProxyRuntimeQueueChangeEvent(IProxyRuntimeQueueChangeEvent e) {
		String[] args = e.getArguments();
		
		if (args.length < 1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: not enough arguments"));
			return;
		}
		
		try {
			ElementAttributeManager eMgr = getElementAttributeManager(args, 0);
			if (eMgr != null) {			
				fireRuntimeQueueChangeEvent(new RuntimeQueueChangeEvent(eMgr));
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: could not parse message"));				
			}
		} catch (NumberFormatException e1) {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("AbstractProxyRuntimeSystem: invalid parent ID"));				
		}
	}

	public void handleProxyRuntimeConnectedStateEvent(IProxyRuntimeConnectedStateEvent e) {
		fireRuntimeConnectedStateEvent(new RuntimeConnectedStateEvent());
	}

	public void handleProxyRuntimeRunningStateEvent(IProxyRuntimeRunningStateEvent e) {
		fireRuntimeRunningStateEvent(new RuntimeRunningStateEvent());
	}

	public void handleProxyRuntimeShutdownStateEvent(IProxyRuntimeShutdownStateEvent e) {
		fireRuntimeShutdownStateEvent(new RuntimeShutdownStateEvent());
	}

	public void shutdown() {
		proxy.shutdown();
	}
	
	public void startup() throws CoreException {
		proxy.startup();
	}
	
	public void submitJob(int jobSubId, AttributeManager attrMgr) throws CoreException {
		try {
			/*
			 * Add the job submission ID to the attributes. This is done here to force the
			 * use of the ID.
			 */
			IIntegerAttribute jobSubAttr = JobAttributes.getSubIdAttributeDefinition().create(jobSubId);
			attrMgr.setAttribute(jobSubAttr);
			proxy.submitJob(attrMgr.toStringArray());
		} catch(IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
				"Control system is shut down, proxy exception.  The proxy may have crashed or been killed.", null));
		} catch (IllegalValueException e) {
		}
	}

	public void terminateJob(IPJob job) throws CoreException {
		if(job == null) {
			System.err.println("ERROR: Tried to abort a null job.");
			return;
		}
		
		int jobID = job.getID();

		if(jobID >= 0) {
			System.out.println("LSFControlSystem: abortJob() with name "+job.toString()+" and ID "+jobID);
			try {
				proxy.terminateJob(jobID);
			} catch(IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
					"Control system is shut down, proxy exception.  The proxy may have crashed or been killed.", null));
			}
		}
		else {
			System.err.println("ERROR: Tried to abort a null job.");
		}
	}

	public void startEvents() throws CoreException {
		try {
			proxy.startEvents();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
					e.getMessage(), e));
		}
	}
	
	public void stopEvents() throws CoreException {
		try {
			proxy.stopEvents();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
					e.getMessage(), e));
		}	
	}

	private AttributeManager getAttributeManager(String[] kvs, int start, int end) {
		AttributeManager mgr = new AttributeManager();
		
		for (int i = start; i <= end; i++) {
			String[] kv = kvs[i].split("=");
			if (kv.length == 2) {
				try {
					IAttributeDefinition attrDef = attrDefManager.getAttributeDefinition(kv[0]);
					if(attrDef != null) {
						IAttribute attr = attrDef.create(kv[1]);
						mgr.setAttribute(attr);
					} else {
						System.out.println("AbstractProxyRuntimSystem: unknown attribute definition");
					}
				} catch (IllegalValueException e1) {
					System.out.println("AbstractProxyRuntimSystem: invalid attribute for definition");
				}
			}
		}
		
		return mgr;
	}

	private ElementAttributeManager getElementAttributeManager(String[] args, int pos) {
		ElementAttributeManager eMgr = new ElementAttributeManager();
		
		try {
			int numRanges = Integer.parseInt(args[pos++]);
			
			for (int i = 0; i < numRanges; i++) {
				if (pos >= args.length) {
					return null;					
				}
				
				RangeSet jobIds = new RangeSet(args[pos++]);
				int numAttrs = Integer.parseInt(args[pos++]);
				
				int start = pos;
				int end = pos + numAttrs - 1;
				
				if (end >= args.length) {
					return null;					
				}
				
				eMgr.setAttributeManager(jobIds, getAttributeManager(args, start, end));
				
				pos = end + 1;
			}
		} catch (NumberFormatException e1) {
			return null;
		}
		
		return eMgr;
	}
	
	/**
	 * Parse and extract an attribute definition.
	 * 
	 * On entry, we know that end < args.length and end - start >= ATTR_MIN_LEN
	 * 
	 */
	private IAttributeDefinition parseAttributeDefinition(String[] args, int start, int end) {
		int pos = start;
		IAttributeDefinition attrDef = null;
		
		String attrId = args[pos++];
		String attrType = args[pos++];
		String attrName = args[pos++];
		String attrDesc = args[pos++];
		String attrDefault = args[pos++];
		
		if (attrType.equals("BOOLEAN")) {
			try {
				Boolean defVal = Boolean.parseBoolean(attrDefault);
				attrDef = attrDefManager.createBooleanAttributeDefinition(attrId, attrName, attrDesc, defVal);
			} catch (NumberFormatException ex) {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not convert args to double"));
			}
		} else if (attrType.equals("DATE")) {
			if (end - pos >= 2) {
				try {
					int dateStyle = toDateStyle(args[pos++]);
					int timeStyle = toDateStyle(args[pos++]);
					Locale locale = toLocale(args[pos++]);
					
					DateFormat fmt = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
					Date defVal = fmt.parse(attrDefault);
					
					if (end - pos >= 1) {
						Date min = fmt.parse(args[pos++]);
						Date max = fmt.parse(args[pos++]);
						attrDef = attrDefManager.createDateAttributeDefinition(attrId, attrName, attrDesc, defVal, fmt, min, max);
					} else {
						attrDef = attrDefManager.createDateAttributeDefinition(attrId, attrName, attrDesc, defVal, fmt);
					}
				} catch (ParseException ex) {
					fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not parse date"));
				} catch (IllegalValueException ex) {
					fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not create attribute definition"));					
				}
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: missing date format"));				
			}
		} else if (attrType.equals("DOUBLE")) {
			try {
				Double defVal = Double.parseDouble(attrDefault);
				if (end - pos >= 1) {
						Double min = Double.parseDouble(args[pos++]);
						Double max = Double.parseDouble(args[pos++]);
						attrDef = attrDefManager.createDoubleAttributeDefinition(attrId, attrName, attrDesc, defVal, min, max);
				} else {
					attrDef = attrDefManager.createDoubleAttributeDefinition(attrId, attrName, attrDesc, defVal);
				}
			} catch (NumberFormatException ex) {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not convert args to double"));
			} catch (IllegalValueException ex) {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not create attribute definition"));					
			}
		} else if (attrType.equals("ENUMERATED")) {
			if (pos != end) {
				ArrayList<String> values = new ArrayList<String>();
				while (pos < end) {
					values.add(args[pos++]);
				}
				try {
					attrDef = attrDefManager.createEnumeratedAttributeDefinition(attrId, attrName, attrDesc, attrDefault, values.toArray(new String[values.size()]));
				} catch (IllegalValueException ex) {
					fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not create attribute definition"));					
				}
			} else {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: no enumerated values"));	
			}
		} else if (attrType.equals("INTEGER")) {
			try {
				Integer defVal = Integer.parseInt(attrDefault);
				if (end - pos >= 1) {
						Integer min = Integer.parseInt(args[pos++]);
						Integer max = Integer.parseInt(args[pos++]);
						attrDef = attrDefManager.createIntegerAttributeDefinition(attrId, attrName, attrDesc, defVal, min, max);
				} else {
					attrDef = attrDefManager.createIntegerAttributeDefinition(attrId, attrName, attrDesc, defVal);
				}
			} catch (NumberFormatException ex) {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not convert args to integer"));
			} catch (IllegalValueException ex) {
				fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: could not create attribute definition"));					
			}
		} else if (attrType.equals("STRING")) {
			attrDef = attrDefManager.createStringAttributeDefinition(attrId, attrName, attrDesc, attrDefault);
		} else {
			fireRuntimeErrorEvent(new RuntimeErrorEvent("Bad proxy event: unknown attribute type"));
		}
		
		return attrDef;
	}

	private int toDateStyle(String val) {
		if (val.equals("SHORT")) {
			return DateFormat.SHORT;
		} else if (val.equals("MEDIUM")) {
			return DateFormat.MEDIUM;
		} else if (val.equals("LONG")) {
			return DateFormat.LONG;
		} else if (val.equals("FULL")) {
			return DateFormat.FULL;
		} else {
			return DateFormat.DEFAULT;
		}
	}

	private Locale toLocale(String val) {
		if (val.equals("CANADA")) {
			return Locale.CANADA;
		} else if (val.equals("CHINA")) {
			return Locale.CHINA;
		} else if (val.equals("FRANCE")) {
			return Locale.FRANCE;
		} else if (val.equals("GERMANY")) {
			return Locale.GERMANY;
		} else if (val.equals("ITALY")) {
			return Locale.ITALY;
		} else if (val.equals("JAPAN")) {
			return Locale.JAPAN;
		} else if (val.equals("TAIWAN")) {
			return Locale.TAIWAN;
		} else if (val.equals("UK")) {
			return Locale.UK;
		} else if (val.equals("US")) {
			return Locale.US;
		} else {
			return Locale.US;
		}
	}

	
}
