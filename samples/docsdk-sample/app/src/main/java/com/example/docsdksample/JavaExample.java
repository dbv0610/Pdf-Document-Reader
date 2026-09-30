package com.example.docsdksample;

import android.content.Context;
import android.util.Log;

import com.editor.docsdk.DocumentException;
import com.editor.docsdk.DocumentTools;
import com.editor.docsdk.DocumentType;
import com.editor.docsdk.DocumentViewer;

import java.io.File;

/** DocSDK from Java: the viewer is one static call, the tools call back on the main thread. */
public final class JavaExample {

    private JavaExample() {
    }

    public static void openInViewer(Context context, File file) {
        DocumentViewer.open(context, file, new DocumentViewer.Options("Hợp đồng", null));
    }

    public static DocumentTools.Task compress(Context context, File pdf, File output) {
        DocumentTools tools = new DocumentTools(context);
        return tools.compressPdfAsync(pdf, output, DocumentTools.Compression.MEDIUM, null,
                new DocumentTools.Callback<Integer>() {
                    @Override
                    public void onSuccess(Integer changedPictures) {
                        Log.d("DocSDK", "Compressed, pictures made smaller: " + changedPictures);
                    }

                    @Override
                    public void onError(DocumentException error) {
                        Log.w("DocSDK", "Cannot compress: " + error.getReason(), error);
                    }
                });
    }

    public static boolean isSupported(String fileName) {
        return DocumentType.fromFileName(fileName) != null;
    }
}
