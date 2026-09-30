# DocSDK Sample

Một app Android dùng DocSDK giống như một app của người tích hợp: chỉ qua Maven (`com.editor:docsdk`), không có mã nguồn SDK.

- `MainActivity`: chọn tài liệu, mở bằng màn xem của SDK (`DocumentViewer`), mở trong màn của app (`DocumentView`), chuyển PDF sang Word (`DocumentTools`).
- `EmbeddedViewerActivity`: nhúng `DocumentView` dưới một dòng số trang.
- `JavaExample.java`: dùng SDK từ Java.
- `androidTest/SdkIntegrationTest`: chạy qua mọi API công khai trên máy thật.

Chạy (cần `../../sdk-repo` đã được publish):

```sh
../../gradlew installDebug                 # cài app mẫu
../../gradlew connectedDebugAndroidTest    # chạy test tích hợp
```

Hướng dẫn đầy đủ: `android_document/docs/HUONG_DAN_TICH_HOP.md`.
