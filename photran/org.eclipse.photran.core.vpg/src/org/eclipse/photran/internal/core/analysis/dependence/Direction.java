/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.dependence;

/**
 * Enumeration representing the possible entries in a direction vector:
 * &quot;&lt;&quot, &quot;=&quot;, or &quot;&gt;&quot;.
 * <p>
 * A special value &quot;*&quot, used to indicate &quot;any direction&quot;
 * in the Banerjee Inequality, is also provided.
 * <p>
 * Reference: Allen and Kennedy, <i>Optimizing Compilers for Modern
 * Architectures.</i>  Direction vectors are defined on p. 46
 * (Definition 2.10); the Banerjee Inequality is described on pp. 97-111.
 * 
 * @author Jeff Overbey
 * @see IDependenceTester
 */
enum Direction
{
    /** &quot;&lt;&quot */
    LESS_THAN,
    
    /** &quot;=&quot; */
    EQUALS,
    
    /** &quot;&gt;&quot; */
    GREATER_THAN,
    
    /** * */
    ANY;
}
