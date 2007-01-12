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

import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueArray;
import org.eclipse.ptp.debug.core.aif.ITypeAggregate;
import org.eclipse.ptp.debug.core.aif.ITypeDerived;
import org.eclipse.ptp.debug.core.aif.IValueAggregate;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariableDescriptor;
import org.eclipse.ptp.debug.external.core.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.core.cdi.MemoryManager;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.cdi.VariableManager;
import org.eclipse.ptp.debug.external.core.cdi.event.VarChangedEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;
import org.eclipse.ptp.debug.external.core.commands.GetPartialAIFCommand;

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
	public Variable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, String keyName) {
		super(target, thread, frame, name, fullName, pos, depth);
		this.keyName = keyName;
	}
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	public void setUpdated(boolean update) {
		isUpdated = update;
	}
	public boolean isUpdated() {
		return isUpdated;
	}
	public Variable getChild(String name) {
		for (int i = 0; i < children.length; i++) {
			Variable var = (Variable) children[i];
			try {
				if (name.equals(var.getKeyName())) {
					return var;
				}
				// Look also in the grandchildren.
				Variable grandChild = var.getChild(name);
				if (grandChild != null) {
					return grandChild;
				}
			} catch (PCDIException e) {}
		}
		return null;
	}
	void setIsFake(boolean f) {
		isFake = f;
	}
	boolean isFake() {
		return isFake;
	}
	public String getKeyName() throws PCDIException {
		if (keyName == null) {
			throw new PCDIException("No key name found");
		}
		return keyName;
	}
	public IPCDIVariable[] getChildren() throws PCDIException {
		String key = keyName;
		String fn = getFullName();
		boolean childFake = false;
		
		Target target = (Target)getTarget();
		GetPartialAIFCommand command = new GetPartialAIFCommand(target.getTask(), getQualifiedName(), key, true);
		target.getDebugger().postCommand(command);
		
		IAIF aif = command.getPartialAIF();
		String ch_key = command.getName();

		IAIFType type = aif.getType();
		IAIFValue value = aif.getValue();
		fTypename = aif.getDescription();
		if (key == null) {
			key = keyName = ch_key;
		}

		if (type instanceof ITypeDerived) {
			IAIFType baseType = ((ITypeDerived)type).getBaseType();
			if (type instanceof IAIFTypeArray) {
				//always get from 0
				IAIFValue[] values = ((IAIFValueArray)value).getValues();
				children = new Variable[values.length];
				for (int i=0; i<children.length; i++) {
					int index = castingIndex + i;
					String ch_fn = "(" + fn + ")[" + index + "]";
					String ch_n = getName() + "[" + index + "]";
					String ch_k = key + "." + i;
					Variable v = createVariable((Target)getTarget(), (Thread)getThread(), (StackFrame)getStackFrame(), ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);					
					v.setIsFake(childFake);
					v.fType = baseType;
					v.fValue = values[i];
					children[i] = v;
				}
			}
			else if (type instanceof IAIFTypePointer) {
				if (baseType instanceof ITypeAggregate) {
					ITypeAggregate aggrType = (ITypeAggregate)baseType;
					children = new Variable[aggrType.getNumberOfChildren()];
					for (int i=0; i<children.length; i++) {
						String ch_fn = "(" + fn + ")->" + aggrType.getField(i);
						String ch_n = aggrType.getField(i); 
						String ch_k = key + "." + ch_n;
						Variable v = createVariable((Target)getTarget(), (Thread)getThread(), (StackFrame)getStackFrame(), ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
						v.setIsFake(childFake);
						v.fType = aggrType.getType(i);
						v.fValue = ((IValueAggregate)value).getValue(i);
						children[i] = v;
					}
				}
				else {
					children = new Variable[1];
					String ch_fn = "*(" + fn + ")";
					String ch_n = ch_key;
					String ch_k = key + "." + ch_key;
					Variable v = createVariable((Target)getTarget(), (Thread)getThread(), (StackFrame)getStackFrame(), ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
					v.setIsFake(childFake);
					v.fType = type;
					v.fValue = value;
					children[0] = v;					
				}
			}
		}
		else if (type instanceof IAIFTypeReference) {
			children = new Variable[1];
			String ch_fn = "(" + fn + ")->" + ((IAIFTypeReference)type).getName();
			String ch_n = ((IAIFTypeReference)type).getName();
			String ch_k = key + "." + ch_n;
			Variable v = createVariable((Target)getTarget(), (Thread)getThread(), (StackFrame)getStackFrame(), ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
			v.setIsFake(childFake);
			v.fType = type;
			v.fValue = value;
			children[0] = v;					
		}
		else if (type instanceof ITypeAggregate) {
			ITypeAggregate aggrType = (ITypeAggregate)type;
			children = new Variable[aggrType.getNumberOfChildren()];
			for (int i=0; i<children.length; i++) {
				String ch_fn = "(" + fn + ")." + aggrType.getField(i);
				String ch_n = aggrType.getField(i); 
				String ch_k = key + "." + ch_n;
				Variable v = createVariable((Target)getTarget(), (Thread)getThread(), (StackFrame)getStackFrame(), ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
				v.setIsFake(childFake);
				v.fType = aggrType.getType(i);
				v.fValue = ((IValueAggregate)value).getValue(i);
				children[i] = v;
			}
		}
		else {
			children = new Variable[1];
			String ch_fn = fn;
			String ch_n = ch_key;
			String ch_k = key + "." + ch_key;
			Variable v = createVariable((Target)getTarget(), (Thread)getThread(), (StackFrame)getStackFrame(), ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
			v.setIsFake(childFake);
			v.fType = type;
			v.fValue = value;
			children[0] = v;
		}
		return children;
	}

	protected abstract Variable createVariable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, String keyName);
	
	public int getChildrenNumber() throws PCDIException {
		IAIFType t = getType();
		if (t instanceof IAIFTypeArray) {
			return ((IAIFTypeArray)t).getRange();
		}
		if (t instanceof ITypeAggregate) {
			return ((ITypeAggregate)t).getNumberOfChildren();
		}
		return 0;
	}
	public IAIFValue getValue() throws PCDIException {
		if (fValue == null || fValue == AIFFactory.UNKNOWNVALUE) {
			Target target = (Target)getTarget();
			GetPartialAIFCommand command = new GetPartialAIFCommand(getTarget().getTask(), getQualifiedName(), getKeyName(), false, true);
			target.getDebugger().postCommand(command);

			IAIF aif = command.getPartialAIF();
			fType = aif.getType();
			fValue = aif.getValue();
			fTypename = aif.getDescription();
			keyName = command.getName();
		}
		return fValue;
	}
	public void resetValue() {
		fValue = null;
	}
	
	public void setValue(IAIFValue fValue) throws PCDIException {
		try {
			setValue(fValue.getValueString());
		} catch (AIFException e) {
			throw new PCDIException(e);
		}
	}
	public void setValue(String expression) throws PCDIException {
		Target target = (Target)getTarget();
		//TODO - not implement yet - variable assign
		//target.getDebugger().varAssign(target.getTasks(), getKeyName(), expression);
		//If the assign was succesfull fire a MIVarChangedEvent() for the variable
		// Note GDB will not fire an event for the changed variable we have to do it manually.
		target.getDebugger().fireEvent(new VarChangedEvent(target.getSession(), target.getTask(), this, getKeyName()));

		// Changing values may have side effects i.e. affecting other variables
		// if the manager is on autoupdate check all the other variables.
		// Note: This maybe very costly.
		// assigning may have side effects i.e. affecting other registers.
		/*
		// If register was on autoupdate, update all the other registers
		RegisterManager regMgr = ((Session)target.getSession()).getRegisterManager();
		if (regMgr.isAutoUpdate()) {
			regMgr.update(target);
		}
		*/
		// If expression manager is on autoupdate, update all expressions
		ExpressionManager expMgr = ((Session)target.getSession()).getExpressionManager();
		if (expMgr.isAutoUpdate()) {
			expMgr.update(target);
		}
		// If variable manager is on autoupdate, update all variables
		VariableManager varMgr = ((Session)target.getSession()).getVariableManager();
		if (varMgr.isAutoUpdate()) {
			varMgr.update(target);
		}
		// If memory manager is on autoupdate, update all memory blocks
		MemoryManager memMgr = ((Session)target.getSession()).getMemoryManager();
		if (memMgr.isAutoUpdate()) {
			memMgr.update(target);
		}		
		throw new PCDIException("Not implemented yet - Variable: setValue -- value assgin");
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
		try {
			return getKeyName().equals(variable.getKeyName());
		} catch (PCDIException e) {
		}
		return super.equals(variable);
	}	
	public void dispose() throws PCDIException {
		IPCDITarget target = getTarget();
		VariableManager varMgr = ((Session)target.getSession()).getVariableManager();
		varMgr.destroyVariable(this);
	}
	public String getTypeName() throws PCDIException {
		return getType().toString();
	}
	public IPCDIVariable[] getChildren(int findex, int psize) throws PCDIException {
		IPCDITarget target = getTarget();
		VariableManager varMgr = ((Session)target.getSession()).getVariableManager();
		IPCDIVariableDescriptor vo = varMgr.getVariableDescriptorAsArray(this, findex, psize);
		Variable var = varMgr.createVariable((VariableDescriptor)vo);
		return var.getChildren();
	}
}
