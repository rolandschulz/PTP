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

import java.util.Arrays;

/**
 * Generalized GCD Dependence Test
 * <p>
 * (Wolfe pp. 246-247)
 *
 * @author Jeff Overbey
 */
public class GeneralizedGCDTest implements IDependenceTester
{
    public Result test(int n, int[] L, int[] U, int[] a, int[] b, Direction[] direction)
    {
        // a: Coefficients in definition
        // b: Coefficients in reference

        // For each index variable
        //     element 2*i is definition
        //     element 2*i+1 is use
        IntMatrix coeffs = IntMatrix.zero(2*n /* but only 1 row used */, 2*n);

        for (int i = 1; i <= n; i++)
        {
            coeffs.set(1, 2*(i-1)+1, a[i]);
            coeffs.set(1, 2*(i-1)+2, -b[i]);
        }

        int[] c = new int[2*n];
        c[0] = b[0] - a[0];

        System.out.println("Solving\n" + coeffs + " * t =\n" + Arrays.toString(c));

        IntVector sol = coeffs.solve(c);
        System.out.println("    t =\n" + (sol == null ? "null" : sol));

        return sol != null && !sol.isZero() ? Result.POSSIBLE_DEPENDENCE : Result.NO_DEPENDENCE;
    }

    /**
    * A mutable matrix of integers, used solely to support the Generalized GCD dependence tester.
    *
    * @author Jeff Overbey
    *
    * @see GeneralizedGCDTest
    */
   public static final class IntMatrix
   {
       public static IntMatrix zero(int rows, int cols)
       {
           return new IntMatrix(rows, cols);
       }

       public static IntMatrix identity(int rank)
       {
           IntMatrix id = new IntMatrix(rank, rank);
           for (int i = 1; i <= rank; i++)
               id.set(i, i, 1);
           return id;
       }

       public static IntMatrix copyFrom(IntMatrix m)
       {
           IntMatrix result = zero(m.rows, m.cols);
           for (int col = 0; col < m.cols; col++)
               System.arraycopy(m.data[col], 0, result.data[col], 0, m.rows);
           return result;
       }

       public static IntMatrix create(int rows, int cols, int... vals)
       {
           if (vals.length != rows*cols) throw new IllegalArgumentException("Wrong number of values");

           IntMatrix result = zero(rows, cols);
           int i = 0;
           for (int row = 1; row <= rows; row++)
               for (int col = 1; col <= cols; col++)
                   result.set(row, col, vals[i++]);
           return result;
       }

       protected final int rows, cols;
       protected final int[][] data;

       protected IntMatrix(int rows, int cols)
       {
           this.rows = rows;
           this.cols = cols;
           this.data = new int[cols][rows];
       }

       /**
        * @param row (1-based)
        * @param col (1-based)
        * @return
        */
       public int get(int row, int col)
       {
           check(row, col);

           return data[col-1][row-1];
       }

       /**
        * @param row (1-based)
        * @param col (1-based)
        * @param value
        */
       public void set(int row, int col, int value)
       {
           check(row, col);

           data[col-1][row-1] = value;
       }

       /**
        *
        * @param col1
        * @param col2
        */
       public void swapColumns(int col1, int col2)
       {
           int[] tmp = data[col1-1];
           data[col1-1] = data[col2-1];
           data[col2-1] = tmp;
       }

       protected void check(int row, int col)
       {
           if (row < 1 || row > rows) throw new IllegalArgumentException("Invalid row " + row);
           if (col < 1 || col > cols) throw new IllegalArgumentException("Invalid column " + col);
       }

       /**
        * @return
        */
       public int numRows()
       {
           return rows;
       }

       /**
        * @return
        */
       public int numCols()
       {
           return cols;
       }

       @Override public String toString()
       {
           StringBuilder sb = new StringBuilder();
           for (int row = 1; row <= rows; row++)
           {
               for (int col = 1; col <= cols; col++)
               {
                   sb.append(String.format("%5d", get(row, col)));
               }
               sb.append('\n');
           }
           return sb.toString();
       }

       public boolean equalsUnwrapped(int... vals)
       {
           if (vals.length != rows * cols) throw new IllegalArgumentException("Wrong length array");

           int i = 0;
           for (int row = 1; row <= rows; row++)
               for (int col = 1; col <= cols; col++)
                   if (get(row, col) != vals[i++])
                       return false;
           return true;
       }

