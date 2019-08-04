package net.haesleinhuepf.clij.clearcl.recycling;

import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.coremem.recycling.RecyclerRequestInterface;

/**
 * Recycling request for generating a recyclable peer pointer of a given type.
 *
 */
public class ClearCLRecyclableRequest implements
        RecyclerRequestInterface
{
  public ClearCLDevice mDevice;
  public Class<?> mClass;

  /**
   * Constructs a request given a device and a type (class)
   * 
   * @param pDevice
   *          device
   * @param pClass
   *          type
   */
  public ClearCLRecyclableRequest(ClearCLDevice pDevice,
                                  Class<?> pClass)
  {
    mDevice = pDevice;
    mClass = pClass;
  }
}
