package org.eclipse.photran.internal.core.lexer;

public final class LexerOptions
{
    // TODO: Make Java 5 enum after we migrate
    
    private LexerOptions() {;}
    
    public static final int NONE = 0;
    public static final int AUTO_DETECT_SOURCE_FORM = 0;
    public static final int FREE_FORM = 1;
    public static final int FIXED_FORM = 2;
    public static final int ASSOCIATE_LINE_COL = 4;
    public static final int ASSOCIATE_OFFSET_LENGTH = 8;
}
