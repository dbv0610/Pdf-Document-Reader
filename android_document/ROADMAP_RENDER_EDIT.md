# Lộ trình: đọc/render DOCX · XLSX · PPTX chuẩn hơn → realtime edit (module `android_document`)

> Tạo ngày 26/09/2026, dựa trên 3 file mẫu đã giải nén ở `DataExtractor/{docx,xlsx,pptx}` và code hiện tại của
> `android_document/src/main/java/com/wxiwei/office`. Mọi đường dẫn code bên dưới tính từ thư mục đó.
>
> Cách dùng file: làm lần lượt từng task, tick `[x]` khi xong, ghi 1 dòng vào **Nhật ký** (mục 12).
> Mỗi task có **Sửa ở đâu → Làm gì → Xong khi nào**. Mã task (D1, X3, P2…) dùng để ghi commit/nhật ký.

---

## 0. Kết luận nhanh

- **Khả thi.** Engine (wxiwei/aOffice) đã có đủ khung: đọc OOXML bằng dom4j, mô hình thuộc tính `AttrManage`,
  layout riêng cho Word (`wp/view`), Excel (`ss/view`), PowerPoint (`pg/view`). Lớp editor cũng đã có
  (`editor/docx`, `editor/xlsx`, `editor/pptx`). Việc cần làm là **lấp các chỗ reader bỏ qua / hard-code**,
  không cần viết lại engine.
- Phần lớn lỗi hiển thị sai trên 3 file mẫu đến từ **~15 chỗ cụ thể** (mục 2). Sửa nhóm P0 là thấy khác biệt rõ.
- Realtime edit: **XLSX và PPTX đã có phiên sửa live** (`SheetEditSession`, `LivePptxSession`) — chỉ cần mở rộng.
  **DOCX hiện chỉ "xếp hàng thao tác rồi lưu"** (`DocxEditor`) — realtime cho DOCX là phần khó nhất, làm cuối.
- ~~Điều kiện tiên quyết: build xanh~~ → **đã xanh** (26/09: `compileDebugKotlin`, `compileDebugJavaWithJavac`,
  `:app:compileDevDebugKotlin` đều pass; số 0 lỗi cũ trong `.kotlin-fix/build_history.txt` là do lỗi cấu hình, build lại thật thì pass).

### Cách chạy test render (dùng sau mỗi task)
```bash
./gradlew :android_document:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.wxiwei.office.render.SampleRenderTest \
  -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true
adb pull /sdcard/Android/data/com.wxiwei.office.test/files/render ./render_out   # docx_NN / pptx_NN / xlsx_sheetN.png
```
Test: `src/androidTest/java/com/wxiwei/office/render/SampleRenderTest.kt`, file mẫu: `src/androidTest/assets/samples/`
(`sample.pptx` đã giảm ảnh xuống ≤1280px, 7 MB; bản gốc 34 MB vẫn ở `DataExtractor/pptx`).

---

## 1. Phân tích 3 file mẫu (những gì file thật đang dùng)

### 1.1 DOCX — `DataExtractor/docx` (tạo bằng WPS, tiếng Việt, 1 trang A4)

| Tính năng | Ví dụ trong file | Ghi chú đơn vị / ý nghĩa |
|---|---|---|
| Trang | `pgSz w=11906 h=16838` (A4), `pgMar` 1260/1180, header/footer 708 | twips (1/20 pt) |
| Font mặc định | `docDefaults/rPrDefault`: Arial; style Normal: Arial 11pt (`sz=22`) | `sz` = nửa point |
| Font thực dùng | Arial, **Consolas** (code block), emoji `🎼🎹` 36pt, ✅ | fontTable còn khai báo SimSun, Calibri, Apple Color Emoji… |
| Cỡ chữ | 9 / 10 / 11 / 13 / 14 / 17 / 23 / 36 pt (`sz` 18…72) | **nhiều cỡ < 12pt** |
| Màu chữ | 00695C, 546E7A, 222222, **FFFFFF** (chữ trắng trên nền header bảng) | |
| Style | styleId dạng số `"1"`, `"2"`… (WPS), heading 1–3 có `outlineLvl`, spacing riêng | tra theo `styleId`, không theo tên |
| Đoạn | `spacing before/after` (twips), `jc=center/right/both`, `ind left=360/480` | `both` = căn đều 2 bên |
| **Viền đoạn `pBdr`** | đường kẻ ngang ngăn mục (`bottom B2DFDB sz=4`), callout **viền trái** (`left 2E7D32 sz=14 space=10`), header có `bottom`, footer có `top` | `sz` viền = 1/8 pt (4 → 0.5pt, 14 → 1.75pt); `space` = pt |
| **Nền đoạn `shd`** | code block: `shd fill=F2F7F6` + Consolas 9pt + `ind left=480`, nhiều đoạn liền nhau | các đoạn liền nhau cùng nền phải trông như 1 khối |
| Danh sách | numId 1: `decimal "%1."` ind 720/hanging 360; numId 2: bullet `•` / `◦` 2 cấp | |
| **Bảng** (5 bảng) | `tblW 9200 dxa`, `tblGrid` 3000/6200, `tblBorders` single sz=4 auto, `tblLayout autofit`, `tblCellMar`, `tblHeader` (lặp dòng tiêu đề) | |
| Ô bảng | `tcW`, **`tcBorders` màu AAAAAA/DDDDDD sz=0**, **`shd fill=00695C`** (header) / FFFFFF, **`tcMar` 80/120/80/120** | `sz=0` = nét mảnh nhất (hairline) |
| Field | TOC trong `sdt` (field rỗng — chưa update), `PAGE` ở footer "Trang N • Internal Dev Doc" | |
| Ảnh | 1 ảnh inline PNG 1195×1297, `extent cx=5815330 cy=4794250` EMU, **`srcRect l=-46 r=230 b=8944`** (crop, đơn vị 1/1000 %) | EMU: 914400/inch |
| Khác | `settings/zoom 132%`, `bookmarkStart _GoBack`, `w14:paraId` trên mọi đoạn | `paraId` hữu ích làm khóa ổn định cho edit |

### 1.2 XLSX — `DataExtractor/xlsx` (tạo bằng WPS/Excel, 4 sheet tiếng Việt)

