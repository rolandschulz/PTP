/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.binding;

import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.intrinsics.IntrinsicProcDescription;
import org.eclipse.photran.internal.core.intrinsics.Intrinsics;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Collection of all intrinsic functions in Fortran.  A static method
 * is provided to attempt to resolve a Token to an intrinsic procedure.
 * 
 * @author Jeff Overbey
 */
public class Intrinsic extends Definition
{
    private static final long serialVersionUID = 1L;

    public Intrinsic(String declaredName, PhotranTokenRef tokenRef)
    {
    	super(declaredName, tokenRef, Definition.Classification.INTRINSIC, /*Visibility.PUBLIC,*/ Type.UNKNOWN);
    }

    @Override
    public boolean isExternallyVisibleSubprogramDefinition()
    {
        return false;
    }

    @Override
    public boolean isSubprogram()
    {
        return true;
    }

//        @Override
//        public boolean isExternal()
//        {
//            return false;
//        }

    @Override
    public String describe()
    {
        String canonicalizedName = PhotranVPG.canonicalizeIdentifier(declaredName);
        
        IntrinsicProcDescription proc = Intrinsics.get(canonicalizedName);
        if (proc == null)
            return ""; //$NON-NLS-1$
        else
            return proc.toString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BINDING RESOLUTION
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** @return all Definitions in this scope to which the given name resolves */
    public static Definition resolve(Token identifier)
    {
        String canonicalizedName = PhotranVPG.canonicalizeIdentifier(identifier.getText());
        
        for (IntrinsicProcDescription intrinsic : Intrinsics.getAllIntrinsicProcedures())
           if (PhotranVPG.canonicalizeIdentifier(intrinsic.genericName).equals(canonicalizedName))
               return new Intrinsic(identifier.getText(), identifier.getTokenRef());

        return null;
    }
}
