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
package org.eclipse.ptp.debug.internal.core.model;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.PVariableFormat;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIVariableInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;

/**
 * @author Clement chu
 *
 */
public abstract class PVariable extends AbstractPVariable implements IPDIEventListener {
	interface IInternalVariable {
		IInternalVariable createShadow(int start, int length) throws DebugException;
		IInternalVariable createShadow(String type) throws DebugException;
		String getQualifiedName() throws DebugException;
		IPValue getValue() throws DebugException;
		void setValue(String expression) throws DebugException;
		boolean isChanged();
		void setChanged(boolean changed);
		void dispose(boolean destroy);
		boolean isSameDescriptor(IPDIVariableDescriptor desc);
		boolean isSameVariable(IPDIVariable pdiVar);
		void resetValue();
		boolean isEditable() throws DebugException;
		boolean isArgument();
		int sizeof();
		void invalidateValue();
		void preserve();
	}

	private boolean fIsEnabled = true;
	private IInternalVariable fOriginal;
	private IInternalVariable fShadow;
	private String fName;
	private PVariableFormat fFormat = PVariableFormat.getFormat(PTPDebugCorePlugin.getDefault().getPluginPreferences().getInt(IPDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT));
	private boolean fIsDisposed = false;

	protected PVariable(PDebugElement parent, IPDIVariableDescriptor pdiVariableObject) {
		super(parent);
		if (pdiVariableObject != null) {
			setName(pdiVariableObject.getName());
			createOriginal(pdiVariableObject);
		}
		fIsEnabled = (parent instanceof AbstractPValue) ? ((AbstractPValue) parent).getParentVariable().isEnabled() : !isBookkeepingEnabled();
		getPDISession().getEventManager().addEventListener(this);
	}
	protected PVariable(PDebugElement parent, IPDIVariableDescriptor pdiVariableObject, String errorMessage) {
		super(parent);
		if (pdiVariableObject != null) {
			setName(pdiVariableObject.getName());
			createOriginal(pdiVariableObject);
		}
		fIsEnabled = !isBookkeepingEnabled();
		setStatus(IPDebugElementStatus.ERROR, MessageFormat.format(CoreModelMessages.getString("PVariable.1"), new Object[] { errorMessage }));
		getPDISession().getEventManager().addEventListener(this);
	}
	public boolean isEnabled() {
		return fIsEnabled;
	}
	public void setEnabled(boolean enabled) throws DebugException {
		IInternalVariable iv = getOriginal();
		if (iv != null)
			iv.dispose(true);
		iv = getShadow();
		if (iv != null)
			iv.dispose(true);
		fIsEnabled = enabled;
		fireChangeEvent(DebugEvent.STATE);
	}
	public boolean canEnableDisable() {
		return !(getParent() instanceof IValue);
	}
	public boolean isArgument() {
		IInternalVariable iv = getOriginal();
		return (iv != null) ? iv.isArgument() : false;
	}
	public IPValue getValue() throws DebugException {
		if (!isDisposed() && isEnabled()) {
			IInternalVariable iv = getCurrentInternalVariable();
			if (iv != null) {
				try {
					return iv.getValue();
				} catch (DebugException e) {
					setStatus(IPDebugElementStatus.ERROR, e.getMessage());
				}
			}
		}
		return PValueFactory.NULL_VALUE;
	}
	public String getName() throws DebugException {
		return fName;
	}
	public String getReferenceTypeName() throws DebugException {
		IAIF aif = getAIF();
		return (aif != null) ? aif.getType().toString() : "";
	}
	public boolean hasValueChanged() throws DebugException {
		if (isDisposed())
			return false;
		IInternalVariable iv = getCurrentInternalVariable();
		return (iv != null) ? iv.isChanged() : false;
	}
	public boolean supportsFormatting() {
		return true;
	}
	public PVariableFormat getFormat() {
		return fFormat;
	}
	public void changeFormat(PVariableFormat format) throws DebugException {
		setFormat(format);
		resetValue();
	}
	public boolean canCastToArray() {
		try {
			return (getOriginal() != null && isEnabled() && (getAIF().getType() instanceof IAIFTypePointer));
		} catch (DebugException e) {
			return false;
		}
	}
	public void castToArray(int startIndex, int length) throws DebugException {
		IInternalVariable current = getCurrentInternalVariable();
		if (current != null) {
			IInternalVariable newVar = current.createShadow(startIndex, length);
			if (getShadow() != null)
				getShadow().dispose(true);
			setShadow(newVar);
			resetValue();
		}
	}
	public void setValue(String expression) throws DebugException {
		IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			String newExpression = processExpression(expression);
			iv.setValue(newExpression);
		}
	}
	public void setValue(IValue value) throws DebugException {
		notSupported(CoreModelMessages.getString("PVariable.3"));
	}
	public boolean supportsValueModification() {
		try {
			return getCurrentInternalVariable().isEditable();
		} catch (DebugException e) {
			return false;
		}
	}
	public boolean verifyValue(String expression) throws DebugException {
		return true;
	}
	public boolean verifyValue(IValue value) throws DebugException {
		return value.getDebugTarget().equals(getDebugTarget());
	}
	public boolean canCast() {
		return (getOriginal() != null && isEnabled());
	}
	public void restoreOriginal() throws DebugException {
		IInternalVariable oldVar = getShadow();
		setShadow(null);
		if (oldVar != null)
			oldVar.dispose(true);
		IInternalVariable iv = getOriginal();
		if (iv != null)
			iv.invalidateValue();
		resetValue();
	}
	public boolean isCasted() {
		return (getShadow() != null);
	}
	public void handleDebugEvents(IPDIEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			IPDIEvent event = events[i];
			if (!event.contains(getTasks()))
				continue;
			
			if (event instanceof IPDIChangedEvent) {
				handleChangedEvent((IPDIChangedEvent) event);
			}
			else if (event instanceof IPDIResumedEvent) {
				handleResumedEvent((IPDIResumedEvent)event);
			}
		}
	}
	private void handleResumedEvent(IPDIResumedEvent event) {
		boolean changed = false;
		if (hasErrors()) {
			resetStatus();
			changed = true;
			IInternalVariable iv = getCurrentInternalVariable();
			if (iv != null)
				iv.invalidateValue();
		}
		if (changed)
			fireChangeEvent(DebugEvent.STATE);
	}
	private void handleChangedEvent(IPDIChangedEvent event) {
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIVariableInfo) {
			IInternalVariable iv = getCurrentInternalVariable();
			if (iv != null) {
				if (iv.isSameVariable(((IPDIVariableInfo)reason).getVariable())) {
					iv.setChanged(true);
					fireChangeEvent(DebugEvent.STATE);
				}
			}
		}
	}
	private IInternalVariable getCurrentInternalVariable() {
		if (getShadow() != null)
			return getShadow();
		return getOriginal();
	}
	private IInternalVariable getOriginal() {
		return fOriginal;
	}
	protected void setOriginal(IInternalVariable original) {
		fOriginal = original;
	}
	private IInternalVariable getShadow() {
		return fShadow;
	}
	private void setShadow(IInternalVariable shadow) {
		fShadow = shadow;
	}
	protected boolean isBookkeepingEnabled() {
		boolean result = false;
		return result;
	}
	abstract protected void createOriginal(IPDIVariableDescriptor vo);
	protected boolean hasErrors() {
		return !isOK();
	}
	protected void setChanged(boolean changed) {
		IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			iv.setChanged(changed);
		}
	}
	protected void resetValue() {
		IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			resetStatus();
			iv.resetValue();
			fireChangeEvent(DebugEvent.STATE);
		}
	}
	private String processExpression(String oldExpression) throws DebugException {
		return oldExpression;
	}
	public void dispose() {
		internalDispose(false);
		setDisposed(true);
	}
	protected int sizeof() {
		IInternalVariable iv = getCurrentInternalVariable();
		return (iv != null) ? iv.sizeof() : -1;
	}
	public boolean equals(Object obj) {
		if (obj instanceof PVariable) {
			if (isDisposed() != ((PVariable)obj).isDisposed())
				return false;
			IInternalVariable iv = getOriginal();
			return (iv != null) ? iv.equals(((PVariable) obj).getOriginal()) : false;
		}
		return false;
	}
	protected boolean sameVariable(IPDIVariableDescriptor vo) {
		IInternalVariable iv = getOriginal();
		return (iv != null && iv.isSameDescriptor(vo));
	}
	protected void setFormat(PVariableFormat format) {
		fFormat = format;
	}
	public String getExpressionString() throws DebugException {
		IInternalVariable iv = getCurrentInternalVariable();
		return (iv != null) ? iv.getQualifiedName() : null;
	}
	protected void preserve() {
		resetStatus();
		IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null)
			iv.preserve();
	}
	protected void internalDispose(boolean destroy) {
		getPDISession().getEventManager().removeEventListener(this);
		IInternalVariable iv = getOriginal();
		if (iv != null)
			iv.dispose(destroy);
		iv = getShadow();
		if (iv != null)
			iv.dispose(destroy);
	}
	protected boolean isDisposed() {
		return fIsDisposed;
	}
	protected void setDisposed(boolean isDisposed) {
		fIsDisposed = isDisposed;
	}
	protected void invalidateValue() {
		resetStatus();
		IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null)
			iv.invalidateValue();
	}
	protected void setName(String name) {
		fName = name;
	}
}
