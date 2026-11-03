# bugs-coverage

Generates `docs/original-spec/BUGS-COVERAGE.md`: for every ID in the bug register (`BUGS.md`, template in `docs/templates/bug-register.md`), the `doesNotReproduce<AREA><NN>_...` JUnit tests that guard it, or the reason it cannot occur (recorded in `docs/original-spec/bug-reasons.json` as `{"ID": ["status", "text"]}`). IDs with neither are listed as `MISSING`.

```sh
python3 -I tools/bugs-coverage/generate.py .
```

Standard library only. Run it when a bug gets a test or a reason, and commit the result. A dedicated test or CI step can fail the build when the table reports `MISSING`.
