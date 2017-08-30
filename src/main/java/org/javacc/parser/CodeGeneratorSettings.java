package org.javacc.parser;

import java.util.HashMap;
import java.util.Map;

public final class CodeGeneratorSettings extends HashMap<String, Object>
{
   CodeGeneratorSettings(Map<String, Object> userOptions)
   {
     putAll(userOptions);
   }
}
