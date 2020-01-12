
package org.javacc.parser;

import java.util.HashMap;
import java.util.Map;

public final class CodeGeneratorSettings extends HashMap<String, Object> {

  private static final long serialVersionUID = -3963288772981602994L;

  CodeGeneratorSettings(Map<String, Object> userOptions) {
    putAll(userOptions);
  }
}
