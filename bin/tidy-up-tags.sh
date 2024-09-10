tags=(
"4.4.0-fdroid"
"4.4.0-googleplay"
)

for tag in "${tags[@]}"; do
  echo "Deleting tag: $tag"
  git tag -d $tag
  git push origin --delete $tag
done
