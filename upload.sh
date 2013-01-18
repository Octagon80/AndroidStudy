#!/bin/bash


# Checkout and track the gh-pages branch
git checkout -t origin/master


# Stage all files in git and create a commit
git add .
git add -u
git commit -m "CellId версия от $(date)"

# Push the new files up to GitHub
git push origin master

