# PowerShell script

# Function to compile the Java program using Maven
function Invoke-Maven {
  Write-Host "Compiling Java program with Maven..."
  try {
    mvn clean install
    if ($?) {
      Write-Host "Java program compiled successfully with Maven."
    } else {
      Write-Host "Error compiling Java program with Maven."
    }
  }
  catch {
    Write-Host "Error compiling Java program with Maven."
  }
}

# Function to run the program using Java
function Run-Program {
  Write-Host "Running program using Java..."
  try {
    java -jar target/banking-system-1.0-SNAPSHOT-jar-with-dependencies.jar
    if ($?) {
      Write-Host "Program ran successfully with Java."
    } else {
      Write-Host "Error running program with Java."
    }
  }
  catch {
    Write-Host "Error running program with Java."
  }
}

# Function to push the repository to GitHub (secondary push)
function Push-ToGitHub {
  param (
    [string]$BranchName = "secondary"
  )
  Write-Host "Pushing repository to GitHub to branch $BranchName..."

  # Check if Git is installed
  if (!(Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Git is not installed."
    return
  }

  # Check if the current directory is a Git repository
  try {
    $gitTopLevelDir = (git rev-parse --show-toplevel)
    if (-not $gitTopLevelDir) {
      Write-Host "Error: Not a git repository."
      return
    }
  }
  catch {
    Write-Host "Error: Not a git repository."
    return
  }

  git add .
  git commit -m "Secondary push"
  git push origin HEAD:$BranchName

  if ($?) {
    Write-Host "Repository pushed to GitHub successfully."
  } else {
    Write-Host "Error pushing repository to GitHub."
  }
}

# Main execution block (optional)
# You can call the functions here if you want the script to execute them automatically
# For example:
# Compile-Java
# Run-Program
# Push-ToGitHub

# Main menu loop
while ($true) {
  Write-Host "Menu:"
  Write-Host "1. Compile Maven"
  Write-Host "2. Run Program"
  Write-Host "3. Push to GitHub"
  Write-Host "4. Exit"

  $choice = Read-Host "Enter your choice (1-4)"

  switch ($choice) {
    "1" {
      Invoke-Maven
    }
    "2" {
      Run-Program
      clear
    }
    "3" {
      Push-ToGitHub
    }
    "4" {
      Write-Host "Exiting..."
      clear
      exit
    }
    default {
      Write-Host "Invalid choice. Please try again."
    }
  }
}
