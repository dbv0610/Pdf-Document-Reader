/*
 * 文件名称:          PictureConverterThread.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:30:35
 */
package com.wxiwei.office.common.picture;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.wxiwei.office.constant.EventConstant;
import com.wxiwei.office.system.IControl;
import com.wxiwei.office.thirdpart.emf.util.EMFUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import kotlinx.coroutines.Job;

/**
 * TODO: 文件注释
 * <p>
 * <p>
 * Read版本:        Read V1.0
 * <p>
 * 作者:            jqin
 * <p>
 * 日期:            2013-4-25
 * <p>
 * 负责人:           jqin
 * <p>
 * 负责小组:           
 * <p>
 * <p>
 */
public class PictureConverterMgr
{

    public PictureConverterMgr(IControl control)
    {
        this.control = control;
        convertingThread = new ArrayList<PictureConversionTask>();
        convertingPictPathMap = new HashMap<String, PictureConversionTask>();
        vectorgraphViews = new HashMap<String, List<Integer>>();
        viewVectorgraphs = new HashMap<Integer, List<String>>();
    }
    
    public void setControl(IControl control)
    {
        this.control = control;
    }

    /** The document this converter works for; its coroutine scope runs the conversions. */
    public IControl getControl()
    {
        return control;
    }    
    
    public synchronized void addConvertPicture(int viewIndex, byte type , String srcPath, String dstPath, int width, int height, boolean singleThread)
    {
        control.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, true);
        
