/*
 * Boolean.cpp
 *
 *  Created on: 28 déc. 2016
 *      Author: FrancisANDRE
 */

#include "Boolean.h"
#include "SPLParserConstants.h"

Boolean::Boolean(bool value) : SimpleNode(BOOL), boolean(value){
}

Boolean::~Boolean() {
}

Boolean* Boolean::operator||(const Boolean& value) {
	return new Boolean(boolean || value.boolean);
}
Boolean* Boolean::operator&&(const Boolean& value) {
	return new Boolean(boolean && value.boolean);
}
