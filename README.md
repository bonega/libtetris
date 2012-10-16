# tetris

Example of a functional Tetris-library implemented in Clojure.

The core logic is completely functional.

## Usage
Look at the [documentation](http://bonega.github.com/libtetris/index.html)

### Example
``` clojure
(-> (build-state) r d d rot d d)
```

This will return an immutable state.

## Installation

Add Libtetris to `project.clj`

    (defproject hello-tetris "1.0.0-SNAPSHOT"
      :description "FIXME: write"
      :dependencies [[org.clojure/clojure "1.4.0"]
                    [libtetris "0.1.0]])

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
