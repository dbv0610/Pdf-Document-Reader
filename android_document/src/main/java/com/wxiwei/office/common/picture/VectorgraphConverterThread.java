/*
 * 文件名称:          PictureConverterThread.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:33:03
 */
package com.wxiwei.office.common.picture;


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
import com.wxiwei.office.system.DocumentCoroutines;
import kotlinx.coroutines.Job;

public class VectorgraphConverterThread implements PictureConversionTask
{
    public  VectorgraphConverterThread(PictureConverterMgr converterMgr, byte type , String srcPath, String dstPath, int width, int height)
    {
        this.converterMgr = converterMgr;
        this.type = type;
        
        this.sourPath = srcPath;
        this.destPath = dstPath;
        this.picWidth = width;
        this.picHeight = height;
    }
    
    private Job job;

    @Override public Job start()
    {
        if (job != null && job.isActive()) return job;
        job = DocumentCoroutines.launch(converterMgr.getControl(), new Runnable()
        {
            @Override public void run()
            {
                converterMgr.convertWMF_EMF(type, sourPath, destPath, picWidth, picHeight, false);
            }
        });
        return job;
    }   

    @Override public void cancel()
    {
        if (job != null) job.cancel(null);
    }
    
    
    private PictureConverterMgr converterMgr;
    private byte type;
    private String sourPath;
    private String destPath;
    private int picWidth;
    private int picHeight;
}
