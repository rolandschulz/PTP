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
package org.eclipse.rephraserengine.core.analysis.dependence;

/**
 * A dependence test operating on a perfect loop nest
 * <pre>
 * DO i_1 = L_1, U_1
 *     DO i_2 = L_2, U_2
 *        ...
 *            DO i_n = L_n, U_n
 *                A( a_0 + a_1*i_1 + ... + a_n*i_n ) = ...    ! Stmt1
 *                ... = A( b_0 + b_1*i_1 + ... + b_n*i_n )    ! Stmt2
 *            ENDDO
 *        ...
 *     ENDDO
 * ENDDO
 * </pre>
 * takes as input
 * <ol>
 *   <li> the number of loops <i>n</i> in the nest,
 *   <li> the lower and upper bounds, L_i and U_i, of each loop (for each 1 &lt;= i &lt;= n),
 *   <li> the coefficients a_i of a linearly-subscripted array access (for each 1 &lt;= i &lt;= n), and
 *   <li> the coefficients b_i of another linearly-subscripted array access (for each 1 &lt;= i &lt;= n),
 * </ol>
 * as well as the entries of a direction vector
 * D = (D_1, D_2, ..., D_n)
 * where each entry is either &quot;&lt;&quot, &quot;=&quot;, or &quot;&gt;&quot;; it returns false only if
 * it can prove that there is not a dependence between the statements Stmt1 and Stmt2 with the given
 * direction vector.  Otherwise, it returns true, indicating that there is (or might be) a dependence
 * (note that this means dependence testing is conservative).
 * <p>
 * Reference: Allen and Kennedy, <i>Optimizing Compilers for Modern Architectures,</i> pp. 95-96.
 * <p>
 * THIS IS PRELIMINARY AND EXPERIMENTAL.  IT IS NOT APPROPRIATE FOR PRODUCTION USE.
 *
 * @author Jeff Overbey
 */
public interface IDependenceTester
{
    public static enum Result
    {
        //                  Dependence might exist?
        //                  |       Definite result?
        //                  |       |
        NO_DEPENDENCE      (false,  true),
        POSSIBLE_DEPENDENCE(true,   false),
        DEFINITE_DEPENDENCE(true,   true);

        private boolean asBoolean;
        private boolean isDefinite;

        private Result(boolean asBoolean, boolean isDefinite)
        {
            this.asBoolean = asBoolean;
            this.isDefinite = isDefinite;
        }

        public boolean dependenceMightExist() { return asBoolean; }

        public boolean isDefinite() { return isDefinite; }
    }

    Result test(int n, int[] L, int[] U, int[] a, int[] b, Direction[] direction);
}
