/*
 * Boolean.h
 *
 *  Created on: 28 déc. 2016
 *      Author: FrancisANDRE
 */

#pragma once

#include "SimpleNode.h"

class Boolean : public SimpleNode {
public:
	Boolean(bool value = false);
	virtual ~Boolean();

	Boolean* operator||(const Boolean& value);
	Boolean* operator&&(const Boolean& value);
	operator bool() const { return boolean; }

private:
	bool boolean = false;
};


