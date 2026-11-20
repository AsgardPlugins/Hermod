# Hermod

Announces in chat when someone on the server goes live on Twitch, tells players who join what is already live, and exposes placeholders to mark streamers. Runs on Paper, Folia and their forks from 1.18.2 to 26.3.

## How it works

`LiveTracker` asks the Twitch Helix API which tracked channels are live, on a timer (60 seconds by default, one request per 100 channels). A channel that was offline and is now live fires the stream started message to everyone online. Whoever joins gets the currently live message after a delay, so it shows up after the MOTD and the resource pack. Streamers are stored in `plugins/Hermod/streams.yml` as a Minecraft name and a Twitch link.

Messages go to each player through their own scheduler, so it works on Folia. The Folia scheduler is only loaded when Folia is detected, everything else uses the normal Bukkit scheduler.

## Using it

Create an app at dev.twitch.tv, put the client id and secret in `config.yml`, then `/hermod reload`.

```
/hermod add <name> <link>
/hermod remove <name>
/hermod list
/hermod reload
```

All of them need `hermod.admin`. The name is the Minecraft name, the link is `https://twitch.tv/<channel>`.

Messages use MiniMessage with `<user>`, `<link>` and `<users>` placeholders. The sounds, the join delay and the placeholder text are all in `config.yml`.

## Placeholders

With PlaceholderAPI installed:

| Placeholder | Value |
|---|---|
| `%hermod_live%` | `Live 🔴` if the player is live, empty otherwise |
| `%hermod_live_compact%` | `🔴` if the player is live, empty otherwise |
| `%hermod_user_live%` | the streamer who went live last |
| `%hermod_users_live%` | everyone live right now |

`placeholders.live` and `placeholders.live-compact` change the text, so you can use a resource pack glyph instead of the emoji.

## Building

```sh
./gradlew build
./gradlew runServer
./gradlew runFolia
```

The jar ends up in `build/libs`. It is compiled against the 1.18.2 API on Java 17. `publishModrinth` uploads it once `modrinthId` is set in `publish.gradle` and `modrinthToken` is in `~/.gradle/gradle.properties`.

## License

[GPL-3.0](LICENSE)
