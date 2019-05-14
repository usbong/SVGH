cd /d %1
java -cp .\software;.\software\org.apache.commons.text.jar generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFilesSVGH input/treatment/*.txt assets/*.txt
cd ..
"add-on software"\requirements\"chrome.exe - Shortcut.lnk" file:///%CD%/add-on%%20software/output/MonthlySummaryReportOutputTreatmentSVGH.html file:///%CD%/add-on%%20software/output/MonthlySummaryReportOfUnclassifiedDiagnosedCasesOutputSVGH.html
PAUSE