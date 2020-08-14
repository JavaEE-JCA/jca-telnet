/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.moke.jca.adapter;

import ir.moke.jca.api.TelnetListener;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class TelnetServer implements TtyCodes {

    private final TelnetListener listener;

    private final TelnetActivationSpec spec;

    private final int port;

    private final Map<String, Cmd> cmds = new TreeMap<String, Cmd>();

    private final AtomicBoolean running = new AtomicBoolean();
    private ServerSocket serverSocket;

    public TelnetServer(TelnetActivationSpec spec, TelnetListener listener, int port) {
        this.port = port;
        this.spec = spec;
        this.listener = listener;

        for (Cmd cmd : spec.getCmds()) {
            this.cmds.put(cmd.getName(), cmd);
        }

        try {
            cmds.put("help", new BuiltInCmd("help", this.getClass().getMethod("help", String.class)));
            cmds.put("exit", new BuiltInCmd("exit", this.getClass().getMethod("exit")));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public TelnetListener getListener() {
        return listener;
    }

    public void activate() throws IOException {
        if (running.compareAndSet(false, true)) {
            serverSocket = new ServerSocket(port);
            final Logger logger = Logger.getLogger(TelnetServer.class.getName());
            logger.info("Listening on " + serverSocket.getLocalPort());
            while (running.get()) {
                final Socket accept = serverSocket.accept();
                final Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            session(accept);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        }
    }

    public void deactivate() throws IOException {
        if (running.compareAndSet(true, false)) {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void session(Socket socket) throws IOException {
        InputStream telnetIn = null;
        PrintStream telnetOut = null;

        try {
            final InputStream in = socket.getInputStream();
            final OutputStream out = socket.getOutputStream();

            telnetIn = new TelnetInputStream(in, out);
            telnetOut = new TelnetPrintStream(out);

            telnetOut.println("");
            telnetOut.println("type \'help\' for a list of commands");


            final DataInputStream dataInputStream = new DataInputStream(telnetIn);

            while (running.get()) {

                prompt(dataInputStream, telnetOut);

            }

        } catch (StopException s) {
            // exit normally
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            close(telnetIn);
            close(telnetOut);
            if (socket != null) socket.close();
        }
    }

    private static void close(Closeable closeable) {
        if (closeable == null) return;

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void prompt(DataInputStream in, PrintStream out) throws StopException {

        try {

            out.print(TTY_Reset + TTY_Bright + spec.getPrompt() + " " + TTY_Reset);

            out.flush();

            final String commandline = in.readLine().trim();

            if (commandline.length() < 1) return;

            final List<String> list = new ArrayList<String>();
            Collections.addAll(list, commandline.split(" +"));

            final String command = list.remove(0);

            final String[] args = list.toArray(new String[list.size()]);

            final Cmd cmd = cmds.get(command);

            if (cmd == null) {

                out.print(command);

                out.println(": command not found");

            } else {

                try {
                    cmd.exec(listener, args, out);
                } catch (StopException stop) {
                    throw stop;
                } catch (Throwable throwable) {
                    throwable.printStackTrace(out);
                }

            }


        } catch (StopException stop) {
            throw stop;
        } catch (UnsupportedOperationException e) {

            throw new StopException(e);

        } catch (Throwable e) {

            e.printStackTrace(new PrintStream(out));

            throw new StopException(e);

        }
    }

    public class BuiltInCmd extends Cmd {
        public BuiltInCmd(String name, Method method) {
            super(name, method);
        }

        @Override
        public void exec(Object impl, String[] args, PrintStream out) throws Throwable {
            super.exec(TelnetServer.this, args, out);
        }
    }

    public String help(String arg) {
        final StringBuilder sb = new StringBuilder();

        if (arg == null) {
            for (String s : cmds.keySet()) {
                sb.append(s).append("\n");
            }
        } else {
            final Cmd cmd = cmds.get(arg);
            if (cmd == null) {
                sb.append("Unkown command: ").append(arg);
            } else {
                final Method method = cmd.getMethod();

                sb.append(cmd.getName()).append(" ");

                final Class<?>[] types = method.getParameterTypes();
                for (Class<?> type : types) {
                    sb.append("<").append(type.getSimpleName().toLowerCase()).append(">").append(" ");
                }

                if (types.length == 0) {
                    sb.append("[no options]");
                }
            }
        }

        return sb.toString();
    }

    public void exit() throws StopException {
        throw new StopException();
    }

    public static class StopException extends Exception {
        public StopException() {
        }

        public StopException(Throwable cause) {
            super(cause);
        }
    }
}
