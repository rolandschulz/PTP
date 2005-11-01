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
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;

public abstract class CVariable extends AbstractCVariable implements ICDIEventListener {
	interface IInternalVariable {
		IInternalVariable createShadow(int start, int length) throws DebugException;
		IInternalVariable createShadow(String type) throws DebugException;
		CType getType() throws DebugException;
		String getQualifiedName() throws DebugException;
		ICValue getValue() throws DebugException;
		void setValue(String expression) throws DebugException;
		boolean isChanged();
		void setChanged(boolean changed);
		void dispose(boolean destroy);
		boolean isSameDescriptor(ICDIVariableDescriptor desc);
		boolean isSameVariable(ICDIVariable cdiVar);
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
	private CVariableFormat fFormat = CVariableFormat.getFormat(PTPDebugCorePlugin.getDefault().getPluginPreferences().getInt(IPDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT));
	private boolean fIsDisposed = false;

	protected CVariable(PDebugElement parent, ICDIVariableDescriptor cdiVariableObject) {
		super(parent);
		if (cdiVariableObject != null) {
			setName(cdiVariableObject.getName());
			createOriginal(cdiVariableObject);
		}
		fIsEnabled = (parent instanceof AbstractCValue) ? ((AbstractCValue) parent).getParentVariable().isEnabled() : !isBookkeepingEnabled();
		getCDISession().getEventManager().addEventListener(this);
	}
	protected CVariable(PDebugElement parent, ICDIVariableDescriptor cdiVariableObject, String errorMessage) {
		super(parent);
		if (cdiVariableObject != null) {
			setName(cdiVariableObject.getName());
			createOriginal(cdiVariableObject);
		}
		fIsEnabled = !isBookkeepingEnabled();
		setStatus(ICDebugElementStatus.ERROR, MessageFormat.format(CoreModelMessages.getString("CVariable.1"), new String[] { errorMessage })); //$NON-NLS-1$
		getCDISession().getEventManager().addEventListener(this);
	}
	public ICType getType() throws DebugException {
		if (isDisposed())
			return null;
		IInternalVariable iv = getCurrentInternalVariable();
		return (iv != null) ? iv.getType() : null;
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
	public IValue getValue() throws DebugException {
		if (!isDisposed() && isEnabled()) {
			IInternalVariable iv = getCurrentInternalVariable();
			if (iv != null) {
				try {
					return iv.getValue();
				} catch (DebugException e) {
					setStatus(ICDebugElementStatus.ERROR, e.getMessage());
				}
			}
		}
		return CValueFactory.NULL_VALUE;
	}
	public String getName() throws DebugException {
		return fName;
	}
	public String getReferenceTypeName() throws DebugException {
		ICType type = getType();
		return (type != null) ? type.getName() : "";
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
	public CVariableFormat getFormat() {
		return fFormat;
	}
	public void changeFormat(CVariableFormat format) throws DebugException {
		setFormat(format);
		resetValue();
	}
	public boolean canCastToArray() {
		ICType type;
		try {
			type = getType();
			return (getOriginal() != null && isEnabled() && type.isPointer());
		} catch (DebugException e) {
		}
		return false;
	}
	public void castToArray(int startIndex, int length) throws DebugException {
		IInternalVariable current = getCurrentInternalVariable();
		if (current != null) {
			IInternalVariable newVar = current.createShadow(startIndex, length);
			if (getShadow() != null)
				getShadow().dispose(true);
			setShadow(newVar);
			// If casting of variable to a type or array causes an error, the status
			// of the variable is set to "error" and it can't be reset by subsequent castings.
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
		notSupported(CoreModelMessages.getString("CVariable.3"));
	}
	public boolean supportsValueModification() {
		try {
			return getCurrentInternalVariable().isEditable();
		} catch (DebugException e) {
		}
		return false;
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
	public String getCurrentType() {
		String typeName = "";
		try {
			typeName = getReferenceTypeName();
		} catch (DebugException e) {
		}
		return typeName;
	}
	public void cast(String type) throws DebugException {
		IInternalVariable current = getCurrentInternalVariable();
		if (current != null) {
			IInternalVariable newVar = current.createShadow(type);
			if (getShadow() != null)
				getShadow().dispose(true);
			setShadow(newVar);
			// If casting of variable to a type or array causes an error, the status
			// of the variable is set to "error" and it can't be reset by subsequent castings.
			resetValue();
		}
	}
	public void restoreOriginal() throws DebugException {
		IInternalVariable oldVar = getShadow();
		setShadow(null);
		if (oldVar != null)
			oldVar.dispose(true);
		IInternalVariable iv = getOriginal();
		if (iv != null)
			iv.invalidateValue();
		// If casting of variable to a type or array causes an error, the status
		// of the variable is set to "error" and it can't be reset by subsequent castings.
		resetValue();
	}
	public boolean isCasted() {
		return (getShadow() != null);
	}
	public void handleDebugEvents(ICDIEvent[] events) {
		IInternalVariable iv = getCurrentInternalVariable();
		if (iv == null)
			return;
		for (int i = 0; i < events.length; i++) {
			IPCDIEvent event = (IPCDIEvent) events[i];
			ICDIObject source = event.getSource(getCDITarget().getTargetID());
			if (source == null)
				continue;
			ICDITarget target = source.getTarget();
			if (target.equals(getCDITarget())) {
				// TODO - not implement ICDIChangedEvent yet
				if (event instanceof ICDIChangedEvent) {
					if (source instanceof ICDIVariable && iv.isSameVariable((ICDIVariable) source)) {
						handleChangedEvent((ICDIChangedEvent) event);
					}
				} else if (event instanceof ICDIResumedEvent) {
					handleResumedEvent((ICDIResumedEvent) event);
				}
			}
		}
	}
	private void handleResumedEvent(ICDIResumedEvent event) {
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
	private void handleChangedEvent(ICDIChangedEvent event) {
		IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			iv.setChanged(true);
			fireChangeEvent(DebugEvent.STATE);
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
		// try {
		// result = getLaunch().getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false);
		// } catch( CoreException e ) {}
		return result;
	}
	abstract protected void createOriginal(ICDIVariableDescriptor vo);
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
		// Hack: do not destroy local variables
		internalDispose(false);
		setDisposed(true);
	}
	protected int sizeof() {
		IInternalVariable iv = getCurrentInternalVariable();
		return (iv != null) ? iv.sizeof() : -1;
	}
	public boolean equals(Object obj) {
		if (obj instanceof CVariable) {
			IInternalVariable iv = getOriginal();
			return (iv != null) ? iv.equals(((CVariable) obj).getOriginal()) : false;
		}
		return false;
	}
	protected boolean sameVariable(ICDIVariableDescriptor vo) {
		IInternalVariable iv = getOriginal();
		return (iv != null && iv.isSameDescriptor(vo));
	}
	protected void setFormat(CVariableFormat format) {
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
		getCDISession().getEventManager().removeEventListener(this);
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
