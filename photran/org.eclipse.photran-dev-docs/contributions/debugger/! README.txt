In Spring, 2007, a Senior Projects group hacked CDT's debugger to solve some problems debugging
Fortran code.  Many of these were due to eccentricities in the g95 compiler.  The project was
never distributed publicly because the hacked debugger did not integrate with the newer versions of
Eclipse and CDT released in June, 2007.  Kurt Hendle reviewed the Senior Projects work in Summer,
2009; most of the "problems" fixed by the Senior Projects group had since been fixed in CDT proper,
or we decided explicitly not to fix them (e.g., filtering U variables, because that is a problem
with the g95 compiler, not Photran).

--Jeff Overbey (12 Nov 2009)