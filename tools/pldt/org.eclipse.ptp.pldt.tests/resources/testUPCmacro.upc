// note: this is ridiculous code, not expected to run, just testing macro etc. recognition.
#include <upc.h>
#define FOO upc_lock(0)

/* for any error messages returned by LAPI */
char err_msg_buf[MPI_MAX_ERR_STRING];

#define CHECK(func_and_args)                                      \
{                                                                 \
    int rc;                                                       \
    if ((rc = (func_and_args)) != 0) {                            \
        upc_string(rc, err_msg_buf);                         \
        fprintf(stderr,                                           \
                "UPC returns error message: %s, rc = %d\n",      \
                 err_msg_buf, rc);                                \
        exit(1);                                                  \
    }                                                             \
                                                                  \
}



void do_accumulate(foo_handle_t *handle, void *param)
{
	upc_addrfield(null);
	FOO;

    CHECK((upc_something(*handle, buddy,
                       (void *)(hdr_hdl_list[buddy]), &uhdr,
                        sizeof(uhdr_t), &(data_buffer[0]),
                        len*(sizeof(data_buffer[0])),
                        NULL, NULL, NULL)));

}
 
