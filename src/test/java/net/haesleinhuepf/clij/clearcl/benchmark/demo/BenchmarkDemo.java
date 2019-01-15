package net.haesleinhuepf.clij.clearcl.benchmark.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.backend.jocl.ClearCLBackendJOCL;
import net.haesleinhuepf.clij.clearcl.benchmark.Benchmark;
import net.haesleinhuepf.clij.clearcl.enums.BenchmarkTest;

import org.junit.Test;

/**
 * Bencmark demo
 *
 * @author royer
 */
public class BenchmarkDemo
{

  /**
   * Benchmark demo
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void demo() throws Exception
  {
    Benchmark.sStdOutVerbose = true;
    testWithBackend(new ClearCLBackendJOCL());
    // testWithBackend(new ClearCLBackendJavaCL());
  }

  private void testWithBackend(ClearCLBackendInterface lClearCLBackendInterface)
  {
    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ArrayList<ClearCLDevice> lAllDevices = lClearCL.getAllDevices();

      Collections.shuffle(lAllDevices, new Random(System.nanoTime()));

      for (BenchmarkTest lBenchmarkTest : BenchmarkTest.values())
      {
        ClearCLDevice lFastestDevice =
                                     Benchmark.getFastestDevice(lAllDevices,
                                                                lBenchmarkTest);

        System.out.format("Fastest device for test '%s': %s \n",
                          lBenchmarkTest,
                          lFastestDevice);
      }
    }
  }

}
