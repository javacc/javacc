
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


import java.awt.*;

public class CalcGUI extends Frame implements CalcInputParserConstants {

  /**
   * A button object is created for each calculator button.  Since
   * there is going to be only one calculator GUI, these objects can
   * be static.
   */
  static Button one = new Button("1");
  static Button two = new Button("2");
  static Button three = new Button("3");
  static Button four = new Button("4");
  static Button five = new Button("5");
  static Button six = new Button("6");
  static Button seven = new Button("7");
  static Button eight = new Button("8");
  static Button nine = new Button("9");
  static Button zero = new Button("0");
  static Button dot = new Button(".");
  static Button equal = new Button("=");
  static Button add = new Button("+");
  static Button sub = new Button("-");
  static Button mul = new Button("*");
  static Button div = new Button("/");
  static Button quit = new Button("QUIT");

  /**
   * The display window with its initial setting.
   */
  static Label display = new Label("0 ");

  /**
   * The class that collects the characters produced by the GUI
   */
  static final CharCollector collector = new CharCollector();

  static final CharStream getCollector()
  {
     return collector;
  }

  /**
   * string that holds what the calculator displays
   */
  static String label = "0";

  /**
   * flag to indicate if this is the first digit.
   */
  static boolean firstDigit = true;

  /**
   * Indicates an error has occured 
   */
  static boolean error = false;

  /**
   * Constructor that creates the full GUI.  This is called by the
   * main program to create one calculator GUI.
   */
  public CalcGUI() {

    super("Calculator");

    GridBagLayout gb = new GridBagLayout();
    setLayout(gb);
    GridBagConstraints gbc = new GridBagConstraints();

    display.setFont(new Font("TimesRoman", Font.BOLD, 18));
    display.setAlignment(Label.RIGHT);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gb.setConstraints(display, gbc);
    add(display);

    Panel buttonPanel = new Panel();
    buttonPanel.setFont(new Font("TimesRoman", Font.BOLD, 14));
    buttonPanel.setLayout(new GridLayout(4,4));
    buttonPanel.add(one); buttonPanel.add(two); buttonPanel.add(three); buttonPanel.add(four);
    buttonPanel.add(five); buttonPanel.add(six); buttonPanel.add(seven); buttonPanel.add(eight);
    buttonPanel.add(nine); buttonPanel.add(zero); buttonPanel.add(dot);  buttonPanel.add(equal);
    buttonPanel.add(add); buttonPanel.add(sub); buttonPanel.add(mul); buttonPanel.add(div);
    gbc.weighty = 1.0;
    gb.setConstraints(buttonPanel, gbc);
    add(buttonPanel);

    quit.setFont(new Font("TimesRoman", Font.BOLD, 14));
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weighty = 0.0;
    gb.setConstraints(quit, gbc);
    add(quit);
    pack();
    show();
  }

  /**
   * Here we just return the character that is input.
   */
  public boolean handleEvent(Event evt) {
    char c = 0;
 
    if (evt.id != Event.ACTION_EVENT) {
      return false;
    }

    if (evt.target == quit) {
      System.exit(0);
    }

    if (error)
    {
       if (evt.target == zero)
       {
          error = false;
          print("0");
          firstDigit = true;
          return true;
       }

       return false;
    }

    if (evt.target == equal) {
      c = '=';
      firstDigit = true;
    }
    else if (evt.target == add) {
      c = '+';
      label = "0";
      firstDigit = true;
    }
    else if (evt.target == sub) {
      c = '-';
      label = "0";
      firstDigit = true;
    }
    else if (evt.target == mul) {
      c = '*';
      label = "0";
      firstDigit = true;
    }
    else if (evt.target == div) {
      c = '/';
      label = "0";
      firstDigit = true;
    }
    else 
    {
      if (firstDigit)
         label = "";
      firstDigit = false;

      if (evt.target == one) {
        c = '1';
        label += c;
      }
      else if (evt.target == two) {
        c = '2';
        label += c;
      }
      else if (evt.target == three) {
        c = '3';
        label += c;
      }
      else if (evt.target == four) {
        c = '4';
        label += c;
      }
      else if (evt.target == five) {
        c = '5';
        label += c;
      }
      else if (evt.target == six) {
        c = '6';
        label += c;
      }
      else if (evt.target == seven) {
        c = '7';
        label += c;
      }
      else if (evt.target == eight) {
        c = '8';
        label += c;
      }
      else if (evt.target == nine) {
        c = '9';
        label += c;
      }
      else if (evt.target == zero) {
        c = '0';
        label += c;
      }
      else if (evt.target == dot) {
        c = '.';
        label += ".";
      }
      else
        return false;
    }

    print(label);
    collector.put(c);
    return true;
  }

  public static void print(double value) {
    display.setText(label = Double.toString(value));
  }

  public static void print(String image) {
    display.setText(label = image);
  }

  public static void Error(String image) {
     print(image);
     Toolkit.getDefaultToolkit().beep();
     collector.Clear();
     label = "0";
     error = true;
  }

}
