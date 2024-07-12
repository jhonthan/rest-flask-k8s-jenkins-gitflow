def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('helm') {
    sh '''
      apk add openssh

      mkdir $HOME/.ssh
      cp $JENKINS_SSH_PRIVATE_KEY $HOME/.ssh/id_rsa
      chmod 400 $HOME/.ssh/id_rsa

      ssh-keyscan gitea.localhost.com > $HOME/.ssh/known_hosts

      git clone git@gitea.localhost.com:jhonthan-company/helm-applications.git


      if [ $(echo $GIT_BRANCH | grep ^develop$) ]; then
        ENVIRONMENT="dev"
      elif [ $(echo $GIT_BRANCH | grep -E "^hotfix-.*") ]; then        
        ENVIRONMENT="stg"     
      fi

      cd helm-applications/${JOB_NAME%/*}
      helm dependency build
      helm upgrade --install \
        --values values-ci.yaml \
        --namespace citest \
        --create-namespace \
        --set image.tag="dev-0cac7c3308" \
        --wait \
        flask-ci .

      status_code="$(curl --silent \
        --output /dev/null \
        --write-out '%{http_code}\n' \
        http://flask.citest.svc.cluster.local:5000/users)"

      if [ "$status_code" == "200" ]; then
        echo "All good, API response HTTP 200"
      else
        echo "ERROR: $status_code"
        exit 1
      fi
    '''
  }
} //--set image.tag="$(cat /artifacts/${ENVIRONMENT}.artifact)" \