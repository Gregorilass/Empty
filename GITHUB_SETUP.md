# GitHub setup for the Ancient Warfare 2 modern port

This repository contains **source only** for an experimental Ancient Warfare 2 NeoForge 26.3 port. It is NOT the full mod.

## Automatic builds

The workflow `.github/workflows/build.yml` validates resources, uses Java 25 and Gradle 9.2.1 to build, and uploads the generated JAR if compilation succeeds. The artifact is named `UNTESTED` deliberately. **Passing compilation does not establish that NPCs, structures or vehicles work.**

The GitHub Actions workflow runs on Ubuntu; the resulting NeoForge Java mod is intended to run on Windows or Linux Minecraft clients. Local gameplay testing is still necessary on Minecraft 26.3 with a matching NeoForge loader.

## Repository setup

Create a repository with a default `main` branch and push the contents of this folder (not the folder itself). Leave the original `ancientwarfare-0.2.9-remastered.jar` and `ancientwarfare-1.12.2-2.7.0.1038.jar` outside the repository; only audited source and license-compatible assets belong here.

The stage-2 port uses a NeoForge 26.3 ModDevGradle source layout and requires Gradle 9.2.1 and Java 25. A future stage will need tests and migration of almost all of Remastered's gameplay code.
