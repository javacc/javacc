/*
 * Interpret.h
 *
 *  Created on: 24 janv. 2017
 *      Author: FrancisANDRE
 */

#ifndef JJTREE_VST_VST_INTERPRET_H_
#define JJTREE_VST_VST_INTERPRET_H_

#include <iostream>
#include <string>
#include <stack>
#include <map>
using std::string;
using std::stack;
using std::map;

#include "SPLParserVisitor.h"
#include "JavaCC.h"

class Interpret : public SPLParserVisitor {
public:
	Interpret();
	~Interpret();

	void visit(const SimpleNode *node, void* data);
	void visit(const ASTCompilationUnit *node, void* data);
	void visit(const ASTVarDeclaration *node, void* data);
	void visit(const ASTAssignment *node, void* data);
	void visit(const ASTOrNode *node, void* data);
	void visit(const ASTAndNode *node, void* data);
	void visit(const ASTBitwiseOrNode *node, void* data);
	void visit(const ASTBitwiseXorNode *node, void* data);
	void visit(const ASTBitwiseAndNode *node, void* data);
	void visit(const ASTEQNode *node, void* data);
	void visit(const ASTNENode *node, void* data);
	void visit(const ASTLTNode *node, void* data);
	void visit(const ASTGTNode *node, void* data);
	void visit(const ASTLENode *node, void* data);
	void visit(const ASTGENode *node, void* data);
	void visit(const ASTAddNode *node, void* data);
	void visit(const ASTSubtractNode *node, void* data);
	void visit(const ASTMulNode *node, void* data);
	void visit(const ASTDivNode *node, void* data);
	void visit(const ASTModNode *node, void* data);
	void visit(const ASTBitwiseComplNode *node, void* data);
	void visit(const ASTNotNode *node, void* data);
	void visit(const ASTId *node, void* data);
	void visit(const ASTIntConstNode *node, void* data);
	void visit(const ASTTrueNode *node, void* data);
	void visit(const ASTFalseNode *node, void* data);
	void visit(const ASTReadStatement *node, void* data);
	void visit(const ASTWriteStatement *node, void* data);

private:
	map<string, Node*>	symtab;
	stack<Node*>		nodestack;
};

#endif
