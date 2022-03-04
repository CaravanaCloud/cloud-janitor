jpackage --input target/ \
  --name tasktree \
  --main-jar tasktree-runner.jar \
  --main-class tasktree.Main \
  --type rpm \
  --java-options '--enable-preview'
