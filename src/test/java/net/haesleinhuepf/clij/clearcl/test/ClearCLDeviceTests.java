package net.haesleinhuepf.clij.clearcl.test;

import static org.junit.Assert.assertTrue;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackends;
import net.haesleinhuepf.clij.clearcl.selector.BadDeviceSelector;
import net.haesleinhuepf.clij.clearcl.selector.DeviceTypeSelector;
import net.haesleinhuepf.clij.clearcl.selector.GlobalMemorySelector;

import org.junit.Test;

/**
 * Basic Kernel tests
 *
 * @author royer
 */
public class ClearCLDeviceTests
{

  private void testDeviceVersion_Impl(ClearCLBackendInterface pClearCLBackendInterface) throws Exception
  {
    ClearCL lClearCL = new ClearCL(pClearCLBackendInterface);

    ClearCLDevice lClearClDevice =
                                 lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                        BadDeviceSelector.NotIntegratedIntel,
                                                        GlobalMemorySelector.MAX);

    final double version = lClearClDevice.getVersion();
    assertTrue(version > 0.);

  }

  /**
   * Test with best backend
   *
   * @throws Exception
   *           NA
   */
  @Test
  public void testDeviceVersion() throws Exception
  {
    ClearCLBackendInterface lClearCLBackendInterface =
                                                     ClearCLBackends.getBestBackend();

    testDeviceVersion_Impl(lClearCLBackendInterface);
  }

}
