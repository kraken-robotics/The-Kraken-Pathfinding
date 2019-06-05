# Unreleased
- New "KrakenParameters" class to easily handle the initialization parameters
- QuadTree physics engine
- Fast and dirty coeff instead of a boolean
* The backup path can be disabled
- Remove the linear acceleration simulation

# v1.4.5
- Remove the dependency to the home-made "graphic toolbox" library
- Uses maven for the examples

# v1.4.4
- Abort the search when interrupted
- Far better A* heuristic for XYO/XYOC0 search
- New exceptions: StartPointException and EndPointException

# v1.4.3
- Fix the curvature of clothoids curves

# v1.4.2
- Fast and dirty mode, to find quickly any path
- May check new obstacles while searching or not
- Check the final position in XYO mode
- API : add a timeout in SearchParameters
- API : add a "stop()" function to end the current search

# v1.4.1
- May use an external physics engine
- May be used with the replanning only

# v1.4.0
- Add the autoreplanning mode
- May get the complete path or the diff only
- Maven package available at packagecloud.io
- Add examples 5 and 6

# v1.3.1
- API : introduction of SearchParameters and ResearchProfile
- API : may specify the maximal speed for a search
- Bezier curves may initiate a motion direction change
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
