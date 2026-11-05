# Stage 7 — build through existing Gregorilass/Empty workflow

The repository's existing `.github/workflows/import.yml` triggers only when a source upload has the exact historical filename `AncientWarfare_Stages_5_and_6_GitHub_Upload.zip`.

The **contents of the new archive are Stage 7**, despite that legacy filename.

1. In https://github.com/Gregorilass/Empty choose **Add file → Upload files**.
2. Upload `AncientWarfare_Stages_5_and_6_GitHub_Upload.zip` from this deliverable. Commit it to `main`.
3. GitHub Actions automatically runs `Import Ancient Warfare Stages 5 and 6` (the old workflow name). It unpacks Stage 7, commits the code, sets up Java 25/Gradle 9.2.1 and compiles.
4. Open the successful Actions run and download its artifact (currently named `AncientWarfare-26.3-Stage5-6-UNTESTED` by the old workflow). Extract the artifact ZIP and check that the **inner mod JAR is named `ancientwarfare-0.0.7-stage7.jar`**.
5. Back up the Warfare world, stop the server, replace the old Ancient Warfare JAR with the Stage 7 one; also replace the old Ancient Warfare JAR in the Windows NeoForge 26.3 client `mods` folder. Keep only one AW JAR per folder. Do not replace the separately patched Proxy Compatible Forge JAR. Start and test on a copied/backed-up world.

If the Action is red, do not install any JAR. Retrieve its Gradle build log and fix compiler errors before attempting gameplay.

**Status of this archive:** all offline source/resource/regression validations pass; **full Gradle compile and in-game tests have not yet been performed.**
