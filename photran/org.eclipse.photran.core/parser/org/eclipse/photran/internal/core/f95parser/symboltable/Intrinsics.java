package org.eclipse.photran.internal.core.f95parser.symboltable;

import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.IntrinsicEntry;

/**
 * Fills a <code>SymbolTable</code> with the names of Fortran intrinsic
 * functions, etc.
 * 
 * @author joverbey
 */
final class Intrinsics
{
    /**
     * Fills the specified <code>SymbolTable</code> with a list of all
     * the Fortran 95 intrinsics.
     * @param t
     */
    static void fill(SymbolTable t)
    {
        new Intrinsics(t);
    }

    private SymbolTable t;
    
    private Intrinsics(SymbolTable t)
    {
        this.t = t;

        // From Metcalf and Reid, "Fortran 90/95 Explained", Chapter 8
        
        // Functions
        add("associated");
        add("present");
        add("kind");
        add("abs");
        add("aimag");
        add("aint");
        add("anint");
        add("ceiling");
        add("cmplx");
        add("floor");
        add("int");
        add("nint");
        add("real");
        add("conjg");
        add("dim");
        add("max");
        add("min");
        add("mod");
        add("modulo");
        add("sign");
        add("acos");
        add("asin");
        add("atan");
        add("atan2");
        add("cos");
        add("cosh");
        add("exp");
        add("log");
        add("log10");
        add("sin");
        add("sinh");
        add("sqrt");
        add("tan");
        add("tanh");
        add("achar");
        add("char");
        add("iachar");
        add("ichar");
        add("lge");
        add("lgt");
        add("lle");
        add("llt");
        add("adjustl");
        add("adjustr");
        add("index");
        add("len_trim");
        add("scan");
        add("verify");
        add("logical");
        add("len");
        add("repeat");
        add("trim");
        add("digits");
        add("epsilon");
        add("huge");
        add("maxexponent");
        add("minexponent");
        add("precision");
        add("radix");
        add("range");
        add("tiny");
        add("exponent");
        add("fraction");
        add("nearest");
        add("rrspacing");
        add("scale");
        add("set_exponent");
        add("spacing");
        add("selected_int_kind");
        add("selected_real_kind");
        add("bit_size");
        add("btest");
        add("iand");
        add("ibclr");
        add("ibits");
        add("ibset");
        add("ieor");
        add("ior");
        add("ishft");
        add("ishftc");
        add("not");
        add("dot_product");
        add("matmul");
        add("all");
        add("any");
        add("count");
        add("maxval");
        add("minval");
        add("product");
        add("sum");
        add("allocated");
        add("lbound");
        add("shape");
        add("size");
        add("ubound");
        add("merge");
        add("pack");
        add("unpack");
        add("reshape");
        add("spread");
        add("cshift");
        add("eoshift");
        add("transpose");
        add("maxloc");
        add("minloc");
        add("null");
    
        // Subroutines
        add("mvbits");
        add("date_and_time");
        add("system_clock");
        add("cpu_time");
        add("random_number");
        add("random_seed");
    }
    
    private void add(String name)
    {
        Token token = new Token();
        token.setText(name);
        t.addEntry(new IntrinsicEntry(t, token, null));
    }
}
