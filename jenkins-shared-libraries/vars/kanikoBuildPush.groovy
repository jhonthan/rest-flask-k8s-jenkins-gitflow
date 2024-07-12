def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('kaniko') {
    sh '''
      REGISTRY="harbor.localhost.com/jhonthanlocal"
      REPOSITORY=${JOB_NAME%/*}
      TAG=""
      ENVIRONMENT=""

      if [ $(echo $GIT_BRANCH | grep ^develop$) ]; then
        TAG="dev-${GIT_COMMIT:0:10}"
        ENVIRONMENT="dev"
      elif [ $(echo $GIT_BRANCH | grep -E "^hotfix-.*") ]; then
        TAG="${GIT_BRANCH#*-}-${GIT_COMMIT:0:10}"
        ENVIRONMENT="stg"     
      fi

      DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

      /kaniko/executor \
        --insecure \
        --destination ${DESTINATION} \
        --context $(pwd)
      
      echo "${TAG}" > /artifacts/${ENVIRONMENT}.artifact
    '''
  }
}