# http://localhost:8080

docker run --rm -it -p 8080:8080 \
  -v $PWD/app/src:/data/project/ \
  -v $PWD/qodana_output:/data/results/ \
jetbrains/qodana-jvm-android --show-report
