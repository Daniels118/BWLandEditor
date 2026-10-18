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
package it.ld.bw.lndgui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class VSCodeExtensionInstaller {
    private static final String VSIX_RESOURCE = "/vscode/chl-language.vsix";
    private static final long INSTALL_TIMEOUT_SECONDS = 90;

    private VSCodeExtensionInstaller() {}

    public static void install() throws IOException, InterruptedException {
        Path vsix = Files.createTempFile("chl-language-", ".vsix");
        Path outputFile = null;

        try {
            try (InputStream input = VSCodeExtensionInstaller.class.getResourceAsStream(VSIX_RESOURCE)) {
                if (input == null) {
                    throw new FileNotFoundException("Bundled VS Code extension not found: " + VSIX_RESOURCE);
                }
                Files.copy(input, vsix, StandardCopyOption.REPLACE_EXISTING);
            }

            outputFile = Files.createTempFile("chl-vscode-install-", ".log");
            List<String> command = new ArrayList<>();
            command.add(findVSCodeCommand());
            command.add("--install-extension");
            command.add(vsix.toAbsolutePath().toString());
            command.add("--force");

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(outputFile.toFile());

            Process process;
            try {
                process = builder.start();
            } catch (IOException e) {
                throw new IOException("Unable to start the VS Code CLI. Make sure VS Code is installed and its 'code' command is available.", e);
            }

            boolean finished;
            try {
                finished = process.waitFor(INSTALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
                throw e;
            }

            if (!finished) {
                process.destroyForcibly();
                throw new IOException("VS Code extension installation timed out after " + INSTALL_TIMEOUT_SECONDS + " seconds.");
            }

            if (process.exitValue() != 0) {
                String output = new String(Files.readAllBytes(outputFile), Charset.defaultCharset()).trim();
                throw new IOException("VS Code extension installation failed (exit code " + process.exitValue() + "): " + output);
            }
        } finally {
            Files.deleteIfExists(vsix);
            if (outputFile != null) {
                Files.deleteIfExists(outputFile);
            }
        }
    }

    private static String findVSCodeCommand() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            String[] variables = { "LOCALAPPDATA", "ProgramFiles", "ProgramFiles(x86)" };
            for (String variable : variables) {
                String root = System.getenv(variable);
                if (root != null && !root.isEmpty()) {
                    Path command = Paths.get(root, "Microsoft VS Code", "bin", "code.cmd");
                    if (Files.isRegularFile(command)) {
                        return command.toString();
                    }
                }
            }
            return "code.cmd";
        }

        if (os.contains("mac")) {
            Path[] candidates = {
                Paths.get("/Applications/Visual Studio Code.app/Contents/Resources/app/bin/code"),
                Paths.get(System.getProperty("user.home"), "Applications/Visual Studio Code.app/Contents/Resources/app/bin/code")
            };
            for (Path candidate : candidates) {
                if (Files.isRegularFile(candidate)) {
                    return candidate.toString();
                }
            }
        }

        return "code";
    }
}
