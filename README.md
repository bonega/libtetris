# Libtetris

Example of a Tetris-library implemented in Clojure.

The core logic is completely functional.

## Usage
Look at the [documentation](http://bonega.github.com/libtetris/index.html)

### Example
``` clojure
(-> (build-state) r d d rot d d)
```
This will return an immutable state.

### Clojurescript example

Look at the [clojurescript](https://github.com/bonega/cljs-tetris) implementation.
Or play the [game](http://bonega.github.com/cljs-tetris/index.html)!

## Installation

Add Libtetris to `project.clj`

    (defproject hello-tetris "1.0.0-SNAPSHOT"
      :description "FIXME: write"
      :dependencies [[org.clojure/clojure "1.4.0"]
                    [libtetris "0.1.0]])

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
