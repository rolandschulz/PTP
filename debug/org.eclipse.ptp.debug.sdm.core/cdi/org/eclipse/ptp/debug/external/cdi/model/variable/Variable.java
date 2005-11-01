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
package org.eclipse.ptp.debug.external.cdi.model.variable;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIEnumType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharType;
import org.eclipse.ptp.debug.external.aif.IAIF;
import org.eclipse.ptp.debug.external.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.debug.external.cdi.model.type.ArrayValue;
import org.eclipse.ptp.debug.external.cdi.model.type.BoolValue;
import org.eclipse.ptp.debug.external.cdi.model.type.CharValue;
import org.eclipse.ptp.debug.external.cdi.model.type.DoubleValue;
import org.eclipse.ptp.debug.external.cdi.model.type.EnumValue;
import org.eclipse.ptp.debug.external.cdi.model.type.FloatValue;
import org.eclipse.ptp.debug.external.cdi.model.type.FunctionValue;
import org.eclipse.ptp.debug.external.cdi.model.type.IntValue;
import org.eclipse.ptp.debug.external.cdi.model.type.LongLongValue;
import org.eclipse.ptp.debug.external.cdi.model.type.LongValue;
import org.eclipse.ptp.debug.external.cdi.model.type.PointerValue;
import org.eclipse.ptp.debug.external.cdi.model.type.ReferenceValue;
import org.eclipse.ptp.debug.external.cdi.model.type.ShortValue;
import org.eclipse.ptp.debug.external.cdi.model.type.StructValue;
import org.eclipse.ptp.debug.external.cdi.model.type.Value;
import org.eclipse.ptp.debug.external.cdi.model.type.WCharValue;

/**
 */
public abstract class Variable extends VariableDescriptor implements ICDIVariable {
	Value value;
	public ICDIVariable[] children = new ICDIVariable[0];
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
	public boolean isUpdated() {
		return isUpdated;
	}
	public void update() throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		mgr.update(this);
	}
	public Variable getChild(String name) {
		for (int i = 0; i < children.length; i++) {
			Variable variable = (Variable) children[i];
			if (name.equals(variable.getName())) {
				return variable;
			}
			// Look also in the grandchildren.
			Variable grandChild = variable.getChild(name);
			if (grandChild != null) {
				return grandChild;
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
	public ICDIVariable[] getChildren() throws CDIException {
		// Use the default timeout.
		return getChildren(-1);
	}
	//TODO - dunno whether it implemented correctly or not
	public ICDIVariable[] getChildren(int timeout) throws CDIException {
		List varList = new ArrayList(1);
		Target target = (Target)getTarget();
		Session session = (Session)target.getSession();
		
		String name = getQualifiedName();
		ICDILocalVariable[] vars = target.getDebugger().listLocalVariables(session.createBitList(target.getTargetID()), getStackFrame());
		for (int i = 0; i < vars.length; i++) {
			if (name.equals(vars[i].getQualifiedName())) {
				varList.add(vars[i]);
			}
		}
		return (ICDIVariable[])varList.toArray(new ICDIVariable[0]);
	}

	protected abstract Variable createVariable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, IAIF aif);
	
	public int getChildrenNumber() throws CDIException {
		//FIXME no child number provided
		return 1;
	}

	public ICDIValue getValue() throws CDIException {
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
	public void setValue(ICDIValue value) throws CDIException {
		setValue(value.getValueString());
	}
	public void setValue(String expression) throws CDIException {
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
		throw new CDIException("Not implemented yet - Variable: setValue");
	}
	public boolean isEditable() throws CDIException {
		if (editable == null) {
			/*
			Target target = (Target)getTarget();
			IAbstractDebugger debugger = target.getDebugger();
			Session session = (Session)target.getSession();
			*/
			//TODO - not implement yet - show attributes
			//editable = String.valueOf(debugger.showAttributes(session.createBitList(target.getTargetID()), getName()));
			throw new CDIException("Not implement yet - Variable: isEditable");
		}
		return (editable == null) ? false : editable.equalsIgnoreCase("true");
	}
	public void setFormat(int format) throws CDIException {
		/*
		Target target = (Target)getTarget();
		IAbstractDebugger debugger = target.getDebugger();
		Session session = (Session)target.getSession();
		*/
		//TODO - not implement yet - set format
		//debugger.setFormat(session.createBitList(target.getTargetID()), getName());
		throw new CDIException("Not implement yet - Variable: setFormat");
	}
	public boolean equals(ICDIVariable var) {
		if (var instanceof Variable) {
			Variable variable = (Variable) var;
			return equals(variable);
		}
		return super.equals(var);
	}
	public boolean equals(Variable variable) {
		return getName().equals(variable.getName());
	}	
	public void dispose() throws CDIException {
		ICDITarget target = getTarget();
		VariableManager varMgr = ((Session)target.getSession()).getVariableManager();
		varMgr.destroyVariable(this);
	}
	public String getTypeName() throws CDIException {
		if (aif == null) {
			Target target = (Target)getTarget();
			Session session = (Session)target.getSession();
			fTypename = target.getDebugger().getVariableType(session.createBitList(target.getTargetID()), getName());
		}
		else {
			//TODO - fix the toString later
			fTypename = aif.getType().toString();
		}
		return fTypename;
	}
}
