package net.haesleinhuepf.clij.clearcl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLContext;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.ClearCLKernel;
import net.haesleinhuepf.clij.clearcl.ClearCLProgram;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackends;
import net.haesleinhuepf.clij.clearcl.enums.BuildStatus;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.exceptions.ClearCLArgumentMissingException;
import net.haesleinhuepf.clij.clearcl.selector.BadDeviceSelector;
import net.haesleinhuepf.clij.clearcl.selector.DeviceTypeSelector;
import net.haesleinhuepf.clij.clearcl.selector.GlobalMemorySelector;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import org.junit.Test;

/**
 * Basic Kernel tests
 *
 * @author royer
 */
public class ClearCLKernelTests
{

  private static final int cFloatArrayLength = 1024 * 1024;

  /**
   * Test with best backend
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void testBestBackend() throws Exception
  {
    ClearCLBackendInterface lClearCLBackendInterface =
                                                     ClearCLBackends.getBestBackend();

    testWithBackend(lClearCLBackendInterface);
  }

  private void testWithBackend(ClearCLBackendInterface pClearCLBackendInterface) throws Exception
  {
    try (ClearCL lClearCL = new ClearCL(pClearCLBackendInterface))
    {

      ClearCLDevice lClearClDevice =
                                   lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                          BadDeviceSelector.NotIntegratedIntel,
                                                          GlobalMemorySelector.MAX);

      // System.out.println(lClearClDevice.getInfoString());

      ClearCLContext lContext = lClearClDevice.createContext();

      ClearCLProgram lProgram =
                              lContext.createProgram(this.getClass(),
                                                     "test.cl");
      lProgram.addDefine("CONSTANT", "10");
      lProgram.addBuildOptionAllMathOpt();

      BuildStatus lBuildStatus = lProgram.buildAndLog();

      assertEquals(lBuildStatus, BuildStatus.Success);
      // assertTrue(lProgram.getBuildLog().isEmpty());

      ClearCLBuffer lBufferA =
                             lContext.createBuffer(HostAccessType.WriteOnly,
                                                   KernelAccessType.ReadOnly,
                                                   NativeTypeEnum.Float,
                                                   cFloatArrayLength);

      ClearCLBuffer lBufferB =
                             lContext.createBuffer(HostAccessType.WriteOnly,
                                                   KernelAccessType.ReadOnly,
                                                   NativeTypeEnum.Float,
                                                   cFloatArrayLength);

      ClearCLBuffer lBufferC =
                             lContext.createBuffer(HostAccessType.ReadOnly,
                                                   KernelAccessType.WriteOnly,
                                                   NativeTypeEnum.Float,
                                                   cFloatArrayLength);

      ClearCLKernel lKernel = lProgram.createKernel("buffersum");
      lKernel.setGlobalSizes(cFloatArrayLength);

      // System.out.println(lKernel.getSourceCode());

      // checking if include is, well , included:
      assertTrue(lKernel.getSourceCode()
                        .contains("inline float4 matrix_mult"));

      // checking if define is, well , defined:
      assertTrue(lKernel.getSourceCode().contains("CONSTANT"));

      // checking if define is, well , defined:
      assertTrue(lKernel.getSourceCode()
                        .contains("WARNING!! Could not resolve include"));

      // different ways to set arguments:
      lKernel.setArguments(11f, lBufferA, lBufferB, lBufferC);
      lKernel.run();

      lKernel.clearArguments();
      lKernel.setArgument(0, 11f);
      lKernel.setArgument(1, lBufferA);
      lKernel.setArgument(2, lBufferB);
      lKernel.setArgument(3, lBufferC);
      lKernel.run();

      //
      lKernel.clearArguments();
      lKernel.setArgument("p", 11f);
      lKernel.setArgument("a", lBufferA);
      lKernel.setArgument("b", lBufferB);
      lKernel.setArgument("c", lBufferC);
      lKernel.run();

      // what if a argument is missing but there is a default value defined?
      try
      {
        lKernel.clearArguments();
        lKernel.setArgument("a", lBufferA);
        lKernel.setArgument("b", lBufferB);
        lKernel.setArgument("c", lBufferB);
        lKernel.run();
        assertTrue(true);
      }
      catch (ClearCLArgumentMissingException e)
      {
        fail();
      }

      boolean lFailed = false;

      // what if a argument is missing?
      try
      {
        lKernel.clearArguments();
        lKernel.setArgument("p", 11f);
        lKernel.setArgument("a", lBufferA);
        lKernel.setArgument("b", lBufferB);
        lKernel.run();
        lFailed = true;
      }
      catch (RuntimeException e)
      {
        System.out.println("ALL GOOD, Caught as expected: " + e);
      }

      // what if an unknown argument is added?
      try
      {
        lKernel.clearArguments();
        lKernel.setArgument("p", 11f);
        lKernel.setArgument("a", lBufferA);
        lKernel.setArgument("b", lBufferB);
        lKernel.setArgument("c", lBufferC);
        lKernel.setArgument("z", 1.3f);
        lKernel.run();
        lFailed = true;
      }
      catch (RuntimeException e)
      {
        System.out.println("ALL GOOD, Caught as expected: " + e);
      }

      assertTrue(!lFailed);

    }
  }

}
