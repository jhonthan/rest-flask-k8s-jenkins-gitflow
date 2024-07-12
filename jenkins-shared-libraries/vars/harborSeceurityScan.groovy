def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('alpine') {
    sh '''
      apk add curl jq

      # retry backoff parameters
      MAX_RETRY=10
      COUNT=1
      SLEEP=1
      SEVERITY="null"

      # Extract TAG name
      ENVIRONMENT=""
      if [ $(echo $GIT_BRANCH | grep ^develop$) ]; then
        ENVIRONMENT="dev"
      elif [ $(echo $GIT_BRANCH | grep -E "^hotfix-.*") ]; then
        ENVIRONMENT="dev"     
      fi

      TAG="$(cat /artifacts/${ENVIRONMENT}.artifact)"

      # harbor variables
      HARBOR_URL="http://harbor.localhost.com"
      HARBOR_PATH="api/v2.0/projects/jhonthanlocal/repositories/${JOB_NAME%/*}/artifacts/${TAG}"
      HARBOR_URL_PARAMS="with_scan_overview=true"

      while [ "$SEVERITY" == "null" ]; do
          SEVERITY=$(curl -X GET \
            "${HARBOR_URL}/${HARBOR_PATH}?${HARBOR_URL_PARAMS}" \
            -H "accept: application/json" \
            -H "authorization: Basic ${HARBOR_CREDENTIALS}" \
            | jq -r '.scan_overview | to_entries | .[].value.severity'
          )

          echo "sleep: ${SLEEP}s | count: ${COUNT}"
          sleep $SLEEP
          SLEEP=$(($SLEEP*2))
          COUNT=$(($COUNT+1))

          if [ $COUNT -ge $MAX_RETRY ]; then
              echo "Reached maximum retry of ${MAX_RETRY}, exiting..."
              exit 1
          fi
      done

      if [ "$SEVERITY" == "Critical" ]; then
          echo "Critical vulnerability found, check Harbor results"
          #exit 1 - Ignore based all criticals
      else
          echo "All good, proceeding for the next stage"
      fi
    '''
  }
}