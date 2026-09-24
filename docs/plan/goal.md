Objective:
Deliver the complete ProgressiveStages 3.1.0 plan. Successful completion is permitted only when every mandatory requirement and every stable-release gate passes, the authoritative default-branch commit is verified, every release artifact is bound to that commit, required runtime verification passes, both platform downloads match, the released editor and guide work, documentation, wiki, and issue 57 converge, the final plan-wide audit passes, and no known mandatory repository-owned defect remains.

Immediate checkpoint:
Active phase state: /mnt/hermes/projects/ProgressiveStages/docs/plan/active_phase.md
Read phase ID, plan path, and first unfinished task or gate. Preserve the dirty checkout and unrelated work. Reverify `origin/master`, ownership, signing, checks, and milestone. Perform one bounded inspection that ends as soon as each mandatory criterion is classified as implemented with valid evidence, incomplete, stale evidence, or externally blocked. Immediately execute the first incomplete or stale-evidence criterion. The requirement map is not a deliverable. Do not stop after producing the map, do not rebuild it from unchanged evidence, and do not produce a narrative audit before implementation.

Authoritative plan:
Plan: /mnt/hermes/projects/ProgressiveStages/docs/general/plan.md
Plan SHA-256: 66219974c8a5ebc22e6ea2d9db56f3583e93a42c68c4020a9d25ac045a90274a
Plan manifest: /mnt/hermes/projects/ProgressiveStages/docs/general/plan.index.json
Plan set SHA-256: 0e50570c5caeebc42f4ae534a27b2041a74232ef3de38afc61a0543fc8c2561a
Phase plans directory: /mnt/hermes/projects/ProgressiveStages/docs/general/phases
Completion endpoint: ProgressiveStages 3.1.0 is tested, integrated through checked master pull requests and signed phase tags, published on CurseForge and Modrinth with matching verified artifacts, and its updated mod-served web editor and in-game guide are verified from the released JAR. Documentation, wiki and issue 57 reflect the verified result.

Plan digests covering complete registered plan set are creation-time provenance, not runtime locks. Current plan is live product contract. At start, resumption, compaction, transition, or plan changes, reread current authoritative plan through EOF: `plan.md`, `plan.index.json`, `plan.handoff.json`, all registered plans, `docs/plan/active_phase.md`, and selected phase. Classify current plan changes. Routine progress, evidence, status, clarification, and phase transitions continue without owner input. Never stop solely for plan or handoff digest drift.

Repository root: /mnt/hermes/projects/ProgressiveStages
Observed checkout branch: envy/remove-completed-plan
Observed checkout commit: f7ae37bb80679d8ea359a7e0e315d1edae51d67a

Authoritative remote:
origin
https://github.com/MCEnvision/ProgressiveStages.git

Observed local default branch: master
Observed local default-branch commit: c0a3ef2c32a5406a0bf12e43d2c475aa576fe9d6
Observed local remote-tracking ref: origin/master
Observed local remote-tracking commit: c0a3ef2c32a5406a0bf12e43d2c475aa576fe9d6
Current remote default-branch head: c0a3ef2c32a5406a0bf12e43d2c475aa576fe9d6
Remote-head evidence: git ls-remote origin refs/heads/master and GitHub branches/master API, observed 2026-09-24 12:58:15.598882+00:00
Authoritative working baseline: established
Applicable implementation branch: none identified at checkpoint
Applicable open pull request: none identified at checkpoint

Execution behavior:
Read the active phase plan through EOF and pass its evidence and exit criteria. Complete integration and verify resulting default branch and signed phase tag before next phase. Never stack phase branches. Advance `active_phase.md` only after exit, audit, integration, default branch, tag, and cleanup pass, using receipt, expected digest, and `/home/envy/.codex/skills/goal-creator/scripts/advance_active_phase.py`. Reread the next contiguous phase and continue remaining mandatory work under same immutable goal with separate phase cursor. Do not repeat completed work. Validate consequential assumptions, investigate failures with planned diagnostics, and resolve ordinary engineering choices autonomously. Documentation changes do not substitute for implementation.

