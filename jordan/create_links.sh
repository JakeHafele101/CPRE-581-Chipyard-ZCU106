#!/bin/bash

# Check that two arguments were provided
if [ "$#" -ne 2 ]; then
  echo "Error: This script requires two arguments, the path to the master directory and the path to the target directory."
  exit 1
fi

# The path to the master directory containing the original files
master_dir="$1"

# Get the absolute path to the master directory
master_dir="$(cd "$(dirname "$master_dir")"; pwd)/$(basename "$master_dir")"

# Check if the master directory exists
if [ ! -d "$master_dir" ]; then
  echo "Error: master directory does not exist"
  exit 1
fi

# The path to the larger directory containing corresponding files
target_dir="$2"

# Get the absolute path to the target directory
target_dir="$(cd "$(dirname "$target_dir")"; pwd)/$(basename "$target_dir")"

# Check if the target directory exists
if [ ! -d "$target_dir" ]; then
  echo "Error: target directory does not exist"
  exit 1
fi

# Check that the master directory and target directory are not the same
if [ "$master_dir" = "$target_dir" ]; then
  echo "Error: The master directory and target directory cannot be the same."
  exit 1
fi

# Loop through all files in the master directory (including those in subdirectories)
find "$master_dir" -type f -print0 | while read -d $'\0' file; do
  # Get the name of the file without the path
  filename="$(basename "$file")"

  # Get the path to the corresponding file in the target directory
  target_file="$target_dir/${file#$master_dir/}"

  # Check if the file already exists in the target directory
  if [ -e "$target_file" ]; then
    echo "File already exists for $target_file replacing with a link"
  else
    echo "Created link for $target_file"
  fi

  # Create the parent directories if they don't exist
  mkdir -p "$(dirname "$target_file")"

  # Create a symbolic link to the file in the target directory
  ln -sf "$file" "$target_file"
 
done

echo "Done!"

