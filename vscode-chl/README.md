# CHL Language for Visual Studio Code

A Visual Studio Code extension for editing **CHL (Challenge Language)** scripts used to create challenges in **Black & White**, with bidirectional coordinate integration with [Black & White Land Editor](https://github.com/Daniels118/BWLandEditor).

Write challenge scripts in VS Code while using the Land Editor to locate positions on the game map. You can preview coordinates from your script on the map, or click a point in the Land Editor to insert its coordinates directly into your code.

## Features

- **CHL language support:** syntax highlighting for `.chl` files, comment definitions, and automatic bracket and quote pairing.
- **Script → map:** place the text cursor inside a coordinate expression to display a temporary marker at that location in the Land Editor.
- **Map → script:** select a point on the Land Editor map to send its coordinates to VS Code. The extension replaces the coordinate expression under the cursor, or inserts the coordinates at the cursor if none is present.
- **Automatic marker cleanup:** moving the cursor away from a coordinate expression requests removal of the temporary marker.
- **Configurable integration:** customize the Land Editor URL, the incoming command port, and the delay before sending marker updates.

## Typical workflow

1. Launch **Black & White Land Editor** and open the land you are working on.
2. Open your challenge's `.chl` script in **Visual Studio Code**.
3. Place the cursor inside a coordinate expression, such as `[120, 340]`. The Land Editor displays the corresponding position on the map.
4. To choose a different location, click the desired point on the map using the Land Editor's coordinate-to-script feature. The selected coordinates are sent back to VS Code.
5. Continue editing your challenge script with the updated coordinates.

### Coordinate formats

The extension recognizes coordinates written as either:

```text
[120, 340]
[120, 25, 340]
```

When previewing a two-component coordinate, the extension interprets it as **X, Z**, with **Y = 0**. Three-component coordinates are interpreted as **X, Y, Z**. Signed numbers and decimal values are supported.

## Installation

Install the extension from a `.vsix` package:

1. Open Visual Studio Code.
2. Open the Command Palette (`Ctrl+Shift+P`).
3. Run **Extensions: Install from VSIX...**.
4. Select the CHL extension's `.vsix` file.

Alternatively, install it from a terminal:

```sh
code --install-extension chl-language.vsix --force
```

The Land Editor must be running for map integration to work. Editing and syntax highlighting of `.chl` files do not require a connection to the Land Editor.

## Configuration

Open **Settings** in VS Code and search for `CHL`, or add the following to your `settings.json`:

```json
{
  "chl.landViewerUrl": "http://127.0.0.1:8086",
  "chl.cmdPort": 8085,
  "chl.hoverPostDebounceMs": 250
}
```

| Setting | Default | Description |
| --- | --- | --- |
| `chl.landViewerUrl` | `http://127.0.0.1:8086` | Base URL of the Land Editor HTTP endpoint. Set to an empty string to disable script-to-map updates. |
| `chl.cmdPort` | `8085` | HTTP port used by the VS Code extension to receive coordinates from the Land Editor. Set to `0` to disable the listener. |
| `chl.hoverPostDebounceMs` | `250` | Delay, in milliseconds, before updating the map marker after a cursor movement. |

Make sure the ports and URL match the corresponding settings in the Land Editor. The incoming command port is read when the extension activates; reload the VS Code window after changing it.

## Communication between VS Code and the Land Editor

The integration uses HTTP requests on the local machine:

| Direction | Request | Purpose |
| --- | --- | --- |
| VS Code → Land Editor | `POST /show_tmp_marker` | Show a temporary marker for the current script coordinates, using a JSON body with `x`, `y`, and `z`. |
| VS Code → Land Editor | `POST /hide_tmp_marker` | Hide the temporary marker. |
| Land Editor → VS Code | `POST /insert_coord` | Send a coordinate expression as a `text/plain` request body for insertion or replacement in the active editor. |

By default, the Land Editor endpoint is expected at `127.0.0.1:8086`, and the VS Code extension receives commands on port `8085`.

## Development

This extension is written in TypeScript and uses the Visual Studio Code Extension API.

From the extension's directory:

```sh
npm ci
npm run compile
```

Open the project in VS Code and press `F5` to start an **Extension Development Host** for testing.

To produce a distributable VSIX package, use the VS Code extension packaging tool:

```sh
npx @vscode/vsce package
```
## Author / Copyright

Copyright (C) 2024-2026 Daniels118

Developed by Daniels118.

## License

This project is distributed under the GNU General Public License v3.0 (GPLv3).

For more information, see the `LICENSE.TXT` file.