       public Reduce reduce()
       {
           return new Reduce(this);
       }

       public IntVector solve(int... c)
       {
           if (!isSquare()) throw new IllegalArgumentException();

           // "this" is the matrix A

           // We want to find a vector x such that A x = c
           // Find unimodular matrix U and column echelon form D such that A U = D
           Reduce reduce = reduce();

           //        A      x  = c
           // <=> (D  U^-1) x  = c
           // <=>  D (U^-1  x) = c
           // <=>  D      t    = c       for some t
           // Find t such that D t = c
           IntMatrix d = reduce.getColumnEchelonForm();
           IntVector t = d.forwardSubstitute(c);
           if (t == null) return null; // No solution

           //   t =   U^-1 x
           // U t = U U^-1 x
           // U t =        x
           IntVector x = reduce.getUnimodularMatrix().times(t);
           return x;
       }

       /**
        * @param vec
        * @return
        */
       private IntVector times(IntVector vec)
       {
           int origSize = vec.size();
           if (origSize < numRows())
           {
               IntVector newVec = IntVector.zero(numRows());
               for (int i = 1; i <= origSize; i++)
                   newVec.set(i, vec.get(i));
               vec = newVec;
           }

           int size = vec.size();
           if (numRows() != size) throw new IllegalArgumentException();
           if (numCols() != size) throw new IllegalArgumentException();

           IntVector result = IntVector.zero(size);
           for (int row = 1; row <= size; row++)
           {
               int sum = 0;
               for (int col = 1; col <= size; col++)
                   sum += vec.get(col) * this.get(row, col);
               result.set(row, sum);
           }

           if (origSize != size)
           {
               IntVector newVec = IntVector.zero(origSize);
               for (int i = 1; i <= origSize; i++)
                   newVec.set(i, result.get(i));
               return newVec;
           }
           else return result;
       }

       /**
        * Reduces a coefficient matrix D = (d, 0, 0, ..., 0) and finds an appropriate unimodular matrix
        * U such that AU = D
        *
        * Reduces a coefficient matrix to column echelon form
        *
        * Wolfe p.112
        */
       public static final class Reduce
       {
           protected IntMatrix a;
           private IntMatrix d;
           private IntMatrix u;

           protected Reduce(IntMatrix a)
           {
               /** ReduceRow -- Wolfe p. 112 */
               if (a.numRows() == 1)
               {
                   this.a = a;
                   this.u = IntMatrix.identity(a.numCols()); // TODO
                   this.d = IntMatrix.copyFrom(a); // TODO
                   reduce(1, 1, a.numCols());
               }
               /** ReduceMatrix -- Wolfe p. 115 */
               else
               {
                   this.a = a;
                   this.u = IntMatrix.identity(a.numCols()); // TODO
                   this.d = IntMatrix.copyFrom(a); // TODO
                   int c = 1;
                   for (int i = 1; i <= a.numRows(); i++)
                   {
                       reduce(i, c, a.numCols());
                       if (d.get(i, c) != 0) c++;
                   }
               }
           }

           /** Leaves only a single nonzero entry in columns j through n of row i */
           protected void reduce(int i, int j, int n)
           {
               int k;
               boolean f;

               do
               {
                   if (d.isZero(i, j, n)) return;
                   k = d.colWithMinimumMagnitude(i, j, d.numCols());
                   f = true;
                   for (int m = j; m <= n; m++)
                   {
                       if (m != k)
                       {
                           int q = d.get(i, m) / d.get(i, k);
                           if (q != 0)
                           {
                               d.addColumnMultiple(m, -q, k);
                               u.addColumnMultiple(m, -q, k);
                               if (d.get(i, m) > 0) f = false;
                           }
                       }
                   }
               } while (!f);
               if (k != 1)
               {
                   d.swapColumns(j, k);
                   u.swapColumns(j, k);
               }
           }

           /**
            * @return the u
            */
           public IntMatrix getUnimodularMatrix()
           {
               return u;
           }

           /**
            * @return the d
            */
           public IntMatrix getColumnEchelonForm()
           {
               return d;
           }

           @Override public String toString()
           {
               return "A =\n" + a + "\n\nD =\n" + d + "\n\nU =\n" + u;
           }
       }

       /**
        * @param i
        * @param j
        * @param n
        * @return
        */
       public boolean isZero(int row, int colFrom, int colThru)
       {
           for (int col = colFrom; col <= colThru; col++)
               if (get(row, col) != 0)
                   return false;

           return true;
       }

