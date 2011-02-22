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
package org.eclipse.photran.internal.core.vpg;

/**
 * Enumeration of the types of annotations stored in Photran's VPG.
 * 
 * @author Jeff Overbey
 * 
 * @see PhotranVPG
 */
public enum AnnotationType
{
    SCOPE_DEFAULT_VISIBILITY_IS_PRIVATE_ANNOTATION_TYPE(Messages.PhotranVPG_DefaultVisibilityForScopeIsPrivate),
    SCOPE_IS_INTERNAL_ANNOTATION_TYPE(Messages.PhotranVPG_ScopeIsInternal),
    SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE(Messages.PhotranVPG_ImplicitSpecForScope),
    DEFINITION_ANNOTATION_TYPE(Messages.PhotranVPG_Definition),
    TYPE_ANNOTATION_TYPE(Messages.PhotranVPG_Type),
    MODULE_TOKENREF_ANNOTATION_TYPE(Messages.PhotranVPG_ModuleTokenRef),
    MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE(Messages.PhotranVPG_ModuleSymbolTableEntryCount),
    MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE(Messages.PhotranVPG_ModuleSymbolTableEntry),
    VARIABLE_ACCESS_ANNOTATION_TYPE(Messages.PhotranVPG_VariableAccess);

    private final String description;
    
    private AnnotationType(String description)
    {
        this.description = description;
    }
    
    @Override public String toString()
    {
        return description;
    }
}
