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
import org.eclipse.ptp.debug.core.cdi.event.IPCDIChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIObject;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariableDescriptor;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPType;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.PVariableFormat;

/**
 * @author Clement chu
 *
 */
public abstract class PVariable extends AbstractPVariable implements IPCDIEventListener {
	interface IInternalVariable {
		IInternalVariable createShadow(int start, int length) throws DebugException;
		IInternalVariable createShadow(String type) throws DebugException;
		PType getType() throws DebugException;
		String getQualifiedName() throws DebugException;
		IPValue getValue() throws DebugException;
		void setValue(String expression) throws DebugException;
		boolean isChanged();
		void setChanged(boolean changed);
		void dispose(boolean destroy);
		boolean isSameDescriptor(IPCDIVariableDescriptor desc);
		boolean isSameVariable(IPCDIVariable cdiVar);
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

	protected PVariable(PDebugElement parent, IPCDIVariableDescriptor cdiVariableObject) {
		super(parent);
		if (cdiVariableObject != null) {
			setName(cdiVariableObject.getName());
			createOriginal(cdiVariableObject);
		}
		fIsEnabled = (parent instanceof AbstractPValue) ? ((AbstractPValue) parent).getParentVariable().isEnabled() : !isBookkeepingEnabled();
		getCDISession().getEventManager().addEventListener(this);
	}
	protected PVariable(PDebugElement parent, IPCDIVariableDescriptor cdiVariableObject, String errorMessage) {
		super(parent);
		if (cdiVariableObject != null) {
			setName(cdiVariableObject.getName());
			createOriginal(cdiVariableObject);
		}
		fIsEnabled = !isBookkeepingEnabled();
		setStatus(IPDebugElementStatus.ERROR, MessageFormat.format(CoreModelMessages.getString("PVariable.1"), new String[] { errorMessage })); //$NON-NLS-1$
		getCDISession().getEventManager().addEventListener(this);
	}
	public IPType getType() throws DebugException {
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
		IPType type = getType();
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
	public PVariableFormat getFormat() {
		return fFormat;
	}
	public void changeFormat(PVariableFormat format) throws DebugException {
		setFormat(format);
		resetValue();
	}
	public boolean canCastToArray() {
		IPType type;
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
		notSupported(CoreModelMessages.getString("PVariable.3"));
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
	public void handleDebugEvents(IPCDIEvent[] events) {
		IInternalVariable iv = getCurrentInternalVariable();
		if (iv == null)
			return;
		for (int i = 0; i < events.length; i++) {
			IPCDIEvent event = events[i];
			IPCDIObject source = event.getSource(getCDITarget().getTargetID());
			if (source == null)
				continue;
			IPCDITarget target = source.getTarget();
			if (target.equals(getCDITarget())) {
				if (event instanceof IPCDIChangedEvent) {
					if (source instanceof IPCDIVariable && iv.isSameVariable((IPCDIVariable) source)) {
						handleChangedEvent((IPCDIChangedEvent) event);
					}
				} else if (event instanceof IPCDIResumedEvent) {
					handleResumedEvent((IPCDIResumedEvent) event);
				}
			}
		}
	}
	private void handleResumedEvent(IPCDIResumedEvent event) {
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
	private void handleChangedEvent(IPCDIChangedEvent event) {
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
	abstract protected void createOriginal(IPCDIVariableDescriptor vo);
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
		//TODO whether Hack: do not destroy local variables
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
	protected boolean sameVariable(IPCDIVariableDescriptor vo) {
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
