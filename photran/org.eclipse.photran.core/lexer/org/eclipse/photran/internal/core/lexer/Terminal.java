/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

/**
 * Terminal symbols used by the Fortran lexer and parser.
 * 
 * @author Jeff Overbey
 */
public final class Terminal
{
    public static final Terminal T_WRITEEQ = new Terminal(0, "t writeeq");
    public static final Terminal T_RESULT = new Terminal(1, "t result");
    public static final Terminal T_ENDSUBROUTINE = new Terminal(2, "t endsubroutine");
    public static final Terminal T_ENDBLOCKDATA = new Terminal(3, "t endblockdata");
    public static final Terminal T_DOUBLE = new Terminal(4, "t double");
    public static final Terminal T_FILE = new Terminal(5, "t file");
    public static final Terminal T_LESSTHANEQ = new Terminal(6, "t lessthaneq");
    public static final Terminal T_ENDFILE = new Terminal(7, "t endfile");
    public static final Terminal T_BACKSPACE = new Terminal(8, "t backspace");
    public static final Terminal T_PCON = new Terminal(9, "t pcon");
    public static final Terminal T_FORALL = new Terminal(10, "t forall");
    public static final Terminal T_SELECTCASE = new Terminal(11, "t selectcase");
    public static final Terminal T_MINUS = new Terminal(12, "t minus");
    public static final Terminal T_WRITE = new Terminal(13, "t write");
    public static final Terminal T_NONE = new Terminal(14, "t none");
    public static final Terminal T_COMMON = new Terminal(15, "t common");
    public static final Terminal T_CYCLE = new Terminal(16, "t cycle");
    public static final Terminal T_IOSTATEQ = new Terminal(17, "t iostateq");
    public static final Terminal T_EQEQ = new Terminal(18, "t eqeq");
    public static final Terminal T_ASTERISK = new Terminal(19, "t asterisk");
    public static final Terminal T_ENDEQ = new Terminal(20, "t endeq");
    public static final Terminal T_PUBLIC = new Terminal(21, "t public");
    public static final Terminal T_FCON = new Terminal(22, "t fcon");
    public static final Terminal T_ELSE = new Terminal(23, "t else");
    public static final Terminal T_ENDMODULE = new Terminal(24, "t endmodule");
    public static final Terminal T_PAUSE = new Terminal(25, "t pause");
    public static final Terminal T_XDOP = new Terminal(26, "t xdop");
    public static final Terminal T_END = new Terminal(27, "t end");
    public static final Terminal T_PURE = new Terminal(28, "t pure");
    public static final Terminal T_WHERE = new Terminal(29, "t where");
    public static final Terminal T_ENTRY = new Terminal(30, "t entry");
    public static final Terminal T_CONTAINS = new Terminal(31, "t contains");
    public static final Terminal T_OPTIONAL = new Terminal(32, "t optional");
    public static final Terminal T_TO = new Terminal(33, "t to");
    public static final Terminal T_ALLOCATABLE = new Terminal(34, "t allocatable");
    public static final Terminal T_COMMA = new Terminal(35, "t comma");
    public static final Terminal T_SIZEEQ = new Terminal(36, "t sizeeq");
    public static final Terminal T_BLANKEQ = new Terminal(37, "t blankeq");
    public static final Terminal T_ASSIGN = new Terminal(38, "t assign");
    public static final Terminal T_ENDSELECT = new Terminal(39, "t endselect");
    public static final Terminal T_GO = new Terminal(40, "t go");
    public static final Terminal T_POSITIONEQ = new Terminal(41, "t positioneq");
    public static final Terminal T_REWIND = new Terminal(42, "t rewind");
    public static final Terminal T_CLOSE = new Terminal(43, "t close");
    public static final Terminal T_BLOCK = new Terminal(44, "t block");
    public static final Terminal T_CONTINUE = new Terminal(45, "t continue");
    public static final Terminal T_DEALLOCATE = new Terminal(46, "t deallocate");
    public static final Terminal T_NAMEDEQ = new Terminal(47, "t namedeq");
    public static final Terminal T_EOS = new Terminal(48, "t eos");
    public static final Terminal T_STATEQ = new Terminal(49, "t stateq");
    public static final Terminal T_SLASH = new Terminal(50, "t slash");
    public static final Terminal T_ENDTYPE = new Terminal(51, "t endtype");
    public static final Terminal T_LESSTHAN = new Terminal(52, "t lessthan");
    public static final Terminal T_DIRECTEQ = new Terminal(53, "t directeq");
    public static final Terminal T_CHARACTER = new Terminal(54, "t character");
    public static final Terminal T_SLASHRPAREN = new Terminal(55, "t slashrparen");
    public static final Terminal T_NAMEEQ = new Terminal(56, "t nameeq");
    public static final Terminal T_FUNCTION = new Terminal(57, "t function");
    public static final Terminal T_INQUIRE = new Terminal(58, "t inquire");
    public static final Terminal T_NUMBEREQ = new Terminal(59, "t numbereq");
    public static final Terminal T_BCON = new Terminal(60, "t bcon");
    public static final Terminal T_RPAREN = new Terminal(61, "t rparen");
    public static final Terminal T_ENDFORALL = new Terminal(62, "t endforall");
    public static final Terminal T_LOGICAL = new Terminal(63, "t logical");
    public static final Terminal T_NE = new Terminal(64, "t ne");
    public static final Terminal T_PADEQ = new Terminal(65, "t padeq");
    public static final Terminal T_EQV = new Terminal(66, "t eqv");
    public static final Terminal T_ENDPROGRAM = new Terminal(67, "t endprogram");
    public static final Terminal T_RECLEQ = new Terminal(68, "t recleq");
    public static final Terminal T_THEN = new Terminal(69, "t then");
    public static final Terminal T_DIMENSION = new Terminal(70, "t dimension");
    public static final Terminal T_DELIMEQ = new Terminal(71, "t delimeq");
    public static final Terminal T_X_IMPL = new Terminal(72, "T x Impl");
    public static final Terminal T_LT = new Terminal(73, "t lt");
    public static final Terminal T_SUBROUTINE = new Terminal(74, "t subroutine");
    public static final Terminal T_ENDWHERE = new Terminal(75, "t endwhere");
    public static final Terminal T_CALL = new Terminal(76, "t call");
    public static final Terminal T_USE = new Terminal(77, "t use");
    public static final Terminal T_RCON = new Terminal(78, "t rcon");
    public static final Terminal T_FORMEQ = new Terminal(79, "t formeq");
    public static final Terminal T_FMTEQ = new Terminal(80, "t fmteq");
    public static final Terminal T_DATA = new Terminal(81, "t data");
    public static final Terminal T_OPEN = new Terminal(82, "t open");
    public static final Terminal T_CASE = new Terminal(83, "t case");
    public static final Terminal T_ASSIGNMENT = new Terminal(84, "t assignment");
    public static final Terminal T_RECEQ = new Terminal(85, "t receq");
    public static final Terminal T_ICON = new Terminal(86, "t icon");
    public static final Terminal T_MODULE = new Terminal(87, "t module");
    public static final Terminal T_REAL = new Terminal(88, "t real");
    public static final Terminal T_FORMAT = new Terminal(89, "t format");
    public static final Terminal T_BLOCKDATA = new Terminal(90, "t blockdata");
    public static final Terminal T_ZCON = new Terminal(91, "t zcon");
    public static final Terminal T_UNITEQ = new Terminal(92, "t uniteq");
    public static final Terminal T_PRECISION = new Terminal(93, "t precision");
    public static final Terminal T_INOUT = new Terminal(94, "t inout");
    public static final Terminal T_ELEMENTAL = new Terminal(95, "t elemental");
    public static final Terminal T_OR = new Terminal(96, "t or");
    public static final Terminal T_EOREQ = new Terminal(97, "t eoreq");
    public static final Terminal T_FALSE = new Terminal(98, "t false");
    public static final Terminal T_INTEGER = new Terminal(99, "t integer");
    public static final Terminal T_EQUIVALENCE = new Terminal(100, "t equivalence");
    public static final Terminal T_STATUSEQ = new Terminal(101, "t statuseq");
    public static final Terminal T_TYPE = new Terminal(102, "t type");
    public static final Terminal T_RETURN = new Terminal(103, "t return");
    public static final Terminal T_SELECT = new Terminal(104, "t select");
    public static final Terminal T_ELSEIF = new Terminal(105, "t elseif");
    public static final Terminal T_IDENT = new Terminal(106, "t ident");
    public static final Terminal T_GE = new Terminal(107, "t ge");
    public static final Terminal T_POW = new Terminal(108, "t pow");
    public static final Terminal T_PARAMETER = new Terminal(109, "t parameter");
    public static final Terminal T_ENDINTERFACE = new Terminal(110, "t endinterface");
    public static final Terminal T_OUT = new Terminal(111, "t out");
    public static final Terminal T_INTENT = new Terminal(112, "t intent");
    public static final Terminal T_EXISTEQ = new Terminal(113, "t existeq");
    public static final Terminal T_NULLIFY = new Terminal(114, "t nullify");
    public static final Terminal T_PRINT = new Terminal(115, "t print");
    public static final Terminal T_EQ = new Terminal(116, "t eq");
    public static final Terminal T_STOP = new Terminal(117, "t stop");
    public static final Terminal T_DEFAULT = new Terminal(118, "t default");
    public static final Terminal T_SEQUENCE = new Terminal(119, "t sequence");
    public static final Terminal T_UNFORMATTEDEQ = new Terminal(120, "t unformattedeq");
    public static final Terminal T_OPERATOR = new Terminal(121, "t operator");
    public static final Terminal T_SCON = new Terminal(122, "t scon");
    public static final Terminal T_ERREQ = new Terminal(123, "t erreq");
    public static final Terminal T_IF = new Terminal(124, "t if");
    public static final Terminal T_ADVANCEEQ = new Terminal(125, "t advanceeq");
    public static final Terminal T_EXTERNAL = new Terminal(126, "t external");
    public static final Terminal T_PRIVATE = new Terminal(127, "t private");
    public static final Terminal T_NEXTRECEQ = new Terminal(128, "t nextreceq");
    public static final Terminal T_SLASHSLASH = new Terminal(129, "t slashslash");
    public static final Terminal T_PLUS = new Terminal(130, "t plus");
    public static final Terminal T_EQGREATERTHAN = new Terminal(131, "t eqgreaterthan");
    public static final Terminal END_OF_INPUT = new Terminal(132, "end of input");
    public static final Terminal T_ENDFUNCTION = new Terminal(133, "t endfunction");
    public static final Terminal T_TARGET = new Terminal(134, "t target");
    public static final Terminal T_PERCENT = new Terminal(135, "t percent");
    public static final Terminal T_READWRITEEQ = new Terminal(136, "t readwriteeq");
    public static final Terminal T_POINTER = new Terminal(137, "t pointer");
    public static final Terminal T_IOLENGTHEQ = new Terminal(138, "t iolengtheq");
    public static final Terminal T_OPENEDEQ = new Terminal(139, "t openedeq");
    public static final Terminal T_LE = new Terminal(140, "t le");
    public static final Terminal T_ENDDO = new Terminal(141, "t enddo");
    public static final Terminal T_IN = new Terminal(142, "t in");
    public static final Terminal T_GOTO = new Terminal(143, "t goto");
    public static final Terminal T_COLON = new Terminal(144, "t colon");
    public static final Terminal T_READ = new Terminal(145, "t read");
    public static final Terminal T_LENEQ = new Terminal(146, "t leneq");
    public static final Terminal T_NOT = new Terminal(147, "t not");
    public static final Terminal T_DCON = new Terminal(148, "t dcon");
    public static final Terminal T_ALLOCATE = new Terminal(149, "t allocate");
    public static final Terminal T_EQUALS = new Terminal(150, "t equals");
    public static final Terminal T_ENDIF = new Terminal(151, "t endif");
    public static final Terminal T_TRUE = new Terminal(152, "t true");
    public static final Terminal T_UNDERSCORE = new Terminal(153, "t underscore");
    public static final Terminal T_XCON = new Terminal(154, "t xcon");
    public static final Terminal T_IMPLICIT = new Terminal(155, "t implicit");
    public static final Terminal T_NAMELIST = new Terminal(156, "t namelist");
    public static final Terminal T_RECURSIVE = new Terminal(157, "t recursive");
    public static final Terminal T_OCON = new Terminal(158, "t ocon");
    public static final Terminal T_ENDBLOCK = new Terminal(159, "t endblock");
    public static final Terminal T_ACCESSEQ = new Terminal(160, "t accesseq");
    public static final Terminal T_SLASHEQ = new Terminal(161, "t slasheq");
    public static final Terminal T_COMPLEX = new Terminal(162, "t complex");
    public static final Terminal T_ONLY = new Terminal(163, "t only");
    public static final Terminal T_PROCEDURE = new Terminal(164, "t procedure");
    public static final Terminal T_INTRINSIC = new Terminal(165, "t intrinsic");
    public static final Terminal T_KINDEQ = new Terminal(166, "t kindeq");
    public static final Terminal T_FORMATTEDEQ = new Terminal(167, "t formattedeq");
    public static final Terminal T_SEQUENTIALEQ = new Terminal(168, "t sequentialeq");
    public static final Terminal T_ELSEWHERE = new Terminal(169, "t elsewhere");
    public static final Terminal T_DOUBLEPRECISION = new Terminal(170, "t doubleprecision");
    public static final Terminal T_PROGRAM = new Terminal(171, "t program");
    public static final Terminal T_SAVE = new Terminal(172, "t save");
    public static final Terminal T_DO = new Terminal(173, "t do");
    public static final Terminal T_FILEEQ = new Terminal(174, "t fileeq");
    public static final Terminal T_WHILE = new Terminal(175, "t while");
    public static final Terminal T_ACTIONEQ = new Terminal(176, "t actioneq");
    public static final Terminal T_LPARENSLASH = new Terminal(177, "t lparenslash");
    public static final Terminal T_INTERFACE = new Terminal(178, "t interface");
    public static final Terminal T_READEQ = new Terminal(179, "t readeq");
    public static final Terminal T_GREATERTHANEQ = new Terminal(180, "t greaterthaneq");
    public static final Terminal T_NEQV = new Terminal(181, "t neqv");
    public static final Terminal T_NULL = new Terminal(182, "t null");
    public static final Terminal T_LPAREN = new Terminal(183, "t lparen");
    public static final Terminal T_NMLEQ = new Terminal(184, "t nmleq");
    public static final Terminal T_GT = new Terminal(185, "t gt");
    public static final Terminal T_GREATERTHAN = new Terminal(186, "t greaterthan");
    public static final Terminal T_AND = new Terminal(187, "t and");
    public static final Terminal T_HCON = new Terminal(188, "t hcon");
    public static final Terminal T_EXIT = new Terminal(189, "t exit");

    private int index;
    private String description;

    public Terminal(int index, String description)
    {
        this.index = index;
        this.description = description;
    }
    
    public int getIndex()
    {
        return index;
    }

    public String toString()
    {
        return description;
    }
}
