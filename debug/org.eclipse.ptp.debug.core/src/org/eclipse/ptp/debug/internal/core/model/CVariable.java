/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.model;

import java.text.MessageFormat;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * Represents a variable in the CDI model.
 */
public class CVariable extends AbstractCVariable implements ICDIEventListener {

	/**
	 * Represents a single CDI variable.
	 */
	private class InternalVariable {
		
		/**
		 * The enclosing <code>CVariable</code> instance.
		 */
		private CVariable fVariable;

		/**
		 * The CDI variable object this variable is based on.
		 */
		private ICDIVariableDescriptor fCDIVariableObject;

		/**
		 * The underlying CDI variable.
		 */
		private ICDIVariable fCDIVariable;

		/**
		 * The type of this variable.
		 */
		private CType fType;

		/**
		 * The expression used to eveluate the value of this variable.
		 */
		private String fQualifiedName;

		/**
		 * The cache of the current value.
		 */
		private ICValue fValue = CValueFactory.NULL_VALUE;

		/**
		 * The change flag.
		 */
		private boolean fChanged = false;

		/**
		 * Constructor for InternalVariable.
		 */
		InternalVariable( CVariable var, ICDIVariableDescriptor varObject ) {
			setVariable( var );
			setCDIVariableObject( varObject );
			setCDIVariable( (varObject instanceof ICDIVariable) ? (ICDIVariable)varObject : null );
		}

		InternalVariable createShadow( int start, int length ) throws DebugException {
			InternalVariable iv = null;
			try {
				iv = new InternalVariable( getVariable(), getCDIVariableObject().getVariableDescriptorAsArray( start, length ) );
			}
			catch( CDIException e ) {
				requestFailed( e.getMessage(), null );
			}
			return iv;
		}

		InternalVariable createShadow( String type ) throws DebugException {
			InternalVariable iv = null;
			try {
				iv = new InternalVariable( getVariable(), getCDIVariableObject().getVariableDescriptorAsType( type ) );
			}
			catch( CDIException e ) {
				requestFailed( e.getMessage(), null );
			}
			return iv;
		}

		private synchronized ICDIVariable getCDIVariable() throws DebugException {
			if ( fCDIVariable == null ) {
				try {
					fCDIVariable = getCDIVariableObject().createVariable( );
				}
				catch( CDIException e ) {
					requestFailed( e.getMessage(), null );
				}
			}
			return fCDIVariable;
		}

		private void setCDIVariable( ICDIVariable variable ) {
			fCDIVariable = variable;
		}

		private ICDIVariableDescriptor getCDIVariableObject() {
			if ( fCDIVariable != null ) {
				return fCDIVariable;
			}
			return fCDIVariableObject;
		}

		private void setCDIVariableObject( ICDIVariableDescriptor variableObject ) {
			fCDIVariableObject = variableObject;
		}

		String getQualifiedName() throws DebugException {
			if ( fQualifiedName == null ) {
				try {
					fQualifiedName = (fCDIVariableObject != null) ? fCDIVariableObject.getQualifiedName() : null;
				}
				catch( CDIException e ) {
					requestFailed( e.getMessage(), null );
				}
			}
			return fQualifiedName;
		}

		CType getType() throws DebugException {
			if ( fType == null ) {
				ICDIVariableDescriptor varObject = getCDIVariableObject();
				if ( varObject != null ) {
					synchronized( this ) {
						if ( fType == null ) {
							try {
								fType = new CType( varObject.getType() );
							}
							catch( CDIException e ) {
								requestFailed( e.getMessage(), null );
							}
						}
					}
				}
			}
			return fType;
		}

		synchronized void invalidate( boolean destroy ) {
			try {
				if ( destroy && fCDIVariable != null )
					fCDIVariable.dispose();
			}
			catch( CDIException e ) {
				logError( e.getMessage() );
			}
			invalidateValue();
			setCDIVariable( null );
			if ( fType != null )
				fType.dispose();
			fType = null;
		}

		void dispose( boolean destroy ) {
			invalidate( destroy );
		}

		boolean isSameVariable( ICDIVariable cdiVar ) {
			return ( fCDIVariable != null ) ? fCDIVariable.equals( cdiVar ) : false;
		}

		int sizeof() {
			if ( getCDIVariableObject() != null ) {
				try {
					return getCDIVariableObject().sizeof();
				}
				catch( CDIException e ) {
				}
			}
			return 0;
		}

