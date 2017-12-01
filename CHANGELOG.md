# Unreleased

# v1.2.0
- add multithreaded tentacle computation (significant speed-up). Corresponding configuration key is THREAD_NUMBER.

# v1.1.2

- add a changelog.
- add in configuration the MINIMAL_SPEED key. The maximal curvature is limited in order to generate paths that satisfy this minimal speed.
- taking into account the maximal linear acceleration (config key: MAX_LINEAR_ACCELERATION).
- add a "stop" attribute in ItineraryPoint structure.
