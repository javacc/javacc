public class TokenFactory {

  /**
   * Returns a new Token object. By default a standard "Token" object
   * is returned, but a "GTToken" is returned as necessary to handle the
   * complexities caused by the introduction of generics.
  */

  public static final Token newToken(int ofKind, String tokenImage)
  {
     switch(ofKind)
     {
       //case JavaParserConstants.RUNSIGNEDSHIFT:
       //case JavaParserConstants.RSIGNEDSHIFT:
       //case JavaParserConstants.GT:
          //return new GTToken();
       default :
          return new Token(ofKind, tokenImage);
     }
  }
}
