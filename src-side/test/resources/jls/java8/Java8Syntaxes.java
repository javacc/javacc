/* PackageDeclaration */

package java8;

/* ImportDeclaration */

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java8.ATD2.CL1;
import java8.Java8Syntaxes.Ann1;
import java8.Java8Syntaxes.Cl2.Cl2in;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import java8.Java8Syntaxes.Cl3;
import java8.Java8Syntaxes.In2;

/* empty TypeDeclaration */
; // Unnecessary but allowed semicolon

/* ClassOrInterfaceDeclaration */

/**
 * Class holding as many java syntaxes as possible for JDK 8 level, including recoverable errors,<br>
 * to be an input to any corresponding JavaCC parser, for its testing phase.<br>
 * Not intended to compile fine (nor to be executed!).<br>
 * To check the completeness of the test suite, when parsed, in the JaCoCo report should remain in red only
 * the branches that throw a new ParseException.
 * <p>
 * Apr 2024.
 * 
 * @author Maͫzͣaͬsͨ
 */
@SuppressWarnings("unused")
public class Java8Syntaxes {
  
  @FunctionalInterface
  public interface BiFunction<T, U, R> {
    
    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
      return (T t, U u) -> after.apply(apply(t, u));
    }
    
    R apply(T t, U u);
  }
  
  public static void main2(final String args[]) throws SecondException {
    final Java8Syntaxes mc;
    mc = new Java8Syntaxes();
    @Ann1
    class ac {
    }
    
    Class<?> arrayType;
    Class<?> elemType = arrayType.getComponentType();
    int nargs;
    MethodHandle mh = mh.asType(MethodType.methodType(arrayType, Collections.<Class<?>>nCopies(nargs, elemType)));

     final class fc extends Bar { }
     abstract class ad extends Cl1, Bar { } // JAVAC_ERROR
     @Ann1 volatile class avc extends Cl1 { } // JAVAC_ERROR
     @Ann1 interface ai { } // JAVAC_ERROR
     interface itf1 implements itf2 { } // JAVAC_ERROR
  }
  
  protected Java8Syntaxes() {
  }
  
  /* Types */
  
  public final @Ann1 class Foo implements @Ann4 AutoCloseable {
    @Override
    public void close() throws FirstException {
    }
  }
  
  @SuppressWarnings("unused")
  public strictfp class Bar<@Ann1 A extends B, B, @Ann2 C extends In1 & In2>
      implements java.io.@Ann4 Closeable {
    @Override
    public void close() throws SecondException {
    }
  }
  
  class Cl1 {
    public void mCPublic() {
    }
    
    protected void mCProtected() {
    }
    
    void mCPackage() {
    }
    
    private void mCPrivate() {
    }
    
    int i;
    // instance initializer
    {
      i = Integer.MAX_VALUE;
    }
    
  }
  
  interface In1 {
    abstract void mIn1();
  }
  
  interface In2 {
    Boolean mIn2();
  }
  
  interface In3 extends In1, In2 {
    boolean mIn3();
  }
  
  private class Cl1In1 extends Cl1 implements In1 {
    private void mCl1In1() {
    }
    
    @Override
    public void mIn1() {
    }
  }
  
  private class Cl1In1In2 extends Cl1In1 implements In2 {
    @Override
    public Boolean mIn2() {
      return null;
    }
    
    Cl1In1In2 currentClass = Cl1In1In2.this;
  }
  
  protected class Cl2 implements In1, In2 {
    
    synchronized <T extends Cl1 & In1> void test(T t) {
      t.mIn1();
      t.mCPublic();
      t.mCProtected();
      t.mCPackage();
    }
    
    public Cl2(String[] args) throws FirstException, SecondException {
      Collection<String> cs = new ArrayList<String>();
      int hc = me1(null);
      SecondException se = new SecondException();
      if (hc == 0)
        throw new FirstException();
      else
        throw se;
    }
    
    int me1(Collection<?> c) {
      return c.hashCode();
    }
    
    public void me2(String[] args) throws FirstException, SecondException {
      Collection<String> cs = new ArrayList<>(1);
      me1(cs);
      SecondException se = new SecondException();
      if (cs.size() == 0)
        throw new FirstException();
      else
        throw se;
    }
    
    /* FormalParameterList / FormParList / Throws */
    
    public <T extends FirstException, @Ann1 U> @Ann4 Cl3<?> getCl3a(Java8Syntaxes.Cl2 this, //
        final @Ann1 Class<@Ann4 ?> @Ann4 [] pa2, @SuppressWarnings("unchecked") U... pa3)
        throws @Ann4 T, SecondException {
      return null;
    }
    
    public <T, U extends FirstException> Cl2(@Ann4 Java8Syntaxes Java8Syntaxes.this,
        @Ann1 Class<@Ann4 T> @Ann4 [] @Ann4 [] pa2, U pa3) throws @Ann4 SecondException {
    }
    
    public <T, @Ann1 U> @Ann4 Cl3<?> getCl3bErr(final @Ann1 Class<@Ann4 ?> @Ann4 [] pa2,
        Java8Syntaxes.Cl2 this, // JAVAC_ERROR
        @ SuppressWarnings("unchecked") U... pa3, U... pa4) { // JAVAC_ERROR
      return null;
    }
    
    public <T, @Ann1 U> @Ann4 Cl3<?> getCl3cErr(Java8Syntaxes.Cl2 this, //
        @SuppressWarnings("unchecked") U... pa3, final @Ann1 Class<@Ann4 ?> @Ann4 [] pa2) { // JAVAC_ERROR
      return null;
    }
    
    @Override
    public void mIn1() {
    }
    
    @Override
    public void mIn2() { // JAVAC_ERROR
    }
    
    @Override
    public Boolean mIn2() { // JAVAC_ERROR
      return null;
    }
    
    class Cl2in {
      
      Cl2in(Bar<?, ?, ?> pa) {
      }
      
      Cl2 cl2;
      
    }
    
  }
  
  class Cl2ine<E> extends Cl2.Cl2in {
    
    /* ExplicitConstructorInvocation */
    
    Cl2ine() throws SecondException, FirstException {
      new Cl2(new String[1]).new Cl2in(null).cl2.super(null);
    }
    
  }
  
  class Cl3<@Ann1 E> implements If1 {
    
    Java8Syntaxes js;
    Cl3<?>        cl;
    
    Cl3(@SuppressWarnings("hiding") Java8Syntaxes js) {
      this.js = js;
    }
    
    @SuppressWarnings("hiding")
    Cl3(Java8Syntaxes js, Cl3<?> cl) {
      this.js = js;
      this.cl = cl;
    }
    
    boolean me1(Collection<? extends E> pa)[] {
      return new boolean[] {
          false
      };
    }
    
    synchronized <T> boolean me2(Collection<T> pa) @Ann4 [] {
      ;
      return new boolean[] {
          false,
      };
    }
    
    @Ann1
    strictfp Foo foo(@Ann1 E pa1, @Ann1 Cl4<@Ann4 ? super @Ann4 E> pa2) {
      return null;
    }
    
    String s = "azer";
    
    native void na();
    
    transient int tr1 = -1, tr2 = 0;
    
    volatile int[] vo = {
        0, 1
    };
    
    int in_ar1[] = new int[] {
        -1, -2
    };
    
    int in_ar2[] = new int[] {
        0
    };
    
    int in_ar3[] = new int @Ann4 [] {};
    
    int in_ar4[][] = new int @Ann4 [2][2];
    
    int in_ar5[][] = new int @Ann4 [2][];
    
    int in_ar6[][] = new int @Ann4 [][] {
        {}, {
            1
        }
    };
    
    class Cl3in {
      
      Cl3<?> cl3;
      
      int[] voin;
      
      Cl3in(Bar<?, ?, ?> pa) {
        cl3 = new Cl3<Bar<?, ?, ?>>(js);
      }
      
      public int m3in() {
        return sh;
      }
      
    }
    
    @Override
    public void m3() {
      int[] res = bo ? vo : bo ? vo : bo ? vo : vo;
    }
    
    public void dm3() {
    }
    
  }
  
  class Cl4<D> extends Cl3<@Ann4 D>.Cl3in {
    
    /* ExplicitConstructorInvocation */
    
    @Ann1
    Cl4() {
      (new Cl3<D>(new Java8Syntaxes())).super(new @Ann4 Bar<String, CharSequence, Cl1In1In2>());
    }
    
    Cl4(Cl1 pa) {
      (new Cl3<@Ann4 D>(new Java8Syntaxes())).<D> super(
          new @Ann4 Bar<ArrayList<D>, AbstractList<D>, Cl1In1In2>());
    }
    
    Cl4(Cl3<D> pa) {
      pa.<D> super(new Bar<String[], CharSequence[], Cl1In1In2>());
    }
    
    Cl4(Foo pa) {
      this();
    }
    
    Cl4(Foo pa1, Bar<?, ?, ?> pa2) {
      <D> this(pa1);
    }
    
  }
  
  class Cl5<E extends Cl1 & In1> {
    
    E cl5;
    
    Cl5(E pa) {
      cl5 = pa;
    }
    
    void docl5() {
    }
    
  }
  
  class Cl5b<E> extends Cl3<E> {
    
    Cl5b(Java8Syntaxes aJs, Cl3<?> aCl) {
      super(aJs, aCl);
    }
    
    /* CastExpression */
    @SuppressWarnings("cast")
    void castbcl5() {
      @SuppressWarnings("synthetic-access") Cl5<Cl1In1> cl5a = new Cl5<Cl1In1>(new Cl1In1());
      cl5a.docl5();
      Cl1 cl1 = new Cl1();
      if (cl1 instanceof Cl1 & cl1 instanceof In1) {
        In1 cl5b = (Cl1 & In1) cl1;
        cl5b.mIn1();
      }
    }
    
  }
  
  class Cl5bin<E> extends Cl3<E>.Cl3in {
    
    Cl5bin(Cl3<E> aCl3, Bar<?, ?, ?> aPa) {
      aCl3.super(aPa);
    }
    
    @Override
    public int m3in() {
      return 0;
    }
    
    /* PrimaryPrefix / super & ClassLiteral & AllocationExpression */
    
    void supercl5in() {
      /* super. */
      super.voin[0] = super.cl3.tr1;
      /* ClassLiteral */
      CL1.class.getClass();
      boolean[].class.getClass();
      byte.class.getClass();
      short.class.getClass();
      int.class.getClass();
      long.class.getClass();
      char.class.getClass();
      float.class.getClass();
      double.class.getClass();
      void.class.toString();
      /* PrimaryPrefix & PrimarySuffix / AllocationExpression */
      Cl3<E>.Cl3in c = new Cl3<E>(null).new Cl3in(null) {
        // empty
      };
    }
    
    Runnable r = new Runnable() {
      @SuppressWarnings("synthetic-access")
      public void run() {
        /* TN.super. */
        Cl5bin.super.voin[0] = Cl5bin.super.m3in();
      }
    };
    
  }
  
  enum En1 implements In3 {
    EN1C1, EN1C2,;
    
    @Override
    public void mIn1() {
    }
    
    @Override
    public Boolean mIn2() {
      return true;
    }
    
    @Override
    public boolean mIn3() {
      return false;
    }
    
    ; // Unnecessary but allowed semicolon
  }
  
  /* Exceptions */
  
  @SuppressWarnings("serial")
  protected class FirstException extends Exception {
  }
  
  static final class SecondException extends java.io.IOException {
    
    private static final long serialVersionUID = 1L;
  }
  
  /* Annotations */
  public @interface Ann1 {
  }
  
  @interface Ann2 {
    String value() default "2nd";
  }
  
  abstract @interface Ann3 {
    String[] value();
  }
  
  @Inherited
  @Target({
      TYPE,
      FIELD,
      METHOD,
      PARAMETER,
      CONSTRUCTOR,
      LOCAL_VARIABLE,
      ANNOTATION_TYPE,
      PACKAGE,
      TYPE_PARAMETER,
      TYPE_USE
  })
  public @interface Ann4 {
    
    Level level()
    
    default Level.BAD;
    
    @Ann1
    public int depth()
    
    default 0;
    
    @Ann2("?")
    enum Level {
      BAD, GOOD
    }
    
    Ann2 ann2() default @Ann2;
  }
  
  /* PrimitiveType & Annotation */
  
  private static String LOOKAHEAD = "\"as a JavaIdentifier\"";
  static {
    LOOKAHEAD = "LOOKAHEAD";
  }
  
  @Ann1 final byte                   by = (byte) LOOKAHEAD.length();
  @Ann2("cz") char                   cz = '\u0000';
  @Ann2(value = "db") double         db = -0.;
  @Ann3({
      "fl1", "fl2"
  }) float                           fl = -0.0f;
  @Ann4(level = Ann4.Level.GOOD) int in = Integer.MAX_VALUE;
  @Ann1 @Ann2("lo") long             lo = Long.MIN_VALUE;
  short                              sh = by;
  boolean                            bo = cz == '\u0000';
  
  String foo = "foo";
  
  @Ann4(depth = 0, level = java8.Java8Syntaxes.Ann4.Level.GOOD) boolean @Ann4() [] @Ann4(ann2 = @Ann2) [] bo_ar = //
      new @Ann4(depth = 0) boolean @Ann4(depth = 1) [2] @Ann4(depth = 2) [2];
  
  private final java.util.List<Float> lst1 = new java.util.ArrayList<Float>();
  private final List<Float>           lst2 = new ArrayList<>();
  
  /* Simple Statements */
  @SuppressWarnings("resource")
  public void SimpleStatements() {
    /* AssertStatement */
    assert !bo;
    assert (bo == (sh == 0)) : SwitchStatements();
    /* LabeledStatement */
    eof:
    /* EmptyStatement */
    ;
    /* BreakStatement & ContinueStatement & LabeledStatement */
    for (;;) {
      if (bo)
        continue;
      else
        break;
    }
    eofc:
    for (;;) {
      if (bo)
        continue eofc;
      else
        break eofc;
    }
    /* DoStatement */
    do
      ; while (bo);
    /* ReturnStatement */
    // done further
    /* SynchronizedStatement */
    synchronized (this) {
      SwitchStatements();
    }
    /* WhileStatement & Expression with AssignmentOperator */
    while (in == (lo *= 0))
      ;
    return;
  }
  
  /* IfStatement */
  public boolean IfStatements() {
    int i = 0;
    if (bo)
      i = 1;
    if (bo)
      i = 1;
    else
      i = 2;
    if (bo)
      i = 1;
    else if (sh == 0)
      i = 2;
    if (bo)
      i = 1;
    else if (sh == 0)
      i = 2;
    else
      i = 3;
    return i == 0;
  }
  
  /* ForStatement */
  protected int ForStatements() {
    
    for (final float f : lst1) {
    }
    for (final float f : lst2) {
    }
    
    final Boolean arrb[] = new Boolean[1];
    for (final Boolean be : arrb) {
      final boolean bv = be.booleanValue();
    }
    
    final Class<ArrayList<? extends Integer>> clazz = null;
    @SuppressWarnings("unchecked") final List<Double> arrlst[] = (List<Double>[]) Array.newInstance(clazz, 1);
    for (final double d : arrlst[0]) {
    }
    
    double sd = 0;
    final double[][] arrarrd = new double[2][];
    for (final double arrd[] : arrarrd) {
      for (double d : arrd) {
        sd += --d;
        ++sd;
      }
    }
    
    for (long l = 10; l > 0; --l)
      ;
    for (; lo > 0; lo++)
      ;
    for (;; lo--)
      break;
    for (; lo > 0;)
      break;
    for (long l = 10, ll = 0;; l--, ++ll)
      break;
    for (lo = 10; lo > 0;)
      ;
    for (Class<?> cl = this.getClass(); cl != Thread.class; cl = cl.getSuperclass())
      ;
    
    // Type inference for generic instance creation
    final java.util.Map<String, String> m = new java.util.HashMap<>();
    final java.util.Map<?, ?> n = new java.util.HashMap<String, String>();
    final java.util.Map<?, ?> o = new java.util.HashMap<>();
    
    return m.size() + n.size() + o.size();
  }
  
  /* SwitchStatement */
  protected Foo SwitchStatements() {
    switch (foo) {
      case "foo": {
      }
      //$FALL-THROUGH$
      case "foobar":
      case "bar":
        System.out.println("never");
        break;
      default: {
      }
    }
    switch (by) { // empty SwitchBlock
    }
    switch (sh) {
      case 0: // empty SwitchBlockStatementGroup
    }
    return null;
  }
  
  /* TryStatement */
  public boolean TryStatements() {
    try {
    } catch (@Ann1 Exception e) {
    }
    ;
    try {
    } catch (final Exception e) {
    } catch (Error e) {
    }
    ;
    try {
    } finally {
    }
    ;
    try {
    } catch (Exception | Error e) {
    } finally {
    }
    ;
    
    try (Foo fo = new Foo()) {
    } catch (@Ann1 Exception e) {
    }
    ;
    
    try (final Foo fo = new Foo(); Bar<?, ?, ?> ba = new Bar<Cl2ine<?>, Cl2.Cl2in, Cl1In1In2>();) {
    } catch (@Ann1 Exception e) {
    }
    ;
    
     try {}; // JAVAC_ERROR
    
    return bo;
  }
  
  /* Expression and down */
  protected Foo expr() {
    int i;
    i = 0;
    i *= 0;
    i /= 0;
    i %= 0;
    i += 0;
    i -= 0;
    i <<= 0;
    i >>= 0;
    i >>>= 0;
    i &= 0;
    i ^= 0;
    i |= 0;
    boolean b;
    b = i != 0 ? true : false;
    b = b || bo && bo ^ !(lst1 instanceof ArrayList);
    b = b & in < 0 || in > 0 && in-- <= 1 | in >= 1;
    i = ++in << 1 + --in >> 1 - in++ >>> 1;
    i = ~in * 1 / +2 % -3;
    @SuppressWarnings("cast") char[] c = (char[]) "abc".toCharArray();
    return null;
  }
  
  /* Underscores in numeric literals */
  protected static Foo uinl() {
    
    /* TOKEN LITERALs */
    
    final int one_million = 1_000__000;
    final int cafe_babe = 0xCAFE_BABE;
    final int zero = 0____0;
    final int binary = 0b1001_1001;
    final double avogadro = 6_0.22e2_2;
    final double hundred1 = 1_0_0d, hundred2 = 1_0_0., hundred3 = 100.;
    final double pi = 3.141_592_65;
    final double half = .5_0;
    final double h = 0x4__3p4_4;
    
    return null;
  }
  
  /* JavaCC reserved words as JavaIdentifers */
  protected static boolean jjidredef() {
    
    /* JavaIdentifier */
    @SuppressWarnings("hiding") final String LOOKAHEAD = null;
    final String IGNORE_CASE = null;
    final String PARSER_BEGIN = null;
    final String PARSER_END = null;
    final String JAVACODE = null;
    final String TOKEN = null;
    final String SPECIAL_TOKEN = null;
    final String MORE = null;
    final String SKIP = null;
    final String TOKEN_MGR_DECLS = null;
    final String EOF = null;
    return true;
  }
  
  void yield() {
  } // not error
  
  void call_yield() {
    yield(); // not error
  }
  
  yield yield = null; // javac warning instead of error on type
  
}