Verify the plan, repository identity, package metadata, and remote describe the same project, and verify `origin` is the intended repository. Fetch `origin` without altering the remote; compare the fetched remote-tracking ref with the current remote default-branch head. Refresh and inspect repository state without altering the remote. Classify the local default branch as equal, behind, ahead, or diverged; fast-forward only when safe. Do not reset, force, discard, or overwrite history. Search local branches, remote branches, and repository-wide open pull requests. Resume applicable work; otherwise branch from the verified authoritative baseline. Do not invent a branch when an applicable active branch exists. Create or resume the implementation branch before modifying tracked files. Do not commit directly to master. Default branch reconciliation permits safe fast-forward only and authorized pull-request integration.

Guardrails and authority:
Preserve completed or uncommitted work, unrelated files, branches, tags, and recovery paths. DEC-001 through DEC-011 are resolved. Optional and future work, FUT-001 through FUT-005, remains excluded. Publications and editor are authorized. Use EnVy, `contact.enviouse@gmail.com`, as sole author and committer with registered EnVisione SSH key. Keep credentials in approved mechanisms, outside outputs and evidence. Public prose uses maintainer voice and excludes private drafting details. `docs/plan/goal.md` is immutable; never refresh, rewrite, rebind, overwrite, or replace it. Executors never invoke Plan Creator, Plan Maintainer, or Goal Creator or spawn their authors. Only EnVy's direct current request authorizes them. Avoid dependency or lockfile churn. Perform safe in-scope next action without permission or passive waiting.

Verification and stopping:
Verify real behavior at highest fidelity. Never weaken, skip, disable, delete, narrow, or reclassify valid tests. Never suppress a valid failure, ignore a required exit code, reduce a required threshold, or mark a required check as allowed to fail. Never introduce a production bypass solely for tests. Never substitute mocked behavior for required real proof. If a test contradicts the plan or contract, prove it and replace it with equal or stronger coverage. Fix the root cause, add regression coverage, rerun affected gates, and audit adjacent risks and recovery.

Use `node-1` only for headless compute, dedicated servers, and server-only tests; use verified discrete-GPU Linux laptop for clients and graphics. Zero isolated client master output before launch; bind its Hyprland window and PID to stream, verify mute, and remute recreated streams. Stop if identity or mute unproven. Match candidate, configuration, loader, and dependencies across hosts; verify intended player joined exact endpoint on both sides. Console fixtures never replace player or UI proof. Singleplayer requires identified integrated-server defect. Set and read back `eula=true` for disposable servers. Register and verify cleanup.

Before integration run `git status`, `git diff --check`, and `git log`; reject secret-bearing files and unintended changes, then verify the authoritative remote branch and artifact identity. After integration inspect exact authoritative merged default-branch commit. Rerun gates affected by merge resolution, release state, or default-branch configuration; never rely only on pre-merge evidence. Final proof includes regressions, real server events, Brave and silent multiplayer workflows, exact candidate JAR, both accepted downloads and hashes, released editor and guide, diagnostics, documentation/wiki/issue 57 convergence, and cleanup. The final phase must pass the plan-wide Definition of Done.

Permitted terminal states: `SUCCESS` only after endpoint and Definition of Done. `PLAN_MAINTENANCE_REQUIRED` only for material contract change, with affected stable IDs and owner decision. `GOAL_REVISION_CONFLICT` only for changed goal, with expected goal digest and observed goal digest. Attempt safe cursor reconstruction from registered plan and completed-phase evidence before `ACTIVE_PHASE_STATE_CONFLICT`; report expected cursor digest and observed cursor digest. `OWNER_INPUT_REQUIRED — REPOSITORY MISMATCH` is identity conflict; `REPOSITORY_STATE_CONFLICT` is unsafe history. Before returning repository states, attempt safe non-destructive resolution from repository metadata and remote evidence. Plan or handoff digest drift is never a stopping state. No other early stopping state is permitted.

Continuity:
Maintain a terse ledger of completed phase gates, evidence, hashes, runtime identity, receipts, blockers, cleanup, and next contiguous phase or action. The requirement map and ledger are temporary internal continuity state; unless required evidence, do not commit or publish them or add them to `plan.md`, `status.md`, issues, pull requests, or repository documentation. Do not rerun the same unchanged failing check more than twice without changing code, configuration, environment, instrumentation, or diagnostic hypothesis. Reuse evidence while inputs remain unchanged. Bound external retries, complete independent mandatory actions, and never wait passively; owned defects remain work. Genuine blocker records name exact prerequisite, unavailability evidence, attempted operation, required external action, and resume verification. Only `active_phase.md` transitions; `goal.md` remains immutable and Plan Creator artifacts stay unchanged.
