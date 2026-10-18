/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import * as vscode from "vscode";
import * as http from "node:http";

type CoordMatch = { text: string; range: vscode.Range };

const COORD_REGEX =
  /\[\s*-?\d+(?:\.\d+)?\s*,\s*-?\d+(?:\.\d+)?(?:\s*,\s*-?\d+(?:\.\d+)?\s*)?\]/g;

function findCoordAtPosition(doc: vscode.TextDocument, pos: vscode.Position): CoordMatch | null {
  const line = doc.lineAt(pos.line);
  const text = line.text;

  for (const m of text.matchAll(COORD_REGEX)) {
    if (m.index === undefined) continue;
    const start = m.index;
    const end = start + m[0].length;

    if (pos.character >= start && pos.character <= end) {
      return {
        text: m[0],
        range: new vscode.Range(
          new vscode.Position(pos.line, start),
          new vscode.Position(pos.line, end)
        )
      };
    }
  }
  return null;
}

async function showTmpMarker(baseurl: string, coordText: string): Promise<void> {
  let coord = JSON.parse(coordText);
  if (!Array.isArray(coord) || (coord.length != 2 && coord.length != 3)) {
    throw new Error("Invalid coordinate format");
  }
  let x = coord[0];
  let y = coord.length == 3 ? coord[1] : 0;
  let z = coord.length == 3 ? coord[2] : coord[1];
  //vscode.window.showInformationMessage(`Posting coordinate: x=${x}, y=${y}, z=${z}`);
  const res = await fetch(baseurl + "/show_tmp_marker", {
    method: "POST",
    headers: {
      "content-type": "application/json"
    },
    body: JSON.stringify({ x: x, y: y, z: z })
  });

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status} ${res.statusText}${body ? ` - ${body}` : ""}`);
  }
}

async function hideTmpMarker(baseurl: string): Promise<void> {
  const res = await fetch(baseurl + "/hide_tmp_marker", {
    method: "POST"
  });
  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status} ${res.statusText}${body ? ` - ${body}` : ""}`);
  }
}

export function activate(context: vscode.ExtensionContext) {
  let timer: NodeJS.Timeout | undefined;
  let lastSent: string | null = null;
  let warnedFetch = false;

  const cfg = vscode.workspace.getConfiguration();
  const cmdPort = cfg.get<number>("chl.cmdPort") ?? 0;
  if (cmdPort > 0) {
    const server = http.createServer((req, res) => {
      if (req.url === "/insert_coord") {
        if (req.method === "POST") {
          if (req.headers["content-type"] && req.headers["content-type"].includes("text/plain")) {
            let body = "";
            req.on("data", chunk => (body += chunk));
            req.on("end", () => {
              vscode.window.setStatusBarMessage(`CHL SERVER received: ${body}`, 4000);
              const editor = vscode.window.activeTextEditor; 
              if (editor && body) {
                editor.edit(editBuilder => {
                  const pos = editor.selection.active;
                  const match = findCoordAtPosition(editor.document, pos);
                  if (match) {
                    const range = match.range;
                    editBuilder.replace(range, body);
                  } else {
                    editBuilder.insert(pos, body);
                  }
                });
              }
              res.writeHead(200);
              res.end();
            });
          } else {
            res.writeHead(400);
            res.end();
            return;
          }
        } else {
          res.writeHead(405);
          res.end();
        }
      } else {
        res.writeHead(404);
        res.end();
      }
    });

    server.listen(cmdPort, "127.0.0.1", () => {
      console.log(`CHL internal server listening on 127.0.0.1:${cmdPort}`);
    });

    context.subscriptions.push({
      dispose: () => server.close()
    });
  }

  const disposable = vscode.window.onDidChangeTextEditorSelection((e) => {
    const editor = e.textEditor;
    if (!editor) return;

    if (editor.document.languageId !== "chl") return;

    const sel = editor.selection;
    if (!sel.isEmpty) return;

    const cfg = vscode.workspace.getConfiguration();
    const baseurl = cfg.get<string>("chl.landViewerUrl")?.trim() ?? "";
    if (!baseurl) return;
    
    const debounceMs = cfg.get<number>("chl.hoverPostDebounceMs") ?? 250;

    if (typeof fetch !== "function") {
        if (!warnedFetch) {
            warnedFetch = true;
            vscode.window.showWarningMessage(
                "CHL: fetch unavailable. Please install Node >= 18."
            );
        }
        return;
    }

    const match = findCoordAtPosition(editor.document, sel.active);
    if (match) {
        if (match.text === lastSent) return;
        if (timer) clearTimeout(timer);
        timer = setTimeout(async () => {
            try {
                await showTmpMarker(baseurl, match.text);
                lastSent = match.text;
            } catch (err: any) {
                const msg = err?.message ? String(err.message) : String(err);
                vscode.window.setStatusBarMessage(`CHL POST failed: ${msg}`, 4000);
            }
        }, Math.max(0, debounceMs));
    } else {
        if (match === lastSent) return;
        if (timer) clearTimeout(timer);
        timer = setTimeout(async () => {
            try {
                await hideTmpMarker(baseurl);
                lastSent = null;
            } catch (err: any) {
                const msg = err?.message ? String(err.message) : String(err);
                vscode.window.setStatusBarMessage(`CHL POST failed: ${msg}`, 4000);
            }
        }, Math.max(0, debounceMs));
    }
  });

  context.subscriptions.push(disposable);
}

export function deactivate() {}
