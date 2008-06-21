/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.tests.a_parser;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.parser.Parser;

/**
 * These are meant to be used as performance tests.  When this one of the test methods in this class is run standalone with
 * JVM arguments such as
 * <pre>-Xmx512M -Xrunhprof:cpu=samples,depth=5,file=/Users/joverbey/Desktop/java.hprof.txt</pre>,
 * the profile can be used to find bottlenecks in the lexer or parser code.
 * 
 * @author Jeff Overbey
 */
public class HugeFile extends TestCase
{
    private String hugeFile = null;
    
    public void setUp()
    {
        long start = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("program p\n");
        for (int i = 0; i < 30000; i++)
            sb.append("    print *, 3+4*5\n");
            //sb.append("    print *, \"----+----|----+----|----+----|----+----|----+----|----+----|----+----|----+----|\"\n");
        sb.append("end");
        hugeFile = sb.toString();
        System.out.println("String building: " + (System.currentTimeMillis() - start));
    }

    public void testLexHugeFile() throws Exception
    {
        ByteArrayInputStream in = new ByteArrayInputStream(hugeFile.getBytes());

        long start = System.currentTimeMillis();
        IAccumulatingLexer lexer = LexerFactory.createLexer(in, "<stdin>", SourceForm.preprocessedFreeForm(new IncludeLoaderCallback(null)), true);
        while (lexer.yylex().getTerminal() != Terminal.END_OF_INPUT)
            ;
        System.out.println("Lexing: " + (System.currentTimeMillis() - start));
    }

    public void testParseHugeFile() throws Exception
    {
        ByteArrayInputStream in = new ByteArrayInputStream(hugeFile.getBytes());

        long start = System.currentTimeMillis();
        IAccumulatingLexer lexer = LexerFactory.createLexer(in, "<stdin>", SourceForm.preprocessedFreeForm(new IncludeLoaderCallback(null)), true);
        new Parser().parse(lexer);
        System.out.println("Parsing: " + (System.currentTimeMillis() - start));
    }
}
