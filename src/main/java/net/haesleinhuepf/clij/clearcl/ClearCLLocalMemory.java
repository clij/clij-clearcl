package net.haesleinhuepf.clij.clearcl;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.coremem.interfaces.SizedInBytes;
import net.haesleinhuepf.clij.coremem.util.Size;

/**
 * ClearCLLocalMemory is the ClearCL abstraction for local memory passed to
 * kernels.
 *
 * @author royer
 */
public class ClearCLLocalMemory implements SizedInBytes
{
  private NativeTypeEnum mNativeTypeEnum;
  private long mNumberOfElements;

  /**
   * Instantiates a local memory object,
   * 
   * @param pNativeTypeEnum
   *          native type
   * @param pNumberOfElements
   *          number of elements
   */
  public ClearCLLocalMemory(NativeTypeEnum pNativeTypeEnum,
                            long pNumberOfElements)
  {

    mNativeTypeEnum = pNativeTypeEnum;
    mNumberOfElements = pNumberOfElements;
  }

  @Override
  public long getSizeInBytes()
  {
    return Size.of(mNativeTypeEnum) * mNumberOfElements;
  }

}