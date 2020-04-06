package net.haesleinhuepf.clij.clearcl.abs;

import java.util.concurrent.CopyOnWriteArrayList;

import net.haesleinhuepf.clij.clearcl.ClearCLPeerPointer;
import net.haesleinhuepf.clij.clearcl.ClearCLQueue;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.enums.MemAllocMode;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLMemChangeListener;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLMemInterface;

/**
 * Base class providing common fields and methods for all mem objects.
 *
 * @author royer
 */
public abstract class ClearCLMemBase extends ClearCLBase
                                     implements ClearCLMemInterface
{

  private final MemAllocMode mMemAllocMode;
  private final HostAccessType mHostAccessType;
  private final KernelAccessType mKernelAccessType;

  private CopyOnWriteArrayList<ClearCLMemChangeListener> mListener =
                                                                   new CopyOnWriteArrayList<>();

  /**
   * Constructs the abstract class for all images and buffers.
   * 
   * @param pClearCLBackendInterface
   *          backend
   * @param pPointer
   *          peer pointer
   * @param pMemAllocMode
   *          mem allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   */
  public ClearCLMemBase(ClearCLBackendInterface pClearCLBackendInterface,
                        ClearCLPeerPointer pPointer,
                        MemAllocMode pMemAllocMode,
                        HostAccessType pHostAccessType,
                        KernelAccessType pKernelAccessType)
  {
    super(pClearCLBackendInterface, pPointer);
    mMemAllocMode = pMemAllocMode;
    mHostAccessType = pHostAccessType;
    mKernelAccessType = pKernelAccessType;
  }

  /**
   * Adds an image or buffer change listener.
   * 
   * @param pListener
   *          listener
   */
  @Override
  public void addListener(ClearCLMemChangeListener pListener)
  {
    mListener.add(pListener);
  }

  /**
   * Removes an image or buffer change listener.
   * 
   * @param pListener
   *          listener
   */
  public void removeListener(ClearCLMemChangeListener pListener)
  {
    mListener.remove(pListener);
  }

  /* (non-Javadoc)
   * @see ClearCLMemInterface#notifyListenersOfChange(ClearCLQueue)
   */
  @Override
  public void notifyListenersOfChange(ClearCLQueue pQueue)
  {
    for (ClearCLMemChangeListener lClearCLMemChangeListener : mListener)
    {
      lClearCLMemChangeListener.change(pQueue, this);
    }
  }

  @Override
  public MemAllocMode getMemAllocMode()
  {
    return mMemAllocMode;
  }

  /**
   * Returns host access type
   * 
   * @return host acess type
   */
  @Override
  public HostAccessType getHostAccessType()
  {
    return mHostAccessType;
  }

  /**
   * Returns kernel access type
   * 
   * @return kernel access type
   */
  @Override
  public KernelAccessType getKernelAccessType()
  {
    return mKernelAccessType;
  }

  @Override
  public String toString()
  {
    return String.format("ClearCLMemBase [mMemAllocMode=%s, mHostAccessType=%s, mKernelAccessType=%s, mListener=%s, getBackend()=%s, getPeerPointer()=%s]",
                         mMemAllocMode,
                         mHostAccessType,
                         mKernelAccessType,
                         mListener,
                         getBackend(),
                         getPeerPointer());
  }

}
