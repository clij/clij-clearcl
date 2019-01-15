package net.haesleinhuepf.clij.clearcl.backend.jocl.test.jocl;

import static org.junit.Assert.assertTrue;

import net.haesleinhuepf.clij.clearcl.backend.jocl.ClearCLBackendJOCL;

import org.junit.Test;

/**
 *
 *
 * @author royer
 */
public class ClearCLBackendJOCLTests
{

  /**
   * Basic test
   */
  @Test
  public void test()
  {
    ClearCLBackendJOCL lClearCLBackendJOCL = new ClearCLBackendJOCL();

    assertTrue(lClearCLBackendJOCL.getNumberOfPlatforms() > 0);
  }

}
