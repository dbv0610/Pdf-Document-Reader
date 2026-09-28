# Third-party notices — module `android_document`

This module contains source code and binaries from the third-party projects listed below.
Their original copyright and license notices are kept in the files. Changes to those files are
marked at the top of each file with "Modifications Copyright (c) 2026 dongb2002". The
modifications belong to dongb2002. For each project, its own license still applies to the original code.

Files that begin with "Copyright (c) 2026 dongb2002. All rights reserved." were written by
dongb2002 and are not third-party code.

## Source code included in this module

| Project | Where | Copyright | License |
|---|---|---|---|
| Apache POI | `com.wxiwei.office.fc` (`hslf`, `hssf`, `hwpf`, `poifs`, `openxml4j`, `ddf`, `hpsf`, `ss`, `util`, …) and other files with the ASF header | The Apache Software Foundation | Apache License 2.0 |
| dom4j | `com.wxiwei.office.fc.dom4j` | Copyright 2001-2005 (C) MetaStuff, Ltd. | dom4j license (BSD-style); the terms are at the end of the dom4j files |
| AChartEngine | `com.wxiwei.office.thirdpart.achartengine` | Copyright (C) 2009, 2010 SC 4ViewSoft SRL | Apache License 2.0 |
| FreeHEP (EMF support) | `com.wxiwei.office.thirdpart.emf` | Copyright 2001-2002, FreeHEP | GNU LGPL 2.1, the license FreeHEP publishes these libraries under. Check the original distribution; the files carry only the copyright line. |
| Mozilla charset detector (jchardet) | `com.wxiwei.office.thirdpart.mozilla.intl.chardet` | Copyright (C) 1998 Netscape Communications Corporation | MPL 1.1 / GPL 2.0 / LGPL 2.1 (tri-license) |
| OpenJDK class library (geometry) | `com.wxiwei.office.java.awt`, `com.wxiwei.office.java.util` | Copyright Oracle and/or its affiliates; Sun Microsystems | GNU GPL 2.0 with the Classpath Exception |
| Office viewer engine (base of `com.wxiwei.office`: `wp`, `ss`, `pg`, `system`, …) | `com.wxiwei.office` | Original authors, named in some files | The source has no license notice. Keep the terms it was obtained under. |

The files under GPL, LGPL or MPL above have not been modified by dongb2002. Any future change
to them must stay under their own license.

## Binaries and headers included in this module

| Project | Where | Copyright | License |
|---|---|---|---|
| PDFium | `src/main/jni/lib/<abi>/libpdfium.so`, `src/main/jni/include` | Copyright 2014 The PDFium Authors; original code copyright 2014 Foxit Software Inc. | BSD-style (PDFium `LICENSE`) |

## Libraries this module depends on (not included in its source)

| Library | License |
|---|---|
| pdfbox-android (`com.tom-roush:pdfbox-android`) | Apache License 2.0 |
| AndroidX (activity, appcompat, constraintlayout, core, recyclerview, viewpager2), Material Components | Apache License 2.0 |
| Kotlin coroutines | Apache License 2.0 |
| Gson | Apache License 2.0 |
| Lottie | Apache License 2.0 |
| sdp-android / ssp-android | MIT License |

The Apache License 2.0 requires the NOTICE file of an Apache project to go with any
redistribution. Apache POI's NOTICE says: "This product includes software developed by The
Apache Software Foundation (https://www.apache.org/)."
