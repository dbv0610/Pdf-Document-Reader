"""Generate the app's blank Office templates using only Python's standard library.
Run from any directory: python3 android_document/tools/generate_blank_documents.py
Templates contain no user content or third-party sample metadata.
"""
from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED

OUT = Path(__file__).resolve().parents[1] / 'src/main/assets/document_templates'
REL = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships/'
W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
S = 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'
P = 'http://schemas.openxmlformats.org/presentationml/2006/main'
A = 'http://schemas.openxmlformats.org/drawingml/2006/main'
R = REL.rstrip('/')

def rels(*entries):
    return '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' + ''.join(
        f'<Relationship Id="rId{i}" Type="{REL}{kind}" Target="{target}"/>'
        for i, (kind, target) in enumerate(entries, 1)) + '</Relationships>'

def types(entries):
    return '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/>' + ''.join(
        f'<Override PartName="/{part}" ContentType="application/vnd.openxmlformats-officedocument.{kind}+xml"/>'
        for part, kind in entries) + '</Types>'

def write(ext, parts, content_types, main):
    parts['[Content_Types].xml'] = types(content_types)
    parts['_rels/.rels'] = rels(('officeDocument', main))
    OUT.mkdir(parents=True, exist_ok=True)
    with ZipFile(OUT / f'blank.{ext}', 'w') as archive:
        for name, xml in sorted(parts.items()):
            info = ZipInfo(name, (2026, 1, 1, 0, 0, 0))
            info.compress_type = ZIP_DEFLATED
            archive.writestr(info, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n' + xml)

write('docx', {
    'word/document.xml': f'<w:document xmlns:w="{W}"><w:body><w:p/><w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/></w:sectPr></w:body></w:document>',
    'word/styles.xml': f'<w:styles xmlns:w="{W}"><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:eastAsia="Arial" w:cs="Arial"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after="0" w:line="240" w:lineRule="auto"/></w:pPr></w:pPrDefault></w:docDefaults><w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/></w:style></w:styles>',
    'word/_rels/document.xml.rels': rels(('styles', 'styles.xml')),
}, [('word/document.xml', 'wordprocessingml.document.main'), ('word/styles.xml', 'wordprocessingml.styles')], 'word/document.xml')

write('xlsx', {
    'xl/workbook.xml': f'<workbook xmlns="{S}" xmlns:r="{R}"><bookViews><workbookView activeTab="0"/></bookViews><sheets><sheet name="Sheet1" sheetId="1" r:id="rId1"/></sheets><calcPr calcId="0" fullCalcOnLoad="1"/></workbook>',
    'xl/worksheets/sheet1.xml': f'<worksheet xmlns="{S}"><dimension ref="A1"/><sheetViews><sheetView tabSelected="1" workbookViewId="0"><selection activeCell="A1" sqref="A1"/></sheetView></sheetViews><sheetFormatPr defaultRowHeight="15"/><sheetData/><pageMargins left="0.7" right="0.7" top="0.75" bottom="0.75" header="0.3" footer="0.3"/></worksheet>',
    'xl/styles.xml': f'<styleSheet xmlns="{S}"><fonts count="1"><font><sz val="11"/><color rgb="FF000000"/><name val="Arial"/><family val="2"/></font></fonts><fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs><cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles><dxfs count="0"/><tableStyles count="0" defaultTableStyle="TableStyleMedium2" defaultPivotStyle="PivotStyleLight16"/></styleSheet>',
    'xl/_rels/workbook.xml.rels': rels(('worksheet', 'worksheets/sheet1.xml'), ('styles', 'styles.xml')),
}, [('xl/workbook.xml', 'spreadsheetml.sheet.main'), ('xl/worksheets/sheet1.xml', 'spreadsheetml.worksheet'), ('xl/styles.xml', 'spreadsheetml.styles')], 'xl/workbook.xml')

ns = f'xmlns:a="{A}" xmlns:r="{R}" xmlns:p="{P}"'
tree = '<p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree>'
colors = dict(dk1='000000', lt1='FFFFFF', dk2='1F497D', lt2='EEECE1', accent1='4F81BD', accent2='C0504D', accent3='9BBB59', accent4='8064A2', accent5='4BACC6', accent6='F79646', hlink='0000FF', folHlink='800080')
color_scheme = ''.join(f'<a:{key}><a:srgbClr val="{value}"/></a:{key}>' for key, value in colors.items())
fonts = ''.join(f'<a:{kind}Font><a:latin typeface="Arial"/><a:ea typeface=""/><a:cs typeface=""/></a:{kind}Font>' for kind in ('major', 'minor'))
fill = '<a:solidFill><a:schemeClr val="phClr"/></a:solidFill>'
line = '<a:ln w="9525" cap="flat" cmpd="sng" algn="ctr">' + fill + '<a:prstDash val="solid"/><a:miter lim="800000"/></a:ln>'
fmt = '<a:fmtScheme name="Office"><a:fillStyleLst>' + fill * 3 + '</a:fillStyleLst><a:lnStyleLst>' + line * 3 + '</a:lnStyleLst><a:effectStyleLst>' + '<a:effectStyle><a:effectLst/></a:effectStyle>' * 3 + '</a:effectStyleLst><a:bgFillStyleLst>' + fill * 3 + '</a:bgFillStyleLst></a:fmtScheme>'
parts = {
    'ppt/presentation.xml': f'<p:presentation {ns}><p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId1"/></p:sldMasterIdLst><p:sldIdLst><p:sldId id="256" r:id="rId2"/></p:sldIdLst><p:sldSz cx="12192000" cy="6858000"/><p:notesSz cx="6858000" cy="9144000"/><p:defaultTextStyle><a:defPPr><a:defRPr lang="en-US"/></a:defPPr></p:defaultTextStyle></p:presentation>',
    'ppt/_rels/presentation.xml.rels': rels(('slideMaster', 'slideMasters/slideMaster1.xml'), ('slide', 'slides/slide1.xml'), ('theme', 'theme/theme1.xml')),
    'ppt/slides/slide1.xml': f'<p:sld {ns}><p:cSld>{tree}</p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sld>',
    'ppt/slides/_rels/slide1.xml.rels': rels(('slideLayout', '../slideLayouts/slideLayout1.xml')),
    'ppt/slideLayouts/slideLayout1.xml': f'<p:sldLayout {ns} type="blank" preserve="1"><p:cSld name="Blank">{tree}</p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sldLayout>',
    'ppt/slideLayouts/_rels/slideLayout1.xml.rels': rels(('slideMaster', '../slideMasters/slideMaster1.xml')),
    'ppt/slideMasters/slideMaster1.xml': f'<p:sldMaster {ns}><p:cSld><p:bg><p:bgPr><a:solidFill><a:srgbClr val="FFFFFF"/></a:solidFill><a:effectLst/></p:bgPr></p:bg>{tree}</p:cSld><p:clrMap accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" bg1="lt1" bg2="lt2" folHlink="folHlink" hlink="hlink" tx1="dk1" tx2="dk2"/><p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst><p:txStyles><p:titleStyle/><p:bodyStyle/><p:otherStyle><a:lvl1pPr><a:defRPr sz="1800"><a:solidFill><a:schemeClr val="tx1"/></a:solidFill><a:latin typeface="Arial"/></a:defRPr></a:lvl1pPr></p:otherStyle></p:txStyles></p:sldMaster>',
    'ppt/slideMasters/_rels/slideMaster1.xml.rels': rels(('slideLayout', '../slideLayouts/slideLayout1.xml'), ('theme', '../theme/theme1.xml')),
    'ppt/theme/theme1.xml': f'<a:theme xmlns:a="{A}" name="Office"><a:themeElements><a:clrScheme name="Office">{color_scheme}</a:clrScheme><a:fontScheme name="Office">{fonts}</a:fontScheme>{fmt}</a:themeElements><a:objectDefaults/><a:extraClrSchemeLst/></a:theme>',
}
write('pptx', parts, [('ppt/presentation.xml', 'presentationml.presentation.main'), ('ppt/slides/slide1.xml', 'presentationml.slide'), ('ppt/slideLayouts/slideLayout1.xml', 'presentationml.slideLayout'), ('ppt/slideMasters/slideMaster1.xml', 'presentationml.slideMaster'), ('ppt/theme/theme1.xml', 'theme')], 'ppt/presentation.xml')