		boolean isArgument() {
			return ( getCDIVariableObject() instanceof ICDIArgumentDescriptor );
		}

		void setValue( String expression ) throws DebugException {
			ICDIVariable cdiVariable = null;
			try {
				cdiVariable = getCDIVariable();
				if ( cdiVariable != null )
					cdiVariable.setValue( expression );
				else
					requestFailed( CoreModelMessages.getString( "CModificationVariable.0" ), null ); //$NON-NLS-1$
			}
			catch( CDIException e ) {
				targetRequestFailed( e.getMessage(), null );
			}
		}

		void setValue( ICValue value ) {
			fValue = value;
		}

		synchronized ICValue getValue() throws DebugException {
			if ( fValue.equals( CValueFactory.NULL_VALUE ) ) {
				ICDIVariable var = getCDIVariable();
				if ( var != null ) {
					try {
						ICDIValue cdiValue = var.getValue();
						if ( cdiValue != null ) {
							ICDIType cdiType = cdiValue.getType();
							if ( cdiValue instanceof ICDIArrayValue && cdiType != null ) {
								ICType type = new CType( cdiType );
								if ( type.isArray() ) {
									int[] dims = type.getArrayDimensions();
									if ( dims.length > 0 && dims[0] > 0 )
										fValue = CValueFactory.createIndexedValue( getVariable(), (ICDIArrayValue)cdiValue, 0, dims[0] );
								}
							}
							else {
								fValue = CValueFactory.createValue( getVariable(), cdiValue );
							}
						}
					}
					catch( CDIException e ) {
						requestFailed( e.getMessage(), e );
					}
				}
			}
			return fValue;
		}
		
		void invalidateValue() {
			if ( fValue instanceof AbstractCValue ) {
				((AbstractCValue)fValue).dispose();
				fValue = CValueFactory.NULL_VALUE;
			}
		}

		boolean isChanged() {
			return fChanged;
		}

		synchronized void setChanged( boolean changed ) {
			if ( changed ) {
				invalidateValue();
			}
			if ( fValue instanceof AbstractCValue ) {
				((AbstractCValue)fValue).setChanged( changed );
			}
			fChanged = changed;
		}

		synchronized void preserve() {
			setChanged( false );
			if ( fValue instanceof AbstractCValue ) {
				((AbstractCValue)fValue).preserve();
			}
		}

		CVariable getVariable() {
			return fVariable;
		}

		private void setVariable( CVariable variable ) {
			fVariable = variable;
		}

		void resetValue() {
			if ( fValue instanceof AbstractCValue ) {
				((AbstractCValue)fValue).reset();
			}
		}

		boolean isEditable() throws DebugException {
			ICDIVariable var = getCDIVariable();
			if ( var != null ) {
				try {
					return var.isEditable();
				}
				catch( CDIException e ) {
				}
			}
			return false;
		}
		/**
		 * Compares the underlying variable objects.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals( Object obj ) {
			if ( obj instanceof InternalVariable ) {
				return getCDIVariableObject().equals( ((InternalVariable)obj).getCDIVariableObject() );
			}
			return false;
		}

		boolean sameVariable( ICDIVariableDescriptor vo ) {
			return getCDIVariableObject().equals( vo );
		}
	}

	/**
	 * Whether this variable is currently enabled.
	 */
	private boolean fIsEnabled = true;

	/**
	 * The original internal variable.
	 */
	private InternalVariable fOriginal;

	/**
	 * The shadow internal variable used for casting.
	 */
	private InternalVariable fShadow;

	/**
	 * The name of this variable.
	 */
	private String fName;

