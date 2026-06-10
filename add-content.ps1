$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$pptx = 'C:\Users\mayan\IdeaProjects\chubb_Insurance\presentation_from_images.pptx'

$PURPLE = '5B2D90'
$DETAIL = '404040'
$FLAT   = '333333'
$SUBBUL = '7A7A7A'

function Esc([string]$s) {
  $s = $s -replace '&', '&amp;'
  $s = $s -replace '<', '&lt;'
  $s = $s -replace '>', '&gt;'
  return $s
}

# Styles: 'head' = section header (bold purple, dot), 'sub' = detail (gray, en-dash),
#         'flat' = single-level point (purple dot, dark gray, larger)
function Para($text, $style) {
  $t = Esc $text
  switch ($style) {
    'head' {
      return '<a:p><a:pPr marL="285750" indent="-285750"><a:lnSpc><a:spcPct val="108000"/></a:lnSpc><a:spcBef><a:spcPts val="1200"/></a:spcBef>' +
             '<a:buClr><a:srgbClr val="' + $PURPLE + '"/></a:buClr><a:buFont typeface="Arial"/><a:buChar char="&#8226;"/></a:pPr>' +
             '<a:r><a:rPr lang="en-US" sz="1800" b="1" dirty="0"><a:solidFill><a:srgbClr val="' + $PURPLE + '"/></a:solidFill></a:rPr><a:t>' + $t + '</a:t></a:r></a:p>'
    }
    'sub' {
      return '<a:p><a:pPr marL="742950" lvl="1" indent="-285750"><a:lnSpc><a:spcPct val="108000"/></a:lnSpc><a:spcBef><a:spcPts val="300"/></a:spcBef>' +
             '<a:buClr><a:srgbClr val="' + $SUBBUL + '"/></a:buClr><a:buFont typeface="Arial"/><a:buChar char="&#8211;"/></a:pPr>' +
             '<a:r><a:rPr lang="en-US" sz="1450" dirty="0"><a:solidFill><a:srgbClr val="' + $DETAIL + '"/></a:solidFill></a:rPr><a:t>' + $t + '</a:t></a:r></a:p>'
    }
    'flat' {
      return '<a:p><a:pPr marL="285750" indent="-285750"><a:lnSpc><a:spcPct val="112000"/></a:lnSpc><a:spcBef><a:spcPts val="900"/></a:spcBef>' +
             '<a:buClr><a:srgbClr val="' + $PURPLE + '"/></a:buClr><a:buFont typeface="Arial"/><a:buChar char="&#8226;"/></a:pPr>' +
             '<a:r><a:rPr lang="en-US" sz="1650" dirty="0"><a:solidFill><a:srgbClr val="' + $FLAT + '"/></a:solidFill></a:rPr><a:t>' + $t + '</a:t></a:r></a:p>'
    }
  }
}

function BodyShape($bullets) {
  $paras = ($bullets | ForEach-Object { Para $_.T $_.S }) -join ''
  $sp = '<p:sp><p:nvSpPr><p:cNvPr id="100" name="Body Content"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>' +
        '<p:spPr><a:xfrm><a:off x="731520" y="1188720"/><a:ext cx="10728960" cy="4937760"/></a:xfrm>' +
        '<a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>' +
        '<p:txBody><a:bodyPr wrap="square" anchor="ctr" rtlCol="0"><a:normAutofit/></a:bodyPr><a:lstStyle/>' +
        $paras + '</p:txBody></p:sp>'
  return $sp
}

