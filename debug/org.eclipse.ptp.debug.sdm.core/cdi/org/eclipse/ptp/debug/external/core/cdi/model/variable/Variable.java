/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.debug.external.core.cdi.model.variable;

import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.aif.ITypeAggregate;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;
import org.eclipse.ptp.debug.external.core.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.cdi.VariableManager;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;
import org.eclipse.ptp.debug.external.core.commands.GetAIFCommand;

/**
 * @author Clement chu
 * 
 */
public abstract class Variable extends VariableDescriptor implements IPCDIVariable {
	public IPCDIVariable[] children = new IPCDIVariable[0];
	String editable = null;
	String language;
	boolean isFake = false;
	boolean isUpdated = true;
	
	public Variable(VariableDescriptor obj) {
		super(obj);
	}
	public Variable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, IAIF aif) {
		super(target, thread, frame, name, fullName, pos, depth, aif);
	}
	public void setUpdated(boolean update) {
		isUpdated = update;
	}
	public void setChildren(IPCDIVariable[] children) {
		if (children != null)
			this.children = children;
	}
	public boolean isUpdated() {
		return isUpdated;
	}
	/*
	public void update() throws PCDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		mgr.update(this);
	}
	*/
	public Variable getChild(String name) {
		for (int i = 0; i < children.length; i++) {
			Variable var = (Variable) children[i];
			if (name.equals(var.getMIName())) {
				return var;
			}
			// Look also in the grandchildren.
			Variable grandChild = var.getChild(name);
			if (grandChild != null) {
				return grandChild;
			}
			if (name.startsWith(var.getMIName()+".")) {
				return var;
			}
		}
		return null;
	}
	void setIsFake(boolean f) {
		isFake = f;
	}
	boolean isFake() {
		return isFake;
	}
	public IPCDIVariable[] getChildren() {
		return children;
	}
	/*
	public IPCDIVariable[] getChildren(int timeout) throws PCDIException {
		List varList = new ArrayList(1);
		Target target = (Target)getTarget();
		
		String name = getQualifiedName();
		ListLocalVariablesCommand command = new ListLocalVariablesCommand(target.getTask(), getStackFrame());
		target.getDebugger().postCommand(command);
		IPCDILocalVariable[] vars = command.getLocalVariables();
		for (int i = 0; i < vars.length; i++) {
			if (name.equals(vars[i].getQualifiedName())) {
				varList.add(vars[i]);
			}
		}
		return (IPCDIVariable[])varList.toArray(new IPCDIVariable[0]);
	}
	*/
	protected abstract Variable createVariable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, IAIF aif);
	
	public int getChildrenNumber() throws PCDIException {
		IAIF aif = getAIF();
		if (aif.getType() instanceof ITypeAggregate) {
			return ((ITypeAggregate)aif.getType()).getNumberOfChildren();
		}
		return 0;
	}

	/*
	public ICDIValue getValue() throws PCDIException {
		if (value == null) {
			ICDIType t = getType();
			if (t instanceof ICDIBoolType) {
				value = new BoolValue(this);
			} else if (t instanceof ICDICharType) {
				value = new CharValue(this);
			} else if (t instanceof ICDIWCharType) {
				value = new WCharValue(this);
			} else if (t instanceof ICDIShortType) {
				value = new ShortValue(this);
			} else if (t instanceof ICDIIntType) {
				value = new IntValue(this);
			} else if (t instanceof ICDILongType) {
				value = new LongValue(this);
			} else if (t instanceof ICDILongLongType) {
				value = new LongLongValue(this);
			} else if (t instanceof ICDIEnumType) {
				value = new EnumValue(this);
			} else if (t instanceof ICDIFloatType) {
				value = new FloatValue(this);
			} else if (t instanceof ICDIDoubleType) {
				value = new DoubleValue(this);
			} else if (t instanceof ICDIFunctionType) {
				value = new FunctionValue(this);
			} else if (t instanceof ICDIPointerType) {
				value = new PointerValue(this);
			} else if (t instanceof ICDIReferenceType) {
				value = new ReferenceValue(this);
			} else if (t instanceof ICDIArrayType) {
				value = new ArrayValue(this);
			} else if (t instanceof ICDIStructType) {
				value = new StructValue(this);
			} else {
				value = new Value(this);
			}
		}
		return value;
	}
	public void setValue(ICDIValue value) throws PCDIException {
		setValue(value.getValueString());
	}
	*/
	public void setValue(IAIF aif) throws PCDIException {
		setAIF(aif);
	}
	
	public void setValue(String expression) throws PCDIException {
		Target target = (Target)getTarget();
		Session session = (Session)target.getSession();
		//TODO - not implement yet - variable assign
		//target.getDebugger().varAssign(session.createBitList(target.getTargetID()), getName(), expression);

		// If expression was on autoupdate, update all the other expression
		ExpressionManager expMgr = ((Session)target.getSession()).getExpressionManager();
		if (expMgr.isAutoUpdate()) {
			expMgr.update(target);
		}
		// If variable was on autoupdate, update all the variables.
		VariableManager varMgr = session.getVariableManager();
		if (varMgr.isAutoUpdate()) {
			varMgr.update(target);
		}
		throw new PCDIException("Not implemented yet - Variable: setValue");
	}
	public boolean isEditable() throws PCDIException {
		if (editable == null) {
			/*
			Target target = (Target)getTarget();
			IAbstractDebugger debugger = target.getDebugger();
			Session session = (Session)target.getSession();
			*/
			//TODO - not implement yet - show attributes
			//editable = String.valueOf(debugger.showAttributes(session.createBitList(target.getTargetID()), getName()));
			throw new PCDIException("Not implement yet - Variable: isEditable");
		}
		return (editable == null) ? false : editable.equalsIgnoreCase("true");
	}
	public void setFormat(int format) throws PCDIException {
		/*
		Target target = (Target)getTarget();
		IAbstractDebugger debugger = target.getDebugger();
		Session session = (Session)target.getSession();
		*/
		//TODO - not implement yet - set format
		//debugger.setFormat(session.createBitList(target.getTargetID()), getName());
		throw new PCDIException("Not implement yet - Variable: setFormat");
	}
	public boolean equals(IPCDIVariable var) {
		if (var instanceof Variable) {
			Variable variable = (Variable) var;
			return equals(variable);
		}
		return super.equals(var);
	}
	public boolean equals(Variable variable) {
		return getName().equals(variable.getName());
	}	
	public void dispose() throws PCDIException {
		IPCDITarget target = getTarget();
		VariableManager varMgr = ((Session)target.getSession()).getVariableManager();
		varMgr.destroyVariable(this);
	}
	public String getTypeName() throws PCDIException {
		if (aif == null) {
			Target target = (Target)getTarget();
			GetAIFCommand command = new GetAIFCommand(target.getTask(), getName());
			target.getDebugger().postCommand(command);
			aif = command.getAIF();
		}
		return aif.getDescription();
	}
}