/* Interfaces */

abstract interface If1 {
  default void dm1() {
  }
  
  @Deprecated
  public default void dm2() {
  }
  
  public abstract void m3();
  
  static strictfp void m4() {
  }
  
  String s1 = "";
  
  @Deprecated public static final String s2 = null;
  
  @Deprecated
  abstract default void dm3(); // JAVAC_ERROR
  
  static {
  } // JAVAC_ERROR
  
  volatile String s3; // JAVAC_ERROR
  
}

/* Lambdas and functions */

@FunctionalInterface
interface FuIf1 {
  
  default String dhi1(String string) {
    return null;
  }
  
  String hi1(String string);
  
}

class FuIf1Cl1 implements FuIf1 {
  
  public FuIf1Cl1() {
    ;
  }
  
  public FuIf1Cl1(String s) {
    ;
  }
  
  @Override
  public String hi1(String aString) {
    return "hi " + aString;
  }
  
  public FuIf1 hi2(boolean b) {
    Supplier<Consumer<?>> v1 = () -> a -> {};
    return b ? p -> "b is true" : p -> "b is false";
  }
  
  private static final boolean OLD_UTF8 = AccessController
      .doPrivileged((PrivilegedAction<Boolean>) () -> Boolean
          .getBoolean("com.sun.org.apache.xml.internal.security.c14n.oldUtf8"));
  
}

