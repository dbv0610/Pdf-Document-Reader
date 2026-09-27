# Fix lỗi build sau khi convert Java → Kotlin (module `android_document`)

Trạng thái: **TẠM DỪNG** theo yêu cầu — 17:54, còn 1.160 lỗi Kotlin (G01=2 G02=163 G03=160 G04=0 G05=0 G06=58 G07=83 G08=481 G09=8 G10=14 G11=191). Yêu cầu chéo chưa xử lý: requests/G01.md (PageSettingsBlock.positionRecords → MutableList<RecordBase>, thuộc G03).

## Tiến độ

| Thời điểm | Tổng lỗi Kotlin |
|---|---|
| Ban đầu (17:30) | 8.643 (787 file) |
| Sau Claude subagent (17:38) | **5.068** |

Chi tiết theo nhóm (ban đầu → hiện tại):

| Nhóm | Package (`com/wxiwei/office/...`) | Ban đầu | Hiện tại |
|---|---|---|---|
| G01 | fc/hssf/model, fc/hssf/util | 888 | 667 |
| G02 | fc/hssf/usermodel | 647 | 563 |
| G03 | fc/hssf/record/** | 1070 | 523 |
| G04 | fc/hssf/formula/function, formula/eval | 691 | 179 |
| G05 | fc/hssf/formula (còn lại) + hssf khác | 556 | 245 |
| G06 | fc/hslf/model | 750 | 372 |
| G07 | fc/hslf (còn lại) | 662 | 329 |
| G08 | fc/dom4j/tree, dom, util | 754 | 772 |
| G09 | fc/dom4j (còn lại) | 540 | 291 |
| G10 | fc/ddf, fc/hpsf | 704 | 380 |
| G11 | mọi package khác (ppt, xls, codec, fs, doc, ss, editor, common…) | 1381 | 747 |

G08 tăng nhẹ vì sửa lỗi làm lộ thêm lỗi phía sau (bình thường).

Loại lỗi còn lại nhiều nhất:
- 2308 `Unresolved reference` — chủ yếu gọi getter Java (`getFoo()`) trong khi Kotlin đã là property (`foo`)
- 409 Argument type mismatch (thường là nullable `T?` vs `T`)
- 554 `overrides nothing` — chữ ký override không khớp lớp cha
- 334 star projection (`List<*>`, `Map<*,*>`) từ raw type Java
- 136 Assignment type mismatch, 129 Incompatible types, 122 nullable receiver

## Vì sao dừng
- Codex: hết hạn mức sau ~2 phút (mở lại 20:45) — gần như chưa sửa được gì.
- Claude subagent: hết session limit (reset 18:40 Asia/Saigon) — cả 11 agent dừng giữa chừng.
  Một số file có thể đang sửa dở (vd. G01 đang rewrite header một file hssf/model, G08 đang viết BackedList/ContentListFacade, G06 đang sửa TextShape.kt) — build tiếp sẽ chỉ ra.
- Chưa có yêu cầu sửa chéo nào (`requests/` trống).

## Việc còn lại
1. Tiếp tục sửa 5.068 lỗi Kotlin (theo nhóm, `errors/Gxx.txt`).
2. Xử lý yêu cầu sửa chéo giữa các nhóm (nếu có).
3. Khi Kotlin compile được → build tiếp để lộ lỗi của **447 file Java** còn lại gọi sang Kotlin (javac chạy sau kotlinc).
4. Build module `:app`.

## Cách chạy tiếp
Thư mục này: `/Volumes/Data/Android/Pdf-Document-Reader/.kotlin-fix/` (untracked — **không commit**).

- `build.sh` — chạy `:android_document:compileDebugKotlin`, tách lỗi vào `errors/Gxx.txt`, ghi `build_history.txt`.
- `groups.txt` — định nghĩa nhóm (prefix đầu tiên khớp thắng; G11 = còn lại).
- `prompt_Gxx.md` — prompt cho từng agent (chỉ sửa file của nhóm mình, không chạy gradle, ghi yêu cầu chéo vào `requests/Gxx.md`).
  Chạy lại bằng codex:
  `codex exec -s workspace-write --add-dir .kotlin-fix -C . - < .kotlin-fix/prompt_G01.md`
  Trong lúc agent chạy, cho `build.sh` chạy lặp mỗi ~3 phút để cập nhật danh sách lỗi.
- `src_backup_before_codex.tgz` — bản sao lưu `android_document/src` trước khi agent sửa
  (khôi phục: `tar xzf .kotlin-fix/src_backup_before_codex.tgz` tại thư mục gốc repo).