	/**
	 * The current format of this variable.
	 */
	private CVariableFormat fFormat = CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT ) );

	/**
	 * Whether this variable has been disposed.
	 */
	private boolean fIsDisposed = false;

	/**
	 * Constructor for CVariable.
	 */
	protected CVariable( PDebugElement parent, ICDIVariableDescriptor cdiVariableObject ) {
		super( parent );
		if ( cdiVariableObject != null ) {
			fName = cdiVariableObject.getName();
			createOriginal( cdiVariableObject );
		}
		fIsEnabled = ( parent instanceof AbstractCValue ) ? ((AbstractCValue)parent).getParentVariable().isEnabled() : !isBookkeepingEnabled();
		getCDISession().getEventManager().addEventListener( this );
	}

	/**
	 * Constructor for CVariable.
	 */
	protected CVariable( PDebugElement parent, ICDIVariableDescriptor cdiVariableObject, String errorMessage ) {
		super( parent );
		if ( cdiVariableObject != null ) {
			fName = cdiVariableObject.getName();
			createOriginal( cdiVariableObject );
		}
		fIsEnabled = !isBookkeepingEnabled();
		setStatus( ICDebugElementStatus.ERROR, MessageFormat.format( CoreModelMessages.getString( "CVariable.1" ), new String[]{ errorMessage } ) ); //$NON-NLS-1$
		getCDISession().getEventManager().addEventListener( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getType()
	 */
	public ICType getType() throws DebugException {
		if ( isDisposed() )
			return null;
		InternalVariable iv = getCurrentInternalVariable();
		return ( iv != null ) ? iv.getType() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEnabled()
	 */
	public boolean isEnabled() {
		return fIsEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#setEnabled(boolean)
	 */
	public void setEnabled( boolean enabled ) throws DebugException {
		InternalVariable iv = getOriginal();
		if ( iv != null )
			iv.dispose( true );
		iv = getShadow();
		if ( iv != null )
			iv.dispose( true );
		fIsEnabled = enabled;
		fireChangeEvent( DebugEvent.STATE );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return !( getParent() instanceof IValue );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isArgument()
	 */
	public boolean isArgument() {
		InternalVariable iv = getOriginal();
		return ( iv != null ) ? iv.isArgument() : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		if ( !isDisposed() && isEnabled() ) {
			InternalVariable iv = getCurrentInternalVariable();
			if ( iv != null ) {
				try {
					return iv.getValue();
				}
				catch( DebugException e ) {
					setStatus( ICDebugElementStatus.ERROR, e.getMessage() );
				}
			}
		}
		return CValueFactory.NULL_VALUE;
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
		ICType type = getType();
		return ( type != null ) ? type.getName() : ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		if ( isDisposed() )
			return false;
		InternalVariable iv = getCurrentInternalVariable(); 
		return ( iv != null ) ? iv.isChanged() : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#supportsFormatting()
	 */
	public boolean supportsFormatting() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#getFormat()
	 */
	public CVariableFormat getFormat() {
		return fFormat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#changeFormat(org.eclipse.cdt.debug.core.model.CVariableFormat)
	 */
	public void changeFormat( CVariableFormat format ) throws DebugException {
		setFormat( format );
		resetValue();
	}

	/*
	 * (non-Javadoc)
	 * Allow this operation only for the pointer types (???).
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToArray#canCastToArray()
	 */
	public boolean canCastToArray() {
		ICType type;
		try {
			type = getType();
			return ( getOriginal() != null && isEnabled() && type.isPointer() );
		}
		catch( DebugException e ) {
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToArray#castToArray(int, int)
	 */
	public void castToArray( int startIndex, int length ) throws DebugException {
		InternalVariable current = getCurrentInternalVariable();
		if ( current != null ) {
			InternalVariable newVar = current.createShadow( startIndex, length );
			if ( getShadow() != null )
				getShadow().dispose( true );
			setShadow( newVar );
			// If casting of variable to a type or array causes an error, the status 
			// of the variable is set to "error" and it can't be reset by subsequent castings.
			resetValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public void setValue( String expression ) throws DebugException {
		InternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			String newExpression = processExpression( expression );
			iv.setValue( newExpression );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue( IValue value ) throws DebugException {
		notSupported( CoreModelMessages.getString( "CVariable.3" ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		try {
			return getCurrentInternalVariable().isEditable();
		}
		catch( DebugException e ) {
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue( String expression ) throws DebugException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue( IValue value ) throws DebugException {
		return value.getDebugTarget().equals( getDebugTarget() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#canCast()
	 */
	public boolean canCast() {
		return ( getOriginal() != null && isEnabled() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#getCurrentType()
	 */
	public String getCurrentType() {
		String typeName = ""; //$NON-NLS-1$
		try {
			typeName = getReferenceTypeName();
		}
		catch( DebugException e ) {
		}
		return typeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#cast(java.lang.String)
	 */
	public void cast( String type ) throws DebugException {
		InternalVariable current = getCurrentInternalVariable();
		if ( current != null ) {
			InternalVariable newVar = current.createShadow( type );
			if ( getShadow() != null )
				getShadow().dispose( true );
			setShadow( newVar );
			// If casting of variable to a type or array causes an error, the status 
			// of the variable is set to "error" and it can't be reset by subsequent castings.
			resetValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#restoreOriginal()
	 */
	public void restoreOriginal() throws DebugException {
		InternalVariable oldVar = getShadow();
		setShadow( null );
		if ( oldVar != null )
			oldVar.dispose( true );
		InternalVariable iv = getOriginal();
		if ( iv != null )
			iv.invalidateValue();
		// If casting of variable to a type or array causes an error, the status 
		// of the variable is set to "error" and it can't be reset by subsequent castings.
		resetValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#isCasted()
	 */
	public boolean isCasted() {
		return ( getShadow() != null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		InternalVariable iv = getCurrentInternalVariable();
		if ( iv == null )
			return;
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source == null )
				continue;
			ICDITarget target = source.getTarget();
			if ( target.equals( getCDITarget() ) ) {
				if ( event instanceof ICDIChangedEvent ) {
					if ( source instanceof ICDIVariable && iv.isSameVariable( (ICDIVariable)source ) ) {
						handleChangedEvent( (ICDIChangedEvent)event );
					}
				}
				else if ( event instanceof ICDIResumedEvent ) {
					handleResumedEvent( (ICDIResumedEvent)event );
				}
			}
		}
	}

	private void handleResumedEvent( ICDIResumedEvent event ) {
		boolean changed = false;
		if ( hasErrors() ) {
			resetStatus();
			changed = true;
			InternalVariable iv = getCurrentInternalVariable();
			if ( iv != null )
				iv.invalidateValue();
		}
		if ( changed )
			fireChangeEvent( DebugEvent.STATE );
	}

	private void handleChangedEvent( ICDIChangedEvent event ) {
		InternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			iv.setChanged( true );
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	private InternalVariable getCurrentInternalVariable() {
		if ( getShadow() != null )
			return getShadow();
		return getOriginal();
	}

	private InternalVariable getOriginal() {
		return fOriginal;
	}

	private void setOriginal( InternalVariable original ) {
		fOriginal = original;
	}

	private InternalVariable getShadow() {
		return fShadow;
	}

	private void setShadow( InternalVariable shadow ) {
		fShadow = shadow;
	}

	protected boolean isBookkeepingEnabled() {
		boolean result = false;
		try {
			result = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false );
		}
		catch( CoreException e ) {
		}
		return result;
	}

	private void createOriginal( ICDIVariableDescriptor vo ) {
		if ( vo != null )
			fName = vo.getName();
		setOriginal( new InternalVariable( this, vo ) );
	}

	protected boolean hasErrors() {
		return !isOK();
	}

	protected void setChanged( boolean changed ) {
		InternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			iv.setChanged( changed );
		}
	}

	protected void resetValue() {
		InternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			resetStatus();
			iv.resetValue();
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	private String processExpression( String oldExpression ) throws DebugException {
		return oldExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#dispose()
	 */
	public void dispose() {
		// Hack: do not destroy local variables
		internalDispose( false );
		setDisposed( true );
	}

	protected int sizeof() {
		InternalVariable iv = getCurrentInternalVariable(); 
		return ( iv != null ) ? iv.sizeof() : -1;
	}

	/**
	 * Compares the original internal variables.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj ) {
		if ( obj instanceof CVariable ) {
			InternalVariable iv = getOriginal();
			return ( iv != null ) ? iv.equals( ((CVariable)obj).getOriginal() ) : false;
		}
		return false;
	}

	protected boolean sameVariable( ICDIVariableDescriptor vo ) {
		InternalVariable iv = getOriginal();
		return ( iv != null && iv.sameVariable( vo ) );
	}

	protected void setFormat( CVariableFormat format ) {
		fFormat = format;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getExpressionString()
	 */
	public String getExpressionString() throws DebugException {
		InternalVariable iv = getCurrentInternalVariable(); 
		return ( iv != null ) ? iv.getQualifiedName() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#preserve()
	 */
	protected void preserve() {
		resetStatus();
		InternalVariable iv = getCurrentInternalVariable(); 
		if ( iv != null )
			iv.preserve();
	}

	protected void internalDispose( boolean destroy ) {
		getCDISession().getEventManager().removeEventListener( this );
		InternalVariable iv = getOriginal();
		if ( iv != null )
			iv.dispose( destroy );
		iv = getShadow();
		if ( iv != null )
			iv.dispose( destroy );
	}
	
	protected boolean isDisposed() {
		return fIsDisposed;
	}

	protected void setDisposed( boolean isDisposed ) {
		fIsDisposed = isDisposed;
	}
}
