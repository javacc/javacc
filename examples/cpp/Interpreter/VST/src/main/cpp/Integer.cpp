/*
 * Integer.cpp
 *
 *  Created on: 28 déc. 2016
 *      Author: FrancisANDRE
 */

#include "Integer.h"
#include "SPLParserConstants.h"

Integer::Integer(int value) : SimpleNode(INT), integer(value) {
}

Integer::~Integer() {
}

Integer* Integer::operator+(const Integer& value) const {
	return new Integer(integer + value.integer);
}
Integer* Integer::operator-(const Integer& value) const {
	return new Integer(integer - value.integer);
}
Integer* Integer::operator*(const Integer& value) const {
	return new Integer(integer * value.integer);
}
Integer* Integer::operator/(const Integer& value) const {
	return new Integer(integer / value.integer);
}
Integer* Integer::operator%(const Integer& value) const {
	return new Integer(integer % value.integer);
}
bool Integer::operator<(const Integer& value) const {
	return integer < value.integer;
}
bool Integer::operator<=(const Integer& value) const {
	return integer <= value.integer;
}
bool Integer::operator==(const Integer& value) const {
	return integer == value.integer;
}
bool Integer::operator>=(const Integer& value) const {
	return integer >= value.integer;
}
bool Integer::operator>(const Integer& value) const {
	return integer > value.integer;
}
ostream& operator<<(std::ostream& os, const Integer& obj) {
	os << obj.integer;
	return os;
}
istream& operator >> (std::istream& is, Integer& obj) {
	is >> obj.integer;

	if ( false/* T could not be constructed */)
		is.setstate(std::ios::failbit);
	return is;
}