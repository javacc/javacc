
/*
 * Copyright © 2002 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * California 95054, U.S.A. All rights reserved.  Sun Microsystems, Inc. has
 * intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation,
 * these intellectual property rights may include one or more of the U.S.
 * patents listed at http://www.sun.com/patents and one or more additional
 * patents or pending patent applications in the U.S. and in other countries.
 * U.S. Government Rights - Commercial software. Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and applicable
 * provisions of the FAR and its supplements.  Use is subject to license terms.
 * Sun,  Sun Microsystems,  the Sun logo and  Java are trademarks or registered
 * trademarks of Sun Microsystems, Inc. in the U.S. and other countries.  This
 * product is covered and controlled by U.S. Export Control laws and may be
 * subject to the export or import laws in other countries.  Nuclear, missile,
 * chemical biological weapons or nuclear maritime end uses or end users,
 * whether direct or indirect, are strictly prohibited.  Export or reexport
 * to countries subject to U.S. embargo or to entities identified on U.S.
 * export exclusion lists, including, but not limited to, the denied persons
 * and specially designated nationals lists is strictly prohibited.
 */



/**
 * A subclass of CalcInputTokenManager so that we can do better error reporting
 * via the GUI object.
 */
class MyLexer extends CalcInputParserTokenManager {

   /**
    * We redefined the lexical error reporting function so that it beeps
    * and displays a messgae thru the GUI.
    */
   protected void LexicalError() {
     CalcGUI.Error("ERROR (click 0)");
     ReInit(gui.getCollector(), OPERAND);
     result = 0.0;
   }

   public MyLexer(CalcGUI guiObj)
   {
      super(guiObj.getCollector(), OPERAND);
      gui = guiObj;
   }
}
