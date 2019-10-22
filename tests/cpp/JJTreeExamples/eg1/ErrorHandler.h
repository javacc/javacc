#ifndef JAVACC__Parser_ERRORHANDLER_H_
#define JAVACC__Parser_ERRORHANDLER_H_
#include <string>
#include "JavaCC.h"
#include "Token.h"

namespace EG1 {

JAVACC_SIMPLE_STRING addUnicodeEscapes(const JAVACC_STRING_TYPE& str);

  class Parser;
  class ErrorHandler {
    friend class ParserTokenManager;
    friend class Parser;
    protected:
      int error_count;
    public:
      // Called when the parser encounters a different token when expecting to
      // consume a specific kind of token.
      // expectedKind - token kind that the parser was trying to consume.
      // expectedToken - the image of the token - tokenImages[expectedKind].
      // actual - the actual token that the parser got instead.
      virtual void handleUnexpectedToken(int expectedKind, JAVACC_STRING_TYPE expectedToken, Token *actual, Parser *parser) {
        error_count++;
		std::cerr << "Expecting " << addUnicodeEscapes(expectedToken) << " at: " << actual->beginLine << ":" << actual->beginColumn << "but got " << addUnicodeEscapes(actual->image) << std::endl;
      }
      // Called when the parser cannot continue parsing.
      // last - the last token successfully parsed.
      // unexpected - the token at which the error occurs.
      // production - the production in which this error occurrs.
      virtual void handleParseError(Token *last, Token *unexpected, JAVACC_SIMPLE_STRING production, Parser *parser) {
        error_count++;
		std::cerr << "Encountered: " << addUnicodeEscapes(unexpected->image) << " at: " << unexpected->beginLine << ":" << unexpected->beginColumn << "while parsing: " << production << std::endl;
	  }
      virtual int getErrorCount() {
        return error_count;
      }
      virtual void handleOtherError(JAVACC_STRING_TYPE message, Parser *parser) {
	    std::cerr << "Error: " << message << std::endl;
      }
      virtual ~ErrorHandler() {}
      ErrorHandler() { error_count = 0; }
  };

  class ParserTokenManager;
  class TokenManagerErrorHandler {
    friend class ParserTokenManager;
    protected:
      int error_count;
    public:
      // Returns a detailed message for the Error when it is thrown by the
      // token manager to indicate a lexical error.
      // Parameters :
      //    EOFSeen     : indicates if EOF caused the lexical error
      //    curLexState : lexical state in which this error occurred
      //    errorLine   : line number when the error occurred
      //    errorColumn : column number when the error occurred
      //    errorAfter  : prefix that was seen before this error occurred
      //    curchar     : the offending character
      //
      virtual void lexicalError(bool EOFSeen, int lexState, int errorLine, int errorColumn, JAVACC_STRING_TYPE errorAfter, JAVACC_CHAR_TYPE curChar, ParserTokenManager* token_manager) {
        // by default, we just print an error message and return.
		std::cerr << "Lexical error at: " << errorLine << ":" << errorColumn << ". Encountered: " << curChar << " after: " << errorAfter << "." << std::endl;
      }
      virtual void lexicalError(JAVACC_STRING_TYPE errorMessage, ParserTokenManager* token_manager) {
		std::cerr << errorMessage << std::endl;
      }
      virtual ~TokenManagerErrorHandler() {}
  };

}

#endif