| Tính năng | Ví dụ | Ghi chú |
|---|---|---|
| Sheet | "Tổng quan" (dashboard), "Dữ liệu chi tiết" (74 dòng × 18 cột), "Trùng ngày song song", "Ghi chú dữ liệu"; `activeTab=2` | mở đúng sheet active |
| **Ẩn lưới** | sheet1 `showGridLines="0"` | dashboard không có kẻ ô |
| Freeze | sheet2 `ySplit=1`; sheet3 `xSplit=1 ySplit=5` | |
| **AutoFilter đang bật** | table1 `autoFilter` + `customFilter val="X"` ở cột 9 → **68 dòng hidden** (sheet2), 109 dòng hidden (sheet3) | cần nút lọc ở header |
| Table (ListObject) | 3 table, style **tùy biến** `TableStylePreset3_Accent1` định nghĩa trong `styles.xml/tableStyles` bằng `dxf` (theme + tint) | không phải style builtin |
| Công thức | COUNTA/SUM/COUNTIF chéo sheet, `IF(OR(ISNUMBER(SEARCH(...))))`, `TEXT(E2,"yyyy-mm")`; **shared formula** (`t="shared" si=`) | giá trị cache trong `<v>` |
| Định dạng số | **`numFmtId=58`** (ngày builtin theo locale) cho cột ngày: `E2 = 46202` | phải hiện thành ngày |
| | 9 (%), 2 (0.00), 41–44 (accounting) | |
| Style ô | 30 font (Arial/Calibri, bold, màu 1F4E78…), fill solid, border, `alignment center/wrapText` | |
| Merge | 15 vùng merge ở dashboard (B5:C5, H6:K6…) | |
| Kích thước | `customHeight` dòng (17.25…42.75pt), cột `width` 2…65 ký tự | |
| Chart | 4 chart: 3 **bar clustered**, 1 **pie** (`dPt` màu `schemeClr` + `lumMod/lumOff`), data label, `manualLayout`, leader lines (c15) | |
| Ảnh | drawing2: `twoCellAnchor` ảnh ở dòng 118→137 sheet3 (vùng có dòng ẩn) | |

### 1.3 PPTX — `DataExtractor/pptx` (xuất từ **Canva**, 10 slide, 34 MB)