       /**
        * @param thruCol
        * @param fromCol
        * @param i
        * @return
        */
       public int colWithMinimumMagnitude(int row, int fromCol, int thruCol)
       {
           while (get(row, fromCol) == 0 && fromCol <= cols)
               fromCol++;
           if (fromCol > cols) throw new IllegalArgumentException();

           int result = fromCol;
           for (int col = fromCol; col <= thruCol; col++)
               if (get(row, col) != 0 && Math.abs(get(row, col)) < Math.abs(get(row, result)))
                   result = col;
           return result;
       }

       /**
        * @param m
        * @param i
        * @param k
        */
       public void addColumnMultiple(int col1, int factor, int col2)
       {
           for (int row = 1; row <= rows; row++)
               set(row, col1, get(row, col1) + factor * get(row, col2));
       }

       // http://www.ecs.fullerton.edu/~mathews/n2003/BackSubstitutionMod.html
       public IntVector forwardSubstitute(int... c)
       {
           //assert isLowerTriangular() && isColumnEchelonForm();
           if (!isSquare()) throw new IllegalArgumentException();
           if (c.length != numRows()) throw new IllegalArgumentException();

           int size = numRows(); //Math.min(numRows(), numNonzeroCols());
           IntVector x = IntVector.zero(size);
           for (int i = 1; i <= size; i++)
           {
               int sum = 0;
               for (int j = 1; j < i; j++)
                   sum += get(i, j) * x.get(j);
               if (c[i-1] - sum == 0 && get(i, i) == 0)
                   x.set(i, 0); // Infinitely many solutions
               else if (get(i, i) == 0)
                   return null; // No solution
               else
                   x.set(i, (c[i-1] - sum) / get(i, i));
           }
           return x;
       }

       /**
        * @return
        */
       private boolean isSquare()
       {
           return numRows() == numCols();
       }
   }

   /**
    * A mutable vector of integers, used solely to support the Generalized GCD dependence tester.
    *
    * @author Jeff Overbey
    *
    * @see GeneralizedGCDTest
    */
   public static final class IntVector
   {
       public static IntVector zero(int size)
       {
           return new IntVector(size);
       }

       public static IntVector copyFrom(IntVector m)
       {
           IntVector result = zero(m.data.length);
           System.arraycopy(m.data, 0, result.data, 0, m.data.length);
           return result;
       }

       public static IntVector create(int... vals)
       {
           IntVector result = zero(vals.length);
           System.arraycopy(vals, 0, result.data, 0, vals.length);
           return result;
       }

       protected final int[] data;

       protected IntVector(int size)
       {
           this.data = new int[size];
       }

       /**
        * @param element (1-based)
        * @return
        */
       public int get(int element)
       {
           check(element);

           return data[element-1];
       }

       /**
        * @param element (1-based)
        * @param value
        */
       public void set(int element, int value)
       {
           check(element);

           data[element-1] = value;
       }

       protected void check(int element)
       {
           if (element < 1 || element > data.length) throw new IllegalArgumentException("Invalid element " + element);
       }

       /**
        * @return
        */
       public int size()
       {
           return data.length;
       }

       @Override public String toString()
       {
           StringBuilder sb = new StringBuilder();
           for (int row = 1; row <= size(); row++)
           {
               sb.append(String.format("%5d", get(row)));
               sb.append('\n');
           }
           return sb.toString();
       }

       public boolean equalsUnwrapped(int... vals)
       {
           if (vals.length != data.length) throw new IllegalArgumentException("Wrong length array");

           return Arrays.equals(vals, data);
       }

       /**
        * @return
        */
       public boolean isZero()
       {
           for (int i = 0; i < data.length; i++)
               if (data[i] != 0)
                   return false;

           return true;
       }

       /**
        * @param unimodularMatrix
        * @return
        */
       public IntVector asRowTimes(IntMatrix matrix)
       {
           int size = this.size();
           if (matrix.numRows() != size) throw new IllegalArgumentException();
           if (matrix.numCols() != size) throw new IllegalArgumentException();

           IntVector result = IntVector.zero(size);
           for (int col = 1; col <= size; col++)
           {
               int sum = 0;
               for (int row = 1; row <= size; row++)
                   sum += this.get(row) * matrix.get(row, col);
               result.set(col, sum);
           }
           return result;
       }
   }
}