        if(singleThread)
        {
        	convertWMF_EMF(type, srcPath, dstPath, width, height, true);
            if(isIdle())
            {
                control.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false);
            }
        }
        else
        {
    		VectorgraphConverterThread thread = new VectorgraphConverterThread(this, type, srcPath, dstPath, width, height);        
    		
    		convertingThread.add(thread);
    		convertingPictPathMap.put(dstPath, thread);
            
            List<Integer> listIndex = new ArrayList<Integer>();
            listIndex.add(viewIndex);
            vectorgraphViews.put(dstPath, listIndex);
            
            if(viewVectorgraphs.get(viewIndex) == null)
            {
            	List<String> listPath = new ArrayList<String>();
            	listPath.add(dstPath);
                viewVectorgraphs.put(viewIndex, listPath);
            }
            else
            {
            	viewVectorgraphs.get(viewIndex).add(dstPath);
            }
            
            if(convertingThread.size() == 1)
            {
            	//is not converting now, so start the thread
            	convertingThread.get(convertingThread.size() - 1).start();
            }
        }
    }
    
    public void convertWMF_EMF(byte type, String sourPath, String destPath, int picWidth, int picHeight, boolean thumbnail)
    {
    	 try
         {
    		 Bitmap sBitmap = null;
             if(type == Picture.WMF)
             {
                 // WMF needs the native converter that shipped with the removed PDF module; not rendered
             }
             else if(type == Picture.EMF)
             {
            	 sBitmap = EMFUtil.convert(sourPath, destPath, picWidth, picHeight);
             }
             
             if(control != null && ((!thumbnail && !isConversionActive(destPath)) || control.getView() == null))
             {
            	 //has disposed
            	 return;
             }
             
             if(sBitmap != null)
             {
            	 control.getSysKit().getPictureManage().addBitmap(destPath, sBitmap);
                 finishConversion(destPath, thumbnail);
                 
                 if(!thumbnail)
                 {
                	 control.actionEvent(EventConstant.TEST_REPAINT_ID, null);
                 }
             }
             else
             {
            	 finishConversion(destPath, thumbnail);
             }
         }
    	 catch(OutOfMemoryError e)
    	 {
    		 if(control.getSysKit().getPictureManage().hasBitmap())
             {
                 control.getSysKit().getPictureManage().clearBitmap();
                 convertWMF_EMF(type, sourPath, destPath, picWidth, picHeight, thumbnail);
             }
    		 else
    		 {
    			 control.getSysKit().getErrorKit().writerLog(e);
    			 finishConversion(destPath, thumbnail);
    		 }
    	 }
         catch(Exception e)
         {
        	 if(control != null && ((!thumbnail && !isConversionActive(destPath)) || control.getView() == null))
             {
            	 //has disposed
            	 return;
             }
        	 
        	 control.getSysKit().getErrorKit().writerLog(e);
             finishConversion(destPath, thumbnail);
         }
    }
    
    
    public synchronized void addConvertPicture(int viewIndex, String srcPath, String dstPath, String picType, boolean singleThread)
    {
        control.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, true);
        
        if(singleThread)
        {
        	convertPNG(srcPath, dstPath, picType, true);
            if(isIdle())
            {
                control.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false);
            }
        }
        else
        {
        	PictureConverterThread thread = new PictureConverterThread(this, srcPath, dstPath, picType);        
    		
    		convertingThread.add(thread);
    		convertingPictPathMap.put(dstPath, thread);
            
            List<Integer> listIndex = new ArrayList<Integer>();
            listIndex.add(viewIndex);
            vectorgraphViews.put(dstPath, listIndex);
            
            if(viewVectorgraphs.get(viewIndex) == null)
            {
            	List<String> listPath = new ArrayList<String>();
            	listPath.add(dstPath);
                viewVectorgraphs.put(viewIndex, listPath);
            }
            else
            {
            	viewVectorgraphs.get(viewIndex).add(dstPath);
            }
            
            if(convertingThread.size() == 1)
            {
            	//is not converting now, so start the thread
            	convertingThread.get(convertingThread.size() - 1).start();
            }
        }
    }
    
    public void convertPNG(String sourPath, String destPath, String picType, boolean thumbnail)
    {
    	 try
         {
    		 // the native PNG converter shipped with the removed PDF module
    		 boolean ret = false;
             
             if(control != null && ((!thumbnail && !isConversionActive(destPath)) || control.getView() == null))
             {
            	 //has disposed
            	 return;
             }
             
             if(ret)
             {
            	 InputStream in = new FileInputStream(destPath);
            	 Bitmap sBitmap = BitmapFactory.decodeStream(in, null, null);
            	 if(sBitmap != null)
            	 {
            		 control.getSysKit().getPictureManage().addBitmap(destPath, sBitmap);
                     finishConversion(destPath, thumbnail);
                     
                     if(!thumbnail)
                     {
                    	 control.actionEvent(EventConstant.TEST_REPAINT_ID, null);
                     }
            	 }
            	 else
            	 {
            		 finishConversion(destPath, thumbnail);
            	 }
             }
             else
             {
            	 finishConversion(destPath, thumbnail);
             }
         }
    	 catch(OutOfMemoryError e)
    	 {
    		 if(control.getSysKit().getPictureManage().hasBitmap())
             {
                 control.getSysKit().getPictureManage().clearBitmap();
                 convertPNG(sourPath, destPath, picType, thumbnail);
             }
    		 else
    		 {
    			 control.getSysKit().getErrorKit().writerLog(e);
    			 finishConversion(destPath, thumbnail);
    		 }
    	 }
         catch(Exception e)
         {
        	 if(control != null && ((!thumbnail && !isConversionActive(destPath)) || control.getView() == null))
             {
            	 //has disposed
            	 return;
             }
        	 
        	 control.getSysKit().getErrorKit().writerLog(e);
             finishConversion(destPath, thumbnail);
         }
    }
    
    /**
     * 
     * @param path
     */
    public void remove(String path)
    {
        List<Integer> updateViewList = null;
        boolean allConverted;
        // Read before locking: it walks the view tree, which has locks of its own. The document
        // may be disposed by now (PGControl's view is then gone), which must not kill this thread.
        int currentViewIndex;
        try
        {
            currentViewIndex = control.getCurrentViewIndex();
        }
        catch(Exception e)
        {
            currentViewIndex = -1;
        }
        // Collected under the lock, sent after it: the events reach the views (exportImage draws),
        // and a drawing thread holds PictureKit's lock while it asks isPictureConverting.
        synchronized(this)
        {
            if(convertingPictPathMap == null)
            {
                return;
            }
            PictureConversionTask thread = convertingPictPathMap.remove(path);
            convertingThread.remove(thread);

            List<Integer> viewList = vectorgraphViews.remove(path);
            if(viewList != null)
            {
                for(int i = 0; i < viewList.size(); i++)
                {
                    int viewIndex = viewList.get(i);
                    List<String> vectorgraphs = viewVectorgraphs.get(viewIndex);
                    if(vectorgraphs == null)
                    {
                        continue;
                    }
                    vectorgraphs.remove(path);
                    if(vectorgraphs.size() == 0)
                    {
                        //all vector graphs contained in this view have been converted
                        //so notify to update this view
                        viewVectorgraphs.remove(viewIndex);

                        if(updateViewList == null)
                        {
                            updateViewList = new ArrayList<Integer>();
                        }

                        updateViewList.add(viewIndex);
                    }
                }
            }

            if(convertingThread.size() > 0)
            {
                //check current view vector graphs
                List<String> vectorgraphs = viewVectorgraphs.get(currentViewIndex);
                PictureConversionTask next = vectorgraphs != null && vectorgraphs.size() > 0
                    ? convertingPictPathMap.get(vectorgraphs.get(0)) : null;
                if(next == null)
                {
                    //start the last vector graph converting thread
                    next = convertingThread.get(convertingThread.size() - 1);
                }
                next.start();
            }
            allConverted = convertingPictPathMap.size() == 0;
        }

        if(updateViewList != null && updateViewList.size() > 0)
        {
            if(updateViewList.contains(currentViewIndex))
            {
                control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null);
            }

            control.actionEvent(EventConstant.SYS_VECTORGRAPH_PROGRESS, updateViewList);
        }

        if(allConverted)
        {
            control.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false);
        }
    }

    /**
     * Ends a conversion. [sync] ones (singleThread, the "thumbnail" flag of convertWMF_EMF and
     * convertPNG) ran inside addConvertPicture and were never queued, so there is nothing to remove.
     */
    private void finishConversion(String destPath, boolean sync)
    {
        if(!sync)
        {
            remove(destPath);
        }
    }

    private synchronized boolean isIdle()
    {
        return convertingPictPathMap == null || convertingPictPathMap.isEmpty();
    }

    /** False once dispose() or remove() dropped the conversion to [destPath]. */
    private synchronized boolean isConversionActive(String destPath)
    {
        return convertingPictPathMap != null && convertingPictPathMap.get(destPath) != null;
    }

    public synchronized boolean hasConvertingVectorgraph(int viewIndex)
    {
        return viewVectorgraphs.containsKey(viewIndex);
    }

    /**
     * 
     * @param path
     * @return
     */
    public synchronized boolean isPictureConverting(String path)
    {
        return vectorgraphViews.containsKey(path);
    }

    /**
     * call when vector graph has been converted, but with different view index
     * eg. different pages has same vector graph
     * @param path
     * @param viewIndex
     */
    public synchronized void appendViewIndex(String path, int viewIndex)
    {
        List<Integer> views = vectorgraphViews.get(path);
        if(views != null)
        {
            views.add(viewIndex);

            if(viewVectorgraphs.get(viewIndex) == null)
            {
                List<String> listPath = new ArrayList<String>();
                listPath.add(path);
                viewVectorgraphs.put(viewIndex, listPath);
            }
            else
            {
                viewVectorgraphs.get(viewIndex).add(path);
            }
        }
    }

    public synchronized void dispose()
    {
    	if(convertingPictPathMap != null)
        {
            Iterator<PictureConversionTask> iter = convertingPictPathMap.values().iterator();
            while(iter.hasNext())
            {
                try
                {
                    iter.next().cancel();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                
            }
            convertingPictPathMap.clear();
            
            vectorgraphViews.clear();
            viewVectorgraphs.clear();
        }
    }
    private IControl control;
    //last-in，first-out
    private List<PictureConversionTask> convertingThread;
    //
    private Map<String, PictureConversionTask> convertingPictPathMap;
    //vector graph path and view indexs which contains this vector graph
    private Map<String, List<Integer>> vectorgraphViews;
    //view index and vector graphs which is contained in this view
    private Map<Integer, List<String>> viewVectorgraphs;
}
