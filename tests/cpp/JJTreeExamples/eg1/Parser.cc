/* Parser.cc */
#include "Parser.h"
#include "TokenMgrError.h"
#include "ParserTree.h"
namespace EG1 {
  unsigned int jj_la1_0[] = {
0x18000,0x18000,0xe0000,0xe0000,0x100880,};

  /** Constructor with user supplied TokenManager. */



SimpleNode* Parser::Start() {/*@bgen(jjtree) Start */
  SimpleNode *jjtn000 = new SimpleNode(JJTSTART);
  bool jjtc000 = true;
  jjtree.openNodeScope(jjtn000);


                                                                 
/*@egen*/

    try {
      Expression();
      jj_consume_token(14);

    jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;

                    
                     
  
 return jjtn000;                 
    } catch (...) {
        if (jjtc000) {
          jjtree.clearNodeScope(jjtn000);
          jjtc000 = false;
        } else {
          jjtree.popNode();
        }
    
    
    
    
    
         
          
      
    }

    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }


     
      
  
}


void Parser::Expression() {/*@bgen(jjtree) Expression */
  SimpleNode *jjtn000 = new SimpleNode(JJTEXPRESSION);
  bool jjtc000 = true;
  jjtree.openNodeScope(jjtn000);


                                                                 
/*@egen*/

    try {
      AdditiveExpression();
    } catch (...) {
        if (jjtc000) {
          jjtree.clearNodeScope(jjtn000);
          jjtc000 = false;
        } else {
          jjtree.popNode();
        }
    
    
    
    
    
         
          
      
    }

    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }


     
      
  
}


void Parser::AdditiveExpression() {/*@bgen(jjtree) AdditiveExpression */
  SimpleNode *jjtn000 = new SimpleNode(JJTADDITIVEEXPRESSION);
  bool jjtc000 = true;
  jjtree.openNodeScope(jjtn000);


                                                                 
/*@egen*/

    try {
      MultiplicativeExpression();
      while (!hasError) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 15:
        case 16:{
          ;
          break;
          }
        default:
          jj_la1[0] = jj_gen;
          goto end_label_1;
        }
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 15:{
          jj_consume_token(15);
          break;
          }
        case 16:{
          jj_consume_token(16);
          break;
          }
        default:
          jj_la1[1] = jj_gen;
          jj_consume_token(-1);
          errorHandler->handleParseError(token, getToken(1), __FUNCTION__, this), hasError = true;
        }
        MultiplicativeExpression();
      }
      end_label_1: ;
    } catch (...) {
        if (jjtc000) {
          jjtree.clearNodeScope(jjtn000);
          jjtc000 = false;
        } else {
          jjtree.popNode();
        }
    
    
    
    
    
         
          
      
    }

    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }


     
      
  
}


void Parser::MultiplicativeExpression() {/*@bgen(jjtree) MultiplicativeExpression */
  SimpleNode *jjtn000 = new SimpleNode(JJTMULTIPLICATIVEEXPRESSION);
  bool jjtc000 = true;
  jjtree.openNodeScope(jjtn000);


                                                                 
/*@egen*/

    try {
      UnaryExpression();
      while (!hasError) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 17:
        case 18:
        case 19:{
          ;
          break;
          }
        default:
          jj_la1[2] = jj_gen;
          goto end_label_2;
        }
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 17:{
          jj_consume_token(17);
          break;
          }
        case 18:{
          jj_consume_token(18);
          break;
          }
        case 19:{
          jj_consume_token(19);
          break;
          }
        default:
          jj_la1[3] = jj_gen;
          jj_consume_token(-1);
          errorHandler->handleParseError(token, getToken(1), __FUNCTION__, this), hasError = true;
        }
        UnaryExpression();
      }
      end_label_2: ;
    } catch (...) {
        if (jjtc000) {
          jjtree.clearNodeScope(jjtn000);
          jjtc000 = false;
        } else {
          jjtree.popNode();
        }
    
    
    
    
    
         
          
      
    }

    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }


     
      
  
}


