/*
 * Variable.h
 *
 *  Created on: 28 déc. 2016
 *      Author: FrancisANDRE
 */

#pragma once

#include <string>
using std::string;

#include "Node.h"


class Variable : public Node {
public:
	Variable();
	virtual ~Variable();


private:
	int type;
	string name;
};


