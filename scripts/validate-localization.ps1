[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$defaultStringsPath = Join-Path $projectRoot 'app\src\main\res\values\strings.xml'
$englishStringsPath = Join-Path $projectRoot 'app\src\main\res\values-en\strings.xml'

function Read-ResourceXml([string]$path) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Missing localization resource: $path"
    }
    [xml](Get-Content -LiteralPath $path -Raw -Encoding UTF8)
}

function Get-NamedNodes($document, [string]$xpath) {
    $nodes = @($document.SelectNodes($xpath))
    $map = @{}
    foreach ($node in $nodes) {
        $name = [string]$node.name
        if ([string]::IsNullOrWhiteSpace($name)) {
            throw "Localization node without a name at $xpath"
        }
        if ($map.ContainsKey($name)) {
            throw "Duplicate localization key: $name"
        }
        $map[$name] = $node
    }
    $map
}

function Get-FormatTokens([string]$value) {
    @([regex]::Matches($value, '%(?:\d+\$)?[a-zA-Z]') | ForEach-Object { $_.Value } | Sort-Object)
}

function Assert-SameKeys($source, $translation, [string]$resourceType) {
    $missing = @($source.Keys | Where-Object { -not $translation.ContainsKey($_) } | Sort-Object)
    $extra = @($translation.Keys | Where-Object { -not $source.ContainsKey($_) } | Sort-Object)
    if ($missing.Count -gt 0 -or $extra.Count -gt 0) {
        throw "$resourceType keys differ. Missing: $($missing -join ', '); extra: $($extra -join ', ')"
    }
}

$defaultXml = Read-ResourceXml $defaultStringsPath
$englishXml = Read-ResourceXml $englishStringsPath
$defaultStrings = Get-NamedNodes $defaultXml '/resources/string'
$englishStrings = Get-NamedNodes $englishXml '/resources/string'
$defaultPlurals = Get-NamedNodes $defaultXml '/resources/plurals'
$englishPlurals = Get-NamedNodes $englishXml '/resources/plurals'

Assert-SameKeys $defaultStrings $englishStrings 'String'
Assert-SameKeys $defaultPlurals $englishPlurals 'Plural'

foreach ($name in $defaultStrings.Keys) {
    $translatedValue = [string]$englishStrings[$name].InnerText
    if ([string]::IsNullOrWhiteSpace($translatedValue)) {
        throw "Empty English translation: $name"
    }
    $sourceTokens = @(Get-FormatTokens ([string]$defaultStrings[$name].InnerText))
    $translationTokens = @(Get-FormatTokens $translatedValue)
    if (($sourceTokens -join '|') -ne ($translationTokens -join '|')) {
        throw "Format tokens differ for $name. Source: $($sourceTokens -join ', '); English: $($translationTokens -join ', ')"
    }
}

foreach ($name in $defaultPlurals.Keys) {
    $sourceItems = @{}
    foreach ($item in @($defaultPlurals[$name].item)) {
        $sourceItems[[string]$item.quantity] = [string]$item.InnerText
    }
    $translationItems = @{}
    foreach ($item in @($englishPlurals[$name].item)) {
        $translationItems[[string]$item.quantity] = [string]$item.InnerText
    }
    Assert-SameKeys $sourceItems $translationItems "Plural quantity for $name"
    foreach ($quantity in $sourceItems.Keys) {
        if ([string]::IsNullOrWhiteSpace($translationItems[$quantity])) {
            throw "Empty English plural translation: $name/$quantity"
        }
        $sourceTokens = @(Get-FormatTokens $sourceItems[$quantity])
        $translationTokens = @(Get-FormatTokens $translationItems[$quantity])
        if (($sourceTokens -join '|') -ne ($translationTokens -join '|')) {
            throw "Format tokens differ for $name/$quantity"
        }
    }
}

$hardcodedTextRoots = @(
    (Join-Path $projectRoot 'app\src\main\java'),
    (Join-Path $projectRoot 'app\src\main\res\layout'),
    (Join-Path $projectRoot 'app\src\main\res\xml')
)
$hardcodedFiles = @()
foreach ($root in $hardcodedTextRoots) {
    if (Test-Path -LiteralPath $root) {
        $hardcodedFiles += @(Get-ChildItem -LiteralPath $root -File -Recurse)
    }
}
$hardcodedFiles += @(Get-Item -LiteralPath (Join-Path $projectRoot 'app\src\main\AndroidManifest.xml'))
$hardcodedMatches = @($hardcodedFiles | Select-String -Pattern '[\u4e00-\u9fff]')
if ($hardcodedMatches.Count -gt 0) {
    $locations = @($hardcodedMatches | ForEach-Object { "$($_.Path):$($_.LineNumber)" })
    throw "Hardcoded Chinese user-facing text found outside string resources: $($locations -join ', ')"
}

Write-Host "Localization validation passed: $($defaultStrings.Count) strings, $($defaultPlurals.Count) plurals, no hardcoded Chinese UI text."
