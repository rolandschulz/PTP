/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.binding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.vpg.IPhotranSerializable;
import org.eclipse.photran.internal.core.vpg.PhotranVPGSerializer;

/**
 * Enumeration of ways in which a variable may be accessed within an {@link IExecutionPartConstruct}.
 * 
 * @author Jeff Overbey
 */
public enum VariableAccess implements IPhotranSerializable
{
    /**
     * Indicates a use of a variable name in a declarative context, i.e., a context which does not
     * correspond to either a read or a write of the variable.
     */
    NONE,
    
    /** Indicates a (potential) read of the variable */
    READ,

    /** Indicates a (potential) write of the variable */
    WRITE,

    /** Indicates that the variable may (potentially) be read, written, or both */
    RW,

    /**
     * Indicates that the variable is being used as an implied-do variable.  This indicates
     * neither a read nor a write of the variable, however, since the implied-do variable is
     * in a new scope.
     */
    IMPLIED_DO,

    /**
     * Indicates that the variable is being used as a forall index variable.  This indicates
     * neither a read nor a write of the variable, however, since the index variable is in a
     * new scope within the forall construct.
     */
    FORALL,

    /**
     * Indicates that the variable's name is being used as a formal argument name for a
     * statement function.  This indicates neither a read nor a write of the variable, however,
     * since the function argument is in a new scope.
     */
    STMT_FUNCTION_ARG;

    public char getSerializationCode()
    {
        return PhotranVPGSerializer.CLASS_VARIABLEACCESS;
    }

    public void writeTo(OutputStream out) throws IOException
    {
        PhotranVPGSerializer.serialize(this.ordinal(), out);
    }

    public static VariableAccess readFrom(InputStream in)
    {
        int ordinal = PhotranVPGSerializer.deserialize(in);
        return VariableAccess.values()[ordinal];
    }
}
