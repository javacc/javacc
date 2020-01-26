/*
 * Interpret.cpp
 *
 *  Created on: 24 janv. 2017
 *      Author: FrancisANDRE
 */

#include <iostream>
#include <memory>
#include <stdexcept>
using namespace std;

#include "Interpret.h"
#include "Variable.h"
#include "Boolean.h"
#include "Integer.h"
#include "SPLParserConstants.h"

Interpret::Interpret() {
}

Interpret::~Interpret() {
	for(const auto& kv: symtab) {
		delete kv.second;
	}
}

void Interpret::visit(const SimpleNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
}
void Interpret::visit(const ASTCompilationUnit *node, void* data) {
	node->jjtChildrenAccept(this, data);
}
void Interpret::visit(const ASTVarDeclaration *node, void* data) {
	node->jjtChildrenAccept(this, data);
	{
		if (node->type == BOOL)
			symtab[node->name] = new Boolean(false);
		else
			symtab[node->name] = new Integer(0);
	}

}
void Interpret::visit(const ASTAssignment *node, void* data) {
	node->jjtChildrenAccept(this, data);
	string name;

	Node* value = nodestack.top(); nodestack.pop();
	unique_ptr<Node>  top(nodestack.top()); nodestack.pop();
	name = ((ASTId*)node->jjtGetChild(0))->name;
	symtab[name] = value;
}
void Interpret::visit(const ASTOrNode *node, void* data) {
	node->jjtChildAccept(0, this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		const Boolean* boolean = (Boolean*)top;
		if (*boolean) {
			nodestack.push(new Boolean(true));
			return;
		}
	}

	node->jjtChildAccept(1, this, data);
	unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
	unique_ptr<Boolean> right((Boolean*)nodestack.top()); nodestack.pop();
	nodestack.push(*left || *right);
}
void Interpret::visit(const ASTAndNode *node, void* data) {
	node->jjtChildAccept(0, this, data);
	const Node* top = nodestack.top();

	if (typeid(*top) == typeid(Boolean)) {
		const Boolean* boolean = (Boolean*)top;
		if (!*boolean) {
			nodestack.push(new Boolean(false));
			return;
		}
	}
	node->jjtChildAccept(1, this, data);

	unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
	unique_ptr<Boolean> right((Boolean*)nodestack.top()); nodestack.pop();
	nodestack.push(new Boolean(*left || *right));
}
void Interpret::visit(const ASTBitwiseComplNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	unique_ptr<Integer> top((Integer*)nodestack.top()); nodestack.pop();
	nodestack.push(new Integer(~(*top)));
}
void Interpret::visit(const ASTBitwiseOrNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top());nodestack.pop();
		unique_ptr<Boolean> righ((Boolean*)nodestack.top());nodestack.pop();
		nodestack.push(new Boolean(*left | *righ));
	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top());nodestack.pop();
		unique_ptr<Integer> righ((Integer*)nodestack.top());nodestack.pop();
		nodestack.push(new Integer(*left | *righ));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTBitwiseXorNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
		unique_ptr<Boolean> righ((Boolean*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left ^ *righ));
	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
		unique_ptr<Integer> righ((Integer*)nodestack.top()); nodestack.pop();
		nodestack.push(new Integer(*left ^ *righ));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTBitwiseAndNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
		unique_ptr<Boolean> righ((Boolean*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left & *righ));
	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
		unique_ptr<Integer> righ((Integer*)nodestack.top()); nodestack.pop();
		nodestack.push(new Integer(*left & *righ));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTEQNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
		unique_ptr<Boolean> right((Boolean*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left == *right));

	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
		unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left == *right));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTNENode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
		unique_ptr<Boolean> right((Boolean*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left != *right));

	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
		unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left != *right));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTLTNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
		unique_ptr<Boolean> right((Boolean*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left < *right));

	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
		unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left < *right));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTGTNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
		unique_ptr<Boolean> right((Boolean*)nodestack.top());nodestack.pop();
		nodestack.push(new Boolean(*left > *right));

	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
		unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left > *right));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTLENode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
		unique_ptr<Boolean> right((Boolean*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left <= *right));

	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
		unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left <= *right));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTGENode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	const Node* top = nodestack.top();
	if (typeid(*top) == typeid(Boolean)) {
		unique_ptr<Boolean> left((Boolean*)nodestack.top()); nodestack.pop();
		unique_ptr<Boolean> right((Boolean*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left >= *right));

	} else if (typeid(*top) == typeid(Integer)) {
		unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
		unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
		nodestack.push(new Boolean(*left >= *right));
	} else
		throw runtime_error("Invalid node on top of stack");
}
void Interpret::visit(const ASTAddNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
	unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
	nodestack.push(*left + *right);
}
void Interpret::visit(const ASTSubtractNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
	unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
	nodestack.push(*left - *right);
}
void Interpret::visit(const ASTMulNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
	unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
	nodestack.push(*left * *right);
}
void Interpret::visit(const ASTDivNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
	unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
	nodestack.push(*left / *right);
}
void Interpret::visit(const ASTModNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	unique_ptr<Integer> left((Integer*)nodestack.top()); nodestack.pop();
	unique_ptr<Integer> right((Integer*)nodestack.top()); nodestack.pop();
	nodestack.push(*left % *right);
}
void Interpret::visit(const ASTNotNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	unique_ptr<Boolean> top((Boolean*)nodestack.top()); nodestack.pop();
	nodestack.push(new Boolean(!*top));
}
void Interpret::visit(const ASTId *node, void* data) {
	node->jjtChildrenAccept(this, data);
	Node* value = symtab[node->name];
	nodestack.push(value);
}
void Interpret::visit(const ASTIntConstNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	nodestack.push(new Integer(node->val));
}
void Interpret::visit(const ASTTrueNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	nodestack.push(new Boolean(true));
}
void Interpret::visit(const ASTFalseNode *node, void* data) {
	node->jjtChildrenAccept(this, data);
	nodestack.push(new Boolean(false));
}
void Interpret::visit(const ASTReadStatement *node, void* data) {
	Integer* integer = new Integer();
	cin >> *integer;
	unique_ptr<Node> value(symtab[node->name]);
	symtab[node->name] = integer;
}
void Interpret::visit(const ASTWriteStatement *node, void* data) {
	const Node* value = symtab[node->name];
	if (value == nullptr) {
		cerr << "value is null" << endl;
		return;
	}
	if (typeid(*value) == typeid(Integer)) {
		const Integer& integer = *static_cast<const Integer*>(value);
		cout << integer << endl;
	}
}
