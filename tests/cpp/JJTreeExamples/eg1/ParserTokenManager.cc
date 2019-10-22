/* ParserTokenManager.cc */
#include "ParserTokenManager.h"
#include "TokenMgrError.h"
#include "ParserTree.h"
namespace EG1 {
static const unsigned long long jjbitVec0[] = {
   0x0ULL, 0x0ULL, 0xffffffffffffffffULL, 0xffffffffffffffffULL
};
static const int jjnextStates[] = {
   11, 12, 14, 10, 15, 6, 8, 2, 18, 20, 
};
static JJChar jjstrLiteralChars_0[] = {0};
static JJChar jjstrLiteralChars_1[] = {0};
static JJChar jjstrLiteralChars_2[] = {0};
static JJChar jjstrLiteralChars_3[] = {0};
static JJChar jjstrLiteralChars_4[] = {0};
static JJChar jjstrLiteralChars_5[] = {0};
static JJChar jjstrLiteralChars_6[] = {0};

static JJChar jjstrLiteralChars_7[] = {0};
static JJChar jjstrLiteralChars_8[] = {0};
static JJChar jjstrLiteralChars_9[] = {0};
static JJChar jjstrLiteralChars_10[] = {0};
static JJChar jjstrLiteralChars_11[] = {0};
static JJChar jjstrLiteralChars_12[] = {0};
static JJChar jjstrLiteralChars_13[] = {0};

static JJChar jjstrLiteralChars_14[] = {0x3b, 0};
static JJChar jjstrLiteralChars_15[] = {0x2b, 0};

static JJChar jjstrLiteralChars_16[] = {0x2d, 0};
static JJChar jjstrLiteralChars_17[] = {0x2a, 0};

static JJChar jjstrLiteralChars_18[] = {0x2f, 0};
static JJChar jjstrLiteralChars_19[] = {0x25, 0};

static JJChar jjstrLiteralChars_20[] = {0x28, 0};
static JJChar jjstrLiteralChars_21[] = {0x29, 0};
static const JJString jjstrLiteralImages[] = {
jjstrLiteralChars_0, 
jjstrLiteralChars_1, 
jjstrLiteralChars_2, 
jjstrLiteralChars_3, 
jjstrLiteralChars_4, 
jjstrLiteralChars_5, 
jjstrLiteralChars_6, 
jjstrLiteralChars_7, 
jjstrLiteralChars_8, 
jjstrLiteralChars_9, 
jjstrLiteralChars_10, 
jjstrLiteralChars_11, 
jjstrLiteralChars_12, 
jjstrLiteralChars_13, 
jjstrLiteralChars_14, 
jjstrLiteralChars_15, 
jjstrLiteralChars_16, 
jjstrLiteralChars_17, 
jjstrLiteralChars_18, 
jjstrLiteralChars_19, 
jjstrLiteralChars_20, 
jjstrLiteralChars_21, 
};

/** Lexer state names. */
static const JJChar lexStateNames_arr_0[] = 
{0x44, 0x45, 0x46, 0x41, 0x55, 0x4c, 0x54, 0};
static const JJString lexStateNames[] = {
lexStateNames_arr_0, 
};
static const unsigned long long jjtoToken[] = {
   0x3fc881ULL, 
};
static const unsigned long long jjtoSkip[] = {
   0x7eULL, 
};

  void  ParserTokenManager::setDebugStream(FILE *ds){ debugStream = ds; }

 int ParserTokenManager::jjStopStringLiteralDfa_0(int pos, unsigned long long active0){
   switch (pos)
   {
      default :
         return -1;
   }
}

int  ParserTokenManager::jjStartNfa_0(int pos, unsigned long long active0){
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}

 int  ParserTokenManager::jjStopAtPos(int pos, int kind){
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}

