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
 * Enumeration of the types of edges stored in Photran's VPG.
 * 
 * @author Jeff Overbey
 * 
 * @see PhotranVPG
 */
public enum EdgeType
{
    DEFINED_IN_SCOPE_EDGE_TYPE(Messages.PhotranVPG_DefinitionScopeRelationship),
    IMPORTED_INTO_SCOPE_EDGE_TYPE(Messages.PhotranVPG_DefinitionScopeRelationshipDueToModuleImport),
    BINDING_EDGE_TYPE(Messages.PhotranVPG_NameBinding),
    RENAMED_BINDING_EDGE_TYPE(Messages.PhotranVPG_RenamedBinding),
    DEFINITION_IS_PRIVATE_IN_SCOPE_EDGE_TYPE(Messages.PhotranVPG_DefinitionIsPrivateInScope),
    ILLEGAL_SHADOWING_EDGE_TYPE(Messages.PhotranVPG_IllegalShadowing),
    CONTROL_FLOW_EDGE_TYPE(Messages.PhotranVPG_ControlFlow);

    private final String description;
    
    private EdgeType(String description)
    {
        this.description = description;
    }
    
    @Override public String toString()
    {
        return description;
    }
}
