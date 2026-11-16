# PoseLib 打包脚本：aapt2 + javac + d8 + zipalign + apksigner
param(
    [switch]$SkipAssets
)
$ErrorActionPreference = "Stop"

$ROOT = "C:\PoseLibDev\PoseLibApp"
$DEV = "C:\PoseLibDev"
$OUT = "$DEV\build"
$LOG = "$OUT\build.log"

New-Item -ItemType Directory -Force -Path $OUT | Out-Null

function Log($m) { $m | Tee-Object -FilePath $LOG -Append }

# ---------- 工具链定位 ----------
$JDK = (Get-ChildItem "$DEV" -Directory | Where-Object { Test-Path "$($_.FullName)\bin\javac.exe" } | Select-Object -First 1).FullName
if (-not $JDK) { throw "JDK not found in $DEV" }
$env:JAVA_HOME = $JDK
$env:Path = "$JDK\bin;$env:Path"

$BT = (Get-ChildItem "$DEV" -Recurse -Filter "aapt2.exe" -ErrorAction SilentlyContinue |
    Select-Object -First 1).Directory.FullName
if (-not $BT) { throw "aapt2 not found" }
$ANDROID_JAR = (Get-ChildItem "$DEV" -Recurse -Filter "android.jar" -ErrorAction SilentlyContinue |
    Sort-Object Length -Descending | Select-Object -First 1).FullName
if (-not (Test-Path $ANDROID_JAR)) { throw "android.jar not found" }

$aapt2 = "$BT\aapt2.exe"
$d8bat = "$BT\d8.bat"
$zipalign = "$BT\zipalign.exe"
$apksigner = "$BT\apksigner.bat"
foreach ($t in @($aapt2, $d8bat, $zipalign, $apksigner)) { if (-not (Test-Path $t)) { throw "missing $t" } }
Log "JDK=$JDK"
Log "BT=$BT"

# ---------- 版本号自动递增 ----------
$VERFILE = "$ROOT\tools\version.txt"
if (Test-Path $VERFILE) {
    $parts = (Get-Content $VERFILE -Raw).Trim().Split(".")
} else {
    $parts = @("1", "0", "0")
}
$major = [int]$parts[0]; $minor = [int]$parts[1]; $patch = [int]$parts[2] + 1
$versionName = "$major.$minor.$patch"
$versionCode = $patch + ($minor * 100) + ($major * 1000)
"$versionName" | Out-File -FilePath $VERFILE -Encoding utf8 -NoNewline
# versionCode 单独记录
$codeFile = "$ROOT\tools\versioncode.txt"
$lastCode = 0
if (Test-Path $codeFile) { $lastCode = [int]((Get-Content $codeFile -Raw).Trim()) }
if ($versionCode -le $lastCode) { $versionCode = $lastCode + 1 }
"$versionCode" | Out-File -FilePath $codeFile -Encoding utf8 -NoNewline
Log "version: $versionName ($versionCode)"

# ---------- 清单写版本 ----------
$mf = Get-Content "$ROOT\AndroidManifest.xml" -Raw -Encoding utf8
$mf = $mf -replace 'android:versionCode="\d+"', "android:versionCode=`"$versionCode`""
$mf = $mf -replace 'android:versionName="[^"]*"', "android:versionName=`"$versionName`""
$mfPath = "$OUT\AndroidManifest.xml"
[IO.File]::WriteAllText($mfPath, $mf, (New-Object Text.UTF8Encoding $false))

