package org.javacc.jls;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Class for timing a process, measuring the process CPU time, the elapse time and the process and
 * system CPU loads between a {@link #start()} and a {@link #stop()} invocations.<br>
 * Results can then be obtained with {@link #getProcessCpuTime()}, {@link #getElapsedTime()}, {@link
 * #getProcessCPULoad()} and {@link #getSystemCPULoad()}.
 *
 * <p>May 2024 - Jan 2025.
 *
 * @author Maͫzͣaͬsͨ
 */
public class Timings {

  /** For CPU usage reporting (generic interface java.lang.management) */
  private OperatingSystemMXBean osmxb = null;

  /** Method to get the process CPU time (by reflection}) */
  private Method gpct;

  /** Method to get the Process CPU load (by reflection}) */
  private Method gpcl;

  /** Method to get the System CPU load (by reflection}) */
  private Method gscl;

  /** Elapsed start time */
  private long est = 0;

  /** Process CPU start time */
  private long pcpust = 0;

  /** Process CPU start load */
  private double pcpusl = 0;

  /** System CPU start load */
  private double scpusl = 0;

  /** Elapsed consumed time */
  private double ect = 0;

  /** Process CPU consumed time */
  private double pcpuct = 0;

  /** Process CPU load */
  private double pcpul = 0;

  /** System CPU load */
  private double scpul = 0;

  /** Standard constructor. Initializes the CPU measuring. */
  public Timings() {
    // instantiate a MXBean
    osmxb = ManagementFactory.getOperatingSystemMXBean();

    // on Oracle Windows: implementation is in package com.sun.management.OperatingSystemImpl
    // on IBM Linux: implementation is in package com.ibm.lang.management.OperatingSystemImpl

//    System.out.println(
//        "OperatingSystemMXBean: class = "
//            + osmxb.getClass()
//            + ", name = "
//            + osmxb.getName()
//            + ", version = "
//            + osmxb.getVersion()
//            + ", arch = "
//            + osmxb.getArch()
//            + ", object name = "
//            + osmxb.getObjectName()
//            + ",\n methods = "
//            + Arrays.toString(osmxb.getClass().getMethods()).replace(", ", ",\n            ")
//            + ",\n declared methods = "
//            + Arrays.toString(osmxb.getClass().getDeclaredMethods())
//                .replace(", ", ",\n                     "));

    // find getProcessCpuTime() / getProcessCpuLoad() / getCpuLoad()
    // from the declared and inherited public member methods
    for (final Method method : osmxb.getClass().getMethods()) {
      // find getProcessCpuTime() / getProcessCpuLoad() / getCpuLoad() from the declared methods
      //      for (final Method method : osmxb.getClass().getDeclaredMethods()) {
      //      System.out.println("Method: " + method.toString());
      if (method.getName().equals("getProcessCpuTime")
          && Modifier.isPublic(method.getModifiers())) {
        gpct = method;
//        System.out.println("Method " + gpct.toString() + " found!");
        if (gpcl != null && gscl != null) break;
      }
      if (method.getName().equals("getProcessCpuLoad")
          && Modifier.isPublic(method.getModifiers())) {
        gpcl = method;
//        System.out.println("Method " + gpcl.toString() + " found!");
        if (gpct != null && gscl != null) break;
      }
      // getSystemCpuLoad since 1.7, deprecated / replaced by getCpuLoad in 14,  still there in 23
      if ((method.getName().equals("getCpuLoad") || method.getName().equals("getSystemCpuLoad"))
          && Modifier.isPublic(method.getModifiers())) {
        gscl = method;
//        System.out.println("Method " + gscl.toString() + " found!");
        if (gpct != null && gpcl != null) break;
      }
      ManagementFactory.getRuntimeMXBean().getVmVersion();
    }
    // to avoid the exception that will be thrown on setAccessible(true), one must use in JDK 9+ the
    // following jvmarg --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED
    if (gpct != null) {
      gpct.setAccessible(true);
    } else {
      System.err.println(
          "Unable to access OperatingSystemMXBean.getProcessCpuTime(); no process cpu time available!");
    }
    if (gpcl != null) {
      gpcl.setAccessible(true);
    } else {
      System.err.println(
          "Unable to access OperatingSystemMXBean.getProcessCpuLoad(); no process cpu load available!");
    }
    if (gscl != null) {
      gscl.setAccessible(true);
    } else {
      System.err.println(
          "Unable to access OperatingSystemMXBean.getCpuLoad(); no system cpu load available!");
    }
  }

  /** Starts measuring the times. */
  public void start() {
    est = System.currentTimeMillis();
    pcpust = getCpuTime();
    // these first calls will return something that we'll discard,
    // but they should (?) set the start or the "recent cpu usage" interval
    pcpusl = getCpuLoad(gpcl);
    scpusl = getCpuLoad(gscl);
//    System.out.printf(
//        "pcpust = %d cpu nanosec, pcpusl = %.0f%%, scpusl = %.0f%%%n",
//        pcpust, 100 * pcpusl, 100 * scpusl);
  }

  /** Stops measuring the times and computes results. */
  public void stop() {
    final long eet = System.currentTimeMillis(); // milliseconds
    ect = Math.rint((eet - est) / 100L) / 10L; // -> seconds

    final long pcpuet = getCpuTime(); // nanoseconds
    pcpuct = (Math.rint((pcpuet - pcpust) / 100_000_000L) / 10L); // -> seconds

    pcpul = getCpuLoad(gpcl); // 0.nnnnn

    scpul = getCpuLoad(gscl); // 0.nnnnn

//    System.out.printf(
//        "pcpuet = %d cpu nanosec, pcpuct = %.1f cpu sec, pcpul = %.0f%%, scpul = %.0f%%%n",
//        pcpuet, pcpuct, 100 * pcpul, 100 * scpul);
  }

  /**
   * @return the current process CPU time corresponding, or 0 if any exception
   */
  private long getCpuTime() {
    if (gpct == null) {
      return 0;
    }
    try {
      return (long) gpct.invoke(osmxb);
    } catch (final Exception e) {
      e.printStackTrace();
      return 0L;
    }
  }

  /**
   * @param aM - the method to use (for process - JVM - or system - OS -)
   * @return the current CPU load corresponding to the argument, or 0 if any exception
   */
  private double getCpuLoad(final Method aM) {
    if (aM == null) {
      return 0;
    }
    try {
      return (double) aM.invoke(osmxb);
    } catch (final Exception e) {
      e.printStackTrace();
      return 0L;
    }
  }

  /**
   * @return the timed (i.e. between start() and stop()) elapsed time
   */
  public double getElapsedTime() {
    return ect;
  }

  /**
   * @return the timed (i.e. between start() and stop()) process CPU time (in seconds)
   */
  public double getProcessCpuTime() {
    return pcpuct;
  }

  /**
   * @return the timed (i.e. between start() and stop()) process (JVM) CPU load (in 0.0 - 1.0 range)
   */
  public double getProcessCpuLoad() {
    return pcpul;
  }

  /**
   * @return the current process (JVM) CPU load (in 0.0 - 1.0 range)
   */
  public double getCurrentProcessCpuLoad() {
    return getCpuLoad(gpcl);
  }

  /**
   * @return the timed (i.e. between start() and stop()) system (OS) CPU load (in 0.0 - 1.0 range)
   */
  public double getSystemCpuLoad() {
    return scpul;
  }

  /**
   * @return the current system (OS) CPU load (in 0.0 - 1.0 range)
   */
  public double getCurrentSystemCpuLoad() {
    return getCpuLoad(gscl);
  }
}
