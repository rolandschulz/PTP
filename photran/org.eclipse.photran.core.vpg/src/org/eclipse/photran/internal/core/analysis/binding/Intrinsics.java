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

import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;

/**
 * Collection of all intrinsic functions in Fortran.  A static method
 * is provided to attempt to resolve a Token to an intrinsic procedure.
 * 
 * @author Jeff Overbey
 */
public class Intrinsics
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BINDING RESOLUTION
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** @return all Definitions in this scope to which the given name resolves */
    public static Definition resolveIntrinsic(Token identifier)
    {
    	String canonicalizedName = PhotranVPG.canonicalizeIdentifier(identifier.getText());
    	
        for (String intrinsic : intrinsics)
           if (intrinsic.matches(canonicalizedName))
               return new Intrinsic(identifier.getText(), identifier.getTokenRef());

        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INTRINSICS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // All names here are canonicalized according to PhotranVPG.canonicalizeIdentifier
    public static final String[] intrinsics =
    {
        // From Metcalf and Reid, "Fortran 90/95 Explained", Chapter 8
        // Functions
        "associated",
        "present",
        "kind",
        "abs",
        "aimag",
        "aint",
        "anint",
        "ceiling",
        "cmplx",
        "floor",
        "int",
        "nint",
        "real",
        "conjg",
        "dim",
        "max",
        "min",
        "mod",
        "modulo",
        "sign",
        "acos",
        "asin",
        "atan",
        "atan2",
        "cos",
        "cosh",
        "exp",
        "log",
        "log10",
        "sin",
        "sinh",
        "sqrt",
        "tan",
        "tanh",
        "achar",
        "char",
        "iachar",
        "ichar",
        "lge",
        "lgt",
        "lle",
        "llt",
        "adjustl",
        "adjustr",
        "index",
        "len_trim",
        "scan",
        "verify",
        "logical",
        "len",
        "repeat",
        "trim",
        "digits",
        "epsilon",
        "huge",
        "maxexponent",
        "minexponent",
        "precision",
        "radix",
        "range",
        "tiny",
        "exponent",
        "fraction",
        "nearest",
        "rrspacing",
        "scale",
        "set_exponent",
        "spacing",
        "selected_int_kind",
        "selected_real_kind",
        "bit_size",
        "btest",
        "iand",
        "ibclr",
        "ibits",
        "ibset",
        "ieor",
        "ior",
        "ishft",
        "ishftc",
        "not",
        "dot_product",
        "matmul",
        "all",
        "any",
        "count",
        "maxval",
        "minval",
        "product",
        "sum",
        "allocated",
        "lbound",
        "shape",
        "size",
        "ubound",
        "merge",
        "pack",
        "unpack",
        "reshape",
        "spread",
        "cshift",
        "eoshift",
        "transpose",
        "maxloc",
        "minloc",
        "null",

        // Subroutines
        "mvbits",
        "date_and_time",
        "system_clock",
        "cpu_time",
        "random_number",
        "random_seed"
    };

    /**
     * A <code>Binding</code> representing an intrinsic subprogram, such as <code>sin</code> or
     * <code>selected_int_kind</code>.
     */
    private static class Intrinsic extends Definition
    {
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
    }
}
