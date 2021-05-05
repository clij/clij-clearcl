package net.haesleinhuepf.clij.clearcl.util;

/**
 * Utility class to measure execution time of a block of code (closure-based)
 *
 * @author royer
 */
public class ElapsedTime
{
  /**
   * This flag can be set statically to enable/disable stdout logging of elapsed
   * times. Practical for performance evaluation.
   */
  public static boolean sStandardOutput = false;

  /**
   * Measures the elapsed time of a Runnable.
   * 
   * @param pDescription
   *          description of the runnable
   * @param pRunnable
   *          runnable
   * @return elapsed time in milliseconds
   */
  public static double measure(String pDescription,
                               Runnable pRunnable)
  {
    return measure(true, false, pDescription, pRunnable);
  }

  /**
   * Measures the elapsed time of a Runnable. This version forces the outputto
   * standard out.
   * 
   * 
   * @param pDescription
   *          description of the runnable
   * @param pRunnable
   *          runnable
   * @return elapsed time in milliseconds
   */
  public static double measureForceOutput(String pDescription,
                                          Runnable pRunnable)
  {
    return measure(true, true, pDescription, pRunnable);
  }

  /**
   * Measures the elapsed time of a Runnable. An optional boolean flag can be
   * used to switch of the timing.
   * 
   * @param pActive
   *          true measure, false execute without measuring
   * @param pDescription
   *          description of the code (runnable)
   * @param pRunnable
   *          runnable
   * @return elapsed time in milliseconds
   */
  public static double measure(boolean pActive,
                               String pDescription,
                               Runnable pRunnable)
  {
    return measure(pActive, false, pDescription, pRunnable);
  }

  /**
   * Measures the elapsed time of a Runnable. An optional boolean flag can be
   * used to switch of the timing.
   * 
   * @param pActive
   *          true measure, false execute without measuring
   * @param pForceOutput
   *          forces output to standard out
   * @param pDescription
   *          description of the code (runnable)
   * @param pRunnable
   *          runnable
   * @return elapsed time in milliseconds
   */
  public static double measure(boolean pActive,
                               boolean pForceOutput,
                               String pDescription,
                               Runnable pRunnable)
  {
    if (!pActive)
    {
      pRunnable.run();
      return 0;
    }

    Throwable lThrowable = null;

    long lNanosStart = System.nanoTime();
    try
    {
      pRunnable.run();
    }
    catch (Throwable e)
    {
      lThrowable = e;
    }
    long lNanosStop = System.nanoTime();

    long lElapsedNanos = lNanosStop - lNanosStart;
    double lElapsedTimeInMilliseconds = lElapsedNanos * 1e-6;

    if (pForceOutput || sStandardOutput)
      System.out.format("%g ms for %s \n",
                        lElapsedTimeInMilliseconds,
                        pDescription);

    if (lThrowable != null) {
      if (lThrowable instanceof RuntimeException)
        throw (RuntimeException) lThrowable;
      else
        throw new RuntimeException(lThrowable);
    }

    return lElapsedTimeInMilliseconds;
  }
}
