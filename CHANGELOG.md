# Unreleased

# v1.3.1
- API : introduction of SearchParameters and ResearchProfile
- API : can specify the maximal speed for a search
- Add the XYOC0 mode
- Various bugfix

# v1.3.0
- New default research mode : aim either at XY or XYO
- Possibility to add customised research mode
- On the fly load balance
- Add cubic Bezier curves
- Add example 4
- API : "stop()" isn't necessary anymore

# v1.2.2
- API : add getVersion() to Kraken
- Bugfix multithreading
- Dozen new sanity checks
- New multithreading architecture

# v1.2.1
- Unit tests : introduction
- Bugfix multithreading

# v1.2.0
- Add multithreaded tentacle computation (significant speed-up). Corresponding configuration key is *THREAD_NUMBER*.

# v1.1.2
- Add a changelog.
- Add in configuration the *MINIMAL_SPEED* key. The maximal curvature is limited in order to generate paths that satisfy this minimal speed.
- Taking into account the maximal linear acceleration (config key: *MAX_LINEAR_ACCELERATION*).
- Add a "stop" attribute in ItineraryPoint structure.
