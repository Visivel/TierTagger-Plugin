# TierTagger Plugin

A Minecraft plugin for displaying player tiers from MCTiers (Non Brazilian Tierslist) and South Tiers API (Brazilian Tierlist) with advanced caching, multilanguage support, and PlaceholderAPI integration
![logo](/images/tiertaggerplugin.png)
![display](/images/example.png)

## Features

- **Dual API Support**: Compatible with both MCTiers and South Tiers API
- **Caching**: Efficient local caching system to reduce API calls
- **Multi-language Support**: English (en_us) and Portuguese (pt_br) language files
- **PlaceholderAPI Integration**: Extensive placeholder support for other plugins, like TAB
- **Configurable Display**: Customizable tier colors with gradient support
- **Permission System**: Customizable permission settings, useful for LuckPerms
- **Realtime Updates**: Automatic tier fetching and display on player join

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/TierTagger/config.yml`
5. Do `/tt reload` to restart the configuration file

## Configurations

### API Configuration
```yaml
api:
  provider: MCTIERS  # MCTIERS or SOUTH_TIERS
  default_gamemode: vanilla
  timeout: 10
  cache_duration: 30
```

### Display Configuration
```yaml
display:
  enabled: true
  format: "{tier} | {player}"
  show_in_tablist: true
  show_in_chat: true
```

### Language Configuration
```yaml
language: en_us  # en_us or pt_br
```
### Colors Configuration
```yaml
tier_colors:
  ht1:
    start: "#FF0000" # Start = start color of the gradient
    end: "#A60000" # End = end color of the gradient
  lt1:
    start: "#FF3200"
    end: "#FF5E37"
  ht2:
    start: "#C4FF00"
    end: "#8DB800"
  lt2:
    start: "#0DFF00"
    end: "#08A800"
  ht3:
    start: "#FFE200"
    end: "#CCB500"
  lt3:
    start: "#00FFAB"
    end: "#00BAA9"
  ht4:
    start: "#00B2FF"
    end: "#0074A6"
  lt4:
    start: "#5500FF"
    end: "#3B00B0"
  ht5:
    start: "#D47410"
    end: "#AD6214"
  lt5:
    start: "#916231"
    end: "#704518"
```

## Commands

- `/tier <player>` - Check a player tier information
- `/display` - Toggle tier display for yourself
- `/tieroff` - Toggle tier system globally (Admin only)
- `/tierhide` - Toggle your tier for everyone
- `/tiertagger reload` - Reload plugin configuration

## Permissions

- `tiertagger.reload` - Allows reloading plugin configuration
- `tiertagger.tiersearch` - Allows searching for player tiers
- `tiertagger.display` - Allows toggling tier display for yourself (you dont see people tier)
- `tiertagger.tierhide` - Allows toggling your tier display for everyone (they dont see your tier)
- `tiertagger.admin` - Administrative permissions for TierTagger

## PlaceholderAPI Placeholders

### General Placeholders
- `%tiertagger_tier%` - Player best tier
- `%tiertagger_tier_formatted%` - Player best tier with formatting
- `%tiertagger_points%` - Player total points
- `%tiertagger_overall%` - Player overall rank
- `%tiertagger_region%` - Player region

### Gamemode Specific Placeholders
- `%tiertagger_vanilla%` - Vanilla tier
- `%tiertagger_sword%` - Sword tier
- `%tiertagger_pot%` - Pot tier
- `%tiertagger_uhc%` - UHC tier
- `%tiertagger_axe%` - Axe tier
- `%tiertagger_nethop%` - Netherite OP tier
- `%tiertagger_mace%` - Mace tier
- `%tiertagger_smp%` - SMP tier
- `%tiertagger_crystal%` - Crystal tier

### Tier Specific Placeholders
- `%tiertagger_ht1%` - Returns "HT1" if player has High Tier 1
- `%tiertagger_lt1%` - Returns "LT1" if player has Low Tier 1
- And so on for all other tiers (HT2, LT2, HT3, LT3, HT4, LT4, HT5, LT5)

## API Support

### MCTiers API
- Uses UUID-based lookups
- Supports all gamemodes (vanilla, sword, pot, uhc, axe, nethop, mace, smp)
- Provides detailed statistics including points, overall rank, and region

### South Tiers API
- Uses Cracked-based lookups
- Primarily supports Crystal gamemode only

## Building

```bash
git clone https://github.com/Visivel/TierTagger-plugin.git
cd TierTagger-plugin
gradle build
```

The compiled jar will be available in `build/libs/`

## Requirements

- Java 17 or higher
- Minecraft Server 1.21.1+
- Paper/Spigot server
- PlaceholderAPI (optional)
