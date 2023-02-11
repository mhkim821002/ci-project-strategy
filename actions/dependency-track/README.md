# Upload SBOM file to Dependency Track

This action uploads a SBOM description to Denpendency Track server.

## Inputs

## `api-key`

**Required** Dependency Track API Key

## `project-key`

**Required** Object identifier for a project

## `api-url`

**Required** Dependency Track API URL. The value shoud start from http:// or https://

## bom

**Required** File name or CycloneDX SBOM spec with JSON format

## Outputs

## `result`

API response

## api-url

API call url

## Example usage
- name: 'Upload SBOM to Dependency Track with Custom Action'
  uses: ./actions/dependency-track
  with:
    api-key: ${{secrets.DT_API_KEY}}
    project-key: ${{secrets.DT_PROJECT_KEY}}
    api-url: ${{secrets.DT_API_URL}}
    bom: 'cyclonedx.json'
