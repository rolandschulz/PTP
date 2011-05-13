// test recognition of artifacts within preprocessor
// Note that this is ridiculous code, only used for testing
#include <mpi.h>

/* for any error messages returned by LAPI */
char err_msg_buf[MPI_MAX_ERR_STRING];
#define FOO MPI_Init(&argc, &argv)

#define CHECK(func_and_args)                                      \
{                                                                 \
    int rc;                                                       \
    if ((rc = (func_and_args)) != MPI_SUCCESS) {                 \
        MPI_Address(rc, 0);                                      \
        fprintf(stderr,                                           \
                "MPI returns error message: %s, rc = %d\n",      \
                 err_msg_buf, rc);                                \
        exit(1);                                                  \
    }                                                             \
                                                                  \
}



void do_accumulate(foo_handle_t *handle, void *param)
{
	FOO;
	MPI_Send(null,0,null,0,0,0); // artifact not within preproc
    CHECK((MPI_Send(*handle, buddy,
                       (void *)(hdr_hdl_list[buddy]), &uhdr,
                        sizeof(uhdr_t), &(data_buffer[0]),
                        len*(sizeof(data_buffer[0])),
                        NULL, NULL, NULL)));

}
 