 int  ParserTokenManager::jjMoveStringLiteralDfa0_0(){
   switch(curChar)
   {
      case 37:
         return jjStopAtPos(0, 19);
      case 40:
         return jjStopAtPos(0, 20);
      case 41:
         return jjStopAtPos(0, 21);
      case 42:
         return jjStopAtPos(0, 17);
      case 43:
         return jjStopAtPos(0, 15);
      case 45:
         return jjStopAtPos(0, 16);
      case 47:
         return jjStartNfaWithStates_0(0, 18, 10);
      case 59:
         return jjStopAtPos(0, 14);
      default :
         return jjMoveNfa_0(0, 0);
   }
}

int ParserTokenManager::jjStartNfaWithStates_0(int pos, int kind, int state){
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   if (input_stream->endOfInput()) { return pos + 1; }
   curChar = input_stream->readChar();
   return jjMoveNfa_0(state, pos + 1);
}

int ParserTokenManager::jjMoveNfa_0(int startState, int curPos){
   int startsAt = 0;
   jjnewStateCnt = 21;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         unsigned long long l = 1ULL << curChar;
         (void)l;
         do
         {
            switch(jjstateSet[--i])
            {
               case 10:
                  if (curChar == 42)
                     { jjCheckNAddTwoStates(16, 17); }
                  else if (curChar == 47)
                     { jjCheckNAddStates(0, 2); }
                  break;
               case 0:
                  if ((0x3fe000000000000ULL & l) != 0L)
                  {
                     if (kind > 7)
                        kind = 7;
                     { jjCheckNAddTwoStates(1, 2); }
                  }
                  else if (curChar == 47)
                     { jjAddStates(3, 4); }
                  else if (curChar == 48)
                  {
                     if (kind > 7)
                        kind = 7;
                     { jjCheckNAddStates(5, 7); }
                  }
                  break;
               case 1:
                  if ((0x3ff000000000000ULL & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  { jjCheckNAddTwoStates(1, 2); }
                  break;
               case 4:
                  if ((0x3ff000000000000ULL & l) == 0L)
                     break;
                  if (kind > 11)
                     kind = 11;
                  jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 5:
                  if (curChar != 48)
                     break;
                  if (kind > 7)
                     kind = 7;
                  { jjCheckNAddStates(5, 7); }
                  break;
               case 7:
                  if ((0x3ff000000000000ULL & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  { jjCheckNAddTwoStates(7, 2); }
                  break;
               case 8:
                  if ((0xff000000000000ULL & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  { jjCheckNAddTwoStates(8, 2); }
                  break;
               case 9:
                  if (curChar == 47)
                     { jjAddStates(3, 4); }
                  break;
               case 11:
                  if ((0xffffffffffffdbffULL & l) != 0L)
                     { jjCheckNAddStates(0, 2); }
                  break;
               case 12:
                  if ((0x2400ULL & l) != 0L && kind > 5)
                     kind = 5;
                  break;
               case 13:
                  if (curChar == 10 && kind > 5)
                     kind = 5;
                  break;
               case 14:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 15:
                  if (curChar == 42)
                     { jjCheckNAddTwoStates(16, 17); }
                  break;
               case 16:
                  if ((0xfffffbffffffffffULL & l) != 0L)
                     { jjCheckNAddTwoStates(16, 17); }
                  break;
               case 17:
                  if (curChar == 42)
                     { jjAddStates(8, 9); }
                  break;
               case 18:
                  if ((0xffff7fffffffffffULL & l) != 0L)
                     { jjCheckNAddTwoStates(19, 17); }
                  break;
               case 19:
                  if ((0xfffffbffffffffffULL & l) != 0L)
                     { jjCheckNAddTwoStates(19, 17); }
                  break;
               case 20:
                  if (curChar == 47 && kind > 6)
                     kind = 6;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         unsigned long long l = 1ULL << (curChar & 077);
         (void)l;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 4:
                  if ((0x7fffffe87fffffeULL & l) == 0L)
                     break;
                  if (kind > 11)
                     kind = 11;
                  { jjCheckNAdd(4); }
                  break;
               case 2:
                  if ((0x100000001000ULL & l) != 0L && kind > 7)
                     kind = 7;
                  break;
               case 6:
                  if ((0x100000001000000ULL & l) != 0L)
                     { jjCheckNAdd(7); }
                  break;
               case 7:
                  if ((0x7e0000007eULL & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  { jjCheckNAddTwoStates(7, 2); }
                  break;
               case 11:
                  { jjAddStates(0, 2); }
                  break;
               case 16:
                  { jjCheckNAddTwoStates(16, 17); }
                  break;
               case 18:
               case 19:
                  { jjCheckNAddTwoStates(19, 17); }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         unsigned long long l2 = 1ULL << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 11:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     { jjAddStates(0, 2); }
                  break;
               case 16:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     { jjCheckNAddTwoStates(16, 17); }
                  break;
               case 18:
               case 19:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     { jjCheckNAddTwoStates(19, 17); }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt), (jjnewStateCnt = startsAt), (i == (startsAt = 21 - startsAt)))
         return curPos;
      if (input_stream->endOfInput()) { return curPos; }
      curChar = input_stream->readChar();
   }
}

/** Token literal values. */

Token * ParserTokenManager::jjFillToken(){
   Token *t;
   JJString curTokenImage;
   int beginLine   = -1;
   int endLine     = -1;
   int beginColumn = -1;
   int endColumn   = -1;
   JJString im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im.length() == 0) ? input_stream->GetImage() : im;
   if (input_stream->getTrackLineColumn()) {
     beginLine = input_stream->getBeginLine();
     beginColumn = input_stream->getBeginColumn();
     endLine = input_stream->getEndLine();
     endColumn = input_stream->getEndColumn();
   }
   t = Token::newToken(jjmatchedKind);
   t->kind = jjmatchedKind;
   t->image = curTokenImage;
   t->specialToken = nullptr;
   t->next = nullptr;

   if (input_stream->getTrackLineColumn()) {
   t->beginLine = beginLine;
   t->endLine = endLine;
   t->beginColumn = beginColumn;
   t->endColumn = endColumn;
   }

   return t;
}
const int defaultLexState = 0;
/** Get the next Token. */

Token * ParserTokenManager::getNextToken(){
  Token *matchedToken = nullptr;
  int curPos = 0;

  for (;;)
  {
   EOFLoop: 
   if (input_stream->endOfInput())
   {
      jjmatchedKind = 0;
      jjmatchedPos = -1;
      matchedToken = jjFillToken();
      return matchedToken;
   }
   curChar = input_stream->BeginToken();

   { input_stream->backup(0);
      while (curChar <= 32 && (0x100002600ULL & (1ULL << curChar)) != 0L)
   {
   if (input_stream->endOfInput()) { goto EOFLoop; }
   curChar = input_stream->BeginToken();
   }
   }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream->backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1ULL << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         goto EOFLoop;
      }
   }
   int error_line = input_stream->getEndLine();
   int error_column = input_stream->getEndColumn();
   JJString error_after;
   bool EOFSeen = false;
   if (input_stream->endOfInput()) {
      EOFSeen = true;
      error_after = curPos <= 1 ? EMPTY : input_stream->GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      error_after = curPos <= 1 ? EMPTY : input_stream->GetImage();
   }
   errorHandler->lexicalError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, this);
  }
}

  /** Reinitialise parser. */
  void ParserTokenManager::ReInit(JAVACC_CHARSTREAM *stream, int lexState) {
    clear();
    jjmatchedPos = jjnewStateCnt = 0;
    curLexState = lexState;
    input_stream = stream;
    ReInitRounds();
    debugStream = stdout; // init
    SwitchTo(lexState);
    if (delete_eh) delete errorHandler, errorHandler = nullptr;
    errorHandler = new TokenManagerErrorHandler();
    delete_eh = true;
  }

  void ParserTokenManager::ReInitRounds() {
    int i;
    jjround = 0x80000001;
    for (i = 21; i-- > 0;)
      jjrounds[i] = 0x80000000;
  }

  /** Switch to specified lex state. */
  void ParserTokenManager::SwitchTo(int lexState) {
    if (lexState >= 1 || lexState < 0) {
      JJString message;
#ifdef WIDE_CHAR
      message += L"Error: Ignoring invalid lexical state : ";
      message += lexState; message += L". State unchanged.";
#else
      message += "Error: Ignoring invalid lexical state : ";
      message += lexState; message += ". State unchanged.";
#endif
      throw new TokenMgrError(message, INVALID_LEXICAL_STATE);
    } else
      curLexState = lexState;
  }

  /** Constructor. */
  ParserTokenManager::ParserTokenManager (JAVACC_CHARSTREAM *stream, int lexState)
  {
    input_stream = nullptr;
    ReInit(stream, lexState);
  }

  // Destructor
  ParserTokenManager::~ParserTokenManager () {
    clear();
  }

  // clear
  void ParserTokenManager::clear() {
    //Since input_stream was generated outside of TokenManager
    //TokenManager should not take care of deleting it
    //if (input_stream) delete input_stream;
    if (delete_eh) delete errorHandler, errorHandler = nullptr;    
  }


}
