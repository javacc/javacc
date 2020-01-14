
package org.javacc.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link CodeGeneratorSettings} implements a {@link Map} builder
 */
public final class CodeGeneratorSettings extends HashMap<String, Object> {

  private static final long serialVersionUID = -3963288772981602994L;

  /**
   * Constructs an instance of {@link CodeGeneratorSettings}.
   *
   * @param options
   */
  private CodeGeneratorSettings(Map<String, Object> options) {
    putAll(options);
  }

  /**
   * Add another {@link CodeGeneratorSettings} to the current instance.
   * 
   * @param options
   */
  public final CodeGeneratorSettings add(CodeGeneratorSettings options) {
    putAll(options);
    return this;
  }

  /**
   * Set an option to he {@link CodeGeneratorSettings}.
   */
  public final CodeGeneratorSettings set(String key, Object value) {
    put(key, value);
    return this;
  }

  /**
   * Creates a new instance of {@link CodeGeneratorSettings} from another option
   * {@link Map}.
   * 
   * @param options
   */
  public static CodeGeneratorSettings of(Map<String, Object> options) {
    return new CodeGeneratorSettings(options);
  }

  /**
   * Creates an empty instance of {@link CodeGeneratorSettings}.
   */
  public static CodeGeneratorSettings create() {
    return new CodeGeneratorSettings(Collections.emptyMap());
  }
}
