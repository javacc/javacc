/*
 * DumpVisitor.cpp
 *
 *  Created on: 12 janv. 2017
 *      Author: FrancisANDRE
 */

#include<iostream>
#include "DumpVisitor.h"

using namespace std;

DumpVisitor::DumpVisitor() {
}

DumpVisitor::~DumpVisitor() {
}

void DumpVisitor::defaultVisit(const SimpleNode *node, void* data) {
	clog << node->toString() << endl;
	node->jjtChildrenAccept(this, data);
}
 void DumpVisitor::visit(const ASTIntConstNode *node, void* data) {
	 node->jjtChildrenAccept(this, data);
 }
 JJSimpleString DumpVisitor::indentString() const {
	return "    ";
 }
