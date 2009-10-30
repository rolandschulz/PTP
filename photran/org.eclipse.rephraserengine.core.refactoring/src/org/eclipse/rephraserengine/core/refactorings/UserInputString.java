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
package org.eclipse.rephraserengine.core.refactorings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate a setter method in a refactoring class, this indicates that a text box control
 * should be made available in the refactoring wizard's user input page; when the user types in the
 * text box, the annotated method will be invoked and passed the contents of the text box as an
 * argument.
 * <p>
 * This annotation is only used when a refactoring uses an automatically-generated user input page,
 * and it must only be used to annotate a method with the signature
 * <code>void setterMethod(String newValue)</code>
 *
 * @author Jeff Overbey
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UserInputString
{
    /**
     * @return a label that will be displayed to the left of the text box (non-<code>null</code>)
     */
    String label();

    /**
     * @return the name of a method which will provide an initial default value
     */
    String defaultValueMethod() default "";
}
