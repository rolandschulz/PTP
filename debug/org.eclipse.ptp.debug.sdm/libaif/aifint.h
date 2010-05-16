/*
 * Header file for internal AIF utility routines.
 *
 * Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

#ifndef _AIFINT_H
#define _AIFINT_H

#define MAX_VALUES_SEEN 1000
#define MAX_TYPES_SEEN 100

extern char *	_aif_values_seen[];
extern char *	_aif_types_seen[];

extern void		_aif_add_int(char *, char *, int, char *, int);
extern void *	_aif_alloc(int);
extern void		_aif_and_bool(char *, char *, int, char *, int);
extern void		_aif_and_int(char *, char *, int, char *, int);
extern AIF *	_aif_array_ref(AIF *, int, int *, int *, int *, char *, int);
extern int 		_aif_array_slice_post(char **, char **, char **, char **);
extern int 		_aif_array_slice_pre(char **, char **, char **);
extern int		_aif_array_to_str(int, char **, char **);
extern int		_aif_binary_op(aifop, char **, char **, char **, char **, char **, char **);
extern int		_aif_binary_op_bool(aifop, char **, char *, int, char *, int);
extern int		_aif_binary_op_float(aifop, char **, char *, int, char *, int);
extern int		_aif_binary_op_int(aifop, char **, char *, int, char *, int);
extern int		_aif_bool_to_str(int, char **, char **);
extern int		_aif_char_to_str(int, char **, char **);
extern int		_aif_compare(int, int *, char **, char **, char **, char **);
extern int		_aif_cmp_int(int *, char *, int, char *, int);
extern int		_aif_cmp_float(int *, char *, int, char *, int);
extern int		_aif_diff(int, char **, char **, char **, char **, char **, char **);
extern void		_aif_div_int(char *, char *, char *, int, char *, int);
extern void		_aif_dump_int(char **, char **, FILE *);
extern void		_aif_dump_float(char **, char **, FILE *);
extern void		_aif_dump(char **, char **, FILE *);
extern void		_aif_dump_array(char **, char **, FILE *);
extern int		_aif_enum_to_str(int, char **, char **);
extern int		_aif_eps(char *, char *, int, char *, char *, int, char *, char *, int, int *);
extern void		_aif_free(void *);
extern int		_aif_float_to_aif_int(char **, char *, int);
extern int		_aif_float_to_str(int, char **, char **);
extern int		_aif_function_to_str(int, char **, char **);
extern int		_aif_int_is_zero(char *, int);
extern int		_aif_int_to_aif_float(char **, char *, int);
extern int		_aif_int_to_double(char *, int, double *);
extern int		_aif_int_to_doublest(char *, int, AIFDOUBLEST *);
extern int		_aif_int_to_str(int, char **, char **);
extern int		_aif_is_zero(char **, char **, int, int *);
extern void		_aif_mul_int(char *, char *, int, char *, int);
extern int		_aif_name_to_str(int, char **, char **);
extern void		_aif_neg_int(char *, char *, int);
extern void		_aif_not_bool(char *, char *, int);
extern void		_aif_not_int(char *, char *, int);
extern void		_aif_or_bool(char *, char *, int, char *, int);
extern void		_aif_or_int(char *, char *, int, char *, int);
extern int		_aif_pointer_to_str(int, char **, char **);
extern int		_aif_print(FILE *, int, char **, char **);
extern int		_aif_print_array(FILE *, int, char **, char **);
extern int		_aif_print_bool(FILE *, int, char **, char **);
extern int		_aif_print_char(FILE *, int, char **, char **);
extern int		_aif_print_enum(FILE *, int, char **, char **);
extern int		_aif_print_float(FILE *, int, char **, char **);
extern int		_aif_print_function(FILE *, int, char **, char **);
extern int		_aif_print_int(FILE *, int, char **, char **);
extern int		_aif_print_name(FILE *, int, char **, char **);
extern int		_aif_print_pointer(FILE *, int, char **, char **);
extern int		_aif_print_reference(FILE *, int, char **, char **);
extern int		_aif_print_region(FILE *, int, char **, char **);
extern int		_aif_print_string(FILE *, int, char **, char **);
extern int		_aif_print_aggregate(FILE *, int, char **, char **);
extern int		_aif_print_union(FILE *, int, char **, char **);
extern int		_aif_print_void(FILE *, int, char **, char **);
extern int		_aif_reference_to_str(int, char **, char **);
extern int		_aif_region_to_str(int, char **, char **);
extern void *	_aif_resize(void *, int);
extern int		_aif_string_to_str(int, char **, char **);
extern int		_aif_aggregate_to_str(int, char **, char **);
extern void		_aif_sub_int(char *, char *, int, char *, int);
extern int		_aif_to_char(char *, char *);
extern int		_aif_to_longest(char *, int, AIFLONGEST *);
extern int		_aif_to_doublest(char *, int, AIFDOUBLEST *);
extern int		_aif_to_str(int, char **, char **);
extern int		_aif_unary_op(aifop, char **, char **, char **, char **);
extern int		_aif_unary_op_bool(aifop, char **, char *, int);
extern int		_aif_unary_op_float(aifop, char **, char *, int);
extern int		_aif_unary_op_int(aifop, char **, char *, int);
extern int		_aif_union_to_str(int, char **, char **);
extern int		_aif_void_to_str(int, char **, char **);
extern int		_char_to_aif(char **, char);
extern int		_doublest_to_aif(char **, int, AIFDOUBLEST);
extern int 		_data_len_index(char *, int);
extern int 		_data_len_public(char *);
extern char * 	_fds_add_function_arg(char *, char *);
extern char *	_fds_base_type(char *);
extern int		_fds_count_bytes(char **);
extern int		_fds_count_bytes_na(char **);
extern void		_fds_advance(char **);
extern int		_fds_array_min_index(char *);
extern int		_fds_array_size(char *);
extern int		_fds_getnum(char *);
extern char *	_fds_lookup(char **);
extern void		_fds_resolve(char **);
extern void		_fds_skip_data(char **, char **);
extern char *	_fds_skiptomatch(char *);
extern char * 	_fds_skiptofield(char *fds, int n);
extern char *	_fds_skipto(char *, char *);
extern char *	_fds_skipnum(char *);
extern void  	_fds_skipid(char **);
extern int		_fds_aggregate_arrange(char *, char *, char *, char**, char**);
extern char *	_field_attribute(char *, char *, char *);
extern char *	_find_target(char **, char **, int);
extern int		_get_pointer_type(char *);
extern int		_get_pointer_name(char *);
extern int		_longest_to_aif(char **, int, AIFLONGEST);
extern void		_str_init(void);
extern void		_str_cat(char *);
extern void		_str_add(char);
extern char *	_str_get(void);
extern void		_ptrname_to_int(char **, int *);
extern void		_int_to_ptrname(int, char *);

#endif /* _AIFINT_H */



