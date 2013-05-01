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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeUnion;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueUnion;
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeDerived;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluatePartialExpressionRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

/**
 * @author clement
 * 
 */
public abstract class Variable extends VariableDescriptor implements IPDIVariable {
	private static IPDIVariable[] NO_CHILDREN = new IPDIVariable[0];
	private IPDIVariable[] children = NO_CHILDREN;

	protected String editable = null;
	protected String language;
	protected boolean isUpdated = true;

	public Variable(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame frame, String name, String fullName,
			int pos, int depth, String varId) {
		super(session, tasks, thread, frame, name, fullName, pos, depth);
		this.varId = varId;
	}

	public Variable(IPDISession session, IPDIVariableDescriptor varDesc, String varId) {
		super(session, varDesc);
		this.varId = varId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#dispose()
	 */
	public void dispose() throws PDIException {
		session.getVariableManager().destroyVariable(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#equals(org.eclipse.
	 * ptp.debug.core.pdi.model.IPDIVariable)
	 */
	public boolean equals(IPDIVariable var) {
		if (var instanceof Variable) {
			Variable variable = (Variable) var;
			return variable.getId().equals(getId());
		}
		return super.equalDescriptors(var);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#getChild(java.lang.
	 * String)
	 */
	public IPDIVariable getChild(String varid) {
		for (IPDIVariable variable : children) {
			if (variable.getId().equals(varid)) {
				return variable;
			}
			IPDIVariable grandChild = (variable).getChild(varid);
			if (grandChild != null) {
				return grandChild;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#getChildren()
	 */
	public IPDIVariable[] getChildren() throws PDIException {
		String fn = getFullName();

		Target target = (Target) fStackFrame.getTarget();
		Thread currentThread = target.getCurrentThread();
		IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(fStackFrame.getThread(), false);
			((Thread) fStackFrame.getThread()).setCurrentStackFrame(fStackFrame, false);

			IPDIEvaluatePartialExpressionRequest request = session.getRequestFactory().getEvaluatePartialExpressionRequest(
					getTasks(), getQualifiedName(), varId, true);
			session.getEventRequestManager().addEventRequest(request);
			IAIF aif = request.getPartialAIF(getTasks());
			String ch_varid = request.getId(getTasks());

			IAIFType type = aif.getType();
			IAIFValue value = aif.getValue();
			fTypename = aif.getDescription();
			if (varId == null) {
				varId = ch_varid;
			}

			if (type instanceof ITypeDerived) {
				IAIFType baseType = ((ITypeDerived) type).getBaseType();
				if (type instanceof IAIFTypeArray) {
					// always get from 0
					IAIFValue[] values = ((IAIFValueArray) value).getValues();
					children = new Variable[values.length];
					for (int i = 0; i < children.length; i++) {
						int index = castingIndex + i;
						String ch_fn = "(" + fn + ")[" + index + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						String ch_n = getName() + "[" + index + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						String ch_k = varId + "." + i; //$NON-NLS-1$
						IPDIVariable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(),
								getStackDepth(), ch_k);
						v.setAIF(AIFFactory.newAIF(baseType, values[i]));
						children[i] = v;
					}
				} else if (type instanceof IAIFTypePointer) {
					if (baseType instanceof IAIFTypeAggregate) {
						IAIFTypeAggregate aggrType = (IAIFTypeAggregate) baseType;
						IAIFValueAggregate aggrVal = (IAIFValueAggregate) ((IAIFValuePointer) value).getValue();
						String[] fieldNames = aggrType.getFieldNames(IAIFTypeAggregate.AIF_CLASS_ACCESS_ALL);
						IAIFType[] fieldTypes = aggrType.getFieldTypes(IAIFTypeAggregate.AIF_CLASS_ACCESS_ALL);
						children = new Variable[fieldNames.length];
						for (int i = 0; i < children.length; i++) {
							String ch_fn = "(" + fn + ")->" + fieldNames[i]; //$NON-NLS-1$ //$NON-NLS-2$
							String ch_n = fieldNames[i];
							String ch_k = varId + "." + ch_n; //$NON-NLS-1$
							IPDIVariable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(),
									getStackDepth(), ch_k);
							v.setAIF(AIFFactory.newAIF(fieldTypes[i], aggrVal.getFieldValue(fieldNames[i])));
							children[i] = v;
						}
					} else if (baseType instanceof IAIFTypeUnion) {
						IAIFTypeUnion unionType = (IAIFTypeUnion) baseType;
						IAIFValueUnion unionVal = (IAIFValueUnion) ((IAIFValuePointer) value).getValue();
						String[] fieldNames = unionType.getFieldNames();
						IAIFType[] fieldTypes = unionType.getFieldTypes();
						children = new Variable[fieldNames.length];
						for (int i = 0; i < children.length; i++) {
							String ch_fn = "(" + fn + ")->" + fieldNames[i]; //$NON-NLS-1$ //$NON-NLS-2$
							String ch_n = fieldNames[i];
							String ch_k = varId + "." + ch_n; //$NON-NLS-1$
							IPDIVariable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(),
									getStackDepth(), ch_k);
							v.setAIF(AIFFactory.newAIF(fieldTypes[i], unionVal.getFieldValue(fieldNames[i])));
							children[i] = v;
						}
					} else {
						children = new Variable[1];
						String ch_fn = "*(" + fn + ")"; //$NON-NLS-1$ //$NON-NLS-2$
						String ch_n = ch_varid;
						String ch_k = varId + "." + ch_n; //$NON-NLS-1$
						IPDIVariable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(),
								getStackDepth(), ch_k);
						v.setAIF(AIFFactory.newAIF(baseType, ((IAIFValuePointer) value).getValue()));
						children[0] = v;
					}
				}
			} else if (type instanceof IAIFTypeReference) {
				children = new Variable[1];
				String ch_fn = "(" + fn + ")->" + ((IAIFTypeReference) type).getName(); //$NON-NLS-1$ //$NON-NLS-2$
				String ch_n = ((IAIFTypeReference) type).getName();
				String ch_k = varId + "." + ch_n; //$NON-NLS-1$
				IPDIVariable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(),
						getStackDepth(), ch_k);
				v.setAIF(AIFFactory.newAIF(type, value));
				children[0] = v;
			} else if (type instanceof IAIFTypeAggregate) {
				IAIFTypeAggregate aggrType = (IAIFTypeAggregate) type;
				String[] fieldNames = aggrType.getFieldNames(IAIFTypeAggregate.AIF_CLASS_ACCESS_ALL);
				IAIFType[] fieldTypes = aggrType.getFieldTypes(IAIFTypeAggregate.AIF_CLASS_ACCESS_ALL);
				children = new Variable[fieldNames.length];
				for (int i = 0; i < children.length; i++) {
					String ch_fn = "(" + fn + ")." + fieldNames[i]; //$NON-NLS-1$ //$NON-NLS-2$
					String ch_n = fieldNames[i];
					String ch_k = varId + "." + ch_n; //$NON-NLS-1$
					IPDIVariable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(),
							getStackDepth(), ch_k);
					IAIFValue val = AIFFactory.UNKNOWNVALUE;
					if (value instanceof IAIFValueAggregate) {
						val = ((IAIFValueAggregate) value).getFieldValue(fieldNames[i]);
					}
					v.setAIF(AIFFactory.newAIF(fieldTypes[i], val));
					children[i] = v;
				}
			} else if (type instanceof IAIFTypeUnion) {
				IAIFTypeUnion unionType = (IAIFTypeUnion) type;
				String[] fieldNames = unionType.getFieldNames();
				IAIFType[] fieldTypes = unionType.getFieldTypes();
				children = new Variable[fieldNames.length];
				for (int i = 0; i < children.length; i++) {
					String ch_fn = "(" + fn + ")." + fieldNames[i]; //$NON-NLS-1$ //$NON-NLS-2$
					String ch_n = fieldNames[i];
					String ch_k = varId + "." + ch_n; //$NON-NLS-1$
					IPDIVariable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(),
							getStackDepth(), ch_k);
					IAIFValue val = AIFFactory.UNKNOWNVALUE;
					if (value instanceof IAIFValueUnion) {
						val = ((IAIFValueUnion) value).getFieldValue(fieldNames[i]);
					}
					v.setAIF(AIFFactory.newAIF(fieldTypes[i], val));
					children[i] = v;
				}
			} else {
				children = new Variable[1];
				String ch_fn = fn;
				String ch_n = ch_varid;
				String ch_k = varId + "." + ch_n; //$NON-NLS-1$
				IPDIVariable v = createVariable(session, getTasks(), fThread, fStackFrame, ch_n, ch_fn, getPosition(),
						getStackDepth(), ch_k);
				v.setAIF(AIFFactory.newAIF(type, value));
				children[0] = v;
			}
			return children;
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#getChildren(int,
	 * int)
	 */
	public IPDIVariable[] getChildren(int findex, int psize) throws PDIException {
		IPDIVariableDescriptor vo = getVariableDescriptorAsArray(findex, psize);
		IPDIVariable var = session.getVariableManager().createVariable(vo);
		return var.getChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#getChildrenNumber()
	 */
	public int getChildrenNumber() throws PDIException {
		IAIF a = getAIF();
		if (a.getType() instanceof IAIFTypeArray) {
			IAIFTypeRange range = ((IAIFTypeArray) a.getType()).getRange();
			return range.getSize();
		}
		if (a.getType() instanceof IAIFTypeAggregate) {
			return ((IAIFTypeAggregate) a.getType()).getFieldNames(IAIFTypeAggregate.AIF_CLASS_ACCESS_ALL).length;
		}
		if (a.getType() instanceof IAIFTypeUnion) {
			return ((IAIFTypeUnion) a.getType()).getFieldNames().length;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#isEditable()
	 */
	public boolean isEditable() throws PDIException {
		if (editable == null) {
			throw new PDIException(getTasks(), Messages.Variable_0);
		}
		return (editable == null) ? false : editable.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#resetValue()
	 */
	public void resetValue() {
		setAIF(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariable#setValue(java.lang.
	 * String)
	 */
	public void setValue(String expression) throws PDIException {
		throw new PDIException(getTasks(), Messages.Variable_1);
	}

	/**
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @param varId
	 * @return
	 */
	protected abstract IPDIVariable createVariable(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame frame,
			String name, String fullName, int pos, int depth, String varId);
}
