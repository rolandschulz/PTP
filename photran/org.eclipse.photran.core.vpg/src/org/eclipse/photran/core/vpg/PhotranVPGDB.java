package org.eclipse.photran.core.vpg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;

import bz.over.vpg.db.caching.CachingDB;
import bz.over.vpg.db.cdt.CDTDB;

public class PhotranVPGDB extends CachingDB<IFortranAST, Token, PhotranTokenRef, PhotranVPGDB.PhotranCDTDB>
{
    public PhotranVPGDB()
    {
        super(new PhotranCDTDB(), 500, 2000);
    }
    
    static class PhotranCDTDB extends CDTDB<IFortranAST, Token, PhotranTokenRef>
    {
        public PhotranCDTDB()
        {
            this(PhotranVPG.inTestingMode() ? createTempFile() : Activator.getDefault().getStateLocation().addTrailingSeparator().toOSString() + "vpg");
        }
        
        private PhotranCDTDB(String filename)
        {
            super(filename);
            PhotranVPG.printDebug("Using Photran VPG database " + filename, "");
        }

        private static String createTempFile()
        {
            try
            {
                File f = File.createTempFile("vpg", null);
                f.deleteOnExit();
                return f.getAbsolutePath();
            }
            catch (IOException e)
            {
                throw new Error(e);
            }
        }
    
        @Override
        protected long getModificationStamp(String filename)
        {
            if (filename.startsWith("module:")) return Long.MIN_VALUE;
            
            IFile ifile = PhotranVPG.getIFileForFilename(filename);
            return ifile == null ? Integer.MIN_VALUE : ifile.getLocalTimeStamp();
        }
        
        @Override protected byte[] serialize(Serializable annotation) throws IOException
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ObjectOutputStream(out).writeObject(annotation);
            return out.toByteArray();
        }
        
        @Override protected Serializable deserialize(InputStream binaryStream) throws IOException, ClassNotFoundException
        {
            return (Serializable)new ObjectInputStream(binaryStream).readObject();
        }
    }
    
    public String describeEdgeType(int edgeType)
    {
        return super.describeEdgeType(edgeType);
    }

    public String describeAnnotationType(int annotationType)
    {
        return super.describeAnnotationType(annotationType);
    }

    public String describeToken(String filename, int offset, int length)
    {
        return super.describeToken(filename, offset, length);
    }

    public void clearDatabase()
    {
        super.db.clearDatabase();
    }
}
