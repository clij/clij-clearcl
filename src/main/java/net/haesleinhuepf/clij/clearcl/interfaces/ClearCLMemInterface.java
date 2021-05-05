package net.haesleinhuepf.clij.clearcl.interfaces;

import net.haesleinhuepf.clij.clearcl.ClearCLContext;
import net.haesleinhuepf.clij.clearcl.ClearCLQueue;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.enums.MemAllocMode;
import net.haesleinhuepf.clij.coremem.interfaces.SizedInBytes;

/**
 * Interface for all mem objects
 *
 * @author royer
 */
public interface ClearCLMemInterface extends SizedInBytes
{

  /**
   * Adds listener to this mem object.
   * 
   * @param pListener
   *          listener
   */
  public void addListener(ClearCLMemChangeListener pListener);

  /**
   * Calling this method notifies listeners that the contents of this OpenCL
   * object might have changed.
   * 
   * @param pQueue ?
   */
  void notifyListenersOfChange(ClearCLQueue pQueue);

  /**
   * Returns ClearCL context associated to this OpenCL mem object.
   * 
   * @return context
   */
  public ClearCLContext getContext();

  /**
   * Returns memory allocation mode
   * 
   * @return memory allocation mode
   */
  public MemAllocMode getMemAllocMode();

  /**
   * Returns host access type
   * 
   * @return host acess type
   */
  public HostAccessType getHostAccessType();

  /**
   * Returns kernel access type of this buffer.
   * 
   * @return kernel access type
   */
  public KernelAccessType getKernelAccessType();

}
