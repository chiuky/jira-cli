/*-
 * #%L
 * jira-cli
 *  
 * Copyright (C) 2019 László-Róbert, Albert (robert@albertlr.ro)
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ro.albertlr.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;

import java.util.Map;
import java.util.Set;

@Slf4j
public class CLI {

    public static final String ISSUE_E2E = "End-to-end Test";
    public static final String DEPENDS_ON_LINK = "Depends On";
    public static final String TESTED_BY_LINK = "Tests Writing";

    public static void main(String[] args) throws Exception {
        CommandLine cli = Params.cli(args);

        String jiraSourceKey = Params.getParameter(cli, Params.SOURCE_ARG);

        final Action.Name action = Action.Name.from(Params.getParameter(cli, Params.ACTION_ARG));

        try (final Jira jira = new Jira()) {
            switch (action) {
                case GET: {
                    Issue issue = action.execute(jira, jiraSourceKey);

                    IssueLogger.logIssue(log, issue);
                }
                break;
                case LINK: {
                    final String jiraTargetKey = Params.getParameter(cli, Params.TARGET_ARG);
                    final String linkType = Params.getParameter(cli, Params.LINK_TYPE_ARG);

                    action.execute(jira, jiraSourceKey, jiraTargetKey, linkType);
                }
                break;
                case GET_E2ES: {
                    boolean recursive = cli.hasOption("recursive");

                    Map<String, Set<String>> e2ea = action.execute(jira, jiraSourceKey, String.valueOf(recursive));

                    log.info("E2Es for {}", jiraSourceKey);
                    for (Map.Entry<String, Set<String>> listOfDependencies : e2ea.entrySet()) {
                        log.info("    {} -> {}", listOfDependencies.getKey(), listOfDependencies.getValue());
                    }
                }
                break;
                case ASSIGN_TO_ME: {
                    action.execute(jira, jiraSourceKey);

                    log.info("Successfully assigned {} to me", jiraSourceKey);
                }
                break;
                case CLONE: {
                    String cloneKey = action.execute(jira, jiraSourceKey);

                    log.info("Cloned {} to {}", jiraSourceKey, cloneKey);
                }
                break;
                case MOVE: {
                    String destProjectKey = Params.getParameter(cli, Params.PROJECT_ARG, "JVCLD");
                    String cloneKey = action.execute(jira, jiraSourceKey, destProjectKey);

                    log.info("Moved {} to {}", jiraSourceKey, cloneKey);
                }
                break;
                default:
                    Params.printUsage();
                    break;
            }
        }
    }


    private static void doGetIssue(Jira jira, String issueKey) {
        Issue issue = jira.loadIssue(issueKey);

        System.out.printf("GET issue: %s", issue);
    }


    private static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
        for (Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                return transition;
            }
        }
        return null;
    }
}
