# Import Ancient Warfare Stages 9 + 10 on GitHub

Repository: https://github.com/Gregorilass/Empty

**Important:** The existing repository workflow `.github/workflows/import.yml` triggers only when a ZIP named `AncientWarfare_Stages_5_and_6_GitHub_Upload.zip` is committed. The historical filename is reused for compatibility. **The ZIP contains Stages 9 + 10 and version `0.0.10-stage10`.**

1. Open the repository main branch > **Add file** > **Upload files**.
2. Upload the ZIP `AncientWarfare_Stages_5_and_6_GitHub_Upload.zip`. Its source file is removed automatically by the existing importer, so it may not exist in the repository until you upload it again. Commit the ZIP on `main`.
3. The existing import job unzips the project, commits the changed source, runs the legacy validators, then runs all Stage 9+10 offline tests as part of Gradle `check`, builds with Java 25 / Gradle 9.2.1 and uploads a compiled artifact. **This package contains NO `.github/workflows/*.yml` files**, avoiding the workflow-permission error we previously encountered.
4. Verify **Import**, **Compile Ancient Warfare** and **Upload compiled JAR** all succeed in GitHub Actions. Do not treat any success as confirmation of in-game functionality.
5. Download the compiled JAR from the artifact ZIP. Expected filename: **`ancientwarfare-0.0.10-stage10.jar`**.
6. Stop the dedicated server; install **only that one version** on both server and matching NeoForge 26.3 client. Never keep Stage 8 and Stage 10 installed together. Start a world backup/test copy before validating woodcutting behavior.

Run the new offline checks locally (automatically run through Gradle `check` in GitHub Actions):

```bash
python3 scripts/validate_stage910.py
```

Use `README.md` for exact in-game instructions. The user-supplied original 1.12.2 and remastered JARs are references only; do not install them alongside this modern port.
