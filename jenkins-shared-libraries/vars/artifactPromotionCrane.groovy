def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('crane') {
    sh '''
      REGISTRY="harbor.localhost.com/jhonthanlocal"
      REPOSITORY=${JOB_NAME%/*}

      OLD_TAG=""
      TAG=""
      ENVIRONMENT=""

      if [ $(echo $GIT_BRANCH | grep -E "^release-.*") ]; then
        OLD_TAG="$(cat /artifacts/dev.artifact)"
        ENVIRONMENT="stg"
        TAG="${GIT_BRANCH#*-}-$(echo ${OLD_TAG} | cut -d - -f 2)"
      elif [ $(echo $GIT_BRANCH | grep -E "^v[0-9]\\.[0-9]\\.[0-9]{1,3}$") ]; then
        OLD_TAG="$(cat /artifacts/stg.artifact)"
        ENVIRONMENT="pro"
        TAG="$(echo ${OLD_TAG} | cut -d - -f 1)"
      fi

      OLD_DESTINATION="${REGISTRY}/${REPOSITORY}:${OLD_TAG}"

      crane tag ${OLD_DESTINATION} ${TAG} --insecure

      echo "${TAG}" > /artifacts/${ENVIRONMENT}.artifact
    '''
  }
}