class FuIf1Cl1e extends FuIf1Cl1 {
  
  public String dohi1(String s, FuIf1Cl1 f) {
    return ((Function<String, String>) super::hi1).apply(s);
  }
  
  @SuppressWarnings("unused")
  public String dohi2(String s, FuIf1Cl1 f) {
    return ((Function<String, String>) super::<String, String> hi1).apply(s);
  }
  
}

class UseFuIf1 {
  
  public String dohi1(String s, FuIf1 fuIf1) {
    return fuIf1.hi1(s);
  }
  
  public String doapp1(String s, char[] t, BiFunction<String, char[], String> fn) {
    return fn.apply(s, t);
  }
  
  public void douse1() {
    
    char[][] ca = {
        {
            'X'
        }
    };
    FuIf1 fuIf1 = p -> p + " from lambda";
    String r1 = new UseFuIf1().dohi1("Message ", fuIf1);
    
    BiFunction<String, char[], String> fn = (@Ann1 String p, final char[] q) -> p + String.valueOf(q)
        + " from lambda";
    String r2 = new UseFuIf1().doapp1("Message ", new char[] {
        'Y'
    }, fn);
    String r3 = new UseFuIf1().doapp1("Message ", ca[0], fn);
    
    FuIf1 fuIf2 = new FuIf1() {
      @Override
      public String hi1(String s) {
        return s + " from inner";
      }
    };
  }
  
