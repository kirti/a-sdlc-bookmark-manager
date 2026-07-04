# Kotlin scaffold

A minimal, dependency-light Kotlin/Gradle project. Use this to confirm your local
Kotlin + Gradle + JDK setup works before touching the full `a-sdlc` orchestrator
project — it's a 2-file scaffold, not a subset of that project.

## Run it

```bash
./gradlew run
```

Expected output:

```
Kotlin: 2.0.21
JVM:    21...
[UppercaseAgent] "hello a-sdlc" -> "HELLO A-SDLC"
[ReverseAgent] "HELLO A-SDLC" -> "CLDS-A OLLEH"

Final result: CLDS-A OLLEH

If you see this, your local Kotlin/Gradle/JDK setup works.
```

If that prints, your toolchain is good and you're ready to work with the full project.

## Run the tests

```bash
./gradlew test
```

Three trivial JUnit5 tests in `src/test/kotlin/MiniPipelineTest.kt` — a working example
of the standard `kotlin("test")` setup (unlike the full project's target-app, which uses
a custom test runner for reasons specific to how *that* project was built and verified —
see its README if you're curious).

## What's here and why

- `Main.kt` defines a tiny `Agent` interface (one method) and a `MiniPipeline` that
  chains agents together, logging each step. This is the same shape as the real
  project's `Pipeline.kt` and the 12 agent classes under `orchestrator/src/main/kotlin/asdlc/agents/`
  — just shrunk to two toy agents so you can see the pattern without wading through
  the full pipeline.
- No external dependencies except `kotlin("test")` for the test task — this starts
  clean and lets you add real dependencies (an HTTP client, a JSON library, whatever)
  as you need them, rather than inheriting the full project's zero-dependency
  constraint (that constraint existed for a specific reason in that project — see its
  README — and doesn't apply here).

## Try extending it

Add a new `Agent` implementation in `Main.kt`:

```kotlin
class ExclaimAgent : Agent {
    override val name = "ExclaimAgent"
    override fun run(input: String): String = "$input!"
}
```

Add it to the pipeline:

```kotlin
val pipeline = MiniPipeline(listOf(UppercaseAgent(), ReverseAgent(), ExclaimAgent()))
```

Re-run `./gradlew run` and watch the new step appear in the log. This is the smallest
possible version of what the full orchestrator's `Pipeline.executeTasks()` does across
12 real agents with retry loops and human gates — same idea, much smaller.
