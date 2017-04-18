# Change Log
All notable changes to this project will be documented in this
file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## [1.3.1] - 2017-04-18
### Fixed
- Re-instated the ability to skip fingerprinting with the :skip task option

## [1.3.0] - 2017-03-28
### Changed
- Changed asset fingerprinting strategy. Now instead of a query parameter at the
  end of the url (/some.css?qwdfm...) files will be renamed to (/some-qwdfm.css)

## [1.2.0] - 2017-01-31
### Fixed
- Fixed bug that would cause files not to be written to the classpath
  correctly. Huge problem when serving from classpath in dev mode.

### Changed
- Changed log message printed by task

## [1.1.1] - 2017-01-31
### Changed
- Refactored to keep all input/output code together

## [1.1.0] - 2017-01-09
### Added

- A new task option `asset-host`. Used for specifying a hostname that
  will be prepended to all asset references.
- A new task option to specify one or more file extensions to process
  for asset-references `${...}` to replace.

[1.1.0]: https://github.com/AdamFrey/boot-asset-fingerprint/compare/1.0.0...1.1.0

## [1.0.0] - 2016-06-08
### Initial Release

- Implements an `asset-fingerprint` task for replacing references to
  asset files with URLs containing a query-param based on the hash of
  the contents of the file.

[1.0.0]: https://github.com/AdamFrey/boot-asset-fingerprint/compare/d01ad09...1.0.0
