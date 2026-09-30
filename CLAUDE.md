# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

An Android app (`com.alf06.document.reader`) that reads PDF, Word, Excel and PowerPoint files and edits them in place. It also has PDF tools (annotate, sign, forms, OCR, organize, compress, password, redact, watermark), a camera document scanner, image-to-PDF, merge/split, translate and zip. It uses Kotlin with Java 17 and AGP 9 (versions are in `gradle/libs.versions.toml`), with compile/target SDK 37 and min SDK 26.

## Commands

The app has product flavors `dev` and `product`, so app tasks carry the flavor name.

```sh
./gradlew :app:assembleDevDebug                  # debug APK
./gradlew :app:installDevDebug
./gradlew :app:bundleProductRelease              # what CI builds on master (signed with keystore/)
./gradlew :app:lintDevDebug

# JVM unit tests (only android_document has them)
./gradlew :android_document:testDebugUnitTest
./gradlew :android_document:testDebugUnitTest --tests "com.wxiwei.office.editor.xlsx.RefShifterTest"

# Instrumented tests (device/emulator required)
./gradlew :android_document:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.wxiwei.office.editor.DocxEditorTest
./gradlew :app:connectedDevDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alf06.document.reader.edit.ReadDocumentEditTest
```

App instrumented tests drive the real activities using sample files in `app/src/androidTest/assets/samples/`. Some of them save screenshots on the device, under the app's external files dir (for example `edit-ui/`).

## Modules

- **`app`**: UI only. Koin DI (`di/AppModule.kt`, `di/ViewModelModule.kt`; register new ViewModels there), Room (`database/`, schemas in `app/schemas`), Firebase, ViewBinding. Screens live under `ui/home/...`. The document screens are `ui/home/document/pdf/ReadPdfActivity` (PDF tools in `pdf/tools/`) and `ui/home/document/office/ReadDocumentActivity`, which shows the edit panels from `com.wxiwei.office.editor.ui` (see below).
- **`lib`** (`com.ui.baselib`): shared base classes (`BaseActivity`, `BaseFragment`, `BaseDialog`, `BaseBottomSheet`, adapters), widgets and extensions that the app screens extend.
- **`android_document`** holds both document engines:
  - `com.wxiwei.office`: a fork of an office viewer (Java + Kotlin). Parsing is in `fc/`, the per-format model/view/control code is in `wp/` (Word), `ss/` (Excel) and `pg/` (PowerPoint), and `system/` has `MainControl`, the reader factory and threads. The embedding API for the app is `reader/` (`OfficeReader`, `OfficeDocumentView`, `ReaderConfig`, `ReaderState` as a StateFlow).
  - `com.wxiwei.office.editor`: the editing layer, which writes OOXML directly (`ooxml/OoxmlPackage`). Each format has a live session that updates the on-screen model at once and queues the same change in a file editor, which patches the original file on `save`: `LiveDocxSession`→`DocxEditor`, `LivePptxSession`→`PptxEditor`, `SheetEditSession`→`XlsxWriter` (with `XlsxFormulaEngine` for recalculation). When a change can't be shown live, it is still saved, and the session sets `needsReopen`. Sessions are main-thread only. Results come back as `EditResult` (`Ok` or `Error(Reason, …)`). `DocumentCreator` makes new blank documents.
  - `com.wxiwei.office.editor.ui`: the per-format edit UI: `WordEditPanel`, `ExcelEditPanel`, `SlideEditPanel`, overlays for caret/selection/pictures, and `DialogKit`, the one shared dialog builder (the PDF tools use it too). `EditDrafts` writes unsaved edits to the cache when the app goes to the background. Its colors and sizes come from `com.editor.docsdk.EditStyle` and `DialogStyle`, which you change in code with their `customizer`. Don't hard-code colors (see `android_document/docs/TUY_BIEN_GIAO_DIEN.md`). Texts are `docsdk_edit_*` string resources: English in `values/`, Vietnamese in `values-vi/`. A panel has no views of its own. It registers its commands with `action(EditAction.X, EditFeature…) { … }` and lists them in `tabs`. `EditToolbar` builds the SDK's bar (tabs and icons, each button tagged with its action's name) from those tabs. An app can instead run the commands from its own buttons through `DocumentEditor`. Questions such as a color or a size go through `pickColor`, `pickOne`, `askText` and the other ask helpers: they take a value passed to `run(action, value)`, or the app's `EditDialogs`, or else show the SDK dialog. The public entry points are `DocumentView.startEditing(features, showToolbar)` and `DocumentViewer.Options(editFeatures = …)`. Icons are Material Symbols (`res/drawable/docsdk_ic_*`).
  - `com.reader.pdfviewer`: the PDF viewer (`PDFView`) on top of prebuilt PDFium. The JNI bridge is `src/main/jni/src/mainJNILib.cpp`, built by CMake against `jni/lib/<abi>/libpdfium.so` with 16 KB page alignment. The Kotlin wrapper is `pdfium/PdfiumCore`. `tools/PdfTools` is the PDF tools menu/actions (a Koin singleton). pdfbox-android is used only to write encrypted copies.
- **`opencv`**: a slim local OpenCV 4.12 build (core/imgproc/imgcodecs) used by the scanner. `opencv/README.md` has the rebuild steps.

`DataExtractor/` holds unzipped sample OOXML files (docx/xlsx/pptx). Use them to inspect raw XML when you work on parsing or editing.

## Conventions seen in the codebase

- Commit messages are short, user-facing descriptions of the behavior, often prefixed with the format (`Word:`, `PPTX:`, `Excel:`, `PDF:`).
- The release signing config and keystore are committed (`keystore/`). Don't touch them unless asked.

---

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
