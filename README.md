# disa-returns-frontend-ui-tests

disa-returns-frontend UI journey tests.

## Pre-requisites

### Services

Start Mongo Docker container as follows:

```bash
docker run --rm -d -p 27017:27017 --name mongo percona/percona-server-mongodb:6.0
```

Start `DISA_RETURNS_ALL` services as follows:

```bash
sm2 --start DISA_RETURNS_ALL
```

The test uses local `AUTH_LOGIN_API` for a backend read session and the local backend internal-auth token to read
submission status. Leave the backend and submission workers enabled.

## Tests

Run locally with `chrome`, `edge`, or `firefox`:

```bash
sbt -Dbrowser="<browser>" -Denvironment=local test testReport
```

### Test data

`FileUploadSpec` reserves `Z9101`, `Z9102`, and `Z9103`. `BaseSpec` clears the suite's references before the suite,
then clears only the scenario's reference before and after each successful scenario, calling backend first and
submission second. Clock overrides are set and reset for the same reference.

The confirmation page only confirms the declaration was accepted. Teardown waits for `npsDeclaredOn` and, for
file uploads, backend's `SUBMITTED` status before deleting return data. If the scenario fails or polling times out,
the test resets its clock override but leaves unverified data for the next scoped reset.

## Scalafmt

Check all project files are formatted as expected as follows:

```bash
sbt scalafmtCheckAll scalafmtCheck
```

Format `*.sbt` and `project/*.scala` files as follows:

```bash
sbt scalafmtSbt
```

Format all project files as follows:

```bash
sbt scalafmtAll
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
