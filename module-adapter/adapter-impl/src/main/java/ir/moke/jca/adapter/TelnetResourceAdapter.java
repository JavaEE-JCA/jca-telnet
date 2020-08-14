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

import ir.moke.jca.api.Command;
import ir.moke.jca.api.Prompt;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Connector(
        description = "Telnet ResourceAdapter",
        displayName = "Telnet ResourceAdapter",
        eisType = "Telnet Adapter",
        version = "1.0"
)
public class TelnetResourceAdapter implements javax.resource.spi.ResourceAdapter {

    private final Map<Integer, TelnetServer> activated = new HashMap<Integer, TelnetServer>();
    private static final int PORT = 2020;
    private Class<?> beanClass;
    private TelnetActivationSpec telnetActivationSpec;

    /**
     * Corresponds to the ra.xml <config-property>
     */

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
    }

    public void stop() {
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        telnetActivationSpec = (TelnetActivationSpec) activationSpec;
        beanClass = messageEndpointFactory.getEndpointClass() != null ? messageEndpointFactory.getEndpointClass() : telnetActivationSpec.getBeanClass();
        validate();

        final TelnetServer telnetServer = new TelnetServer(messageEndpointFactory, telnetActivationSpec, PORT);
        telnetServer.start();
        activated.put(PORT, telnetServer);
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        final TelnetActivationSpec telnetActivationSpec = (TelnetActivationSpec) activationSpec;

        final TelnetServer telnetServer = activated.remove(PORT);

        try {
            telnetServer.deactivate();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final MessageEndpoint endpoint = (MessageEndpoint) telnetServer.getListener();

        endpoint.release();
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }

    @Override
    public int hashCode() {
        return PORT;
    }

    public void validate() throws InvalidPropertyException {
        // Set Prompt
        final Prompt prompt = beanClass.getAnnotation(Prompt.class);
        if (prompt != null) {
            telnetActivationSpec.setPrompt(prompt.value());
        }

        // Get Commands
        final Method[] methods = beanClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Command.class)) {
                final Command command = method.getAnnotation(Command.class);
                telnetActivationSpec.addCmd(new Cmd(command.value(), method));
            }
        }

        // Validate
        if (telnetActivationSpec.getPrompt() == null || telnetActivationSpec.getPrompt().length() == 0) {
            telnetActivationSpec.setPrompt("prompt>");
        }
        if (telnetActivationSpec.getCmds().size() == 0) {
            throw new InvalidPropertyException("No @Command methods");
        }
    }

}
