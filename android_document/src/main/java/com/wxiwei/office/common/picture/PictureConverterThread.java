package com.wxiwei.office.common.picture;

import com.wxiwei.office.system.DocumentCoroutines;
import kotlinx.coroutines.Job;

public class PictureConverterThread implements PictureConversionTask
{
	public  PictureConverterThread(PictureConverterMgr converterMgr, String srcPath, String dstPath, String type )
    {
        this.converterMgr = converterMgr;
        this.type = type;
        
        this.sourPath = srcPath;
        this.destPath = dstPath;
    }
    
    private Job job;

    @Override public Job start()
    {
        if (job != null && job.isActive()) return job;
        job = DocumentCoroutines.launch(converterMgr.getControl(), new Runnable()
        {
            @Override public void run()
            {
                converterMgr.convertPNG(sourPath, destPath, type, false);
            }
        });
        return job;
    }   

    @Override public void cancel()
    {
        if (job != null) job.cancel(null);
    }
    
    
    private PictureConverterMgr converterMgr;
    private String type;
    private String sourPath;
    private String destPath;
}
