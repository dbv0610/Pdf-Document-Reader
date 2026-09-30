# DocSDK: build và phát hành (dành cho chủ sở hữu mã nguồn)

Tài liệu cho người giữ mã nguồn module `android_document`. Người tích hợp chỉ cần [HUONG_DAN_TICH_HOP.md](HUONG_DAN_TICH_HOP.md).

## Cấu trúc

| Phần | Vị trí |
|---|---|
| API công khai (giữ tên) | `src/main/java/com/editor/docsdk/`: `DocumentViewer`, `DocumentViewerActivity`, `DocumentView`, `DocumentTools`, `DocumentType`, `DocumentException` |
| Engine (bị làm rối khi phát hành) | `com.wxiwei.office.*`, `com.reader.pdfviewer.*`, phần nội bộ của `docsdk` |
| Chuỗi của màn xem | `res/values/docsdk_strings.xml`, `res/values-vi/docsdk_strings.xml` |
| Luật R8 đi kèm AAR (áp dụng cả trong app tích hợp) | `consumer-rules.pro` |
| Luật R8 chỉ khi build SDK | `proguard-rules.pro` |
| Thông báo giấy phép bên thứ ba | `THIRD_PARTY_NOTICES.md` |
| App mẫu, cũng là bài kiểm tra tích hợp | `samples/docsdk-sample/` (project Gradle riêng) |

Toạ độ Maven và phiên bản khai báo ở đầu `android_document/build.gradle.kts`:

```kotlin
group = "com.editor"
version = "1.0.0"
```

## Phát hành một phiên bản

1. **Tăng `version`** trong `android_document/build.gradle.kts`. Quy ước: vá lỗi thì tăng số cuối (1.0.1); thêm API thì tăng số giữa (1.1.0); đổi hoặc xoá API công khai thì tăng số đầu (2.0.0).

2. **Build và publish ra thư mục `sdk-repo`:**

   ```sh
   ./gradlew :android_document:publishReleasePublicationToSdkRepository
   ```

   Kết quả nằm ở `sdk-repo/com/editor/docsdk/<version>/`: file `.aar` (đã làm rối, không có mã nguồn), `.pom` và `.module` (danh sách thư viện phụ thuộc), kèm checksum.

3. **Kiểm tra bằng app mẫu.** Sửa phiên bản trong `samples/docsdk-sample/app/build.gradle.kts`, cắm máy Android rồi chạy:

   ```sh
   cd samples/docsdk-sample
   ../../gradlew --refresh-dependencies assembleRelease connectedDebugAndroidTest
   ```

   `SdkIntegrationTest` chạy qua mọi hàm công khai trên AAR đã làm rối, trong một app cũng bật R8. Bài test này pass nghĩa là luật R8 đủ và API dùng được đúng như người tích hợp sẽ dùng.

4. **Lưu file mapping** `android_document/build/outputs/mapping/release/mapping.txt` cùng phiên bản, ví dụ vào `mappings/docsdk-<version>.txt`, ở chỗ riêng tư. Không gửi file này cho người tích hợp. Khi họ báo lỗi có stack trace bị làm rối, giải mã bằng lệnh:

   ```sh
   $ANDROID_HOME/cmdline-tools/latest/bin/retrace mappings/docsdk-1.0.0.txt stacktrace.txt
   ```

5. **Giao cho người tích hợp**, theo một trong hai cách:
   - **Thư mục:** nén thư mục `sdk-repo`, gửi kèm `docs/HUONG_DAN_TICH_HOP.md` và `THIRD_PARTY_NOTICES.md`.
   - **GitHub Packages riêng tư:**

     ```sh
     export GITHUB_ACTOR=<tài khoản> GITHUB_TOKEN=<token có quyền write:packages>
     ./gradlew :android_document:publishReleasePublicationToGithubRepository -Pdocsdk.githubRepo=<owner>/<repo>
     ```

     Sau đó cấp quyền đọc gói cho tài khoản của người tích hợp.

## Thay đổi API công khai

- Chỉ thêm code vào `com.editor.docsdk` khi thật sự muốn người tích hợp dùng. Mọi thứ `public` ở đây đều được giữ tên và trở thành cam kết.
- Code hỗ trợ của SDK thì để `internal` hoặc `private`. Khi đó R8 làm rối nó như phần engine.
- Giữ tương thích Java: dùng `@JvmOverloads` cho tham số mặc định, `@JvmStatic` trong `object`, và mỗi hàm `suspend` mới cần có bản `...Async` tương ứng.
- Sau khi đổi API, cập nhật mục 13 của `HUONG_DAN_TICH_HOP.md` và `SdkIntegrationTest`.

## Khi engine có code dùng reflection hoặc JNI mới

R8 đổi tên và xoá những gì nó cho là không dùng tới. Code tìm class hoặc field **theo tên** (`Class.forName`, `getDeclaredField`, `getMethod`, JNI `FindClass`/`GetFieldID`) phải có luật `-keep` trong `consumer-rules.pro`, **không phải** `proguard-rules.pro`, vì app tích hợp cũng chạy R8 lần nữa. Dấu hiệu thiếu luật: `ClassNotFoundException`, `NoSuchFieldException`, `NoSuchMethodError` hoặc crash trong native code, xảy ra ở bản release nhưng không có ở debug. Bài test của app mẫu sẽ bắt được lỗi này.

## Tương thích

| | Giá trị | Đặt ở đâu |
|---|---|---|
| Kotlin tối thiểu của app | 2.3.20 | `languageVersion`/`apiVersion` 2.3, `coreLibrariesVersion` trong khối `kotlin {}` |
| `compileSdk` tối thiểu của app | 36 | `aarMetadata.minCompileSdk` |
| `minSdk` | 26 | `defaultConfig.minSdk` |

Tăng Kotlin tối thiểu thì người tích hợp dùng Kotlin cũ sẽ không build được nữa. Chỉ tăng khi cần, và ghi rõ trong ghi chú phát hành.