  void baeldung() {
    Map<String, Integer> nameMap = new HashMap<>();
    Integer v1 = nameMap.computeIfAbsent("John", s -> s.length());
    Integer v2 = nameMap.computeIfAbsent("John", String::length);
    nameMap.replaceAll((name, oldValue) -> name.equals("Freddy") ? oldValue : oldValue + 10000);
    Function<Integer, String> intToString = Object::toString;
    Function<String, String> quote = s -> "'" + s + "'";
    Function<Integer, String> quoteIntToString = quote.compose(intToString);
  }
  
  FuIf1 fuIf3 = p -> {
    return p + " from lambda with block";
  };
  
}

interface FuIf2<T, R> {
  R apply(T t);
}

class FuCl2 {
  
  int m() {
    return 0;
  }
  
  int m(Object t) {
    return 0;
  }
  
  int m(FuCl2 t) {
    return 0;
  }
  
  void test() {
    
    /* PrimaryPrefix / MethodReference */
    FuIf2<FuCl2, Integer> f1;
    f1 = FuCl2::m;
    FuIf2<FuCl2, Integer> f2 = FuCl2::m;
    int i1 = new FuCl2().m();
    
  }
  
}

class FuCl2If2<T, R> implements FuIf2<T, R> {
  
  public FuCl2If2(T t) {
  }
  
