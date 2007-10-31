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
package org.eclipse.ptp.debug.internal.core.pdi.model;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeDerived;
import org.eclipse.ptp.debug.core.pdi.model.aif.IValueAggregate;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.request.GetPartialAIFRequest;

/**
 * @author clement
 *
 */
public abstract class Variable extends VariableDescriptor implements IPDIVariable {
	private static IPDIVariable[] NO_CHILDREN = new IPDIVariable[0];
	public IPDIVariable[] children = NO_CHILDREN;

	protected String editable = null;
	protected String language;
	protected boolean isFake = false;
	protected boolean isUpdated = true;
			
	public Variable(Session session, VariableDescriptor varDesc, String varid) {
		super(session, varDesc);
		this.varid = varid;
	}
	public Variable(Session session, BitList tasks, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, String varid) {
		super(session, tasks, thread, frame, name, fullName, pos, depth);
		this.varid = varid;
	}
	public void setUpdated(boolean update) {
		this.isUpdated = update;
	}
	public boolean isUpdated() {
		return isUpdated;
	}
	public void update() throws PDIException {
		session.getVariableManager().update(getTasks());
	}
	public Variable getChild(String varid) {
		for (IPDIVariable variable : children) {
			if (variable.getVarId().equals(varid)) {
				return (Variable)variable;
			}
			Variable grandChild = ((Variable)variable).getChild(varid);
			if (grandChild != null)
				return grandChild;
		}
		return null;
	}
	void setIsFake(boolean f) {
		isFake = f;
	}
	boolean isFake() {
		return isFake;
	}
	public IPDIVariable[] getChildren() throws PDIException {
		String fn = getFullName();
		boolean childFake = false;
		
		Target target = (Target)fStackFrame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(fStackFrame.getThread(), false);				
			((Thread)fStackFrame.getThread()).setCurrentStackFrame(fStackFrame, false);

			GetPartialAIFRequest request = new GetPartialAIFRequest(getTasks(), getQualifiedName(), varid, true);
			session.getEventRequestManager().addEventRequest(request);
			IAIF aif = request.getPartialAIF(getTasks());
			String ch_varid = request.getVarId(getTasks());
	
			IAIFType type = aif.getType();
			IAIFValue value = aif.getValue();
			fTypename = aif.getDescription();
			if (varid == null) {
				varid = ch_varid;
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
						String ch_k = varid + "." + i;
						Variable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);					
						v.setIsFake(childFake);
						v.aif = new AIF(baseType, values[i]);
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
							String ch_k = varid + "." + ch_n;
							Variable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
							v.setIsFake(childFake);
							v.aif = new AIF(aggrType.getType(i), ((IValueAggregate)value).getValue(i));
							children[i] = v;
						}
					}
					else {
						children = new Variable[1];
						String ch_fn = "*(" + fn + ")";
						String ch_n = ch_varid;
						String ch_k = varid + "." + ch_n;
						Variable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
						v.setIsFake(childFake);
						v.aif = new AIF(type, value);
						children[0] = v;					
					}
				}
			}
			else if (type instanceof IAIFTypeReference) {
				children = new Variable[1];
				String ch_fn = "(" + fn + ")->" + ((IAIFTypeReference)type).getName();
				String ch_n = ((IAIFTypeReference)type).getName();
				String ch_k = varid + "." + ch_n;
				Variable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
				v.setIsFake(childFake);
				v.aif = new AIF(type, value);
				children[0] = v;					
			}
			else if (type instanceof ITypeAggregate) {
				ITypeAggregate aggrType = (ITypeAggregate)type;
				children = new Variable[aggrType.getNumberOfChildren()];
				for (int i=0; i<children.length; i++) {
					String ch_fn = "(" + fn + ")." + aggrType.getField(i);
					String ch_n = aggrType.getField(i); 
					String ch_k = varid + "." + ch_n;
					Variable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
					v.setIsFake(childFake);
					v.aif = new AIF(aggrType.getType(i), ((IValueAggregate)value).getValue(i));
					children[i] = v;
				}
			}
			else {
				children = new Variable[1];
				String ch_fn = fn;
				String ch_n = ch_varid;
				String ch_k = varid + "." + ch_n;
				Variable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(), getStackDepth(), ch_k);
				v.setIsFake(childFake);
				v.aif = new AIF(type, value);
				children[0] = v;
			}
			return children;
		}
		finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}
	public IPDIVariable[] getChildren(int findex, int psize) throws PDIException {
		IPDIVariableDescriptor vo = getVariableDescriptorAsArray(findex, psize);
		Variable var = session.getVariableManager().createVariable((VariableDescriptor)vo);
		return var.getChildren();
	}
	public int getChildrenNumber() throws PDIException {
		IAIF a= getAIF();
		if (a.getType() instanceof IAIFTypeArray) {
			return ((IAIFTypeArray)a.getType()).getRange();
		}
		if (a.getType() instanceof ITypeAggregate) {
			return ((ITypeAggregate)a.getType()).getNumberOfChildren();
		}
		return 0;
	}
	public boolean isEditable() throws PDIException {
		if (editable == null) {
			throw new PDIException(getTasks(), "Not implement yet - Variable: isEditable()");
		}
		return (editable == null) ? false : editable.equalsIgnoreCase("true");
	}
	public boolean equals(IPDIVariable var) {
		if (var instanceof Variable) {
			Variable variable = (Variable) var;
			return variable.getVarId().equals(getVarId());
		}
		return super.equals(var);
	}
	public void dispose() throws PDIException {
		session.getVariableManager().destroyVariable(this);
	}
	public void resetValue() {
		setAIF(null);
	}
	public void setValue(String expression) throws PDIException {
		throw new PDIException(getTasks(), "Not implemented setValue(String) yet.");
	}
	protected abstract Variable createVariable(Session session, BitList tasks, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, String varid);	
}
