# tetris

Example of a functional Tetris implemented in Clojurescript.

The core logic is completely functional.
There is a gui in tetris.interface 

Have a look at a running example at http://clojure-tetris.herokuapp.com

## Usage

``` clojure
(-> (build-state) r d d rot d d)
```

This will return an immutable state.

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