  @Override
  public R apply(T aT) {
    return null;
  }
}

class useFuCl2If2<T, R> {
  
  class Cl4<Integer> {
    Cl4() {
    }
  }
  
  public <U, V> void douse1() {
    FuIf2<T, FuCl2If2<T, R>> f1 = FuCl2If2::new;
    FuIf2<T, FuCl2If2<T, U>> f2 = FuCl2If2<T, U>::new;
    @SuppressWarnings("unused") FuIf2<T, FuCl2If2<T, V>> f3 = FuCl2If2<T, V>::<T, V> new;
  }
  
}

/* EnumDeclaration */

enum En1 {
  @java8.Java8Syntaxes.Ann4
  E1C1;
}

@java8.Java8Syntaxes.Ann4
enum En2 implements In2 {
  E1C1(), E1C2();
  
  @Override
  public Boolean mIn2() {
  }
  
  class En2Cl1 {
  }
  
  interface En2Itf1 {
  }
  
}

enum En3 {
  E1C1 {
  },
  E1C2 {
  };
  
  class En2Cl1 {
  }
  
  public abstract interface En3Itf1 {
  }
}

/* AnnotationTypeDeclaration */

@interface ATD1 {
}

@java8.Java8Syntaxes.Ann4
@interface ATD2 {
  
