/*
 * Integer.h
 *
 *  Created on: 28 déc. 2016
 *      Author: FrancisANDRE
 */

#pragma once
#include <iostream>
using std::istream;
using std::ostream;

#include "SimpleNode.h"

class Integer : public SimpleNode {
public:
	Integer(int value = 0);
	virtual ~Integer();

	Integer* operator+(const Integer& value) const;
	Integer* operator-(const Integer& value) const;
	Integer* operator*(const Integer& value) const;
	Integer* operator/(const Integer& value) const;
	Integer* operator%(const Integer& value) const;

	bool operator<  (const Integer& value) const;
	bool operator<= (const Integer& value) const;
	bool operator== (const Integer& value) const;
	bool operator>= (const Integer& value) const;
	bool operator>  (const Integer& value) const;

	operator int() const { return integer; }

	friend ostream& operator<<(ostream& os, const Integer& integer);
	friend istream& operator>>(istream& is, Integer& integer);

private:
	int integer;
};


