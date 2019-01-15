package net.haesleinhuepf.clij.clearcl.test;

import static org.junit.Assert.assertEquals;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLContext;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.ClearCLPlatform;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackends;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelOrder;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.util.Region3;
import net.haesleinhuepf.clij.coremem.fragmented.FragmentedMemory;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory;

import org.junit.Test;

/**
 * Basic image tests.
 *
 * @author royer
 */
public class ClearCLImageTests
{

  /**
   * test with best backend
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void testBestBackend() throws Exception
  {
    final ClearCLBackendInterface lClearCLBackendInterface =
                                                           ClearCLBackends.getBestBackend();

    testWithBackend(lClearCLBackendInterface);
  }

  private void testWithBackend(final ClearCLBackendInterface pClearCLBackendInterface) throws Exception
  {
    try (ClearCL lClearCL = new ClearCL(pClearCLBackendInterface))
    {

      final int lNumberOfPlatforms = lClearCL.getNumberOfPlatforms();

      // System.out.println("lNumberOfPlatforms=" + lNumberOfPlatforms);

      for (int p = 0; p < lNumberOfPlatforms; p++)
      {
        final ClearCLPlatform lPlatform = lClearCL.getPlatform(p);

        // System.out.println(lPlatform.getInfoString());

        for (int d = 0; d < lPlatform.getNumberOfDevices(); d++)
        {
          final ClearCLDevice lClearClDevice = lPlatform.getDevice(d);

          /*System.out.println("\t" + d
                             + " -> \n"
                             + lClearClDevice.getInfoString());/**/

          final ClearCLContext lContext =
                                        lClearClDevice.createContext();

          testReadFromFragmentedMemory(lContext);

        }

      }
    }
  }

  private void testReadFromFragmentedMemory(final ClearCLContext lContext)
  {

    final ClearCLImage lImage =
                              lContext.createImage(HostAccessType.ReadWrite,
                                                   KernelAccessType.ReadWrite,
                                                   ImageChannelOrder.Intensity,
                                                   ImageChannelDataType.Float,
                                                   10,
                                                   10,
                                                   10);

    OffHeapMemory lMemory =
                          OffHeapMemory.allocateFloats(10 * 10 * 10);

    for (int i = 0; i < 10 * 10 * 10; i++)
      lMemory.setFloatAligned(i, i);

    FragmentedMemory lFragmentedMemory =
                                       FragmentedMemory.split(lMemory,
                                                              10);

    lImage.readFrom(lFragmentedMemory,
                    Region3.originZero(),
                    Region3.region(10, 10, 10),
                    true);

    OffHeapMemory lMemory2 =
                           OffHeapMemory.allocateFloats(10 * 10 * 10);

    lImage.writeTo(lMemory2, true);

    for (int i = 0; i < 10 * 10 * 10; i++)
      assertEquals((double) i,
                   (double) lMemory2.getFloatAligned(i),
                   0.01);

  }

}
