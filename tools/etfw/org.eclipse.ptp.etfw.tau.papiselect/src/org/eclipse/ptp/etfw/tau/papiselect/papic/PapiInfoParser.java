package org.eclipse.ptp.etfw.tau.papiselect.papic;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PapiInfoParser extends DefaultHandler {
	/* Tag strings */
	private static final String eventinfo = "eventinfo";
	// private static final String hardware="hardware";
	private static final String component = "component";
	private static final String eventset = "eventset";
	private static final String event = "event";
	private static final String modifier = "modifier";

	/* Attribute strings */
	private static final String index = "index";
	private static final String type = "type";
	private static final String id = "id";
	private static final String name = "name";
	private static final String desc = "desc";
	private static final String code = "code";

	private static String getAttribute(String name, Attributes atts)
	{
		final int repdex = atts.getIndex(name);
		if (repdex >= 0)
		{
			return atts.getValue(repdex);
		} else {
			return null;
		}
	}

	private static int getIntAttribute(String name, Attributes atts) {
		final String str = getAttribute(name, atts);
		if (str == null)
		{
			return -1;
		}
		return (Integer.parseInt(str));
	}

	// private Stack<StringBuffer> content = new Stack<StringBuffer>();
	private final Stack<String> tagStack = new Stack<String>();
	// private Tree tree;
	private EventTree et;
	private Component curComp;

	// public void characters(char[] chars, int start, int len)
	// {
	// ((StringBuffer)content.peek()).append(chars, start, len);
	// }

	private EventSet curESet;

	private Event curEvent;

	@Override
	public void endElement(String uri, String localName, String eName) throws SAXException {
		eName = eName.toLowerCase();

		if (eName.equals(eventinfo))
		{
			// et=new EventTree();//TODO: Manage sequential parsings better?
		}
		else if (eName.equals(component))
		{
			curComp = null;
			// if(et!=null)
			// {
			// curComp=new Component(getIntAttribute(index,atts),getAttribute(type,atts),getAttribute(id,atts));
			// et.compList.add(curComp);
			// }
		}
		else if (eName.equals(eventset))
		{
			curESet = null;
			// if(curComp!=null){
			// curESet=new EventSet(getAttribute(type,atts));
			// curComp.eventSetList.add(curESet);
			// }
		}
		else if (eName.equals(event))
		{
			curEvent = null;
			// if(curESet!=null){
			// curEvent=new Event(getAttribute(name,atts),getAttribute(desc,atts),getAttribute(code,atts));
			// curESet.eventList.add(curEvent);
			// }
		}
		// else if(eName.equals(modifier))
		// {
		// if(curEvent!=null){
		// Modifier mod=new Modifier(getAttribute(name,atts),getAttribute(desc,atts),getAttribute(code,atts));
		// curEvent.modifierList.add(mod);
		// }
		// }

		tagStack.pop();
	}

	public EventTree getEventTree() {
		return et;
	}

	// private static boolean getBooleanAttribute(String name, boolean defValue, Attributes atts)
	// {
	// String boolAtt=getAttribute(name,atts);
	// if(boolAtt==null)
	// return defValue;
	// if(boolAtt.toLowerCase().equals("true"))
	// return true;
	// else if(boolAtt.toLowerCase().equals("false"))
	// return false;
	// return defValue;
	// }

	public void reset() {

		et = null;
		curComp = null;
		curESet = null;
		curEvent = null;
	}

	@Override
	public void startElement(String uri, String localName, String eName, Attributes atts) throws SAXException {
		eName = eName.toLowerCase();
		// System.out.println(eName);
		if (eName.equals(eventinfo))
		{
			// if(tree==null)
			// {return;}
			et = new EventTree();
		}
		else if (eName.equals(component))
		{
			if (et == null)
			{
				return;
			}
			curComp = new Component(et, getIntAttribute(index, atts), getAttribute(type, atts), getAttribute(id, atts));
			et.addChild(curComp);
		}
		else if (eName.equals(eventset))
		{
			if (curComp == null)
			{
				return;
			}
			curESet = new EventSet(curComp, getAttribute(type, atts));
			curComp.addChild(curESet);
		}
		else if (eName.equals(event))
		{
			if (curESet == null) {
				return;
			}
			curEvent = new Event(curESet, getIntAttribute(index, atts), getAttribute(name, atts), getAttribute(desc, atts),
					getAttribute(code, atts));
			curESet.addChild(curEvent);
			curESet.fullSet.add(new Integer(curEvent.index));
		}
		else if (eName.equals(modifier))
		{
			if (curEvent == null)
			{
				return;
			}
			// Modifier mod=
			curEvent.addChild(new Modifier(curEvent, getAttribute(name, atts), getAttribute(desc, atts), getAttribute(code, atts)));
		}

		tagStack.push(eName.toLowerCase());

	}

}
