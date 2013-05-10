package org.eclipse.ptp.etfw.tau.papiselect.papic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.etfw.tau.papiselect.messages.Messages;

class Component extends ETItem {
	int index;
	String type;
	String id;
	Set<String> eNames;

	public Component(EventTree parent, int i, String type, String id) {
		super();
		// setParent(parent);//this.ti=new TreeItem(parent,SWT.NONE);
		this.label = type;// ti.setText(type);
		this.index = i;
		this.id = id;
		this.type = type;
		eNames = new HashSet<String>();
	}
}

class ETItem implements IStructuredContentProvider, ITreeContentProvider {// ),IAdaptable
																			// {
	public ETItem parent;
	public ArrayList<ETItem> children;
	public String label;
	public String desc;
	boolean checked = false;

	boolean inited = false;

	ETItem() {
		children = new ArrayList<ETItem>();
	}

	public void addChild(ETItem child) {
		children.add(child);
		child.parent = this;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public boolean getCheck() {
		return checked;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ETItem) {
			return ((ETItem) parentElement).children.toArray();
		}

		return null;
	}

	public Object[] getElements(Object inputElement) {

		if (inputElement instanceof EventTree && !inited) {
			final Object[] o = { inputElement };// initialize() };
			inited = true;
			return o;
		}

		if (inputElement instanceof ETItem) {
			return ((ETItem) inputElement).children.toArray();
		}

		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof ETItem) {
			return ((ETItem) element).parent;
		}

		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ETItem) {
			return ((ETItem) element).children.toArray().length > 0;
		}
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	public void setCheck(boolean state) {
		checked = state;
	}

	void setParent(ETItem parent) {
		parent.children.add(this);
		this.parent = parent;
	}

	// private EventTree initialize(){
	// PapiCSelect cSelect=new
	// PapiCSelect("E:\\PAPIProject\\ptest2.xml");//"/home/wspear/bin/papi_C/bin/papi_event_info");
	// return cSelect.getEventTree();
	// //return et;
	// }

	// @Override
	// public Object getAdapter(Class adapter) {
	// // TODO Auto-generated method stub
	// return null;
	// }
}

class Event extends ETItem {
	String name;
	int index;
	String code;

	public Event(EventSet parent, int index, String name, String desc, String code) {
		super();
		// setParent(parent);
		// ti=new TreeItem(parent,SWT.NONE);
		// ti.setText(name);
		this.label = name;
		this.index = index;
		this.name = name;
		this.desc = desc;
		this.code = code;
	}

	public String getCommand() {
		String com = label;
		int modcount = 0;
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).getCheck()) {
				com += children.get(i).label;
				modcount++;
			}
		}

		if (children.size() > 0 && modcount == 0) {
			com += children.get(0).label;
		}

		return com;
	}

	@Override
	public void setCheck(boolean state) {
		checked = state;

		String modLabel = this.label;
		if (this.children.size() > 0) {
			modLabel += this.children.get(0).label;
		}

		if (state) {
			((Component) this.parent.parent).eNames.add(modLabel);
			((EventSet) this.parent).checkedSet.add(new Integer(this.index));
		} else {
			((Component) this.parent.parent).eNames.remove(modLabel);
			((EventSet) this.parent).checkedSet.remove(new Integer(this.index));
		}
	}

	public String testCommand() {
		String com = label;
		int modcount = 0;
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).getCheck()) {
				com += children.get(i).label;
				modcount++;
			}
		}

		// if(children.size()>0&&modcount==0)
		// {
		// com+=children.get(0).label;
		// }

		return com;
	}
}

class EventSet extends ETItem {
	String type;
	Set<Integer> checkedSet;
	Set<Integer> fullSet;

	public EventSet(Component parent, String type) {
		super();
		// this.parent=parent;//ti=new TreeItem(parent,SWT.NONE);
		// setParent(parent);
		label = type;// ti.setText(type);
		this.type = type;
		checkedSet = new HashSet<Integer>();
		fullSet = new HashSet<Integer>();
	}
}

class EventTree extends ETItem {
	public EventTree() {
		super();
		label = Messages.EventTree_Events;
		desc = null;

	}
}

class Modifier extends ETItem {
	String name;
	String code;

	public Modifier(Event parent, String name, String desc, String code) {
		// ti=new TreeItem(parent,SWT.NONE);
		// ti.setText(name);
		super();
		// setParent(parent);
		this.label = name;
		this.name = name;
		this.desc = desc;
		this.code = code;
	}

}