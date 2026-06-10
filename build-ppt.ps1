$ErrorActionPreference = 'Stop'

$outPath = 'C:\Users\mayan\IdeaProjects\chubb_Insurance\presentation.pptx'
if (Test-Path $outPath) { Remove-Item $outPath -Force }

# COM constants
$ppLayoutTitle = 1
$ppLayoutText  = 2
$ppSaveAsOpenXMLPresentation = 24

$slides = @(
  @{ Title = 'Architecture Design & Decisions'; Bullets = @(
      @{T='Layered architecture with a domain core - dependencies point inward'; L=1},
      @{T='api -> service -> domain;  infrastructure -> domain'; L=2},
      @{T='domain is framework/persistence-free - pure business models'; L=2},
      @{T='api never touches infrastructure directly - always via service'; L=2},
      @{T='DTOs stay in api; JPA entities stay in infrastructure; only domain models cross'; L=2},
      @{T='Explicit mapper at every boundary'; L=1},
      @{T='RequestDtoToDomain, DomainToResponseDto, PolicyMapper'; L=2},
      @{T='Key decisions'; L=1},
      @{T='Contract-first OpenAPI is the source of truth'; L=2},
      @{T='Specification pattern for composable, injection-safe filtering'; L=2},
      @{T='PolicyExpiry centralizes the 30-day expiring-soon rule'; L=2},
      @{T='@RestControllerAdvice for safe errors - no stack traces'; L=2},
      @{T='CorrelationIdFilter + SLF4J MDC for request tracing'; L=2},
      @{T='Enforced rules: <=200-line files, <=50-line methods, complexity <4, Optional over null'; L=2}
  )},
  @{ Title = 'GIT details & working platform'; Bullets = @(
      @{T='Repository'; L=1},
      @{T='Remote: github.com/may2313ank/chubb-insurance.git'; L=2},
      @{T='Default branch: main'; L=2},
      @{T='target/ and Maven wrapper internals are git-ignored'; L=2},
      @{T='Working platform'; L=1},
      @{T='OS: Windows 11'; L=2},
      @{T='Java 21 (LTS); build via Maven 3.9 wrapper (./mvnw)'; L=2},
      @{T='IDE: IntelliJ IDEA; AI assistant: Claude Code'; L=2},
      @{T='DB: H2 in-memory (dev/test); PostgreSQL 16 target'; L=2},
      @{T='Tech stack (pinned)'; L=1},
      @{T='Spring Boot 3.4.1 (latest - 1, for stability)'; L=2},
      @{T='springdoc-openapi (Swagger UI) 2.7.0'; L=2},
      @{T='JUnit 5 via spring-boot-starter-test'; L=2}
  )},
  @{ Title = 'API Specification'; Bullets = @(
      @{T='Contract-first OpenAPI 3.0.3 - served at /swagger-ui.html from the static YAML'; L=1},
      @{T='Base path: /api/v1/policies'; L=1},
      @{T='GET /api/v1/policies - paginated list; filters (status, lineOfBusiness, region, date range) + free-text q as query params; page/size/sort'; L=1},
      @{T='GET /api/v1/policies/{id} - full detail of one policy by UUID'; L=1},
      @{T='PATCH /api/v1/policies/flag - bulk-flag by id list; returns requested/flagged/missing'; L=1},
      @{T='GET /api/v1/policies/summary - counts by status, premium by line of business, expiring-soon count'; L=1},
      @{T='Frontend shaping: display values (Active, Singapore), isExpiringSoon, totalElements/totalPages, {amount, currency}'; L=1},
      @{T='Errors: standard ErrorResponse; 400 / 404 / 503; never exposes stack traces'; L=1}
  )},
  @{ Title = 'AI Working Journal'; Bullets = @(
      @{T='File: .claude/context/ai-journal.md - a complete prompt log of the AI-assisted build'; L=1},
      @{T='Each entry records'; L=1},
      @{T='When it was prompted (date)'; L=2},
      @{T='What was asked (the prompt)'; L=2},
      @{T='What the AI did (the response)'; L=2},
      @{T='Outcome: Accepted / Rejected / Challenged / Pending'; L=2},
      @{T='Why it matters'; L=1},
      @{T='End-to-end traceability and auditability of every change'; L=2},
      @{T='Captures challenges & course-corrections, not just successes'; L=2},
      @{T='Records why decisions were made and which assumptions were flagged'; L=2},
      @{T='Scale: 27 logged interactions; governed by .claude/rules/ + CLAUDE.md'; L=1}
  )},
  @{ Title = 'Testing Strategy'; Bullets = @(
      @{T='26 tests, all green - layered to mirror the architecture'; L=1},
      @{T='PolicyServiceTest (5) - unit (Mockito): business logic & 404 path'; L=1},
      @{T='PolicyControllerTest (8) - web slice (@WebMvcTest + MockMvc): endpoints, validation, JSON shape'; L=1},
      @{T='CorrelationIdFilterTest (3) - unit: ID generation, inbound-header reuse, MDC cleanup'; L=1},
      @{T='PolicySpecificationFactoryTest (4) - unit: filter predicate construction'; L=1},
      @{T='PolicyRepositoryIntegrationTest (6) - integration (@DataJpaTest + H2): filtering, search, aggregation, expiry'; L=1},
      @{T='Principles: unit for service & controller; integration incl. DB; naming method_scenario_outcome; error paths (400/404/503) covered'; L=1}
  )}
)

$msoTrue = -1
$app = New-Object -ComObject PowerPoint.Application
$app.Visible = $msoTrue
$pres = $app.Presentations.Add($msoTrue)

# Title slide
$titleSlide = $pres.Slides.Add(1, $ppLayoutTitle)
$titleSlide.Shapes.Title.TextFrame.TextRange.Text = 'Policy Overview Dashboard - BFF API'
$titleSlide.Shapes.Item(2).TextFrame.TextRange.Text = "APAC-2847  |  Architecture, API, Testing & AI-Assisted Delivery"

$index = 2
foreach ($s in $slides) {
  $slide = $pres.Slides.Add($index, $ppLayoutText)
  $slide.Shapes.Title.TextFrame.TextRange.Text = $s.Title

  $body = $slide.Shapes.Item(2).TextFrame.TextRange
  $text = ($s.Bullets | ForEach-Object { $_.T }) -join "`r"
  $body.Text = $text

  for ($i = 0; $i -lt $s.Bullets.Count; $i++) {
    $body.Paragraphs($i + 1).IndentLevel = $s.Bullets[$i].L
  }
  $index++
}

$pres.SaveAs($outPath, $ppSaveAsOpenXMLPresentation)
$pres.Close()
$app.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($pres) | Out-Null
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($app) | Out-Null
[GC]::Collect()
"OK: $outPath"
