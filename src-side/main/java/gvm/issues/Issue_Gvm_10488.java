package gvm.issues;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class Issue_Gvm_10488 {

  /** For CPU usage reporting (generic interface java.lang.management) */
  private OperatingSystemMXBean osmxb = null;

  /** Method to get the process cpu time (by reflection}) */
  private Method gpct;

  @SuppressWarnings("unused")
  public static void main(final String[] args) {
    new Issue_Gvm_10488();
  }

  public Issue_Gvm_10488() {
    // instantiate a mx bean
    osmxb = ManagementFactory.getOperatingSystemMXBean();

    System.out.println(
        "OperatingSystemMXBean: class = "
            + osmxb.getClass()
            + ", name = "
            + osmxb.getName()
            + ", version = "
            + osmxb.getVersion()
            + ", arch = "
            + osmxb.getArch()
            + ", object name = "
            + osmxb.getObjectName()
            + ",\n methods = "
            + Arrays.toString(osmxb.getClass().getMethods()).replace(", ", ",\n            ")
            + ",\n declared methods = "
            + Arrays.toString(osmxb.getClass().getDeclaredMethods())
                .replace(", ", ",\n                     "));

    // find the getProcessCpuTime method from the declared and inherited public member methods
    for (final Method method : osmxb.getClass().getMethods()) {
      // find the getProcessCpuTime method from the declared methods
      //      for (final Method method : osmxb.getClass().getDeclaredMethods()) {
      //      System.out.println("Method: " + method.toString());
      if (method.getName().equals("getProcessCpuTime")
          && Modifier.isPublic(method.getModifiers())) {
        gpct = method;
        System.out.println("Method " + gpct.toString() + " found!");
        break;
      }
    }
    if (gpct != null)
      // to avoid the exception that will be thrown, one must use in JDK 9+ the following jvmarg
      // --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED
      gpct.setAccessible(true);
    else
      System.out.println(
          "Unable to access OperatingSystemMXBean.getProcessCpuTime(); no cpu timings available!");
  }
}
