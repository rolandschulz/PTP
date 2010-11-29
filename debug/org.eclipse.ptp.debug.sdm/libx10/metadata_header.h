/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 
#ifndef _METADATA_HEADER_H_
#define _METADATA_HEADER_H_

#include <stdint.h>

/*
 * Structure of meta data header base
 */
typedef struct {
  uint16_t _structureSize;
  uint8_t  _language;
  uint8_t  _version;
} MetaDataHeader;

/*
 * Metalanguage definitions
 */
typedef enum {
  MetaLanguage_X10 = 0
} MetaLanguage;

/*
 * Reserved name of the variable to contain the metadata
 */
static const char MetaDataHeaderName[] = "MetaDebugInfo";

#endif /* _METADATA_HEADER_H_ */

