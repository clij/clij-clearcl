package net.haesleinhuepf.clij.clearcl.backend.javacl.test;

import static org.junit.Assert.assertTrue;

import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.backend.jocl.ClearCLBackendJOCL;

import org.junit.Test;

/**
 * 
 *
 * @author royer
 */
public class ClearCLBackendJavaCLTests
{

  /**
   * 
   */
  @Test
  public void test()
  {
    ClearCLBackendInterface lClearCLBackend =
                                            new ClearCLBackendJOCL();

    // System.out.println(lClearCLBackendJavaCL.getNumberOfPlatforms());
    assertTrue(lClearCLBackend.getNumberOfPlatforms() > 0);

  }

}
