# Change Log
All notable changes to this project will be documented in this
file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).


## [Unreleased]

[Unreleased]: https://github.com/AdamFrey/boot-asset-fingerprint/compare/0.1.0...HEAD
### Added

- A new task option `asset-host`. Used for specifying a hostname that
  will be prepended to all asset references.

## [0.1.0] - 2016-06-08
### Initial Release

- Implements an `asset-fingerprint` task for replacing references to
  asset files with URLs containing a query-param based on the hash of
  the contents of the file.

[0.1.0]: https://github.com/AdamFrey/boot-asset-fingerprint/compare/d01ad09...0.1.0
