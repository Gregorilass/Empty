# Stage 8 upload / build

Repository: https://github.com/Gregorilass/Empty

**Status:** Source project is ready for GitHub import. The original Stage 8 upload failed before compilation because `.github/workflows/stage8.yml` was rejected by GitHub's default Actions token, which lacks `workflows` permission. This corrected ZIP excludes that file and uses the repository's already-existing `.github/workflows/import.yml` without modifying it.

## Upload using the existing workflow

1. In the repository, select **Add file -> Upload files**.
2. Replace the existing file on `main` with the file named **`AncientWarfare_Stages_5_and_6_GitHub_Upload.zip`** from this corrected release, then commit. The historical filename is required by the existing `.github/workflows/import.yml` trigger, even though the archive **contains Stage 8**.
3. Existing GitHub Actions will extract and commit the source project, validate Stages 3 and 4, compile with Java 25 / Gradle 9.2.1, and upload the built JAR as an artifact if successful.
4. **Stage 8 static regression checks are included but are not part of the historical importer workflow.** Run them explicitly with `python3 scripts/validate_stage8.py` when possible.
5. If the GitHub build succeeds, download the resulting GitHub Actions artifact. Expected inner JAR filename: **`ancientwarfare-0.0.8-stage8.jar`**. A green compile does not prove in-game functionality.
6. **Back up the world.** Test on a copied Minecraft 26.3 / NeoForge 26.3.0.101 server and matching client. Use just one Ancient Warfare JAR per mods folder.

Local checks (Java 25 and Gradle 9.2.1 needed for compilation):

```bash
python3 scripts/validate_stage8.py
gradle --no-daemon --stacktrace build
```

See `reports/STAGE8_LIMITATIONS.md` for the in-game regression procedure.
