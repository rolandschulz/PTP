/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Oct 18, 2004
 */
package org.eclipse.cldt.internal.core.parser.scanner2;

import org.eclipse.cldt.core.parser.IProblem;
import org.eclipse.cldt.core.parser.ISourceElementRequestor;
import org.eclipse.cldt.core.parser.ast.IASTInclusion;
import org.eclipse.cldt.core.parser.ast.IASTMacro;
import org.eclipse.cldt.internal.core.parser.scanner2.BaseScanner.InclusionData;

/**
 * @author aniefer
 */
public class ScannerCallbackManager {
    private static final int bufferInitialSize = 8;
    
	private Object[] callbackStack = new Object[bufferInitialSize];
	private int callbackPos = -1;
	private ISourceElementRequestor requestor;
	
	public ScannerCallbackManager( ISourceElementRequestor requestor ){
	    this.requestor = requestor;
	}
	
	public void pushCallback( Object obj ){
	    if( ++callbackPos == callbackStack.length ){
	        Object[] temp = new Object[ callbackStack.length << 1 ];
	        System.arraycopy( callbackStack, 0, temp, 0, callbackStack.length );
	        callbackStack = temp;
	    }
	    callbackStack[ callbackPos ] = obj;
	}
	
	public void popCallbacks(){
	    Object obj = null;
	    for( int i = 0; i <= callbackPos; i++ ){
	        obj = callbackStack[i];
	        if( obj == null ) continue;
	        //on the stack, InclusionData means enter, IASTInclusion means exit
	        if( obj instanceof InclusionData && ((InclusionData)obj).inclusion != null )
	            requestor.enterInclusion( (IASTInclusion) ((InclusionData)obj).inclusion );
	        else if( obj instanceof IASTInclusion )
	            requestor.exitInclusion( (IASTInclusion) obj );
	        else if( obj instanceof IASTMacro )
	            requestor.acceptMacro( (IASTMacro) obj );
	        else if( obj instanceof IProblem )
	    		requestor.acceptProblem( (IProblem) obj );
	    }
	    callbackPos = -1;   
	}
	
	public boolean hasCallbacks(){
	    return callbackPos != -1;
	}
}
