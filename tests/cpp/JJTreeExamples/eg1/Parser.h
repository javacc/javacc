#pragma once
#include "JavaCC.h"
#include "CharStream.h"
#include "Token.h"
#include "TokenManager.h"
#include "ParserConstants.h"
#include "JJTParserState.h"
#include "ErrorHandler.h"
#include "ParserTree.h"
namespace EG1 {
  struct JJCalls {
    int        gen;
    int        arg;
    JJCalls*   next;
    Token*     first;
    ~JJCalls() { if (next) delete next; }
     JJCalls() { next = nullptr; arg = 0; gen = -1; first = nullptr; }
  };

class Parser {
public:
SimpleNode* Start();
void Expression();
void AdditiveExpression();
void MultiplicativeExpression();
void UnaryExpression();
void Identifier();
void Integer();

public: 
  void setErrorHandler(ErrorHandler *eh) {
    if (delete_eh) delete errorHandler;
    errorHandler = eh;
    delete_eh = false;
  }

  TokenManager *token_source = nullptr;
  CharStream   *jj_input_stream = nullptr;
  /** Current token. */
  Token        *token = nullptr;
  /** Next token. */
  Token        *jj_nt = nullptr;

private: 
  int           jj_ntk;
  JJCalls       jj_2_rtns[1];
  bool          jj_rescan;
  int           jj_gc;
  Token        *jj_scanpos, *jj_lastpos;
  int           jj_la;
  /** Whether we are looking ahead. */
  bool          jj_lookingAhead;
  bool          jj_semLA;
  int           jj_gen;
  int           jj_la1[6];
  ErrorHandler *errorHandler = nullptr;

protected: 
  bool          delete_eh     = false;
  bool          delete_tokens = true;
  bool          hasError;

  Token        *head;

public: 
  Parser(TokenManager *tokenManager);
  virtual ~Parser();
void ReInit(TokenManager* tokenManager);
void clear();
Token * jj_consume_token(int kind);
Token * getNextToken();
Token * getToken(int index);
int jj_ntk_f();
private:
  int jj_kind;
  int **jj_expentries;
  int *jj_expentry;

protected:
  /** Generate ParseException. */
  virtual void  parseError();
private:
  int  indent; // trace indentation
  bool trace = false; // trace enabled if true

public:
  bool trace_enabled();
  void enable_tracing();
  void disable_tracing();


  JJTParserState jjtree;
private:
  bool jj_done;
};
}
