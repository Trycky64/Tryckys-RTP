# Trycky's RTP

**Trycky's RTP** is a server-side NeoForge mod for **Minecraft 1.21.1** that adds a safe, configurable and production-ready `/rtp` (Random Teleport) command.

The goal is simple:
- Let players teleport randomly in the world
- Never teleport them into lava, void, water or inside blocks
- Avoid caves in the Overworld
- Avoid the Nether roof
- Keep everything configurable and clean for server admins

This mod is designed for survival servers and modpacks.

---

## ✨ Features

- `/rtp` command (random teleport)
- Per-player cooldown (persistent, survives server restarts)
- Fully server-side (no client required)
- Extremely safe teleport algorithm:
  - Solid ground required
  - 2 blocks of free space for the player
  - Avoids liquids, lava, hazards
  - In Overworld: only surface (never in caves)
  - In Nether: never on the roof
- Dimension filtering:
  - Allowlist or denylist
  - Simple toggles for Nether / End
- Highly configurable
- Clean logging (debug by default, optional success logs)

---

## 📦 Installation (Server)

1. Install **NeoForge 1.21.1** on your server
2. Put the mod JAR into:
   ```
   mods/
   ```
3. Start the server once
4. Edit the config file:
   ```
   config/tryckysrtp-common.toml
   ```
5. Restart the server

Done.

---

## 🎮 Commands

### `/rtp`

Teleports the player to a random safe location.

- Only usable by players
- OPs bypass the cooldown
- If no safe spot is found: shows an error message

---

## ⚙️ Configuration

All configuration is located in:

```
config/tryckysrtp-common.toml
```

### Cooldown

```toml
cooldownSeconds = 3600
```

Cooldown in seconds between two `/rtp` uses for the same player.  
Set to `0` to disable cooldown.

---

### Radius

```toml
radiusMin = 500
radiusMax = 5000
```

Random teleport radius around the world spawn.

---

### Attempts

```toml
attemptsMax = 30
```

How many positions the mod will try before giving up.

---

### Distance from Spawn

```toml
minDistanceFromSpawn = 200
```

Minimum distance from spawn. Set to `0` to disable.

---

### Dimension Rules

```toml
dimensionMode = "DENYLIST"
allowedDimensions = ["minecraft:overworld"]
blockedDimensions = []
allowInNether = true
allowInEnd = true
```

You can:
- Use an allowlist OR a denylist
- Additionally block or allow Nether and End

---

### Safety Rules

```toml
avoidLiquids = true
requireSolidGround = true
safeHeightMode = "MOTION_BLOCKING_NO_LEAVES"
```

- Avoid water and lava
- Require solid ground
- Choose how the base height is detected

---

### Rotation

```toml
keepYawPitch = true
```

If true, keeps player rotation when teleporting.

---

### Logging

```toml
logSuccess = false
```

If true, logs one INFO line per successful RTP.  
Otherwise, success logs are DEBUG only.

---

### Surface Only (Overworld)

```toml
surfaceOnlyInSkylightDims = true
```

If true, in dimensions with skylight (like the Overworld), RTP will **never** place players in caves.

---

### Nether Roof Protection

```toml
maxCeilingClearance = 32
```

Prevents teleporting to places with too much empty space above (like the Nether roof).  
Set to `0` to disable this protection.

---

## 🧠 How the Teleport Algorithm Works

For each attempt:
1. Picks a random X/Z in the configured radius
2. Scans the column for valid positions:
   - Solid ground
   - Two blocks of free space
   - No liquids
   - No hazards
3. Applies dimension rules:
   - Overworld: must see the sky (surface)
   - Ceiling dimensions: rejects overly open ceilings
4. Picks the best candidate near the player's current Y
5. Teleports the player safely

If no valid spot is found after `attemptsMax` tries, the command fails gracefully.

---

## 🧪 Testing

This mod has been tested in:
- Overworld: plains, forests, mountains, oceans
- Nether
- End
- With world borders
- With extreme radius values

---

## ❓ FAQ

### Does this mod need to be installed on the client?
No. It is 100% server-side.

---

### Can players bypass the cooldown?
Yes, OPs (permission level 2) bypass the cooldown.

---

### Can this be used in modpacks?
Yes. It is designed to be modpack-friendly.

---

### Can I allow RTP only in some dimensions?
Yes, using the allowlist/denylist system.

---

### What happens if no safe spot is found?
The player receives a message and nothing happens.

---

## 📜 License

See the license file in the repository.

---

## ❤️ Credits

Developed by Trycky.