$content = @{
  'slide3.xml' = @(
    @{T='Layered architecture with a domain core - dependencies point inward'; S='head'},
    @{T='api -> service -> domain;  infrastructure -> domain'; S='sub'},
    @{T='domain is framework/persistence-free - pure business models'; S='sub'},
    @{T='api never touches infrastructure directly - always via service'; S='sub'},
    @{T='DTOs stay in api; JPA entities stay in infrastructure; only domain models cross'; S='sub'},
    @{T='Explicit mapper at every boundary'; S='head'},
    @{T='RequestDtoToDomain, DomainToResponseDto, PolicyMapper'; S='sub'},
    @{T='Key decisions'; S='head'},
    @{T='Contract-first OpenAPI is the source of truth'; S='sub'},
    @{T='Specification pattern for composable, injection-safe filtering'; S='sub'},
    @{T='PolicyExpiry centralizes the 30-day expiring-soon rule'; S='sub'},
    @{T='@RestControllerAdvice for safe errors - no stack traces'; S='sub'},
    @{T='CorrelationIdFilter + SLF4J MDC for request tracing'; S='sub'},
    @{T='Enforced rules: <=200-line files, <=50-line methods, complexity <4, Optional over null'; S='sub'}
  )
  'slide4.xml' = @(
    @{T='Repository'; S='head'},
    @{T='Remote: github.com/may2313ank/chubb-insurance.git'; S='sub'},
    @{T='Default branch: main'; S='sub'},
    @{T='target/ and Maven wrapper internals are git-ignored'; S='sub'},
    @{T='Working platform'; S='head'},
    @{T='OS: Windows 11'; S='sub'},
    @{T='Java 21 (LTS); build via Maven 3.9 wrapper (./mvnw)'; S='sub'},
    @{T='IDE: IntelliJ IDEA; AI assistant: Claude Code'; S='sub'},
    @{T='DB: H2 in-memory (dev/test); PostgreSQL 16 target'; S='sub'},
    @{T='Tech stack (pinned)'; S='head'},
    @{T='Spring Boot 3.4.1 (latest - 1, for stability)'; S='sub'},
    @{T='springdoc-openapi (Swagger UI) 2.7.0'; S='sub'},
    @{T='JUnit 5 via spring-boot-starter-test'; S='sub'}
  )
  'slide5.xml' = @(
    @{T='Contract-first OpenAPI 3.0.3 - served at /swagger-ui.html from the static YAML'; S='flat'},
    @{T='Base path: /api/v1/policies'; S='flat'},
    @{T='GET /api/v1/policies - paginated list; filters (status, lineOfBusiness, region, date range) + free-text q; page/size/sort'; S='flat'},
    @{T='GET /api/v1/policies/{id} - full detail of one policy by UUID'; S='flat'},
    @{T='PATCH /api/v1/policies/flag - bulk-flag by id list; returns requested/flagged/missing'; S='flat'},
    @{T='GET /api/v1/policies/summary - counts by status, premium by line of business, expiring-soon count'; S='flat'},
    @{T='Frontend shaping: display values (Active, Singapore), isExpiringSoon, totalElements/totalPages, {amount, currency}'; S='flat'},
    @{T='Errors: standard ErrorResponse; 400 / 404 / 503; never exposes stack traces'; S='flat'}
  )
  'slide6.xml' = @(
    @{T='26 tests, all green - layered to mirror the architecture'; S='flat'},
    @{T='PolicyServiceTest (5) - unit (Mockito): business logic & 404 path'; S='flat'},
    @{T='PolicyControllerTest (8) - web slice (@WebMvcTest + MockMvc): endpoints, validation, JSON shape'; S='flat'},
    @{T='CorrelationIdFilterTest (3) - unit: ID generation, inbound-header reuse, MDC cleanup'; S='flat'},
    @{T='PolicySpecificationFactoryTest (4) - unit: filter predicate construction'; S='flat'},
    @{T='PolicyRepositoryIntegrationTest (6) - integration (@DataJpaTest + H2): filtering, search, aggregation, expiry'; S='flat'},
    @{T='Principles: unit for service & controller; integration incl. DB; naming method_scenario_outcome; error paths (400/404/503) covered'; S='flat'}
  )
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$zip = [System.IO.Compression.ZipFile]::Open($pptx, [System.IO.Compression.ZipArchiveMode]::Update)
try {
  foreach ($name in $content.Keys) {
    $entry = $zip.Entries | Where-Object { $_.FullName -eq "ppt/slides/$name" }
    if ($null -eq $entry) { throw "Entry not found: ppt/slides/$name" }

    $reader = New-Object System.IO.StreamReader($entry.Open(), $utf8NoBom)
    $xml = $reader.ReadToEnd()
    $reader.Close()

    if ($xml -match 'name="Body Content"') { Write-Host "Skip (already has body): $name"; continue }

    $shape = BodyShape $content[$name]
    $newXml = $xml -replace '</p:spTree>', ($shape + '</p:spTree>')
    if ($newXml -eq $xml) { throw "spTree close tag not found in $name" }

    $stream = $entry.Open()
    $stream.SetLength(0)
    $writer = New-Object System.IO.StreamWriter($stream, $utf8NoBom)
    $writer.Write($newXml)
    $writer.Flush()
    $writer.Close()
    Write-Host "Updated: $name ($($content[$name].Count) bullets)"
  }
}
finally {
  $zip.Dispose()
}
Write-Host "DONE"
