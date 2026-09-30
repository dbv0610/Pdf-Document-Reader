You are fixing Kotlin compile errors in an Android library module that was just converted from Java to Kotlin.
Source root: android_document/src/main/java/com/wxiwei/office/ (all paths below are relative to it).

YOUR GROUP: __G__
YOU OWN (may edit) ONLY these files: __OWN__
Your current error list (refreshed automatically every few minutes by an external build loop; re-read it often):
  __S__/errors/__G__.txt
Build history (timestamps of refreshes): __S__/build_history.txt

Rules:
1. Edit ONLY files you own. Other groups are concurrently editing the rest of the tree. Never touch files outside your ownership.
2. Do NOT run gradle / gradlew / kotlinc and do NOT run any git command that changes state (no checkout, stash, reset, add, commit). Read-only git (git show HEAD:path, git diff) is fine: the original Java for any file is available via `git show HEAD:android_document/src/main/java/com/wxiwei/office/<path>.java`.
3. Most errors are cascades from the conversion: Java getters/setters became Kotlin properties (call `x.foo` not `x.getFoo()`; `x.isFoo` for booleans), nullability (T? vs T), raw types -> star projections, missing `override`, Java static -> companion objects, etc. Look up the actual Kotlin declaration of the called class before fixing a call site.
4. Prefer fixing the CALL SITE in your files. If an error in your file can only be fixed properly by changing a declaration in a file you do NOT own, append a short note to __S__/requests/__G__.md (file, line, what change is needed, why) and move on.
5. Preserve the original Java semantics. Do not delete logic, do not stub methods, do not add @Suppress to hide errors, do not convert files back to Java. Use `!!` only where the Java code clearly assumed non-null; prefer proper types.
6. Fixing a declaration in a file you own (e.g. adding `override`, fixing a signature to match its supertype, making a property nullable, adding a @JvmStatic/@JvmField for remaining Java callers) is fine and encouraged when it's the root cause.
7. Work through the whole error list. When done, wait ~3 minutes (sleep 180) and re-read the error file for newly surfaced errors in your files; repeat until your error file is empty or only contains errors you have documented in requests/__G__.md, or after at most 6 refresh cycles with no progress.
8. Finish with a brief summary: what you fixed, what remains, and what requests you logged.
