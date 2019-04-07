/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.branch.buildstrategies.basic;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.LogTaskListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link BranchBuildStrategy} that builds change requests.
 *
 * @since 1.0.0
 */
public class ChangeRequestBuildStrategyImpl extends BranchBuildStrategy {

    /**
     * Our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ChangeRequestBuildStrategyImpl.class.getName());
    private final boolean ignoreTargetOnlyChanges;
    private final boolean ignoreUntrustedChanges;

    /**
     * Our constructor.
     *
     * @param ignoreTargetOnlyChanges {@code true} to ignore merge revision changes where the only difference is the
     *          target branch revision.
     * @deprecated use {@link #ChangeRequestBuildStrategyImpl(boolean, boolean)}
     * @since 1.2.0
     */
    @Deprecated
    public ChangeRequestBuildStrategyImpl(boolean ignoreTargetOnlyChanges) {
        this(ignoreTargetOnlyChanges, false);
    }

    /**
     * Our constructor.
     *
     * @param ignoreTargetOnlyChanges {@code true} to ignore merge revision changes where the only difference is
     *         the target branch revision.
     * @param ignoreUntrustedChanges {@code true} to check the trusted revision and ignore if different, which
     *         would have the effect of ignoring change requests that originate from an untrusted source.
     */
    @DataBoundConstructor
    public ChangeRequestBuildStrategyImpl(boolean ignoreTargetOnlyChanges, boolean ignoreUntrustedChanges) {
        this.ignoreTargetOnlyChanges = ignoreTargetOnlyChanges;
        this.ignoreUntrustedChanges = ignoreUntrustedChanges;
    }

    public boolean isIgnoreTargetOnlyChanges() {
        return ignoreTargetOnlyChanges;
    }

    public boolean isIgnoreUntrustedChanges() {
        return ignoreUntrustedChanges;
    }

    // TODO change overload to one that has the listener after branch-api is updated to enable listener pass-through
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(SCMHead head) {
        return head instanceof ChangeRequestSCMHead;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    @CheckForNull SCMRevision prevRevision) {
        if (ignoreTargetOnlyChanges
                && currRevision instanceof ChangeRequestSCMRevision
                && prevRevision instanceof ChangeRequestSCMRevision) {
            ChangeRequestSCMRevision<?> curr = (ChangeRequestSCMRevision<?>) currRevision;
            if (curr.isMerge() && curr.equivalent((ChangeRequestSCMRevision<?>) prevRevision)) {
                return false;
            }
        }
        try {
            if (ignoreUntrustedChanges && !currRevision.equals(source.getTrustedRevision(
                    currRevision,
                    new LogTaskListener(LOGGER, Level.INFO))
            )) {
                return false;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Could not determine trust status, assuming untrusted", e);
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChangeRequestBuildStrategyImpl that = (ChangeRequestBuildStrategyImpl) o;

        return ignoreTargetOnlyChanges == that.ignoreTargetOnlyChanges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (ignoreTargetOnlyChanges ? 1 : 0) + (ignoreUntrustedChanges ? 2 : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ChangeRequestBuildStrategyImpl{" +
                "ignoreTargetOnlyChanges=" + ignoreTargetOnlyChanges +
                "ignoreUntrustedChanges=" + ignoreUntrustedChanges +
                '}';
    }

    /**
     * Our descriptor.
     */
    @Symbol("buildChangeRequests")
    @Extension
    public static class DescriptorImpl extends BranchBuildStrategyDescriptor {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.ChangeRequestBuildStrategyImpl_displayName();
        }
    }
}
