#include <jni.h>
#include <stdio.h>
#include <SpawnerInputStream.h>
#include <SpawnerOutputStream.h>
#include <unistd.h>

/* Header for class _org_eclipse_fdt_utils_spawner_SpawnerInputStream */
/* Header for class _org_eclipse_fdt_utils_spawner_SpawnerOutputStream */

/*
 * Class:     org_eclipse_fdt_utils_spawner_SpawnerInputStream
 * Method:    read0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_fdt_utils_spawner_SpawnerInputStream_read0(JNIEnv * env,
                                                          jobject jobj,
                                                          jint jfd,
                                                          jbyteArray buf,
                                                          jint buf_len)
{
    int fd;
    int status;
    jbyte *data;
    int data_len;

    data = (*env)->GetByteArrayElements(env, buf, 0);
    data_len = buf_len;
    fd = jfd;

    status = read( fd, data, data_len );
    (*env)->ReleaseByteArrayElements(env, buf, data, 0);

    if (status == 0) {
        /* EOF. */
        status = -1;
    } else if (status == -1) {
        /* Error, toss an exception */
        jclass exception = (*env)->FindClass(env, "java/io/IOException");
        if (exception == NULL) {
            /* Give up.  */
            return -1;
        }
        (*env)->ThrowNew(env, exception, "read error");
    }

    return status;
}


/*
 * Class:     org_eclipse_fdt_utils_spawner_SpawnerInputStream
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_fdt_utils_spawner_SpawnerInputStream_close0(JNIEnv * env,
                                                           jobject jobj,
                                                           jint fd)
{
    return close(fd);
}

/*
 * Class:     org_eclipse_fdt_utils_spawner_SpawnerOutputStream
 * Method:    write0
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_fdt_utils_spawner_SpawnerOutputStream_write0(JNIEnv * env,
                                                            jobject jobj,
                                                            jint jfd,
                                                            jbyteArray buf,
                                                            jint buf_len)
{
    int status;
    int fd;
    jbyte *data;
    int data_len;

    data = (*env)->GetByteArrayElements(env, buf, 0);
    data_len = buf_len;
    fd = jfd;

    status = write(fd, data, data_len);
    (*env)->ReleaseByteArrayElements(env, buf, data, 0);

    return status;
}


/*
 * Class:     org_eclipse_fdt_utils_spawner_SpawnerOutputStream
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_fdt_utils_spawner_SpawnerOutputStream_close0(JNIEnv * env,
                                                            jobject jobj,
                                                            jint fd)
{
    return close(fd);
}
