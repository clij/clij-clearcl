package net.haesleinhuepf.clij.clearcl.ops.noise.demo;

import java.io.IOException;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLContext;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackends;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.enums.MemAllocMode;
import net.haesleinhuepf.clij.clearcl.ops.noise.FractionalBrownianNoise;
import net.haesleinhuepf.clij.clearcl.util.ElapsedTime;
import net.haesleinhuepf.clij.clearcl.viewer.ClearCLImageViewer;
import coremem.enums.NativeTypeEnum;

import org.junit.Test;

/**
 * Fractional Brownian Noise Demos
 *
 * @author royer
 */
public class FractionalBrownianNoiseDemos
{

  /**
   * Tests 2D FBM
   * 
   * @throws InterruptedException
   *           NA
   * @throws IOException
   *           NA
   */
  @Test
  public void demo2DFBM() throws InterruptedException, IOException
  {
    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lClearCLBackendInterface =
                                                     ClearCLBackends.getBestBackend();

    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      System.out.println(lFastestGPUDevice);

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      FractionalBrownianNoise lNoise =
                                     new FractionalBrownianNoise(lContext.getDefaultQueue());

      ClearCLBuffer lBuffer = lContext.createBuffer(MemAllocMode.Best,
                                                    HostAccessType.ReadOnly,
                                                    KernelAccessType.WriteOnly,
                                                    1,
                                                    NativeTypeEnum.Float,
                                                    512,
                                                    512);

      ClearCLImageViewer lViewImage =
                                    ClearCLImageViewer.view(lBuffer);

      for (int i = 0; i < 10 && lViewImage.isShowing(); i++)
      {
        lNoise.setSeed(i);
        lNoise.fbm2D(lBuffer, true);
        // Thread.sleep(5000);
      }

    }
  }

  /**
   * Tests 3D FBM
   * 
   * @throws InterruptedException
   *           NA
   * @throws IOException
   *           NA
   */
  @Test
  public void demoFBM3D() throws InterruptedException, IOException
  {
    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lClearCLBackendInterface =
                                                     ClearCLBackends.getBestBackend();

    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      System.out.println(lFastestGPUDevice);

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      FractionalBrownianNoise lNoise =
                                     new FractionalBrownianNoise(lContext.getDefaultQueue());

      ClearCLBuffer lBuffer = lContext.createBuffer(MemAllocMode.Best,
                                                    HostAccessType.ReadOnly,
                                                    KernelAccessType.WriteOnly,
                                                    1,
                                                    NativeTypeEnum.Float,
                                                    128,
                                                    128,
                                                    128);

      ClearCLImageViewer lViewImage =
                                    ClearCLImageViewer.view(lBuffer);

      for (int i = 0; i < 10 && lViewImage.isShowing(); i++)
      {
        lNoise.setSeed(i);
        lNoise.fbm3D(lBuffer, true);
        // Thread.sleep(1000);
      }

    }

  }

}