void Parser::UnaryExpression() {/*@bgen(jjtree) UnaryExpression */
  SimpleNode *jjtn000 = new SimpleNode(JJTUNARYEXPRESSION);
  bool jjtc000 = true;
  jjtree.openNodeScope(jjtn000);


                                                                 
/*@egen*/

    try {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 20:{
        jj_consume_token(20);
        Expression();
        jj_consume_token(21);
        break;
        }
      case IDENTIFIER:{
        Identifier();
        break;
        }
      case INTEGER_LITERAL:{
        Integer();
        break;
        }
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        errorHandler->handleParseError(token, getToken(1), __FUNCTION__, this), hasError = true;
      }
    } catch (...) {
        if (jjtc000) {
          jjtree.clearNodeScope(jjtn000);
          jjtc000 = false;
        } else {
          jjtree.popNode();
        }
    
    
    
    
    
         
          
      
    }

    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }


     
      
  
}


void Parser::Identifier() {/*@bgen(jjtree) Identifier */
  SimpleNode *jjtn000 = new SimpleNode(JJTIDENTIFIER);
  bool jjtc000 = true;
  jjtree.openNodeScope(jjtn000);


                                                                 
/*@egen*/

    try {
      jj_consume_token(IDENTIFIER);
    } catch (...) {
        if (jjtc000) {
          jjtree.clearNodeScope(jjtn000);
          jjtc000 = false;
        } else {
          jjtree.popNode();
        }
    
    
    
    
    
         
          
      
    }

    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }


     
      
  
}


void Parser::Integer() {/*@bgen(jjtree) Integer */
  SimpleNode *jjtn000 = new SimpleNode(JJTINTEGER);
  bool jjtc000 = true;
  jjtree.openNodeScope(jjtn000);


                                                                 
/*@egen*/

    try {
      jj_consume_token(INTEGER_LITERAL);
    } catch (...) {
        if (jjtc000) {
          jjtree.clearNodeScope(jjtn000);
          jjtc000 = false;
        } else {
          jjtree.popNode();
        }
    
    
    
    
    
         
          
      
    }

    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }


     
      
  
}


  Parser::Parser(TokenManager *tokenManager){
    head = nullptr;
    ReInit(tokenManager);
}
Parser::~Parser()
{
  clear();
}

void Parser::ReInit(TokenManager* tokenManager){
    clear();
    errorHandler = new ErrorHandler();
    delete_eh = true;
    hasError = false;
    token_source = tokenManager;
    head = token = new Token();
    token->kind = 0;
    token->next = nullptr;
    jj_lookingAhead = false;
    jj_rescan = false;
    jj_done = false;
    jj_scanpos = jj_lastpos = nullptr;
    jj_gc = 0;
    jj_kind = -1;
    indent = 0;
    trace = false;
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }


void Parser::clear(){
  //Since token manager was generate from outside,
  //parser should not take care of deleting
  //if (token_source) delete token_source;
  if (delete_tokens && head) {
    Token *next, *t = head;
    while (t) {
      next = t->next;
      delete t;
      t = next;
    }
  }
  if (delete_eh) {
    delete errorHandler, errorHandler = nullptr;
    delete_eh = false;
  }
}


Token * Parser::jj_consume_token(int kind)  {
    Token *oldToken;
    if ((oldToken = token)->next != nullptr) token = token->next;
    else token = token->next = token_source->getNextToken();
    jj_ntk = -1;
    if (token->kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    JJString image = kind >= 0 ? tokenImage[kind] : tokenImage[0];
    errorHandler->handleUnexpectedToken(kind, image.substr(1, image.size() - 2), getToken(1), this);
    hasError = true;
    return token;
  }


/** Get the next Token. */

Token * Parser::getNextToken(){
    if (token->next != nullptr) token = token->next;
    else token = token->next = token_source->getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */

Token * Parser::getToken(int index){
    Token *t = token;
    for (int i = 0; i < index; i++) {
      if (t->next != nullptr) t = t->next;
      else t = t->next = token_source->getNextToken();
    }
    return t;
  }


int Parser::jj_ntk_f(){
    if ((jj_nt=token->next) == nullptr)
      return (jj_ntk = (token->next=token_source->getNextToken())->kind);
    else
      return (jj_ntk = jj_nt->kind);
  }


 void  Parser::parseError()   {
      std::cerr << "Parse error at : " << token->beginLine << ":" << token->beginColumn << " after token: " << addUnicodeEscapes(token->image) << " encountered: " << addUnicodeEscapes(getToken(1)->image) << std::endl;
   }


  bool Parser::trace_enabled()  {
    return trace;
  }


  void Parser::enable_tracing()  {
  }

  void Parser::disable_tracing()  {
  }


}
