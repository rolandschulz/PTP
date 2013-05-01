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
package org.eclipse.ptp.internal.debug.core.model;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.PVariableFormat;
import org.eclipse.ptp.debug.core.pdi.IPDIFormat;
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
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public abstract class PVariable extends AbstractPVariable implements IPDIEventListener {
	public interface IInternalVariable {
		/**
		 * @param start
		 * @param length
		 * @return
		 * @throws DebugException
		 */
		public IInternalVariable createShadow(int start, int length) throws DebugException;

		/**
		 * @param type
		 * @return
		 * @throws DebugException
		 */
		public IInternalVariable createShadow(String type) throws DebugException;

		/**
		 * @param destroy
		 */
		public void dispose(boolean destroy);

		/**
		 * @return
		 * @throws DebugException
		 */
		public String getQualifiedName() throws DebugException;

		/**
		 * @return
		 * @throws DebugException
		 */
		public IPValue getValue() throws DebugException;

		/**
		 * 
		 */
		public void invalidateValue();

		/**
		 * @return
		 */
		public boolean isArgument();

		/**
		 * @return
		 */
		public boolean isChanged();

		/**
		 * @return
		 * @throws DebugException
		 */
		public boolean isEditable() throws DebugException;

		/**
		 * @param desc
		 * @return
		 */
		public boolean isSameDescriptor(IPDIVariableDescriptor desc);

		/**
		 * @param pdiVar
		 * @return
		 */
		public boolean isSameVariable(IPDIVariable pdiVar);

		/**
		 * 
		 */
		public void preserve();

		/**
		 * 
		 */
		public void resetValue();

		/**
		 * @param changed
		 */
		public void setChanged(boolean changed);

		/**
		 * @param expression
		 * @throws DebugException
		 */
		public void setValue(String expression) throws DebugException;

		/**
		 * @return
		 */
		public int sizeof();
	}

	private PVariableFormat fFormat = PVariableFormat.getFormat(Platform.getPreferencesService().getInt(
			PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, IPDIFormat.NATURAL, null));
	private boolean fIsDisposed = false;
	private boolean fIsEnabled = true;
	private String fName;
	private IInternalVariable fOriginal;
	private IInternalVariable fShadow;

	protected PVariable(PDebugElement parent, IPDIVariableDescriptor pdiVariableObject) {
		super(parent);
		if (pdiVariableObject != null) {
			setName(pdiVariableObject.getName());
			createOriginal(pdiVariableObject);
		}
		fIsEnabled = (parent instanceof AbstractPValue) ? ((AbstractPValue) parent).getParentVariable().isEnabled()
				: !isBookkeepingEnabled();
		getPDISession().getEventManager().addEventListener(this);
	}

	protected PVariable(PDebugElement parent, IPDIVariableDescriptor pdiVariableObject, String errorMessage) {
		super(parent);
		if (pdiVariableObject != null) {
			setName(pdiVariableObject.getName());
			createOriginal(pdiVariableObject);
		}
		fIsEnabled = !isBookkeepingEnabled();
		setStatus(IPDebugElementStatus.ERROR, NLS.bind(Messages.PVariable_0, new Object[] { errorMessage }));
		getPDISession().getEventManager().addEventListener(this);
	}

	/**
	 * @return
	 */
	public boolean canCast() {
		return (getOriginal() != null && isEnabled());
	}

	/**
	 * @return
	 */
	public boolean canCastToArray() {
		try {
			return (getOriginal() != null && isEnabled() && (getAIF().getType() instanceof IAIFTypePointer));
		} catch (final DebugException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IEnableDisableTarget#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return !(getParent() instanceof IValue);
	}

	/**
	 * @param startIndex
	 * @param length
	 * @throws DebugException
	 */
	public void castToArray(int startIndex, int length) throws DebugException {
		final IInternalVariable current = getCurrentInternalVariable();
		if (current != null) {
			final IInternalVariable newVar = current.createShadow(startIndex, length);
			if (getShadow() != null) {
				getShadow().dispose(true);
			}
			setShadow(newVar);
			resetValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IFormatSupport#changeFormat(org.eclipse
	 * .ptp.debug.core.model.PVariableFormat)
	 */
	public void changeFormat(PVariableFormat format) throws DebugException {
		setFormat(format);
		resetValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.AbstractPVariable#dispose()
	 */
	@Override
	public void dispose() {
		internalDispose(false);
		setDisposed(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PVariable) {
			if (isDisposed() != ((PVariable) obj).isDisposed()) {
				return false;
			}
			final IInternalVariable iv = getOriginal();
			return (iv != null) ? iv.equals(((PVariable) obj).getOriginal()) : false;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPVariable#getExpressionString()
	 */
	public String getExpressionString() throws DebugException {
		final IInternalVariable iv = getCurrentInternalVariable();
		return (iv != null) ? iv.getQualifiedName() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IFormatSupport#getFormat()
	 */
	public PVariableFormat getFormat() {
		return fFormat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		final IAIF aif = getAIF();
		return (aif != null) ? aif.getType().toString() : ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IPValue getValue() throws DebugException {
		if (!isDisposed() && isEnabled()) {
			final IInternalVariable iv = getCurrentInternalVariable();
			if (iv != null) {
				try {
					return iv.getValue();
				} catch (final DebugException e) {
					setStatus(IPDebugElementStatus.ERROR, e.getMessage());
				}
			}
		}
		return PValueFactory.NULL_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
		for (final IPDIEvent event2 : events) {
			final IPDIEvent event = event2;
			if (!event.contains(getTasks())) {
				continue;
			}

			if (event instanceof IPDIChangedEvent) {
				handleChangedEvent((IPDIChangedEvent) event);
			} else if (event instanceof IPDIResumedEvent) {
				handleResumedEvent((IPDIResumedEvent) event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		if (isDisposed()) {
			return false;
		}
		final IInternalVariable iv = getCurrentInternalVariable();
		return (iv != null) ? iv.isChanged() : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPVariable#isArgument()
	 */
	public boolean isArgument() {
		final IInternalVariable iv = getOriginal();
		return (iv != null) ? iv.isArgument() : false;
	}

	/**
	 * @return
	 */
	public boolean isCasted() {
		return (getShadow() != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IEnableDisableTarget#isEnabled()
	 */
	public boolean isEnabled() {
		return fIsEnabled;
	}

	/**
	 * @throws DebugException
	 */
	public void restoreOriginal() throws DebugException {
		final IInternalVariable oldVar = getShadow();
		setShadow(null);
		if (oldVar != null) {
			oldVar.dispose(true);
		}
		final IInternalVariable iv = getOriginal();
		if (iv != null) {
			iv.invalidateValue();
		}
		resetValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IEnableDisableTarget#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) throws DebugException {
		IInternalVariable iv = getOriginal();
		if (iv != null) {
			iv.dispose(true);
		}
		iv = getShadow();
		if (iv != null) {
			iv.dispose(true);
		}
		fIsEnabled = enabled;
		fireChangeEvent(DebugEvent.STATE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.
	 * debug.core.model.IValue)
	 */
	public void setValue(IValue value) throws DebugException {
		notSupported(Messages.PVariable_1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String
	 * )
	 */
	public void setValue(String expression) throws DebugException {
		final IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			final String newExpression = processExpression(expression);
			iv.setValue(newExpression);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IFormatSupport#supportsFormatting()
	 */
	public boolean supportsFormatting() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#supportsValueModification
	 * ()
	 */
	public boolean supportsValueModification() {
		try {
			return getCurrentInternalVariable().isEditable();
		} catch (final DebugException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse
	 * .debug.core.model.IValue)
	 */
	public boolean verifyValue(IValue value) throws DebugException {
		return value.getDebugTarget().equals(getDebugTarget());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang
	 * .String)
	 */
	public boolean verifyValue(String expression) throws DebugException {
		return true;
	}

	/**
	 * @return
	 */
	private IInternalVariable getCurrentInternalVariable() {
		if (getShadow() != null) {
			return getShadow();
		}
		return getOriginal();
	}

	/**
	 * @return
	 */
	private IInternalVariable getOriginal() {
		return fOriginal;
	}

	/**
	 * @return
	 */
	private IInternalVariable getShadow() {
		return fShadow;
	}

	/**
	 * @param event
	 */
	private void handleChangedEvent(IPDIChangedEvent event) {
		final IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIVariableInfo) {
			final IInternalVariable iv = getCurrentInternalVariable();
			if (iv != null) {
				if (iv.isSameVariable(((IPDIVariableInfo) reason).getVariable())) {
					iv.setChanged(true);
					fireChangeEvent(DebugEvent.STATE);
				}
			}
		}
	}

	/**
	 * @param event
	 */
	private void handleResumedEvent(IPDIResumedEvent event) {
		boolean changed = false;
		if (hasErrors()) {
			resetStatus();
			changed = true;
			final IInternalVariable iv = getCurrentInternalVariable();
			if (iv != null) {
				iv.invalidateValue();
			}
		}
		if (changed) {
			fireChangeEvent(DebugEvent.STATE);
		}
	}

	/**
	 * @param oldExpression
	 * @return
	 * @throws DebugException
	 */
	private String processExpression(String oldExpression) throws DebugException {
		return oldExpression;
	}

	/**
	 * @param shadow
	 */
	private void setShadow(IInternalVariable shadow) {
		fShadow = shadow;
	}

	/**
	 * @param vo
	 */
	protected abstract void createOriginal(IPDIVariableDescriptor vo);

	/**
	 * @return
	 */
	protected boolean hasErrors() {
		return !isOK();
	}

	/**
	 * @param destroy
	 */
	protected void internalDispose(boolean destroy) {
		getPDISession().getEventManager().removeEventListener(this);
		IInternalVariable iv = getOriginal();
		if (iv != null) {
			iv.dispose(destroy);
		}
		iv = getShadow();
		if (iv != null) {
			iv.dispose(destroy);
		}
	}

	/**
	 * 
	 */
	protected void invalidateValue() {
		resetStatus();
		final IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			iv.invalidateValue();
		}
	}

	/**
	 * @return
	 */
	protected boolean isBookkeepingEnabled() {
		final boolean result = false;
		return result;
	}

	/**
	 * @return
	 */
	protected boolean isDisposed() {
		return fIsDisposed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.AbstractPVariable#preserve()
	 */
	@Override
	protected void preserve() {
		resetStatus();
		final IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			iv.preserve();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.AbstractPVariable#resetValue()
	 */
	@Override
	protected void resetValue() {
		final IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			resetStatus();
			iv.resetValue();
			fireChangeEvent(DebugEvent.STATE);
		}
	}

	/**
	 * @param vo
	 * @return
	 */
	protected boolean sameVariable(IPDIVariableDescriptor vo) {
		final IInternalVariable iv = getOriginal();
		return (iv != null && iv.isSameDescriptor(vo));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.AbstractPVariable#setChanged
	 * (boolean)
	 */
	@Override
	protected void setChanged(boolean changed) {
		final IInternalVariable iv = getCurrentInternalVariable();
		if (iv != null) {
			iv.setChanged(changed);
		}
	}

	/**
	 * @param isDisposed
	 */
	protected void setDisposed(boolean isDisposed) {
		fIsDisposed = isDisposed;
	}

	/**
	 * @param format
	 */
	protected void setFormat(PVariableFormat format) {
		fFormat = format;
	}

	/**
	 * @param name
	 */
	protected void setName(String name) {
		fName = name;
	}

	/**
	 * @param original
	 */
	protected void setOriginal(IInternalVariable original) {
		fOriginal = original;
	}

	/**
	 * @return
	 */
	protected int sizeof() {
		final IInternalVariable iv = getCurrentInternalVariable();
		return (iv != null) ? iv.sizeof() : -1;
	}
}
