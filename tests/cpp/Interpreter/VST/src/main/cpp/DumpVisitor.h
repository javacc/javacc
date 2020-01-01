/*
 * DumpVisitor.h
 *
 *  Created on: 12 janv. 2017
 *      Author: FrancisANDRE
 */

#ifndef JJTREE_VST_VST_DUMPVISITOR_H_
#define JJTREE_VST_VST_DUMPVISITOR_H_
#include "SPLParserVisitor.h"
#include "JavaCC.h"

class DumpVisitor : public SPLParserDefaultVisitor {
public:
	DumpVisitor();
	virtual ~DumpVisitor();
	virtual void defaultVisit(const SimpleNode *node, void* data);
	virtual void visit(const ASTIntConstNode *node, void* data);

private:
	int indent = 0;
	JJSimpleString indentString() const;
};

#endif
