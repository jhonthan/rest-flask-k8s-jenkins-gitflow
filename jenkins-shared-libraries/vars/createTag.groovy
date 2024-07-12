def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('alpine') {
    sh '''
      apk add openssh git
   
      RELEASE_VERSION="$(cat /artifacts/stg.artifacts | cut -d - -f 1)"    

      git config \
        --global \
        --add safe.directory \
        $WORKSPACE

      git fetch --all
      git tag -a $RELEASE_VERSION -m "production release: $RELEASE_VERSION"
      git push --tags
    '''
  }
}