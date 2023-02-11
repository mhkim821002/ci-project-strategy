const core = require('@actions/core');
const github = require('@actions/github');

const repoName = github.context.repo.repo.replace('/', '-');
const branchName = !github.context.payload.pull_request.head.ref ? 'main' : github.context.payload.pull_request.head.ref.replace('/', '-');
const projectName = `${repoName}-${branchName}`;

core.setOutput('sonarqube-project-name', projectName);
