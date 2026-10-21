   #!/usr/bin/env bash
    
    BROWSER=${1:-chrome}
    JOURNEY=${2:-DISA}
    
    echo "Running browser tests..."
    echo "=========================================="
    echo "Browser:              ${BROWSER}"
    echo "Env:                  local"
    echo "Journey:              ${JOURNEY}"
    echo "=========================================="
    sbt clean -Dbrowser="${BROWSER}" -Dbrowser.option.headless=false -Denvironment=local "testOnly uk.gov.hmrc.ui.specs.* -- -n WIP" testReport
    
    #sbt clean -Dbrowser="${BROWSER_TYPE:=$DEFAULT_BROWSER}" -Denvironment="${ENV:=local}" "testOnly uk.gov.hmrc.ui.disa.specs.*" test testReport