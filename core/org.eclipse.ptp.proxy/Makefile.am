noinst_LIBRARIES =			libproxyclnt.a libproxysvr.a

libproxyclnt_a_SOURCES =	src/proxy.c \
							src/proxy_attr.c \
							src/proxy_clnt.c \
							src/proxy_tcp.c \
							src/proxy_tcp_clnt.c \
							src/proxy_msg.c \
							src/compression.c \
							src/huffman_byte_encoder.c \
							src/huffman_coder.c
libproxyclnt_a_CFLAGS =		-I./include $(AM_CFLAGS) -g

libproxysvr_a_SOURCES =		src/proxy.c \
							src/proxy_attr.c \
							src/proxy_svr.c \
							src/proxy_stdio.c \
							src/proxy_stdio_svr.c \
							src/proxy_tcp.c \
							src/proxy_tcp_svr.c \
							src/proxy_msg.c \
							src/proxy_event.c \
							src/compression.c \
							src/huffman_byte_encoder.c \
							src/huffman_coder.c
libproxysvr_a_CFLAGS =		-I./include $(AM_CFLAGS) -g
