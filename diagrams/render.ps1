# Пересобирает PNG-картинки из PUML-исходников.
# Использует tools\plantuml.jar (скачивается автоматически, если отсутствует).

$ErrorActionPreference = "Stop"

$root      = Split-Path -Parent (Split-Path -Parent $PSCommandPath)
$toolsDir  = Join-Path $root "tools"
$jar       = Join-Path $toolsDir "plantuml.jar"
$diagrams  = Join-Path $root "diagrams"

if (-not (Test-Path $jar)) {
    Write-Host "Скачиваю plantuml.jar..."
    if (-not (Test-Path $toolsDir)) { New-Item -ItemType Directory -Path $toolsDir | Out-Null }
    $url = "https://github.com/plantuml/plantuml/releases/download/v1.2024.7/plantuml-1.2024.7.jar"
    Invoke-WebRequest -Uri $url -OutFile $jar -UseBasicParsing
}

Write-Host "Рендерю conceptual.puml и logical.puml в PNG..."
& java -jar $jar -charset UTF-8 -tpng `
    (Join-Path $diagrams "conceptual.puml") `
    (Join-Path $diagrams "logical.puml")

Write-Host ""
Write-Host "Готово. Картинки:"
Get-ChildItem (Join-Path $diagrams "*.png") | ForEach-Object {
    "{0,-30} {1,8} КБ" -f $_.Name, ([int]($_.Length / 1024))
}