| Tính năng | Ví dụ | Ghi chú |
|---|---|---|
| Kích thước slide | `18288000 × 10287000` EMU (1920×1080 @96dpi, 16:9) | |
| **Font nhúng** | 5 font EOT (`.fntdata`, không nén MTX, không XOR): Bahianita, Montserrat, **Montserrat Bold**, Bricolage Grotesque, **Bricolage Grotesque Bold** | Canva đặt tên family riêng cho bản Bold |
| Chữ | `sz=15171` (151.71pt), `sz=3499`, `b="true"` + `latin typeface="Montserrat Bold"`, có cả `a:sym` | `sz` = 1/100 pt |
| Dòng | `lnSpc/spcPts val=21240` (212.4pt), `spcBef spcPct 0`, `algn=ctr` | |
| Text box | `bodyPr lIns=tIns=rIns=bIns=0`, **`spAutoFit`**, `anchor=t` | |
| Hình | 91 `p:sp`, 26 `p:grpSp` (group có `chOff/chExt` scale khác `off/ext`), 28 `custGeom` (moveTo/lnTo/**cubicBezTo**) | |
| **Ảnh trong custGeom** | Freeform có `blipFill` + `stretch/fillRect` **âm** (vd. `t=-9762 b=-9762`) = ảnh cover-crop | đơn vị 1/1000 % |
| Ảnh | 19 ảnh JPEG/PNG, lớn (tới 6.9 MB) | cần downsample khi decode |
| Master/Layout | 1 master, 11 layout, theme | Canva ít dùng placeholder |

---

## 2. Đối chiếu với code hiện tại — các lỗi/thiếu đã xác nhận

Mức: **P0** = làm sai rõ trên file mẫu · **P1** = sai lệch vị trí/kích thước · **P2** = thiếu tính năng phụ.

### 2.1 Chung (font)

| # | Mức | Vấn đề | Vị trí code | Hậu quả |
|---|---|---|---|---|
| F1 | P0 | Tên font Office → `Typeface.create(name)`; Android không có Arial/Calibri/Consolas/Times → **luôn rơi về Roboto** (metrics khác) | `simpletext/font/FontTypefaceManage.kt:94` | ngắt dòng, ngắt trang, độ rộng cột khác Word |
| F2 | P0 | **Đậm/nghiêng luôn là giả** (`isFakeBoldText`, `textSkewX`), không dùng `Typeface.BOLD`/file font Bold | `wp/view/LeafView.kt:90-101`, `simpletext/font/FontKit.kt:47-60`, (6 chỗ `isFakeBoldText = true`) | chữ đậm to/xấu; PPTX "Montserrat Bold" + `b=true` bị **đậm kép** |
| F3 | P0 | Excel **bỏ qua tên font**: mọi ô dùng `SANS_SERIF` | `simpletext/font/FontKit.kt:75-77` | |
| F4 | P1 | Cỡ chữ lưu **Int (pt)** → mất 10.5pt, 151.71pt… | `AttrManage.setFontSize` (`simpletext/model/AttrManage.kt:80`), `fc/ppt/attribute/RunAttr.kt:310` | lệch nhỏ tích lũy |
| F5 | P2 | Font nhúng PPTX: đã đọc được đúng 5 font mẫu (đã kiểm header EOT: flags=0, dữ liệu sfnt `00010000`) — nhưng registry theo **tên family**, không tách regular/bold/italic | `fc/ppt/reader/EmbeddedFontReader.kt` | nếu file nhúng cả regular+bold cùng family chỉ lấy 1 |
| F6 | P2 | Emoji (🎼🎹, ✅): cần kiểm tra chia leaf/ngắt dòng không cắt đôi cặp surrogate UTF-16 | `wp/view/LeafView.kt`, `LayoutKit` | ô vuông / crash khi measure |

### 2.2 DOCX (`fc/doc/DOCXReader.kt` + `wp/view`)

| # | Mức | Vấn đề | Vị trí code | Hậu quả trên file mẫu |
|---|---|---|---|---|
| D-a | P0 | **Cỡ chữ bị ép tối thiểu 12pt**: `var szSize = 12f` rồi `maxOf(...)`; còn lấy `max(sz, szCs)` | `DOCXReader.kt:3146-3153` | toàn bộ thân bài 11pt, bảng 10pt, footer 9pt, code 9pt → **đều thành 12pt** → dài hơn, ngắt trang sai |
| D-b | P0 | **Chữ màu `FFFFFF` bị đổi thành đen** (hack vì chưa đọc nền ô) | `DOCXReader.kt:3170` | header bảng: chữ đen trên nền trắng thay vì chữ trắng trên nền 00695C |
| D-c | P0 | **Không đọc nền ô `tcPr/shd`** (chỉ đọc `tblStylePr firstRow` của table style) | `processCellAttribute` `DOCXReader.kt:943` | mất màu header bảng |
| D-d | P0 | **Không đọc viền bảng/ô** (`tblBorders`, `tcBorders`); `TableView` luôn vẽ khung đen mặc định cho mọi ô | `DOCXReader.kt:870-883`, `wp/view/TableView.kt:84-85` | viền đen đậm thay vì AAAAAA/DDDDDD hairline; bảng không viền cũng bị vẽ viền |
| D-e | P0 | **Không đọc `pBdr`** (viền đoạn) | `processParaAttribute` `DOCXReader.kt:1048` | mất đường kẻ ngăn mục, mất viền trái callout, mất kẻ dưới header / kẻ trên footer |
| D-f | P0 | **Không đọc `pPr/shd`** (nền đoạn) | như trên | code block không có nền F2F7F6 |
| D-g | P1 | **Lề ô hard-code 7px** trái/phải, 0 trên/dưới; bỏ qua `tcMar`/`tblCellMar` (ID `TABLE_*_MARGIN_ID` đã có sẵn nhưng không dùng) | `simpletext/model/AttrManage.kt:1499-1501` | chữ sát viền, dòng bảng thấp hơn Word |
| D-h | P1 | **`jc=both` bị coi là trái**; layout **chưa hỗ trợ căn đều** (hằng `PARA_HOR_ALIGN_JUSTIFIED` có nhưng không dùng) | `DOCXReader.kt:1082`, `wp/view/LayoutKit.kt` / `LineView.kt` | đoạn căn đều bị răng cưa phải |
| D-i | P1 | `tblW`, `tblLayout`, `tblHeader` (lặp tiêu đề khi sang trang) không đọc (có sẵn `TABLE_ROW_HEADER_ID`, DOC reader đã dùng) | `processTable/processRowAttribute` | |
| D-j | P1 | `rFonts`: chọn `hAnsi ?: eastAsia`, bỏ `ascii`, `cs`, **`asciiTheme/hAnsiTheme`** (font theo theme `+mn-lt`) | `DOCXReader.kt:3158` | doc dùng theme font (Calibri/Cambria) sẽ sai font |
| D-k | P2 | `srcRect` (crop ảnh) trong DOCX — cần kiểm tra `addPicture` có áp `PictureEffectInfo` như PPTX không | `DOCXReader.kt:1668` | ảnh bị lệch 9% đáy |
| D-l | P2 | Paragraph `rPr` mark, `w:shd` trên run (`rPr/shd`), `caps`, `smallCaps`, `spacing` (letter), `w:kern` chưa đọc | `processRunAttribute` | |
| D-m | P2 | TOC field rỗng: hiển thị trống là đúng; nếu muốn có thể tự dựng TOC từ heading `outlineLvl` | | |

### 2.3 XLSX (`fc/xls/Reader`, `ss/view`, `ss/model`)

| # | Mức | Vấn đề | Vị trí code | Hậu quả |
|---|---|---|---|---|
| X-a | P0 | **Builtin numFmt chỉ 0–49** → `numFmtId=58` (và 27–36, 50–81 theo locale) rơi về General | `fc/xls/Reader/shared/StyleReader.kt:69-73`, `ss/model/style/BuiltinFormats.kt` | cột ngày hiện **46202** thay vì ngày |
| X-b | P0 | **`showGridLines="0"` không đọc** | `fc/xls/Reader/SheetReader.kt` (chỗ đọc `sheetView`, ~l.160) + `ss/view` vẽ lưới | dashboard có lưới |
| X-c | P0 | Font ô luôn `SANS_SERIF` + đậm giả (F2, F3) | `simpletext/font/FontKit.kt` | |
| X-d | P1 | **Table style tùy biến** (`styles.xml/tableStyles/tableStyle` + `dxf`) không đọc; `TableStyleKit` chỉ biết tên builtin Light/Medium | `ss/model/table/TableStyleKit.kt:71`, `StyleReader.processTableFormat` | table mất màu header/stripe |
| X-e | P2 | AutoFilter: không vẽ nút lọc ở header (dòng ẩn đã đúng nhờ `hidden`) | `SheetReader`, `ss/view` | |
| X-f | P2 | Chart pie: màu `dPt` `schemeClr + lumMod/lumOff`, `manualLayout`, data label/leader line — cần kiểm tra thực tế | `fc/xls/Reader/drawing/ChartReaderImplJava.java`, `thirdpart/achartengine` | |
| X-g | P2 | Ảnh `twoCellAnchor` nằm trên vùng dòng ẩn → tính kích thước theo dòng ẩn (0 cao) | `DrawingReader` | ảnh bị co/biến mất |
| X-h | OK | Shared formula đã xử lý (`SheetReader.kt:532`, `XlsxWriter.kt:105`); freeze pane có đọc (`SheetReader.kt:165`) — chỉ cần kiểm tra hiển thị `xSplit+ySplit` | | |

### 2.4 PPTX (`fc/ppt`, `pg/view`, `common/autoshape`)

| # | Mức | Vấn đề | Vị trí code | Hậu quả |
|---|---|---|---|---|
| P-a | P0 | Đậm kép: family "Montserrat Bold" (đã là bold) + `b=true` → fake bold thêm (F2) | `LeafView.kt:90-101` | chữ dày, rộng → tràn/ngắt dòng |
| P-b | P1 | `spcPts`: `val.toInt() / 100` chia nguyên → mất phần lẻ (3499 → 34pt) | `fc/ppt/attribute/ParaAttr.kt:363, 382, 405` | lệch dòng nhỏ |
| P-c | P1 | Cỡ chữ Int (`RunAttr.kt:310`: `sz/100` → Int) | | 151.71 → 151 |
| P-d | P1 | `spAutoFit` không đọc (chỉ có `normAutofit`) — chiều cao box nên theo nội dung | `ParaAttr.kt:73-90`, `ShapeManage` | box/viền nền lệch |
| P-e | P2 | Ảnh trong `custGeom` + `fillRect` âm: đường `AutoShapeDataKit.processBackground` có đọc `stretch/fillRect` → **cần kiểm tra bằng mắt** (slide 2, 9) | `common/autoshape/AutoShapeDataKit.kt:165-190` | ảnh méo nếu sai |
| P-f | P2 | Ảnh 1–7 MB: kiểm tra có `inSampleSize` theo kích thước hiển thị | `common/picture/PictureManage` | OOM/lag |

---

## 3. Kiến trúc cần bổ sung (dùng chung cho cả 3 định dạng)

1. **FontResolver** (thay `FontTypefaceManage` nội bộ): `resolve(family, bold, italic, script) → (Typeface, needFakeBold, needFakeItalic)`
   - Thứ tự: font nhúng trong file → font bundle metric-compatible → font hệ thống → fallback theo script.
   - Bảng thay thế **metric-compatible** (cùng độ rộng glyph → ngắt dòng giống Office), license OFL/Apache:
     | Office | Thay bằng |
     |---|---|
     | Arial, Helvetica, Arial Unicode MS | Liberation Sans / **Arimo** |
     | Times New Roman | Liberation Serif / **Tinos** |
     | Courier New | Liberation Mono / **Cousine** |
     | Calibri | **Carlito** |
     | Cambria | **Caladea** |
     | Consolas | Inconsolata (gần đúng, không metric-compatible) |
     | Segoe UI, Tahoma, Verdana | Noto Sans / Open Sans (gần đúng) |
     | SimSun/宋体, SimHei, 等线, 苹方 | Noto Sans CJK hệ thống |
   - Bundle trong `src/main/assets/fonts/` (Arimo/Tinos/Cousine/Carlito/Caladea ~ 4–6 MB cả 4 kiểu). Có thể tải theo nhu cầu nếu lo dung lượng APK.
   - Chỉ bật fake bold/italic khi **không có** biến thể thật.
2. **Đơn vị chính xác**: thêm helper `Units.kt` (twips↔px, EMU↔px, halfPt, 1/8pt viền, 1/100pt PPTX, 1/1000% crop) và lưu cỡ chữ dạng **1/100 pt** (xem task C3).
3. **Harness so sánh render** (bắt buộc để làm "từ từ" mà không vỡ):
   - Ảnh tham chiếu: mở 3 file mẫu bằng WPS Office (đã có trên máy) / MS Office → xuất PDF → PNG từng trang.
   - Instrumented test render trang/slide/vùng sheet ra Bitmap → lưu `build/render-out/` → so sánh (diff % pixel, hoặc chỉ chụp để xem mắt).
   - Test JVM thuần cho reader: đọc file mẫu → assert thuộc tính (cỡ chữ, màu, viền…) mà không cần vẽ.

---

## 4. Giai đoạn 0 — Nền tảng (làm trước mọi thứ)

- [x] **G0.1 Build xanh.** (26/09 — đã pass, xem mục 0) Hoàn tất `.kotlin-fix/PROGRESS.md` (còn ~1.160 lỗi Kotlin + lỗi Java phía sau) → `./gradlew :android_document:assembleDebug :app:assembleDebug` pass.
  - Xong khi: app mở được 3 file mẫu không crash.
- [x] **G0.2 Đưa file mẫu vào test.** (26/09) Nén lại 3 thư mục thành `sample.docx/.xlsx/.pptx` (giữ nguyên cấu trúc zip) và đặt vào `android_document/src/androidTest/assets/samples/` (PPTX 34 MB: cân nhắc bản rút gọn 2–3 slide cho CI).
  - Lệnh: `cd DataExtractor/docx && zip -X -r ../sample.docx '[Content_Types].xml' _rels docProps word` (tương tự cho xlsx/pptx).
- [ ] **G0.3 Ảnh tham chiếu.** Xuất PDF + PNG từ WPS/Office cho 3 file → `android_document/src/androidTest/assets/reference/`. Ghi rõ phần mềm/phiên bản dùng để xuất.
- [x] **G0.4 Render test.** (26/09 — `SampleRenderTest`, máy SM-A165F) Viết instrumented test: mở file qua `IControl` headless → vẽ trang 1 DOCX, slide 2 + slide 9 PPTX, sheet "Tổng quan" + "Dữ liệu chi tiết" XLSX ra PNG. Chưa cần so sánh tự động, chỉ cần sinh ảnh để xem trước/sau mỗi task.
- [ ] **G0.5 Test JVM reader.** Thêm test cho các hàm parse thuần (vd. `EmbeddedFontReader.extractFontData` với 5 file `.fntdata`, `parseHexColor`, sau này là numFmt/đơn vị).
- [ ] **G0.6 Debug overlay** (tùy chọn nhưng rất đáng): cờ debug vẽ khung bounding box của paragraph/line/cell/shape để soi lệch vị trí.

---

## 5. Giai đoạn 1 — Font (ảnh hưởng cả 3 định dạng)

- [ ] **C1. FontResolver + bundle font metric-compatible**
  - Sửa: tạo `simpletext/font/FontResolver.kt`; `FontTypefaceManage.getFontTypeface` gọi qua resolver; thêm `assets/fonts/*`.
  - Làm: map tên (không phân biệt hoa thường, bỏ hậu tố " Bold"/" Italic"/" Regular" khi so), cache `Typeface` theo (family, style).
  - Xong khi: DOCX mẫu (Arial) render bằng Arimo; đo chiều rộng dòng "Tính năng Play-along cho phép người dùng…" lệch < 2% so với ảnh tham chiếu.
- [ ] **C2. Bỏ fake bold/italic khi có biến thể thật**
  - Sửa: `wp/view/LeafView.kt:90-104`, `simpletext/font/FontKit.kt:47-77`, 4 chỗ còn lại có `isFakeBoldText = true` (grep).
  - Làm: `resolver.resolve(fontIndex, bold, italic)` trả về typeface + cờ `fakeBold/fakeItalic`; chỉ set paint khi cờ bật.
  - Trường hợp Canva: family tên kết thúc bằng "Bold" (hoặc font nhúng có `usWeightClass ≥ 600`) + `b=true` → **không** fake bold.
  - Xong khi: slide 2 PPTX tiêu đề "01." và "KỲ VĨ MIỀN BẮC" (Montserrat Bold) có độ dày giống ảnh tham chiếu.
- [ ] **C3. Cỡ chữ lẻ (1/100 pt)**
  - Sửa: `constant/wp/AttrIDConstant.kt` (thêm `FONT_SIZE_CENTI_ID`), `AttrManage` (`setFontSizeF/getFontSizeF`, fallback về `FONT_SIZE_ID`*100), `fillCharAttr`, `CharAttr.fontSize` → Float; `LeafView` dùng float.
  - Reader: DOCX `sz/2`, PPTX `sz/100`, XLSX `sz` float.
  - Lưu ý: nhiều chỗ đọc `getFontSize` Int (DOC, PPT cũ) — giữ API cũ trả Int để không vỡ.
  - Xong khi: PPTX `sz=15171` → 151.71pt; DOCX `sz=21` → 10.5pt.
- [ ] **C4. Font Excel theo tên** — `FontKit.getCellPaint`: dùng `font.getName()` qua resolver thay `SANS_SERIF` (cache theo fontIndex đã có).
- [ ] **C5. Font nhúng theo biến thể** — `EmbeddedFontReader`: đọc cả `regular/bold/italic/boldItalic`, đăng ký `(family, style)`; đọc `usWeightClass` từ bảng OS/2 để biết font đó thực chất là bold.
- [ ] **C6. Emoji & surrogate**: kiểm tra `LayoutKit` không cắt giữa cặp surrogate/ZWJ sequence khi ngắt dòng (dùng `BreakIterator.getCharacterInstance`). Thêm test đoạn "🎼🎹" 36pt và "✅  Play-along…".

---

## 6. Giai đoạn 2 — DOCX render

Thứ tự đề xuất: D1 → D2 → D3 → D4 → D5 (P0), rồi D6… (P1/P2).

- [x] **D1. Sửa cỡ chữ run** (26/09 — bỏ sàn 12pt, bỏ `szCs`, làm tròn half-point; *kèm*: đoạn không có `pStyle` giờ dùng style `default="1"` (Normal) thay vì docDefaults — `applyDefaultParaStyle`) (lỗi D-a) — *nhanh nhất, hiệu quả lớn nhất*
  - Sửa: `DOCXReader.processRunAttribute` `:3143-3154`.
  - Làm: `sz` → cỡ chữ (half-point / 2); `szCs` chỉ dùng cho chữ complex-script (bỏ qua hoặc lưu riêng). **Bỏ sàn 12pt.** Không có `sz` → không set (để kế thừa style/docDefaults).
  - Kiểm tra thêm: `defaultFontSize()` `:1035` trả 12 khi thiếu docDefaults — Word mặc định là **10pt** (`sz` vắng mặt = 20 half-point) nếu không có style; với file có Normal style thì lấy Normal.
  - Xong khi: body = 11pt, cell = 10pt, footer = 9pt, code = 9pt trong test JVM đọc thuộc tính.
- [x] **D2. Nền ô + viền bảng/ô** (26/09 — `processCellBordersAndMargins`, `parseBorders`, viền table style trong styles.xml, `TableView.drawCellBorders`; DOC/PPT không có thông tin viền vẫn vẽ lưới cũ; `tblHeader` cũng đã đọc) (D-b, D-c, D-d)
  - Sửa reader: `processTableAttribute` đọc `tblBorders` (top/left/bottom/right/insideH/insideV), `tblCellMar`, `tblW`, `tblLayout`; `processCellAttribute` đọc `tcBorders`, `shd` (`fill`, bỏ qua `auto`), `tcMar`.
  - Model: dùng các ID đã có `TABLE_*_BORDER_ID/_COLOR_ID`, `TABLE_*_MARGIN_ID`; thêm `TABLE_CELL_BACKGROUND` nếu `setTableCellBackground` chưa đủ; lưu kiểu nét + độ dày (1/8 pt) — có thể tái dùng `common/borders/Border.kt` + `BordersManage`.
  - Quy tắc ưu tiên viền (Word): `tcBorders` của ô > `tblBorders` (cạnh ngoài dùng top/left/bottom/right, cạnh trong dùng insideH/insideV) > table style. Viền chung giữa 2 ô: ô nào định nghĩa nét "nặng" hơn thắng (đơn giản hóa: ô sau ghi đè).
  - Vẽ: `wp/view/TableView.draw` — bỏ `drawRect` STROKE mặc định (`:84-85`); vẽ từng cạnh theo thuộc tính; `val=nil/none` → không vẽ; `sz=0` → 1px hairline; độ dày = `sz/8 pt * POINT_TO_PIXEL * zoom`.
  - **Bỏ hack chữ trắng → đen** ở `:3170` (chỉ giữ `auto` → đen). Nếu lo văn bản trắng trên nền trắng thật: xử lý ở lúc vẽ (so với nền ô/đoạn/trang), không ở reader.
  - Xong khi: bảng "Hạng mục / Thông tin": header nền 00695C chữ trắng đậm; các dòng nền trắng, viền xám nhạt mảnh.
- [x] **D3. Lề ô** (26/09 — `AttrManage.fillTableAttr` dùng twips khi reader set, mặc định Word 108 twips trái/phải) (D-g)
  - Sửa: `AttrManage.kt:1499-1501` dùng `getTableLeftMargin…` (twips → px); mặc định Word: trái/phải 108 twips (0.075"), trên/dưới 0 khi không khai báo; thứ tự `tcMar` > `tblCellMar` > style.
  - Xong khi: chữ trong ô cách viền trái 120 twips (6pt), trên/dưới 80 twips (4pt) như file mẫu.
- [x] **D4. Viền đoạn `pBdr`** (26/09 — `ParaDecoration`, ID `PARA_BORDER_*` 0x1011–0x101C, vẽ trong `ParagraphView.drawDecoration`; chưa làm `between`) (D-e)
  - Model: thêm `PARA_BORDER_ID` (index vào `BordersManage`, giống `PAGE_BORDER_ID` ở `AttrManage.kt:891-902`).
  - Reader: `processParaAttribute` + trong style (`styles.xml` pPr) + header/footer.
  - Vẽ: `wp/view/ParagraphView.draw` (`:83`) trước khi vẽ con: cạnh trên/dưới/trái/phải; `space` (pt) = khoảng cách từ chữ tới viền; vùng ngang = từ indent trái − space tới lề phải/indent phải.
  - **Gộp** các đoạn liền nhau có cùng `pBdr`: chỉ vẽ top ở đoạn đầu, bottom ở đoạn cuối, dùng `between` giữa chúng (Word rule).
  - Xong khi: có đường kẻ ngang màu B2DFDB giữa các mục; callout ✅ có vạch trái xanh 2E7D32 dày 1.75pt, cách chữ 10pt; header có gạch dưới, footer có gạch trên.
- [x] **D5. Nền đoạn `shd`** (26/09 — `PARA_SHADING_ID` 0x1010; các đoạn liền nhau cùng nền được tô liền khối) (D-f)
  - Model: `PARA_SHADING_ID` (màu ARGB). Reader: `pPr/shd fill` (bỏ `auto`), hỗ trợ `val=clear` (các pattern khác xấp xỉ bằng `fill`).
  - Vẽ: `ParagraphView.draw` tô nền trước viền/chữ; nhiều đoạn liền nhau cùng nền → tô liền không hở (nền phủ cả khoảng spacing giữa các đoạn cùng nền — đối chiếu ảnh tham chiếu để chốt).
  - Xong khi: khối code Consolas có nền F2F7F6 liền một khối, lùi 480 twips.
- [ ] **D6. Căn đều `jc=both/distribute`** (D-h)
  - Reader: map `both` → `PARA_HOR_ALIGN_JUSTIFIED`, `distribute` → justified cả dòng cuối.
  - Layout: trong `LayoutKit`/`LineView`, sau khi xếp leaf vào dòng: nếu justified và **không phải dòng cuối đoạn** (và dòng không kết thúc bằng ngắt dòng cứng) → chia phần dư cho các khoảng trắng (cộng `extraSpace` vào x của leaf phía sau mỗi space).
  - Ảnh hưởng: `editor/word/WordSelection.kt` (offsetAt/rectsFor) phải dùng vị trí sau khi giãn → kiểm tra lại chọn chữ.
  - Xong khi: đoạn ảnh (có `jc=both`) mép phải thẳng.
- [ ] **D7. Bảng: `tblW`, autofit/fixed, `tblHeader` lặp khi sang trang** (D-i) — `TableLayoutKit.kt`; `setTableHeaderRow` đã có trong `AttrManage.kt:1141`.
- [ ] **D8. Font theo theme** (D-j): đọc `theme1.xml` `majorFont/minorFont` (latin/ea/cs); `rFonts asciiTheme="minorHAnsi"` → font minor; ưu tiên `ascii` cho ký tự ASCII, `hAnsi` cho Latin mở rộng (tiếng Việt), `eastAsia` cho CJK (có thể chia leaf theo script — bước sau).
- [ ] **D9. Crop ảnh `srcRect`** (D-k): áp `srcRect` l/t/r/b (1/1000 %) khi vẽ `PictureShape` DOCX, giá trị âm = thêm lề trống.
- [ ] **D10. Thuộc tính run còn thiếu** (D-l): `rPr/shd`, `caps/smallCaps`, `spacing` (giãn chữ, twips), `position` (nâng/hạ), `vanish` (ẩn).
- [x] **D12. Số trang trong footer trống** (26/09 — placeholder "1" khi field PAGE/NUMPAGES trong header/footer không có kết quả cache; LeafView thay bằng số trang thật) — render thấy "Trang  • Internal Dev Doc" thiếu số: field `PAGE` dạng
  `fldChar begin / instrText PAGE / separate / end` **không có run kết quả** giữa separate và end (WPS ghi vậy) → reader
  phải tự tạo leaf số trang (`setFontPageNumberType`) khi gặp instrText `PAGE`/`NUMPAGES` dù không có kết quả cache. Xem `processRun` quanh `fldChar`.
- [x] **D13. Khoảng cách giữa 2 đoạn** (26/09 — đã đổi sang cộng; còn `contextualSpacing` chưa đọc): `LayoutKit.layoutPara` (~l.105) lấy `max(before, after trước)` kiểu HTML; Word
  **cộng** `after` của đoạn trước + `before` của đoạn sau (trừ khi `contextualSpacing` cùng style). Sửa + so ảnh tham chiếu.
- [x] **D14. Hiệu năng layout** (26/09 — xóa toàn bộ `Log.e` debug trong `LayoutKit`/`WPLayouter`): `LayoutKit.layoutPara` gọi `Log.e` cho **mỗi dòng** (l.~121, ~204) — xóa.
- [ ] **D15. TOC rỗng + các đoạn trống tạo khoảng trắng lớn** sau bảng đầu (trang 1) — so với ảnh tham chiếu WPS rồi quyết định.
- [ ] **D11. Kiểm tra hồi quy**: danh sách lặp, header/footer, field PAGE ("Trang 1 • Internal Dev Doc" 9pt màu 4DB6AC căn phải), emoji tiêu đề 36pt căn giữa.

---

## 7. Giai đoạn 3 — XLSX render

- [x] **X1. Builtin number format theo locale** (26/09 — id 27–36, 50–58 → ngày ngắn theo locale thiết bị / giờ; `BuiltinFormats.localeShortDate`) (X-a)
  - Sửa: `ss/model/style/BuiltinFormats.kt` + `StyleReader.getBuiltinNumberFormats`.
  - Làm: bổ sung ID 27–36, 50–58 (ngày/giờ Đông Á — trong file WPS/Excel CJK, 58 = "m月d日"), 59–62, 67–81 (Thái). Với app tiếng Việt: map 14, 27–36, 50–58 → định dạng ngày ngắn theo locale thiết bị (`dd/MM/yyyy`); 20–22, 32–35 → giờ.
  - Xong khi: `Dữ liệu chi tiết!E2` (46202) hiện thành ngày (serial 46202 = 29/06/2026), không còn số.
- [x] **X2. Ẩn lưới** (26/09 — `Sheet.isShowGridLines`, `SSConstant.gridlineColor`; đọc trong pull parser của `SheetReader`) (X-b): đọc `sheetView@showGridLines` (và `showRowColHeaders`, `zoomScale`, `topLeftCell`) → lưu vào `Sheet`; `ss/view` bỏ vẽ gridline khi tắt.
- [ ] **X3. Font ô thật** (X-c) — xong cùng C2/C4.
- [ ] **X4. Table style tùy biến** (X-d)
  - Reader: trong `StyleReader` đọc `<tableStyles><tableStyle name=…><tableStyleElement type=… dxfId=…>`, map sang `SSTableStyle` (wholeTable, headerRow, totalRow, firstColumn, lastColumn, firstRowStripe, secondRowStripe, firstColumnStripe).
  - `TableStyleKit`: nếu tên style có trong bảng tùy biến → dùng, không thì builtin như cũ. Áp `tableStyleInfo showRowStripes/showFirstColumn…`.
  - Màu `dxf` dùng `theme` + `tint` → đi qua `ThemeColorReader`/`SchemeColorUtil` (đã có xử lý tint).
- [ ] **X5. Nút AutoFilter** (X-e): đọc `autoFilter@ref` (sheet và table) → vẽ icon ▼ ở góc phải ô header; cột đang lọc (`filterColumn`) dùng icon phễu.
- [ ] **X6. Freeze pane 2 chiều** — ⚠️ `Sheet.getPaneInformation()` đang **trả `null` cố định** (`ss/model/baseModel/Sheet.kt`, comment `/*paneInformation*/`) → freeze pane bị tắt hoàn toàn; tìm lý do tắt (có thể lỗi vẽ) trước khi bật lại.: kiểm tra sheet3 (`xSplit=1 ySplit=5`, `topLeftCell=B19`) cuộn đúng, đường freeze đúng vị trí.
- [x] **X0. Màu sai do palette** (26/09 — `Workbook.addColor(argb)` tái dùng slot palette 0..65; `indexedColors` trong styles.xml (đọc SAU theme/font/fill) ghi đè slot đó → nền chart `bg1` và chữ trắng hóa **xanh lá 008000**. Giờ màu động luôn ở index ≥ 66.)
- [ ] **X7. Chart** (X-f): kiểm tra 4 chart với ảnh tham chiếu; ưu tiên: màu `schemeClr + lumMod/lumOff` trên `dPt` pie, data label `showVal/showPercent`, tiêu đề chart rich text, `manualLayout` plot area.
- [ ] **X8. Ảnh qua vùng dòng ẩn** (X-g): khi tính `twoCellAnchor` bỏ qua dòng hidden theo `editAs` (mặc định `twoCell` → ảnh co theo; nếu ≈0 thì dùng `a:ext` làm kích thước).
- [ ] **X10. Test render Excel không ổn định vị trí cuộn** — `SampleRenderTest.xlsx` chụp sheet ở vị trí cuộn khác nhau giữa các lần chạy; cần cuộn về `topLeftCell` (hoặc A1) trước khi chụp.
- [ ] **X11. Tiêu đề chart**: chart 1 hiện "Series 1" thay vì tiêu đề thật, pie (chart 4) mất tiêu đề — kiểm tra đọc `c:title/c:tx/c:rich` và `autoTitleDeleted`.
- [ ] **X9. Kiểm tra lại**: merge + căn giữa + wrapText (dashboard B5:C5…), chiều cao dòng customHeight, độ rộng cột theo *max digit width* của font mặc định (Calibri 11 → 7px @96dpi), numFmt 41–44 accounting, `%`.

---

## 8. Giai đoạn 4 — PPTX render

- [ ] **P1. Đậm kép Canva** (P-a) — xong cùng C2/C5. Kiểm tra slide 2–8.
- [ ] **P2. Khoảng cách dòng/đoạn chính xác** (P-b): `ParaAttr.kt:363, 382, 405` đổi `val.toInt() / 100` → `val.toFloat() / 100f`.
- [ ] **P3. Cỡ chữ lẻ** (P-c) — cùng C3 (`RunAttr.kt:310`, và nhánh `:586`).
- [ ] **P4. `spAutoFit`** (P-d): khi box có `spAutoFit`, chiều cao hiển thị = chiều cao text đã layout (tối thiểu `ext cy`), vẫn giữ `anchor`. Không đổi file.
- [ ] **P5. Ảnh trong custGeom** (P-e): so slide 2 (3 ảnh cover-crop, `fillRect` âm) và slide 9 (custGeom `cubicBezTo`) với ảnh tham chiếu; sửa `AutoShapeDataKit` nếu `fillRect` âm bị hiểu thành co ảnh.
- [ ] **P6. Group scale**: kiểm tra `ReaderKit.getChildShapeAnchor` với `chExt ≠ ext` (slide 2: `ext 5511800×3454400`, `chExt 1325326×830619`, tỷ lệ ~4.16) — viền/độ dày nét cũng phải scale.
- [ ] **P7. Ảnh lớn**: decode với `inSampleSize` theo kích thước hiển thị × zoom tối đa; cache theo slide; thumbnail dùng ảnh nhỏ hơn nữa.
- [ ] **P8. Kiểm tra**: tiêu đề "LỘ TRÌNH KHÁM PHÁ XUYÊN VIỆT" (Bahianita 151.71pt, `lnSpc` 212.4pt, căn giữa) nằm đúng dòng như ảnh tham chiếu (lỗi font = tiêu đề xuống 3 dòng).

---

## 9. Giai đoạn 5 — Realtime edit

### 9.1 Hiện trạng

| Định dạng | Có gì | Cơ chế |
|---|---|---|
| XLSX | `editor/xlsx/SheetEditSession.kt` — sửa ô live, tính lại công thức phụ thuộc (`XlsxFormulaEngine`), `A1FormulaShifter`, `XlsxWriter` vá file gốc (xử lý shared formula) | **live** |
| PPTX | `editor/pptx/LivePptxSession.kt` — thêm textbox/ảnh, sửa text shape, di chuyển, xóa, undo/redo; `PptxEditor` ghi file | **live** (có `needsReopen` khi không hiển thị live được) |
| DOCX | `editor/docx/DocxEditor.kt` — bold/italic/underline/màu/cỡ/highlight, chèn/xóa/thay text, chèn ảnh, thêm đoạn; `DocxSourceMap` ánh xạ offset model ↔ run XML; `editor/word/WordSelection.kt` chọn chữ | **xếp hàng → save → mở lại** |

Nguyên tắc chung giữ nguyên cho mọi task dưới: **mỗi thao tác cập nhật 2 lớp** — (1) model hiển thị (thấy ngay) và
(2) hàng đợi thao tác trên XML gốc (ghi khi save) — cùng một `Step(redo, undo)` như `LivePptxSession`.
Save luôn ghi ra file tạm rồi thay thế nguyên tử (đã làm trong các editor).

### 9.2 XLSX (dễ nhất — làm trước)

- [ ] **E-X1. UI nhập ô**: thanh công thức + ô đang sửa overlay trên `ss/view`, bàn phím, Enter/Tab di chuyển, hủy bằng Back.
- [ ] **E-X2. Định dạng ô live**: bold/italic/màu chữ/nền/căn/wrap/numFmt → tạo `CellStyle` mới trong model + `XlsxWriter` thêm `xf`/`font`/`fill` vào `styles.xml` (tái dùng nếu trùng).
- [ ] **E-X3. Chèn/xóa dòng-cột**: dời ô + merge + công thức (`A1FormulaShifter`) + `dimension`, table `ref`, `autoFilter ref`, anchor drawing.
- [ ] **E-X4. Undo/redo** thống nhất như PPTX (hiện `SheetEditSession` có `Change(before, after)` — gom thành stack).
- [ ] **E-X5. Kiểm tra với file mẫu**: sửa `Dữ liệu chi tiết!G2` → `Tổng quan!D6` (SUM) cập nhật live; save → mở bằng WPS thấy giá trị & công thức đúng; table/filter không hỏng.

### 9.3 PPTX

- [ ] **E-P1. Chọn shape bằng chạm** (hit-test theo z-order, xuyên group), khung chọn + 8 tay nắm + tay xoay.
- [ ] **E-P2. Kéo/thay kích thước/xoay live** — `moveShape` đã có; thêm resize (giữ tỷ lệ với ảnh), rotate (`xfrm rot`), shape trong group (tính ngược `chOff/chExt`).
- [ ] **E-P3. Sửa chữ tại chỗ**: overlay `EditText` trong suốt đúng vị trí/khung/font/cỡ (sau C1–C3 mới khớp), commit → `setShapeText`. Giữ định dạng run: khi sửa chỉ 1 run thì chỉ thay `a:t` của run đó.
- [ ] **E-P4. Định dạng run/đoạn**: bold/italic/màu/cỡ/căn → `PptxEditor` op mới (`a:rPr`/`a:pPr`), live qua `LiveSlideModel`.
- [ ] **E-P5. Slide**: thêm/nhân bản/xóa/đổi thứ tự (`presentation.xml sldIdLst` + rels + `[Content_Types].xml`).
- [ ] **E-P6. Kiểm tra file Canva**: sửa text slide 2 bằng font nhúng Bahianita; save → PowerPoint/WPS mở không báo lỗi repair; font nhúng giữ nguyên.

### 9.4 DOCX (khó nhất — chia nhỏ)

Khó ở 3 điểm: (a) layout Word chạy nền (`wp/view/LayoutThread.kt`) và phân trang toàn bộ; (b) offset model dịch
chuyển khi chèn/xóa trong khi `DocxEditor` dùng **offset gốc**; (c) nhập liệu IME (tiếng Việt Telex/VNI có composing text).

- [ ] **E-D0. Khảo sát relayout cục bộ** (1–2 ngày, viết kết quả vào đây):
  - Có thể layout lại **một `ParagraphView`** rồi dời các view phía sau không? (`LayoutKit.buildLine`, `WPLayouter`, `PageRoot`).
  - Nếu chiều cao đoạn đổi: phân trang lại **từ trang chứa đoạn đó** trở đi (dừng khi các trang sau không đổi vị trí bắt đầu).
  - Kết luận chọn: (A) relayout cục bộ, hoặc (B) relayout toàn bộ nhưng giữ vị trí cuộn (chấp nhận cho doc ngắn < 30 trang).
- [ ] **E-D1. Live formatting (không đổi offset)**: bold/italic/underline/màu/cỡ/highlight trên vùng chọn
  - Model: tách `LeafElement` tại biên vùng chọn, set attribute → relayout đoạn (cỡ chữ đổi → cao dòng đổi).
  - File: op tương ứng trong `DocxEditor` (đã có). Undo/redo theo mẫu `LivePptxSession`.
  - Tạo `editor/docx/LiveDocxSession.kt`.
- [ ] **E-D2. Bảng ánh xạ offset hiện tại ↔ gốc** (`OffsetMapper`, piece list): mỗi insert/delete thêm 1 mảnh; `DocxEditor` nhận offset gốc qua mapper; text mới chèn không có offset gốc → op "insert sau offset gốc X" (đã có kiểu `insert`).
  - Test JVM: chuỗi 100 thao tác ngẫu nhiên → áp lên model và lên XML → text của 2 bên bằng nhau.
- [ ] **E-D3. Gõ chữ trong 1 đoạn**: con trỏ nhấp nháy (vị trí từ `WordSelection.rectsFor`), `onCreateInputConnection` (hỗ trợ `setComposingText` cho bộ gõ tiếng Việt), chèn vào leaf tại con trỏ (thừa hưởng thuộc tính run bên trái), xóa lùi.
- [ ] **E-D4. Enter / Backspace đầu đoạn**: tách/gộp `ParagraphElement` (+ XML: tách `w:p` giữ `pPr`), số thứ tự danh sách tự cập nhật.
- [ ] **E-D5. Tay nắm chọn chữ, copy/cut/paste** (plain text trước, sau đó giữ định dạng từ chính tài liệu).
- [ ] **E-D6. Sửa trong ô bảng, header/footer** (offset không thuộc MAIN — hiện `DocxEditor` từ chối: "Only MAIN offsets are editable"; mở rộng `DocxSourceMap` cho HEADER/FOOTER/textbox).
- [ ] **E-D7. Định dạng đoạn**: căn lề, thụt lề, giãn dòng, bullet/numbering, và (sau D4/D5) nền/viền đoạn.
- [ ] **E-D8. Ổn định**: tự lưu nháp định kỳ vào cache, khôi phục khi crash; giới hạn undo; đo hiệu năng gõ (mục tiêu < 16 ms/ký tự với doc 20 trang).

---

## 10. Thứ tự làm khuyến nghị (tóm tắt)

1. G0.1 → G0.4 (build + ảnh tham chiếu + render test).
2. **D1** (cỡ chữ) → **D2** (nền/viền bảng + bỏ hack chữ trắng) → **X1** (ngày) → **X2** (ẩn lưới) — sửa nhỏ, thấy khác ngay.
3. C1 → C2 → C3 → C4/C5 (font) — tác động mọi định dạng, là điều kiện để edit tại chỗ khớp vị trí.
4. D3 → D4 → D5 → D6, rồi P2 → P4 → P5, X4 → X5.
5. Realtime: E-X1…E-X5 → E-P1…E-P6 → E-D0 → E-D1 → E-D2 → E-D3 → …
6. Các P2 còn lại khi có file thật báo lỗi.

---

## 11. Phụ lục — đơn vị & quy ước OOXML (gặp trong file mẫu)

| Đại lượng | Đơn vị | Quy đổi |
|---|---|---|
| DOCX kích thước trang, lề, indent, spacing, `tcW`/`tblW` (`type=dxa`), `tcMar` | twips | 1pt = 20 twips; 1 inch = 1440 |
| DOCX `sz` chữ | half-point | `sz=22` → 11pt |
| DOCX `sz` viền (`pBdr`, `tcBorders`) | 1/8 pt | `sz=4` → 0.5pt; `sz=0` → hairline |
| DOCX `space` của viền | pt | |
| DOCX `tcW type=pct` | 1/50 % | 5000 = 100% |
| DrawingML (`extent`, `xfrm`) | EMU | 1 inch = 914400; 1pt = 12700; 1px@96dpi = 9525 |
| `srcRect` / `fillRect` | 1/1000 % | `t=-9762` → −9.762% |
| PPTX `sz`, `spcPts` | 1/100 pt | `sz=3500` → 35pt |
| PPTX `spcPct`, `lnSpcReduction` | 1/1000 % | 100000 = 100% |
| XLSX `row@ht` | pt | |
| XLSX `col@width` | số ký tự của max-digit-width | px = trunc((256·w + trunc(128/mdw))/256 · mdw) |
| Màu theme | `schemeClr`/`theme` + `lumMod/lumOff/tint/shade` | tính trên HSL |
| `MainConstant` | `POINT_TO_PIXEL = 96/72`, `TWIPS_TO_PIXEL`, `EMU_PER_INCH` | `constant/MainConstant.kt` |

---

## 12. Nhật ký tiến độ

| Ngày | Task | Kết quả / ghi chú |
|---|---|---|
| 26/09/2026 | Phân tích | Tạo file này. |
| 26/09/2026 | G0.1 | Build xanh (Kotlin + Java + app dev). |
| 26/09/2026 | **Bug PPTX** | `PictureManage.getPicTempPath()` tự gọi chính nó (lỗi convert Java→Kotlin) → **StackOverflow, mọi PPTX có font nhúng không mở được**. Đã sửa; đã quét toàn module, không còn getter đệ quy nào khác. |
| 26/09/2026 | G0.2, G0.4 | Harness `SampleRenderTest` + 3 file mẫu; ảnh "trước" và "sau" đã chụp trên SM-A165F. |
| 26/09/2026 | D1–D5 | Cỡ chữ, style Normal, nền/viền/lề ô, bỏ hack chữ trắng→đen, viền + nền đoạn. Kiểm tra bằng ảnh: header bảng nền 00695C chữ trắng, viền xám mảnh, callout có vạch trái, khối code nền F2F7F6, header/footer có đường kẻ. |
| 26/09/2026 | D12, D13, D14, X1, X2, X0 | Số trang footer, spacing cộng, bỏ log layout, ngày builtin 58, ẩn lưới, sửa màu palette (chart xanh lá / chữ trắng thành xanh). Đã kiểm bằng ảnh. |
| — | **Làm tiếp** | C1/C2 (font metric-compatible + bỏ fake bold) → P2/P3 → X11 → X10 → D6 (justify). |
