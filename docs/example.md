[Home](index.md) > Example

---

This example recognizes matching braces followed by zero or more line terminators and then an end of file.

Examples of legal strings in this grammar are:

  `"{}"`, `"{{{{{}}}}}"` etc.

Examples of illegal strings are:

  `"{{{{"`, `"{}{}"`, `"{}}"`, `"{{}{}}"`, `"{ }"`, `"{x}"` etc.

#### Grammar
```
PARSER_BEGIN(Example)

/** Simple brace matcher. */
public class Example {

  /** Main entry point. */
  public static void main(String args[]) throws ParseException {
    Example parser = new Example(System.in);
    parser.Input();
  }

}

PARSER_END(Example)

/** Root production. */
void Input() :
{}
{
  MatchedBraces() ("\n"|"\r")* <EOF>
}

/** Brace matching production. */
void MatchedBraces() :
{}
{
  "{" [ MatchedBraces() ] "}"
}
```

#### Output
```
$ java Example
{{}}<return>
<control-d>

$ java Example
{x<return>
Lexical error at line 1, column 2.  Encountered: "x"
TokenMgrError: Lexical error at line 1, column 2.  Encountered: "x" (120), after : ""
        at ExampleTokenManager.getNextToken(ExampleTokenManager.java:146)
        at Example.getToken(Example.java:140)
        at Example.MatchedBraces(Example.java:51)
        at Example.Input(Example.java:10)
        at Example.main(Example.java:6)

$ java Example
{}}<return>
ParseException: Encountered "}" at line 1, column 3.
Was expecting one of:
    <EOF>
    "\n" ...
    "\r" ...
        at Example.generateParseException(Example.java:184)
        at Example.jj_consume_token(Example.java:126)
        at Example.Input(Example.java:32)
        at Example.main(Example.java:6)
```

<br>

---

[Home](index.md)

<br>
