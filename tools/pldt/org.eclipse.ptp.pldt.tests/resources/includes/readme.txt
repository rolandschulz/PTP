this test suite assumes various header files are available in order to run the analysis tests.

To run the tests you will need to put header files in this directory.

For example mpi.h,  such as from openmpi.

The header files you need are indicated near the top of each testcase function
with a method call to import the required header file.
For example, in Test_MPI.java, the testMPIartifacts() method has this near the top:

     IFile mpiInclude = importFile("resources/includes","mpi.h");
     
     
     Beth Tibbitts
     April 2010
     