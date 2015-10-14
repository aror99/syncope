/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.cli.commands;

import javax.xml.ws.WebServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.cli.Command;
import org.apache.syncope.client.cli.Input;
import org.apache.syncope.client.cli.SyncopeServices;
import org.apache.syncope.client.cli.messages.UsageMessages;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.policy.AbstractPolicyTO;
import org.apache.syncope.common.lib.types.PolicyType;
import org.apache.syncope.common.rest.api.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "policy")
public class PolicyCommand extends AbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyCommand.class);

    private static final String HELP_MESSAGE = "Usage: policy [options]\n"
            + "  Options:\n"
            + "    --help \n"
            + "    --list-policy \n"
            + "       Syntax: --list-policy {POLICY-TYPE} \n"
            + "          Policy type: ACCOUNT / PASSWORD / SYNC / PUSH\n"
            + "    --read \n"
            + "       Syntax: --read {POLICY-ID} {POLICY-ID} [...]\n"
            + "    --delete \n"
            + "       Syntax: --delete {POLICY-ID} {POLICY-ID} [...]";

    @Override
    public void execute(final Input input) {
        LOG.debug("Option: {}", input.getOption());
        LOG.debug("Parameters:");
        for (final String parameter : input.getParameters()) {
            LOG.debug("   > " + parameter);
        }

        final String[] parameters = input.getParameters();

        if (StringUtils.isBlank(input.getOption())) {
            input.setOption(Options.HELP.getOptionName());
        }

        final PolicyService policyService = SyncopeServices.get(PolicyService.class);
        switch (Options.fromName(input.getOption())) {
            case LIST_POLICY:
                final String listPolicyErrorMessage = UsageMessages.optionCommandMessage(
                        "Usage: policy --list-policy {POLICY-TYPE}\n"
                        + "   Policy type: ACCOUNT / PASSWORD / SYNC / PUSH");
                if (parameters.length == 1) {
                    try {
                        for (final AbstractPolicyTO policyTO : policyService.list(PolicyType.valueOf(parameters[0]))) {
                            System.out.println(policyTO);
                        }
                    } catch (final SyncopeClientException ex) {
                        UsageMessages.printErrorMessage(ex.getMessage());
                    } catch (final IllegalArgumentException ex) {
                        UsageMessages.printErrorMessage(
                                "Error: " + parameters[0] + " isn't a valid policy type, try with:");
                        for (final PolicyType type : PolicyType.values()) {
                            System.out.println("  *** " + type.name());
                        }
                        System.out.println("");
                    }
                } else {
                    System.out.println(listPolicyErrorMessage);
                }
                break;
            case READ:
                final String readErrorMessage = UsageMessages.optionCommandMessage(
                        "Usage: policy --read {POLICY-ID} {POLICY-ID} [...]");
                if (parameters.length >= 1) {
                    for (final String parameter : parameters) {
                        try {
                            System.out.println(policyService.read(Long.valueOf(parameter)));
                        } catch (final NumberFormatException ex) {
                            UsageMessages.printErrorMessage(
                                    "Error reading " + parameter + ". It isn't a valid policy id");
                        } catch (final WebServiceException | SyncopeClientException ex) {
                            if (ex.getMessage().startsWith("NotFound")) {
                                UsageMessages.printErrorMessage("Policy " + parameter + " doesn't exists!");
                            } else {
                                UsageMessages.printErrorMessage(ex.getMessage());
                            }
                        }
                    }
                } else {
                    System.out.println(readErrorMessage);
                }
                break;
            case DELETE:
                final String deleteErrorMessage = UsageMessages.optionCommandMessage(
                        "Usage: policy --delete {POLICY-ID} {POLICY-ID} [...]");

                if (parameters.length >= 1) {
                    for (final String parameter : parameters) {
                        try {
                            policyService.delete(Long.valueOf(parameter));
                            System.out.println(" - Policy " + parameter + " deleted!");
                        } catch (final WebServiceException | SyncopeClientException ex) {
                            System.out.println("Error:");
                            if (ex.getMessage().startsWith("NotFound")) {
                                UsageMessages.printErrorMessage("Policy " + parameter + " doesn't exists!");
                            } else if (ex.getMessage().startsWith("DataIntegrityViolation")) {
                                UsageMessages.printErrorMessage("You cannot delete policy " + parameter);
                            } else {
                                UsageMessages.printErrorMessage(ex.getMessage());
                            }
                        } catch (final NumberFormatException ex) {
                            UsageMessages.printErrorMessage(
                                    "Error reading " + parameter + ". It isn't a valid policy id");
                        }
                    }
                } else {
                    System.out.println(deleteErrorMessage);
                }
                break;
            case HELP:
                System.out.println(HELP_MESSAGE);
                break;
            default:
                System.out.println(input.getOption() + " is not a valid option.");
                System.out.println("");
                System.out.println(HELP_MESSAGE);
        }
    }

    private enum Options {

        HELP("--help"),
        LIST_POLICY("--list-policy"),
        READ("--read"),
        DELETE("--delete");

        private final String optionName;

        private Options(final String optionName) {
            this.optionName = optionName;
        }

        public String getOptionName() {
            return optionName;
        }

        public boolean equalsOptionName(final String otherName) {
            return (otherName == null) ? false : optionName.equals(otherName);
        }

        public static Options fromName(final String name) {
            Options optionToReturn = HELP;
            for (final Options option : Options.values()) {
                if (option.equalsOptionName(name)) {
                    optionToReturn = option;
                }
            }
            return optionToReturn;
        }
    }

}
