package org.wiwi.cftestjenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.kohsuke.stapler.DataBoundConstructor;

import java.net.MalformedURLException;
import java.net.URL;

public class CFTestBuilder extends Builder {

    private final String target;
    private final String organization;
    private final String cloudSpace;
    private final String username;
    private final String password;
    private final String uri;

    @DataBoundConstructor
    public CFTestBuilder(String target, String organization, String cloudSpace,
                         String username, String password, String uri) {
        this.target = target;
        this.organization = organization;
        this.cloudSpace = cloudSpace;
        this.username = username;
        this.password = password;
        this.uri = uri;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            listener.getLogger().println("CF-test-jenkins-plugin");
            listener.getLogger().println("target: " + target);
            listener.getLogger().println("organization: " + organization);
            listener.getLogger().println("cloudSpace: " + cloudSpace);
            listener.getLogger().println("username: " + username);
            listener.getLogger().println("password: " + password);
            listener.getLogger().println("uri: " + uri);

            URL targetUrl = new URL(target);
            CloudCredentials credentials = new CloudCredentials(username, password);
            CloudFoundryClient client = new CloudFoundryClient(credentials, targetUrl, organization, cloudSpace);
            client.login();

            listener.getLogger().println("\nSpaces:");
            for (CloudSpace space : client.getSpaces()) {
                listener.getLogger().println(space.getName() + ":" + space.getOrganization().getName());
            }

            listener.getLogger().println("Applications:");
            for (CloudApplication app : client.getApplications()) {
                listener.getLogger().println(app.getName());
            }

            listener.getLogger().println("Services:");
            for (CloudService service : client.getServices()) {
                listener.getLogger().println(service.getName() + ":" + service.getLabel());
            }
            return true;
        } catch (MalformedURLException e) {
            listener.getLogger().println("The target URL is not valid: " + e.getMessage());
            return false;
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Push to CF";
        }
    }
}
