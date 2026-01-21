# Mining V2

This is the place I want to share my design on this version.

## Core

### Matcher

The block predicator, called by selector, to check whether the block at certain position is good to be vein-mined.

Matchers don't care about the positions, but it will still receive the position info to retrieve nearby status.
For example, I can match the cactus on top of other cactus, but not ones on the sand.

### Selector

The block position generator, start generating the positions when either position preview requested or the execution is
started.
It gives the information about how far, how large, this vein-mining operation can be, like I can only scan for a very
small cubic area, or a very large area from the sky to the bedrock.
The selected positions can be dependent on the matcher, like I will select up to 128 blocks as long as they're matched (
like all same).

### Executor

Just the executor. It simulate block breaking and collect the drop items.
For every session, it creates a brand-new executor.

## Registration

Since every computation occurs in the server, so actually, we can just simply tell the client what matcher and selector
we have, and the client can just don't care about the actual implementation.

The registration is server only, and when the client joins, send a packet to tell the client that what we have, and the
client can simply render them as texts.

This feature also gives the oppertunity to update version on server while not force updating on clients.
