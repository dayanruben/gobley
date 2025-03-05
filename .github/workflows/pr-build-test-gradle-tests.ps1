$ErrorActionPreference = "Stop";
$PSNativeCommandUseErrorActionPreference = $true;

try {
    ./gradlew allTests `
        "-Pgobley.projects.uniffiTests=false" `
        "-Pgobley.projects.examples=false";
} finally {
    ./.github/workflows/pr-build-test-copy-test-result.ps1;
    ./gradlew clean `
        "-Pgobley.projects.uniffiTests=false" `
        "-Pgobley.projects.examples=false";
    ./.github/workflows/pr-build-test-change-file-owner.ps1;
}