# ---------- 清目录 ----------
$resZip = "$OUT\res.zip"
$genDir = "$OUT\gen"
$clsDir = "$OUT\classes"
$dexDir = "$OUT\dex"
foreach ($d in @($genDir, $clsDir, $dexDir)) {
    if (Test-Path $d) { Remove-Item -LiteralPath $d -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $d | Out-Null
}
if (Test-Path $resZip) { Remove-Item -LiteralPath $resZip -Force }

# ---------- aapt2 compile ----------
Log "aapt2 compile..."
& $aapt2 compile --dir "$ROOT\res" -o $resZip 2>&1 | Tee-Object -FilePath $LOG -Append
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed" }

# ---------- aapt2 link ----------
Log "aapt2 link..."
$baseApk = "$OUT\base.apk"
if (Test-Path $baseApk) { Remove-Item -LiteralPath $baseApk -Force }
& $aapt2 link -o $baseApk -I $ANDROID_JAR --manifest $mfPath -A "$ROOT\assets" `
    --java $genDir -v $resZip 2>&1 | Tee-Object -FilePath $LOG -Append
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed" }

# ---------- javac ----------
Log "javac..."
$srcs = @()
$srcs += (Get-ChildItem "$ROOT\java" -Recurse -Filter *.java | ForEach-Object { $_.FullName })
$srcs += (Get-ChildItem $genDir -Recurse -Filter *.java | ForEach-Object { $_.FullName })
$argFile = "$OUT\javac.txt"
$argLines = ($srcs | ForEach-Object { '"' + $_.Replace("\", "/") + '"' }) -join "`r`n"
[IO.File]::WriteAllText($argFile, $argLines, (New-Object Text.UTF8Encoding $false))
& "$JDK\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -bootclasspath $ANDROID_JAR `
    -classpath $ANDROID_JAR -d $clsDir "@$argFile" 2>&1 | Tee-Object -FilePath $LOG -Append
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

# ---------- d8 ----------
Log "d8..."
$classesJar = "$OUT\classes.jar"
if (Test-Path $classesJar) { Remove-Item -LiteralPath $classesJar -Force }
Push-Location $clsDir
& "$JDK\bin\jar.exe" cf $classesJar . 2>&1 | Tee-Object -FilePath $LOG -Append
Pop-Location
& "$JDK\bin\java.exe" -cp "$BT\lib\d8.jar" com.android.tools.r8.D8 --release --min-api 21 `
    --lib $ANDROID_JAR --output $dexDir $classesJar 2>&1 | Tee-Object -FilePath $LOG -Append
if (-not (Test-Path "$dexDir\classes.dex")) { throw "d8 failed: no classes.dex" }

# ---------- 组装 APK ----------
Log "packaging..."
$unsigned = "$OUT\unsigned.apk"
if (Test-Path $unsigned) { Remove-Item -LiteralPath $unsigned -Force }
Copy-Item -LiteralPath $baseApk -Destination $unsigned
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
$zip = [System.IO.Compression.ZipFile]::Open($unsigned, [System.IO.Compression.ZipArchiveMode]::Update)
$dexFile = "$dexDir\classes.dex"
try {
    $entry = $zip.GetEntry("classes.dex")
    if ($entry) { $entry.Delete() }
    [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $dexFile, "classes.dex",
        [IO.Compression.CompressionLevel]::NoCompression) | Out-Null
} finally {
    $zip.Dispose()
}

# ---------- 修正 assets 路径分隔符 ----------
# aapt2 在 Windows 上会把 assets 子目录写成反斜杠（assets/lib\x.jpg），
# 手机端 AssetManager.open("lib/x.jpg") 会读不到，必须在签名前统一改成正斜杠。
Log "fix asset paths..."
$fixedApk = "$OUT\unsigned_fixed.apk"
if (Test-Path $fixedApk) { Remove-Item -LiteralPath $fixedApk -Force }
$srcZip = [System.IO.Compression.ZipFile]::OpenRead($unsigned)
$fixedFs = [System.IO.File]::Create($fixedApk)
$dstZip = New-Object System.IO.Compression.ZipArchive($fixedFs,
    [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($e in $srcZip.Entries) {
        $name = $e.FullName.Replace("\", "/")
        if ($name.EndsWith("/")) { continue }
        $ne = $dstZip.CreateEntry($name,
            [System.IO.Compression.CompressionLevel]::NoCompression)
        $os = $ne.Open()
        if ($e.Length -gt 0) {
            $is = $e.Open()
            $is.CopyTo($os)
            $is.Dispose()
        }
        $os.Dispose()
    }
} finally {
    $dstZip.Dispose()
    $fixedFs.Dispose()
    $srcZip.Dispose()
}
Move-Item -LiteralPath $fixedApk -Destination $unsigned -Force
$bsLeft = ([System.IO.Compression.ZipFile]::OpenRead($unsigned).Entries |
    Where-Object { $_.FullName -like "*\*" }).Count
Log "backslash entries left: $bsLeft"
if ($bsLeft -ne 0) { throw "asset path fix failed" }

# ---------- zipalign ----------
$aligned = "$OUT\aligned.apk"
if (Test-Path $aligned) { Remove-Item -LiteralPath $aligned -Force }
& $zipalign -f -p 4 $unsigned $aligned 2>&1 | Tee-Object -FilePath $LOG -Append
if ($LASTEXITCODE -ne 0) { throw "zipalign failed" }

# ---------- 签名 ----------
$KS = "$ROOT\tools\poselib.keystore"
if (-not (Test-Path $KS)) {
    Log "generating keystore..."
    & "$JDK\bin\keytool.exe" -genkeypair -v -keystore $KS -storetype JKS `
        -keyalg RSA -keysize 2048 -validity 10950 -alias poselib `
        -storepass poselib123 -keypass poselib123 `
        -dname "CN=PoseLib, OU=PoseLib, O=PoseLib, L=Xi'an, ST=Shaanxi, C=CN" 2>&1 |
        Tee-Object -FilePath $LOG -Append
    if ($LASTEXITCODE -ne 0) { throw "keytool failed" }
}

$signed = "$OUT\美拍姿势库V$versionName.apk"
if (Test-Path $signed) { Remove-Item -LiteralPath $signed -Force }
& "$JDK\bin\java.exe" -cp "$BT\lib\apksigner.jar" com.android.apksigner.ApkSignerTool sign `
    --ks $KS --ks-key-alias poselib --ks-pass pass:poselib123 --key-pass pass:poselib123 `
    --min-sdk-version 21 --out $signed $aligned 2>&1 | Tee-Object -FilePath $LOG -Append
if (-not (Test-Path $signed)) { throw "apksigner failed" }

# ---------- 校验 ----------
Log "verify..."
& "$JDK\bin\java.exe" -cp "$BT\lib\apksigner.jar" com.android.apksigner.ApkSignerTool verify `
    --print-certs $signed 2>&1 | Tee-Object -FilePath $LOG -Append
$sz = [math]::Round((Get-Item $signed).Length / 1MB, 1)
Log "APK: $signed  ($sz MB)"

# ---------- 输出到桌面 ----------
$desktop = [Environment]::GetFolderPath("Desktop")
$final = "$desktop\美拍姿势库V$versionName.apk"
Copy-Item -LiteralPath $signed -Destination $final -Force
Log "COPIED: $final"
Write-Host "OK $final $sz MB version=$versionName"
