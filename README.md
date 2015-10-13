# markov-chain

This library implements an n level markov chain in Clojure. It can be
trained on any sequence of data and detect likeness or even generate it.

## Usage

See `examples/language.clj` for how to use it to detect gibberish.

After training a 3 layer MC on individual chars of Great Expectations, Chapter V:
```
> (detect-likeness mc mc-depth "I was walking on the road.")
0.20905892931107461

> (detect-likeness mc mc-depth "a;lsjdfj lkeflh ashoe iofj")
0.05896670169096736

> (->> (generate-likeness mc 100) (reduce str ""))
"en to hoseliarefuld some wit'll. Joe an him ingitew and forns anceir
mened preandied try hat ineve a"
```

It also works for entire words, trained on Great Expectations, the whole
book, without punctuation or caps:
```
> (detect-likeness mc mc-depth (clojure.string/split "i went to the room with them" #"\s+"))
0.04716500678653864

> (detect-likeness mc mc-depth (clojure.string/split "them room the i with to went" #"\s+"))
0.0045216139294960095

> (->> (markov/generate-likeness mc 100) (reduce str ""))
" a juryman in some unlucky hour i resolved a word further without
introducing estella s father would occasionally have some important
bearing on the forehead hardens the brain into a general air and flavor
about the same dim suggestion that i had tried hard at me but he really
is a pretty large experience of him in walking out of england without
laying it upon me i felt any tenderness in her wondering lament of
gracious asked my sister s sudden fancy for me this may lead to trouble
i know i did know it much i ve got you"
```

A little more training and it'll be ready to generate some high quality spam.

## License

Copyright © 2015 Matt Parker

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
