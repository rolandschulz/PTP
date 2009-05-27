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
package org.eclipse.photran.internal.core.tests.x_cpreprocessor;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.NullCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;

/**
 * 
 * @author joverbey
 */
@SuppressWarnings("restriction")
public class CPreprocessorTests extends TestCase
{
    private static class MyToken
    {
        IToken token;
        boolean isPreprocessed;
    }
    
    public void test1() throws OffsetLimitReachedException
    {
//        String source =
//         "q\n#define HELLO w\n" +
//        	"// Try this\n" +
//        	"HELLO/* */ hh \"wo\" \"rld\" ??( HELLO ??)\n" +
//        	"  # define W(Z) #Z\n" +
//        	"a W(bc) d e";
//        
//        CPreprocessor cpp = createCPP(source);
//        //cpp.setComputeImageLocations(true);
//        ArrayList<MyToken> toks = collectTokens(cpp);
//        
//        for (MyToken t : toks)
//            describe(cpp, t.token);
//
//        for (IASTPreprocessorStatement s : cpp.getLocationMap().getAllPreprocessorStatements())
//            System.out.println(s + " - " + s.getFileLocation());
//        
//        int lastOffset = 0;
//        for (MyToken t : toks)
//        {
//            if (!t.isPreprocessed)
//            {
//                // This offset is actually a "sequence number" (which is affected
//                // by macro expansions and so forth)...
//                int midOffset = t.token.getOffset();
//                int nextOffset = t.token.getEndOffset();
//
//                // ...which we have to map back to a location in the original file
//                IASTFileLocation fl = cpp.getLocationResolver().getMappedFileLocation(midOffset, nextOffset-midOffset);
//                midOffset  = fl.getNodeOffset();
//                nextOffset = midOffset + fl.getNodeLength();
//                
//                System.err.print("[");
//                System.err.print(source.substring(lastOffset, midOffset));
//                System.err.print("|");
//                String textFromFile = source.substring(midOffset, nextOffset);
//                if (t.token.getImage().equals(textFromFile))
//                    System.err.print(textFromFile);
//                else
//                    System.err.print(textFromFile + "/" + t.token.getImage());
//                System.err.print("]");
//                lastOffset = nextOffset;
//            }
//        }
//        System.err.println();
//
//        for (MyToken t : toks)
//            System.out.print(t.token.getImage() + " ");
//    }
//
//    private ArrayList<MyToken> collectTokens(CPreprocessor cpp) throws OffsetLimitReachedException
//    {
//        ArrayList<MyToken> toks = new ArrayList<MyToken>();
//        for (;;)
//        {
//            MyToken t = new MyToken();
//            t.token = cpp.nextTokenRaw(); // gcc -E does not concatenate adjacent string literals
//            
//            if (t.token.getType() == IToken.tEND_OF_INPUT)
//                break;
//            
//            if (!cpp.isOnTopContext() || cpp.getLocationMap().getImageLocation(t.token.getOffset(), t.token.getLength()) == null)
//                t.isPreprocessed = true;
//            else
//                t.isPreprocessed = false;
//            
//            toks.add(t);
//        }
//        return toks;
//    }
//
//    private void describe(CPreprocessor cpp, final IToken t)
//    {
//        System.out.println(t.getImage() + " --- " + t.getOffset() + "/" + t.getLength() + " --- " + t.getType() + " --- " + t.getClass().getName());
//        
//        ILocationResolver locMap = cpp.getLocationMap();
//        IASTImageLocation imgLoc = locMap.getImageLocation(t.getOffset(), t.getLength());
//        if (imgLoc != null)
//            System.out.println(imgLoc.getFileName() + " " + imgLoc.getNodeOffset() + "/" + imgLoc.getNodeLength() + " -- " + describeImageLocationKind(imgLoc.getLocationKind()));
//            
//        System.out.println(locMap.getUnpreprocessedSignature(imgLoc));
//    }
//
//    private String describeImageLocationKind(int k)
//    {
//        switch (k)
//        {
//            case IASTImageLocation.REGULAR_CODE: return "REGULAR_CODE";
//            case IASTImageLocation.MACRO_DEFINITION: return "MACRO_DEFINITION";
//            case IASTImageLocation.ARGUMENT_TO_MACRO_EXPANSION: return "ARGUMENT_TO_MACRO_EXPANSION";
//            default: return Integer.toString(k);
//        }
//    }
//
//    private CPreprocessor createCPP(String source)
//    {
//        return new CPreprocessor(new CodeReader(source.toCharArray()),
//            new ScannerInfo(), ParserLanguage.C, new NullLogService(), 
//            new GCCScannerExtensionConfiguration(), NullCodeReaderFactory.getInstance());
    }
}
