// Local development only. On S3 OpenTofu writes this file with the URL of the EC2
// instance; the frontend deployment never overwrites it and the release archive
// does not contain it.
window.TICTACTEST_CONFIG = {
  webConsoleUrl: "http://localhost:8080/",
};
