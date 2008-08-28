// $ANTLR 3.1 FortranLexer.g 2008-08-28 10:52:36

/**
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.  This
 * material was produced under U.S. Government contract DE-
 * AC52-06NA25396 for Los Alamos National Laboratory (LANL), which is
 * operated by the Los Alamos National Security, LLC (LANS) for the
 * U.S. Department of Energy. The U.S. Government has rights to use,
 * reproduce, and distribute this software. NEITHER THE GOVERNMENT NOR
 * LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified to
 * produce derivative works, such modified software should be clearly
 * marked, so as not to confuse it with the version available from
 * LANL.
 *  
 * Additionally, this program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 *
 * @author Craig E Rasmussen, Christopher D. Rickett, Jeffrey Overbey
 */
 
package org.eclipse.ptp.lang.fortran.core.parser;

import java.io.File;
import java.io.IOException;
import java.util.Stack;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class FortranLexer extends Lexer {
    public static final int T_COLON_COLON=24;
    public static final int T_ENDBLOCKDATA=172;
    public static final int T_ENDSUBROUTINE=183;
    public static final int T_ENDFILE=176;
    public static final int T_BIND=190;
    public static final int Special_Character=18;
    public static final int T_GREATERTHAN_EQ=30;
    public static final int T_FORALL=106;
    public static final int T_LABEL_DO_TERMINAL=193;
    public static final int T_NON_OVERRIDABLE=126;
    public static final int T_WRITE=169;
    public static final int T_NONE=124;
    public static final int T_COMMON=80;
    public static final int SQ_Rep_Char=6;
    public static final int T_CYCLE=83;
    public static final int T_ASTERISK=22;
    public static final int Letter=16;
    public static final int T_UNFORMATTED=162;
    public static final int T_PTR_ASSIGNMENT_STMT=199;
    public static final int T_END=186;
    public static final int T_OPTIONAL=132;
    public static final int T_TO=160;
    public static final int T_CONTROL_EDIT_DESC=195;
    public static final int T_DEFERRED=87;
    public static final int T_REWIND=150;
    public static final int T_SLASH_EQ=40;
    public static final int T_PASS=135;
    public static final int T_CLOSE=79;
    public static final int WS=15;
    public static final int T_DEALLOCATE=86;
    public static final int T_WHERE_CONSTRUCT_STMT=205;
    public static final int T_ASYNCHRONOUS=72;
    public static final int T_IF_STMT=203;
    public static final int T_ENDTYPE=184;
    public static final int T_LESSTHAN=31;
    public static final int T_LESSTHAN_EQ=32;
    public static final int T_CHARACTER=64;
    public static final int T_FUNCTION=109;
    public static final int T_ENDFORALL=175;
    public static final int T_FORALL_CONSTRUCT_STMT=206;
    public static final int T_NE=46;
    public static final int T_ENDPROGRAM=181;
    public static final int T_DIMENSION=187;
    public static final int T_THEN=159;
    public static final int T_OPEN=130;
    public static final int T_ASSIGNMENT=69;
    public static final int T_REAL=62;
    public static final int T_ABSTRACT=66;
    public static final int T_FINAL=104;
    public static final int T_STMT_FUNCTION=197;
    public static final int T_FORMAT=107;
    public static final int BINARY_CONSTANT=11;
    public static final int Digit=13;
    public static final int T_PRECISION=139;
    public static final int T_INTEGER=61;
    public static final int T_EXTENDS=101;
    public static final int T_TYPE=161;
    public static final int T_RETURN=149;
    public static final int T_SELECT=152;
    public static final int T_GE=50;
    public static final int T_IDENT=210;
    public static final int T_PARAMETER=134;
    public static final int T_PERIOD_EXPONENT=58;
    public static final int MISC_CHAR=212;
    public static final int T_NOPASS=127;
    public static final int T_INTENT=118;
    public static final int T_ENDASSOCIATE=170;
    public static final int T_INQUIRE_STMT_2=207;
    public static final int T_PRINT=138;
    public static final int T_FORMATTED=108;
    public static final int T_IMPORT=115;
    public static final int T_EXTERNAL=102;
    public static final int T_PRIVATE=140;
    public static final int DQ_Rep_Char=7;
    public static final int T_DIGIT_STRING=10;
    public static final int T_PLUS=37;
    public static final int T_POWER=38;
    public static final int T_ASSIGNMENT_STMT=198;
    public static final int T_TARGET=158;
    public static final int T_PERCENT=36;
    public static final int T_POINTER=137;
    public static final int T_SLASH_SLASH=41;
    public static final int T_EQ_GT=28;
    public static final int T_LE=48;
    public static final int T_IN=116;
    public static final int T_GOTO=112;
    public static final int T_COLON=23;
    public static final int T_PERIOD=59;
    public static final int T_ALLOCATE=68;
    public static final int T_TRUE=51;
    public static final int T_UNDERSCORE=44;
    public static final int T_DOUBLECOMPLEX=91;
    public static final int T_IMPLICIT=114;
    public static final int T_NAMELIST=123;
    public static final int T_CLASS=78;
    public static final int OCTAL_CONSTANT=12;
    public static final int T_RECURSIVE=147;
    public static final int T_KIND=188;
    public static final int T_REAL_CONSTANT=208;
    public static final int T_DOUBLEPRECISION=90;
    public static final int T_DO=88;
    public static final int T_WHILE=168;
    public static final int T_ASSOCIATE=71;
    public static final int T_NEQV=57;
    public static final int T_LPAREN=34;
    public static final int T_GT=49;
    public static final int T_GREATERTHAN=29;
    public static final int T_CHAR_STRING_EDIT_DESC=196;
    public static final int T_XYZ=60;
    public static final int T_RESULT=148;
    public static final int T_DOUBLE=89;
    public static final int T_INCLUDE=21;
    public static final int T_FILE=103;
    public static final int T_BACKSPACE=73;
    public static final int T_SELECTCASE=153;
    public static final int T_PROTECTED=143;
    public static final int T_MINUS=35;
    public static final int CONTINUE_CHAR=5;
    public static final int T_PUBLIC=144;
    public static final int T_ELSE=93;
    public static final int T_ENDMODULE=180;
    public static final int T_WHERE_STMT=202;
    public static final int T_PAUSE=136;
    public static final int T_LBRACKET=33;
    public static final int T_PURE=145;
    public static final int T_EQ_EQ=27;
    public static final int T_WHERE=167;
    public static final int T_ENTRY=96;
    public static final int T_CONTAINS=81;
    public static final int Rep_Char=19;
    public static final int T_ALLOCATABLE=67;
    public static final int T_COMMA=25;
    public static final int T_ASSIGN=70;
    public static final int T_ENDSELECT=182;
    public static final int T_RBRACKET=42;
    public static final int T_GO=111;
    public static final int T_BLOCK=74;
    public static final int T_CONTINUE=82;
    public static final int T_EOS=4;
    public static final int T_SLASH=39;
    public static final int T_NON_INTRINSIC=125;
    public static final int LINE_COMMENT=211;
    public static final int T_ENUM=97;
    public static final int T_INQUIRE=121;
    public static final int T_RPAREN=43;
    public static final int T_LOGICAL=65;
    public static final int T_DATA_EDIT_DESC=194;
    public static final int T_LEN=189;
    public static final int T_EQV=56;
    public static final int PREPROCESS_LINE=20;
    public static final int T_EOF=209;
    public static final int T_LT=47;
    public static final int T_SUBROUTINE=157;
    public static final int T_ENDWHERE=185;
    public static final int T_ENUMERATOR=98;
    public static final int T_CALL=76;
    public static final int T_USE=163;
    public static final int T_VOLATILE=165;
    public static final int T_DATA=84;
    public static final int Alphanumeric_Character=17;
    public static final int T_CASE=77;
    public static final int T_MODULE=122;
    public static final int T_ARITHMETIC_IF_STMT=200;
    public static final int T_BLOCKDATA=75;
    public static final int T_INOUT=117;
    public static final int T_OR=55;
    public static final int T_ELEMENTAL=92;
    public static final int T_FALSE=52;
    public static final int T_EQUIVALENCE=99;
    public static final int T_ELSEIF=94;
    public static final int T_SELECTTYPE=154;
    public static final int T_ENDINTERFACE=179;
    public static final int T_CHAR_CONSTANT=8;
    public static final int T_OUT=133;
    public static final int T_NULLIFY=128;
    public static final int T_EQ=45;
    public static final int T_ALLOCATE_STMT_1=201;
    public static final int T_STOP=156;
    public static final int T_VALUE=164;
    public static final int T_FORALL_STMT=204;
    public static final int T_DEFAULT=85;
    public static final int T_DEFINED_OP=192;
    public static final int T_FLUSH=105;
    public static final int T_SEQUENCE=155;
    public static final int T_OPERATOR=131;
    public static final int T_IF=113;
    public static final int T_ENDFUNCTION=177;
    public static final int HEX_CONSTANT=14;
    public static final int T_GENERIC=110;
    public static final int T_ENDDO=173;
    public static final int T_READ=146;
    public static final int Digit_String=9;
    public static final int T_NOT=53;
    public static final int T_ENDIF=178;
    public static final int T_EQUALS=26;
    public static final int T_WAIT=166;
    public static final int T_ENDBLOCK=171;
    public static final int T_ONLY=129;
    public static final int T_COMPLEX=63;
    public static final int T_PROCEDURE=141;
    public static final int T_INTRINSIC=120;
    public static final int T_ELSEWHERE=95;
    public static final int T_ENDENUM=174;
    public static final int T_SAVE=151;
    public static final int T_PROGRAM=142;
    public static final int EOF=-1;
    public static final int T_HOLLERITH=191;
    public static final int T_INTERFACE=119;
    public static final int T_AND=54;
    public static final int T_EXIT=100;

        private Token prevToken;
        private boolean continueFlag;
        private boolean includeLine;
        private boolean inFormat;
        private ArrayList<String> includeDirs;
        private Stack<FortranStream> oldStreams;

        protected StringBuilder whiteText = new StringBuilder();

        public Token emit() {
            FortranToken t = new FortranToken(input, state.type, state.channel,
                                          state.tokenStartCharIndex, getCharIndex()-1);
            t.setLine(state.tokenStartLine);
            t.setText(state.text);
            t.setCharPositionInLine(state.tokenStartCharPositionInLine);

            if (state.channel == HIDDEN) {
                whiteText.append(getText());
            } else {
                t.setWhiteText(whiteText.toString());
                whiteText.delete(0, whiteText.length());
            }

            emit(t);
            return t;
        }

        public boolean isKeyword(Token tmpToken) {
            if(tmpToken.getType() >= T_INTEGER && 
                tmpToken.getType() <= T_LEN) {
                return true;
            } else {
                return false;
            }
        }// end isKeyword()

        public boolean isKeyword(int tokenType) {
            if(tokenType >= T_INTEGER && tokenType <= T_LEN) {
                return true;
            } else {
                return false;
            }
        }// end isKeyword()


        /**
         * This is necessary because the lexer class caches some values from 
         * the input stream.  Here we reset them to what the current input stream 
         * values are.  This is done when we switch streams for including files.
         */    
        private void resetLexerState() {
            state.tokenStartCharIndex = input.index();
            state.tokenStartCharPositionInLine = input.getCharPositionInLine();
            state.tokenStartLine = input.getLine();
            state.token = null;
            state.text = null;
        }// end resetLexerState()


        // overrides nextToken in superclass
        public Token nextToken() {
            Token tmpToken;
            tmpToken = super.nextToken();

            if(tmpToken.getType() == EOF) {
                tmpToken.setChannel(Token.DEFAULT_CHANNEL);
                if(this.oldStreams != null && this.oldStreams.empty() == false) {
                    FortranToken eofToken = 
                        new FortranToken(this.input, T_EOF, Token.DEFAULT_CHANNEL,
                            this.input.index(), this.input.index()+1);
                    eofToken.setText("EOF token text");
                    tmpToken = eofToken;
                    /* We have at least one previous input stream on the stack, 
                       meaning we should be at the end of an included file.  
                       Switch back to the previous stream and continue.  */
                    this.input = this.oldStreams.pop();
                    /* Is this ok to do??  */
                    resetLexerState();
                }

                return tmpToken;
            }

            if(tmpToken.getType() != LINE_COMMENT && 
               tmpToken.getType() != WS &&
               tmpToken.getType() != PREPROCESS_LINE) {
                prevToken = tmpToken;
            } 

            if(tmpToken.getType() == T_EOS && continueFlag == true) {
                tmpToken.setChannel(99);
            } else if(continueFlag == true) {
                if(tmpToken.getType() != LINE_COMMENT &&
                   tmpToken.getType() != WS &&
                   tmpToken.getType() != PREPROCESS_LINE &&
                   tmpToken.getType() != CONTINUE_CHAR) {
                    // if the token we have is not T_EOS or any kind of WS or 
                    // comment, and we have a continue, then this should be the
                    // first token on the line folliwng the '&'.  this means that
                    // we only have one '&' (no '&' on the second line) and we 
                    // need to clear the flag so we know to process the T_EOS.
                    continueFlag = false;
                }
            }

            return tmpToken;
        }// end nextToken()


        public int getIgnoreChannelNumber() {
            // return the channel number that antlr uses for ignoring a token
            return 99;
        }// end getIgnoreChannelNumber()

        
        public CharStream getInput() {
            return this.input;
        }


        /**
         * Do this here because not sure how to get antlr to generate the 
         * init code.  It doesn't seem to do anything with the @init block below.
         * This is called my FortranMain().
         */
        public FortranLexer(FortranStream input) {
            super(input);
            prevToken = null;
            continueFlag = false;
            includeLine = false;
            inFormat = false;
            oldStreams = new Stack<FortranStream>();
        }// end constructor()


        public void setIncludeDirs(ArrayList<String> includeDirs) {
            this.includeDirs = includeDirs;
        }// end setIncludeDirs()

        
        private File findFile(String fileName) {
            File tmpFile;
            String tmpPath;
            StringBuffer newFileName;
            
            tmpFile = new File(fileName);
            if(tmpFile.exists() == false) {
                /* the file doesn't exist by the given name from the include line, 
                 * so we need to append it to each include dir and search.  */
                for(int i = 0; i < this.includeDirs.size(); i++) {
                    tmpPath = this.includeDirs.get(i);

                    newFileName = new StringBuffer();

                    /* Build the new file name with the path.  Add separator to 
                     * end of path if necessary (unix specific).  */
                    newFileName = newFileName.append(tmpPath);
                    if(tmpPath.charAt(tmpPath.length()-1) != '/') {
                        newFileName = newFileName.append('/');
                    }
                    newFileName = newFileName.append(fileName);

                    /* Try opening the new file.  */
                    tmpFile = new File(newFileName.toString());
                    if(tmpFile.exists() == true) {
                        return tmpFile;
                    }
                }

                /* File did not exist.  */
                return null;
            } else {
                return null;
            }
        }// end findFile()


        private void includeFile() {
            /* For debugging, print out the list of include dirs.  */
            if(this.includeDirs == null || this.includeDirs.size() == 0) {
                System.err.println("no include dirs given!");
                return;
            }

            if(prevToken != null) {
                String fileName = null;
                String charConst = null;
                FortranStream includedStream = null;
                File includedFile;

                charConst = prevToken.getText();
                fileName = charConst.substring(1, charConst.length()-1);

                /* Find the file, including it's complete path.  */
                includedFile = findFile(fileName);
                if(includedFile == null) {
                    System.err.println("Error: Could not open file '" + 
                        charConst.substring(1, charConst.length()-1) + "'");
                    return;
                }

    //             System.err.println("includedFile.getAbsolutePath() is: " + 
    //                 includedFile.getAbsolutePath());

                /* Create a new stream for the included file.  */
                try {
                    includedStream = 
                    new FortranStream(includedFile.getAbsolutePath(), 
                        ((FortranStream)this.input).getSourceForm());
                } catch(IOException e) {
                    e.printStackTrace();
                    return;
                }

                /* Save current character stream.  */
                oldStreams.push((FortranStream)(this.input));
                this.input = includedStream;
                /* Is this ok to do??  */
                resetLexerState();
            } else {
                System.err.println("Error: Unable to determine file name from " + 
                                   "include line");
            }

            return;
        }// end includeFile()



    // delegates
    // delegators

    public FortranLexer() {;} 
    public FortranLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public FortranLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "FortranLexer.g"; }

    // $ANTLR start "T_EOS"
    public final void mT_EOS() throws RecognitionException {
        try {
            int _type = T_EOS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:305:7: ( ';' | ( '\\r' )? ( '\\n' ) )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==';') ) {
                alt2=1;
            }
            else if ( (LA2_0=='\n'||LA2_0=='\r') ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // FortranLexer.g:305:9: ';'
                    {
                    match(';'); 

                    }
                    break;
                case 2 :
                    // FortranLexer.g:306:10: ( '\\r' )? ( '\\n' )
                    {
                    // FortranLexer.g:306:10: ( '\\r' )?
                    int alt1=2;
                    int LA1_0 = input.LA(1);

                    if ( (LA1_0=='\r') ) {
                        alt1=1;
                    }
                    switch (alt1) {
                        case 1 :
                            // FortranLexer.g:306:11: '\\r'
                            {
                            match('\r'); 

                            }
                            break;

                    }

                    // FortranLexer.g:306:18: ( '\\n' )
                    // FortranLexer.g:306:19: '\\n'
                    {
                    match('\n'); 

                    }


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;

                // if the previous token was a T_EOS, then the one we're 
                // processing now is whitespace, so throw it away.
                // also, if the previous token is null it means we have a 
                // blank line or a semicolon at the start of the file and 
                // we need to ignore it.  
                if(prevToken == null || 
                    (prevToken != null && prevToken.getType() == T_EOS)) {
                    _channel=HIDDEN;
                } 

                if(includeLine) {
                    _channel=HIDDEN;
                    // Part of include file handling..
                    includeFile();
                    includeLine = false;
                }

                // Make sure we clear the flag saying we're in a format-stmt
                inFormat = false;
        }
        finally {
        }
    }
    // $ANTLR end "T_EOS"

    // $ANTLR start "CONTINUE_CHAR"
    public final void mCONTINUE_CHAR() throws RecognitionException {
        try {
            int _type = CONTINUE_CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:315:15: ( '&' )
            // FortranLexer.g:315:17: '&'
            {
            match('&'); 
             continueFlag = !continueFlag;
            //                       _channel = 99;
                                _channel=HIDDEN;
            //                     _channel=99;
                    

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CONTINUE_CHAR"

    // $ANTLR start "T_CHAR_CONSTANT"
    public final void mT_CHAR_CONSTANT() throws RecognitionException {
        try {
            int _type = T_CHAR_CONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:325:9: ( ( '\\'' ( SQ_Rep_Char )* '\\'' )+ | ( '\\\"' ( DQ_Rep_Char )* '\\\"' )+ )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\'') ) {
                alt7=1;
            }
            else if ( (LA7_0=='\"') ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // FortranLexer.g:325:11: ( '\\'' ( SQ_Rep_Char )* '\\'' )+
                    {
                    // FortranLexer.g:325:11: ( '\\'' ( SQ_Rep_Char )* '\\'' )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0=='\'') ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // FortranLexer.g:325:12: '\\'' ( SQ_Rep_Char )* '\\''
                    	    {
                    	    match('\''); 
                    	    // FortranLexer.g:325:17: ( SQ_Rep_Char )*
                    	    loop3:
                    	    do {
                    	        int alt3=2;
                    	        int LA3_0 = input.LA(1);

                    	        if ( ((LA3_0>='\u0000' && LA3_0<='&')||(LA3_0>='(' && LA3_0<='\uFFFE')) ) {
                    	            alt3=1;
                    	        }


                    	        switch (alt3) {
                    	    	case 1 :
                    	    	    // FortranLexer.g:325:19: SQ_Rep_Char
                    	    	    {
                    	    	    mSQ_Rep_Char(); 

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop3;
                    	        }
                    	    } while (true);

                    	    match('\''); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt4 >= 1 ) break loop4;
                                EarlyExitException eee =
                                    new EarlyExitException(4, input);
                                throw eee;
                        }
                        cnt4++;
                    } while (true);

                     
                                if(includeLine) 
                    //                 _channel=99;
                                    _channel=HIDDEN;
                    //             _channel=99;
                            

                    }
                    break;
                case 2 :
                    // FortranLexer.g:331:11: ( '\\\"' ( DQ_Rep_Char )* '\\\"' )+
                    {
                    // FortranLexer.g:331:11: ( '\\\"' ( DQ_Rep_Char )* '\\\"' )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0=='\"') ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // FortranLexer.g:331:12: '\\\"' ( DQ_Rep_Char )* '\\\"'
                    	    {
                    	    match('\"'); 
                    	    // FortranLexer.g:331:17: ( DQ_Rep_Char )*
                    	    loop5:
                    	    do {
                    	        int alt5=2;
                    	        int LA5_0 = input.LA(1);

                    	        if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='\uFFFE')) ) {
                    	            alt5=1;
                    	        }


                    	        switch (alt5) {
                    	    	case 1 :
                    	    	    // FortranLexer.g:331:19: DQ_Rep_Char
                    	    	    {
                    	    	    mDQ_Rep_Char(); 

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop5;
                    	        }
                    	    } while (true);

                    	    match('\"'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt6 >= 1 ) break loop6;
                                EarlyExitException eee =
                                    new EarlyExitException(6, input);
                                throw eee;
                        }
                        cnt6++;
                    } while (true);

                     
                                if(includeLine) 
                    //                _channel=99;
                                    _channel=HIDDEN;
                    //             _channel=99;
                            

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CHAR_CONSTANT"

    // $ANTLR start "T_DIGIT_STRING"
    public final void mT_DIGIT_STRING() throws RecognitionException {
        try {
            int _type = T_DIGIT_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:340:9: ( Digit_String )
            // FortranLexer.g:340:17: Digit_String
            {
            mDigit_String(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DIGIT_STRING"

    // $ANTLR start "BINARY_CONSTANT"
    public final void mBINARY_CONSTANT() throws RecognitionException {
        try {
            int _type = BINARY_CONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:345:5: ( ( 'b' | 'B' ) '\\'' ( '0' .. '1' )+ '\\'' | ( 'b' | 'B' ) '\\\"' ( '0' .. '1' )+ '\\\"' )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='B'||LA10_0=='b') ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1=='\'') ) {
                    alt10=1;
                }
                else if ( (LA10_1=='\"') ) {
                    alt10=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 10, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // FortranLexer.g:345:7: ( 'b' | 'B' ) '\\'' ( '0' .. '1' )+ '\\''
                    {
                    if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('\''); 
                    // FortranLexer.g:345:22: ( '0' .. '1' )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0>='0' && LA8_0<='1')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // FortranLexer.g:345:23: '0' .. '1'
                    	    {
                    	    matchRange('0','1'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt8 >= 1 ) break loop8;
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);

                    match('\''); 

                    }
                    break;
                case 2 :
                    // FortranLexer.g:346:7: ( 'b' | 'B' ) '\\\"' ( '0' .. '1' )+ '\\\"'
                    {
                    if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('\"'); 
                    // FortranLexer.g:346:22: ( '0' .. '1' )+
                    int cnt9=0;
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( ((LA9_0>='0' && LA9_0<='1')) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // FortranLexer.g:346:23: '0' .. '1'
                    	    {
                    	    matchRange('0','1'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt9 >= 1 ) break loop9;
                                EarlyExitException eee =
                                    new EarlyExitException(9, input);
                                throw eee;
                        }
                        cnt9++;
                    } while (true);

                    match('\"'); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BINARY_CONSTANT"

    // $ANTLR start "OCTAL_CONSTANT"
    public final void mOCTAL_CONSTANT() throws RecognitionException {
        try {
            int _type = OCTAL_CONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:351:5: ( ( 'o' | 'O' ) '\\'' ( '0' .. '7' )+ '\\'' | ( 'o' | 'O' ) '\\\"' ( '0' .. '7' )+ '\\\"' )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='O'||LA13_0=='o') ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1=='\'') ) {
                    alt13=1;
                }
                else if ( (LA13_1=='\"') ) {
                    alt13=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // FortranLexer.g:351:7: ( 'o' | 'O' ) '\\'' ( '0' .. '7' )+ '\\''
                    {
                    if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('\''); 
                    // FortranLexer.g:351:22: ( '0' .. '7' )+
                    int cnt11=0;
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0>='0' && LA11_0<='7')) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // FortranLexer.g:351:23: '0' .. '7'
                    	    {
                    	    matchRange('0','7'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt11 >= 1 ) break loop11;
                                EarlyExitException eee =
                                    new EarlyExitException(11, input);
                                throw eee;
                        }
                        cnt11++;
                    } while (true);

                    match('\''); 

                    }
                    break;
                case 2 :
                    // FortranLexer.g:352:7: ( 'o' | 'O' ) '\\\"' ( '0' .. '7' )+ '\\\"'
                    {
                    if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('\"'); 
                    // FortranLexer.g:352:22: ( '0' .. '7' )+
                    int cnt12=0;
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( ((LA12_0>='0' && LA12_0<='7')) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // FortranLexer.g:352:23: '0' .. '7'
                    	    {
                    	    matchRange('0','7'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt12 >= 1 ) break loop12;
                                EarlyExitException eee =
                                    new EarlyExitException(12, input);
                                throw eee;
                        }
                        cnt12++;
                    } while (true);

                    match('\"'); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OCTAL_CONSTANT"

    // $ANTLR start "HEX_CONSTANT"
    public final void mHEX_CONSTANT() throws RecognitionException {
        try {
            int _type = HEX_CONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:357:5: ( ( 'z' | 'Z' ) '\\'' ( Digit | 'a' .. 'f' | 'A' .. 'F' )+ '\\'' | ( 'z' | 'Z' ) '\\\"' ( Digit | 'a' .. 'f' | 'A' .. 'F' )+ '\\\"' )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='Z'||LA16_0=='z') ) {
                int LA16_1 = input.LA(2);

                if ( (LA16_1=='\'') ) {
                    alt16=1;
                }
                else if ( (LA16_1=='\"') ) {
                    alt16=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // FortranLexer.g:357:7: ( 'z' | 'Z' ) '\\'' ( Digit | 'a' .. 'f' | 'A' .. 'F' )+ '\\''
                    {
                    if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('\''); 
                    // FortranLexer.g:357:22: ( Digit | 'a' .. 'f' | 'A' .. 'F' )+
                    int cnt14=0;
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( ((LA14_0>='0' && LA14_0<='9')||(LA14_0>='A' && LA14_0<='F')||(LA14_0>='a' && LA14_0<='f')) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // FortranLexer.g:
                    	    {
                    	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt14 >= 1 ) break loop14;
                                EarlyExitException eee =
                                    new EarlyExitException(14, input);
                                throw eee;
                        }
                        cnt14++;
                    } while (true);

                    match('\''); 

                    }
                    break;
                case 2 :
                    // FortranLexer.g:358:7: ( 'z' | 'Z' ) '\\\"' ( Digit | 'a' .. 'f' | 'A' .. 'F' )+ '\\\"'
                    {
                    if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('\"'); 
                    // FortranLexer.g:358:22: ( Digit | 'a' .. 'f' | 'A' .. 'F' )+
                    int cnt15=0;
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( ((LA15_0>='0' && LA15_0<='9')||(LA15_0>='A' && LA15_0<='F')||(LA15_0>='a' && LA15_0<='f')) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // FortranLexer.g:
                    	    {
                    	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt15 >= 1 ) break loop15;
                                EarlyExitException eee =
                                    new EarlyExitException(15, input);
                                throw eee;
                        }
                        cnt15++;
                    } while (true);

                    match('\"'); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HEX_CONSTANT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:362:5: ( ( ' ' | '\\r' | '\\t' | '\\u000C' ) )
            // FortranLexer.g:362:8: ( ' ' | '\\r' | '\\t' | '\\u000C' )
            {
            if ( input.LA(1)=='\t'||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // _channel=99;
                        _channel=HIDDEN;
            //             _channel=99;
            //             _channel=99;
                    

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "Digit_String"
    public final void mDigit_String() throws RecognitionException {
        try {
            // FortranLexer.g:375:14: ( ( Digit )+ )
            // FortranLexer.g:375:16: ( Digit )+
            {
            // FortranLexer.g:375:16: ( Digit )+
            int cnt17=0;
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( ((LA17_0>='0' && LA17_0<='9')) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // FortranLexer.g:375:16: Digit
            	    {
            	    mDigit(); 

            	    }
            	    break;

            	default :
            	    if ( cnt17 >= 1 ) break loop17;
                        EarlyExitException eee =
                            new EarlyExitException(17, input);
                        throw eee;
                }
                cnt17++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "Digit_String"

    // $ANTLR start "Alphanumeric_Character"
    public final void mAlphanumeric_Character() throws RecognitionException {
        try {
            // FortranLexer.g:380:24: ( Letter | Digit | '_' )
            // FortranLexer.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Alphanumeric_Character"

    // $ANTLR start "Special_Character"
    public final void mSpecial_Character() throws RecognitionException {
        try {
            // FortranLexer.g:384:5: ( ' ' .. '/' | ':' .. '@' | '[' .. '^' | '`' | '{' .. '~' )
            // FortranLexer.g:
            {
            if ( (input.LA(1)>=' ' && input.LA(1)<='/')||(input.LA(1)>=':' && input.LA(1)<='@')||(input.LA(1)>='[' && input.LA(1)<='^')||input.LA(1)=='`'||(input.LA(1)>='{' && input.LA(1)<='~') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Special_Character"

    // $ANTLR start "Rep_Char"
    public final void mRep_Char() throws RecognitionException {
        try {
            // FortranLexer.g:392:10: (~ ( '\\'' | '\\\"' ) )
            // FortranLexer.g:392:12: ~ ( '\\'' | '\\\"' )
            {
            if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFE') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Rep_Char"

    // $ANTLR start "SQ_Rep_Char"
    public final void mSQ_Rep_Char() throws RecognitionException {
        try {
            // FortranLexer.g:395:13: (~ ( '\\'' ) )
            // FortranLexer.g:395:15: ~ ( '\\'' )
            {
            if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFE') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "SQ_Rep_Char"

    // $ANTLR start "DQ_Rep_Char"
    public final void mDQ_Rep_Char() throws RecognitionException {
        try {
            // FortranLexer.g:397:13: (~ ( '\\\"' ) )
            // FortranLexer.g:397:15: ~ ( '\\\"' )
            {
            if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFE') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "DQ_Rep_Char"

    // $ANTLR start "Letter"
    public final void mLetter() throws RecognitionException {
        try {
            // FortranLexer.g:400:8: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // FortranLexer.g:400:10: ( 'a' .. 'z' | 'A' .. 'Z' )
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Letter"

    // $ANTLR start "Digit"
    public final void mDigit() throws RecognitionException {
        try {
            // FortranLexer.g:403:7: ( '0' .. '9' )
            // FortranLexer.g:403:9: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "Digit"

    // $ANTLR start "PREPROCESS_LINE"
    public final void mPREPROCESS_LINE() throws RecognitionException {
        try {
            int _type = PREPROCESS_LINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:405:17: ( '#' (~ ( '\\n' | '\\r' ) )* )
            // FortranLexer.g:405:19: '#' (~ ( '\\n' | '\\r' ) )*
            {
            match('#'); 
            // FortranLexer.g:405:23: (~ ( '\\n' | '\\r' ) )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( ((LA18_0>='\u0000' && LA18_0<='\t')||(LA18_0>='\u000B' && LA18_0<='\f')||(LA18_0>='\u000E' && LA18_0<='\uFFFE')) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // FortranLexer.g:405:23: ~ ( '\\n' | '\\r' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);

             // _channel=99;
                        _channel=HIDDEN;
            //             _channel=99;
                    

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PREPROCESS_LINE"

    // $ANTLR start "T_INCLUDE"
    public final void mT_INCLUDE() throws RecognitionException {
        try {
            int _type = T_INCLUDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:411:16: ( 'INCLUDE' )
            // FortranLexer.g:411:18: 'INCLUDE'
            {
            match("INCLUDE"); 

             includeLine = true; // _channel=99;
                        _channel=HIDDEN;
            //             _channel=99;
                    

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_INCLUDE"

    // $ANTLR start "T_ASTERISK"
    public final void mT_ASTERISK() throws RecognitionException {
        try {
            int _type = T_ASTERISK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:421:17: ( '*' )
            // FortranLexer.g:421:19: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ASTERISK"

    // $ANTLR start "T_COLON"
    public final void mT_COLON() throws RecognitionException {
        try {
            int _type = T_COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:422:17: ( ':' )
            // FortranLexer.g:422:19: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_COLON"

    // $ANTLR start "T_COLON_COLON"
    public final void mT_COLON_COLON() throws RecognitionException {
        try {
            int _type = T_COLON_COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:423:17: ( '::' )
            // FortranLexer.g:423:19: '::'
            {
            match("::"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_COLON_COLON"

    // $ANTLR start "T_COMMA"
    public final void mT_COMMA() throws RecognitionException {
        try {
            int _type = T_COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:424:17: ( ',' )
            // FortranLexer.g:424:19: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_COMMA"

    // $ANTLR start "T_EQUALS"
    public final void mT_EQUALS() throws RecognitionException {
        try {
            int _type = T_EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:425:17: ( '=' )
            // FortranLexer.g:425:19: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EQUALS"

    // $ANTLR start "T_EQ_EQ"
    public final void mT_EQ_EQ() throws RecognitionException {
        try {
            int _type = T_EQ_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:426:17: ( '==' )
            // FortranLexer.g:426:19: '=='
            {
            match("=="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EQ_EQ"

    // $ANTLR start "T_EQ_GT"
    public final void mT_EQ_GT() throws RecognitionException {
        try {
            int _type = T_EQ_GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:427:17: ( '=>' )
            // FortranLexer.g:427:19: '=>'
            {
            match("=>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EQ_GT"

    // $ANTLR start "T_GREATERTHAN"
    public final void mT_GREATERTHAN() throws RecognitionException {
        try {
            int _type = T_GREATERTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:428:17: ( '>' )
            // FortranLexer.g:428:19: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_GREATERTHAN"

    // $ANTLR start "T_GREATERTHAN_EQ"
    public final void mT_GREATERTHAN_EQ() throws RecognitionException {
        try {
            int _type = T_GREATERTHAN_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:429:17: ( '>=' )
            // FortranLexer.g:429:19: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_GREATERTHAN_EQ"

    // $ANTLR start "T_LESSTHAN"
    public final void mT_LESSTHAN() throws RecognitionException {
        try {
            int _type = T_LESSTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:430:17: ( '<' )
            // FortranLexer.g:430:19: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LESSTHAN"

    // $ANTLR start "T_LESSTHAN_EQ"
    public final void mT_LESSTHAN_EQ() throws RecognitionException {
        try {
            int _type = T_LESSTHAN_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:431:17: ( '<=' )
            // FortranLexer.g:431:19: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LESSTHAN_EQ"

    // $ANTLR start "T_LBRACKET"
    public final void mT_LBRACKET() throws RecognitionException {
        try {
            int _type = T_LBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:432:17: ( '[' )
            // FortranLexer.g:432:19: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LBRACKET"

    // $ANTLR start "T_LPAREN"
    public final void mT_LPAREN() throws RecognitionException {
        try {
            int _type = T_LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:433:17: ( '(' )
            // FortranLexer.g:433:19: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LPAREN"

    // $ANTLR start "T_MINUS"
    public final void mT_MINUS() throws RecognitionException {
        try {
            int _type = T_MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:434:17: ( '-' )
            // FortranLexer.g:434:19: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_MINUS"

    // $ANTLR start "T_PERCENT"
    public final void mT_PERCENT() throws RecognitionException {
        try {
            int _type = T_PERCENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:435:17: ( '%' )
            // FortranLexer.g:435:19: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PERCENT"

    // $ANTLR start "T_PLUS"
    public final void mT_PLUS() throws RecognitionException {
        try {
            int _type = T_PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:436:17: ( '+' )
            // FortranLexer.g:436:19: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PLUS"

    // $ANTLR start "T_POWER"
    public final void mT_POWER() throws RecognitionException {
        try {
            int _type = T_POWER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:437:17: ( '**' )
            // FortranLexer.g:437:19: '**'
            {
            match("**"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_POWER"

    // $ANTLR start "T_SLASH"
    public final void mT_SLASH() throws RecognitionException {
        try {
            int _type = T_SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:438:17: ( '/' )
            // FortranLexer.g:438:19: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SLASH"

    // $ANTLR start "T_SLASH_EQ"
    public final void mT_SLASH_EQ() throws RecognitionException {
        try {
            int _type = T_SLASH_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:439:17: ( '/=' )
            // FortranLexer.g:439:19: '/='
            {
            match("/="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SLASH_EQ"

    // $ANTLR start "T_SLASH_SLASH"
    public final void mT_SLASH_SLASH() throws RecognitionException {
        try {
            int _type = T_SLASH_SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:440:17: ( '//' )
            // FortranLexer.g:440:19: '//'
            {
            match("//"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SLASH_SLASH"

    // $ANTLR start "T_RBRACKET"
    public final void mT_RBRACKET() throws RecognitionException {
        try {
            int _type = T_RBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:441:17: ( ']' )
            // FortranLexer.g:441:19: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_RBRACKET"

    // $ANTLR start "T_RPAREN"
    public final void mT_RPAREN() throws RecognitionException {
        try {
            int _type = T_RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:442:17: ( ')' )
            // FortranLexer.g:442:19: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_RPAREN"

    // $ANTLR start "T_UNDERSCORE"
    public final void mT_UNDERSCORE() throws RecognitionException {
        try {
            int _type = T_UNDERSCORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:443:17: ( '_' )
            // FortranLexer.g:443:19: '_'
            {
            match('_'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_UNDERSCORE"

    // $ANTLR start "T_EQ"
    public final void mT_EQ() throws RecognitionException {
        try {
            int _type = T_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:445:17: ( '.EQ.' )
            // FortranLexer.g:445:19: '.EQ.'
            {
            match(".EQ."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EQ"

    // $ANTLR start "T_NE"
    public final void mT_NE() throws RecognitionException {
        try {
            int _type = T_NE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:446:17: ( '.NE.' )
            // FortranLexer.g:446:19: '.NE.'
            {
            match(".NE."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NE"

    // $ANTLR start "T_LT"
    public final void mT_LT() throws RecognitionException {
        try {
            int _type = T_LT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:447:17: ( '.LT.' )
            // FortranLexer.g:447:19: '.LT.'
            {
            match(".LT."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LT"

    // $ANTLR start "T_LE"
    public final void mT_LE() throws RecognitionException {
        try {
            int _type = T_LE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:448:17: ( '.LE.' )
            // FortranLexer.g:448:19: '.LE.'
            {
            match(".LE."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LE"

    // $ANTLR start "T_GT"
    public final void mT_GT() throws RecognitionException {
        try {
            int _type = T_GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:449:17: ( '.GT.' )
            // FortranLexer.g:449:19: '.GT.'
            {
            match(".GT."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_GT"

    // $ANTLR start "T_GE"
    public final void mT_GE() throws RecognitionException {
        try {
            int _type = T_GE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:450:17: ( '.GE.' )
            // FortranLexer.g:450:19: '.GE.'
            {
            match(".GE."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_GE"

    // $ANTLR start "T_TRUE"
    public final void mT_TRUE() throws RecognitionException {
        try {
            int _type = T_TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:452:17: ( '.TRUE.' )
            // FortranLexer.g:452:19: '.TRUE.'
            {
            match(".TRUE."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_TRUE"

    // $ANTLR start "T_FALSE"
    public final void mT_FALSE() throws RecognitionException {
        try {
            int _type = T_FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:453:17: ( '.FALSE.' )
            // FortranLexer.g:453:19: '.FALSE.'
            {
            match(".FALSE."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FALSE"

    // $ANTLR start "T_NOT"
    public final void mT_NOT() throws RecognitionException {
        try {
            int _type = T_NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:455:17: ( '.NOT.' )
            // FortranLexer.g:455:19: '.NOT.'
            {
            match(".NOT."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NOT"

    // $ANTLR start "T_AND"
    public final void mT_AND() throws RecognitionException {
        try {
            int _type = T_AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:456:17: ( '.AND.' )
            // FortranLexer.g:456:19: '.AND.'
            {
            match(".AND."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_AND"

    // $ANTLR start "T_OR"
    public final void mT_OR() throws RecognitionException {
        try {
            int _type = T_OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:457:17: ( '.OR.' )
            // FortranLexer.g:457:19: '.OR.'
            {
            match(".OR."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_OR"

    // $ANTLR start "T_EQV"
    public final void mT_EQV() throws RecognitionException {
        try {
            int _type = T_EQV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:458:17: ( '.EQV.' )
            // FortranLexer.g:458:19: '.EQV.'
            {
            match(".EQV."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EQV"

    // $ANTLR start "T_NEQV"
    public final void mT_NEQV() throws RecognitionException {
        try {
            int _type = T_NEQV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:459:17: ( '.NEQV.' )
            // FortranLexer.g:459:19: '.NEQV.'
            {
            match(".NEQV."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NEQV"

    // $ANTLR start "T_PERIOD_EXPONENT"
    public final void mT_PERIOD_EXPONENT() throws RecognitionException {
        try {
            int _type = T_PERIOD_EXPONENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:462:5: ( '.' ( '0' .. '9' )+ ( 'E' | 'e' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+ | '.' ( 'E' | 'e' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+ | '.' ( '0' .. '9' )+ | ( '0' .. '9' )+ ( 'e' | 'E' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            int alt28=4;
            alt28 = dfa28.predict(input);
            switch (alt28) {
                case 1 :
                    // FortranLexer.g:462:7: '.' ( '0' .. '9' )+ ( 'E' | 'e' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+
                    {
                    match('.'); 
                    // FortranLexer.g:462:11: ( '0' .. '9' )+
                    int cnt19=0;
                    loop19:
                    do {
                        int alt19=2;
                        int LA19_0 = input.LA(1);

                        if ( ((LA19_0>='0' && LA19_0<='9')) ) {
                            alt19=1;
                        }


                        switch (alt19) {
                    	case 1 :
                    	    // FortranLexer.g:462:12: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt19 >= 1 ) break loop19;
                                EarlyExitException eee =
                                    new EarlyExitException(19, input);
                                throw eee;
                        }
                        cnt19++;
                    } while (true);

                    if ( (input.LA(1)>='D' && input.LA(1)<='E')||(input.LA(1)>='d' && input.LA(1)<='e') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // FortranLexer.g:462:47: ( '+' | '-' )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0=='+'||LA20_0=='-') ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // FortranLexer.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    // FortranLexer.g:462:60: ( '0' .. '9' )+
                    int cnt21=0;
                    loop21:
                    do {
                        int alt21=2;
                        int LA21_0 = input.LA(1);

                        if ( ((LA21_0>='0' && LA21_0<='9')) ) {
                            alt21=1;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // FortranLexer.g:462:61: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt21 >= 1 ) break loop21;
                                EarlyExitException eee =
                                    new EarlyExitException(21, input);
                                throw eee;
                        }
                        cnt21++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // FortranLexer.g:463:7: '.' ( 'E' | 'e' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+
                    {
                    match('.'); 
                    if ( (input.LA(1)>='D' && input.LA(1)<='E')||(input.LA(1)>='d' && input.LA(1)<='e') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // FortranLexer.g:463:35: ( '+' | '-' )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0=='+'||LA22_0=='-') ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // FortranLexer.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    // FortranLexer.g:463:48: ( '0' .. '9' )+
                    int cnt23=0;
                    loop23:
                    do {
                        int alt23=2;
                        int LA23_0 = input.LA(1);

                        if ( ((LA23_0>='0' && LA23_0<='9')) ) {
                            alt23=1;
                        }


                        switch (alt23) {
                    	case 1 :
                    	    // FortranLexer.g:463:49: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt23 >= 1 ) break loop23;
                                EarlyExitException eee =
                                    new EarlyExitException(23, input);
                                throw eee;
                        }
                        cnt23++;
                    } while (true);


                    }
                    break;
                case 3 :
                    // FortranLexer.g:464:7: '.' ( '0' .. '9' )+
                    {
                    match('.'); 
                    // FortranLexer.g:464:11: ( '0' .. '9' )+
                    int cnt24=0;
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( ((LA24_0>='0' && LA24_0<='9')) ) {
                            alt24=1;
                        }


                        switch (alt24) {
                    	case 1 :
                    	    // FortranLexer.g:464:12: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt24 >= 1 ) break loop24;
                                EarlyExitException eee =
                                    new EarlyExitException(24, input);
                                throw eee;
                        }
                        cnt24++;
                    } while (true);


                    }
                    break;
                case 4 :
                    // FortranLexer.g:465:7: ( '0' .. '9' )+ ( 'e' | 'E' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+
                    {
                    // FortranLexer.g:465:7: ( '0' .. '9' )+
                    int cnt25=0;
                    loop25:
                    do {
                        int alt25=2;
                        int LA25_0 = input.LA(1);

                        if ( ((LA25_0>='0' && LA25_0<='9')) ) {
                            alt25=1;
                        }


                        switch (alt25) {
                    	case 1 :
                    	    // FortranLexer.g:465:8: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt25 >= 1 ) break loop25;
                                EarlyExitException eee =
                                    new EarlyExitException(25, input);
                                throw eee;
                        }
                        cnt25++;
                    } while (true);

                    if ( (input.LA(1)>='D' && input.LA(1)<='E')||(input.LA(1)>='d' && input.LA(1)<='e') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // FortranLexer.g:465:43: ( '+' | '-' )?
                    int alt26=2;
                    int LA26_0 = input.LA(1);

                    if ( (LA26_0=='+'||LA26_0=='-') ) {
                        alt26=1;
                    }
                    switch (alt26) {
                        case 1 :
                            // FortranLexer.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    // FortranLexer.g:465:56: ( '0' .. '9' )+
                    int cnt27=0;
                    loop27:
                    do {
                        int alt27=2;
                        int LA27_0 = input.LA(1);

                        if ( ((LA27_0>='0' && LA27_0<='9')) ) {
                            alt27=1;
                        }


                        switch (alt27) {
                    	case 1 :
                    	    // FortranLexer.g:465:57: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt27 >= 1 ) break loop27;
                                EarlyExitException eee =
                                    new EarlyExitException(27, input);
                                throw eee;
                        }
                        cnt27++;
                    } while (true);


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PERIOD_EXPONENT"

    // $ANTLR start "T_PERIOD"
    public final void mT_PERIOD() throws RecognitionException {
        try {
            int _type = T_PERIOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:468:17: ( '.' )
            // FortranLexer.g:468:19: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PERIOD"

    // $ANTLR start "T_XYZ"
    public final void mT_XYZ() throws RecognitionException {
        try {
            int _type = T_XYZ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:476:17: ( '__XYZ__' )
            // FortranLexer.g:476:19: '__XYZ__'
            {
            match("__XYZ__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_XYZ"

    // $ANTLR start "T_INTEGER"
    public final void mT_INTEGER() throws RecognitionException {
        try {
            int _type = T_INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:478:17: ( 'INTEGER' )
            // FortranLexer.g:478:25: 'INTEGER'
            {
            match("INTEGER"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_INTEGER"

    // $ANTLR start "T_REAL"
    public final void mT_REAL() throws RecognitionException {
        try {
            int _type = T_REAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:479:17: ( 'REAL' )
            // FortranLexer.g:479:25: 'REAL'
            {
            match("REAL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_REAL"

    // $ANTLR start "T_COMPLEX"
    public final void mT_COMPLEX() throws RecognitionException {
        try {
            int _type = T_COMPLEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:480:17: ( 'COMPLEX' )
            // FortranLexer.g:480:25: 'COMPLEX'
            {
            match("COMPLEX"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_COMPLEX"

    // $ANTLR start "T_CHARACTER"
    public final void mT_CHARACTER() throws RecognitionException {
        try {
            int _type = T_CHARACTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:481:17: ( 'CHARACTER' )
            // FortranLexer.g:481:25: 'CHARACTER'
            {
            match("CHARACTER"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CHARACTER"

    // $ANTLR start "T_LOGICAL"
    public final void mT_LOGICAL() throws RecognitionException {
        try {
            int _type = T_LOGICAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:482:17: ( 'LOGICAL' )
            // FortranLexer.g:482:25: 'LOGICAL'
            {
            match("LOGICAL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LOGICAL"

    // $ANTLR start "T_ABSTRACT"
    public final void mT_ABSTRACT() throws RecognitionException {
        try {
            int _type = T_ABSTRACT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:484:17: ( 'ABSTRACT' )
            // FortranLexer.g:484:25: 'ABSTRACT'
            {
            match("ABSTRACT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ABSTRACT"

    // $ANTLR start "T_ALLOCATABLE"
    public final void mT_ALLOCATABLE() throws RecognitionException {
        try {
            int _type = T_ALLOCATABLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:485:17: ( 'ALLOCATABLE' )
            // FortranLexer.g:485:25: 'ALLOCATABLE'
            {
            match("ALLOCATABLE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ALLOCATABLE"

    // $ANTLR start "T_ALLOCATE"
    public final void mT_ALLOCATE() throws RecognitionException {
        try {
            int _type = T_ALLOCATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:486:17: ( 'ALLOCATE' )
            // FortranLexer.g:486:25: 'ALLOCATE'
            {
            match("ALLOCATE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ALLOCATE"

    // $ANTLR start "T_ASSIGNMENT"
    public final void mT_ASSIGNMENT() throws RecognitionException {
        try {
            int _type = T_ASSIGNMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:487:17: ( 'ASSIGNMENT' )
            // FortranLexer.g:487:25: 'ASSIGNMENT'
            {
            match("ASSIGNMENT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ASSIGNMENT"

    // $ANTLR start "T_ASSIGN"
    public final void mT_ASSIGN() throws RecognitionException {
        try {
            int _type = T_ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:489:17: ( 'ASSIGN' )
            // FortranLexer.g:489:25: 'ASSIGN'
            {
            match("ASSIGN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ASSIGN"

    // $ANTLR start "T_ASSOCIATE"
    public final void mT_ASSOCIATE() throws RecognitionException {
        try {
            int _type = T_ASSOCIATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:490:17: ( 'ASSOCIATE' )
            // FortranLexer.g:490:25: 'ASSOCIATE'
            {
            match("ASSOCIATE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ASSOCIATE"

    // $ANTLR start "T_ASYNCHRONOUS"
    public final void mT_ASYNCHRONOUS() throws RecognitionException {
        try {
            int _type = T_ASYNCHRONOUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:491:17: ( 'ASYNCHRONOUS' )
            // FortranLexer.g:491:25: 'ASYNCHRONOUS'
            {
            match("ASYNCHRONOUS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ASYNCHRONOUS"

    // $ANTLR start "T_BACKSPACE"
    public final void mT_BACKSPACE() throws RecognitionException {
        try {
            int _type = T_BACKSPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:492:17: ( 'BACKSPACE' )
            // FortranLexer.g:492:25: 'BACKSPACE'
            {
            match("BACKSPACE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_BACKSPACE"

    // $ANTLR start "T_BLOCK"
    public final void mT_BLOCK() throws RecognitionException {
        try {
            int _type = T_BLOCK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:493:17: ( 'BLOCK' )
            // FortranLexer.g:493:25: 'BLOCK'
            {
            match("BLOCK"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_BLOCK"

    // $ANTLR start "T_BLOCKDATA"
    public final void mT_BLOCKDATA() throws RecognitionException {
        try {
            int _type = T_BLOCKDATA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:494:17: ( 'BLOCKDATA' )
            // FortranLexer.g:494:25: 'BLOCKDATA'
            {
            match("BLOCKDATA"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_BLOCKDATA"

    // $ANTLR start "T_CALL"
    public final void mT_CALL() throws RecognitionException {
        try {
            int _type = T_CALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:495:17: ( 'CALL' )
            // FortranLexer.g:495:25: 'CALL'
            {
            match("CALL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CALL"

    // $ANTLR start "T_CASE"
    public final void mT_CASE() throws RecognitionException {
        try {
            int _type = T_CASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:496:17: ( 'CASE' )
            // FortranLexer.g:496:25: 'CASE'
            {
            match("CASE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CASE"

    // $ANTLR start "T_CLASS"
    public final void mT_CLASS() throws RecognitionException {
        try {
            int _type = T_CLASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:497:17: ( 'CLASS' )
            // FortranLexer.g:497:25: 'CLASS'
            {
            match("CLASS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CLASS"

    // $ANTLR start "T_CLOSE"
    public final void mT_CLOSE() throws RecognitionException {
        try {
            int _type = T_CLOSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:498:17: ( 'CLOSE' )
            // FortranLexer.g:498:25: 'CLOSE'
            {
            match("CLOSE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CLOSE"

    // $ANTLR start "T_COMMON"
    public final void mT_COMMON() throws RecognitionException {
        try {
            int _type = T_COMMON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:499:17: ( 'COMMON' )
            // FortranLexer.g:499:25: 'COMMON'
            {
            match("COMMON"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_COMMON"

    // $ANTLR start "T_CONTAINS"
    public final void mT_CONTAINS() throws RecognitionException {
        try {
            int _type = T_CONTAINS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:500:17: ( 'CONTAINS' )
            // FortranLexer.g:500:25: 'CONTAINS'
            {
            match("CONTAINS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CONTAINS"

    // $ANTLR start "T_CONTINUE"
    public final void mT_CONTINUE() throws RecognitionException {
        try {
            int _type = T_CONTINUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:501:17: ( 'CONTINUE' )
            // FortranLexer.g:501:25: 'CONTINUE'
            {
            match("CONTINUE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CONTINUE"

    // $ANTLR start "T_CYCLE"
    public final void mT_CYCLE() throws RecognitionException {
        try {
            int _type = T_CYCLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:502:17: ( 'CYCLE' )
            // FortranLexer.g:502:25: 'CYCLE'
            {
            match("CYCLE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CYCLE"

    // $ANTLR start "T_DATA"
    public final void mT_DATA() throws RecognitionException {
        try {
            int _type = T_DATA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:503:17: ( 'DATA' )
            // FortranLexer.g:503:25: 'DATA'
            {
            match("DATA"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DATA"

    // $ANTLR start "T_DEFAULT"
    public final void mT_DEFAULT() throws RecognitionException {
        try {
            int _type = T_DEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:504:17: ( 'DEFAULT' )
            // FortranLexer.g:504:25: 'DEFAULT'
            {
            match("DEFAULT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DEFAULT"

    // $ANTLR start "T_DEALLOCATE"
    public final void mT_DEALLOCATE() throws RecognitionException {
        try {
            int _type = T_DEALLOCATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:505:17: ( 'DEALLOCATE' )
            // FortranLexer.g:505:25: 'DEALLOCATE'
            {
            match("DEALLOCATE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DEALLOCATE"

    // $ANTLR start "T_DEFERRED"
    public final void mT_DEFERRED() throws RecognitionException {
        try {
            int _type = T_DEFERRED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:506:17: ( 'DEFERRED' )
            // FortranLexer.g:506:25: 'DEFERRED'
            {
            match("DEFERRED"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DEFERRED"

    // $ANTLR start "T_DO"
    public final void mT_DO() throws RecognitionException {
        try {
            int _type = T_DO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:507:17: ( 'DO' )
            // FortranLexer.g:507:25: 'DO'
            {
            match("DO"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DO"

    // $ANTLR start "T_DOUBLE"
    public final void mT_DOUBLE() throws RecognitionException {
        try {
            int _type = T_DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:508:17: ( 'DOUBLE' )
            // FortranLexer.g:508:25: 'DOUBLE'
            {
            match("DOUBLE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DOUBLE"

    // $ANTLR start "T_DOUBLEPRECISION"
    public final void mT_DOUBLEPRECISION() throws RecognitionException {
        try {
            int _type = T_DOUBLEPRECISION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:509:18: ( 'DOUBLEPRECISION' )
            // FortranLexer.g:509:25: 'DOUBLEPRECISION'
            {
            match("DOUBLEPRECISION"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DOUBLEPRECISION"

    // $ANTLR start "T_DOUBLECOMPLEX"
    public final void mT_DOUBLECOMPLEX() throws RecognitionException {
        try {
            int _type = T_DOUBLECOMPLEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:510:16: ( 'DOUBLECOMPLEX' )
            // FortranLexer.g:510:25: 'DOUBLECOMPLEX'
            {
            match("DOUBLECOMPLEX"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DOUBLECOMPLEX"

    // $ANTLR start "T_ELEMENTAL"
    public final void mT_ELEMENTAL() throws RecognitionException {
        try {
            int _type = T_ELEMENTAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:511:17: ( 'ELEMENTAL' )
            // FortranLexer.g:511:25: 'ELEMENTAL'
            {
            match("ELEMENTAL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ELEMENTAL"

    // $ANTLR start "T_ELSE"
    public final void mT_ELSE() throws RecognitionException {
        try {
            int _type = T_ELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:512:17: ( 'ELSE' )
            // FortranLexer.g:512:25: 'ELSE'
            {
            match("ELSE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ELSE"

    // $ANTLR start "T_ELSEIF"
    public final void mT_ELSEIF() throws RecognitionException {
        try {
            int _type = T_ELSEIF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:513:17: ( 'ELSEIF' )
            // FortranLexer.g:513:25: 'ELSEIF'
            {
            match("ELSEIF"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ELSEIF"

    // $ANTLR start "T_ELSEWHERE"
    public final void mT_ELSEWHERE() throws RecognitionException {
        try {
            int _type = T_ELSEWHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:514:17: ( 'ELSEWHERE' )
            // FortranLexer.g:514:25: 'ELSEWHERE'
            {
            match("ELSEWHERE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ELSEWHERE"

    // $ANTLR start "T_ENTRY"
    public final void mT_ENTRY() throws RecognitionException {
        try {
            int _type = T_ENTRY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:515:17: ( 'ENTRY' )
            // FortranLexer.g:515:25: 'ENTRY'
            {
            match("ENTRY"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENTRY"

    // $ANTLR start "T_ENUM"
    public final void mT_ENUM() throws RecognitionException {
        try {
            int _type = T_ENUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:516:17: ( 'ENUM' )
            // FortranLexer.g:516:25: 'ENUM'
            {
            match("ENUM"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENUM"

    // $ANTLR start "T_ENUMERATOR"
    public final void mT_ENUMERATOR() throws RecognitionException {
        try {
            int _type = T_ENUMERATOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:517:17: ( 'ENUMERATOR' )
            // FortranLexer.g:517:25: 'ENUMERATOR'
            {
            match("ENUMERATOR"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENUMERATOR"

    // $ANTLR start "T_EQUIVALENCE"
    public final void mT_EQUIVALENCE() throws RecognitionException {
        try {
            int _type = T_EQUIVALENCE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:518:17: ( 'EQUIVALENCE' )
            // FortranLexer.g:518:25: 'EQUIVALENCE'
            {
            match("EQUIVALENCE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EQUIVALENCE"

    // $ANTLR start "T_EXIT"
    public final void mT_EXIT() throws RecognitionException {
        try {
            int _type = T_EXIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:519:17: ( 'EXIT' )
            // FortranLexer.g:519:25: 'EXIT'
            {
            match("EXIT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EXIT"

    // $ANTLR start "T_EXTENDS"
    public final void mT_EXTENDS() throws RecognitionException {
        try {
            int _type = T_EXTENDS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:520:17: ( 'EXTENDS' )
            // FortranLexer.g:520:25: 'EXTENDS'
            {
            match("EXTENDS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EXTENDS"

    // $ANTLR start "T_EXTERNAL"
    public final void mT_EXTERNAL() throws RecognitionException {
        try {
            int _type = T_EXTERNAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:521:17: ( 'EXTERNAL' )
            // FortranLexer.g:521:25: 'EXTERNAL'
            {
            match("EXTERNAL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EXTERNAL"

    // $ANTLR start "T_FILE"
    public final void mT_FILE() throws RecognitionException {
        try {
            int _type = T_FILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:522:17: ( 'FILE' )
            // FortranLexer.g:522:25: 'FILE'
            {
            match("FILE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FILE"

    // $ANTLR start "T_FINAL"
    public final void mT_FINAL() throws RecognitionException {
        try {
            int _type = T_FINAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:523:17: ( 'FINAL' )
            // FortranLexer.g:523:25: 'FINAL'
            {
            match("FINAL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FINAL"

    // $ANTLR start "T_FLUSH"
    public final void mT_FLUSH() throws RecognitionException {
        try {
            int _type = T_FLUSH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:524:17: ( 'FLUSH' )
            // FortranLexer.g:524:25: 'FLUSH'
            {
            match("FLUSH"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FLUSH"

    // $ANTLR start "T_FORALL"
    public final void mT_FORALL() throws RecognitionException {
        try {
            int _type = T_FORALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:525:17: ( 'FORALL' )
            // FortranLexer.g:525:25: 'FORALL'
            {
            match("FORALL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FORALL"

    // $ANTLR start "T_FORMAT"
    public final void mT_FORMAT() throws RecognitionException {
        try {
            int _type = T_FORMAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:526:17: ( 'FORMAT' )
            // FortranLexer.g:526:25: 'FORMAT'
            {
            match("FORMAT"); 

             inFormat = true; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FORMAT"

    // $ANTLR start "T_FORMATTED"
    public final void mT_FORMATTED() throws RecognitionException {
        try {
            int _type = T_FORMATTED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:527:17: ( 'FORMATTED' )
            // FortranLexer.g:527:25: 'FORMATTED'
            {
            match("FORMATTED"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FORMATTED"

    // $ANTLR start "T_FUNCTION"
    public final void mT_FUNCTION() throws RecognitionException {
        try {
            int _type = T_FUNCTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:528:17: ( 'FUNCTION' )
            // FortranLexer.g:528:25: 'FUNCTION'
            {
            match("FUNCTION"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FUNCTION"

    // $ANTLR start "T_GENERIC"
    public final void mT_GENERIC() throws RecognitionException {
        try {
            int _type = T_GENERIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:529:17: ( 'GENERIC' )
            // FortranLexer.g:529:25: 'GENERIC'
            {
            match("GENERIC"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_GENERIC"

    // $ANTLR start "T_GO"
    public final void mT_GO() throws RecognitionException {
        try {
            int _type = T_GO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:530:17: ( 'GO' )
            // FortranLexer.g:530:25: 'GO'
            {
            match("GO"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_GO"

    // $ANTLR start "T_GOTO"
    public final void mT_GOTO() throws RecognitionException {
        try {
            int _type = T_GOTO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:531:17: ( 'GOTO' )
            // FortranLexer.g:531:25: 'GOTO'
            {
            match("GOTO"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_GOTO"

    // $ANTLR start "T_IF"
    public final void mT_IF() throws RecognitionException {
        try {
            int _type = T_IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:532:17: ( 'IF' )
            // FortranLexer.g:532:25: 'IF'
            {
            match("IF"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_IF"

    // $ANTLR start "T_IMPLICIT"
    public final void mT_IMPLICIT() throws RecognitionException {
        try {
            int _type = T_IMPLICIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:533:17: ( 'IMPLICIT' )
            // FortranLexer.g:533:25: 'IMPLICIT'
            {
            match("IMPLICIT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_IMPLICIT"

    // $ANTLR start "T_IMPORT"
    public final void mT_IMPORT() throws RecognitionException {
        try {
            int _type = T_IMPORT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:534:17: ( 'IMPORT' )
            // FortranLexer.g:534:25: 'IMPORT'
            {
            match("IMPORT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_IMPORT"

    // $ANTLR start "T_IN"
    public final void mT_IN() throws RecognitionException {
        try {
            int _type = T_IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:535:17: ( 'IN' )
            // FortranLexer.g:535:25: 'IN'
            {
            match("IN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_IN"

    // $ANTLR start "T_INOUT"
    public final void mT_INOUT() throws RecognitionException {
        try {
            int _type = T_INOUT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:536:17: ( 'INOUT' )
            // FortranLexer.g:536:25: 'INOUT'
            {
            match("INOUT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_INOUT"

    // $ANTLR start "T_INTENT"
    public final void mT_INTENT() throws RecognitionException {
        try {
            int _type = T_INTENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:537:17: ( 'INTENT' )
            // FortranLexer.g:537:25: 'INTENT'
            {
            match("INTENT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_INTENT"

    // $ANTLR start "T_INTERFACE"
    public final void mT_INTERFACE() throws RecognitionException {
        try {
            int _type = T_INTERFACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:538:17: ( 'INTERFACE' )
            // FortranLexer.g:538:25: 'INTERFACE'
            {
            match("INTERFACE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_INTERFACE"

    // $ANTLR start "T_INTRINSIC"
    public final void mT_INTRINSIC() throws RecognitionException {
        try {
            int _type = T_INTRINSIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:539:17: ( 'INTRINSIC' )
            // FortranLexer.g:539:25: 'INTRINSIC'
            {
            match("INTRINSIC"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_INTRINSIC"

    // $ANTLR start "T_INQUIRE"
    public final void mT_INQUIRE() throws RecognitionException {
        try {
            int _type = T_INQUIRE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:540:17: ( 'INQUIRE' )
            // FortranLexer.g:540:25: 'INQUIRE'
            {
            match("INQUIRE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_INQUIRE"

    // $ANTLR start "T_MODULE"
    public final void mT_MODULE() throws RecognitionException {
        try {
            int _type = T_MODULE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:541:17: ( 'MODULE' )
            // FortranLexer.g:541:25: 'MODULE'
            {
            match("MODULE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_MODULE"

    // $ANTLR start "T_NAMELIST"
    public final void mT_NAMELIST() throws RecognitionException {
        try {
            int _type = T_NAMELIST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:542:17: ( 'NAMELIST' )
            // FortranLexer.g:542:25: 'NAMELIST'
            {
            match("NAMELIST"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NAMELIST"

    // $ANTLR start "T_NONE"
    public final void mT_NONE() throws RecognitionException {
        try {
            int _type = T_NONE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:543:17: ( 'NONE' )
            // FortranLexer.g:543:25: 'NONE'
            {
            match("NONE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NONE"

    // $ANTLR start "T_NON_INTRINSIC"
    public final void mT_NON_INTRINSIC() throws RecognitionException {
        try {
            int _type = T_NON_INTRINSIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:544:17: ( 'NON_INTRINSIC' )
            // FortranLexer.g:544:25: 'NON_INTRINSIC'
            {
            match("NON_INTRINSIC"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NON_INTRINSIC"

    // $ANTLR start "T_NON_OVERRIDABLE"
    public final void mT_NON_OVERRIDABLE() throws RecognitionException {
        try {
            int _type = T_NON_OVERRIDABLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:545:18: ( 'NON_OVERRIDABLE' )
            // FortranLexer.g:545:25: 'NON_OVERRIDABLE'
            {
            match("NON_OVERRIDABLE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NON_OVERRIDABLE"

    // $ANTLR start "T_NOPASS"
    public final void mT_NOPASS() throws RecognitionException {
        try {
            int _type = T_NOPASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:546:17: ( 'NOPASS' )
            // FortranLexer.g:546:25: 'NOPASS'
            {
            match("NOPASS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NOPASS"

    // $ANTLR start "T_NULLIFY"
    public final void mT_NULLIFY() throws RecognitionException {
        try {
            int _type = T_NULLIFY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:547:17: ( 'NULLIFY' )
            // FortranLexer.g:547:25: 'NULLIFY'
            {
            match("NULLIFY"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_NULLIFY"

    // $ANTLR start "T_ONLY"
    public final void mT_ONLY() throws RecognitionException {
        try {
            int _type = T_ONLY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:548:17: ( 'ONLY' )
            // FortranLexer.g:548:25: 'ONLY'
            {
            match("ONLY"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ONLY"

    // $ANTLR start "T_OPEN"
    public final void mT_OPEN() throws RecognitionException {
        try {
            int _type = T_OPEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:549:17: ( 'OPEN' )
            // FortranLexer.g:549:25: 'OPEN'
            {
            match("OPEN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_OPEN"

    // $ANTLR start "T_OPERATOR"
    public final void mT_OPERATOR() throws RecognitionException {
        try {
            int _type = T_OPERATOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:550:17: ( 'OPERATOR' )
            // FortranLexer.g:550:25: 'OPERATOR'
            {
            match("OPERATOR"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_OPERATOR"

    // $ANTLR start "T_OPTIONAL"
    public final void mT_OPTIONAL() throws RecognitionException {
        try {
            int _type = T_OPTIONAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:551:17: ( 'OPTIONAL' )
            // FortranLexer.g:551:25: 'OPTIONAL'
            {
            match("OPTIONAL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_OPTIONAL"

    // $ANTLR start "T_OUT"
    public final void mT_OUT() throws RecognitionException {
        try {
            int _type = T_OUT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:552:17: ( 'OUT' )
            // FortranLexer.g:552:25: 'OUT'
            {
            match("OUT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_OUT"

    // $ANTLR start "T_PARAMETER"
    public final void mT_PARAMETER() throws RecognitionException {
        try {
            int _type = T_PARAMETER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:553:17: ( 'PARAMETER' )
            // FortranLexer.g:553:25: 'PARAMETER'
            {
            match("PARAMETER"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PARAMETER"

    // $ANTLR start "T_PASS"
    public final void mT_PASS() throws RecognitionException {
        try {
            int _type = T_PASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:554:17: ( 'PASS' )
            // FortranLexer.g:554:25: 'PASS'
            {
            match("PASS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PASS"

    // $ANTLR start "T_PAUSE"
    public final void mT_PAUSE() throws RecognitionException {
        try {
            int _type = T_PAUSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:555:17: ( 'PAUSE' )
            // FortranLexer.g:555:25: 'PAUSE'
            {
            match("PAUSE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PAUSE"

    // $ANTLR start "T_POINTER"
    public final void mT_POINTER() throws RecognitionException {
        try {
            int _type = T_POINTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:556:17: ( 'POINTER' )
            // FortranLexer.g:556:25: 'POINTER'
            {
            match("POINTER"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_POINTER"

    // $ANTLR start "T_PRINT"
    public final void mT_PRINT() throws RecognitionException {
        try {
            int _type = T_PRINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:557:17: ( 'PRINT' )
            // FortranLexer.g:557:25: 'PRINT'
            {
            match("PRINT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PRINT"

    // $ANTLR start "T_PRECISION"
    public final void mT_PRECISION() throws RecognitionException {
        try {
            int _type = T_PRECISION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:558:17: ( 'PRECISION' )
            // FortranLexer.g:558:25: 'PRECISION'
            {
            match("PRECISION"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PRECISION"

    // $ANTLR start "T_PRIVATE"
    public final void mT_PRIVATE() throws RecognitionException {
        try {
            int _type = T_PRIVATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:559:17: ( 'PRIVATE' )
            // FortranLexer.g:559:25: 'PRIVATE'
            {
            match("PRIVATE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PRIVATE"

    // $ANTLR start "T_PROCEDURE"
    public final void mT_PROCEDURE() throws RecognitionException {
        try {
            int _type = T_PROCEDURE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:560:17: ( 'PROCEDURE' )
            // FortranLexer.g:560:25: 'PROCEDURE'
            {
            match("PROCEDURE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PROCEDURE"

    // $ANTLR start "T_PROGRAM"
    public final void mT_PROGRAM() throws RecognitionException {
        try {
            int _type = T_PROGRAM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:561:17: ( 'PROGRAM' )
            // FortranLexer.g:561:25: 'PROGRAM'
            {
            match("PROGRAM"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PROGRAM"

    // $ANTLR start "T_PROTECTED"
    public final void mT_PROTECTED() throws RecognitionException {
        try {
            int _type = T_PROTECTED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:562:17: ( 'PROTECTED' )
            // FortranLexer.g:562:25: 'PROTECTED'
            {
            match("PROTECTED"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PROTECTED"

    // $ANTLR start "T_PUBLIC"
    public final void mT_PUBLIC() throws RecognitionException {
        try {
            int _type = T_PUBLIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:563:17: ( 'PUBLIC' )
            // FortranLexer.g:563:25: 'PUBLIC'
            {
            match("PUBLIC"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PUBLIC"

    // $ANTLR start "T_PURE"
    public final void mT_PURE() throws RecognitionException {
        try {
            int _type = T_PURE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:564:17: ( 'PURE' )
            // FortranLexer.g:564:25: 'PURE'
            {
            match("PURE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PURE"

    // $ANTLR start "T_READ"
    public final void mT_READ() throws RecognitionException {
        try {
            int _type = T_READ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:565:17: ( 'READ' )
            // FortranLexer.g:565:25: 'READ'
            {
            match("READ"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_READ"

    // $ANTLR start "T_RECURSIVE"
    public final void mT_RECURSIVE() throws RecognitionException {
        try {
            int _type = T_RECURSIVE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:566:17: ( 'RECURSIVE' )
            // FortranLexer.g:566:25: 'RECURSIVE'
            {
            match("RECURSIVE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_RECURSIVE"

    // $ANTLR start "T_RESULT"
    public final void mT_RESULT() throws RecognitionException {
        try {
            int _type = T_RESULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:567:17: ( 'RESULT' )
            // FortranLexer.g:567:25: 'RESULT'
            {
            match("RESULT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_RESULT"

    // $ANTLR start "T_RETURN"
    public final void mT_RETURN() throws RecognitionException {
        try {
            int _type = T_RETURN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:568:17: ( 'RETURN' )
            // FortranLexer.g:568:25: 'RETURN'
            {
            match("RETURN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_RETURN"

    // $ANTLR start "T_REWIND"
    public final void mT_REWIND() throws RecognitionException {
        try {
            int _type = T_REWIND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:569:17: ( 'REWIND' )
            // FortranLexer.g:569:25: 'REWIND'
            {
            match("REWIND"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_REWIND"

    // $ANTLR start "T_SAVE"
    public final void mT_SAVE() throws RecognitionException {
        try {
            int _type = T_SAVE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:570:17: ( 'SAVE' )
            // FortranLexer.g:570:25: 'SAVE'
            {
            match("SAVE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SAVE"

    // $ANTLR start "T_SELECT"
    public final void mT_SELECT() throws RecognitionException {
        try {
            int _type = T_SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:571:17: ( 'SELECT' )
            // FortranLexer.g:571:25: 'SELECT'
            {
            match("SELECT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SELECT"

    // $ANTLR start "T_SELECTCASE"
    public final void mT_SELECTCASE() throws RecognitionException {
        try {
            int _type = T_SELECTCASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:572:17: ( 'SELECTCASE' )
            // FortranLexer.g:572:25: 'SELECTCASE'
            {
            match("SELECTCASE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SELECTCASE"

    // $ANTLR start "T_SELECTTYPE"
    public final void mT_SELECTTYPE() throws RecognitionException {
        try {
            int _type = T_SELECTTYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:573:17: ( 'SELECTTYPE' )
            // FortranLexer.g:573:25: 'SELECTTYPE'
            {
            match("SELECTTYPE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SELECTTYPE"

    // $ANTLR start "T_SEQUENCE"
    public final void mT_SEQUENCE() throws RecognitionException {
        try {
            int _type = T_SEQUENCE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:574:17: ( 'SEQUENCE' )
            // FortranLexer.g:574:25: 'SEQUENCE'
            {
            match("SEQUENCE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SEQUENCE"

    // $ANTLR start "T_STOP"
    public final void mT_STOP() throws RecognitionException {
        try {
            int _type = T_STOP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:575:17: ( 'STOP' )
            // FortranLexer.g:575:25: 'STOP'
            {
            match("STOP"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_STOP"

    // $ANTLR start "T_SUBROUTINE"
    public final void mT_SUBROUTINE() throws RecognitionException {
        try {
            int _type = T_SUBROUTINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:576:17: ( 'SUBROUTINE' )
            // FortranLexer.g:576:25: 'SUBROUTINE'
            {
            match("SUBROUTINE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_SUBROUTINE"

    // $ANTLR start "T_TARGET"
    public final void mT_TARGET() throws RecognitionException {
        try {
            int _type = T_TARGET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:577:17: ( 'TARGET' )
            // FortranLexer.g:577:25: 'TARGET'
            {
            match("TARGET"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_TARGET"

    // $ANTLR start "T_THEN"
    public final void mT_THEN() throws RecognitionException {
        try {
            int _type = T_THEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:578:17: ( 'THEN' )
            // FortranLexer.g:578:25: 'THEN'
            {
            match("THEN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_THEN"

    // $ANTLR start "T_TO"
    public final void mT_TO() throws RecognitionException {
        try {
            int _type = T_TO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:579:17: ( 'TO' )
            // FortranLexer.g:579:25: 'TO'
            {
            match("TO"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_TO"

    // $ANTLR start "T_TYPE"
    public final void mT_TYPE() throws RecognitionException {
        try {
            int _type = T_TYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:580:17: ( 'TYPE' )
            // FortranLexer.g:580:25: 'TYPE'
            {
            match("TYPE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_TYPE"

    // $ANTLR start "T_UNFORMATTED"
    public final void mT_UNFORMATTED() throws RecognitionException {
        try {
            int _type = T_UNFORMATTED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:581:17: ( 'UNFORMATTED' )
            // FortranLexer.g:581:25: 'UNFORMATTED'
            {
            match("UNFORMATTED"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_UNFORMATTED"

    // $ANTLR start "T_USE"
    public final void mT_USE() throws RecognitionException {
        try {
            int _type = T_USE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:582:17: ( 'USE' )
            // FortranLexer.g:582:25: 'USE'
            {
            match("USE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_USE"

    // $ANTLR start "T_VALUE"
    public final void mT_VALUE() throws RecognitionException {
        try {
            int _type = T_VALUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:583:17: ( 'VALUE' )
            // FortranLexer.g:583:25: 'VALUE'
            {
            match("VALUE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_VALUE"

    // $ANTLR start "T_VOLATILE"
    public final void mT_VOLATILE() throws RecognitionException {
        try {
            int _type = T_VOLATILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:584:17: ( 'VOLATILE' )
            // FortranLexer.g:584:25: 'VOLATILE'
            {
            match("VOLATILE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_VOLATILE"

    // $ANTLR start "T_WAIT"
    public final void mT_WAIT() throws RecognitionException {
        try {
            int _type = T_WAIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:585:17: ( 'WAIT' )
            // FortranLexer.g:585:25: 'WAIT'
            {
            match("WAIT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_WAIT"

    // $ANTLR start "T_WHERE"
    public final void mT_WHERE() throws RecognitionException {
        try {
            int _type = T_WHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:586:17: ( 'WHERE' )
            // FortranLexer.g:586:25: 'WHERE'
            {
            match("WHERE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_WHERE"

    // $ANTLR start "T_WHILE"
    public final void mT_WHILE() throws RecognitionException {
        try {
            int _type = T_WHILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:587:17: ( 'WHILE' )
            // FortranLexer.g:587:25: 'WHILE'
            {
            match("WHILE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_WHILE"

    // $ANTLR start "T_WRITE"
    public final void mT_WRITE() throws RecognitionException {
        try {
            int _type = T_WRITE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:588:17: ( 'WRITE' )
            // FortranLexer.g:588:25: 'WRITE'
            {
            match("WRITE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_WRITE"

    // $ANTLR start "T_ENDASSOCIATE"
    public final void mT_ENDASSOCIATE() throws RecognitionException {
        try {
            int _type = T_ENDASSOCIATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:590:17: ( 'ENDASSOCIATE' )
            // FortranLexer.g:590:25: 'ENDASSOCIATE'
            {
            match("ENDASSOCIATE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDASSOCIATE"

    // $ANTLR start "T_ENDBLOCK"
    public final void mT_ENDBLOCK() throws RecognitionException {
        try {
            int _type = T_ENDBLOCK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:591:17: ( 'ENDBLOCK' )
            // FortranLexer.g:591:25: 'ENDBLOCK'
            {
            match("ENDBLOCK"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDBLOCK"

    // $ANTLR start "T_ENDBLOCKDATA"
    public final void mT_ENDBLOCKDATA() throws RecognitionException {
        try {
            int _type = T_ENDBLOCKDATA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:592:17: ( 'ENDBLOCKDATA' )
            // FortranLexer.g:592:25: 'ENDBLOCKDATA'
            {
            match("ENDBLOCKDATA"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDBLOCKDATA"

    // $ANTLR start "T_ENDDO"
    public final void mT_ENDDO() throws RecognitionException {
        try {
            int _type = T_ENDDO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:593:17: ( 'ENDDO' )
            // FortranLexer.g:593:25: 'ENDDO'
            {
            match("ENDDO"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDDO"

    // $ANTLR start "T_ENDENUM"
    public final void mT_ENDENUM() throws RecognitionException {
        try {
            int _type = T_ENDENUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:594:17: ( 'ENDENUM' )
            // FortranLexer.g:594:25: 'ENDENUM'
            {
            match("ENDENUM"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDENUM"

    // $ANTLR start "T_ENDFORALL"
    public final void mT_ENDFORALL() throws RecognitionException {
        try {
            int _type = T_ENDFORALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:595:17: ( 'ENDFORALL' )
            // FortranLexer.g:595:25: 'ENDFORALL'
            {
            match("ENDFORALL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDFORALL"

    // $ANTLR start "T_ENDFILE"
    public final void mT_ENDFILE() throws RecognitionException {
        try {
            int _type = T_ENDFILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:596:17: ( 'ENDFILE' )
            // FortranLexer.g:596:25: 'ENDFILE'
            {
            match("ENDFILE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDFILE"

    // $ANTLR start "T_ENDFUNCTION"
    public final void mT_ENDFUNCTION() throws RecognitionException {
        try {
            int _type = T_ENDFUNCTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:597:17: ( 'ENDFUNCTION' )
            // FortranLexer.g:597:25: 'ENDFUNCTION'
            {
            match("ENDFUNCTION"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDFUNCTION"

    // $ANTLR start "T_ENDIF"
    public final void mT_ENDIF() throws RecognitionException {
        try {
            int _type = T_ENDIF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:598:17: ( 'ENDIF' )
            // FortranLexer.g:598:25: 'ENDIF'
            {
            match("ENDIF"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDIF"

    // $ANTLR start "T_ENDINTERFACE"
    public final void mT_ENDINTERFACE() throws RecognitionException {
        try {
            int _type = T_ENDINTERFACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:599:17: ( 'ENDINTERFACE' )
            // FortranLexer.g:599:25: 'ENDINTERFACE'
            {
            match("ENDINTERFACE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDINTERFACE"

    // $ANTLR start "T_ENDMODULE"
    public final void mT_ENDMODULE() throws RecognitionException {
        try {
            int _type = T_ENDMODULE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:600:17: ( 'ENDMODULE' )
            // FortranLexer.g:600:25: 'ENDMODULE'
            {
            match("ENDMODULE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDMODULE"

    // $ANTLR start "T_ENDPROGRAM"
    public final void mT_ENDPROGRAM() throws RecognitionException {
        try {
            int _type = T_ENDPROGRAM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:601:17: ( 'ENDPROGRAM' )
            // FortranLexer.g:601:25: 'ENDPROGRAM'
            {
            match("ENDPROGRAM"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDPROGRAM"

    // $ANTLR start "T_ENDSELECT"
    public final void mT_ENDSELECT() throws RecognitionException {
        try {
            int _type = T_ENDSELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:602:17: ( 'ENDSELECT' )
            // FortranLexer.g:602:25: 'ENDSELECT'
            {
            match("ENDSELECT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDSELECT"

    // $ANTLR start "T_ENDSUBROUTINE"
    public final void mT_ENDSUBROUTINE() throws RecognitionException {
        try {
            int _type = T_ENDSUBROUTINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:603:17: ( 'ENDSUBROUTINE' )
            // FortranLexer.g:603:25: 'ENDSUBROUTINE'
            {
            match("ENDSUBROUTINE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDSUBROUTINE"

    // $ANTLR start "T_ENDTYPE"
    public final void mT_ENDTYPE() throws RecognitionException {
        try {
            int _type = T_ENDTYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:604:17: ( 'ENDTYPE' )
            // FortranLexer.g:604:25: 'ENDTYPE'
            {
            match("ENDTYPE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDTYPE"

    // $ANTLR start "T_ENDWHERE"
    public final void mT_ENDWHERE() throws RecognitionException {
        try {
            int _type = T_ENDWHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:605:17: ( 'ENDWHERE' )
            // FortranLexer.g:605:25: 'ENDWHERE'
            {
            match("ENDWHERE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ENDWHERE"

    // $ANTLR start "T_END"
    public final void mT_END() throws RecognitionException {
        try {
            int _type = T_END;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:607:9: ( 'END' )
            // FortranLexer.g:607:11: 'END'
            {
            match("END"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_END"

    // $ANTLR start "T_DIMENSION"
    public final void mT_DIMENSION() throws RecognitionException {
        try {
            int _type = T_DIMENSION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:610:17: ( 'DIMENSION' )
            // FortranLexer.g:610:25: 'DIMENSION'
            {
            match("DIMENSION"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DIMENSION"

    // $ANTLR start "T_KIND"
    public final void mT_KIND() throws RecognitionException {
        try {
            int _type = T_KIND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:612:8: ( 'KIND' )
            // FortranLexer.g:612:10: 'KIND'
            {
            match("KIND"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_KIND"

    // $ANTLR start "T_LEN"
    public final void mT_LEN() throws RecognitionException {
        try {
            int _type = T_LEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:613:7: ( 'LEN' )
            // FortranLexer.g:613:9: 'LEN'
            {
            match("LEN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LEN"

    // $ANTLR start "T_BIND"
    public final void mT_BIND() throws RecognitionException {
        try {
            int _type = T_BIND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:615:8: ( 'BIND' )
            // FortranLexer.g:615:10: 'BIND'
            {
            match("BIND"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_BIND"

    // $ANTLR start "T_HOLLERITH"
    public final void mT_HOLLERITH() throws RecognitionException {
        try {
            int _type = T_HOLLERITH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            Token Digit_String1=null;

            // FortranLexer.g:617:13: ( Digit_String 'H' )
            // FortranLexer.g:617:15: Digit_String 'H'
            {
            int Digit_String1Start4814 = getCharIndex();
            mDigit_String(); 
            Digit_String1 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, Digit_String1Start4814, getCharIndex()-1);
            match('H'); 
             
                    // If we're inside a format stmt we don't want to process it as 
                    // a Hollerith constant because it's most likely an H-edit descriptor. 
                    // However, the H-edit descriptor needs processed the same way both 
                    // here and in the prepass.
                    StringBuffer hollConst = new StringBuffer();
                    int count = Integer.parseInt((Digit_String1!=null?Digit_String1.getText():null));

                    for(int i = 0; i < count; i++) 
                       hollConst = hollConst.append((char)input.LA(i+1));
                    for(int i = 0; i < count; i++)
                       // consume the character so the lexer doesn't try matching it.
                       input.consume();
                

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_HOLLERITH"

    // $ANTLR start "T_DEFINED_OP"
    public final void mT_DEFINED_OP() throws RecognitionException {
        try {
            int _type = T_DEFINED_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:637:5: ( '.' ( Letter )+ '.' )
            // FortranLexer.g:637:10: '.' ( Letter )+ '.'
            {
            match('.'); 
            // FortranLexer.g:637:14: ( Letter )+
            int cnt29=0;
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( ((LA29_0>='A' && LA29_0<='Z')||(LA29_0>='a' && LA29_0<='z')) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // FortranLexer.g:637:14: Letter
            	    {
            	    mLetter(); 

            	    }
            	    break;

            	default :
            	    if ( cnt29 >= 1 ) break loop29;
                        EarlyExitException eee =
                            new EarlyExitException(29, input);
                        throw eee;
                }
                cnt29++;
            } while (true);

            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DEFINED_OP"

    // $ANTLR start "T_LABEL_DO_TERMINAL"
    public final void mT_LABEL_DO_TERMINAL() throws RecognitionException {
        try {
            int _type = T_LABEL_DO_TERMINAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:650:9: ( '__LABEL_DO_TERMINAL__' )
            // FortranLexer.g:650:17: '__LABEL_DO_TERMINAL__'
            {
            match("__LABEL_DO_TERMINAL__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_LABEL_DO_TERMINAL"

    // $ANTLR start "T_DATA_EDIT_DESC"
    public final void mT_DATA_EDIT_DESC() throws RecognitionException {
        try {
            int _type = T_DATA_EDIT_DESC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:653:18: ( '__T_DATA_EDIT_DESC__' )
            // FortranLexer.g:653:20: '__T_DATA_EDIT_DESC__'
            {
            match("__T_DATA_EDIT_DESC__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_DATA_EDIT_DESC"

    // $ANTLR start "T_CONTROL_EDIT_DESC"
    public final void mT_CONTROL_EDIT_DESC() throws RecognitionException {
        try {
            int _type = T_CONTROL_EDIT_DESC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:654:21: ( '__T_CONTROL_EDIT_DESC__' )
            // FortranLexer.g:654:23: '__T_CONTROL_EDIT_DESC__'
            {
            match("__T_CONTROL_EDIT_DESC__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CONTROL_EDIT_DESC"

    // $ANTLR start "T_CHAR_STRING_EDIT_DESC"
    public final void mT_CHAR_STRING_EDIT_DESC() throws RecognitionException {
        try {
            int _type = T_CHAR_STRING_EDIT_DESC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:655:25: ( '__T_CHAR_STRING_EDIT_DESC__' )
            // FortranLexer.g:655:27: '__T_CHAR_STRING_EDIT_DESC__'
            {
            match("__T_CHAR_STRING_EDIT_DESC__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_CHAR_STRING_EDIT_DESC"

    // $ANTLR start "T_STMT_FUNCTION"
    public final void mT_STMT_FUNCTION() throws RecognitionException {
        try {
            int _type = T_STMT_FUNCTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:658:9: ( 'STMT_FUNCTION' )
            // FortranLexer.g:658:17: 'STMT_FUNCTION'
            {
            match("STMT_FUNCTION"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_STMT_FUNCTION"

    // $ANTLR start "T_ASSIGNMENT_STMT"
    public final void mT_ASSIGNMENT_STMT() throws RecognitionException {
        try {
            int _type = T_ASSIGNMENT_STMT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:661:19: ( '__T_ASSIGNMENT_STMT__' )
            // FortranLexer.g:661:21: '__T_ASSIGNMENT_STMT__'
            {
            match("__T_ASSIGNMENT_STMT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ASSIGNMENT_STMT"

    // $ANTLR start "T_PTR_ASSIGNMENT_STMT"
    public final void mT_PTR_ASSIGNMENT_STMT() throws RecognitionException {
        try {
            int _type = T_PTR_ASSIGNMENT_STMT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:662:23: ( '__T_PTR_ASSIGNMENT_STMT__' )
            // FortranLexer.g:662:25: '__T_PTR_ASSIGNMENT_STMT__'
            {
            match("__T_PTR_ASSIGNMENT_STMT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_PTR_ASSIGNMENT_STMT"

    // $ANTLR start "T_ARITHMETIC_IF_STMT"
    public final void mT_ARITHMETIC_IF_STMT() throws RecognitionException {
        try {
            int _type = T_ARITHMETIC_IF_STMT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:663:22: ( '__T_ARITHMETIC_IF_STMT__' )
            // FortranLexer.g:663:24: '__T_ARITHMETIC_IF_STMT__'
            {
            match("__T_ARITHMETIC_IF_STMT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ARITHMETIC_IF_STMT"

    // $ANTLR start "T_ALLOCATE_STMT_1"
    public final void mT_ALLOCATE_STMT_1() throws RecognitionException {
        try {
            int _type = T_ALLOCATE_STMT_1;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:664:19: ( '__T_ALLOCATE_STMT_1__' )
            // FortranLexer.g:664:21: '__T_ALLOCATE_STMT_1__'
            {
            match("__T_ALLOCATE_STMT_1__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_ALLOCATE_STMT_1"

    // $ANTLR start "T_WHERE_STMT"
    public final void mT_WHERE_STMT() throws RecognitionException {
        try {
            int _type = T_WHERE_STMT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:665:14: ( '__T_WHERE_STMT__' )
            // FortranLexer.g:665:16: '__T_WHERE_STMT__'
            {
            match("__T_WHERE_STMT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_WHERE_STMT"

    // $ANTLR start "T_IF_STMT"
    public final void mT_IF_STMT() throws RecognitionException {
        try {
            int _type = T_IF_STMT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:666:11: ( '__T_IF_STMT__' )
            // FortranLexer.g:666:13: '__T_IF_STMT__'
            {
            match("__T_IF_STMT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_IF_STMT"

    // $ANTLR start "T_FORALL_STMT"
    public final void mT_FORALL_STMT() throws RecognitionException {
        try {
            int _type = T_FORALL_STMT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:667:15: ( '__T_FORALL_STMT__' )
            // FortranLexer.g:667:17: '__T_FORALL_STMT__'
            {
            match("__T_FORALL_STMT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FORALL_STMT"

    // $ANTLR start "T_WHERE_CONSTRUCT_STMT"
    public final void mT_WHERE_CONSTRUCT_STMT() throws RecognitionException {
        try {
            int _type = T_WHERE_CONSTRUCT_STMT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:668:24: ( '__T_WHERE_CONSTRUCT_STMT__' )
            // FortranLexer.g:668:26: '__T_WHERE_CONSTRUCT_STMT__'
            {
            match("__T_WHERE_CONSTRUCT_STMT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_WHERE_CONSTRUCT_STMT"

    // $ANTLR start "T_FORALL_CONSTRUCT_STMT"
    public final void mT_FORALL_CONSTRUCT_STMT() throws RecognitionException {
        try {
            int _type = T_FORALL_CONSTRUCT_STMT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:669:25: ( '__T_FORALL_CONSTRUCT_STMT__' )
            // FortranLexer.g:669:27: '__T_FORALL_CONSTRUCT_STMT__'
            {
            match("__T_FORALL_CONSTRUCT_STMT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_FORALL_CONSTRUCT_STMT"

    // $ANTLR start "T_INQUIRE_STMT_2"
    public final void mT_INQUIRE_STMT_2() throws RecognitionException {
        try {
            int _type = T_INQUIRE_STMT_2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:670:18: ( '__T_INQUIRE_STMT_2__' )
            // FortranLexer.g:670:20: '__T_INQUIRE_STMT_2__'
            {
            match("__T_INQUIRE_STMT_2__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_INQUIRE_STMT_2"

    // $ANTLR start "T_REAL_CONSTANT"
    public final void mT_REAL_CONSTANT() throws RecognitionException {
        try {
            int _type = T_REAL_CONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:673:17: ( '__T_REAL_CONSTANT__' )
            // FortranLexer.g:673:19: '__T_REAL_CONSTANT__'
            {
            match("__T_REAL_CONSTANT__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_REAL_CONSTANT"

    // $ANTLR start "T_EOF"
    public final void mT_EOF() throws RecognitionException {
        try {
            int _type = T_EOF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:675:6: ( '__T_EOF__' )
            // FortranLexer.g:675:8: '__T_EOF__'
            {
            match("__T_EOF__"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_EOF"

    // $ANTLR start "T_IDENT"
    public final void mT_IDENT() throws RecognitionException {
        try {
            int _type = T_IDENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:680:9: ( Letter ( Alphanumeric_Character )* )
            // FortranLexer.g:680:17: Letter ( Alphanumeric_Character )*
            {
            mLetter(); 
            // FortranLexer.g:680:24: ( Alphanumeric_Character )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( ((LA30_0>='0' && LA30_0<='9')||(LA30_0>='A' && LA30_0<='Z')||LA30_0=='_'||(LA30_0>='a' && LA30_0<='z')) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // FortranLexer.g:680:26: Alphanumeric_Character
            	    {
            	    mAlphanumeric_Character(); 

            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T_IDENT"

    // $ANTLR start "LINE_COMMENT"
    public final void mLINE_COMMENT() throws RecognitionException {
        try {
            int _type = LINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:684:5: ( '!' (~ ( '\\n' | '\\r' ) )* )
            // FortranLexer.g:684:7: '!' (~ ( '\\n' | '\\r' ) )*
            {
            match('!'); 
            // FortranLexer.g:684:12: (~ ( '\\n' | '\\r' ) )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( ((LA31_0>='\u0000' && LA31_0<='\t')||(LA31_0>='\u000B' && LA31_0<='\f')||(LA31_0>='\u000E' && LA31_0<='\uFFFE')) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // FortranLexer.g:684:12: ~ ( '\\n' | '\\r' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);

            // _channel=99;
                        _channel=HIDDEN;
            //             _channel=99;
                    

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LINE_COMMENT"

    // $ANTLR start "MISC_CHAR"
    public final void mMISC_CHAR() throws RecognitionException {
        try {
            int _type = MISC_CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // FortranLexer.g:694:11: (~ ( '\\n' | '\\r' ) )
            // FortranLexer.g:694:13: ~ ( '\\n' | '\\r' )
            {
            if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFE') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MISC_CHAR"

    public void mTokens() throws RecognitionException {
        // FortranLexer.g:1:8: ( T_EOS | CONTINUE_CHAR | T_CHAR_CONSTANT | T_DIGIT_STRING | BINARY_CONSTANT | OCTAL_CONSTANT | HEX_CONSTANT | WS | PREPROCESS_LINE | T_INCLUDE | T_ASTERISK | T_COLON | T_COLON_COLON | T_COMMA | T_EQUALS | T_EQ_EQ | T_EQ_GT | T_GREATERTHAN | T_GREATERTHAN_EQ | T_LESSTHAN | T_LESSTHAN_EQ | T_LBRACKET | T_LPAREN | T_MINUS | T_PERCENT | T_PLUS | T_POWER | T_SLASH | T_SLASH_EQ | T_SLASH_SLASH | T_RBRACKET | T_RPAREN | T_UNDERSCORE | T_EQ | T_NE | T_LT | T_LE | T_GT | T_GE | T_TRUE | T_FALSE | T_NOT | T_AND | T_OR | T_EQV | T_NEQV | T_PERIOD_EXPONENT | T_PERIOD | T_XYZ | T_INTEGER | T_REAL | T_COMPLEX | T_CHARACTER | T_LOGICAL | T_ABSTRACT | T_ALLOCATABLE | T_ALLOCATE | T_ASSIGNMENT | T_ASSIGN | T_ASSOCIATE | T_ASYNCHRONOUS | T_BACKSPACE | T_BLOCK | T_BLOCKDATA | T_CALL | T_CASE | T_CLASS | T_CLOSE | T_COMMON | T_CONTAINS | T_CONTINUE | T_CYCLE | T_DATA | T_DEFAULT | T_DEALLOCATE | T_DEFERRED | T_DO | T_DOUBLE | T_DOUBLEPRECISION | T_DOUBLECOMPLEX | T_ELEMENTAL | T_ELSE | T_ELSEIF | T_ELSEWHERE | T_ENTRY | T_ENUM | T_ENUMERATOR | T_EQUIVALENCE | T_EXIT | T_EXTENDS | T_EXTERNAL | T_FILE | T_FINAL | T_FLUSH | T_FORALL | T_FORMAT | T_FORMATTED | T_FUNCTION | T_GENERIC | T_GO | T_GOTO | T_IF | T_IMPLICIT | T_IMPORT | T_IN | T_INOUT | T_INTENT | T_INTERFACE | T_INTRINSIC | T_INQUIRE | T_MODULE | T_NAMELIST | T_NONE | T_NON_INTRINSIC | T_NON_OVERRIDABLE | T_NOPASS | T_NULLIFY | T_ONLY | T_OPEN | T_OPERATOR | T_OPTIONAL | T_OUT | T_PARAMETER | T_PASS | T_PAUSE | T_POINTER | T_PRINT | T_PRECISION | T_PRIVATE | T_PROCEDURE | T_PROGRAM | T_PROTECTED | T_PUBLIC | T_PURE | T_READ | T_RECURSIVE | T_RESULT | T_RETURN | T_REWIND | T_SAVE | T_SELECT | T_SELECTCASE | T_SELECTTYPE | T_SEQUENCE | T_STOP | T_SUBROUTINE | T_TARGET | T_THEN | T_TO | T_TYPE | T_UNFORMATTED | T_USE | T_VALUE | T_VOLATILE | T_WAIT | T_WHERE | T_WHILE | T_WRITE | T_ENDASSOCIATE | T_ENDBLOCK | T_ENDBLOCKDATA | T_ENDDO | T_ENDENUM | T_ENDFORALL | T_ENDFILE | T_ENDFUNCTION | T_ENDIF | T_ENDINTERFACE | T_ENDMODULE | T_ENDPROGRAM | T_ENDSELECT | T_ENDSUBROUTINE | T_ENDTYPE | T_ENDWHERE | T_END | T_DIMENSION | T_KIND | T_LEN | T_BIND | T_HOLLERITH | T_DEFINED_OP | T_LABEL_DO_TERMINAL | T_DATA_EDIT_DESC | T_CONTROL_EDIT_DESC | T_CHAR_STRING_EDIT_DESC | T_STMT_FUNCTION | T_ASSIGNMENT_STMT | T_PTR_ASSIGNMENT_STMT | T_ARITHMETIC_IF_STMT | T_ALLOCATE_STMT_1 | T_WHERE_STMT | T_IF_STMT | T_FORALL_STMT | T_WHERE_CONSTRUCT_STMT | T_FORALL_CONSTRUCT_STMT | T_INQUIRE_STMT_2 | T_REAL_CONSTANT | T_EOF | T_IDENT | LINE_COMMENT | MISC_CHAR )
        int alt32=201;
        alt32 = dfa32.predict(input);
        switch (alt32) {
            case 1 :
                // FortranLexer.g:1:10: T_EOS
                {
                mT_EOS(); 

                }
                break;
            case 2 :
                // FortranLexer.g:1:16: CONTINUE_CHAR
                {
                mCONTINUE_CHAR(); 

                }
                break;
            case 3 :
                // FortranLexer.g:1:30: T_CHAR_CONSTANT
                {
                mT_CHAR_CONSTANT(); 

                }
                break;
            case 4 :
                // FortranLexer.g:1:46: T_DIGIT_STRING
                {
                mT_DIGIT_STRING(); 

                }
                break;
            case 5 :
                // FortranLexer.g:1:61: BINARY_CONSTANT
                {
                mBINARY_CONSTANT(); 

                }
                break;
            case 6 :
                // FortranLexer.g:1:77: OCTAL_CONSTANT
                {
                mOCTAL_CONSTANT(); 

                }
                break;
            case 7 :
                // FortranLexer.g:1:92: HEX_CONSTANT
                {
                mHEX_CONSTANT(); 

                }
                break;
            case 8 :
                // FortranLexer.g:1:105: WS
                {
                mWS(); 

                }
                break;
            case 9 :
                // FortranLexer.g:1:108: PREPROCESS_LINE
                {
                mPREPROCESS_LINE(); 

                }
                break;
            case 10 :
                // FortranLexer.g:1:124: T_INCLUDE
                {
                mT_INCLUDE(); 

                }
                break;
            case 11 :
                // FortranLexer.g:1:134: T_ASTERISK
                {
                mT_ASTERISK(); 

                }
                break;
            case 12 :
                // FortranLexer.g:1:145: T_COLON
                {
                mT_COLON(); 

                }
                break;
            case 13 :
                // FortranLexer.g:1:153: T_COLON_COLON
                {
                mT_COLON_COLON(); 

                }
                break;
            case 14 :
                // FortranLexer.g:1:167: T_COMMA
                {
                mT_COMMA(); 

                }
                break;
            case 15 :
                // FortranLexer.g:1:175: T_EQUALS
                {
                mT_EQUALS(); 

                }
                break;
            case 16 :
                // FortranLexer.g:1:184: T_EQ_EQ
                {
                mT_EQ_EQ(); 

                }
                break;
            case 17 :
                // FortranLexer.g:1:192: T_EQ_GT
                {
                mT_EQ_GT(); 

                }
                break;
            case 18 :
                // FortranLexer.g:1:200: T_GREATERTHAN
                {
                mT_GREATERTHAN(); 

                }
                break;
            case 19 :
                // FortranLexer.g:1:214: T_GREATERTHAN_EQ
                {
                mT_GREATERTHAN_EQ(); 

                }
                break;
            case 20 :
                // FortranLexer.g:1:231: T_LESSTHAN
                {
                mT_LESSTHAN(); 

                }
                break;
            case 21 :
                // FortranLexer.g:1:242: T_LESSTHAN_EQ
                {
                mT_LESSTHAN_EQ(); 

                }
                break;
            case 22 :
                // FortranLexer.g:1:256: T_LBRACKET
                {
                mT_LBRACKET(); 

                }
                break;
            case 23 :
                // FortranLexer.g:1:267: T_LPAREN
                {
                mT_LPAREN(); 

                }
                break;
            case 24 :
                // FortranLexer.g:1:276: T_MINUS
                {
                mT_MINUS(); 

                }
                break;
            case 25 :
                // FortranLexer.g:1:284: T_PERCENT
                {
                mT_PERCENT(); 

                }
                break;
            case 26 :
                // FortranLexer.g:1:294: T_PLUS
                {
                mT_PLUS(); 

                }
                break;
            case 27 :
                // FortranLexer.g:1:301: T_POWER
                {
                mT_POWER(); 

                }
                break;
            case 28 :
                // FortranLexer.g:1:309: T_SLASH
                {
                mT_SLASH(); 

                }
                break;
            case 29 :
                // FortranLexer.g:1:317: T_SLASH_EQ
                {
                mT_SLASH_EQ(); 

                }
                break;
            case 30 :
                // FortranLexer.g:1:328: T_SLASH_SLASH
                {
                mT_SLASH_SLASH(); 

                }
                break;
            case 31 :
                // FortranLexer.g:1:342: T_RBRACKET
                {
                mT_RBRACKET(); 

                }
                break;
            case 32 :
                // FortranLexer.g:1:353: T_RPAREN
                {
                mT_RPAREN(); 

                }
                break;
            case 33 :
                // FortranLexer.g:1:362: T_UNDERSCORE
                {
                mT_UNDERSCORE(); 

                }
                break;
            case 34 :
                // FortranLexer.g:1:375: T_EQ
                {
                mT_EQ(); 

                }
                break;
            case 35 :
                // FortranLexer.g:1:380: T_NE
                {
                mT_NE(); 

                }
                break;
            case 36 :
                // FortranLexer.g:1:385: T_LT
                {
                mT_LT(); 

                }
                break;
            case 37 :
                // FortranLexer.g:1:390: T_LE
                {
                mT_LE(); 

                }
                break;
            case 38 :
                // FortranLexer.g:1:395: T_GT
                {
                mT_GT(); 

                }
                break;
            case 39 :
                // FortranLexer.g:1:400: T_GE
                {
                mT_GE(); 

                }
                break;
            case 40 :
                // FortranLexer.g:1:405: T_TRUE
                {
                mT_TRUE(); 

                }
                break;
            case 41 :
                // FortranLexer.g:1:412: T_FALSE
                {
                mT_FALSE(); 

                }
                break;
            case 42 :
                // FortranLexer.g:1:420: T_NOT
                {
                mT_NOT(); 

                }
                break;
            case 43 :
                // FortranLexer.g:1:426: T_AND
                {
                mT_AND(); 

                }
                break;
            case 44 :
                // FortranLexer.g:1:432: T_OR
                {
                mT_OR(); 

                }
                break;
            case 45 :
                // FortranLexer.g:1:437: T_EQV
                {
                mT_EQV(); 

                }
                break;
            case 46 :
                // FortranLexer.g:1:443: T_NEQV
                {
                mT_NEQV(); 

                }
                break;
            case 47 :
                // FortranLexer.g:1:450: T_PERIOD_EXPONENT
                {
                mT_PERIOD_EXPONENT(); 

                }
                break;
            case 48 :
                // FortranLexer.g:1:468: T_PERIOD
                {
                mT_PERIOD(); 

                }
                break;
            case 49 :
                // FortranLexer.g:1:477: T_XYZ
                {
                mT_XYZ(); 

                }
                break;
            case 50 :
                // FortranLexer.g:1:483: T_INTEGER
                {
                mT_INTEGER(); 

                }
                break;
            case 51 :
                // FortranLexer.g:1:493: T_REAL
                {
                mT_REAL(); 

                }
                break;
            case 52 :
                // FortranLexer.g:1:500: T_COMPLEX
                {
                mT_COMPLEX(); 

                }
                break;
            case 53 :
                // FortranLexer.g:1:510: T_CHARACTER
                {
                mT_CHARACTER(); 

                }
                break;
            case 54 :
                // FortranLexer.g:1:522: T_LOGICAL
                {
                mT_LOGICAL(); 

                }
                break;
            case 55 :
                // FortranLexer.g:1:532: T_ABSTRACT
                {
                mT_ABSTRACT(); 

                }
                break;
            case 56 :
                // FortranLexer.g:1:543: T_ALLOCATABLE
                {
                mT_ALLOCATABLE(); 

                }
                break;
            case 57 :
                // FortranLexer.g:1:557: T_ALLOCATE
                {
                mT_ALLOCATE(); 

                }
                break;
            case 58 :
                // FortranLexer.g:1:568: T_ASSIGNMENT
                {
                mT_ASSIGNMENT(); 

                }
                break;
            case 59 :
                // FortranLexer.g:1:581: T_ASSIGN
                {
                mT_ASSIGN(); 

                }
                break;
            case 60 :
                // FortranLexer.g:1:590: T_ASSOCIATE
                {
                mT_ASSOCIATE(); 

                }
                break;
            case 61 :
                // FortranLexer.g:1:602: T_ASYNCHRONOUS
                {
                mT_ASYNCHRONOUS(); 

                }
                break;
            case 62 :
                // FortranLexer.g:1:617: T_BACKSPACE
                {
                mT_BACKSPACE(); 

                }
                break;
            case 63 :
                // FortranLexer.g:1:629: T_BLOCK
                {
                mT_BLOCK(); 

                }
                break;
            case 64 :
                // FortranLexer.g:1:637: T_BLOCKDATA
                {
                mT_BLOCKDATA(); 

                }
                break;
            case 65 :
                // FortranLexer.g:1:649: T_CALL
                {
                mT_CALL(); 

                }
                break;
            case 66 :
                // FortranLexer.g:1:656: T_CASE
                {
                mT_CASE(); 

                }
                break;
            case 67 :
                // FortranLexer.g:1:663: T_CLASS
                {
                mT_CLASS(); 

                }
                break;
            case 68 :
                // FortranLexer.g:1:671: T_CLOSE
                {
                mT_CLOSE(); 

                }
                break;
            case 69 :
                // FortranLexer.g:1:679: T_COMMON
                {
                mT_COMMON(); 

                }
                break;
            case 70 :
                // FortranLexer.g:1:688: T_CONTAINS
                {
                mT_CONTAINS(); 

                }
                break;
            case 71 :
                // FortranLexer.g:1:699: T_CONTINUE
                {
                mT_CONTINUE(); 

                }
                break;
            case 72 :
                // FortranLexer.g:1:710: T_CYCLE
                {
                mT_CYCLE(); 

                }
                break;
            case 73 :
                // FortranLexer.g:1:718: T_DATA
                {
                mT_DATA(); 

                }
                break;
            case 74 :
                // FortranLexer.g:1:725: T_DEFAULT
                {
                mT_DEFAULT(); 

                }
                break;
            case 75 :
                // FortranLexer.g:1:735: T_DEALLOCATE
                {
                mT_DEALLOCATE(); 

                }
                break;
            case 76 :
                // FortranLexer.g:1:748: T_DEFERRED
                {
                mT_DEFERRED(); 

                }
                break;
            case 77 :
                // FortranLexer.g:1:759: T_DO
                {
                mT_DO(); 

                }
                break;
            case 78 :
                // FortranLexer.g:1:764: T_DOUBLE
                {
                mT_DOUBLE(); 

                }
                break;
            case 79 :
                // FortranLexer.g:1:773: T_DOUBLEPRECISION
                {
                mT_DOUBLEPRECISION(); 

                }
                break;
            case 80 :
                // FortranLexer.g:1:791: T_DOUBLECOMPLEX
                {
                mT_DOUBLECOMPLEX(); 

                }
                break;
            case 81 :
                // FortranLexer.g:1:807: T_ELEMENTAL
                {
                mT_ELEMENTAL(); 

                }
                break;
            case 82 :
                // FortranLexer.g:1:819: T_ELSE
                {
                mT_ELSE(); 

                }
                break;
            case 83 :
                // FortranLexer.g:1:826: T_ELSEIF
                {
                mT_ELSEIF(); 

                }
                break;
            case 84 :
                // FortranLexer.g:1:835: T_ELSEWHERE
                {
                mT_ELSEWHERE(); 

                }
                break;
            case 85 :
                // FortranLexer.g:1:847: T_ENTRY
                {
                mT_ENTRY(); 

                }
                break;
            case 86 :
                // FortranLexer.g:1:855: T_ENUM
                {
                mT_ENUM(); 

                }
                break;
            case 87 :
                // FortranLexer.g:1:862: T_ENUMERATOR
                {
                mT_ENUMERATOR(); 

                }
                break;
            case 88 :
                // FortranLexer.g:1:875: T_EQUIVALENCE
                {
                mT_EQUIVALENCE(); 

                }
                break;
            case 89 :
                // FortranLexer.g:1:889: T_EXIT
                {
                mT_EXIT(); 

                }
                break;
            case 90 :
                // FortranLexer.g:1:896: T_EXTENDS
                {
                mT_EXTENDS(); 

                }
                break;
            case 91 :
                // FortranLexer.g:1:906: T_EXTERNAL
                {
                mT_EXTERNAL(); 

                }
                break;
            case 92 :
                // FortranLexer.g:1:917: T_FILE
                {
                mT_FILE(); 

                }
                break;
            case 93 :
                // FortranLexer.g:1:924: T_FINAL
                {
                mT_FINAL(); 

                }
                break;
            case 94 :
                // FortranLexer.g:1:932: T_FLUSH
                {
                mT_FLUSH(); 

                }
                break;
            case 95 :
                // FortranLexer.g:1:940: T_FORALL
                {
                mT_FORALL(); 

                }
                break;
            case 96 :
                // FortranLexer.g:1:949: T_FORMAT
                {
                mT_FORMAT(); 

                }
                break;
            case 97 :
                // FortranLexer.g:1:958: T_FORMATTED
                {
                mT_FORMATTED(); 

                }
                break;
            case 98 :
                // FortranLexer.g:1:970: T_FUNCTION
                {
                mT_FUNCTION(); 

                }
                break;
            case 99 :
                // FortranLexer.g:1:981: T_GENERIC
                {
                mT_GENERIC(); 

                }
                break;
            case 100 :
                // FortranLexer.g:1:991: T_GO
                {
                mT_GO(); 

                }
                break;
            case 101 :
                // FortranLexer.g:1:996: T_GOTO
                {
                mT_GOTO(); 

                }
                break;
            case 102 :
                // FortranLexer.g:1:1003: T_IF
                {
                mT_IF(); 

                }
                break;
            case 103 :
                // FortranLexer.g:1:1008: T_IMPLICIT
                {
                mT_IMPLICIT(); 

                }
                break;
            case 104 :
                // FortranLexer.g:1:1019: T_IMPORT
                {
                mT_IMPORT(); 

                }
                break;
            case 105 :
                // FortranLexer.g:1:1028: T_IN
                {
                mT_IN(); 

                }
                break;
            case 106 :
                // FortranLexer.g:1:1033: T_INOUT
                {
                mT_INOUT(); 

                }
                break;
            case 107 :
                // FortranLexer.g:1:1041: T_INTENT
                {
                mT_INTENT(); 

                }
                break;
            case 108 :
                // FortranLexer.g:1:1050: T_INTERFACE
                {
                mT_INTERFACE(); 

                }
                break;
            case 109 :
                // FortranLexer.g:1:1062: T_INTRINSIC
                {
                mT_INTRINSIC(); 

                }
                break;
            case 110 :
                // FortranLexer.g:1:1074: T_INQUIRE
                {
                mT_INQUIRE(); 

                }
                break;
            case 111 :
                // FortranLexer.g:1:1084: T_MODULE
                {
                mT_MODULE(); 

                }
                break;
            case 112 :
                // FortranLexer.g:1:1093: T_NAMELIST
                {
                mT_NAMELIST(); 

                }
                break;
            case 113 :
                // FortranLexer.g:1:1104: T_NONE
                {
                mT_NONE(); 

                }
                break;
            case 114 :
                // FortranLexer.g:1:1111: T_NON_INTRINSIC
                {
                mT_NON_INTRINSIC(); 

                }
                break;
            case 115 :
                // FortranLexer.g:1:1127: T_NON_OVERRIDABLE
                {
                mT_NON_OVERRIDABLE(); 

                }
                break;
            case 116 :
                // FortranLexer.g:1:1145: T_NOPASS
                {
                mT_NOPASS(); 

                }
                break;
            case 117 :
                // FortranLexer.g:1:1154: T_NULLIFY
                {
                mT_NULLIFY(); 

                }
                break;
            case 118 :
                // FortranLexer.g:1:1164: T_ONLY
                {
                mT_ONLY(); 

                }
                break;
            case 119 :
                // FortranLexer.g:1:1171: T_OPEN
                {
                mT_OPEN(); 

                }
                break;
            case 120 :
                // FortranLexer.g:1:1178: T_OPERATOR
                {
                mT_OPERATOR(); 

                }
                break;
            case 121 :
                // FortranLexer.g:1:1189: T_OPTIONAL
                {
                mT_OPTIONAL(); 

                }
                break;
            case 122 :
                // FortranLexer.g:1:1200: T_OUT
                {
                mT_OUT(); 

                }
                break;
            case 123 :
                // FortranLexer.g:1:1206: T_PARAMETER
                {
                mT_PARAMETER(); 

                }
                break;
            case 124 :
                // FortranLexer.g:1:1218: T_PASS
                {
                mT_PASS(); 

                }
                break;
            case 125 :
                // FortranLexer.g:1:1225: T_PAUSE
                {
                mT_PAUSE(); 

                }
                break;
            case 126 :
                // FortranLexer.g:1:1233: T_POINTER
                {
                mT_POINTER(); 

                }
                break;
            case 127 :
                // FortranLexer.g:1:1243: T_PRINT
                {
                mT_PRINT(); 

                }
                break;
            case 128 :
                // FortranLexer.g:1:1251: T_PRECISION
                {
                mT_PRECISION(); 

                }
                break;
            case 129 :
                // FortranLexer.g:1:1263: T_PRIVATE
                {
                mT_PRIVATE(); 

                }
                break;
            case 130 :
                // FortranLexer.g:1:1273: T_PROCEDURE
                {
                mT_PROCEDURE(); 

                }
                break;
            case 131 :
                // FortranLexer.g:1:1285: T_PROGRAM
                {
                mT_PROGRAM(); 

                }
                break;
            case 132 :
                // FortranLexer.g:1:1295: T_PROTECTED
                {
                mT_PROTECTED(); 

                }
                break;
            case 133 :
                // FortranLexer.g:1:1307: T_PUBLIC
                {
                mT_PUBLIC(); 

                }
                break;
            case 134 :
                // FortranLexer.g:1:1316: T_PURE
                {
                mT_PURE(); 

                }
                break;
            case 135 :
                // FortranLexer.g:1:1323: T_READ
                {
                mT_READ(); 

                }
                break;
            case 136 :
                // FortranLexer.g:1:1330: T_RECURSIVE
                {
                mT_RECURSIVE(); 

                }
                break;
            case 137 :
                // FortranLexer.g:1:1342: T_RESULT
                {
                mT_RESULT(); 

                }
                break;
            case 138 :
                // FortranLexer.g:1:1351: T_RETURN
                {
                mT_RETURN(); 

                }
                break;
            case 139 :
                // FortranLexer.g:1:1360: T_REWIND
                {
                mT_REWIND(); 

                }
                break;
            case 140 :
                // FortranLexer.g:1:1369: T_SAVE
                {
                mT_SAVE(); 

                }
                break;
            case 141 :
                // FortranLexer.g:1:1376: T_SELECT
                {
                mT_SELECT(); 

                }
                break;
            case 142 :
                // FortranLexer.g:1:1385: T_SELECTCASE
                {
                mT_SELECTCASE(); 

                }
                break;
            case 143 :
                // FortranLexer.g:1:1398: T_SELECTTYPE
                {
                mT_SELECTTYPE(); 

                }
                break;
            case 144 :
                // FortranLexer.g:1:1411: T_SEQUENCE
                {
                mT_SEQUENCE(); 

                }
                break;
            case 145 :
                // FortranLexer.g:1:1422: T_STOP
                {
                mT_STOP(); 

                }
                break;
            case 146 :
                // FortranLexer.g:1:1429: T_SUBROUTINE
                {
                mT_SUBROUTINE(); 

                }
                break;
            case 147 :
                // FortranLexer.g:1:1442: T_TARGET
                {
                mT_TARGET(); 

                }
                break;
            case 148 :
                // FortranLexer.g:1:1451: T_THEN
                {
                mT_THEN(); 

                }
                break;
            case 149 :
                // FortranLexer.g:1:1458: T_TO
                {
                mT_TO(); 

                }
                break;
            case 150 :
                // FortranLexer.g:1:1463: T_TYPE
                {
                mT_TYPE(); 

                }
                break;
            case 151 :
                // FortranLexer.g:1:1470: T_UNFORMATTED
                {
                mT_UNFORMATTED(); 

                }
                break;
            case 152 :
                // FortranLexer.g:1:1484: T_USE
                {
                mT_USE(); 

                }
                break;
            case 153 :
                // FortranLexer.g:1:1490: T_VALUE
                {
                mT_VALUE(); 

                }
                break;
            case 154 :
                // FortranLexer.g:1:1498: T_VOLATILE
                {
                mT_VOLATILE(); 

                }
                break;
            case 155 :
                // FortranLexer.g:1:1509: T_WAIT
                {
                mT_WAIT(); 

                }
                break;
            case 156 :
                // FortranLexer.g:1:1516: T_WHERE
                {
                mT_WHERE(); 

                }
                break;
            case 157 :
                // FortranLexer.g:1:1524: T_WHILE
                {
                mT_WHILE(); 

                }
                break;
            case 158 :
                // FortranLexer.g:1:1532: T_WRITE
                {
                mT_WRITE(); 

                }
                break;
            case 159 :
                // FortranLexer.g:1:1540: T_ENDASSOCIATE
                {
                mT_ENDASSOCIATE(); 

                }
                break;
            case 160 :
                // FortranLexer.g:1:1555: T_ENDBLOCK
                {
                mT_ENDBLOCK(); 

                }
                break;
            case 161 :
                // FortranLexer.g:1:1566: T_ENDBLOCKDATA
                {
                mT_ENDBLOCKDATA(); 

                }
                break;
            case 162 :
                // FortranLexer.g:1:1581: T_ENDDO
                {
                mT_ENDDO(); 

                }
                break;
            case 163 :
                // FortranLexer.g:1:1589: T_ENDENUM
                {
                mT_ENDENUM(); 

                }
                break;
            case 164 :
                // FortranLexer.g:1:1599: T_ENDFORALL
                {
                mT_ENDFORALL(); 

                }
                break;
            case 165 :
                // FortranLexer.g:1:1611: T_ENDFILE
                {
                mT_ENDFILE(); 

                }
                break;
            case 166 :
                // FortranLexer.g:1:1621: T_ENDFUNCTION
                {
                mT_ENDFUNCTION(); 

                }
                break;
            case 167 :
                // FortranLexer.g:1:1635: T_ENDIF
                {
                mT_ENDIF(); 

                }
                break;
            case 168 :
                // FortranLexer.g:1:1643: T_ENDINTERFACE
                {
                mT_ENDINTERFACE(); 

                }
                break;
            case 169 :
                // FortranLexer.g:1:1658: T_ENDMODULE
                {
                mT_ENDMODULE(); 

                }
                break;
            case 170 :
                // FortranLexer.g:1:1670: T_ENDPROGRAM
                {
                mT_ENDPROGRAM(); 

                }
                break;
            case 171 :
                // FortranLexer.g:1:1683: T_ENDSELECT
                {
                mT_ENDSELECT(); 

                }
                break;
            case 172 :
                // FortranLexer.g:1:1695: T_ENDSUBROUTINE
                {
                mT_ENDSUBROUTINE(); 

                }
                break;
            case 173 :
                // FortranLexer.g:1:1711: T_ENDTYPE
                {
                mT_ENDTYPE(); 

                }
                break;
            case 174 :
                // FortranLexer.g:1:1721: T_ENDWHERE
                {
                mT_ENDWHERE(); 

                }
                break;
            case 175 :
                // FortranLexer.g:1:1732: T_END
                {
                mT_END(); 

                }
                break;
            case 176 :
                // FortranLexer.g:1:1738: T_DIMENSION
                {
                mT_DIMENSION(); 

                }
                break;
            case 177 :
                // FortranLexer.g:1:1750: T_KIND
                {
                mT_KIND(); 

                }
                break;
            case 178 :
                // FortranLexer.g:1:1757: T_LEN
                {
                mT_LEN(); 

                }
                break;
            case 179 :
                // FortranLexer.g:1:1763: T_BIND
                {
                mT_BIND(); 

                }
                break;
            case 180 :
                // FortranLexer.g:1:1770: T_HOLLERITH
                {
                mT_HOLLERITH(); 

                }
                break;
            case 181 :
                // FortranLexer.g:1:1782: T_DEFINED_OP
                {
                mT_DEFINED_OP(); 

                }
                break;
            case 182 :
                // FortranLexer.g:1:1795: T_LABEL_DO_TERMINAL
                {
                mT_LABEL_DO_TERMINAL(); 

                }
                break;
            case 183 :
                // FortranLexer.g:1:1815: T_DATA_EDIT_DESC
                {
                mT_DATA_EDIT_DESC(); 

                }
                break;
            case 184 :
                // FortranLexer.g:1:1832: T_CONTROL_EDIT_DESC
                {
                mT_CONTROL_EDIT_DESC(); 

                }
                break;
            case 185 :
                // FortranLexer.g:1:1852: T_CHAR_STRING_EDIT_DESC
                {
                mT_CHAR_STRING_EDIT_DESC(); 

                }
                break;
            case 186 :
                // FortranLexer.g:1:1876: T_STMT_FUNCTION
                {
                mT_STMT_FUNCTION(); 

                }
                break;
            case 187 :
                // FortranLexer.g:1:1892: T_ASSIGNMENT_STMT
                {
                mT_ASSIGNMENT_STMT(); 

                }
                break;
            case 188 :
                // FortranLexer.g:1:1910: T_PTR_ASSIGNMENT_STMT
                {
                mT_PTR_ASSIGNMENT_STMT(); 

                }
                break;
            case 189 :
                // FortranLexer.g:1:1932: T_ARITHMETIC_IF_STMT
                {
                mT_ARITHMETIC_IF_STMT(); 

                }
                break;
            case 190 :
                // FortranLexer.g:1:1953: T_ALLOCATE_STMT_1
                {
                mT_ALLOCATE_STMT_1(); 

                }
                break;
            case 191 :
                // FortranLexer.g:1:1971: T_WHERE_STMT
                {
                mT_WHERE_STMT(); 

                }
                break;
            case 192 :
                // FortranLexer.g:1:1984: T_IF_STMT
                {
                mT_IF_STMT(); 

                }
                break;
            case 193 :
                // FortranLexer.g:1:1994: T_FORALL_STMT
                {
                mT_FORALL_STMT(); 

                }
                break;
            case 194 :
                // FortranLexer.g:1:2008: T_WHERE_CONSTRUCT_STMT
                {
                mT_WHERE_CONSTRUCT_STMT(); 

                }
                break;
            case 195 :
                // FortranLexer.g:1:2031: T_FORALL_CONSTRUCT_STMT
                {
                mT_FORALL_CONSTRUCT_STMT(); 

                }
                break;
            case 196 :
                // FortranLexer.g:1:2055: T_INQUIRE_STMT_2
                {
                mT_INQUIRE_STMT_2(); 

                }
                break;
            case 197 :
                // FortranLexer.g:1:2072: T_REAL_CONSTANT
                {
                mT_REAL_CONSTANT(); 

                }
                break;
            case 198 :
                // FortranLexer.g:1:2088: T_EOF
                {
                mT_EOF(); 

                }
                break;
            case 199 :
                // FortranLexer.g:1:2094: T_IDENT
                {
                mT_IDENT(); 

                }
                break;
            case 200 :
                // FortranLexer.g:1:2102: LINE_COMMENT
                {
                mLINE_COMMENT(); 

                }
                break;
            case 201 :
                // FortranLexer.g:1:2115: MISC_CHAR
                {
                mMISC_CHAR(); 

                }
                break;

        }

    }


    protected DFA28 dfa28 = new DFA28(this);
    protected DFA32 dfa32 = new DFA32(this);
    static final String DFA28_eotS =
        "\4\uffff\1\5\2\uffff";
    static final String DFA28_eofS =
        "\7\uffff";
    static final String DFA28_minS =
        "\1\56\1\60\2\uffff\1\60\2\uffff";
    static final String DFA28_maxS =
        "\1\71\1\145\2\uffff\1\145\2\uffff";
    static final String DFA28_acceptS =
        "\2\uffff\1\4\1\2\1\uffff\1\3\1\1";
    static final String DFA28_specialS =
        "\7\uffff}>";
    static final String[] DFA28_transitionS = {
            "\1\1\1\uffff\12\2",
            "\12\4\12\uffff\2\3\36\uffff\2\3",
            "",
            "",
            "\12\4\12\uffff\2\6\36\uffff\2\6",
            "",
            ""
    };

    static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
    static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
    static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
    static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
    static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
    static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
    static final short[][] DFA28_transition;

    static {
        int numStates = DFA28_transitionS.length;
        DFA28_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
        }
    }

    class DFA28 extends DFA {

        public DFA28(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 28;
            this.eot = DFA28_eot;
            this.eof = DFA28_eof;
            this.min = DFA28_min;
            this.max = DFA28_max;
            this.accept = DFA28_accept;
            this.special = DFA28_special;
            this.transition = DFA28_transition;
        }
        public String getDescription() {
            return "461:1: T_PERIOD_EXPONENT : ( '.' ( '0' .. '9' )+ ( 'E' | 'e' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+ | '.' ( 'E' | 'e' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+ | '.' ( '0' .. '9' )+ | ( '0' .. '9' )+ ( 'e' | 'E' | 'd' | 'D' ) ( '+' | '-' )? ( '0' .. '9' )+ );";
        }
    }
    static final String DFA32_eotS =
        "\2\uffff\1\64\2\uffff\2\63\1\67\3\77\2\uffff\1\77\1\112\1\114\1"+
        "\uffff\1\120\1\122\1\124\5\uffff\1\134\2\uffff\1\140\1\153\23\77"+
        "\7\uffff\1\67\3\uffff\3\77\2\uffff\3\77\2\uffff\1\u00a9\1\u00aa"+
        "\1\77\43\uffff\15\77\1\u00d1\12\77\1\u00e2\16\77\1\u00f9\11\77\1"+
        "\uffff\6\77\1\u010b\4\77\2\uffff\1\77\16\uffff\16\77\1\u0131\10"+
        "\77\1\uffff\5\77\1\u014c\12\77\1\uffff\26\77\1\uffff\2\77\1\u0174"+
        "\11\77\1\u017e\1\u017f\1\u0180\2\77\1\uffff\7\77\16\uffff\1\u01a2"+
        "\1\u01a3\10\77\1\u01ad\1\u01ae\4\77\1\uffff\5\77\1\u01b8\6\77\1"+
        "\u01c1\1\77\1\u01c4\13\77\1\uffff\1\77\1\u01d5\1\77\1\u01d8\6\77"+
        "\1\u01df\2\77\1\u01e2\4\77\1\u01e8\11\77\1\u01f2\1\u01f3\2\77\1"+
        "\u01f6\3\77\1\u01fa\1\u01fb\1\77\1\uffff\2\77\1\u01ff\3\77\1\u0203"+
        "\1\77\1\u0206\3\uffff\7\77\1\u020e\3\77\30\uffff\11\77\2\uffff\1"+
        "\u022a\1\u022b\1\u022c\6\77\1\uffff\10\77\1\uffff\1\u023b\1\77\1"+
        "\uffff\2\77\1\u023f\4\77\1\u0244\10\77\1\uffff\2\77\1\uffff\1\u024f"+
        "\1\u0250\4\77\1\uffff\2\77\1\uffff\5\77\1\uffff\1\u025c\1\77\1\u025e"+
        "\6\77\2\uffff\2\77\1\uffff\3\77\2\uffff\1\77\1\u026b\1\77\1\uffff"+
        "\1\u026d\1\u026e\1\u026f\1\uffff\2\77\1\uffff\4\77\1\u0276\2\77"+
        "\1\uffff\2\77\1\u027b\17\uffff\1\77\1\u0282\1\u0283\1\u0284\1\77"+
        "\1\u0286\3\77\3\uffff\3\77\1\u028e\5\77\1\u0296\2\77\1\u0299\1\77"+
        "\1\uffff\3\77\1\uffff\4\77\1\uffff\12\77\2\uffff\1\u02ac\1\u02ae"+
        "\2\77\1\u02b1\3\77\1\u02b5\2\77\1\uffff\1\77\1\uffff\5\77\1\u02be"+
        "\1\u02c1\3\77\1\u02c5\1\77\1\uffff\1\77\3\uffff\4\77\1\u02cc\1\u02cd"+
        "\1\uffff\2\77\1\u02d0\1\77\6\uffff\1\77\3\uffff\1\u02d6\1\uffff"+
        "\3\77\1\u02da\3\77\1\uffff\2\77\1\u02e1\4\77\1\uffff\2\77\1\uffff"+
        "\4\77\1\u02ec\1\77\1\u02ee\6\77\1\u02f5\2\77\1\u02f8\1\77\1\uffff"+
        "\1\77\1\uffff\1\77\1\u02fc\1\uffff\3\77\1\uffff\1\u0300\1\77\1\u0302"+
        "\1\u0303\2\77\1\u0306\1\77\1\uffff\2\77\1\uffff\3\77\1\uffff\4\77"+
        "\1\u0311\1\u0312\2\uffff\2\77\1\uffff\1\u0315\3\uffff\1\77\1\uffff"+
        "\1\u0319\1\u031a\1\77\1\uffff\1\u031c\1\77\1\u031e\3\77\1\uffff"+
        "\1\u0322\10\77\1\u032c\1\uffff\1\77\1\uffff\6\77\1\uffff\1\u0334"+
        "\1\77\1\uffff\1\u0336\1\77\1\u0338\1\uffff\1\u0339\2\77\1\uffff"+
        "\1\77\2\uffff\2\77\1\uffff\3\77\1\u0342\3\77\1\u0346\1\u0347\1\u0348"+
        "\2\uffff\1\u0349\1\u034a\3\uffff\1\u034d\2\uffff\1\u034e\1\uffff"+
        "\1\77\1\uffff\1\77\1\u0351\1\77\1\uffff\3\77\1\u0356\1\u0357\1\u0358"+
        "\3\77\1\uffff\1\u035c\2\77\1\u035f\1\77\1\u0361\1\77\1\uffff\1\77"+
        "\1\uffff\1\u0364\2\uffff\2\77\1\u0367\1\u0368\1\u0369\1\u036a\2"+
        "\77\1\uffff\3\77\11\uffff\1\77\1\u0374\1\uffff\1\77\1\u0376\2\77"+
        "\3\uffff\1\u0379\2\77\1\uffff\2\77\1\uffff\1\u037e\1\uffff\2\77"+
        "\1\uffff\2\77\4\uffff\1\u0383\1\u0384\1\77\1\u0386\1\77\3\uffff"+
        "\1\u038a\1\uffff\1\77\1\uffff\2\77\1\uffff\2\77\1\u0390\1\77\1\uffff"+
        "\1\77\1\u0393\2\77\2\uffff\1\77\1\uffff\1\u0397\3\uffff\1\u0398"+
        "\2\77\1\u039b\1\u039c\1\uffff\1\u039d\1\77\1\uffff\3\77\2\uffff"+
        "\1\77\1\u03a3\3\uffff\1\u03a4\1\u03a5\1\77\1\u03a7\1\77\3\uffff"+
        "\1\77\1\uffff\1\u03aa\1\u03ab\2\uffff";
    static final String DFA32_eofS =
        "\u03ac\uffff";
    static final String DFA32_minS =
        "\1\0\1\uffff\1\12\2\uffff\2\0\1\60\3\42\2\uffff\1\106\1\52\1\72"+
        "\1\uffff\3\75\5\uffff\1\57\2\uffff\1\137\1\60\1\105\1\101\1\105"+
        "\1\102\1\42\1\101\1\114\1\111\1\105\1\117\1\101\1\42\3\101\1\116"+
        "\2\101\1\111\7\uffff\1\60\3\uffff\1\103\1\117\1\116\2\uffff\1\114"+
        "\1\105\1\124\2\uffff\2\60\1\120\26\uffff\1\114\1\uffff\1\53\7\56"+
        "\1\53\2\uffff\1\101\1\115\1\101\1\114\1\101\1\103\1\107\1\116\1"+
        "\123\1\114\1\123\1\124\1\101\1\60\1\115\1\105\1\104\1\125\1\111"+
        "\1\114\1\125\1\122\2\116\1\60\1\104\1\115\1\116\1\114\1\122\1\111"+
        "\1\105\1\102\1\126\1\114\1\115\1\102\1\122\1\105\1\60\1\120\1\106"+
        "\1\105\2\114\1\111\1\105\1\111\1\116\1\uffff\1\113\1\103\1\104\1"+
        "\131\1\116\1\111\1\60\1\114\1\105\2\125\2\uffff\1\114\2\uffff\1"+
        "\137\13\56\1\104\3\125\1\111\1\115\1\124\1\122\1\114\1\105\2\123"+
        "\1\114\1\111\1\60\1\124\1\117\1\111\1\116\2\101\1\114\1\102\1\uffff"+
        "\1\105\1\115\1\105\1\122\1\115\1\60\1\111\1\124\2\105\1\101\1\123"+
        "\1\101\1\103\1\105\1\117\1\uffff\1\125\2\105\1\101\1\114\1\101\2"+
        "\123\2\116\2\103\1\114\3\105\1\125\1\120\1\124\1\122\1\107\1\116"+
        "\1\uffff\1\105\1\117\1\60\1\125\1\101\1\124\1\122\1\114\1\124\1"+
        "\104\1\123\1\113\3\60\1\101\1\117\1\uffff\1\125\1\107\1\111\1\124"+
        "\2\111\1\122\1\101\1\uffff\1\56\1\uffff\2\56\4\uffff\3\56\1\uffff"+
        "\2\60\1\122\1\114\1\122\1\116\1\114\1\117\2\101\2\60\1\123\2\105"+
        "\1\103\1\uffff\1\122\1\103\1\107\2\103\1\60\1\125\1\122\2\114\1"+
        "\116\1\105\1\60\1\131\1\60\1\123\1\114\1\117\1\116\1\111\1\106\1"+
        "\117\1\122\1\105\1\131\1\110\1\uffff\1\126\1\60\1\116\1\60\1\114"+
        "\1\110\1\114\1\101\1\124\1\122\1\60\2\114\1\60\1\111\1\123\1\111"+
        "\1\115\1\60\1\105\2\124\1\101\1\111\1\105\1\122\1\105\1\111\2\60"+
        "\1\103\1\105\1\60\1\137\1\117\1\105\2\60\1\122\1\uffff\1\105\1\124"+
        "\1\60\3\105\1\60\1\120\1\60\3\uffff\1\124\1\116\1\104\1\105\1\124"+
        "\1\106\1\116\1\60\1\122\1\103\1\124\1\uffff\1\110\1\114\1\uffff"+
        "\1\110\1\106\1\117\5\uffff\1\56\5\uffff\2\56\4\uffff\1\123\1\124"+
        "\1\116\1\104\1\105\1\116\1\111\1\116\1\103\2\uffff\3\60\3\101\1"+
        "\116\1\111\1\110\1\uffff\1\114\1\122\1\117\1\105\1\123\1\116\1\106"+
        "\1\110\1\uffff\1\60\1\122\1\uffff\1\123\1\117\1\60\1\125\1\122\1"+
        "\114\1\116\1\60\1\124\1\104\1\117\1\114\1\102\1\120\1\105\1\101"+
        "\1\uffff\1\104\1\116\1\uffff\2\60\1\114\1\124\2\111\1\uffff\1\105"+
        "\1\111\1\uffff\1\116\1\126\1\123\1\106\1\105\1\uffff\1\60\1\105"+
        "\1\60\1\124\1\123\1\104\1\101\2\103\2\uffff\1\124\1\116\1\uffff"+
        "\1\106\1\125\1\124\2\uffff\1\115\1\60\1\111\1\uffff\3\60\1\uffff"+
        "\2\101\1\uffff\1\117\1\101\1\105\1\122\1\60\1\101\1\123\1\uffff"+
        "\1\105\1\111\1\60\5\uffff\1\105\2\uffff\1\122\4\uffff\1\56\1\uffff"+
        "\1\111\3\60\1\130\1\60\1\116\1\125\1\124\3\uffff\1\114\1\103\1\124"+
        "\1\60\1\101\1\122\1\124\1\105\1\103\1\60\1\111\1\124\1\60\1\105"+
        "\1\uffff\1\101\1\117\1\103\1\uffff\1\115\1\101\1\105\1\103\1\uffff"+
        "\1\105\1\125\1\107\1\105\1\122\1\105\1\122\1\114\1\123\1\101\2\uffff"+
        "\2\60\1\117\1\103\1\60\1\123\1\124\1\105\1\60\1\131\1\124\1\uffff"+
        "\1\122\1\uffff\1\105\1\111\1\125\1\115\1\124\2\60\1\103\1\125\1"+
        "\124\1\60\1\101\1\uffff\1\114\3\uffff\1\103\1\124\1\122\1\114\2"+
        "\60\1\uffff\1\103\1\111\1\60\1\124\1\uffff\1\122\1\101\3\uffff\1"+
        "\126\3\uffff\1\60\1\uffff\1\123\2\105\1\60\1\124\1\101\1\105\1\uffff"+
        "\1\124\1\117\1\60\1\104\1\101\1\122\1\117\1\uffff\1\117\1\101\1"+
        "\uffff\1\122\1\124\1\103\1\113\1\60\1\114\1\60\1\124\1\122\1\114"+
        "\1\122\1\103\1\117\1\60\2\105\1\60\1\114\1\uffff\1\105\1\uffff\1"+
        "\116\1\60\1\uffff\1\124\2\122\1\uffff\1\60\1\105\2\60\1\117\1\122"+
        "\1\60\1\105\1\uffff\1\101\1\131\1\uffff\1\105\1\116\1\111\1\uffff"+
        "\1\124\2\105\1\101\2\60\2\uffff\1\105\1\103\1\uffff\1\60\1\105\1"+
        "\114\1\uffff\1\105\1\uffff\2\60\1\122\1\uffff\1\60\1\102\1\60\1"+
        "\116\1\105\1\116\1\uffff\1\60\1\124\1\105\1\115\1\116\1\114\1\105"+
        "\1\117\1\111\1\60\1\uffff\1\114\1\uffff\1\111\1\106\1\105\1\101"+
        "\1\124\1\125\1\uffff\1\60\1\116\1\uffff\1\60\1\104\1\60\1\uffff"+
        "\1\60\1\111\1\122\1\uffff\1\122\2\uffff\1\116\1\105\1\uffff\1\104"+
        "\1\123\1\120\1\60\1\103\1\116\1\124\3\60\2\uffff\2\60\1\uffff\1"+
        "\137\1\114\1\60\2\uffff\1\60\1\uffff\1\114\1\uffff\1\124\1\60\1"+
        "\117\1\uffff\1\105\1\103\1\120\3\60\1\122\2\101\1\uffff\1\60\1\117"+
        "\1\101\1\60\1\115\1\60\1\124\1\uffff\1\103\1\uffff\1\60\2\uffff"+
        "\1\116\1\111\4\60\2\105\1\uffff\1\124\2\105\5\uffff\1\103\1\137"+
        "\2\uffff\1\105\1\60\1\uffff\1\125\1\60\1\111\1\114\3\uffff\1\60"+
        "\2\124\1\uffff\1\116\1\103\1\uffff\1\60\1\uffff\1\111\1\105\1\uffff"+
        "\1\123\1\104\4\uffff\2\60\1\111\1\60\1\104\2\uffff\1\103\1\60\1"+
        "\uffff\1\123\1\uffff\1\123\1\105\1\uffff\1\105\1\101\1\60\1\105"+
        "\1\uffff\1\116\1\60\1\111\1\101\2\uffff\1\117\1\uffff\1\60\3\uffff"+
        "\1\60\1\111\1\130\2\60\1\uffff\1\60\1\105\1\uffff\1\103\1\102\1"+
        "\116\2\uffff\1\117\1\60\3\uffff\2\60\1\114\1\60\1\116\3\uffff\1"+
        "\105\1\uffff\2\60\2\uffff";
    static final String DFA32_maxS =
        "\1\ufffe\1\uffff\1\12\2\uffff\2\ufffe\1\145\1\114\1\125\1\47\2\uffff"+
        "\1\116\1\52\1\72\1\uffff\1\76\2\75\5\uffff\1\75\2\uffff\1\137\1"+
        "\172\1\105\1\131\1\117\1\123\1\47\1\117\1\130\1\125\2\117\1\125"+
        "\1\47\2\125\1\131\1\123\1\117\1\122\1\111\7\uffff\1\145\3\uffff"+
        "\1\103\1\117\1\116\2\uffff\1\114\2\124\2\uffff\2\172\1\120\26\uffff"+
        "\1\130\1\uffff\11\172\2\uffff\1\127\1\116\1\101\1\123\1\117\1\103"+
        "\1\107\1\116\1\123\1\114\1\131\1\124\1\106\1\172\1\115\1\123\2\125"+
        "\1\124\1\116\1\125\1\122\2\116\1\172\1\104\1\115\1\120\1\114\1\125"+
        "\1\111\1\117\1\122\1\126\1\121\1\117\1\102\1\122\1\105\1\172\1\120"+
        "\1\106\1\105\2\114\3\111\1\116\1\uffff\1\113\1\103\1\104\1\131\1"+
        "\122\1\111\1\172\1\114\1\122\2\125\2\uffff\1\117\2\uffff\1\137\13"+
        "\172\1\114\3\125\1\111\1\120\1\124\1\122\1\114\1\105\2\123\1\114"+
        "\1\111\1\172\1\124\2\117\1\116\1\101\1\105\1\114\1\102\1\uffff\1"+
        "\105\1\115\1\105\1\122\1\115\1\172\1\111\1\124\2\105\1\101\1\123"+
        "\1\115\1\103\1\105\1\117\1\uffff\1\125\1\105\1\137\1\101\1\114\1"+
        "\101\2\123\1\116\1\126\1\103\1\124\1\114\3\105\1\125\1\120\1\124"+
        "\1\122\1\107\1\116\1\uffff\1\105\1\117\1\172\1\125\1\101\1\124\1"+
        "\122\1\114\1\124\1\104\1\123\1\113\3\172\1\101\1\117\1\uffff\1\125"+
        "\1\122\1\111\1\124\2\111\1\122\1\127\1\uffff\1\172\1\uffff\2\172"+
        "\4\uffff\3\172\1\uffff\2\172\1\122\1\114\1\122\1\116\1\114\1\117"+
        "\1\111\1\101\2\172\1\123\2\105\1\103\1\uffff\1\122\1\103\1\107\2"+
        "\103\1\172\1\125\1\122\2\114\1\116\1\105\1\172\1\131\1\172\1\123"+
        "\1\114\1\117\1\116\1\125\1\116\1\117\1\122\1\125\1\131\1\110\1\uffff"+
        "\1\126\1\172\1\122\1\172\1\114\1\110\1\114\1\101\1\124\1\122\1\172"+
        "\2\114\1\172\1\117\1\123\1\111\1\115\1\172\1\105\2\124\1\101\1\111"+
        "\1\105\1\122\1\105\1\111\2\172\1\103\1\105\1\172\1\137\1\117\1\105"+
        "\2\172\1\122\1\uffff\1\105\1\124\1\172\3\105\1\172\1\120\1\172\3"+
        "\uffff\1\124\1\116\1\104\1\105\1\124\1\106\1\116\1\172\1\122\1\103"+
        "\1\124\1\uffff\1\117\1\123\1\uffff\1\110\1\116\1\117\5\uffff\1\172"+
        "\5\uffff\2\172\4\uffff\1\123\1\124\1\116\1\104\1\105\1\116\1\111"+
        "\1\116\1\103\2\uffff\3\172\3\101\1\116\1\111\1\110\1\uffff\1\114"+
        "\1\122\1\117\1\105\1\123\1\116\1\106\1\110\1\uffff\1\172\1\122\1"+
        "\uffff\1\123\1\117\1\172\1\125\1\122\1\114\1\116\1\172\1\124\1\104"+
        "\1\117\1\114\1\102\1\120\1\105\1\101\1\uffff\1\104\1\116\1\uffff"+
        "\2\172\1\114\1\124\2\111\1\uffff\1\105\1\111\1\uffff\1\116\1\126"+
        "\1\123\1\106\1\105\1\uffff\1\172\1\105\1\172\1\124\1\123\1\104\1"+
        "\101\2\103\2\uffff\1\124\1\116\1\uffff\1\106\1\125\1\124\2\uffff"+
        "\1\115\1\172\1\111\1\uffff\3\172\1\uffff\2\101\1\uffff\1\117\1\101"+
        "\1\105\1\122\1\172\1\101\1\123\1\uffff\1\105\1\111\1\172\5\uffff"+
        "\1\105\2\uffff\1\122\4\uffff\1\172\1\uffff\1\111\3\172\1\130\1\172"+
        "\1\116\1\125\1\124\3\uffff\1\114\1\103\1\124\1\172\1\101\1\122\1"+
        "\124\1\105\1\103\1\172\1\111\1\124\1\172\1\105\1\uffff\1\101\1\117"+
        "\1\103\1\uffff\1\115\1\101\1\105\1\103\1\uffff\1\105\1\125\1\107"+
        "\1\105\1\122\1\105\1\122\1\114\1\123\1\101\2\uffff\2\172\1\117\1"+
        "\103\1\172\1\123\1\124\1\105\1\172\1\131\1\124\1\uffff\1\122\1\uffff"+
        "\1\105\1\111\1\125\1\115\1\124\2\172\1\103\1\125\1\124\1\172\1\101"+
        "\1\uffff\1\114\3\uffff\1\103\1\124\1\122\1\114\2\172\1\uffff\1\103"+
        "\1\111\1\172\1\124\1\uffff\1\122\1\101\3\uffff\1\126\3\uffff\1\172"+
        "\1\uffff\1\123\2\105\1\172\1\124\2\105\1\uffff\1\124\1\117\1\172"+
        "\1\104\1\101\1\122\1\117\1\uffff\1\117\1\101\1\uffff\1\122\1\124"+
        "\1\103\1\113\1\172\1\114\1\172\1\124\1\122\1\114\1\122\1\103\1\117"+
        "\1\172\2\105\1\172\1\114\1\uffff\1\105\1\uffff\1\116\1\172\1\uffff"+
        "\1\124\2\122\1\uffff\1\172\1\105\2\172\1\117\1\122\1\172\1\105\1"+
        "\uffff\1\101\1\131\1\uffff\1\105\1\116\1\111\1\uffff\1\124\2\105"+
        "\1\101\2\172\2\uffff\1\105\1\103\1\uffff\1\172\1\105\1\114\1\uffff"+
        "\1\105\1\uffff\2\172\1\122\1\uffff\1\172\1\102\1\172\1\116\1\105"+
        "\1\116\1\uffff\1\172\1\124\1\105\1\115\1\116\1\114\1\105\1\117\1"+
        "\111\1\172\1\uffff\1\114\1\uffff\1\111\1\106\1\105\1\101\1\124\1"+
        "\125\1\uffff\1\172\1\116\1\uffff\1\172\1\104\1\172\1\uffff\1\172"+
        "\1\111\1\122\1\uffff\1\122\2\uffff\1\116\1\105\1\uffff\1\104\1\123"+
        "\1\120\1\172\1\103\1\116\1\124\3\172\2\uffff\2\172\1\uffff\1\137"+
        "\1\114\1\172\2\uffff\1\172\1\uffff\1\114\1\uffff\1\124\1\172\1\117"+
        "\1\uffff\1\105\1\103\1\120\3\172\1\122\2\101\1\uffff\1\172\1\117"+
        "\1\101\1\172\1\115\1\172\1\124\1\uffff\1\103\1\uffff\1\172\2\uffff"+
        "\1\116\1\111\4\172\2\105\1\uffff\1\124\2\105\5\uffff\1\123\1\137"+
        "\2\uffff\1\105\1\172\1\uffff\1\125\1\172\1\111\1\114\3\uffff\1\172"+
        "\2\124\1\uffff\1\116\1\103\1\uffff\1\172\1\uffff\1\111\1\105\1\uffff"+
        "\1\123\1\104\4\uffff\2\172\1\111\1\172\1\104\2\uffff\1\123\1\172"+
        "\1\uffff\1\123\1\uffff\1\123\1\105\1\uffff\1\105\1\101\1\172\1\105"+
        "\1\uffff\1\116\1\172\1\111\1\101\2\uffff\1\117\1\uffff\1\172\3\uffff"+
        "\1\172\1\111\1\130\2\172\1\uffff\1\172\1\105\1\uffff\1\103\1\102"+
        "\1\116\2\uffff\1\117\1\172\3\uffff\2\172\1\114\1\172\1\116\3\uffff"+
        "\1\105\1\uffff\2\172\2\uffff";
    static final String DFA32_acceptS =
        "\1\uffff\1\1\1\uffff\1\1\1\2\6\uffff\1\10\1\11\3\uffff\1\16\3\uffff"+
        "\1\26\1\27\1\30\1\31\1\32\1\uffff\1\37\1\40\25\uffff\1\u00c7\1\u00c8"+
        "\1\u00c9\1\10\1\2\1\3\1\4\1\uffff\1\57\1\u00b4\1\5\3\uffff\1\u00c7"+
        "\1\6\3\uffff\1\7\1\11\3\uffff\1\33\1\13\1\15\1\14\1\16\1\20\1\21"+
        "\1\17\1\23\1\22\1\25\1\24\1\26\1\27\1\30\1\31\1\32\1\35\1\36\1\34"+
        "\1\37\1\40\1\uffff\1\41\11\uffff\1\u00b5\1\60\61\uffff\1\u00c8\13"+
        "\uffff\1\151\1\146\1\uffff\1\61\1\u00b6\43\uffff\1\115\20\uffff"+
        "\1\144\26\uffff\1\u0095\21\uffff\1\172\10\uffff\1\42\1\uffff\1\43"+
        "\2\uffff\1\44\1\45\1\46\1\47\3\uffff\1\54\20\uffff\1\u00b2\32\uffff"+
        "\1\u00af\47\uffff\1\u0098\11\uffff\1\u00b3\1\166\1\167\13\uffff"+
        "\1\u00b7\2\uffff\1\u00bc\3\uffff\1\u00c5\1\u00c6\1\42\1\55\1\43"+
        "\1\uffff\1\52\1\44\1\45\1\46\1\47\2\uffff\1\53\1\54\1\63\1\u0087"+
        "\11\uffff\1\101\1\102\11\uffff\1\111\10\uffff\1\122\2\uffff\1\126"+
        "\20\uffff\1\131\2\uffff\1\134\6\uffff\1\145\2\uffff\1\161\5\uffff"+
        "\1\174\11\uffff\1\u0086\1\u008c\2\uffff\1\u0091\3\uffff\1\u0094"+
        "\1\u0096\3\uffff\1\u009b\3\uffff\1\u00b1\2\uffff\1\77\7\uffff\1"+
        "\152\3\uffff\1\u00b8\1\u00b9\1\u00bb\1\u00bd\1\u00be\1\uffff\1\u00c0"+
        "\1\u00c4\1\uffff\1\55\1\56\1\52\1\50\1\uffff\1\53\11\uffff\1\103"+
        "\1\104\1\110\16\uffff\1\125\3\uffff\1\u00a2\4\uffff\1\u00a7\12\uffff"+
        "\1\135\1\136\13\uffff\1\175\1\uffff\1\177\14\uffff\1\u0099\1\uffff"+
        "\1\u009c\1\u009d\1\u009e\6\uffff\1\153\4\uffff\1\150\2\uffff\1\56"+
        "\1\50\1\51\1\uffff\1\u0089\1\u008a\1\u008b\1\uffff\1\105\7\uffff"+
        "\1\73\7\uffff\1\116\2\uffff\1\123\22\uffff\1\137\1\uffff\1\140\2"+
        "\uffff\1\157\3\uffff\1\164\10\uffff\1\u0085\2\uffff\1\u008d\3\uffff"+
        "\1\u0093\6\uffff\1\12\1\62\2\uffff\1\156\3\uffff\1\51\1\uffff\1"+
        "\64\3\uffff\1\66\6\uffff\1\112\12\uffff\1\u00a3\1\uffff\1\u00a5"+
        "\6\uffff\1\u00ad\2\uffff\1\132\3\uffff\1\143\3\uffff\1\165\1\uffff"+
        "\1\176\1\u0081\2\uffff\1\u0083\12\uffff\1\170\1\171\2\uffff\1\147"+
        "\3\uffff\1\106\1\107\1\uffff\1\67\1\uffff\1\71\3\uffff\1\114\11"+
        "\uffff\1\u00a0\7\uffff\1\u00ae\1\uffff\1\133\1\uffff\1\142\1\160"+
        "\10\uffff\1\u0090\3\uffff\1\u009a\1\76\1\100\1\154\1\155\2\uffff"+
        "\1\u0088\1\65\2\uffff\1\74\4\uffff\1\u00b0\1\121\1\124\3\uffff\1"+
        "\u00a4\2\uffff\1\u00a9\1\uffff\1\u00ab\2\uffff\1\141\2\uffff\1\173"+
        "\1\u0080\1\u0082\1\u0084\5\uffff\1\u00bf\1\u00c2\2\uffff\1\72\1"+
        "\uffff\1\113\2\uffff\1\127\4\uffff\1\u00aa\4\uffff\1\u008e\1\u008f"+
        "\1\uffff\1\u0092\1\uffff\1\u00c1\1\u00c3\1\70\5\uffff\1\u00a6\2"+
        "\uffff\1\130\3\uffff\1\u0097\1\75\2\uffff\1\u009f\1\u00a1\1\u00a8"+
        "\5\uffff\1\120\1\u00ac\1\162\1\uffff\1\u00ba\2\uffff\1\117\1\163";
    static final String DFA32_specialS =
        "\u03ac\uffff}>";
    static final String[] DFA32_transitionS = {
            "\11\63\1\13\1\3\1\63\1\13\1\2\22\63\1\13\1\62\1\6\1\14\1\63"+
            "\1\27\1\4\1\5\1\25\1\33\1\16\1\30\1\20\1\26\1\35\1\31\12\7\1"+
            "\17\1\1\1\23\1\21\1\22\2\63\1\41\1\10\1\37\1\43\1\44\1\45\1"+
            "\46\1\61\1\15\1\61\1\60\1\40\1\47\1\50\1\11\1\52\1\61\1\36\1"+
            "\53\1\54\1\55\1\56\1\57\2\61\1\12\1\24\1\63\1\32\1\63\1\34\1"+
            "\63\1\61\1\42\14\61\1\51\12\61\1\12\uff84\63",
            "",
            "\1\3",
            "",
            "",
            "\uffff\66",
            "\uffff\66",
            "\12\70\12\uffff\2\71\2\uffff\1\72\33\uffff\2\71",
            "\1\73\4\uffff\1\73\31\uffff\1\74\7\uffff\1\76\2\uffff\1\75",
            "\1\100\4\uffff\1\100\46\uffff\1\101\1\uffff\1\102\4\uffff\1"+
            "\103",
            "\1\104\4\uffff\1\104",
            "",
            "",
            "\1\107\6\uffff\1\110\1\106",
            "\1\111",
            "\1\113",
            "",
            "\1\116\1\117",
            "\1\121",
            "\1\123",
            "",
            "",
            "",
            "",
            "",
            "\1\133\15\uffff\1\132",
            "",
            "",
            "\1\137",
            "\12\71\7\uffff\1\147\2\152\1\151\1\141\1\146\1\144\4\152\1"+
            "\143\1\152\1\142\1\150\4\152\1\145\6\152\6\uffff\3\152\2\151"+
            "\25\152",
            "\1\154",
            "\1\157\6\uffff\1\156\3\uffff\1\160\2\uffff\1\155\11\uffff\1"+
            "\161",
            "\1\163\11\uffff\1\162",
            "\1\164\11\uffff\1\165\6\uffff\1\166",
            "\1\73\4\uffff\1\73",
            "\1\167\3\uffff\1\170\3\uffff\1\172\5\uffff\1\171",
            "\1\173\1\uffff\1\174\2\uffff\1\175\6\uffff\1\176",
            "\1\177\2\uffff\1\u0080\2\uffff\1\u0081\5\uffff\1\u0082",
            "\1\u0083\11\uffff\1\u0084",
            "\1\u0085",
            "\1\u0086\15\uffff\1\u0087\5\uffff\1\u0088",
            "\1\100\4\uffff\1\100",
            "\1\u0089\15\uffff\1\u008a\2\uffff\1\u008b\2\uffff\1\u008c",
            "\1\u008d\3\uffff\1\u008e\16\uffff\1\u008f\1\u0090",
            "\1\u0091\6\uffff\1\u0092\6\uffff\1\u0093\11\uffff\1\u0094",
            "\1\u0095\4\uffff\1\u0096",
            "\1\u0097\15\uffff\1\u0098",
            "\1\u0099\6\uffff\1\u009a\11\uffff\1\u009b",
            "\1\u009c",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\70\12\uffff\2\71\2\uffff\1\72\33\uffff\2\71",
            "",
            "",
            "",
            "\1\u009e",
            "\1\u009f",
            "\1\u00a0",
            "",
            "",
            "\1\u00a1",
            "\1\u00a2\16\uffff\1\u00a3",
            "\1\u00a4",
            "",
            "",
            "\12\77\7\uffff\2\77\1\u00a5\13\77\1\u00a7\1\77\1\u00a8\2\77"+
            "\1\u00a6\6\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u00ab",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00ad\7\uffff\1\u00ae\3\uffff\1\u00ac",
            "",
            "\1\71\1\uffff\1\71\1\152\1\uffff\12\71\7\uffff\20\152\1\u00af"+
            "\11\152\6\uffff\32\152",
            "\1\152\22\uffff\4\152\1\u00b0\11\152\1\u00b1\13\152\6\uffff"+
            "\32\152",
            "\1\152\22\uffff\4\152\1\u00b3\16\152\1\u00b2\6\152\6\uffff"+
            "\32\152",
            "\1\152\22\uffff\4\152\1\u00b5\16\152\1\u00b4\6\152\6\uffff"+
            "\32\152",
            "\1\152\22\uffff\21\152\1\u00b6\10\152\6\uffff\32\152",
            "\1\152\22\uffff\1\u00b7\31\152\6\uffff\32\152",
            "\1\152\22\uffff\15\152\1\u00b8\14\152\6\uffff\32\152",
            "\1\152\22\uffff\21\152\1\u00b9\10\152\6\uffff\32\152",
            "\1\71\1\uffff\1\71\1\152\1\uffff\12\71\7\uffff\32\152\6\uffff"+
            "\32\152",
            "",
            "",
            "\1\u00ba\1\uffff\1\u00bb\17\uffff\1\u00bc\1\u00bd\2\uffff\1"+
            "\u00be",
            "\1\u00bf\1\u00c0",
            "\1\u00c1",
            "\1\u00c2\6\uffff\1\u00c3",
            "\1\u00c4\15\uffff\1\u00c5",
            "\1\u00c6",
            "\1\u00c7",
            "\1\u00c8",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb\5\uffff\1\u00cc",
            "\1\u00cd",
            "\1\u00cf\4\uffff\1\u00ce",
            "\12\77\7\uffff\24\77\1\u00d0\5\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u00d2",
            "\1\u00d3\15\uffff\1\u00d4",
            "\1\u00d7\17\uffff\1\u00d5\1\u00d6",
            "\1\u00d8",
            "\1\u00d9\12\uffff\1\u00da",
            "\1\u00db\1\uffff\1\u00dc",
            "\1\u00dd",
            "\1\u00de",
            "\1\u00df",
            "\1\u00e0",
            "\12\77\7\uffff\23\77\1\u00e1\6\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u00e3",
            "\1\u00e4",
            "\1\u00e5\1\uffff\1\u00e6",
            "\1\u00e7",
            "\1\u00e8\1\u00e9\1\uffff\1\u00ea",
            "\1\u00eb",
            "\1\u00ed\3\uffff\1\u00ec\5\uffff\1\u00ee",
            "\1\u00ef\17\uffff\1\u00f0",
            "\1\u00f1",
            "\1\u00f2\4\uffff\1\u00f3",
            "\1\u00f5\1\uffff\1\u00f4",
            "\1\u00f6",
            "\1\u00f7",
            "\1\u00f8",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u00fa",
            "\1\u00fb",
            "\1\u00fc",
            "\1\u00fd",
            "\1\u00fe",
            "\1\u00ff",
            "\1\u0100\3\uffff\1\u0101",
            "\1\u0102",
            "\1\u0103",
            "",
            "\1\u0104",
            "\1\u0105",
            "\1\u0106",
            "\1\u0107",
            "\1\u0108\3\uffff\1\u0109",
            "\1\u010a",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u010c",
            "\1\u010d\14\uffff\1\u010e",
            "\1\u010f",
            "\1\u0110",
            "",
            "",
            "\1\u0111\2\uffff\1\u0112",
            "",
            "",
            "\1\u0113",
            "\1\u0114\22\uffff\25\152\1\u0115\4\152\6\uffff\32\152",
            "\1\u0116\22\uffff\20\152\1\u0117\11\152\6\uffff\32\152",
            "\1\152\22\uffff\23\152\1\u0118\6\152\6\uffff\32\152",
            "\1\u0119\22\uffff\32\152\6\uffff\32\152",
            "\1\u011a\22\uffff\32\152\6\uffff\32\152",
            "\1\u011b\22\uffff\32\152\6\uffff\32\152",
            "\1\u011c\22\uffff\32\152\6\uffff\32\152",
            "\1\152\22\uffff\24\152\1\u011d\5\152\6\uffff\32\152",
            "\1\152\22\uffff\13\152\1\u011e\16\152\6\uffff\32\152",
            "\1\152\22\uffff\3\152\1\u011f\26\152\6\uffff\32\152",
            "\1\u0120\22\uffff\32\152\6\uffff\32\152",
            "\1\u0122\7\uffff\1\u0121",
            "\1\u0123",
            "\1\u0124",
            "\1\u0125",
            "\1\u0126",
            "\1\u0128\2\uffff\1\u0127",
            "\1\u0129",
            "\1\u012a",
            "\1\u012b",
            "\1\u012c",
            "\1\u012d",
            "\1\u012e",
            "\1\u012f",
            "\1\u0130",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0132",
            "\1\u0133",
            "\1\u0134\5\uffff\1\u0135",
            "\1\u0136",
            "\1\u0137",
            "\1\u0138\3\uffff\1\u0139",
            "\1\u013a",
            "\1\u013b",
            "",
            "\1\u013c",
            "\1\u013d",
            "\1\u013e",
            "\1\u013f",
            "\1\u0140",
            "\12\77\7\uffff\1\u0141\1\u0142\1\77\1\u0143\1\u0144\1\u0145"+
            "\2\77\1\u0146\3\77\1\u0147\2\77\1\u0148\2\77\1\u0149\1\u014a"+
            "\2\77\1\u014b\3\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u014d",
            "\1\u014e",
            "\1\u014f",
            "\1\u0150",
            "\1\u0151",
            "\1\u0152",
            "\1\u0153\13\uffff\1\u0154",
            "\1\u0155",
            "\1\u0156",
            "\1\u0157",
            "",
            "\1\u0158",
            "\1\u0159",
            "\1\u015a\31\uffff\1\u015b",
            "\1\u015c",
            "\1\u015d",
            "\1\u015e",
            "\1\u015f",
            "\1\u0160",
            "\1\u0161",
            "\1\u0162\7\uffff\1\u0163",
            "\1\u0164",
            "\1\u0165\3\uffff\1\u0166\14\uffff\1\u0167",
            "\1\u0168",
            "\1\u0169",
            "\1\u016a",
            "\1\u016b",
            "\1\u016c",
            "\1\u016d",
            "\1\u016e",
            "\1\u016f",
            "\1\u0170",
            "\1\u0171",
            "",
            "\1\u0172",
            "\1\u0173",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0175",
            "\1\u0176",
            "\1\u0177",
            "\1\u0178",
            "\1\u0179",
            "\1\u017a",
            "\1\u017b",
            "\1\u017c",
            "\1\u017d",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0181",
            "\1\u0182",
            "",
            "\1\u0183",
            "\1\u0184\6\uffff\1\u0185\3\uffff\1\u0186",
            "\1\u0187",
            "\1\u0188",
            "\1\u0189",
            "\1\u018a",
            "\1\u018b",
            "\1\u018e\1\uffff\1\u018d\1\u018c\1\u0194\1\u0192\2\uffff\1"+
            "\u0191\6\uffff\1\u018f\1\uffff\1\u0193\4\uffff\1\u0190",
            "",
            "\1\u0196\22\uffff\32\152\6\uffff\32\152",
            "",
            "\1\152\22\uffff\25\152\1\u0198\4\152\6\uffff\32\152",
            "\1\u0199\22\uffff\32\152\6\uffff\32\152",
            "",
            "",
            "",
            "",
            "\1\152\22\uffff\4\152\1\u019e\25\152\6\uffff\32\152",
            "\1\152\22\uffff\22\152\1\u019f\7\152\6\uffff\32\152",
            "\1\u01a0\22\uffff\32\152\6\uffff\32\152",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01a4",
            "\1\u01a5",
            "\1\u01a6",
            "\1\u01a7",
            "\1\u01a8",
            "\1\u01a9",
            "\1\u01aa\7\uffff\1\u01ab",
            "\1\u01ac",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01af",
            "\1\u01b0",
            "\1\u01b1",
            "\1\u01b2",
            "",
            "\1\u01b3",
            "\1\u01b4",
            "\1\u01b5",
            "\1\u01b6",
            "\1\u01b7",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01b9",
            "\1\u01ba",
            "\1\u01bb",
            "\1\u01bc",
            "\1\u01bd",
            "\1\u01be",
            "\12\77\7\uffff\10\77\1\u01bf\15\77\1\u01c0\3\77\4\uffff\1\77"+
            "\1\uffff\32\77",
            "\1\u01c2",
            "\12\77\7\uffff\4\77\1\u01c3\25\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01c5",
            "\1\u01c6",
            "\1\u01c7",
            "\1\u01c8",
            "\1\u01ca\5\uffff\1\u01c9\5\uffff\1\u01cb",
            "\1\u01cc\7\uffff\1\u01cd",
            "\1\u01ce",
            "\1\u01cf",
            "\1\u01d0\17\uffff\1\u01d1",
            "\1\u01d2",
            "\1\u01d3",
            "",
            "\1\u01d4",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01d6\3\uffff\1\u01d7",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01d9",
            "\1\u01da",
            "\1\u01db",
            "\1\u01dc",
            "\1\u01dd",
            "\1\u01de",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01e0",
            "\1\u01e1",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01e3\5\uffff\1\u01e4",
            "\1\u01e5",
            "\1\u01e6",
            "\1\u01e7",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01e9",
            "\1\u01ea",
            "\1\u01eb",
            "\1\u01ec",
            "\1\u01ed",
            "\1\u01ee",
            "\1\u01ef",
            "\1\u01f0",
            "\1\u01f1",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01f4",
            "\1\u01f5",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01f7",
            "\1\u01f8",
            "\1\u01f9",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u01fc",
            "",
            "\1\u01fd",
            "\1\u01fe",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0200",
            "\1\u0201",
            "\1\u0202",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0204",
            "\12\77\7\uffff\3\77\1\u0205\26\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "",
            "",
            "\1\u0207",
            "\1\u0208",
            "\1\u0209",
            "\1\u020a",
            "\1\u020b",
            "\1\u020c",
            "\1\u020d",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u020f",
            "\1\u0210",
            "\1\u0211",
            "",
            "\1\u0213\6\uffff\1\u0212",
            "\1\u0216\5\uffff\1\u0215\1\u0214",
            "",
            "\1\u0217",
            "\1\u0218\7\uffff\1\u0219",
            "\1\u021a",
            "",
            "",
            "",
            "",
            "",
            "\1\u021c\22\uffff\32\152\6\uffff\32\152",
            "",
            "",
            "",
            "",
            "",
            "\1\u021e\22\uffff\32\152\6\uffff\32\152",
            "\1\152\22\uffff\4\152\1\u021f\25\152\6\uffff\32\152",
            "",
            "",
            "",
            "",
            "\1\u0221",
            "\1\u0222",
            "\1\u0223",
            "\1\u0224",
            "\1\u0225",
            "\1\u0226",
            "\1\u0227",
            "\1\u0228",
            "\1\u0229",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u022d",
            "\1\u022e",
            "\1\u022f",
            "\1\u0230",
            "\1\u0231",
            "\1\u0232",
            "",
            "\1\u0233",
            "\1\u0234",
            "\1\u0235",
            "\1\u0236",
            "\1\u0237",
            "\1\u0238",
            "\1\u0239",
            "\1\u023a",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u023c",
            "",
            "\1\u023d",
            "\1\u023e",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0240",
            "\1\u0241",
            "\1\u0242",
            "\1\u0243",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0245",
            "\1\u0246",
            "\1\u0247",
            "\1\u0248",
            "\1\u0249",
            "\1\u024a",
            "\1\u024b",
            "\1\u024c",
            "",
            "\1\u024d",
            "\1\u024e",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0251",
            "\1\u0252",
            "\1\u0253",
            "\1\u0254",
            "",
            "\1\u0255",
            "\1\u0256",
            "",
            "\1\u0257",
            "\1\u0258",
            "\1\u0259",
            "\1\u025a",
            "\1\u025b",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u025d",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u025f",
            "\1\u0260",
            "\1\u0261",
            "\1\u0262",
            "\1\u0263",
            "\1\u0264",
            "",
            "",
            "\1\u0265",
            "\1\u0266",
            "",
            "\1\u0267",
            "\1\u0268",
            "\1\u0269",
            "",
            "",
            "\1\u026a",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u026c",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u0270",
            "\1\u0271",
            "",
            "\1\u0272",
            "\1\u0273",
            "\1\u0274",
            "\1\u0275",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0277",
            "\1\u0278",
            "",
            "\1\u0279",
            "\1\u027a",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "",
            "",
            "",
            "",
            "\1\u027c",
            "",
            "",
            "\1\u027d",
            "",
            "",
            "",
            "",
            "\1\u0280\22\uffff\32\152\6\uffff\32\152",
            "",
            "\1\u0281",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0285",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0287",
            "\1\u0288",
            "\1\u0289",
            "",
            "",
            "",
            "\1\u028a",
            "\1\u028b",
            "\1\u028c",
            "\12\77\7\uffff\14\77\1\u028d\15\77\4\uffff\1\77\1\uffff\32"+
            "\77",
            "\1\u028f",
            "\1\u0290",
            "\1\u0291",
            "\1\u0292",
            "\1\u0293",
            "\12\77\7\uffff\2\77\1\u0295\14\77\1\u0294\12\77\4\uffff\1\77"+
            "\1\uffff\32\77",
            "\1\u0297",
            "\1\u0298",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u029a",
            "",
            "\1\u029b",
            "\1\u029c",
            "\1\u029d",
            "",
            "\1\u029e",
            "\1\u029f",
            "\1\u02a0",
            "\1\u02a1",
            "",
            "\1\u02a2",
            "\1\u02a3",
            "\1\u02a4",
            "\1\u02a5",
            "\1\u02a6",
            "\1\u02a7",
            "\1\u02a8",
            "\1\u02a9",
            "\1\u02aa",
            "\1\u02ab",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\23\77\1\u02ad\6\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02af",
            "\1\u02b0",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02b2",
            "\1\u02b3",
            "\1\u02b4",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02b6",
            "\1\u02b7",
            "",
            "\1\u02b8",
            "",
            "\1\u02b9",
            "\1\u02ba",
            "\1\u02bb",
            "\1\u02bc",
            "\1\u02bd",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\2\77\1\u02bf\20\77\1\u02c0\6\77\4\uffff\1\77"+
            "\1\uffff\32\77",
            "\1\u02c2",
            "\1\u02c3",
            "\1\u02c4",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02c6",
            "",
            "\1\u02c7",
            "",
            "",
            "",
            "\1\u02c8",
            "\1\u02c9",
            "\1\u02ca",
            "\1\u02cb",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u02ce",
            "\1\u02cf",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02d1",
            "",
            "\1\u02d2",
            "\1\u02d3",
            "",
            "",
            "",
            "\1\u02d5",
            "",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u02d7",
            "\1\u02d8",
            "\1\u02d9",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02db",
            "\1\u02dc\3\uffff\1\u02dd",
            "\1\u02de",
            "",
            "\1\u02df",
            "\1\u02e0",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02e2",
            "\1\u02e3",
            "\1\u02e4",
            "\1\u02e5",
            "",
            "\1\u02e6",
            "\1\u02e7",
            "",
            "\1\u02e8",
            "\1\u02e9",
            "\1\u02ea",
            "\1\u02eb",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02ed",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02ef",
            "\1\u02f0",
            "\1\u02f1",
            "\1\u02f2",
            "\1\u02f3",
            "\1\u02f4",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02f6",
            "\1\u02f7",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u02f9",
            "",
            "\1\u02fa",
            "",
            "\1\u02fb",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u02fd",
            "\1\u02fe",
            "\1\u02ff",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0301",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0304",
            "\1\u0305",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0307",
            "",
            "\1\u0308",
            "\1\u0309",
            "",
            "\1\u030a",
            "\1\u030b",
            "\1\u030c",
            "",
            "\1\u030d",
            "\1\u030e",
            "\1\u030f",
            "\1\u0310",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "",
            "\1\u0313",
            "\1\u0314",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0316",
            "\1\u0317",
            "",
            "\1\u0318",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u031b",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u031d",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u031f",
            "\1\u0320",
            "\1\u0321",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0323",
            "\1\u0324",
            "\1\u0325",
            "\1\u0326",
            "\1\u0327",
            "\1\u0328",
            "\1\u0329",
            "\1\u032a",
            "\12\77\7\uffff\3\77\1\u032b\26\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u032d",
            "",
            "\1\u032e",
            "\1\u032f",
            "\1\u0330",
            "\1\u0331",
            "\1\u0332",
            "\1\u0333",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0335",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0337",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u033a",
            "\1\u033b",
            "",
            "\1\u033c",
            "",
            "",
            "\1\u033d",
            "\1\u033e",
            "",
            "\1\u033f",
            "\1\u0340",
            "\1\u0341",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0343",
            "\1\u0344",
            "\1\u0345",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u034b",
            "\1\u034c",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u034f",
            "",
            "\1\u0350",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0352",
            "",
            "\1\u0353",
            "\1\u0354",
            "\1\u0355",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0359",
            "\1\u035a",
            "\1\u035b",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u035d",
            "\1\u035e",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0360",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0362",
            "",
            "\1\u0363",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "",
            "\1\u0365",
            "\1\u0366",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u036b",
            "\1\u036c",
            "",
            "\1\u036d",
            "\1\u036e",
            "\1\u036f",
            "",
            "",
            "",
            "",
            "",
            "\1\u0371\17\uffff\1\u0370",
            "\1\u0372",
            "",
            "",
            "\1\u0373",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u0375",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0377",
            "\1\u0378",
            "",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u037a",
            "\1\u037b",
            "",
            "\1\u037c",
            "\1\u037d",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u037f",
            "\1\u0380",
            "",
            "\1\u0381",
            "\1\u0382",
            "",
            "",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0385",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0387",
            "",
            "",
            "\1\u0389\17\uffff\1\u0388",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\u038b",
            "",
            "\1\u038c",
            "\1\u038d",
            "",
            "\1\u038e",
            "\1\u038f",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0391",
            "",
            "\1\u0392",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0394",
            "\1\u0395",
            "",
            "",
            "\1\u0396",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u0399",
            "\1\u039a",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u039e",
            "",
            "\1\u039f",
            "\1\u03a0",
            "\1\u03a1",
            "",
            "",
            "\1\u03a2",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u03a6",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\u03a8",
            "",
            "",
            "",
            "\1\u03a9",
            "",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\12\77\7\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            ""
    };

    static final short[] DFA32_eot = DFA.unpackEncodedString(DFA32_eotS);
    static final short[] DFA32_eof = DFA.unpackEncodedString(DFA32_eofS);
    static final char[] DFA32_min = DFA.unpackEncodedStringToUnsignedChars(DFA32_minS);
    static final char[] DFA32_max = DFA.unpackEncodedStringToUnsignedChars(DFA32_maxS);
    static final short[] DFA32_accept = DFA.unpackEncodedString(DFA32_acceptS);
    static final short[] DFA32_special = DFA.unpackEncodedString(DFA32_specialS);
    static final short[][] DFA32_transition;

    static {
        int numStates = DFA32_transitionS.length;
        DFA32_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA32_transition[i] = DFA.unpackEncodedString(DFA32_transitionS[i]);
        }
    }

    class DFA32 extends DFA {

        public DFA32(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 32;
            this.eot = DFA32_eot;
            this.eof = DFA32_eof;
            this.min = DFA32_min;
            this.max = DFA32_max;
            this.accept = DFA32_accept;
            this.special = DFA32_special;
            this.transition = DFA32_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T_EOS | CONTINUE_CHAR | T_CHAR_CONSTANT | T_DIGIT_STRING | BINARY_CONSTANT | OCTAL_CONSTANT | HEX_CONSTANT | WS | PREPROCESS_LINE | T_INCLUDE | T_ASTERISK | T_COLON | T_COLON_COLON | T_COMMA | T_EQUALS | T_EQ_EQ | T_EQ_GT | T_GREATERTHAN | T_GREATERTHAN_EQ | T_LESSTHAN | T_LESSTHAN_EQ | T_LBRACKET | T_LPAREN | T_MINUS | T_PERCENT | T_PLUS | T_POWER | T_SLASH | T_SLASH_EQ | T_SLASH_SLASH | T_RBRACKET | T_RPAREN | T_UNDERSCORE | T_EQ | T_NE | T_LT | T_LE | T_GT | T_GE | T_TRUE | T_FALSE | T_NOT | T_AND | T_OR | T_EQV | T_NEQV | T_PERIOD_EXPONENT | T_PERIOD | T_XYZ | T_INTEGER | T_REAL | T_COMPLEX | T_CHARACTER | T_LOGICAL | T_ABSTRACT | T_ALLOCATABLE | T_ALLOCATE | T_ASSIGNMENT | T_ASSIGN | T_ASSOCIATE | T_ASYNCHRONOUS | T_BACKSPACE | T_BLOCK | T_BLOCKDATA | T_CALL | T_CASE | T_CLASS | T_CLOSE | T_COMMON | T_CONTAINS | T_CONTINUE | T_CYCLE | T_DATA | T_DEFAULT | T_DEALLOCATE | T_DEFERRED | T_DO | T_DOUBLE | T_DOUBLEPRECISION | T_DOUBLECOMPLEX | T_ELEMENTAL | T_ELSE | T_ELSEIF | T_ELSEWHERE | T_ENTRY | T_ENUM | T_ENUMERATOR | T_EQUIVALENCE | T_EXIT | T_EXTENDS | T_EXTERNAL | T_FILE | T_FINAL | T_FLUSH | T_FORALL | T_FORMAT | T_FORMATTED | T_FUNCTION | T_GENERIC | T_GO | T_GOTO | T_IF | T_IMPLICIT | T_IMPORT | T_IN | T_INOUT | T_INTENT | T_INTERFACE | T_INTRINSIC | T_INQUIRE | T_MODULE | T_NAMELIST | T_NONE | T_NON_INTRINSIC | T_NON_OVERRIDABLE | T_NOPASS | T_NULLIFY | T_ONLY | T_OPEN | T_OPERATOR | T_OPTIONAL | T_OUT | T_PARAMETER | T_PASS | T_PAUSE | T_POINTER | T_PRINT | T_PRECISION | T_PRIVATE | T_PROCEDURE | T_PROGRAM | T_PROTECTED | T_PUBLIC | T_PURE | T_READ | T_RECURSIVE | T_RESULT | T_RETURN | T_REWIND | T_SAVE | T_SELECT | T_SELECTCASE | T_SELECTTYPE | T_SEQUENCE | T_STOP | T_SUBROUTINE | T_TARGET | T_THEN | T_TO | T_TYPE | T_UNFORMATTED | T_USE | T_VALUE | T_VOLATILE | T_WAIT | T_WHERE | T_WHILE | T_WRITE | T_ENDASSOCIATE | T_ENDBLOCK | T_ENDBLOCKDATA | T_ENDDO | T_ENDENUM | T_ENDFORALL | T_ENDFILE | T_ENDFUNCTION | T_ENDIF | T_ENDINTERFACE | T_ENDMODULE | T_ENDPROGRAM | T_ENDSELECT | T_ENDSUBROUTINE | T_ENDTYPE | T_ENDWHERE | T_END | T_DIMENSION | T_KIND | T_LEN | T_BIND | T_HOLLERITH | T_DEFINED_OP | T_LABEL_DO_TERMINAL | T_DATA_EDIT_DESC | T_CONTROL_EDIT_DESC | T_CHAR_STRING_EDIT_DESC | T_STMT_FUNCTION | T_ASSIGNMENT_STMT | T_PTR_ASSIGNMENT_STMT | T_ARITHMETIC_IF_STMT | T_ALLOCATE_STMT_1 | T_WHERE_STMT | T_IF_STMT | T_FORALL_STMT | T_WHERE_CONSTRUCT_STMT | T_FORALL_CONSTRUCT_STMT | T_INQUIRE_STMT_2 | T_REAL_CONSTANT | T_EOF | T_IDENT | LINE_COMMENT | MISC_CHAR );";
        }
    }
 

}