package net.haesleinhuepf.clij.clearcl.test;

import static org.junit.Assert.assertTrue;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackends;
import net.haesleinhuepf.clij.clearcl.selector.BadDeviceSelector;
import net.haesleinhuepf.clij.clearcl.selector.DeviceTypeSelector;
import net.haesleinhuepf.clij.clearcl.selector.FastestDeviceSelector;
import net.haesleinhuepf.clij.clearcl.selector.GlobalMemorySelector;

import org.junit.Test;

/**
 * Test 'best device' functionality
 *
 * @author royer
 */
public class ClearCLBestDeviceTests
{

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

      {
        ClearCLDevice lClearClDevice =
                                     lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                            BadDeviceSelector.NotIntegratedIntel,
                                                            GlobalMemorySelector.MAX);

        System.out.println(lClearClDevice);
        assertTrue(lClearClDevice != null);
      }

      {
        ClearCLDevice lClearClDevice =
                                     lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                            BadDeviceSelector.NotIntegratedIntel,
                                                            FastestDeviceSelector.FastestForImages);

        System.out.println(lClearClDevice);
        assertTrue(lClearClDevice != null);
      }

      {
        ClearCLDevice lClearClDevice =
                                     lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                            FastestDeviceSelector.FastestForImages);

        System.out.println(lClearClDevice);
        assertTrue(lClearClDevice != null);
      }

      {
        ClearCLDevice lClearClDevice =
                                     lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                            FastestDeviceSelector.FastestForBuffers);

        System.out.println(lClearClDevice);
        assertTrue(lClearClDevice != null);
      }
    }
  }

}
