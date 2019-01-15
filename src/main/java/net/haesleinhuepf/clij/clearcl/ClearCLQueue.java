package net.haesleinhuepf.clij.clearcl;

import net.haesleinhuepf.clij.clearcl.abs.ClearCLBase;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import coremem.rgc.Cleanable;
import coremem.rgc.Cleaner;
import coremem.rgc.RessourceCleaner;

/**
 * ClearCLQueue is the ClearCL abstraction for OpenCl queues.
 *
 * @author royer
 */
public class ClearCLQueue extends ClearCLBase implements Cleanable
{

  private ClearCLContext mClearCLContext;

  // This will register this buffer for GC cleanup
  {
    RessourceCleaner.register(this);
  }

  /**
   * This constructor is called internally from an OpenCl context.
   * 
   * @param pClearCLContext
   *          context
   * @param pQueuePointer
   *          queue peer pointer
   */
  public ClearCLQueue(ClearCLContext pClearCLContext,
                      ClearCLPeerPointer pQueuePointer)
  {
    super(pClearCLContext.getBackend(), pQueuePointer);
    mClearCLContext = pClearCLContext;
  }

  /**
   * Returns this queues context.
   * 
   * @return context
   */
  public ClearCLContext getContext()
  {
    return mClearCLContext;
  }

  /**
   * Waits for queue to finish enqueued tasks (such as: kernel execution, buffer
   * and image copies, writes and reads).
   */
  public void waitToFinish()
  {
    getBackend().waitQueueToFinish(getPeerPointer());
  }

  @Override
  public void close()
  {
    if (getPeerPointer() != null)
    {
      if (mQueueCleaner != null)
        mQueueCleaner.mClearCLPeerPointer = null;
      getBackend().releaseQueue(getPeerPointer());
      setPeerPointer(null);
    }
  }

  // NOTE: this _must_ be a static class, otherwise instances of this class will
  // implicitely hold a reference of this image...
  private static class QueueCleaner implements Cleaner
  {
    public ClearCLBackendInterface mBackend;
    public ClearCLPeerPointer mClearCLPeerPointer;

    public QueueCleaner(ClearCLBackendInterface pBackend,
                        ClearCLPeerPointer pClearCLPeerPointer)
    {
      mBackend = pBackend;
      mClearCLPeerPointer = pClearCLPeerPointer;
    }

    @Override
    public void run()
    {
      try
      {
        if (mClearCLPeerPointer != null)
          mBackend.releaseQueue(mClearCLPeerPointer);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  QueueCleaner mQueueCleaner = new QueueCleaner(getBackend(),
                                                getPeerPointer());

  @Override
  public Cleaner getCleaner()
  {
    return mQueueCleaner;
  }
}
