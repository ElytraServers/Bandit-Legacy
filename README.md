# Bandit

[1.12.2 Edition](https://github.com/ElytraServers/Bandit)

Get your ores like a Bandit.

Yet Another Ore Vein Mining Mod, inspired by [Qz-Miner](https://github.com/QuanhuZeYu/Qz-Miner).

## Usage

To start, you need to bind a keybinding for Bandit to enable, because there is no default keybinding for you, to reduce
keybinding duplication.

And when you press the key you just bind, you'll see some information on the top left, with green "Activating".
In this state, you can start the vein mining. But I'd recommend you to customize the settings first.

Pressing the key with either Shift or Ctrl will open a menu, selecting the Executor (or Scanning Mode in general) and
the Block Filter.

Keep in mind, the vein mining task won't stop even if your Pickaxe in hand is broken. So get a tool that has
enough durability before starting the vein mining. Also, you can change the tool in hand while vein mining.

If you want to stop an ongoing vein mining task, you can use command `/bandit stop`. But it is not retroactive, which
you cannot undo the vein mining.

### Executor

Currently, we have 4 executors.

- [Manhattan]: The basic one, where it scans the DIRECTLY connected neighbor blocks within 8-block range from the
  center, globular.
- [Manhattan+]: The slightly advanced one of Manhattan, it scans the neighbor blocks within the 3x3x3 area, also within
  8-block range from the center.
- [Manhattan Large]: The larger version of Manhattan+, which it scans the blocks within 16-block range from the center.
- [Large-scan]: The largest scanner, which it scans all the blocks within a cube radius of 32 from the center. It is
  designed for ore mining, which the block clusters are not directly connected to each other. It starts from a vertex of
  the cube, so don't worry.

### Block Filter

- [All]: Just, all.
- [Matching Block]: Matches the block, and ignoring the block metadata.
- [Matching Block and Metadata]: Matches the block and the metadata.