  int j = "".length();
  
  int i()
  
  default 0;
  
  String s1()
  
  default "x";
  
  String @Ann5 [] s2() default {
      "a", "b",
  };
  
  String s3()[] default { // JAVAC_ERROR
      "a", "b",
  };
  
  interface IF1 {
  }
  
  class CL1 {
  }
  
  enum EN1 {
  }
  
  @interface ATD2i {
  }
}

@interface ATD3 {
  String s1 @Ann5 [] = null;
  
  String s2();
  
  // useless but allowed
  ;
}

@Target({
    TYPE,
    FIELD,
    METHOD,
    PARAMETER,
    CONSTRUCTOR,
    LOCAL_VARIABLE,
    ANNOTATION_TYPE,
    PACKAGE,
    TYPE_PARAMETER,
    TYPE_USE
})
@interface Ann5 {
}

/* TypeIdentifier & JavaIdentifier */

class exports {
  exports() {
  }
  
  void module() {
  }
  
  void open() {
  }
  
  void opens() {
  }
  
  void permits() {
  }
  
  void provides() {
  }
  
  void record() {
  }
  
  void requires() {
  }
  
  void sealed() {
  }
  
  void to() {
  }
  
  void transitive() {
  }
  
  void uses() {
  }
  
  void var() {
  }
  
  void when() {
  }
  
  void with() {
  }
  
  void yield() {
  }
}

interface module {
  default void exports() {
  }
}

enum open {
}

@interface opens {
}

class permits {
}

interface provides {
}

enum record {
}

@interface requires {
}

class sealed {
}

interface to {
}

enum transitive {
}

@interface uses {
}

class var {
}

interface when {
}

enum with {
}

@interface yield {
}

class EOF {
  EOF() {
  }
  
  void IGNORE_CASE() {
  }
  
  void JAVACODE() {
  }
  
  void LOOKAHEAD() {
  }
  
  void MORE() {
  }
  
  void PARSER_BEGIN() {
  }
  
  void PARSER_END() {
  }
  
  void SKIP() {
  }
  
  void SPECIAL_TOKEN() {
  }
  
  void TOKEN() {
  }
  
  void TOKEN_MGR_DECLS() {
  }
}

interface IGNORE_CASE {
  static void EOF() {
  }
}

enum JAVACODE {
}

@interface LOOKAHEAD {
}

class MORE {
}

interface PARSER_BEGIN {
}

enum PARSER_END {
}

@interface SKIP {
}

class SPECIAL_TOKEN {
}

interface TOKEN {
}

enum TOKEN_MGR_DECLS {
}
